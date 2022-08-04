/*

Changes were made to this file for inclusion in the wX program.

joshua.tee@gmail.com

 */

package joshuatee.wx.telecine

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.content.Context.MEDIA_PROJECTION_SERVICE
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Context.WINDOW_SERVICE
import android.content.Intent.ACTION_SEND
import android.content.Intent.ACTION_VIEW
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.graphics.PixelFormat.TRANSLUCENT
import android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION
import android.media.*
import android.media.MediaRecorder.OutputFormat.MPEG_4
import android.media.MediaRecorder.VideoEncoder.H264
import android.media.MediaRecorder.VideoSource.SURFACE
import android.os.Environment.DIRECTORY_MOVIES
import android.os.Environment.DIRECTORY_DCIM
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import android.widget.Toast.LENGTH_SHORT
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.radar.WXGLRender
import joshuatee.wx.radar.WXGLSurfaceView
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.fingerdraw.DrawLineView
import joshuatee.wx.fingerdraw.DrawView
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.notifications.UtilityNotification
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.util.FileProvider

internal class RecordingSession(
    private val context: Context,
    private val listener: Listener,
    private val resultCode: Int,
    private val data: Intent,
    private var showDistanceTool: Boolean,
    private var showRecordingTools: Boolean
) {
    private val mainThread = Handler(Looper.getMainLooper())
    private val picturesDir: File? = context.getExternalFilesDir(DIRECTORY_DCIM)
    private val moviesDir: File? = context.getExternalFilesDir(DIRECTORY_MOVIES)
    private val videoFileFormat = SimpleDateFormat("'${GlobalVariables.packageNameFileNameAsString}'yyyyMMddHHmmss'.mp4'", Locale.US)
    private val audioFileFormat = SimpleDateFormat("'${GlobalVariables.packageNameFileNameAsString}'yyyyMMddHHmmss'.jpeg'", Locale.US)
    private val notificationManager: NotificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    private val windowManager: WindowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
    private var projectionManager: MediaProjectionManager? = null
    private var overlayView: OverlayView? = null
    private var flashView: FlashView? = null
    private var recorder: MediaRecorder? = null
    private var imageReader: ImageReader? = null
    private var projection: MediaProjection? = null
    private var display: VirtualDisplay? = null
    private var outputFile: String? = null
    private var running = false
    private var drawToolActive = false
    private var distanceToolActive = false
    private var drawObject: DrawView? = null
    private var distanceToolObject: DrawLineView? = null

    private val recordingInfo: RecordingInfo
        get() {
            val displayMetrics = DisplayMetrics()
            val wm = context.getSystemService(WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getRealMetrics(displayMetrics)
            val displayWidth = displayMetrics.widthPixels
            val displayHeight = displayMetrics.heightPixels
            val displayDensity = displayMetrics.densityDpi
            val configuration = context.resources.configuration
            val isLandscape = configuration.orientation == ORIENTATION_LANDSCAPE
            val camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
            val cameraWidth = camcorderProfile?.videoFrameWidth ?: -1
            val cameraHeight = camcorderProfile?.videoFrameHeight ?: -1
            val cameraFrameRate = camcorderProfile?.videoFrameRate ?: 30
            val sizePercentage = UIPreferences.telecineVideoSizePercentage
            return calculateRecordingInfo(
                displayWidth, displayHeight, displayDensity, isLandscape,
                cameraWidth, cameraHeight, cameraFrameRate, sizePercentage
            )
        }

    private val screenshotInfo: RecordingInfo
        get() {
            val displayMetrics = DisplayMetrics()
            val wm = context.getSystemService(WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getRealMetrics(displayMetrics)
            val displayWidth = displayMetrics.widthPixels
            val displayHeight = displayMetrics.heightPixels
            val displayDensity = displayMetrics.densityDpi
            return RecordingInfo(displayWidth, displayHeight, 1, displayDensity)
        }

    internal interface Listener {
        /** Invoked immediately prior to the start of recording.  */
        fun onStart()

        /** Invoked immediately after the end of recording.  */
        fun onStop()

        /** Invoked after all work for this session has completed.  */
        fun onEnd()
    }

    init {
        if (showRecordingTools) {
            projectionManager = context.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        }
    }

    fun showOverlay() {
        val overlayListener = object : OverlayView.Listener {
            override fun onCancel() { cancelOverlay() }

            override fun onStart() { startRecording() }

            override fun onStop() {
                stopRecording()
            }

            override fun onScreenshot() { overlayView!!.animate().alpha(0f).setDuration(0).withEndAction { takeScreenshot() } }

            override fun onDrawTool() { addDrawTool() }

            override fun onDistanceTool() { addDistanceTool() }
        }
        overlayView = OverlayView.create(context, overlayListener, showDistanceTool, showRecordingTools)
        windowManager.addView(overlayView, OverlayView.createLayoutParams(context))
    }

    private fun hideOverlay() {
        if (drawObject != null) {
            windowManager.removeView(drawObject)
            drawObject = null
        }
        if (distanceToolObject != null) {
            windowManager.removeView(distanceToolObject)
            distanceToolObject = null
        }
        if (overlayView != null) {
            windowManager.removeView(overlayView)
            overlayView = null
        }
    }

    private fun cancelOverlay() {
        hideOverlay()
        listener.onEnd()
    }

    private fun startRecording() {
        if (!moviesDir!!.exists() && !moviesDir.mkdirs()) {
            Toast.makeText(context, "Unable to create output directory.\nCannot record screen.", LENGTH_SHORT).show()
            return
        }
        val recordingInfo = recordingInfo
        recorder = MediaRecorder()
        // recorder = MediaRecorder(context)
        recorder!!.setVideoSource(SURFACE)
        recorder!!.setOutputFormat(MPEG_4)
        recorder!!.setVideoFrameRate(recordingInfo.frameRate)
        recorder!!.setVideoEncoder(H264)
        recorder!!.setVideoSize(recordingInfo.width, recordingInfo.height)
        recorder!!.setVideoEncodingBitRate(8 * 1000 * 1000)
        val outputName = videoFileFormat.format(Date())
        outputFile = File(moviesDir, outputName).absolutePath
        recorder!!.setOutputFile(outputFile)
        try {
            recorder!!.prepare()
        } catch (e: IOException) {
            throw RuntimeException("Unable to prepare MediaRecorder.", e)
        }

//        projection = projectionManager!!.getMediaProjection(resultCode, data)
        // delay needed because getMediaProjection() throws an error if it's called too soon
        Handler(Looper.getMainLooper()).postDelayed({
            projection = projectionManager!!.getMediaProjection(resultCode, data)

            val surface = recorder!!.surface
            display = projection!!.createVirtualDisplay(
                    DISPLAY_NAME,
                    recordingInfo.width,
                    recordingInfo.height,
                    recordingInfo.density,
                    VIRTUAL_DISPLAY_FLAG_PRESENTATION,
                    surface,
                    null,
                    null
            )
            recorder!!.start()
            running = true
            listener.onStart()

        }, 1000)


    }

    private fun stopRecording() {
        running = false
        showOverlay()
        var propagate = false
        try {
            // Stop the projection in order to flush everything to the recorder.
            projection!!.stop()
            // Stop the recorder which writes the contents to the file.
            try {
                recorder!!.stop()
            } catch (e: RuntimeException) {
            } finally {
                if (recorder != null)
                    recorder!!.release()
                recorder = null
            }
            propagate = true
        } finally {
            try {
                // Ensure the listener can tear down its resources regardless if stopping crashes.
                listener.onStop()
            } catch (e: RuntimeException) {
                if (propagate) {
                    throw e // Only allow listener exceptions to propagate if stopped successfully.
                }
            }
        }
        if (recorder != null) {
            recorder!!.release()
        }
        display!!.release()
        val uri = FileProvider.getUriForFile(context, "${GlobalVariables.packageNameAsString}.fileprovider", File(outputFile!!))
        mainThread.post {showNotification(uri, null)}
    }

    private fun addDrawTool() {
        val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        if (!drawToolActive) {
            val params = WindowManager.LayoutParams(
                MyApplication.dm.widthPixels,
                MyApplication.dm.heightPixels - UtilityUI.statusBarHeight(context),
                layoutFlag,
                FLAG_NOT_FOCUSABLE or FLAG_NOT_TOUCH_MODAL or FLAG_LAYOUT_NO_LIMITS,
                TRANSLUCENT
            )
            params.gravity = Gravity.BOTTOM
            drawObject = DrawView(context)
            windowManager.addView(drawObject, params)
            drawToolActive = true
        } else {
            windowManager.removeView(drawObject)
            drawObject = null
            drawToolActive = false
        }
    }

    private fun addDistanceTool() {
        val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
        }
        if (!distanceToolActive) {
            val params = WindowManager.LayoutParams(
                MyApplication.dm.widthPixels,
                MyApplication.dm.heightPixels - UtilityUI.statusBarHeight(context) * 2,
                layoutFlag,
                FLAG_NOT_FOCUSABLE or FLAG_NOT_TOUCH_MODAL or FLAG_LAYOUT_NO_LIMITS,
                TRANSLUCENT
            )
            params.gravity = Gravity.BOTTOM
            distanceToolObject = DrawLineView(
                context,
                WXGLRender.ridGlobal, WXGLSurfaceView.scaleFactorGlobal,
                WXGLRender.positionXGlobal,
                WXGLRender.positionYGlobal,
                WXGLRender.ortIntGlobal.toFloat(),
                WXGLRender.oneDegreeScaleFactorGlobal
            )
            windowManager.addView(distanceToolObject, params)
            distanceToolActive = true
        } else {
            windowManager.removeView(distanceToolObject)
            distanceToolObject = null
            distanceToolActive = false
        }
    }

    @SuppressLint("WrongConstant")
    private fun takeScreenshot() {
        val recordingInfo = screenshotInfo
        val outputName = audioFileFormat.format(Date())
        outputFile = File(picturesDir, outputName).absolutePath

//        projection = projectionManager!!.getMediaProjection(resultCode, data)
        // delay needed because getMediaProjection() throws an error if it's called too soon
        Handler(Looper.getMainLooper()).postDelayed({
            projection = projectionManager!!.getMediaProjection(resultCode, data)
            imageReader = ImageReader.newInstance(recordingInfo.width, recordingInfo.height, PixelFormat.RGBA_8888, 2)
            val surface = imageReader!!.surface
            display = projection!!.createVirtualDisplay(
                    DISPLAY_NAME,
                    recordingInfo.width,
                    recordingInfo.height,
                    recordingInfo.density,
                    VIRTUAL_DISPLAY_FLAG_PRESENTATION,
                    surface,
                    null,
                    null
            )
            imageReader!!.setOnImageAvailableListener({
                var image: Image? = null
                var fos: FileOutputStream? = null
                var bitmap: Bitmap? = null
                var croppedBitmap: Bitmap? = null
                try {
                    image = imageReader!!.acquireLatestImage()
                    if (image != null) {
                        val flashViewListener = object : FlashView.Listener {
                            override fun onFlashComplete() {
                                if (flashView != null) {
                                    windowManager.removeView(flashView)
                                    flashView = null
                                }
                            }
                        }
                        flashView = FlashView.create(context, flashViewListener)
                        windowManager.addView(flashView, FlashView.createLayoutParams())
                        val planes = image.planes
                        val buffer = planes[0].buffer
                        val pixelStride = planes[0].pixelStride
                        val rowStride = planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * recordingInfo.width
                        // create bitmap
                        bitmap = Bitmap.createBitmap(recordingInfo.width + rowPadding / pixelStride, recordingInfo.height, Bitmap.Config.ARGB_8888)
                        bitmap!!.copyPixelsFromBuffer(buffer)
                        //Trimming the bitmap to the w/h of the screen. For some reason, image reader adds more pixels to width.
                        croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, recordingInfo.width, recordingInfo.height)
                        bitmap.recycle()
                        bitmap = null
                        // write bitmap to a file
                        fos = FileOutputStream(outputFile!!)
                        croppedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                        //UtilityLog.d("wx", outputFile.toString())
                        val uri = FileProvider.getUriForFile(context, "${GlobalVariables.packageNameAsString}.fileprovider", File(outputFile!!))
                        showScreenshotNotification(uri, null)
                    }
                } catch (e: Exception) {
                    UtilityLog.handleException(e)
                } finally {
                    if (fos != null) {
                        try {
                            fos.close()
                        } catch (ioe: IOException) {
                            UtilityLog.handleException(ioe)
                        }
                    }
                    bitmap?.recycle()
                    croppedBitmap?.recycle()
                    image?.close()
                    imageReader!!.close()
                    display!!.release()
                    projection!!.stop()
                    overlayView!!.animate().alpha(1f).duration = 0
                }
            }, null)
        }, 1000)
    }

    private fun showNotification(uri: Uri?, bitmap: Bitmap?) {
        UtilityNotification.initChannels(context)
        val requestID = ObjectDateTime.currentTimeMillis().toInt()
        val viewIntent = Intent(ACTION_VIEW, uri)
        val pendingViewIntent = PendingIntent.getActivity(context, requestID, viewIntent, FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        var shareIntent = Intent(ACTION_SEND)
        shareIntent.type = MIME_TYPE_RECORDING
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.data = uri
        shareIntent = Intent.createChooser(shareIntent, null)
        val pendingShareIntent = PendingIntent.getActivity(context, requestID, shareIntent, FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val title = context.getText(R.string.notification_captured_title)
        val subtitle = context.getText(R.string.notification_captured_subtitle)
        val share = context.getText(R.string.notification_captured_share)
        val builder: NotificationCompat.Builder?
        val actionShare = NotificationCompat.Action.Builder(R.drawable.ic_share_24dp, share, pendingShareIntent).build()
        builder = NotificationCompat.Builder(context, UtilityNotification.notiChannelStrNoSound)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setWhen(ObjectDateTime.currentTimeMillis())
            .setShowWhen(true)
            .setSmallIcon(R.drawable.ic_videocam_24dp)
            .setColor(UIPreferences.colorNotif)
            .setContentIntent(pendingViewIntent)
            .setAutoCancel(true)
            .addAction(actionShare)
        if (bitmap != null) {
            builder.setLargeIcon(createSquareBitmap(bitmap))
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .setBigContentTitle(title)
                        .setSummaryText(subtitle)
                        .bigPicture(bitmap)
                )
        }
        notificationManager.notify(uri!!.toString(), NOTIFICATION_ID, builder.build())
        if (bitmap != null) {
            listener.onEnd()
            return
        }
        object : AsyncTask<Void, Void, Bitmap>() {
            override fun doInBackground(vararg none: Void): Bitmap? {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                return retriever.frameAtTime
            }

            override fun onPostExecute(bitmap: Bitmap?) {
                if (bitmap != null) {
                    showNotification(uri, bitmap)
                } else {
                    listener.onEnd()
                }
            }
        }.execute()
    }

    private fun showScreenshotNotification(uri: Uri, bitmap: Bitmap?) {
        UtilityNotification.initChannels(context)
        val requestID = ObjectDateTime.currentTimeMillis().toInt()
        val viewIntent = Intent(ACTION_VIEW, uri)
        val pendingViewIntent = PendingIntent.getActivity(context, requestID, viewIntent, FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        var shareIntent = Intent(ACTION_SEND)
        shareIntent.type = MIME_TYPE_SCREENSHOT
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.data = uri
        shareIntent = Intent.createChooser(shareIntent, null)
        val pendingShareIntent = PendingIntent.getActivity(context, requestID, shareIntent, FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val title = context.getText(R.string.notification_screenshot_captured_title)
        val subtitle = context.getText(R.string.notification_screenshot_captured_subtitle)
        val share = context.getText(R.string.notification_captured_share)
        val actionShare = NotificationCompat.Action.Builder(R.drawable.ic_share_24dp, share, pendingShareIntent).build()
        val builder = NotificationCompat.Builder(context, UtilityNotification.notiChannelStrNoSound)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setWhen(ObjectDateTime.currentTimeMillis())
            .setShowWhen(true)
            .setSmallIcon(R.drawable.ic_photo_camera_24dp)
            .setColor(UIPreferences.colorNotif)
            .setContentIntent(pendingViewIntent)
            .setAutoCancel(true)
            .addAction(actionShare)
        if (bitmap != null) {
            builder.setLargeIcon(createSquareBitmap(bitmap))
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .setBigContentTitle(title)
                        .setSummaryText(subtitle)
                        .bigPicture(bitmap)
                )
        }
        notificationManager.notify(uri.toString(), NOTIFICATION_ID_SCREENSHOT, builder.build())
        if (bitmap != null) {
            listener.onEnd()
            return
        }
        object : AsyncTask<Void, Void, Bitmap>() {
            override fun doInBackground(vararg none: Void): Bitmap? {
                var bitmapLocal: Bitmap? = null
                try {
                    bitmapLocal = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                } catch (e: Exception) {
                }
                return bitmapLocal
            }

            override fun onPostExecute(bitmap: Bitmap?) {
                if (bitmap != null) {
                    showScreenshotNotification(uri, bitmap)
                } else {
                    listener.onEnd()
                }
            }
        }.execute()
    }

    private class RecordingInfo(
        val width: Int,
        val height: Int,
        val frameRate: Int,
        val density: Int
    )

    fun destroy() { if (running) stopRecording() }

    class DeleteRecordingBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val uriData = intent.dataString
            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(uriData, NOTIFICATION_ID)
            notificationManager.cancel(uriData, NOTIFICATION_ID_SCREENSHOT)
            object : AsyncTask<Void, Void, Void>() {
                override fun doInBackground(vararg none: Void): Void? { return null }
            }.execute()
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 522592
        private const val NOTIFICATION_ID_SCREENSHOT = 522593
        private const val DISPLAY_NAME = "wx_telecine"
        private const val MIME_TYPE_RECORDING = "video/mp4"
        private const val MIME_TYPE_SCREENSHOT = "image/jpeg"

        private fun calculateRecordingInfo(
            displayWidth: Int,
            displayHeight: Int,
            displayDensity: Int,
            isLandscapeDevice: Boolean,
            cameraWidth: Int,
            cameraHeight: Int,
            cameraFrameRate: Int,
            sizePercentage: Int
        ): RecordingInfo {
            // Scale the display size before any maximum size calculations.
            val displayWidthLocal = displayWidth * sizePercentage / 100
            val displayHeightLocal = displayHeight * sizePercentage / 100
            if (cameraWidth == -1 && cameraHeight == -1) {
                // No cameras. Fall back to the display size.
                return RecordingInfo(displayWidthLocal, displayHeightLocal, cameraFrameRate, displayDensity)
            }
            var frameWidth = if (isLandscapeDevice) cameraWidth else cameraHeight
            var frameHeight = if (isLandscapeDevice) cameraHeight else cameraWidth
            if (frameWidth >= displayWidthLocal && frameHeight >= displayHeightLocal) {
                // Frame can hold the entire display. Use exact values.
                return RecordingInfo(displayWidthLocal, displayHeightLocal, cameraFrameRate, displayDensity)
            }
            // Calculate new width or height to preserve aspect ratio.
            if (isLandscapeDevice) {
                frameWidth = displayWidthLocal * frameHeight / displayHeightLocal
            } else {
                frameHeight = displayHeightLocal * frameWidth / displayWidthLocal
            }
            return RecordingInfo(frameWidth, frameHeight, cameraFrameRate, displayDensity)
        }

        private fun createSquareBitmap(bitmap: Bitmap): Bitmap {
            var x = 0
            var y = 0
            var width = bitmap.width
            var height = bitmap.height
            if (width > height) {
                x = (width - height) / 2
                width = height
            } else {
                y = (height - width) / 2
                height = width
            }
            return Bitmap.createBitmap(bitmap, x, y, width, height, null, true)
        }
    }
}

/*

Changes were made to this file for inclusion in the wX program.

joshua.tee@gmail.com

 */

package joshuatee.wx.telecine

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.provider.Settings
import androidx.core.content.ContextCompat

import joshuatee.wx.MyApplication
import joshuatee.wx.R

import androidx.core.app.NotificationCompat
import joshuatee.wx.notifications.UtilityNotification

class TelecineService : Service() {

    private var running = false
    private var recordingSession: RecordingSession? = null

    private val listener = object : RecordingSession.Listener {
        override fun onStart() {
            if (MyApplication.telecineSwitchShowTouches) {
                Settings.System.putInt(MyApplication.contentResolverLocal, SHOW_TOUCHES, 1)
            }
            if (!MyApplication.telecineSwitchRecordingNotification) {
                return
            }
            val context = applicationContext
            val title = context.getString(R.string.notification_recording_title)
            val subtitle = context.getString(R.string.notification_recording_subtitle)
            var notification: Notification? = null
            if (android.os.Build.VERSION.SDK_INT > 20) {
                notification = NotificationCompat.Builder(context, UtilityNotification.notiChannelStrNoSound)
                        .setContentTitle(title)
                        .setContentText(subtitle)
                        .setSmallIcon(R.drawable.ic_videocam_24dp)
                        .setColor(ContextCompat.getColor(context, R.color.primary_normal))
                        .setAutoCancel(true)
                        .build()
            }
            startForeground(NOTIFICATION_ID, notification)
        }

        override fun onStop() {
            if (MyApplication.telecineSwitchShowTouches) {
                Settings.System.putInt(MyApplication.contentResolverLocal, SHOW_TOUCHES, 0)
            }
        }

        override fun onEnd() {
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (running) {
            return Service.START_NOT_STICKY
        }
        running = true
        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
        val data = intent.getParcelableExtra<Intent>(EXTRA_DATA)
        if (resultCode == 0 || data == null) {
            throw IllegalStateException("Result code or data missing.")
        }
        val showDistanceTool = intent.getStringExtra("show_distance_tool")
        val showRecordingTools = intent.getStringExtra("show_recording_tools")
        recordingSession = RecordingSession(this, listener, resultCode, data,
                showDistanceTool == "true",
                showRecordingTools == "true")
        recordingSession!!.showOverlay()
        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        recordingSession!!.destroy()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        throw AssertionError("Not supported.")
    }

    companion object {
        private const val EXTRA_RESULT_CODE = "result-code"
        private const val EXTRA_DATA = "data"
        private const val NOTIFICATION_ID = 99118822
        private const val SHOW_TOUCHES = "show_touches"

        fun newIntent(context: Context, resultCode: Int, data: Intent?): Intent {
            val intent = Intent(context, TelecineService::class.java)
            intent.putExtra(EXTRA_RESULT_CODE, resultCode)
            intent.putExtra(EXTRA_DATA, data)
            return intent
        }
    }
}

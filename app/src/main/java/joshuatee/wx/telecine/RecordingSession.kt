/*

Changes were made to this file for inclusion in the wX program.

joshua.tee@gmail.com

 */

package joshuatee.wx.telecine

import android.app.Activity
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import android.content.Context.WINDOW_SERVICE
import android.graphics.PixelFormat.TRANSLUCENT
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import joshuatee.wx.MyApplication
import joshuatee.wx.radar.NexradRenderSurfaceView
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.fingerdraw.DrawLineView
import joshuatee.wx.fingerdraw.DrawView
import joshuatee.wx.radar.NexradRenderState

internal class RecordingSession(private val context: Activity) {
    private val windowManager: WindowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
    private var overlayView: OverlayView? = null
    private var drawToolActive = false
    private var distanceToolActive = false
    private var drawObject: DrawView? = null
    private var distanceToolObject: DrawLineView? = null

    fun showOverlay() {
        val overlayListener = object : OverlayView.Listener {
            override fun onCancel() {
                cancelOverlay()
            }

            override fun onDrawTool() {
                addDrawTool()
            }

            override fun onDistanceTool() {
                addDistanceTool()
            }
        }
        overlayView = OverlayView.create(context, overlayListener)
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
            distanceToolObject = DrawLineView(context,
                    NexradRenderState.ridGlobal, NexradRenderSurfaceView.scaleFactorGlobal,
                    NexradRenderState.positionXGlobal,
                    NexradRenderState.positionYGlobal,
                    NexradRenderState.ORT_INT_GLOBAL,
                    NexradRenderState.oneDegreeScaleFactorGlobal
            )
            windowManager.addView(distanceToolObject, params)
            distanceToolActive = true
        } else {
            windowManager.removeView(distanceToolObject)
            distanceToolObject = null
            distanceToolActive = false
        }
    }
}

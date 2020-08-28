/*

Changes were made to this file for inclusion in the wX program.

joshua.tee@gmail.com

 */

package joshuatee.wx.telecine

import android.content.Context
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout

import joshuatee.wx.R

import android.graphics.PixelFormat.TRANSLUCENT
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import android.os.Build

internal class FlashView private constructor(context: Context, private val listener: Listener) : FrameLayout(context) {

    init { inflate(context, R.layout.telecine_flash_view, this) }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animate().alpha(100f).setDuration(200).withEndAction { listener.onFlashComplete() }.interpolator = DecelerateInterpolator()
    }

    internal interface Listener {
        /** Called when flash animation has completed.  */
        fun onFlashComplete()
    }

    companion object {

        fun create(context: Context, listener: Listener) = FlashView(context, listener)

        fun createLayoutParams(): WindowManager.LayoutParams {
            val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
            }
            return WindowManager.LayoutParams(
                MATCH_PARENT, MATCH_PARENT, layoutFlag, FLAG_NOT_FOCUSABLE
                        or FLAG_NOT_TOUCH_MODAL
                        or FLAG_LAYOUT_NO_LIMITS
                        or FLAG_LAYOUT_INSET_DECOR
                        or FLAG_LAYOUT_IN_SCREEN, TRANSLUCENT
            )
        }
    }
}

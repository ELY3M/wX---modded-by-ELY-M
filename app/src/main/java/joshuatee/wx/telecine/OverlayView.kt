/*

Changes were made to this file for inclusion in the wX program.

joshua.tee@gmail.com

 */
//modded by ELY M. - changed from yellow to white color for icons.  

package joshuatee.wx.telecine

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import joshuatee.wx.R
import joshuatee.wx.util.UtilityImg
import java.util.Locale
import android.graphics.PixelFormat.TRANSLUCENT
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import androidx.core.text.layoutDirection

@SuppressLint("ViewConstructor")
internal class OverlayView private constructor(
    context: Context,
    private val listener: Listener,
) : FrameLayout(context), View.OnClickListener {

    private val distanceToolView: View
    private val drawToolView: View
    private var animationWidth = 0

    internal interface Listener {
        /** Called when cancel is clicked. This view is unusable once this callback is invoked.  */
        fun onCancel()

        /** Called when screenshot is clicked. This view will hide itself completely before invoking
         * this callback. It will reappear once the screenshot has been saved.
         */

        fun onDrawTool()

        fun onDistanceTool()

        /** Called when screenshot is clicked. This view will hide itself completely before invoking
         * this callback. It will reappear once the screenshot has been saved.
         */

    }

    init {
        inflate(context, R.layout.telecine_overlay_jellybean_view, this)
        distanceToolView = findViewById(R.id.record_overlay_distance_tool)
        drawToolView = findViewById(R.id.record_overlay_drawtool)
        val cancelView: View = findViewById(R.id.record_overlay_cancel)
        animationWidth = R.dimen.overlay_jellybean_width
        distanceToolView.setOnClickListener(this)
        drawToolView.setOnClickListener(this)
        cancelView.setOnClickListener(this)
        distanceToolView.visibility = VISIBLE
        drawToolView.background = UtilityImg.bitmapToLayerDrawable(
                context,
                UtilityImg.vectorDrawableToBitmap(context, R.drawable.ic_edit_24dp, Color.WHITE)
        )
        distanceToolView.background = UtilityImg.bitmapToLayerDrawable(
                context,
                UtilityImg.vectorDrawableToBitmap(context, R.drawable.ic_adjust_24dp, Color.WHITE)
        )
        cancelView.background = UtilityImg.bitmapToLayerDrawable(
                context,
                UtilityImg.vectorDrawableToBitmap(context, R.drawable.ic_clear_24dp, Color.WHITE)
        )
        if (Locale.getDefault().layoutDirection == LAYOUT_DIRECTION_RTL) {
            animationWidth =
                -animationWidth // Account for animating in from the other side of screen.
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        translationX = animationWidth.toFloat()
        animate().translationX(0f).setDuration(DURATION_ENTER_EXIT.toLong()).interpolator =
            DecelerateInterpolator()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.record_overlay_cancel -> animate().translationX(animationWidth.toFloat())
                .setDuration(DURATION_ENTER_EXIT.toLong())
                .setInterpolator(AccelerateInterpolator())
                .withEndAction { listener.onCancel() }

            R.id.record_overlay_drawtool -> {
                drawToolView.isActivated = !drawToolView.isActivated
                listener.onDrawTool()
            }

            R.id.record_overlay_distance_tool -> {
                distanceToolView.isActivated = !distanceToolView.isActivated
                listener.onDistanceTool()
            }
        }
    }

    companion object {
        private const val DURATION_ENTER_EXIT = 300

        fun create(
            context: Context,
            listener: Listener,
        ): OverlayView = OverlayView(context, listener)

        fun createLayoutParams(context: Context): WindowManager.LayoutParams {
            val res = context.resources
            val width = res.getDimensionPixelSize(R.dimen.overlay_width)
            val height = res.getDimensionPixelSize(R.dimen.overlay_height_m)
            val layoutFlag: Int =
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            val params = WindowManager.LayoutParams(
                width,
                height,
                layoutFlag,
                FLAG_NOT_FOCUSABLE or FLAG_NOT_TOUCH_MODAL or FLAG_LAYOUT_NO_LIMITS,
                TRANSLUCENT
            )
            params.gravity = Gravity.TOP or gravityEndLocaleHack()
            return params
        }

        private fun gravityEndLocaleHack(): Int {
            val direction = Locale.getDefault().layoutDirection
            return if (direction == LAYOUT_DIRECTION_RTL) Gravity.START else Gravity.END
        }
    }
}

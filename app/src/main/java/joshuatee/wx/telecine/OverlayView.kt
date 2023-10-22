/*

Changes were made to this file for inclusion in the wX program.

joshua.tee@gmail.com

 */

package joshuatee.wx.telecine

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import joshuatee.wx.R
import joshuatee.wx.util.UtilityImg
import java.util.Locale
import android.graphics.PixelFormat.TRANSLUCENT
import android.text.TextUtils.getLayoutDirectionFromLocale
import android.view.ViewAnimationUtils.createCircularReveal
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
import android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
import joshuatee.wx.settings.UIPreferences

internal class OverlayView private constructor(
        context: Context,
        private val listener: Listener,
        showDistanceTool: Boolean,
        showRecordingTools: Boolean
) : FrameLayout(context), View.OnClickListener {

    private val distanceToolView: View
    private val drawToolView: View
    private val buttonsView: View
    private val startView: View
    private val stopView: View
    private val recordingView: TextView
    private var animationWidth = 0

    internal interface Listener {
        /** Called when cancel is clicked. This view is unusable once this callback is invoked.  */
        fun onCancel()

        /**
         * Called when start is clicked and it is appropriate to start recording. This view will hide
         * itself completely before invoking this callback.
         */
        fun onStart()

        /** Called when stop is clicked. This view is unusable once this callback is invoked.  */
        fun onStop()

        fun onScreenshot()

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
        if (showRecordingTools)
            View.inflate(context, R.layout.telecine_overlay_view, this)
        else
            View.inflate(context, R.layout.telecine_overlay_jellybean_view, this)
        distanceToolView = findViewById(R.id.record_overlay_distancetool)
        drawToolView = findViewById(R.id.record_overlay_drawtool)
        buttonsView = findViewById(R.id.record_overlay_buttons)
        val cancelView: View = findViewById(R.id.record_overlay_cancel)
        startView = findViewById(R.id.record_overlay_start)
        stopView = findViewById(R.id.record_overlay_stop)
        val screenshotView: View = findViewById(R.id.record_overlay_screenshot)
        recordingView = findViewById(R.id.record_overlay_recording)
        animationWidth = if (showRecordingTools) R.dimen.overlay_width else R.dimen.overlay_jellybean_width
        distanceToolView.setOnClickListener(this)
        drawToolView.setOnClickListener(this)
        screenshotView.setOnClickListener(this)
        cancelView.setOnClickListener(this)
        startView.setOnClickListener(this)
        if (!showDistanceTool) {
            distanceToolView.visibility = View.GONE
        }
        if (!showRecordingTools) {
            screenshotView.visibility = View.GONE
            startView.visibility = View.GONE
            distanceToolView.visibility = View.VISIBLE
            drawToolView.background = UtilityImg.bitmapToLayerDrawable(
                    context,
                    UtilityImg.vectorDrawableToBitmap(context, R.drawable.ic_edit_24dp, Color.YELLOW)
            )
            distanceToolView.background = UtilityImg.bitmapToLayerDrawable(
                    context,
                    UtilityImg.vectorDrawableToBitmap(context, R.drawable.ic_adjust_24dp, Color.YELLOW)
            )
            cancelView.background = UtilityImg.bitmapToLayerDrawable(
                    context,
                    UtilityImg.vectorDrawableToBitmap(context, R.drawable.ic_clear_24dp, Color.YELLOW)
            )
        }
        if (getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL) {
            animationWidth = -animationWidth // Account for animating in from the other side of screen.
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        translationX = animationWidth.toFloat()
        animate().translationX(0f).setDuration(DURATION_ENTER_EXIT.toLong()).interpolator = DecelerateInterpolator()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.record_overlay_start -> {
                recordingView.visibility = View.VISIBLE
                val centerX = (startView.x + startView.width / 2).toInt()
                val centerY = (startView.y + startView.height / 2).toInt()
                val reveal = createCircularReveal(recordingView, centerX, centerY, 0f, width / 2f)
                reveal.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        buttonsView.visibility = View.GONE
                    }
                })
                reveal.start()
                postDelayed(
                        { if (UIPreferences.telecineSwitchShowCountdown) showCountDown() else countdownComplete() },
                        (if (UIPreferences.telecineSwitchShowCountdown) COUNTDOWN_DELAY else NON_COUNTDOWN_DELAY).toLong()
                )
            }

            R.id.record_overlay_cancel -> animate().translationX(animationWidth.toFloat())
                    .setDuration(DURATION_ENTER_EXIT.toLong())
                    .setInterpolator(AccelerateInterpolator())
                    .withEndAction { listener.onCancel() }

            R.id.record_overlay_screenshot -> listener.onScreenshot()
            R.id.record_overlay_drawtool -> {
                drawToolView.isActivated = !drawToolView.isActivated
                listener.onDrawTool()
            }

            R.id.record_overlay_distancetool -> {
                distanceToolView.isActivated = !distanceToolView.isActivated
                listener.onDistanceTool()
            }
        }
    }

    private fun startRecording() {
        recordingView.visibility = View.INVISIBLE
        stopView.visibility = View.VISIBLE
        stopView.setOnClickListener { listener.onStop() }
        listener.onStart()
    }

    private fun showCountDown() {
        val countdown = resources.getStringArray(R.array.countdown)
        countdown(countdown, 0)
    }

    private fun countdownComplete() {
        recordingView.animate().alpha(0f).setDuration(COUNTDOWN_DELAY.toLong()).withEndAction { startRecording() }
    }

    private fun countdown(countdownArr: Array<String>, index: Int) {
        postDelayed({
            recordingView.text = countdownArr[index]
            if (index < countdownArr.lastIndex) countdown(countdownArr, index + 1) else countdownComplete()
        }, COUNTDOWN_DELAY.toLong())
    }

    companion object {

        private const val COUNTDOWN_DELAY = 1000
        private const val NON_COUNTDOWN_DELAY = 500
        private const val DURATION_ENTER_EXIT = 300

        fun create(
                context: Context,
                listener: Listener,
                showDistanceTool: Boolean,
                showRecordingTools: Boolean
        ): OverlayView = OverlayView(context, listener, showDistanceTool, showRecordingTools)

        fun createLayoutParams(context: Context): WindowManager.LayoutParams {
            val res = context.resources
            val width = res.getDimensionPixelSize(R.dimen.overlay_width)
            val height = res.getDimensionPixelSize(R.dimen.overlay_height_m)
            val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR
            }
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
            val direction = getLayoutDirectionFromLocale(Locale.getDefault())
            return if (direction == View.LAYOUT_DIRECTION_RTL) Gravity.START else Gravity.END
        }
    }
}

/*
 * Copyright 2012 Lars Werkman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package joshuatee.wx.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

import kotlin.math.*

import joshuatee.wx.R

/**
 * Displays a holo-themed color picker.

 *
 *
 * Use [.getColor] to retrieve the selected color. <br></br>
 * Use [.addSVBar] to add a Saturation/Value Bar. <br></br>
 * Use [.addOpacityBar] to add a Opacity Bar.
 *
 */
class ColorPicker : View {

    /**
     * `Paint` instance used to draw the color wheel.
     */
    private var mColorWheelPaint: Paint? = null

    /**
     * `Paint` instance used to draw the pointer's "halo".
     */
    private var mPointerHaloPaint: Paint? = null

    /**
     * `Paint` instance used to draw the pointer (the selected color).
     */
    private var mPointerColor: Paint? = null

    /**
     * The width of the color wheel thickness.
     */
    private var mColorWheelThickness: Int = 0

    /**
     * The radius of the color wheel.
     */
    private var mColorWheelRadius: Int = 0
    private var mPreferredColorWheelRadius: Int = 0

    /**
     * The radius of the center circle inside the color wheel.
     */
    private var mColorCenterRadius: Int = 0
    private var mPreferredColorCenterRadius: Int = 0

    /**
     * The radius of the halo of the center circle inside the color wheel.
     */
    private var mColorCenterHaloRadius: Int = 0
    private var mPreferredColorCenterHaloRadius: Int = 0

    /**
     * The radius of the pointer.
     */
    private var mColorPointerRadius: Int = 0

    /**
     * The radius of the halo of the pointer.
     */
    private var mColorPointerHaloRadius: Int = 0

    /**
     * The rectangle enclosing the color wheel.
     */
    private val mColorWheelRectangle = RectF()

    /**
     * The rectangle enclosing the center inside the color wheel.
     */
    private val mCenterRectangle = RectF()

    /**
     * `true` if the user clicked on the pointer to start the move mode. <br></br>
     * `false` once the user stops touching the screen.

     * @see .onTouchEvent
     */
    private var mUserIsMovingPointer = false

    /**
     * The ARGB value of the currently selected color.
     */
    private var mColor: Int = 0

    /**
     * The ARGB value of the center with the old selected color.
     */
    private var mCenterOldColor: Int = 0

    /**
     * Whether to show the old color in the center or not.
     */
    private var mShowCenterOldColor: Boolean = false

    /**
     * The ARGB value of the center with the new selected color.
     */
    private var mCenterNewColor: Int = 0

    /**
     * Number of pixels the origin of this view is moved in X- and Y-direction.

     *
     *
     * We use the center of this (quadratic) View as origin of our internal
     * coordinate system. Android uses the upper left corner as origin for the
     * View-specific coordinate system. So this is the value we use to translate
     * from one coordinate system to the other.
     *

     *
     *
     * Note: (Re)calculated in [.onMeasure].
     *

     * @see .onDraw
     */
    private var mTranslationOffset: Float = 0.toFloat()

    /**
     * Distance between pointer and user touch in X-direction.
     */
    private var mSlopX: Float = 0.toFloat()

    /**
     * Distance between pointer and user touch in Y-direction.
     */
    private var mSlopY: Float = 0.toFloat()

    /**
     * The pointer's position expressed as angle (in rad).
     */
    private var mAngle: Float = 0.toFloat()

    /**
     * `Paint` instance used to draw the center with the old selected
     * color.
     */
    private var mCenterOldPaint: Paint? = null

    /**
     * `Paint` instance used to draw the center with the new selected
     * color.
     */
    private var mCenterNewPaint: Paint? = null

    /**
     * `Paint` instance used to draw the halo of the center selected
     * colors.
     */
    private var mCenterHaloPaint: Paint? = null

    /**
     * An array of floats that can be build into a `Color` <br></br>
     * Where we can extract the Saturation and Value from.
     */
    private val mHSV = FloatArray(3)

    /**
     * `SVBar` instance used to control the Saturation/Value bar.
     */
    private var mSVbar: SVBar? = null

    /**
     * `OpacityBar` instance used to control the Opacity bar.
     */
    private val mOpacityBar: OpacityBar? = null

    /**
     * `SaturationBar` instance used to control the Saturation bar.
     */
    private var mSaturationBar: SaturationBar? = null

    /**
     * `ValueBar` instance used to control the Value bar.
     */
    private var mValueBar: ValueBar? = null

    /**
     * `onColorChangedListener` instance of the onColorChangedListener
     */
    private var onColorChangedListener: OnColorChangedListener? = null

    /**
     * `onColorSelectedListener` instance of the onColorSelectedListener
     */
    private val onColorSelectedListener: OnColorSelectedListener? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    /**
     * An interface that is called whenever the color is changed. Currently it
     * is always called when the color is changes.

     * @author lars
     */
    interface OnColorChangedListener {
        fun onColorChanged(color: Int)
    }

    /**
     * An interface that is called whenever a new color has been selected.
     * Currently it is always called when the color wheel has been released.

     */
    interface OnColorSelectedListener {
        fun onColorSelected(color: Int)
    }

    /**
     * Set a onColorChangedListener

     * @param {@code OnColorChangedListener}
     */
    fun setOnColorChangedListener(listener: OnColorChangedListener) {
        this.onColorChangedListener = listener
    }

    /**
     * Gets the onColorChangedListener

     * @return `OnColorChangedListener`
     */
    //public OnColorChangedListener getOnColorChangedListener() {
    //	return this.onColorChangedListener;
    //}

    /**
     * Set a onColorSelectedListener

     * @param {@code OnColorSelectedListener}
     */
    //public void setOnColorSelectedListener(OnColorSelectedListener listener) {
    //	this.onColorSelectedListener = listener;
    //}

    /**
     * Gets the onColorSelectedListener

     * @return `OnColorSelectedListener`
     */
    //public OnColorSelectedListener getOnColorSelectedListener() {
    //	return this.onColorSelectedListener;
    //}

    /**
     * Color of the latest entry of the onColorChangedListener.
     */
    private var oldChangedListenerColor: Int = 0

    /**
     * Color of the latest entry of the onColorSelectedListener.
     */
    private var oldSelectedListenerColor: Int = 0

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.ColorPicker, defStyle, 0
        )
        val b = context.resources

        mColorWheelThickness = a.getDimensionPixelSize(
            R.styleable.ColorPicker_color_wheel_thickness,
            b.getDimensionPixelSize(R.dimen.color_wheel_thickness)
        )
        mColorWheelRadius = a.getDimensionPixelSize(
            R.styleable.ColorPicker_color_wheel_radius,
            b.getDimensionPixelSize(R.dimen.color_wheel_radius)
        )
        mPreferredColorWheelRadius = mColorWheelRadius
        mColorCenterRadius = a.getDimensionPixelSize(
            R.styleable.ColorPicker_color_center_radius,
            b.getDimensionPixelSize(R.dimen.color_center_radius)
        )
        mPreferredColorCenterRadius = mColorCenterRadius
        mColorCenterHaloRadius = a.getDimensionPixelSize(
            R.styleable.ColorPicker_color_center_halo_radius,
            b.getDimensionPixelSize(R.dimen.color_center_halo_radius)
        )
        mPreferredColorCenterHaloRadius = mColorCenterHaloRadius
        mColorPointerRadius = a.getDimensionPixelSize(
            R.styleable.ColorPicker_color_pointer_radius,
            b.getDimensionPixelSize(R.dimen.color_pointer_radius)
        )
        mColorPointerHaloRadius = a.getDimensionPixelSize(
            R.styleable.ColorPicker_color_pointer_halo_radius,
            b.getDimensionPixelSize(R.dimen.color_pointer_halo_radius)
        )

        a.recycle()
        mAngle = (-PI / 2).toFloat()
        val s = SweepGradient(0f, 0f, COLORS, null)
        mColorWheelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mColorWheelPaint!!.shader = s
        mColorWheelPaint!!.style = Paint.Style.STROKE
        mColorWheelPaint!!.strokeWidth = mColorWheelThickness.toFloat()
        mPointerHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPointerHaloPaint!!.color = Color.BLACK
        mPointerHaloPaint!!.alpha = 0x50
        mPointerColor = Paint(Paint.ANTI_ALIAS_FLAG)
        mPointerColor!!.color = calculateColor(mAngle)
        mCenterNewPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mCenterNewPaint!!.color = calculateColor(mAngle)
        mCenterNewPaint!!.style = Paint.Style.FILL
        mCenterOldPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mCenterOldPaint!!.color = calculateColor(mAngle)
        mCenterOldPaint!!.style = Paint.Style.FILL
        mCenterHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mCenterHaloPaint!!.color = Color.BLACK
        mCenterHaloPaint!!.alpha = 0x00
        mCenterNewColor = calculateColor(mAngle)
        mCenterOldColor = calculateColor(mAngle)
        mShowCenterOldColor = true
    }

    override fun onDraw(canvas: Canvas) {
        // All of our positions are using our internal coordinate system.
        // Instead of translating
        // them we let Canvas do the work for us.
        canvas.translate(mTranslationOffset, mTranslationOffset)

        // Draw the color wheel.
        canvas.drawOval(mColorWheelRectangle, mColorWheelPaint!!)

        val pointerPosition = calculatePointerPosition(mAngle)

        // Draw the pointer's "halo"
        canvas.drawCircle(
            pointerPosition[0], pointerPosition[1],
            mColorPointerHaloRadius.toFloat(), mPointerHaloPaint!!
        )

        // Draw the pointer (the currently selected color) slightly smaller on
        // top.
        canvas.drawCircle(
            pointerPosition[0], pointerPosition[1],
            mColorPointerRadius.toFloat(), mPointerColor!!
        )

        // Draw the halo of the center colors.
        canvas.drawCircle(0f, 0f, mColorCenterHaloRadius.toFloat(), mCenterHaloPaint!!)

        if (mShowCenterOldColor) {
            // Draw the old selected color in the center.
            canvas.drawArc(mCenterRectangle, 90f, 180f, true, mCenterOldPaint!!)

            // Draw the new selected color in the center.
            canvas.drawArc(mCenterRectangle, 270f, 180f, true, mCenterNewPaint!!)
        } else {
            // Draw the new selected color in the center.
            canvas.drawArc(mCenterRectangle, 0f, 360f, true, mCenterNewPaint!!)
        }
    }

    @SuppressLint("SwitchIntDef")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val intrinsicSize = 2 * (mPreferredColorWheelRadius + mColorPointerHaloRadius)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width: Int
        val height: Int
        width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(intrinsicSize, widthSize)
            else -> intrinsicSize
        }
        height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(intrinsicSize, heightSize)
            else -> intrinsicSize
        }
        val min = min(width, height)
        setMeasuredDimension(min, min)
        mTranslationOffset = min * 0.5f
        // fill the rectangle instances.
        mColorWheelRadius = min / 2 - mColorWheelThickness - mColorPointerHaloRadius
        mColorWheelRectangle.set(
            (-mColorWheelRadius).toFloat(), (-mColorWheelRadius).toFloat(),
            mColorWheelRadius.toFloat(), mColorWheelRadius.toFloat()
        )

        mColorCenterRadius =
            (mPreferredColorCenterRadius.toFloat() * (mColorWheelRadius.toFloat() / mPreferredColorWheelRadius.toFloat())).toInt()
        mColorCenterHaloRadius =
            (mPreferredColorCenterHaloRadius.toFloat() * (mColorWheelRadius.toFloat() / mPreferredColorWheelRadius.toFloat())).toInt()
        mCenterRectangle.set(
            (-mColorCenterRadius).toFloat(), (-mColorCenterRadius).toFloat(),
            mColorCenterRadius.toFloat(), mColorCenterRadius.toFloat()
        )
    }

    private fun ave(s: Int, d: Int, p: Float): Int {
        return s + round(p * (d - s)).toInt()
    }

    /**
     * Calculate the color using the supplied angle.

     * @param angle
     * *            The selected color's position expressed as angle (in rad).
     * *
     * *
     * @return The ARGB value of the color on the color wheel at the specified
     * *         angle.
     */
    private fun calculateColor(angle: Float): Int {
        var unit = (angle / (2 * PI)).toFloat()
        if (unit < 0) {
            unit += 1f
        }
        if (unit <= 0) {
            mColor = COLORS[0]
            return COLORS[0]
        }
        if (unit >= 1) {
            mColor = COLORS[COLORS.size - 1]
            return COLORS[COLORS.size - 1]
        }
        var p = unit * (COLORS.size - 1)
        val i = p.toInt()
        p -= i.toFloat()
        val c0 = COLORS[i]
        val c1 = COLORS[i + 1]
        val a = ave(Color.alpha(c0), Color.alpha(c1), p)
        val r = ave(Color.red(c0), Color.red(c1), p)
        val g = ave(Color.green(c0), Color.green(c1), p)
        val b = ave(Color.blue(c0), Color.blue(c1), p)
        mColor = Color.argb(a, r, g, b)
        return Color.argb(a, r, g, b)
    }

    /**
     * Get the currently selected color.

     * @return The ARGB value of the currently selected color.
     */
    /**
     * Set the color to be highlighted by the pointer.   If the
     * instances `SVBar` and the `OpacityBar` aren't null the color
     * will also be set to them

     * *            The RGB value of the color to highlight. If this is not a
     * *            color displayed on the color wheel a very simple algorithm is
     * *            used to map it to the color wheel. The resulting color often
     * *            won't look close to the original color. This is especially
     * *            true for shades of grey. You have been warned!
     */
    // check of the instance isn't null
    // set the value of the opacity
    // check if the instance isn't null
    // the array mHSV will be filled with the HSV values of the color.
    // because of the design of the Saturation/Value bar,
    // we can only use Saturation or Value every time.
    // Here will be checked which we shall use.
    // if (mHSV[1] > mHSV[2]) {
    var color: Int
        get() = mCenterNewColor
        set(color) {
            mAngle = colorToAngle(color)
            mPointerColor!!.color = calculateColor(mAngle)
            mCenterNewPaint!!.color = calculateColor(mAngle)
            if (mOpacityBar != null) {
                mOpacityBar.color = mColor
                mOpacityBar.opacity = Color.alpha(color)
            }
            if (mSVbar != null) {
                Color.colorToHSV(color, mHSV)
                mSVbar!!.color = mColor
                if (mHSV[1] < mHSV[2]) {
                    mSVbar!!.setSaturation(mHSV[1])
                } else {
                    mSVbar!!.setValue(mHSV[2])
                }
            }

            if (mSaturationBar != null) {
                Color.colorToHSV(color, mHSV)
                mSaturationBar!!.color = mColor
                mSaturationBar!!.setSaturation(mHSV[1])
            }

            if (mValueBar != null && mSaturationBar == null) {
                Color.colorToHSV(color, mHSV)
                mValueBar!!.color = mColor
                mValueBar!!.setValue(mHSV[2])
            } else if (mValueBar != null) {
                Color.colorToHSV(color, mHSV)
                mValueBar!!.setValue(mHSV[2])
            }

            invalidate()
        }

    /**
     * Convert a color to an angle.

     * @param color
     * *            The RGB value of the color to "find" on the color wheel.
     * *
     * *
     * @return The angle (in rad) the "normalized" color is displayed on the
     * *         color wheel.
     */
    private fun colorToAngle(color: Int): Float {
        val colors = FloatArray(3)
        Color.colorToHSV(color, colors)
        return Math.toRadians((-colors[0]).toDouble()).toFloat()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        // Convert coordinates to our internal coordinate system
        val x = event.x - mTranslationOffset
        val y = event.y - mTranslationOffset
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check whether the user pressed on the pointer.
                val pointerPosition = calculatePointerPosition(mAngle)
                if (x >= pointerPosition[0] - mColorPointerHaloRadius
                    && x <= pointerPosition[0] + mColorPointerHaloRadius
                    && y >= pointerPosition[1] - mColorPointerHaloRadius
                    && y <= pointerPosition[1] + mColorPointerHaloRadius
                ) {
                    mSlopX = x - pointerPosition[0]
                    mSlopY = y - pointerPosition[1]
                    mUserIsMovingPointer = true
                    invalidate()
                } else if (x >= -mColorCenterRadius && x <= mColorCenterRadius
                    && y >= -mColorCenterRadius && y <= mColorCenterRadius
                    && mShowCenterOldColor
                ) {
                    mCenterHaloPaint!!.alpha = 0x50
                    color = oldCenterColor
                    invalidate()
                } else {
                    parent.requestDisallowInterceptTouchEvent(false)
                    return false
                }// If user did not press pointer or center, report event not handled
                // Check whether the user pressed on the center.
            }
            MotionEvent.ACTION_MOVE -> if (mUserIsMovingPointer) {
                mAngle = atan2((y - mSlopY).toDouble(), (x - mSlopX).toDouble()).toFloat()
                mPointerColor!!.color = calculateColor(mAngle)
                mCenterNewColor = calculateColor(mAngle)
                if (mOpacityBar != null) {
                    mOpacityBar.color = mColor
                }
                if (mValueBar != null) {
                    mValueBar!!.color = mColor
                }
                if (mSaturationBar != null) {
                    mSaturationBar!!.color = mColor
                }
                if (mSVbar != null) {
                    mSVbar!!.color = mColor
                }
                invalidate()
            } else {
                parent.requestDisallowInterceptTouchEvent(false)
                return false
            }// If user did not press pointer or center, report event not handled
            MotionEvent.ACTION_UP -> {
                mUserIsMovingPointer = false
                mCenterHaloPaint!!.alpha = 0x00
                if (onColorSelectedListener != null && mCenterNewColor != oldSelectedListenerColor) {
                    onColorSelectedListener.onColorSelected(mCenterNewColor)
                    oldSelectedListenerColor = mCenterNewColor
                }
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> if (onColorSelectedListener != null && mCenterNewColor != oldSelectedListenerColor) {
                onColorSelectedListener.onColorSelected(mCenterNewColor)
                oldSelectedListenerColor = mCenterNewColor
            }
        }
        return true
    }

    /**
     * Calculate the pointer's coordinates on the color wheel using the supplied
     * angle.

     * @param angle
     * *            The position of the pointer expressed as angle (in rad).
     * *
     * *
     * @return The coordinates of the pointer's center in our internal
     * *         coordinate system.
     */
    private fun calculatePointerPosition(angle: Float): FloatArray {
        val x = (mColorWheelRadius * cos(angle.toDouble())).toFloat()
        val y = (mColorWheelRadius * sin(angle.toDouble())).toFloat()
        return floatArrayOf(x, y)
    }

    /**
     * Add a Saturation/Value bar to the color wheel.

     * @param bar
     * *            The instance of the Saturation/Value bar.
     */
    fun addSVBar(bar: SVBar) {
        mSVbar = bar
        // Give an instance of the color picker to the Saturation/Value bar.
        mSVbar!!.setColorPicker(this)
        mSVbar!!.color = mColor
    }

    /**
     * Add a Opacity bar to the color wheel.

     * @param bar
     * *            The instance of the Opacity bar.
     */
    /*public void addOpacityBar(OpacityBar bar) {
		mOpacityBar = bar;
		// Give an instance of the color picker to the Opacity bar.
		mOpacityBar.setColorPicker(this);
		mOpacityBar.setColor(mColor);
	}*/

    fun addSaturationBar(bar: SaturationBar) {
        mSaturationBar = bar
        mSaturationBar!!.setColorPicker(this)
        mSaturationBar!!.color = mColor
    }

    fun addValueBar(bar: ValueBar) {
        mValueBar = bar
        mValueBar!!.setColorPicker(this)
        mValueBar!!.color = mColor
    }

    /**
     * Change the color of the center which indicates the new color.

     * @param color
     * *            int of the color.
     */
    fun setNewCenterColor(color: Int) {
        mCenterNewColor = color
        mCenterNewPaint!!.color = color
        if (mCenterOldColor == 0) {
            mCenterOldColor = color
            mCenterOldPaint!!.color = color
        }
        if (onColorChangedListener != null && color != oldChangedListenerColor) {
            onColorChangedListener!!.onColorChanged(color)
            oldChangedListenerColor = color
        }
        invalidate()
    }

    /**
     * Change the color of the center which indicates the old color.

     * *            int of the color.
     */
    var oldCenterColor: Int
        get() = mCenterOldColor
        set(color) {
            mCenterOldColor = color
            mCenterOldPaint!!.color = color
            invalidate()
        }

    /**
     * Set whether the old color is to be shown in the center or not

     */
    /*	public void setShowOldCenterColor(boolean show) {
		mShowCenterOldColor = show;
		invalidate();
	}*/

    //public boolean getShowOldCenterColor() {
    //	return mShowCenterOldColor;
    //}

    /**
     * Used to change the color of the `OpacityBar` used by the
     * `SVBar` if there is an change in color.

     * @param color
     * *            int of the color used to change the opacity bar color.
     */
    fun changeOpacityBarColor(color: Int) {
        if (mOpacityBar != null) {
            mOpacityBar.color = color
        }
    }

    /**
     * Used to change the color of the `SaturationBar`.

     * *            int of the color used to change the opacity bar color.
     */
    /*public void changeSaturationBarColor(int color) {
		if (mSaturationBar != null) {
			mSaturationBar.setColor(color);
		}
	}*/

    /**
     * Used to change the color of the `ValueBar`.

     * @param color
     * *            int of the color used to change the opacity bar color.
     */
    fun changeValueBarColor(color: Int) {
        if (mValueBar != null) {
            mValueBar!!.color = color
        }
    }

    /**
     * Checks if there is an `OpacityBar` connected.

     * @return true or false.
     */
    fun hasOpacityBar(): Boolean {
        return mOpacityBar != null
    }

    /**
     * Checks if there is a `ValueBar` connected.

     * @return true or false.
     */
    fun hasValueBar(): Boolean {
        return mValueBar != null
    }

    /**
     * Checks if there is a `SaturationBar` connected.

     * @return true or false.
     */
    //public boolean hasSaturationBar(){
    //	return mSaturationBar != null;
    //}

    /**
     * Checks if there is a `SVBar` connected.

     * @return true or false.
     */
    //public boolean hasSVBar(){
    //	return mSVbar != null;
    //}

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val state = Bundle()
        state.putParcelable(STATE_PARENT, superState)
        state.putFloat(STATE_ANGLE, mAngle)
        state.putInt(STATE_OLD_COLOR, mCenterOldColor)
        state.putBoolean(STATE_SHOW_OLD_COLOR, mShowCenterOldColor)
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as Bundle
        val superState = savedState.getParcelable<Parcelable>(STATE_PARENT)
        super.onRestoreInstanceState(superState)
        mAngle = savedState.getFloat(STATE_ANGLE)
        oldCenterColor = savedState.getInt(STATE_OLD_COLOR)
        mShowCenterOldColor = savedState.getBoolean(STATE_SHOW_OLD_COLOR)
        val currentColor = calculateColor(mAngle)
        mPointerColor!!.color = currentColor
        setNewCenterColor(currentColor)
    }

    companion object {
        /*
	 * Constants used to save/restore the instance state.
	 */
        private const val STATE_PARENT = "parent"
        private const val STATE_ANGLE = "angle"
        private const val STATE_OLD_COLOR = "color"
        private const val STATE_SHOW_OLD_COLOR = "showColor"

        /**
         * Colors to construct the color wheel using [SweepGradient].

         *
         *
         * Note: The algorithm in [.normalizeColor] highly depends on
         * these exact values. Be aware that [.setColor] might break if
         * you change this array.
         *
         */
        private val COLORS = intArrayOf(
            0xFFFF0000.toInt(),
            0xFFFF00FF.toInt(),
            0xFF0000FF.toInt(),
            0xFF00FFFF.toInt(),
            0xFF00FF00.toInt(),
            0xFFFFFF00.toInt(),
            0xFFFF0000.toInt()
        )
    }
}
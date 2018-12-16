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
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

import joshuatee.wx.R

class SaturationBar : View {

    /**
     * The thickness of the bar.
     */
    private var mBarThickness: Int = 0

    /**
     * The length of the bar.
     */
    private var mBarLength: Int = 0
    private var mPreferredBarLength: Int = 0

    /**
     * The radius of the pointer.
     */
    private var mBarPointerRadius: Int = 0

    /**
     * The radius of the halo of the pointer.
     */
    private var mBarPointerHaloRadius: Int = 0

    /**
     * The position of the pointer on the bar.
     */
    private var mBarPointerPosition: Int = 0

    /**
     * `Paint` instance used to draw the bar.
     */
    private var mBarPaint: Paint? = null

    /**
     * `Paint` instance used to draw the pointer.
     */
    private var mBarPointerPaint: Paint? = null

    /**
     * `Paint` instance used to draw the halo of the pointer.
     */
    private var mBarPointerHaloPaint: Paint? = null

    /**
     * The rectangle enclosing the bar.
     */
    private val mBarRect = RectF()

    /**
     * `Shader` instance used to fill the shader of the paint.
     */
    private var shader: Shader? = null

    /**
     * `true` if the user clicked on the pointer to start the move mode. <br></br>
     * `false` once the user stops touching the screen.

     * @see .onTouchEvent
     */
    private var mIsMovingPointer: Boolean = false

    /**
     * The ARGB value of the currently selected color.
     */
    private var mColor: Int = 0

    /**
     * An array of floats that can be build into a `Color` <br></br>
     * Where we can extract the color from.
     */
    private val mHSVColor = FloatArray(3)

    /**
     * Factor used to calculate the position to the Opacity on the bar.
     */
    private var mPosToSatFactor: Float = 0.toFloat()

    /**
     * Factor used to calculate the Opacity to the postion on the bar.
     */
    private var mSatToPosFactor: Float = 0.toFloat()

    /**
     * `ColorPicker` instance used to control the ColorPicker.
     */
    private var mPicker: ColorPicker? = null

    /**
     * Used to toggle orientation between vertical and horizontal.
     */
    private var mOrientation: Boolean = false

    /**
     * Interface and listener so that changes in SaturationBar are sent
     * to the host activity/fragment
     */
    private val onSaturationChangedListener: OnSaturationChangedListener? = null

    /**
     * Saturation of the latest entry of the onSaturationChangedListener.
     */
    private var oldChangedListenerSaturation: Int = 0

    interface OnSaturationChangedListener {
        fun onSaturationChanged(saturation: Int)
    }

    /*public void setOnSaturationChangedListener(OnSaturationChangedListener listener) {
		this.onSaturationChangedListener = listener;
	}

	public OnSaturationChangedListener getOnSaturationChangedListener() {
		return this.onSaturationChangedListener;
	}*/

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

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.ColorBars, defStyle, 0
        )
        val b = context.resources

        mBarThickness = a.getDimensionPixelSize(
            R.styleable.ColorBars_bar_thickness,
            b.getDimensionPixelSize(R.dimen.bar_thickness)
        )
        mBarLength = a.getDimensionPixelSize(
            R.styleable.ColorBars_bar_length,
            b.getDimensionPixelSize(R.dimen.bar_length)
        )
        mPreferredBarLength = mBarLength
        mBarPointerRadius = a.getDimensionPixelSize(
            R.styleable.ColorBars_bar_pointer_radius,
            b.getDimensionPixelSize(R.dimen.bar_pointer_radius)
        )
        mBarPointerHaloRadius = a.getDimensionPixelSize(
            R.styleable.ColorBars_bar_pointer_halo_radius,
            b.getDimensionPixelSize(R.dimen.bar_pointer_halo_radius)
        )
        mOrientation = a.getBoolean(
            R.styleable.ColorBars_bar_orientation_horizontal, ORIENTATION_DEFAULT
        )

        a.recycle()

        mBarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBarPaint!!.shader = shader

        mBarPointerPosition = mBarLength + mBarPointerHaloRadius

        mBarPointerHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBarPointerHaloPaint!!.color = Color.BLACK
        mBarPointerHaloPaint!!.alpha = 0x50

        mBarPointerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBarPointerPaint!!.color = 0xff81ff00.toInt()

        mPosToSatFactor = 1 / mBarLength.toFloat()
        mSatToPosFactor = mBarLength.toFloat() / 1
    }

    @SuppressLint("SwitchIntDef")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val intrinsicSize = mPreferredBarLength + mBarPointerHaloRadius * 2
        // Variable orientation
        val measureSpec: Int = if (mOrientation == ORIENTATION_HORIZONTAL) {
            widthMeasureSpec
        } else {
            heightMeasureSpec
        }
        val lengthMode = View.MeasureSpec.getMode(measureSpec)
        val lengthSize = View.MeasureSpec.getSize(measureSpec)
        val length: Int = when (lengthMode) {
            View.MeasureSpec.EXACTLY -> lengthSize
            View.MeasureSpec.AT_MOST -> Math.min(intrinsicSize, lengthSize)
            else -> intrinsicSize
        }
        val barPointerHaloRadiusx2 = mBarPointerHaloRadius * 2
        mBarLength = length - barPointerHaloRadiusx2
        if (mOrientation == ORIENTATION_VERTICAL) {
            setMeasuredDimension(
                barPointerHaloRadiusx2,
                mBarLength + barPointerHaloRadiusx2
            )
        } else {
            setMeasuredDimension(
                mBarLength + barPointerHaloRadiusx2,
                barPointerHaloRadiusx2
            )
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Fill the rectangle instance based on orientation
        val x1: Int
        val y1: Int
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            x1 = mBarLength + mBarPointerHaloRadius
            y1 = mBarThickness
            mBarLength = w - mBarPointerHaloRadius * 2
            mBarRect.set(
                mBarPointerHaloRadius.toFloat(),
                (mBarPointerHaloRadius - mBarThickness / 2).toFloat(),
                (mBarLength + mBarPointerHaloRadius).toFloat(),
                (mBarPointerHaloRadius + mBarThickness / 2).toFloat()
            )
        } else {
            x1 = mBarThickness
            y1 = mBarLength + mBarPointerHaloRadius
            mBarLength = h - mBarPointerHaloRadius * 2
            mBarRect.set(
                (mBarPointerHaloRadius - mBarThickness / 2).toFloat(),
                mBarPointerHaloRadius.toFloat(),
                (mBarPointerHaloRadius + mBarThickness / 2).toFloat(),
                (mBarLength + mBarPointerHaloRadius).toFloat()
            )
        }

        // Update variables that depend of mBarLength.
        if (!isInEditMode) {
            shader = LinearGradient(
                mBarPointerHaloRadius.toFloat(),
                0f,
                x1.toFloat(),
                y1.toFloat(),
                intArrayOf(Color.WHITE, Color.HSVToColor(0xFF, mHSVColor)),
                null,
                Shader.TileMode.CLAMP
            )
        } else {
            shader = LinearGradient(
                mBarPointerHaloRadius.toFloat(),
                0f,
                x1.toFloat(),
                y1.toFloat(),
                intArrayOf(Color.WHITE, 0xff81ff00.toInt()),
                null,
                Shader.TileMode.CLAMP
            )
            Color.colorToHSV(0xff81ff00.toInt(), mHSVColor)
        }
        mBarPaint!!.shader = shader
        mPosToSatFactor = 1 / mBarLength.toFloat()
        mSatToPosFactor = mBarLength.toFloat() / 1
        val hsvColor = FloatArray(3)
        Color.colorToHSV(mColor, hsvColor)
        mBarPointerPosition = if (!isInEditMode) {
            Math.round(mSatToPosFactor * hsvColor[1] + mBarPointerHaloRadius)
        } else {
            mBarLength + mBarPointerHaloRadius
        }
    }

    override fun onDraw(canvas: Canvas) {
        // Draw the bar.
        canvas.drawRect(mBarRect, mBarPaint!!)

        // Calculate the center of the pointer.
        val cX: Int
        val cY: Int
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            cX = mBarPointerPosition
            cY = mBarPointerHaloRadius
        } else {
            cX = mBarPointerHaloRadius
            cY = mBarPointerPosition
        }

        // Draw the pointer halo.
        canvas.drawCircle(
            cX.toFloat(),
            cY.toFloat(),
            mBarPointerHaloRadius.toFloat(),
            mBarPointerHaloPaint!!
        )
        // Draw the pointer.
        canvas.drawCircle(
            cX.toFloat(),
            cY.toFloat(),
            mBarPointerRadius.toFloat(),
            mBarPointerPaint!!
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        // Convert coordinates to our internal coordinate system
        val dimen: Float = if (mOrientation == ORIENTATION_HORIZONTAL) {
            event.x
        } else {
            event.y
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mIsMovingPointer = true
                // Check whether the user pressed on (or near) the pointer
                if (dimen >= mBarPointerHaloRadius && dimen <= mBarPointerHaloRadius + mBarLength) {
                    mBarPointerPosition = Math.round(dimen)
                    calculateColor(Math.round(dimen))
                    mBarPointerPaint!!.color = mColor
                    invalidate()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mIsMovingPointer) {
                    // Move the the pointer on the bar.
                    if (dimen >= mBarPointerHaloRadius && dimen <= mBarPointerHaloRadius + mBarLength) {
                        mBarPointerPosition = Math.round(dimen)
                        calculateColor(Math.round(dimen))
                        mBarPointerPaint!!.color = mColor
                        if (mPicker != null) {
                            mPicker!!.setNewCenterColor(mColor)
                            mPicker!!.changeValueBarColor(mColor)
                            mPicker!!.changeOpacityBarColor(mColor)
                        }
                        invalidate()
                    } else if (dimen < mBarPointerHaloRadius) {
                        mBarPointerPosition = mBarPointerHaloRadius
                        mColor = Color.WHITE
                        mBarPointerPaint!!.color = mColor
                        if (mPicker != null) {
                            mPicker!!.setNewCenterColor(mColor)
                            mPicker!!.changeValueBarColor(mColor)
                            mPicker!!.changeOpacityBarColor(mColor)
                        }
                        invalidate()
                    } else if (dimen > mBarPointerHaloRadius + mBarLength) {
                        mBarPointerPosition = mBarPointerHaloRadius + mBarLength
                        mColor = Color.HSVToColor(mHSVColor)
                        mBarPointerPaint!!.color = mColor
                        if (mPicker != null) {
                            mPicker!!.setNewCenterColor(mColor)
                            mPicker!!.changeValueBarColor(mColor)
                            mPicker!!.changeOpacityBarColor(mColor)
                        }
                        invalidate()
                    }
                }
                if (onSaturationChangedListener != null && oldChangedListenerSaturation != mColor) {
                    onSaturationChangedListener.onSaturationChanged(mColor)
                    oldChangedListenerSaturation = mColor
                }
            }
            MotionEvent.ACTION_UP -> mIsMovingPointer = false
        }
        return true
    }

    /**
     * Set the pointer on the bar. With the opacity value.

     * @param saturation
     * *            float between 0 > 1
     */
    fun setSaturation(saturation: Float) {
        mBarPointerPosition = Math.round(mSatToPosFactor * saturation) + mBarPointerHaloRadius
        calculateColor(mBarPointerPosition)
        mBarPointerPaint!!.color = mColor
        if (mPicker != null) {
            mPicker!!.setNewCenterColor(mColor)
            mPicker!!.changeValueBarColor(mColor)
            mPicker!!.changeOpacityBarColor(mColor)
        }
        invalidate()
    }

    /**
     * Calculate the color selected by the pointer on the bar.

     * @param coordF
     * *            Coordinate of the pointer.
     */
    private fun calculateColor(coordF: Int) {
        var coord = coordF
        coord -= mBarPointerHaloRadius
        if (coord < 0) {
            coord = 0
        } else if (coord > mBarLength) {
            coord = mBarLength
        }
        mColor = Color.HSVToColor(floatArrayOf(mHSVColor[0], mPosToSatFactor * coord, 1f))
    }

    /**
     * Get the currently selected color.

     * @return The ARGB value of the currently selected color.
     */
    /**
     * Set the bar color. <br></br>
     * <br></br>
     * Its discouraged to use this method.

     *
     */
    var color: Int
        get() = mColor
        set(color) {
            val x1: Int
            val y1: Int
            if (mOrientation == ORIENTATION_HORIZONTAL) {
                x1 = mBarLength + mBarPointerHaloRadius
                y1 = mBarThickness
            } else {
                x1 = mBarThickness
                y1 = mBarLength + mBarPointerHaloRadius
            }
            Color.colorToHSV(color, mHSVColor)
            shader = LinearGradient(
                mBarPointerHaloRadius.toFloat(),
                0f,
                x1.toFloat(),
                y1.toFloat(),
                intArrayOf(Color.WHITE, color),
                null,
                Shader.TileMode.CLAMP
            )
            mBarPaint!!.shader = shader
            calculateColor(mBarPointerPosition)
            mBarPointerPaint!!.color = mColor
            if (mPicker != null) {
                mPicker!!.setNewCenterColor(mColor)
                if (mPicker!!.hasValueBar())
                    mPicker!!.changeValueBarColor(mColor)
                else if (mPicker!!.hasOpacityBar())
                    mPicker!!.changeOpacityBarColor(mColor)
            }
            invalidate()
        }

    /**
     * Adds a `ColorPicker` instance to the bar. <br></br>
     * <br></br>
     * WARNING: Don't change the color picker. it is done already when the bar
     * is added to the ColorPicker

     * @see ColorPicker.addSVBar
     * @param picker
     */
    fun setColorPicker(picker: ColorPicker) {
        mPicker = picker
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        val state = Bundle()
        state.putParcelable(STATE_PARENT, superState)
        state.putFloatArray(STATE_COLOR, mHSVColor)

        val hsvColor = FloatArray(3)
        Color.colorToHSV(mColor, hsvColor)
        state.putFloat(STATE_SATURATION, hsvColor[1])

        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as Bundle

        val superState = savedState.getParcelable<Parcelable>(STATE_PARENT)
        super.onRestoreInstanceState(superState)

        color = Color.HSVToColor(savedState.getFloatArray(STATE_COLOR))
        setSaturation(savedState.getFloat(STATE_SATURATION))
    }

    companion object {

        /*
	 * Constants used to save/restore the instance state.
	 */
        private const val STATE_PARENT = "parent"
        private const val STATE_COLOR = "color"
        private const val STATE_SATURATION = "saturation"
        //private static final String STATE_ORIENTATION = "orientation";

        /**
         * Constants used to identify orientation.
         */
        private const val ORIENTATION_HORIZONTAL = true
        private const val ORIENTATION_VERTICAL = false

        /**
         * Default orientation of the bar.
         */
        private const val ORIENTATION_DEFAULT = ORIENTATION_HORIZONTAL
    }
}
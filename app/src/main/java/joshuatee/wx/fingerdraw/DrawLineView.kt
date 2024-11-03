/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

    This file is part of wX.

    wX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    wX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with wX.  If not, see <http://www.gnu.org/licenses/>.

 */

package joshuatee.wx.fingerdraw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.DistanceUnit
import joshuatee.wx.objects.LatLon
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.util.To
import kotlin.math.*

// this class implements the radial distance tool available in single pane radar

class DrawLineView : View {

    private var startX = 0.0f
    private var startY = 0.0f
    private var endX = 0.0f
    private var endY = 0.0f
    private val drawListener = DrawListener()
    private val paint = Paint()
    private val paintText = Paint()
    private var xMiddle = 0.0f
    private var yMiddle = 0.0f
    private var centerX = 0.0f
    private var centerY = 0.0f

    constructor(context: Context, attribs: AttributeSet) : super(context, attribs) {
        setup()
    }

    private var mScaleFactor = 0.0f
    private var mPositionX = 0.0f
    private var mPositionY = 0.0f
    private var ortInt = 0.0f
    private var oneDegreeScaleFactor = 0.0f

    constructor(
        context: Context,
        radarSite: String,
        mScaleFactor: Float,
        mPositionX: Float,
        mPositionY: Float,
        ortInt: Float,
        oneDegreeScaleFactor: Float
    ) : super(context) {
        this.mScaleFactor = mScaleFactor
        this.mPositionX = mPositionX
        this.mPositionY = mPositionY
        this.ortInt = ortInt
        this.oneDegreeScaleFactor = oneDegreeScaleFactor
        val xStr = UtilityLocation.getRadarSiteX(radarSite)
        val yStr = UtilityLocation.getRadarSiteY(radarSite)
        centerX = To.float(xStr)
        centerY = To.float(yStr)
        xMiddle = width / 2.0f
        yMiddle = height / 2.0f
        setup()
    }

    private fun setup() {
        isFocusable = true
        isFocusableInTouchMode = true
        this.setOnTouchListener(drawListener)
        with(paint) {
            color = RadarPreferences.drawToolColor
            isAntiAlias = true
            strokeWidth = RadarPreferences.drawToolSize.toFloat()
            this.textSize = UIPreferences.textSizeLarge
        }
        with(paintText) {
            color = if (!RadarPreferences.blackBg) {
                Color.BLACK
            } else {
                Color.WHITE
            }
            isAntiAlias = true
            strokeWidth = 4.5f
            this.textSize = UIPreferences.textSizeLarge
        }
    }

    public override fun onDraw(canvas: Canvas) {
        val distance = distanceBetweenTwoPoints(startX, startY, endX, endY)
        paint.alpha = 255
        with(canvas) {
            drawLine(startX, startY, endX, endY, paint)
            drawText("mi: " + round(calcMiles()).toString(), startX, startY - 25, paintText)
            val circleEndRadius = 10.0f
            drawCircle(startX, startY, circleEndRadius, paint)
            drawCircle(endX, endY, circleEndRadius, paint)
            paint.alpha = 50
            drawCircle(startX, startY, distance, paint)
        }
    }

    private fun calcMiles(): Float {
        val density = ortInt * 2 / width
        var diffX = density * (xMiddle - startX) / mScaleFactor
        var diffY = density * (yMiddle - startY) / mScaleFactor
        var ppd = oneDegreeScaleFactor
        val newX = (centerY + (mPositionX / mScaleFactor + diffX) / ppd)
        var test2 = (180.0f / PI * log(tan(PI / 4 + centerX * (PI / 180.0f) / 2.0f), E)).toFloat()
        var newY = (test2 + (-mPositionY / mScaleFactor + diffY) / ppd)
        newY = (180.0f / PI * (2.0f * atan(exp(newY * PI / 180.0f)) - PI / 2.0f)).toFloat()
        diffX = density * (xMiddle - endX) / mScaleFactor
        diffY = density * (yMiddle - endY) / mScaleFactor
        ppd = oneDegreeScaleFactor
        val newX2 = (centerY + (mPositionX / mScaleFactor + diffX) / ppd)
        test2 = (180.0f / PI * log(tan(PI / 4 + centerX * (PI / 180.0f) / 2.0f), E)).toFloat()
        var newY2 = (test2 + (-mPositionY / mScaleFactor + diffY) / ppd)
        newY2 = (180.0f / PI * (2.0f * atan(exp(newY2 * PI / 180.0f)) - PI / 2.0f)).toFloat()
        return LatLon.distance(LatLon(newY, newX * -1.0f), LatLon(newY2, newX2 * -1.0f)).toFloat()
    }

    private fun distanceBetweenTwoPoints(x1: Float, y1: Float, x2: Float, y2: Float): Float =
        sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2))

    private inner class DrawListener : OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                val oldStartX = startX
                val oldStartY = startY
                startX = event.x
                startY = event.y
                if (distanceBetweenTwoPoints(startX, startY, endX, endY) < 100) {
                    startX = oldStartX
                    startY = oldStartY
                }
            } else if (event.action == MotionEvent.ACTION_MOVE) {
                endX = event.x
                endY = event.y
                invalidate()
            } else if (event.action == MotionEvent.ACTION_UP) {
                endX = event.x
                endY = event.y
                invalidate()
            }
            return true
        }
    }
}

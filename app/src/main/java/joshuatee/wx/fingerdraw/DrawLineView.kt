/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.DistanceUnit
import joshuatee.wx.radar.LatLon
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility
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
    private val textSize = MyApplication.textSizeLarge
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
        val xStr = Utility.getRadarSiteX(radarSite)
        val yStr = Utility.getRadarSiteY(radarSite)
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
        paint.color = MyApplication.drawToolColor
        paint.isAntiAlias = true
        paint.strokeWidth = MyApplication.drawToolSize.toFloat()
        paint.textSize = textSize
        paintText.color = Color.WHITE
        if (!MyApplication.blackBg) paintText.color = Color.BLACK
        paintText.isAntiAlias = true
        paintText.strokeWidth = 4.5f
        paintText.textSize = textSize
    }

    public override fun onDraw(canvas: Canvas) {
        val distance = distanceBetweenTwoPoints(startX, startY, endX, endY)
        paint.alpha = 255
        canvas.drawLine(startX, startY, endX, endY, paint)
        canvas.drawText("mi: " + round(calcMiles()).toString(), startX, startY - 25, paintText)
        val circleEndRadius = 10.0f
        canvas.drawCircle(startX, startY, circleEndRadius, paint)
        canvas.drawCircle(endX, endY, circleEndRadius, paint)
        paint.alpha = 50
        canvas.drawCircle(startX, startY, distance, paint)
    }

    private fun calcMiles(): Float {
        val density = ortInt * 2 / width
        var diffX = density * (xMiddle - startX) / mScaleFactor
        var diffY = density * (yMiddle - startY) / mScaleFactor
        var ppd = oneDegreeScaleFactor
        val newX = (centerY + (mPositionX / mScaleFactor + diffX) / ppd).toDouble()
        var test2 = 180 / PI * log(tan(PI / 4 + centerX * (PI / 180) / 2), E)
        var newY = (test2.toFloat() + (-mPositionY / mScaleFactor + diffY) / ppd).toDouble()
        newY = (180 / PI * (2 * atan(exp(newY * PI / 180)) - PI / 2)).toFloat().toDouble()
        diffX = density * (xMiddle - endX) / mScaleFactor
        diffY = density * (yMiddle - endY) / mScaleFactor
        ppd = oneDegreeScaleFactor
        val newX2 = (centerY + (mPositionX / mScaleFactor + diffX) / ppd).toDouble()
        test2 = 180 / PI * log(tan(PI / 4 + centerX * (PI / 180) / 2), E)
        var newY2 = (test2.toFloat() + (-mPositionY / mScaleFactor + diffY) / ppd).toDouble()
        newY2 = (180 / PI * (2 * atan(exp(newY2 * PI / 180)) - PI / 2)).toFloat().toDouble()
        return LatLon.distance(LatLon(newY, newX * -1), LatLon(newY2, newX2 * -1), DistanceUnit.MILE).toFloat()
    }

    private fun distanceBetweenTwoPoints(x1: Float, y1: Float, x2: Float, y2: Float) =
        sqrt(((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2)).toDouble()).toFloat()

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






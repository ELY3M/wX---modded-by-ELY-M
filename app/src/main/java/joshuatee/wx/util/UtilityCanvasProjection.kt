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

package joshuatee.wx.util

import java.nio.ByteBuffer

import joshuatee.wx.external.ExternalGlobalCoordinates
import joshuatee.wx.objects.LatLon
import kotlin.math.*

object UtilityCanvasProjection {

//    private fun compute4326Numbers(x: Double, y: Double, pn: ProjectionNumbers) =
//            doubleArrayOf(
//                    (-((y - pn.yDbl) * pn.scale) + pn.xCenter),
//                    (-((x - pn.xDbl) * pn.scale) + pn.yCenter)
//            )

    // fun compute4326Numbers(latLon: LatLon, projectionNumbers: ProjectionNumbers) = compute4326Numbers(latLon.lat, latLon.lon, projectionNumbers)

    fun compute4326NumbersFloatToBuffer(numBuffer: ByteBuffer, tmpBuffer: ByteBuffer, projectionNumbers: ProjectionNumbers) {
        numBuffer.position(0)
        tmpBuffer.position(0)
        var x: Float
        var y: Float
        var xTmp: Float
        var yTmp: Float
        val pnXFloat = projectionNumbers.xFloat
        val pnYFloat = projectionNumbers.yFloat
        val pnScaleFloat = projectionNumbers.scaleFloat
        val pnXCenter = projectionNumbers.xCenter
        val pnYCenter = projectionNumbers.yCenter
        try {
            while (numBuffer.position() < numBuffer.capacity()) {
                xTmp = numBuffer.float
                yTmp = numBuffer.float
                x = (-((yTmp - pnYFloat) * pnScaleFloat) + pnXCenter.toFloat())
                y = (-((xTmp - pnXFloat) * pnScaleFloat) + pnYCenter.toFloat())
                if (tmpBuffer.position() < (tmpBuffer.capacity() - 7)) {
                    tmpBuffer.putFloat(x)
                    tmpBuffer.putFloat(y)
                }
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    fun computeMercatorFloatToBuffer(numBuffer: ByteBuffer, tmpBuffer: ByteBuffer, projectionNumbers: ProjectionNumbers) {
        numBuffer.position(0)
        tmpBuffer.position(0)
        var x: Float
        var y: Float
        var xTmp: Float
        var yTmp: Float
        val pnXFloat = projectionNumbers.xFloat
        val pnYFloat = projectionNumbers.yFloat
        val pnXCenter = projectionNumbers.xCenter
        val pnYCenter = projectionNumbers.yCenter
        val oneDegreeScaleFactor = projectionNumbers.oneDegreeScaleFactorFloat
        while (numBuffer.position() < numBuffer.capacity()) {
            xTmp = numBuffer.float
            yTmp = numBuffer.float
            x = (-((yTmp - pnYFloat) * oneDegreeScaleFactor)) + pnXCenter.toFloat()
            y = (-((180 / PI * log(
                    tan(PI / 4 + xTmp * (PI / 180) / 2),
                    E
            ) - 180 / PI * log(
                    tan(PI / 4 + pnXFloat * (PI / 180) / 2),
                    E
            )) * oneDegreeScaleFactor)).toFloat() + pnYCenter.toFloat()
            tmpBuffer.putFloat(x)
            tmpBuffer.putFloat(y)
        }
    }

    fun computeMercatorNumbers(latLon: LatLon, projectionNumbers: ProjectionNumbers) = computeMercatorNumbers(latLon.lat, latLon.lon, projectionNumbers)

    fun computeMercatorNumbers(ec: ExternalGlobalCoordinates, pn: ProjectionNumbers) = computeMercatorNumbers(ec.latitude, ec.longitude * -1.0, pn)

    fun computeMercatorNumbers(x: Double, y: Double, pn: ProjectionNumbers) =
            doubleArrayOf(
                    (-((y - pn.yDbl) * pn.oneDegreeScaleFactor)) + pn.xCenter.toFloat(),
                    (-((180 / PI * log(
                            tan(PI / 4 + x * (PI / 180) / 2),
                            E
                    ) - 180 / PI * log(
                            tan(PI / 4 + pn.xDbl * (PI / 180) / 2),
                            E
                    )) * pn.oneDegreeScaleFactor)) + pn.yCenter
            )
}

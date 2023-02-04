/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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

package joshuatee.wx.radar

import java.nio.ByteBuffer
import joshuatee.wx.external.ExternalGlobalCoordinates
import joshuatee.wx.objects.LatLon
import joshuatee.wx.util.ProjectionNumbers
import kotlin.math.*

object Projection {

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
            x = (-1.0f * ((yTmp - pnYFloat) * oneDegreeScaleFactor)) + pnXCenter.toFloat()
            y = (-1.0 * ((180.0 / PI * log(
                tan(PI / 4.0 + xTmp * (PI / 180.0) / 2.0),
                E
            ) - 180.0 / PI * log(
                tan(PI / 4.0 + pnXFloat * (PI / 180.0) / 2.0),
                E
            )) * oneDegreeScaleFactor)).toFloat() + pnYCenter.toFloat()
            tmpBuffer.putFloat(x)
            tmpBuffer.putFloat(y)
        }
    }

    fun computeMercatorNumbers(latLon: LatLon, projectionNumbers: ProjectionNumbers): DoubleArray =
        computeMercatorNumbers(latLon.lat, latLon.lon, projectionNumbers)

    fun computeMercatorNumbers(ec: ExternalGlobalCoordinates, pn: ProjectionNumbers): DoubleArray =
        computeMercatorNumbers(ec.latitude, ec.longitude * -1.0, pn)

    // TODO FIXME returning a List<Float> would remove some boilerplate but would likely require changes in quite a few spots and may not be worth it
    fun computeMercatorNumbers(x: Double, y: Double, pn: ProjectionNumbers): DoubleArray =
        doubleArrayOf(
            (-1.0 * ((y - pn.yDbl) * pn.oneDegreeScaleFactor)) + pn.xCenter.toFloat(),
            (-1.0 * ((180.0 / PI * log(
                tan(PI / 4.0 + x * (PI / 180.0) / 2.0),
                E
            ) - 180.0 / PI * log(
                tan(PI / 4.0 + pn.xDbl * (PI / 180.0) / 2.0),
                E
            )) * pn.oneDegreeScaleFactor)) + pn.yCenter
        )

    // nexrad widget storm info
    fun computeMercatorNumbers(x: Float, y: Float, pn: ProjectionNumbers): List<Float> =
        listOf(
                ((-1.0f * ((y - pn.yDbl) * pn.oneDegreeScaleFactor)) + pn.xCenter.toFloat()).toFloat(),
                ((-1.0f * ((180.0f / PI * log(
                        tan(PI / 4.0 + x * (PI / 180.0f) / 2.0f),
                        E
                ) - 180.0f / PI * log(
                        tan(PI / 4.0f + pn.xDbl * (PI / 180.0f) / 2.0f),
                        E
                )) * pn.oneDegreeScaleFactor)) + pn.yCenter).toFloat()
        )
}

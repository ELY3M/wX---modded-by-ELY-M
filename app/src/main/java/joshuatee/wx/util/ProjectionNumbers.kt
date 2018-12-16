/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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

import android.content.Context
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.vis.UtilityUSImgNWSGOESMercator

class ProjectionNumbers {

    var scale: Double = 0.0
    var scaleFloat: Float = 0.0f
    var oneDegreeScaleFactor: Double = 0.0
    var x: String = "0.0"
    var y: String = "0.0"
    var xCenter: Double = 0.0
    var yCenter: Double = 0.0
    var polygonWidth: Double = 2.0
    var radarSite: String = ""

    constructor()

    constructor(
        scalef: Double,
        lat: String,
        lon: String,
        xImageCenterPixels: Double,
        yImageCenterPixels: Double
    ) {
        scale = scalef
        scaleFloat = scale.toFloat()
        x = lat
        y = lon
        xCenter = xImageCenterPixels
        yCenter = yImageCenterPixels
        oneDegreeScaleFactor = UtilityMath.pixPerDegreeLon(xDbl, scale)
    }

    constructor(context: Context, radarSite: String, provider: ProjectionType) {
        this.radarSite = radarSite
        xCenter = 0.0
        yCenter = 0.0
        when (provider) {
            ProjectionType.NWS_MOSAIC -> {
                scale = 55.50
                xCenter = 1700.0
                yCenter = 800.0
            }
            ProjectionType.NWS_MOSAIC_SECTOR -> if (radarSite == "hawaii") {
                scale = 62.00
                xCenter = 300.0
                yCenter = 285.0
            } else {
                scale = 55.50
                xCenter = 420.0
                yCenter = 400.0
            }
            ProjectionType.WX_RENDER -> {
                scale = 38.00 * MyApplication.widgetNexradSize
                xCenter = 500.0
                yCenter = 500.0
                polygonWidth = 2.0
            }
            ProjectionType.WX_RENDER_48 -> {
                scale = 450.00
                xCenter = 500.0
                yCenter = 500.0
                polygonWidth = 1.0
            }
            ProjectionType.WX_OGL -> {
                scale = 190.00
                xCenter = 0.0
                yCenter = 0.0
                polygonWidth = 1.0
            }
            ProjectionType.WX_OGL_48 -> {
                scale = 450.00
                xCenter = 0.0
                yCenter = 0.0
                polygonWidth = 1.0
            }
            else -> {
            }
        }
        x = Utility.readPref(context, "RID_" + radarSite + "_X", "0.00")
        y = Utility.readPref(context, "RID_" + radarSite + "_Y", "0.00")
        if (provider === ProjectionType.NWS_GOES) {
            val mnTmp = UtilityUSImgNWSGOESMercator.getMercatorNumbers(context, radarSite)
            x = mnTmp.x
            y = mnTmp.y
            scale = mnTmp.scale
            xCenter = mnTmp.xCenter
            yCenter = mnTmp.yCenter
        }
        oneDegreeScaleFactor = UtilityMath.pixPerDegreeLon(xDbl, scale)
        scaleFloat = scale.toFloat()
    }

    val oneDegreeScaleFactorFloat: Float
        get() = oneDegreeScaleFactor.toFloat()

    val xDbl: Double
        get() = x.toDoubleOrNull() ?: 0.0

    val xFloat: Float
        get() = x.toFloatOrNull() ?: 0.0f

    val yDbl: Double
        get() = y.toDoubleOrNull() ?: 0.0

    val yFloat: Float
        get() = y.toFloatOrNull() ?: 0.0f
}



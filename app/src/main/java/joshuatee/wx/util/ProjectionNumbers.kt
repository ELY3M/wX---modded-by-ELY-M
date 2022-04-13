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

import joshuatee.wx.MyApplication
import joshuatee.wx.objects.ProjectionType

class ProjectionNumbers {

    var scale = 0.0
    var scaleFloat = 0.0f
    var oneDegreeScaleFactor = 0.0
    var x = "0.0"
    var y = "0.0"
    var xCenter = 0.0
    var yCenter = 0.0
    var polygonWidth = 2.0
    var radarSite = ""

    constructor()

    constructor(scale: Double, lat: String, lon: String, xImageCenterPixels: Double, yImageCenterPixels: Double) {
        this.scale = scale
        scaleFloat = scale.toFloat()
        x = lat
        y = lon
        xCenter = xImageCenterPixels
        yCenter = yImageCenterPixels
        oneDegreeScaleFactor = UtilityMath.pixPerDegreeLon(xDbl, scale)
    }

    constructor(radarSite: String, projectionType: ProjectionType) {
        this.radarSite = radarSite
        xCenter = 0.0
        yCenter = 0.0
        when (projectionType) {
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
        }
        x = Utility.getRadarSiteX(radarSite)
        y = Utility.getRadarSiteY(radarSite)
        oneDegreeScaleFactor = UtilityMath.pixPerDegreeLon(xDbl, scale)
        scaleFloat = scale.toFloat()
    }

    val oneDegreeScaleFactorFloat
        get() = oneDegreeScaleFactor.toFloat()

    val xDbl
        get() = To.double(x)

    val xFloat
        get() = To.float(x)

    val yDbl
        get() = To.double(y)

    val yFloat
        get() = To.float(y)
}

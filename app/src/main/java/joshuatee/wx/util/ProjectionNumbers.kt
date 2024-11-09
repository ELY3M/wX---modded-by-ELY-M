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

package joshuatee.wx.util

import joshuatee.wx.objects.LatLon
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.radar.RadarSites

class ProjectionNumbers {

    var scaleFloat = 0.0f
    var oneDegreeScaleFactor = 0.0
    var xCenter = 0.0
    var yCenter = 0.0
    var polygonWidth = 2.0
    var radarSite = ""
    private var latLon = LatLon()

    constructor()

    constructor(radarSite: String, projectionType: ProjectionType) {
        this.radarSite = radarSite
        when (projectionType) {
            ProjectionType.WX_RENDER -> {
                scaleFloat = 38.00f * UIPreferences.widgetNexradSize
                xCenter = 500.0
                yCenter = 500.0
                polygonWidth = 2.0
            }

            ProjectionType.WX_RENDER_48 -> {
                scaleFloat = 450.00f
                xCenter = 500.0
                yCenter = 500.0
                polygonWidth = 1.0
            }

            ProjectionType.WX_OGL -> {
                scaleFloat = 190.00f
                xCenter = 0.0
                yCenter = 0.0
                polygonWidth = 1.0
            }
        }
        latLon = RadarSites.getLatLon(radarSite).reverse()
        oneDegreeScaleFactor = UtilityMath.pixPerDegreeLon(xDbl, scaleFloat.toDouble())
    }

    val oneDegreeScaleFactorFloat
        get() = oneDegreeScaleFactor.toFloat()

    val xDbl
        get() = latLon.lat

    val xFloat
        get() = xDbl.toFloat()

    val yDbl
        get() = latLon.lon

    val yFloat
        get() = yDbl.toFloat()
}

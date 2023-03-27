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
//modded by ELY M.

package joshuatee.wx.radar

import joshuatee.wx.objects.LatLon
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.ProjectionNumbers

class NexradRenderState(val paneNumber: Int, val data: NexradRenderData, val scaleLength: (Float) -> Float) {

    companion object {
        // all static used by RecordingSession.kt for draw tool
        var ridGlobal = ""
        var positionXGlobal = 0.0f
        var positionYGlobal = 0.0f
        const val ortIntGlobal = 400
        var oneDegreeScaleFactorGlobal = 0.0f
    }

    var x = 0.0f
        set(x) {
            field = x
            positionXGlobal = x
        }
    var y = 0.0f
        set(y) {
            field = y
            positionYGlobal = y
        }
    var zoom: Float = 1.0f
        set(scale) {
            field = scale
            listOf(data.locationDotBuffers, data.spotterBuffers, data.tvsBuffers, data.wbCircleBuffers).forEach {
                if (it.isInitialized) {
                    it.lenInit = it.type.size
                    it.lenInit = scaleLength(it.lenInit)
                    it.draw(projectionNumbers)
                }
            }

            //elys mod - testing and hope this help crashing on hail icons
            data.hiBuffersList.forEach {
                if (it.isInitialized) {
                    it.lenInit = it.type.size
                    it.lenInit = scaleLength(it.lenInit)
                    it.draw(projectionNumbers)
                }
            }

            //elys mod
            if (data.locationDotBuffers.isInitialized && RadarPreferences.locationDotFollowsGps) {
                data.locIconBuffers.lenInit = 0f //was locationDotBuffers.lenInit
                NexradRenderUtilities.genLocdot(data.locIconBuffers, projectionNumbers, gpsLatLon)
            }

        }




    var rid = ""
        set(rid) {
            field = rid
            ridGlobal = rid
        }
    val projectionType = ProjectionType.WX_OGL
    var bgColorFRed = 0.0f
    var bgColorFGreen = 0.0f
    var bgColorFBlue = 0.0f
    val ortInt = 400
    val zoomScreenScaleFactor = if (UtilityUI.isTablet()) {
        2.0
    } else {
        1.0
    }
    var product = "N0Q"
    var projectionNumbers = ProjectionNumbers()
    var gpsLatLon = LatLon(0.0, 0.0)
    var gpsLatLonTransformed = floatArrayOf(0.0f, 0.0f)
    // used in the filename for various radar files, possibly states at "1" and not "0"
    var indexString = "0"
    // this string is normally no string but for dual pane will be set to either 1 or 2 to differentiate timestamps
    var timeStampId = ""
    // list of radar sites when long press occurs
    var closestRadarSites = listOf<RID>()
    // is the user pressing and holding on the screen
    var displayHold = false
}

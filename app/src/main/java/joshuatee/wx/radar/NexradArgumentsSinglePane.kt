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

import joshuatee.wx.settings.Location
import joshuatee.wx.util.To

class NexradArgumentsSinglePane : NexradArguments() {

    var urlStr = ""
    var fixedSite = false

    // set when fixedSite is true
    var originalRadarSite = ""
    var arguments: Array<String>? = null

    fun process(arguments: Array<String>?) {
        this.arguments = arguments
        // set static var to false on start of activity
        WXGLRadarActivity.spotterShowSelected = false
        if (arguments != null) {
            // SPC Storm reports Level 2 archived radar
            // example: arrayOf(radarSite, "", prod, "", patternL2, x, y)
            if (arguments.size > 6) {
                urlStr = arguments[4]
                locXCurrent = To.double(arguments[5])
                locYCurrent = To.double(arguments[6])
                archiveMode = true
            } else if (arguments.size > 4) {
                // Invoked from spotters activity, show nearest radar and spotter text for one
                // example: arrayOf(radarSite, "", "N0Q", "", spotterList[position].unique)
                WXGLRadarActivity.spotterId = arguments[4]
                WXGLRadarActivity.spotterShowSelected = true
            }
            // invoked from an alert, example: arrayOf(radarSite, state, "N0Q", "")
            if (arguments.size > 3) {
                fixedSite = true
                originalRadarSite = arguments[0]
            }
            if (arguments.size < 7) {
                archiveMode = false
            }
        }
        locXCurrent = Location.latLon.lat
        locYCurrent = Location.latLon.lon
    }
}

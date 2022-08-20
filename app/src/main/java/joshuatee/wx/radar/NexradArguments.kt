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

class NexradArguments {

    var urlStr = ""
    var fixedSite = false
    var locXCurrent = ""
    var locYCurrent = ""
    var archiveMode = false

    fun processArguments(arguments: Array<String>?) {
        if (arguments != null) {
            // SPC Storm reports Level 2 archived radar
            // example: arrayOf(radarSite, "", prod, "", patternL2, x, y)
            if (arguments.size > 6) {
                urlStr = arguments[4]
                locXCurrent = arguments[5]
                locYCurrent = arguments[6]
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
            }
            if (arguments.size < 7) {
                archiveMode = false
            }
        }
    }
}

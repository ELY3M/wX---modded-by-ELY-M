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

class NexradArgumentsMultipane : NexradArguments() {

    var doNotSavePref = false
    var useSinglePanePref = false
    var numberOfPanes = 0
    var arguments: Array<String>? = null

    fun process(arguments: Array<String>?) {
        this.arguments = arguments
        if (arguments != null && arguments.size > 3) {
            if (arguments[3] == "true") { // invoked from single pane radar
                doNotSavePref = true
                useSinglePanePref = true
            }
            if (arguments[3] == "false") { // invoked from single pane when single pane is invoked from an alert
                doNotSavePref = true
                useSinglePanePref = false
            }
        }
        numberOfPanes = To.int(arguments!![2])
        locXCurrent = Location.latLon.lat
        locYCurrent = Location.latLon.lon
    }
}

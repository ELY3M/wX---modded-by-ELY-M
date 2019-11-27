/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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

package joshuatee.wx.activitiesmisc

import android.content.Context
import joshuatee.wx.MyApplication
import joshuatee.wx.external.ExternalDuplicateRemover
import joshuatee.wx.objects.PolygonType

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityTime

// encapsulates VTEC data and count for tst,tor, or ffw

internal class SevereWarning(private val type: PolygonType) {

    var text = ""
        private set
    var count = 0
        private set

    var collapsed = false
    var idList = listOf<String>()
    var areaDescList = listOf<String>()
    var effectiveList = listOf<String>()
    var expiresList = listOf<String>()
    var eventList = listOf<String>()
    var senderNameList = listOf<String>()
    var warnings = listOf<String>()

    fun toggleCollapsed() {
        if (collapsed) {
            collapsed = false
        } else {
            collapsed = true
        }
    }

    fun getName(): String {
        var name = ""
        when (type) {
            PolygonType.TOR -> name = "Tornado Warning"
            PolygonType.TST -> name = "Severe Thunderstorm Warning"
            PolygonType.FFW -> name = "Flash Flood Warning"
            else -> {}
        }
        return name
    }

    fun generateString(context: Context, html: String) {
        var vtecComponents: List<String>
        var wfo: String
        var wfoLocation = ""
        idList = html.parseColumn("\"id\": \"(NWS.*?)\"")
        areaDescList = html.parseColumn("\"areaDesc\": \"(.*?)\"")
        effectiveList = html.parseColumn("\"effective\": \"(.*?)\"")
        expiresList = html.parseColumn("\"expires\": \"(.*?)\"")
        eventList = html.parseColumn("\"event\": \"(.*?)\"")
        senderNameList = html.parseColumn("\"senderName\": \"(.*?)\"")
        var label = ""
        when (type) {
            PolygonType.TOR -> label = "Tornado Warnings"
            PolygonType.TST -> label = "Severe Thunderstorm Warnings"
            PolygonType.FFW -> label = "Flash Flood Warnings"
            else -> {
            }
        }
        warnings = html.parseColumn(RegExp.warningVtecPattern)
        warnings.forEach {
            val vtecIsCurrent = UtilityTime.isVtecCurrent(it)
            if (!it.startsWith("O.EXP") && vtecIsCurrent) {
                text += it
                count += 1
                vtecComponents = it.split(".")
                if (vtecComponents.size > 1) {
                    wfo = vtecComponents[2]
                    wfo = wfo.replace("^[KP]".toRegex(), "")
                    wfoLocation = Utility.readPref(context, "NWS_LOCATION_$wfo", "")
                }
                text += "  " + wfoLocation + MyApplication.newline
            }
        }
        val remover = ExternalDuplicateRemover()
        text = remover.stripDuplicates(text)
        text = "(" + text.split(MyApplication.newline).dropLastWhile { it.isEmpty() }.size +
                ") " + label + MyApplication.newline +
                text.replace((MyApplication.newline + "$").toRegex(), "")
    }
}


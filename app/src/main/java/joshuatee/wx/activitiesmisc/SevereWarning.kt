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

package joshuatee.wx.activitiesmisc

import android.content.Context
import android.util.Log
import joshuatee.wx.MyApplication
import joshuatee.wx.external.ExternalDuplicateRemover
import joshuatee.wx.objects.PolygonType

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp
import joshuatee.wx.util.Utility

// encapsulates VTEC data and count for tst, tor, ffw, smw or sps

internal class SevereWarning(private val type: PolygonType) {

    var TAG = "SevereWarning"
    var text = ""
        private set
    var count = 0
        private set

    fun generateString(context: Context, textTor: String) {
        var nwsOfficeArr: List<String>
        var nwsOffice: String
        var nwsLoc = ""
        var label = ""
        when (type) {
            PolygonType.TOR -> label = "Tornado Warnings"
            PolygonType.TST -> label = "Severe Thunderstorm Warnings"
            PolygonType.FFW -> label = "Flash Flood Warnings"
            PolygonType.SMW -> label = "Special Marine Warnings"
            //PolygonType.SPS -> label = "Special Weather Statement"
            else -> {
                Log.i(TAG, "no label\n")
            }
        }

        //FIXME not all warnings/statments have vtec//
        val warningAl = textTor.parseColumn(RegExp.warningVtecPattern)
        Log.i(TAG, textTor+"\n");
        count = warningAl.size
        warningAl.forEach {
            text += it
            Log.i(TAG, it+"\n");
            nwsOfficeArr = it.split(".")
            if (nwsOfficeArr.size > 1) {
                nwsOffice = nwsOfficeArr[2]
                nwsOffice = nwsOffice.replace("^[KP]".toRegex(), "")
                nwsLoc = Utility.readPref(context, "NWS_LOCATION_$nwsOffice", "")
            }
            text += "  " + nwsLoc + MyApplication.newline
            Log.i(TAG, "nwsOfficeArr: "+nwsOfficeArr)
            Log.i(TAG, "nwsOffice: "+nwsOfficeArr[2])
            Log.i(TAG, "nwsLoc: "+nwsLoc)

        }
        val remover = ExternalDuplicateRemover()
        text = remover.stripDuplicates(text)
        text = "(" + text.split(MyApplication.newline).dropLastWhile { it.isEmpty() }.size + ") " + label + MyApplication.newline + text.replace((MyApplication.newline + "$").toRegex(), "")
    }
}


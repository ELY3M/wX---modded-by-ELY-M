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
import joshuatee.wx.Extensions.parseColumn
import joshuatee.wx.MyApplication
import joshuatee.wx.RegExp
import joshuatee.wx.external.ExternalDuplicateRemover
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog

//parses specical weather statements


internal class SpecialWeather(private val type: PolygonType) {

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
            PolygonType.SPS -> label = "Special Weather Statement"
            else -> {
            }
        }
        val warningAl = textTor.parseColumn(RegExp.warningLatLonPattern)
        count = warningAl.size
        warningAl.forEach {
            text += it
            nwsOfficeArr = it.split(".")
            if (nwsOfficeArr.size > 1) {
                nwsOffice = nwsOfficeArr[2]
                nwsOffice = nwsOffice.replace("^[KP]".toRegex(), "")
                nwsLoc = Utility.readPref(context, "NWS_LOCATION_$nwsOffice", "")
            }
            text += "  " + nwsLoc + MyApplication.newline
        }
        val remover = ExternalDuplicateRemover()
        text = remover.stripDuplicates(text)
        text = "(" + text.split(MyApplication.newline).dropLastWhile { it.isEmpty() }.size +
                ") " + label + MyApplication.newline +
                text.replace((MyApplication.newline + "$").toRegex(), "")
    }
}







/*
internal class SpecialWeather(private val type: PolygonType) {

    var TAG = "SpecialWeather"
    var text = ""
    var label = ""
        private set
    var count = 0
        private set

    init {
        when (type) {
            PolygonType.SPS -> label = "SPS"
            else -> {
            }
        }


        UtilityLog.d(TAG, "Trying to parse SPS......\n")
    }


    fun generateString(context: Context, textSps: String) {
        var label = ""
        when (type) {
            PolygonType.SPS -> label = "Special Weather Statement"
            else -> {
            }
        }


        UtilityLog.d(TAG, "textsps: "+textSps+"\n");
        //"eventMotionDescription": [
        // "2018-09-23T17:19:00.000-04:00...storm...249DEG...13KT...37.13,-81.47"
        //"id": "https://api.weather.gov/alerts/NWS-IDP-PROD-3217762"
        //FIXME not all warnings/statments have vtec//
        //"id": "https://api.weather.gov/alerts/NWS-IDP-PROD-3217762"

        var SPSText = MyApplication.severeDashboardSps.valueGet()
        val urlList = SPSText.parseColumn("\"id\"\\: .(https://api.weather.gov/alerts/NWS-IDP-.*?)\"")
        SPSText = SPSText.replace("\n", "")
        SPSText = SPSText.replace(" ", "")
        val polygonArr = SPSText.parseColumn(RegExp.warningLatLonPattern)

        UtilityLog.d(TAG, "urllist: "+urlList)
        val warningAl = textSps.parseColumn(RegExp.warningLatLonPattern)
        //Log.i(TAG, "warningAl: "+warningAl)

        count = polygonArr.size

        urlList.forEach {
            UtilityLog.d(TAG, "foreach it: "+it)
            //text += it
            UtilityLog.d(TAG, "for each text: "+text)

        }

        polygonArr.forEach {
            UtilityLog.d(TAG, "polygonArr it: "+it)
            //text += it
            MyApplication.SPSLatlon.valueSet(context, it)
        }

        val spsremover = ExternalDuplicateRemover()
        text = spsremover.stripDuplicates(text)
        text = "(" + text.split(MyApplication.newline).dropLastWhile { it.isEmpty() }.size + ") " + label + MyApplication.newline + text.replace((MyApplication.newline + "$").toRegex(), "")


        count++
        UtilityLog.d(TAG, "spscount: "+count+"\n");
        val remover = ExternalDuplicateRemover()
        text = remover.stripDuplicates(text)
        text = "(" + text.split(MyApplication.newline).dropLastWhile { it.isEmpty() }.size + ") " + label + MyApplication.newline + text.replace((MyApplication.newline + "$").toRegex(), "")
        UtilityLog.d(TAG, "text: "+text+"\n");


    }

}

*/


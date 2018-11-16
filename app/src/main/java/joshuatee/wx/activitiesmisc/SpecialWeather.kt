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
import androidx.appcompat.app.AppCompatDelegate
import joshuatee.wx.Extensions.parseColumn
import joshuatee.wx.MyApplication
import joshuatee.wx.RegExp
import joshuatee.wx.external.ExternalDuplicateRemover
import joshuatee.wx.external.ExternalPoint
import joshuatee.wx.external.ExternalPolygon
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.util.Utility

//parses specical weather statements

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


        Log.i(TAG, "Trying to parse SPS......\n")
    }


    fun generateString(context: Context, textSps: String) {
        var label = ""
        when (type) {
            PolygonType.SPS -> label = "Special Weather Statement"
            else -> {
            }
        }


        Log.i(TAG, "textsps: "+textSps+"\n");
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

        Log.i(TAG, "urllist: "+urlList)
        //val warningAl = textSps.parseColumn(RegExp.warningLatLonPattern)
        //Log.i(TAG, "warningAl: "+warningAl)

        count = polygonArr.size

        urlList.forEach {
            Log.i(TAG, "foreach it: "+it)
            //text += it
            Log.i(TAG, "for each text: "+text)

        }

        polygonArr.forEach {
            Log.i(TAG, "polygonArr it: "+it)
            //text += it
            MyApplication.SPSLatlon.valueSet(context, it)
        }

        val spsremover = ExternalDuplicateRemover()
        text = spsremover.stripDuplicates(text)
        text = "(" + text.split(MyApplication.newline).dropLastWhile { it.isEmpty() }.size + ") " + label + MyApplication.newline + text.replace((MyApplication.newline + "$").toRegex(), "")


        count++
        Log.i(TAG, "spscount: "+count+"\n");
        val remover = ExternalDuplicateRemover()
        text = remover.stripDuplicates(text)
        text = "(" + text.split(MyApplication.newline).dropLastWhile { it.isEmpty() }.size + ") " + label + MyApplication.newline + text.replace((MyApplication.newline + "$").toRegex(), "")
        Log.i(TAG, "text: "+text+"\n");


    }


    fun showspsProducts(lat: Double, lon: Double): String {
        var SPSText = MyApplication.severeDashboardSps.valueGet()
        val urlList = SPSText.parseColumn("\"id\"\\: .(https://api.weather.gov/alerts/NWS-IDP-.*?)\"")
        SPSText = SPSText.replace("\n", "")
        SPSText = SPSText.replace(" ", "")
        val polygonArr = SPSText.parseColumn(RegExp.warningLatLonPattern)
        val vtecAl = SPSText.parseColumn(RegExp.warningVtecPattern)
        var retStr = ""
        var testArr: List<String>
        var q = 0
        var notFound = true
        var polyCount = -1
        polygonArr.forEach { polys ->
            polyCount += 1
            if (vtecAl.size > polyCount && !vtecAl[polyCount].startsWith("0.EXP") && !vtecAl[polyCount].startsWith("0.CAN")) {
                val polyTmp = polys.replace("[", "").replace("]", "").replace(",", " ")
                testArr = polyTmp.split(" ").dropLastWhile { it.isEmpty() }
                val y = testArr.asSequence().filterIndexed { idx: Int, _: String -> idx and 1 == 0 }.map {
                    it.toDoubleOrNull() ?: 0.0
                }.toList()
                val x = testArr.asSequence().filterIndexed { idx: Int, _: String -> idx and 1 != 0 }.map {
                    it.toDoubleOrNull() ?: 0.0
                }.toList()
                if (y.size > 3 && x.size > 3 && x.size == y.size) {
                    val poly2 = ExternalPolygon.Builder()
                    x.indices.forEach { j -> poly2.addVertex(ExternalPoint(x[j].toFloat(), y[j].toFloat())) }
                    val polygon2 = poly2.build()
                    val contains = polygon2.contains(ExternalPoint(lat.toFloat(), lon.toFloat()))
                    if (contains && notFound) {
                        retStr = urlList[q]
                        notFound = false
                    }
                }
            }
            q += 1
        }
        return retStr
    }


}


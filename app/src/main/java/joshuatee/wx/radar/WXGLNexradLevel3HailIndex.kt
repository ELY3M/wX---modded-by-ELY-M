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

import android.content.Context
import joshuatee.wx.external.ExternalGeodeticCalculator
import joshuatee.wx.external.ExternalGlobalCoordinates

import joshuatee.wx.Extensions.*
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.util.UtilityLog
import java.io.File
import java.util.regex.Pattern

internal object WXGLNexradLevel3HailIndex {

    private const val hiBaseFn = "nids_hi_tab"
    var hailList = mutableListOf<Hail>()

    private val pattern1: Pattern = Pattern.compile("AZ/RAN(.*?)V")
    private val pattern2: Pattern = Pattern.compile("POSH/POH(.*?)V")
    private val pattern3: Pattern = Pattern.compile("MAX HAIL SIZE(.*?)V")
    private val pattern4: Pattern = Pattern.compile("[0-9]*\\.?[0-9]+")
    private val pattern5: Pattern = Pattern.compile("\\d+")

    var hailSizeNumber = 0.0
    var hailSizeText = "Unknown"
    var hailSizeIcon = "hailunknown.png"


    fun decodeAndPlot(context: Context, radarSite: String, fnSuffix: String): List<Double> {
        val fileName = hiBaseFn + fnSuffix
        val stormList = mutableListOf<Double>()
        val location = UtilityLocation.getSiteLocation(radarSite)
        //make sure we clear the list or we get duplicate texts
        hailList.clear()
        ///File("/sdcard/wX/hailtest").copyTo(File("/data/user/0/joshuatee.wx/files/nids_hi_tab0"), true)
        WXGLDownload.getNidsTab(context, "HI", radarSite, fileName)
        val posn: List<String>
        val hailPercent: List<String>
        val hailSize: List<String>
        try {
            val data = UtilityLevel3TextProduct.readFile(context, fileName)
            posn = data.parseColumn(pattern1)
            hailPercent = data.parseColumn(pattern2)
            hailSize = data.parseColumn(pattern3)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
            return listOf()
        }
        var posnStr = ""
        posn.forEach {
            posnStr += it.replace("/", " ")
        }
        var hailPercentStr = ""
        hailPercent.forEach {
            hailPercentStr += it.replace("/", " ")
        }
        hailPercentStr = hailPercentStr.replace("UNKNOWN", " 0 0 ")
        var hailSizeStr = ""
        hailSize.forEach {
            hailSizeStr += it.replace("/", " ")
        }
        hailSizeStr = hailSizeStr.replace("UNKNOWN", " 0.00 ")
        hailSizeStr = hailSizeStr.replace("<0.25", " 0.24 ")
        hailSizeStr = hailSizeStr.replace("<0.50", " 0.49 ")
        val posnNumbers = posnStr.parseColumnAll(pattern5)
        val hailPercentNumbers = hailPercentStr.parseColumnAll(pattern5)
        val hailSizeNumbers = hailSizeStr.parseColumnAll(pattern4)
        if (posnNumbers.size == hailPercentNumbers.size && posnNumbers.size > 1 && hailSizeNumbers.isNotEmpty()) {
            var k = 0 // k is used to track hail size which is /2 of other 2 arrays
            for (s in posnNumbers.indices step 2) {
                val hailSizeDbl = hailSizeNumbers[k].toDoubleOrNull() ?: 0.0
                //var hailSizeText = "Unknown"
                //var hailSizeIcon = "hailunknown.png"
                //UtilityLog.d("hailindex", "hailSizeNumbers: "+hailSizeDbl)
                //UtilityLog.d("hailindex", "hailPercentNumbers: "+hailPercentNumbers[s].toIntOrNull())
                if (hailSizeDbl > 0.24 && ((hailPercentNumbers[s].toIntOrNull() ?: 0) > 60 || (hailPercentNumbers[s + 1].toDoubleOrNull() ?: 0.0) > 60)) {
                    val ecc = ExternalGeodeticCalculator()
                    val degree = posnNumbers[s].toDoubleOrNull() ?: 0.0
                    val nm = posnNumbers[s + 1].toDoubleOrNull() ?: 0.0
                    val start = ExternalGlobalCoordinates(location)
                    val ec = ecc.calculateEndingGlobalCoordinates(start, degree, nm * 1852.0)
                    stormList.add(ec.latitude)
                    stormList.add(ec.longitude * -1.0)

                    //FIXME make this pick a icon!

                    /*
                    if (hailSizeDbl > 0.99) {
                        stormList.add(ec.latitude + markerSize)
                        stormList.add(ec.longitude * -1.0)
                    }
                    if (hailSizeDbl > 1.99) {
                        stormList.add(ec.latitude + markerSize * 2.0)
                        stormList.add(ec.longitude * -1.0)
                    }
                    if (hailSizeDbl > 2.99) {
                        stormList.add(ec.latitude + markerSize * 3.0)
                        stormList.add(ec.longitude * -1.0)
                    }
                    if (hailSizeDbl > 3.99) {
                        stormList.add(ec.latitude + markerSize * 4.0)
                        stormList.add(ec.longitude * -1.0)
                    }
                    */


                    hailSizeText = hailSizeDbl.toString()
                    
                    if (hailSizeDbl > 0.24) {
                        hailSizeNumber = 0.25
                        hailSizeText = "0.25"
                        hailSizeIcon = "hail05.png"
                    }

                    if (hailSizeDbl > 0.49) {
                        hailSizeNumber = 0.50
                        hailSizeText = "0.50"
                        hailSizeIcon = "hail0.png"
                    }
                    if (hailSizeDbl > 0.74) {
                        hailSizeNumber = 0.75
                        hailSizeText = "0.75"
                        hailSizeIcon = "hail0.png"
                    }
                    if (hailSizeDbl > 0.99) {
                        hailSizeNumber = 1.00
                        hailSizeText = "1.00"
                        hailSizeIcon = "hail1.png"
                    }
                    if (hailSizeDbl > 1.49) {
                        hailSizeNumber = 1.50
                        hailSizeText = "1.50"
                        hailSizeIcon = "hail1.png"
                    }
                    if (hailSizeDbl > 1.99) {
                        hailSizeNumber = 2.00
                        hailSizeText = "2.00"
                        hailSizeIcon = "hail2.png"
                    }
                    if (hailSizeDbl > 2.49) {
                        hailSizeNumber = 2.50
                        hailSizeText = "2.50"
                        hailSizeIcon = "hail2.png"
                    }
                    if (hailSizeDbl > 2.99) {
                        hailSizeNumber = 3.00
                        hailSizeText = "3.00"
                        hailSizeIcon = "hail3.png"
                    }
                    if (hailSizeDbl > 3.49) {
                        hailSizeNumber = 3.50
                        hailSizeText = "3.50"
                        hailSizeIcon = "hail3.png"
                    }
                    if (hailSizeDbl > 3.99) {
                        hailSizeNumber = 4.00
                        hailSizeText = "4.00"
                        hailSizeIcon = "hail4.png"
                    }
                    if (hailSizeDbl > 4.49) {
                        hailSizeNumber = 4.50
                        hailSizeText = "4.50"
                        hailSizeIcon = "hail4.png"
                    }
                    //big hail --- only use 1 icon for any hail over 5 inch
                    if (hailSizeDbl > 4.99) {
                        hailSizeNumber = 5.00
                        hailSizeText = "Big Hail!"
                        hailSizeIcon = "hailbig.png"
                    }


                    hailList.add(Hail(hailSizeIcon, hailSizeText, hailSizeNumber, ec.latitude, ec.longitude * -1.0))


                    UtilityLog.d("hailindex", "hailSizeIcon: "+hailSizeIcon)
                    UtilityLog.d("hailindex", "hailSizeText: "+hailSizeText)
                    UtilityLog.d("hailindex", "hailSizeNumber: "+hailSizeNumber)
                    UtilityLog.d("hailindex", "hailSizeDbl: "+hailSizeDbl)



                }
                k += 1
            }
        }

        return stormList
    }



}

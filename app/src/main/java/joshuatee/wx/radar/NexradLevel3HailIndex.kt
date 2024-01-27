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
//modded by ELY M.

package joshuatee.wx.radar

import joshuatee.wx.external.ExternalGeodeticCalculator
import joshuatee.wx.external.ExternalGlobalCoordinates
import joshuatee.wx.objects.OfficeTypeEnum
import joshuatee.wx.parseColumn
import joshuatee.wx.parseColumnAll
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityLog
import java.util.regex.Pattern

internal object NexradLevel3HailIndex {
    private val pattern1: Pattern = Pattern.compile("AZ/RAN(.*?)V")
    private val pattern2: Pattern = Pattern.compile("POSH/POH(.*?)V")
    private val pattern3: Pattern = Pattern.compile("MAX HAIL SIZE(.*?)V")
    private val pattern4: Pattern = Pattern.compile("[0-9]*\\.?[0-9]+")
    private val pattern5: Pattern = Pattern.compile("\\d+")
    val hailList = mutableListOf<Hail>()
    fun decode(radarSite: String): List<Hail> {
     	//var hailSizeDbl = 0.0
    	var hailSizeNumber = 0.0
    	var hailSizeText = "Unknown"
    	var hailSizeIcon = "hailunknown.png"
        val stormList = mutableListOf<Double>()
        val location = UtilityLocation.getSiteLocation(radarSite, OfficeTypeEnum.RADAR)
        //make sure we clear the list or we get duplicate texts and icons
        hailList.clear()
        //File("/sdcard/wX/hailtest").copyTo(File("/data/user/0/joshuatee.wx/files/nids_hi_tab0"), true)
        val data = NexradLevel3TextProduct.download("HI", radarSite)
        val posn = data.parseColumn(pattern1)
        val posnStr = posn.joinToString("")
                .replace("/", " ")

        val hailPercent = data.parseColumn(pattern2)
        val hailPercentStr = hailPercent.joinToString("")
                .replace("/", " ")
                .replace("UNKNOWN", " 0 0 ")

        val hailSize = data.parseColumn(pattern3)
        val hailSizeStr = hailSize.joinToString("")
                .replace("/", " ")
                .replace("UNKNOWN", " 0.00 ")
		.replace("<0.25", " 0.24 ")
                .replace("<0.50", " 0.49 ")

        val posnNumbers = posnStr.parseColumnAll(pattern5)
        val hailPercentNumbers = hailPercentStr.parseColumnAll(pattern5)
        val hailSizeNumbers = hailSizeStr.parseColumnAll(pattern4)
        if (posnNumbers.size == hailPercentNumbers.size && posnNumbers.size > 1 && hailSizeNumbers.isNotEmpty()) {
            var k = 0 // k is used to track hail size which is /2 of other 2 arrays
            for (s in posnNumbers.indices step 2) {
                val hailSizeDbl = To.double(hailSizeNumbers[k])
                if (hailSizeDbl > 0.24 && (To.int(hailPercentNumbers[s]) > 60 || To.double(hailPercentNumbers[s + 1]) > 60.0)) {
                    val degree = To.double(posnNumbers[s])
                    val nm = To.double(posnNumbers[s + 1])
                    val start = ExternalGlobalCoordinates(location)
                    val ec = ExternalGeodeticCalculator.calculateEndingGlobalCoordinates(start, degree, nm * 1852.0)
                    stormList.add(ec.latitude)
                    stormList.add(ec.longitude * -1.0)

                    hailSizeText = hailSizeDbl.toString()
                    hailSizeNumber = hailSizeDbl
                    
                    if (hailSizeDbl in 0.0..0.24) {
                        hailSizeIcon = "hail05.png"
                    }
                    if (hailSizeDbl in 0.49..0.98) {
                        hailSizeIcon = "hail0.png"
                    }
                    if (hailSizeDbl in 0.99..1.98) {
                        hailSizeIcon = "hail1.png"
                    }
                    if (hailSizeDbl in 1.99..2.98) {
                        hailSizeIcon = "hail2.png"
                    }
                    if (hailSizeDbl in 2.99..3.98) {
                        hailSizeIcon = "hail3.png"
                    }
                    if (hailSizeDbl in 3.99..4.98) {
                        hailSizeIcon = "hail4.png"
                    }
                    //big hail --- only use 1 icon for any hail over 5 inch
                    if (hailSizeDbl in 4.99..99.99) {
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

        return hailList
    }



}

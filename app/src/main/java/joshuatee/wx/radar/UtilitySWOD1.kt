/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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

import android.annotation.SuppressLint
import android.content.Context

import joshuatee.wx.MyApplication
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityString

import joshuatee.wx.Extensions.*
import joshuatee.wx.objects.DownloadTimer

internal object UtilitySwoD1 {

    var timer = DownloadTimer("SWO")

    @SuppressLint("UseSparseArrays")
    val HASH_SWO = mutableMapOf<Int, List<Double>>()

    fun get(context: Context) {
        if (timer.isRefreshNeeded(context)) {
            var retStr: String
            /*	... CATEGORICAL ...

		SLGT   26488256 27058145 27138124 27337986
		MRGL   28789106 30249015 31008913 31258721 31278684 30718447
		       30638425 29008048
		TSTM   28699165 30149157 30869191 31359271 31809310 32319337
		       33099340 33579303 34129258 34359194 34479089 34378920
		       34098685 33588443 32698161 31977981 99999999 33441119
		       34061155 35021134 35901083 36201040 37320921 38350840
		       39170739 39440690 39440630 39170596 38030631 37340639
		       36630646 35820694 34680763 33530770 33000803 32740833
		       32680870 32640991 33441119 99999999 43482148 43822052
		       43861949 43591907 43081905 42331997 41952061 41952098
		       41912155 42182207 42402218 42952203 43482148

		&&*/
            val threatList = listOf("HIGH", "MDT", "ENH", "SLGT", "MRGL")
            val day = 1
            val urlBlob =
                "${MyApplication.nwsSPCwebsitePrefix}/products/outlook/KWNSPTSDY" + day.toString() + ".txt"
            val html = urlBlob.getHtmlSep()
            val htmlBlob = html.parse("... CATEGORICAL ...(.*?&)&") // was (.*?)&&
            threatList.indices.forEach { it ->
                retStr = ""
                val threatLevelCode = threatList[it]
                val htmlList = htmlBlob.parseColumn(threatLevelCode.substring(1) + "(.*?)[A-Z&]")
                val warningList = mutableListOf<Double>()
                htmlList.indices.forEach { h ->
                    val coords = htmlList[h].parseColumn("([0-9]{8}).*?")
                    var xStrTmp: String
                    var yStrTmp: String
                    coords.forEach { temp ->
                        xStrTmp = temp.substring(0, 4)
                        yStrTmp = temp.substring(4, 8)
                        if (yStrTmp.matches("^0".toRegex())) {
                            yStrTmp = yStrTmp.replace("^0".toRegex(), "")
                            yStrTmp += "0"
                        }
                        xStrTmp = UtilityString.addPeriodBeforeLastTwoChars(xStrTmp)
                        yStrTmp = UtilityString.addPeriodBeforeLastTwoChars(yStrTmp)
                        try {
                            var tmpDbl = yStrTmp.toDoubleOrNull() ?: 0.0
                            if (tmpDbl < 40.00) {
                                tmpDbl += 100
                                yStrTmp = tmpDbl.toString()
                            }
                        } catch (e: Exception) {
                            UtilityLog.handleException(e)
                        }
                        retStr = "$retStr$xStrTmp $yStrTmp "
                    }
                    retStr += ":"
                    retStr = retStr.replace(" :", ":")
                }
                val tmpArr = MyApplication.colon.split(retStr)
                var testArr: Array<String>
                tmpArr.forEach { warn ->
                    testArr = MyApplication.space.split(warn)
                    val x = testArr.filterIndexed { idx: Int, _: String -> idx and 1 == 0 }.map {
                        it.toDoubleOrNull() ?: 0.0
                    }
                    val y = testArr.filterIndexed { idx: Int, _: String -> idx and 1 != 0 }.map {
                        (it.toDoubleOrNull() ?: 0.0) * -1.0
                    }
                    if (x.isNotEmpty() && y.isNotEmpty()) {
                        warningList.add(x[0])
                        warningList.add(y[0])
                        (1..x.size - 2).forEach { j ->
                            if (x[j] < 99.0) {
                                warningList.add(x[j])
                                warningList.add(y[j])
                                warningList.add(x[j])
                                warningList.add(y[j])
                            } else {
                                warningList.add(x[j - 1])
                                warningList.add(y[j - 1])
                                warningList.add(x[j + 1])
                                warningList.add(y[j + 1])
                            }
                        }
                        warningList.add(x.last())
                        warningList.add(y[x.lastIndex])
                    }
                    HASH_SWO[it] = warningList
                }
            }
        }
    }
}

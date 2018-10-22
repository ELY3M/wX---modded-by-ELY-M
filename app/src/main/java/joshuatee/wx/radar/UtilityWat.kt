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

package joshuatee.wx.radar

import android.content.Context
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.UtilityCanvasProjection
import joshuatee.wx.util.ProjectionNumbers

internal object UtilityWat {

    fun addWat(context: Context, provider: ProjectionType, rid1: String, type: PolygonType): List<Double> {
        var testArr: Array<String>
        val warningList = mutableListOf<Double>()
        var prefToken = ""
        when (type) {
            PolygonType.MCD -> prefToken = MyApplication.mcdLatlon.valueGet()
            PolygonType.WATCH -> prefToken = MyApplication.watchLatlon.valueGet()
            PolygonType.WATCH_TORNADO -> prefToken = MyApplication.watchLatlonTor.valueGet()
            PolygonType.MPD -> prefToken = MyApplication.mpdLatlon.valueGet()
            else -> {
            }
        }
        val pn = ProjectionNumbers(context, rid1, provider)
        var j: Int
        var pixXInit: Double
        var pixYInit: Double
        val textFfw = prefToken
        if (textFfw != "") {
            val tmpArr = MyApplication.colon.split(textFfw)
            tmpArr.forEach { it ->
                testArr = MyApplication.space.split(it)
                val x = testArr.filterIndexed { idx: Int, _: String -> idx and 1 == 0 }.map {
                    it.toDoubleOrNull() ?: 0.0
                }
                val y = testArr.filterIndexed { idx: Int, _: String -> idx and 1 != 0 }.map {
                    it.toDoubleOrNull() ?: 0.0
                }
                if (y.isNotEmpty() && x.isNotEmpty()) {
                    var tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(x[0], y[0], pn)
                    pixXInit = tmpCoords[0]
                    pixYInit = tmpCoords[1]
                    warningList.add(tmpCoords[0])
                    warningList.add(tmpCoords[1])
                    if (x.size == y.size) {
                        j = 1
                        while (j < x.size) {
                            tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(x[j], y[j], pn)
                            warningList.add(tmpCoords[0])
                            warningList.add(tmpCoords[1])
                            warningList.add(tmpCoords[0])
                            warningList.add(tmpCoords[1])
                            j += 1
                        }
                        warningList.add(pixXInit)
                        warningList.add(pixYInit)
                    }
                }
            }
        }
        return warningList
    }
}


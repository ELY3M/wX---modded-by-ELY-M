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

package joshuatee.wx.radar

import android.content.Context
import joshuatee.wx.objects.ObjectPolygonWarning
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.util.UtilityDownloadRadar
import joshuatee.wx.util.UtilityLog

internal object UtilityPolygonsDownload {

    private var initialized = false
    private var lastRefresh = 0.toLong()
    private const val REFRESH_LOC_MIN = 2

    fun get(context: Context) {
        val currentTime1 = System.currentTimeMillis()
        val currentTimeSec = currentTime1 / 1000
        val refreshIntervalSec = (REFRESH_LOC_MIN * 60).toLong()

       /* if (!PolygonType.TST.display) {
            UtilityDownloadRadar.clearPolygonVtec()
        }
        if (!PolygonType.MPD.display) {
            UtilityDownloadRadar.clearMpd()
        }
        if (!PolygonType.MCD.display) {
            UtilityDownloadRadar.clearMcd()
            UtilityDownloadRadar.clearWatch()
        }*/

        UtilityLog.d("wx", "RADAR DOWNLOAD CHECK")

        if (currentTimeSec > lastRefresh + refreshIntervalSec || !initialized) {
            // download data
            UtilityLog.d("wx", "RADAR DOWNLOAD INITIATED")
            initialized = true
            val currentTime = System.currentTimeMillis()
            lastRefresh = currentTime / 1000


            if (PolygonType.TST.pref) {
                UtilityDownloadRadar.getPolygonVtec(context)
            } else {
                //UtilityDownloadRadar.clearPolygonVtec()
            }
            /*ObjectPolygonWarning.polygonList.forEach {
                val polygonType = ObjectPolygonWarning.polygonDataByType[$0]!
                if (polygonType.isEnabled) {
                    UtilityDownloadRadar.getPolygonVtecByType(polygonType)
                } else {
                    UtilityDownloadRadar.getPolygonVtecByTypeClear(polygonType)
                }
            }*/
            if (PolygonType.MPD.pref) {
                //UtilityDownloadRadar.getMpd()
            } else {
                //UtilityDownloadRadar.clearMpd()
            }
            if (PolygonType.MCD.pref) {
                //UtilityDownloadRadar.getMcd()
                //UtilityDownloadRadar.getWatch()
            } else {
                //UtilityDownloadRadar.clearMcd()
                //UtilityDownloadRadar.clearWatch()
            }

            //initialized = true
            //val currentTime = System.currentTimeMillis()
            //lastRefresh = currentTime / 1000
        }
    }
}

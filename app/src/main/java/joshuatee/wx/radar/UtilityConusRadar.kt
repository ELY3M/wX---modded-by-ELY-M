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

import android.annotation.SuppressLint
import android.util.Log
import joshuatee.wx.MyApplication
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityString
import joshuatee.wx.Extensions.*

internal object UtilityConusRadar {

    var TAG = "UtilityConusRadar"
    private var initialized = false
    private var lastRefresh = 0.toLong()
    private const val REFRESH_LOC_MIN = 5


    fun getConus() {
        val currentTime1 = System.currentTimeMillis()
        val currentTimeSec = currentTime1 / 1000
        val refreshIntervalSec = (REFRESH_LOC_MIN * 60).toLong()
        if (currentTimeSec > lastRefresh + refreshIntervalSec || !initialized) {

            val url = MyApplication.NWS_CONUS_RADAR
            val urlgfw = MyApplication.NWS_CONUS_RADAR_GFW

            //READ and parase GFW file....
            var gfw = (urlgfw).getHtmlSep()
            val gfwArr = gfw.split("<br>").dropLastWhile { it.isEmpty() }
            //var tmpArr: List<String>
            gfwArr.forEach {
                    //spotterList.add(Spotter(tmpArr[0], tmpArr[1], tmpArr[2], tmpArr[3], tmpArr[4], tmpArr[5], tmpArr[6], tmpArr[7], tmpArr[8], tmpArr[9], tmpArr[10], tmpArr[11], tmpArr[12], tmpArr[13], tmpArr[14], tmpArr[15]))
                Log.i(TAG, gfwArr[0])
                Log.i(TAG, gfwArr[1])
                Log.i(TAG, gfwArr[2])
                Log.i(TAG, gfwArr[3])
                Log.i(TAG, gfwArr[4])
                Log.i(TAG, gfwArr[5])
                }
            }







            initialized = true
            val currentTime = System.currentTimeMillis()
            lastRefresh = currentTime / 1000
        }



    }

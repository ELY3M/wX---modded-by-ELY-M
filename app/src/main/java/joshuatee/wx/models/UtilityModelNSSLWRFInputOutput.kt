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

package joshuatee.wx.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable

import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.util.UtilityTime

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp

internal object UtilityModelNSSLWRFInputOutput {

    val runTime: RunTimeData
        get() {
            val runTimeData = RunTimeData()
            val htmlRunstatus = "http://wrf.nssl.noaa.gov/newsite/".getHtml()
            val currentRun = htmlRunstatus.parse("<p class=\"selected\"><a href=\"index.php.date=([0-9]{8}).amp")
            runTimeData.listRunClear()
            runTimeData.listRunAdd(currentRun)
            runTimeData.listRunAddAll(UtilityTime.genModelRuns(currentRun, 12, "yyMMddHH"))
            if (currentRun != "") {
                runTimeData.timeStrConv = currentRun.parse(RegExp.ncarEnsPattern2)
            }
            return runTimeData
        }

    fun getImage(sectorF: String, param: String, runF: String, time: String): Bitmap {
        var sector = sectorF
        var run = runF
        val timeLocal = UtilityStringExternal.truncate(time, 2)
        var runLast2 = ""
        if (run.length > 7) {
            runLast2 = run.substring(6, 8) + "Z/"
            run = run.substring(0, 6)
        }
        if (sector == "CONUS" || sector == "SJU" || sector == "HFO") {
            sector = ""
        }
        if (runLast2 == "00Z/") {
            runLast2 = ""
        }
        val url = "http://wrf.nssl.noaa.gov/$run/$runLast2$sector/$param$timeLocal.png"
        return url.getImage()
    }

    fun getAnimation(context: Context, sector: String, param: String, run: String, spinnerTimeValue: Int, listTime: List<String>): AnimationDrawable {
        if (spinnerTimeValue == -1) return AnimationDrawable()
        val bmAl = (spinnerTimeValue until listTime.size).mapTo(mutableListOf()) { k -> getImage(sector, param, run, listTime[k].split(" ").dropLastWhile { it.isEmpty() }[0]) }
        return UtilityImgAnim.getAnimationDrawableFromBMList(context, bmAl)
    }
}

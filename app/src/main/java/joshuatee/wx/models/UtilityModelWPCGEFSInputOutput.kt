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

package joshuatee.wx.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.MyApplication

import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.util.UtilityTime

internal object UtilityModelWPCGEFSInputOutput {

    // 00Z loads around 0540Z
    // 06Z loads around 1140Z
    // 12Z loads around 1740Z
    // 18Z loads around 2340Z
    val runTime: RunTimeData
        get() {
            val runData = RunTimeData()
            val currentHour = UtilityTime.currentHourInUTC
            runData.mostRecentRun = "00"
            if (currentHour in 12..17) {
                runData.mostRecentRun = "06"
            }
            if (currentHour >= 18) {
                runData.mostRecentRun = "12"
            }
            if (currentHour < 6) {
                runData.mostRecentRun = "18"
            }
            runData.listRunAdd("00")
            runData.listRunAdd("06")
            runData.listRunAdd("12")
            runData.listRunAdd("18")
            runData.timeStrConv = runData.mostRecentRun
            return runData
        }

    fun getImage(om: ObjectModel, time: String): Bitmap {
        var sectorAdd = ""
        if (om.sector == "AK") {
            sectorAdd = "_ak"
        }
        val imgUrl =
                "${MyApplication.nwsWPCwebsitePrefix}/exper/gefs/" + om.run + "/GEFS_" +
                        om.currentParam + "_" + om.run + "Z_f" + time + sectorAdd + ".gif"
        return imgUrl.getImage()
    }

    fun getAnimation(context: Context, om: ObjectModel): AnimationDrawable {
        if (om.spinnerTimeValue == -1) return AnimationDrawable()
        val bmAl = (om.spinnerTimeValue until om.spTime.list.size).mapTo(mutableListOf()) {
            getImage(om, om.spTime.list[it].split(" ").getOrNull(0) ?: "")
        }
        return UtilityImgAnim.getAnimationDrawableFromBMList(context, bmAl)
    }
}

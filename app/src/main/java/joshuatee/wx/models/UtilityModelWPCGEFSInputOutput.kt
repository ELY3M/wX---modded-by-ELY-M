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

package joshuatee.wx.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.util.UtilityImgAnim

internal object UtilityModelWpcGefsInputOutput {

    // 00Z loads around 0540Z
    // 06Z loads around 1140Z
    // 12Z loads around 1740Z
    // 18Z loads around 2340Z
    val runTime: RunTimeData
        get() {
            val runData = RunTimeData()
            val currentHour = ObjectDateTime.currentHourInUtc
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
            listOf("00", "06", "12", "18").forEach {
                runData.listRunAdd(it)
            }
            runData.timeStrConv = runData.mostRecentRun
            return runData
        }

    fun getImage(om: ObjectModel, time: String): Bitmap {
        val sectorAdd = if (om.sector == "AK") {
            "_ak"
        } else {
            ""
        }
        val url = "${GlobalVariables.nwsWPCwebsitePrefix}/exper/gefs/" + om.run + "/GEFS_" + om.currentParam + "_" + om.run + "Z_f" + time + sectorAdd + ".gif"
        return url.getImage()
    }

    fun getAnimation(context: Context, om: ObjectModel): AnimationDrawable {
        if (om.spinnerTimeValue == -1) {
            return AnimationDrawable()
        }
        val bitmaps = (om.spinnerTimeValue until om.times.size).map {
            getImage(om, om.times[it].split(" ").getOrNull(0) ?: "")
        }
        return UtilityImgAnim.getAnimationDrawableFromBitmapList(context, bitmaps)
    }
}

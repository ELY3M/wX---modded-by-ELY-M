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

import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication
import joshuatee.wx.RegExp

internal object UtilityModelSpcSrefInputOutput {

    val runTime: RunTimeData
        get() {
            val runData = RunTimeData()
            val html = "${MyApplication.nwsSPCwebsitePrefix}/exper/sref/".getHtml()
            val tmpTxt = html.parse(RegExp.srefPattern2)
            val result = html.parseColumn(RegExp.srefPattern3)
            val latestRunAl = tmpTxt.split("</a>").dropLastWhile { it.isEmpty() }
            if (latestRunAl.isNotEmpty()) {
                runData.listRunAdd(latestRunAl[0])
            }
            if (result.isNotEmpty()) {
                result.forEach { runData.listRunAdd(it) }
            }
            runData.imageCompleteStr = tmpTxt
            runData.mostRecentRun = tmpTxt.parseLastMatch(RegExp.srefPattern1)
            return runData
        }

    fun getImage(context: Context, om: ObjectModel, time: String): Bitmap {
        val run = om.run.replace("z", "")
        return UtilityImg.getBitmapAddWhiteBG(
            context,
            "${MyApplication.nwsSPCwebsitePrefix}/exper/sref/gifs/$run/${om.currentParam}$time.gif"
        )
    }

    fun getAnimation(context: Context, om: ObjectModel): AnimationDrawable {
        if (om.spinnerTimeValue == -1) return AnimationDrawable()
        val bmAl = (om.spinnerTimeValue until om.spTime.list.size).mapTo(mutableListOf()) {
            getImage(context, om, om.spTime.list[it].split(" ").getOrNull(0) ?: "")
        }
        return UtilityImgAnim.getAnimationDrawableFromBMList(context, bmAl)
    }
}


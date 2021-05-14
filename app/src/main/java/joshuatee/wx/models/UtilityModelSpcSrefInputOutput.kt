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

package joshuatee.wx.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication

internal object UtilityModelSpcSrefInputOutput {

    const val pattern1 = "([0-9]{2}z)"
    const val pattern2 = "([0-9]{10}z</a>&nbsp in through <b>f[0-9]{3})"
    const val pattern3 = "<tr><td class=.previous.><a href=.sref.php\\?run=[0-9]{10}&id=SREF_H5__.>([0-9]{10}z)</a></td></tr>"

    val runTime: RunTimeData
        get() {
            val runData = RunTimeData()
            val html = "${MyApplication.nwsSPCwebsitePrefix}/exper/sref/".getHtml()
            val tmpTxt = html.parse(pattern2)
            val result = html.parseColumn(pattern3)
            val latestRunAl = tmpTxt.split("</a>").dropLastWhile { it.isEmpty() }
            if (latestRunAl.isNotEmpty()) {
                runData.listRunAdd(latestRunAl[0])
            }
            if (result.isNotEmpty()) {
                result.forEach { runData.listRunAdd(it) }
            }
            runData.imageCompleteStr = tmpTxt
            runData.mostRecentRun = tmpTxt.parseLastMatch(pattern1)
            return runData
        }

    fun getImage(context: Context, om: ObjectModelNoSpinner, time: String): Bitmap {
        val run = om.run.replace("z", "")
        val url = "${MyApplication.nwsSPCwebsitePrefix}/exper/sref/gifs/$run/${om.currentParam}$time.gif"
        return UtilityImg.getBitmapAddWhiteBackground(context, url)
    }

    fun getAnimation(context: Context, om: ObjectModelNoSpinner): AnimationDrawable {
        if (om.spinnerTimeValue == -1) {
            return AnimationDrawable()
        }
        val bitmaps = (om.spinnerTimeValue until om.times.size).map {
            getImage(context, om, om.times[it].split(" ").getOrNull(0) ?: "")
        }
        return UtilityImgAnim.getAnimationDrawableFromBitmapList(context, bitmaps)
    }
}


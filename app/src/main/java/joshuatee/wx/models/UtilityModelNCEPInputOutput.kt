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
import joshuatee.wx.Extensions.getHtml
import joshuatee.wx.Extensions.getImage

import java.util.Locale

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication
import joshuatee.wx.RegExp
import joshuatee.wx.util.UtilityImgAnim

internal object UtilityModelNcepInputOutput {

    fun getRunTime(model: String, param: String, spinnerSectorCurrent: String): RunTimeData {
        val runData = RunTimeData()
        val runCompletionDataStr = StringBuilder(100)
        val url = "${MyApplication.nwsMagNcepWebsitePrefix}/model-guidance-model-parameter.php?group=Model%20Guidance&model=" + model.uppercase(Locale.US) + "&area=" + spinnerSectorCurrent + "&ps=area"
        val fullHtml = url.getHtml()
        val html = fullHtml.parse(RegExp.ncepPattern2).replace("UTC", "Z").replace(" ", "")
        runCompletionDataStr.append(html.replace("Z", " UTC"))
        if (runCompletionDataStr.length > 8) {
            runCompletionDataStr.insert(8, " ")
        }
        val timeCompleteUrl = "${MyApplication.nwsMagNcepWebsitePrefix}/model-fhrs.php?group=Model%20Guidance&model=" + model.lowercase(Locale.US) +
                "&fhr_mode=image&loop_start=-1&loop_end=-1&area=" + spinnerSectorCurrent + "&fourpan=no&imageSize=&preselected_formatted_cycle_date=" +
                runCompletionDataStr + "&cycle=" + runCompletionDataStr + "&param=" + param + "&ps=area"
        val timeCompleteHtml = timeCompleteUrl.replace(" ", "%20").getHtml()
        runData.imageCompleteStr = timeCompleteHtml.parseLastMatch("SubmitImageForm.(.*?).\"")
        runData.mostRecentRun = html.parseLastMatch(RegExp.ncepPattern1)
        return runData
    }

    fun getImage(om: ObjectModelNoSpinner, time: String): Bitmap {
        val modifiedTime = if (om.model == "HRRR" && time.length == 3) {
            time + "00"
        } else {
            time
        }
        val imgUrl = when (om.model) {
            "GFS" -> "${MyApplication.nwsMagNcepWebsitePrefix}/data/" + om.model.lowercase(Locale.US) + "/" + om.run.replace("Z", "") +
                    "/" + om.sector.lowercase(Locale.US) + "/" + om.currentParam + "/" + om.model.lowercase(Locale.US) + "_" +
                    om.sector.lowercase(Locale.US) + "_" + time + "_" + om.currentParam + ".gif"
            "HRRR" -> "${MyApplication.nwsMagNcepWebsitePrefix}/data/" + om.model.lowercase(Locale.US) + "/" + om.run.replace("Z", "") +
                    "/" + om.model.lowercase(Locale.US) + "_" + om.sector.lowercase(Locale.US) + "_" + modifiedTime + "_" + om.currentParam + ".gif"
            else -> "${MyApplication.nwsMagNcepWebsitePrefix}/data/" + om.model.lowercase(Locale.US) + "/" + om.run.replace("Z", "") +
                    "/" + om.model.lowercase(Locale.US) + "_" + om.sector.lowercase(Locale.US) + "_" + time + "_" + om.currentParam + ".gif"
        }
        // UtilityLog.d("wx", imgUrl)
        return imgUrl.getImage()
    }

    fun getAnimation(context: Context, om: ObjectModelNoSpinner): AnimationDrawable {
        if (om.spinnerTimeValue == -1) {
            return AnimationDrawable()
        }
        val timeList = om.times
        val bitmaps = (om.spinnerTimeValue until timeList.size).map { getImage(om, timeList[it].split(" ").getOrNull(0) ?: "") }
        return UtilityImgAnim.getAnimationDrawableFromBitmapList(context, bitmaps)
    }
}

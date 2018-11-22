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
import joshuatee.wx.Extensions.getHtml
import joshuatee.wx.Extensions.getImage

import java.util.Locale

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication
import joshuatee.wx.RegExp
import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.util.UtilityString

internal object UtilityModelNCEPInputOutput {

    fun getRunTime(model: String, param: String, spinnerSectorCurrent: String): RunTimeData {
        val runData = RunTimeData()
        val runCompletionDataStr = StringBuilder(100)
        var sigHtmlTmp = UtilityString.getHTMLandParse("${MyApplication.nwsMagNcepWebsitePrefix}/model-guidance-model-parameter.php?group=Model%20Guidance&model="
                + model.toUpperCase(Locale.US) + "&area=" + spinnerSectorCurrent + "&ps=area", RegExp.ncepPattern2)
        sigHtmlTmp = sigHtmlTmp.replace("UTC selected_cell", "Z")
        runCompletionDataStr.append(sigHtmlTmp.replace("Z", " UTC"))
        if (runCompletionDataStr.length > 8) {
            runCompletionDataStr.insert(8, " ")
        }
        val timeCompleteUrl = "${MyApplication.nwsMagNcepWebsitePrefix}/model-fhrs.php?group=Model%20Guidance&model=" + model.toLowerCase(Locale.US) + "&fhr_mode=image&loop_start=-1&loop_end=-1&area=" + spinnerSectorCurrent + "&fourpan=no&imageSize=&preselected_formatted_cycle_date=" + runCompletionDataStr + "&cycle=" + runCompletionDataStr + "&param=" + param + "&ps=area"
        val timeCompleteHTML = (timeCompleteUrl.replace(" ", "%20")).getHtml()
        runData.imageCompleteStr = timeCompleteHTML.parseLastMatch("SubmitImageForm.(.*?).\"")
        runData.mostRecentRun = sigHtmlTmp.parseLastMatch(RegExp.ncepPattern1)
        return runData
    }

    fun getImage(om: ObjectModel, time: String): Bitmap {
        val imgUrl: String = if (om.model == "GFS") {
            "${MyApplication.nwsMagNcepWebsitePrefix}/data/" + om.model.toLowerCase(Locale.US) + "/" + om.run.replace("Z", "") +
                    "/" + om.sector.toLowerCase(Locale.US) + "/" + om.currentParam + "/" + om.model.toLowerCase(Locale.US) + "_" +
                    om.sector.toLowerCase(Locale.US) + "_" + time + "_" + om.currentParam + ".gif"
        } else if (om.model == "HRRR") {
            "${MyApplication.nwsMagNcepWebsitePrefix}/data/" + om.model.toLowerCase(Locale.US) + "/" + om.run.replace("Z", "") +
                    "/" + om.model.toLowerCase(Locale.US) + "_" + om.sector.toLowerCase(Locale.US) + "_" + time + "00_" + om.currentParam + ".gif"
        } else {
            "${MyApplication.nwsMagNcepWebsitePrefix}/data/" + om.model.toLowerCase(Locale.US) + "/" + om.run.replace("Z", "") +
                    "/" + om.model.toLowerCase(Locale.US) + "_" + om.sector.toLowerCase(Locale.US) + "_" + time + "_" + om.currentParam + ".gif"
        }
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

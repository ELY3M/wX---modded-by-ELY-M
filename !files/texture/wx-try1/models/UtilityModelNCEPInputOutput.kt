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
import joshuatee.wx.RegExp
import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.util.UtilityString

internal object UtilityModelNCEPInputOutput {

    fun getRunTime(model: String, param: String, spinnerSectorCurrent: String): RunTimeData {
        val runData = RunTimeData()
        val runCompletionDataStr = StringBuilder(100)
        var sigHtmlTmp = UtilityString.getHTMLandParse("http://mag.ncep.noaa.gov/model-guidance-model-parameter.php?group=Model%20Guidance&model="
                + model.toUpperCase(Locale.US) + "&area=" + spinnerSectorCurrent + "&ps=area", RegExp.ncepPattern2)
        sigHtmlTmp = sigHtmlTmp.replace("UTC selected_cell", "Z")
        runCompletionDataStr.append(sigHtmlTmp.replace("Z", " UTC"))
        if (runCompletionDataStr.length > 8) {
            runCompletionDataStr.insert(8, " ")
        }
        val timeCompleteUrl = "http://mag.ncep.noaa.gov/model-fhrs.php?group=Model%20Guidance&model=" + model.toLowerCase(Locale.US) + "&fhr_mode=image&loop_start=-1&loop_end=-1&area=" + spinnerSectorCurrent + "&fourpan=no&imageSize=&preselected_formatted_cycle_date=" + runCompletionDataStr + "&cycle=" + runCompletionDataStr + "&param=" + param + "&ps=area"
        val timeCompleteHTML = (timeCompleteUrl.replace(" ", "%20")).getHtml()
        runData.imageCompleteStr = timeCompleteHTML.parseLastMatch("SubmitImageForm.(.*?).\"")
        runData.mostRecentRun = sigHtmlTmp.parseLastMatch(RegExp.ncepPattern1)
        return runData
    }

    fun getImage(model: String, sector: String, param: String, run: String, time: String): Bitmap {
        val imgUrl: String = if (model == "GFS") {
            "http://mag.ncep.noaa.gov/data/" + model.toLowerCase(Locale.US) + "/" + run.replace("Z", "") + "/" + sector.toLowerCase(Locale.US) + "/" + param + "/" + model.toLowerCase(Locale.US) + "_" + sector.toLowerCase(Locale.US) + "_" + time + "_" + param + ".gif"
        } else {
            "http://mag.ncep.noaa.gov/data/" + model.toLowerCase(Locale.US) + "/" + run.replace("Z", "") + "/" + model.toLowerCase(Locale.US) + "_" + sector.toLowerCase(Locale.US) + "_" + time + "_" + param + ".gif"
        }
        return imgUrl.getImage()
    }

    fun getAnimation(context: Context, model: String, sector: String, param: String, run: String, spinnerTimeValue: Int, listTime: List<String>): AnimationDrawable {
        if (spinnerTimeValue == -1) return AnimationDrawable()
        val bmAl = (spinnerTimeValue until listTime.size)
                .filter { k -> listTime[k].split(" ").dropLastWhile { it.isEmpty() }.isNotEmpty() }
                .mapTo(mutableListOf()) { k ->
                    getImage(model, sector, param, run, listTime[k].split(" ").getOrNull(0) ?: "")
                }
        return UtilityImgAnim.getAnimationDrawableFromBMList(context, bmAl)
    }
}
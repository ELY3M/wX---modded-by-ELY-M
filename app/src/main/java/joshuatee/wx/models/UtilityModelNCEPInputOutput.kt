/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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

import android.graphics.Bitmap
import android.content.Context
import java.util.Locale
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.getHtml
import joshuatee.wx.getImage
import joshuatee.wx.parse
import joshuatee.wx.parseLastMatch
import java.util.regex.Pattern

internal object UtilityModelNcepInputOutput {

    private val pattern1: Pattern = Pattern.compile("([0-9]{2}Z)")

    //    private val pattern2: Pattern = Pattern.compile("var current_cycle_white . .([0-9 ]{11} UTC)")
    private val pattern2: Pattern = Pattern.compile("data-cycle-date=.([0-9 ]{11} UTC)")

    fun getRunTime(model: String, param: String, spinnerSectorCurrent: String): RunTimeData {
        val runData = RunTimeData()
        val runCompletionDataStr = StringBuilder(100)
        val url =
            "${GlobalVariables.NWS_MAG_NCEP_WEBSITE_PREFIX}/model-guidance-model-parameter.php?group=Model%20Guidance&model=" + model.uppercase(
                Locale.US
            ) + "&area=" + spinnerSectorCurrent + "&ps=area"
        val fullHtml = url.getHtml()
        val html = fullHtml.parse(pattern2).replace("UTC", "Z").replace(" ", "")
        runCompletionDataStr.append(html.replace("Z", " UTC"))
        if (runCompletionDataStr.length > 8) {
            runCompletionDataStr.insert(8, " ")
        }
        val timeCompleteUrl =
            "${GlobalVariables.NWS_MAG_NCEP_WEBSITE_PREFIX}/model-fhrs.php?group=Model%20Guidance&model=" + model.lowercase(
                Locale.US
            ) +
                    "&fhr_mode=image&loop_start=-1&loop_end=-1&area=" + spinnerSectorCurrent + "&fourpan=no&imageSize=&preselected_formatted_cycle_date=" +
                    runCompletionDataStr + "&cycle=" + runCompletionDataStr + "&param=" + param + "&ps=area"
        val timeCompleteHtml = timeCompleteUrl.replace(" ", "%20").getHtml()
        runData.imageCompleteStr = timeCompleteHtml.parseLastMatch("SubmitImageForm.(.*?).\"")
        runData.mostRecentRun = html.parseLastMatch(pattern1)
        return runData
    }

    fun getImage(
        @Suppress("UNUSED_PARAMETER") ignoredContext: Context,
        om: ObjectModel,
        time: String
    ): Bitmap {
        val modifiedTime = if (om.model == "HRRR" && time.length == 3) {
            time + "00"
        } else {
            time
        }
        val imgUrl = when (om.model) {
            "GFS" -> "${GlobalVariables.NWS_MAG_NCEP_WEBSITE_PREFIX}/data/" + om.model.lowercase(
                Locale.US
            ) + "/" + om.run.replace("Z", "") +
                    "/" + om.sector.lowercase(Locale.US) + "/" + om.currentParam + "/" + om.model.lowercase(
                Locale.US
            ) + "_" +
                    om.sector.lowercase(Locale.US) + "_" + time + "_" + om.currentParam + ".gif"

            "HRRR" -> "${GlobalVariables.NWS_MAG_NCEP_WEBSITE_PREFIX}/data/" + om.model.lowercase(
                Locale.US
            ) + "/" + om.run.replace("Z", "") +
                    "/" + om.model.lowercase(Locale.US) + "_" + om.sector.lowercase(Locale.US) + "_" + modifiedTime + "_" + om.currentParam + ".gif"

            else -> "${GlobalVariables.NWS_MAG_NCEP_WEBSITE_PREFIX}/data/" + om.model.lowercase(
                Locale.US
            ) + "/" + om.run.replace("Z", "") +
                    "/" + om.model.lowercase(Locale.US) + "_" + om.sector.lowercase(Locale.US) + "_" + time + "_" + om.currentParam + ".gif"
        }
        return imgUrl.getImage()
    }
}

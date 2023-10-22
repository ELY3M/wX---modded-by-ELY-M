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

import android.graphics.Bitmap
import android.content.Context
import java.util.Locale
import joshuatee.wx.util.UtilityString
import joshuatee.wx.getHtml
import joshuatee.wx.getImage
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.parse
import java.util.regex.Pattern

internal object UtilityModelEsrlInputOutput {

    private const val urlBase = "https://rapidrefresh.noaa.gov"
    private val pattern1: Pattern = Pattern.compile("<option selected>([0-9]{2} \\w{3} [0-9]{4} - [0-9]{2}Z)<.option>")

    fun getRunTime(model: String, param: String): RunTimeData {
        val runData = RunTimeData()
        val htmlRunStatus = when (model) {
            // https://rapidrefresh.noaa.gov/RAP/Welcome.cgi?dsKey=rap_jet&domain=full&run_time=23+Nov+2018+-+08Z
            "RAP_NCEP" -> ("$urlBase/RAP/Welcome.cgi?dsKey=" + model.lowercase(Locale.US) + "_jet&domain=full").getHtml()
            "HRRR_NCEP" -> ("$urlBase/hrrr/HRRR/Welcome.cgi?dsKey=" + model.lowercase(Locale.US) + "_jet&domain=full").getHtml()
            else -> ("$urlBase/" + model.lowercase(Locale.US) + "/" + model + "/Welcome.cgi?dsKey=" + model.lowercase(Locale.US) + "_jet&domain=full").getHtml()
        }
        val mostRecentRunString = htmlRunStatus.parse(pattern1)
        runData.listRunAddAll(ObjectDateTime.generateModelRuns(mostRecentRunString, 1, "d MMM yyyy' - 'HH'Z'", "yyyyMMddHH", 12))
        runData.mostRecentRun = runData.listRun.first()
        // <option selected>10 Nov 2022 - 11Z</option>
        val runOffset = if (model.contains("HRRR")) 2 else 0
        runData.imageCompleteInt = UtilityString.parseColumn(htmlRunStatus, "(=${param}&)").size - runOffset
        runData.imageCompleteStr = runData.imageCompleteInt.toString()
        if (mostRecentRunString != "") {
            runData.timeStrConv = mostRecentRunString.parse("([0-9]{2})$")
        }
        return runData
    }

    fun getImage(@Suppress("UNUSED_PARAMETER") ignoredContext: Context, om: ObjectModel, time: String): Bitmap {
        val parentModel = when (om.model) {
            "RAP_NCEP" -> "RAP"
            "HRRR_NCEP" -> "HRRR"
            else -> ""
        }
        val onDemandUrl: String
        val imgUrl: String
        val sectorLocal = om.sector.replace(" ", "")
                .replace("Full", "full")
                .replace("CONUS", "conus")
        val param = om.currentParam.replace("_full_", "_" + sectorLocal + "_")
        if (parentModel.contains("RAP")) {
            imgUrl = "$urlBase/" + parentModel + "/for_web/" + om.model.lowercase(Locale.US) +
                    "_jet/" + om.run.replace("Z", "") +
                    "/" + sectorLocal + "/" + param + "_f" + time + ".png"
            onDemandUrl = "$urlBase/" + parentModel + "/" +
                    "displayMapUpdated" + ".cgi?keys=" +
                    om.model.lowercase(Locale.US) + "_jet:&runtime=" + om.run.replace("Z", "") +
                    "&plot_type=" + param + "&fcst=" + time + "&time_inc=60&num_times=16&model=" +
                    "rr" + "&ptitle=" + om.model +
                    "%20Model%20Fields%20-%20Experimental&maxFcstLen=15&fcstStrLen=-1&domain=" +
                    sectorLocal + "&adtfn=1"

        } else {
            imgUrl = "$urlBase/hrrr/" + parentModel.uppercase(Locale.US) + "/for_web/" +
                    om.model.lowercase(Locale.US) + "_jet/" + om.run.replace("Z", "") +
                    "/" + sectorLocal + "/" + param + "_f" + time + ".png"
            onDemandUrl = "$urlBase/hrrr/" + parentModel.uppercase(Locale.US) + "/" +
                    "displayMapUpdated" + ".cgi?keys=" +
                    om.model.lowercase(Locale.US) + "_jet:&runtime=" + om.run.replace("Z", "") +
                    "&plot_type=" + param + "&fcst=" + time + "&time_inc=60&num_times=16&model=" +
                    om.model.lowercase(Locale.US) + "&ptitle=" + om.model +
                    "%20Model%20Fields%20-%20Experimental&maxFcstLen=15&fcstStrLen=-1&domain=" +
                    sectorLocal + "&adtfn=1"
        }
        onDemandUrl.getHtml()
        return imgUrl.getImage()
    }
}

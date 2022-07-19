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
import java.util.Locale
import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.util.UtilityString
import joshuatee.wx.Extensions.*
import java.util.regex.Pattern

internal object UtilityModelEsrlInputOutput {

    private const val urlBase = "https://rapidrefresh.noaa.gov"
    private val pattern1: Pattern = Pattern.compile("<option selected>([0-9]{2} \\w{3} [0-9]{4} - [0-9]{2}Z)<.option>")
    private val pattern2: Pattern = Pattern.compile("<option>([0-9]{2} \\w{3} [0-9]{4} - [0-9]{2}Z)<.option>")
    private val pattern3: Pattern = Pattern.compile("[0-9]{2} \\w{3} ([0-9]{4}) - [0-9]{2}Z")
    private val pattern4: Pattern = Pattern.compile("([0-9]{2}) \\w{3} [0-9]{4} - [0-9]{2}Z")
    private val pattern5: Pattern = Pattern.compile("[0-9]{2} \\w{3} [0-9]{4} - ([0-9]{2})Z")
    private val pattern6: Pattern = Pattern.compile("[0-9]{2} (\\w{3}) [0-9]{4} - [0-9]{2}Z")

    fun getRunTime(model: String, param: String): RunTimeData {
        val runData = RunTimeData()
        val htmlRunStatus = when (model) {
            "HRRR_AK" -> "$urlBase/alaska/".getHtml()
            // https://rapidrefresh.noaa.gov/RAP/Welcome.cgi?dsKey=rap_jet&domain=full&run_time=23+Nov+2018+-+08Z
            "RAP_NCEP" -> ("$urlBase/RAP/Welcome.cgi?dsKey=" + model.lowercase(Locale.US) + "_jet&domain=full").getHtml()
            "RAP" -> "$urlBase/RAP/".getHtml()
            "HRRR_NCEP" -> ("$urlBase/hrrr/HRRR/Welcome.cgi?dsKey=" + model.lowercase(Locale.US) + "_jet&domain=full").getHtml()
            else -> ("$urlBase/" + model.lowercase(Locale.US) + "/" + model + "/Welcome.cgi?dsKey=" + model.lowercase(Locale.US) + "_jet&domain=full").getHtml()
        }
        var html = htmlRunStatus.parse(pattern1)
        val oldRunTimes = htmlRunStatus.parseColumn(pattern2)
        var year = html.parse(pattern3)
        var day = html.parse(pattern4)
        var hour = html.parse(pattern5)
        var monthStr = html.parse(pattern6)
        monthStr = monthStr.replace("Jan", "01")
                .replace("Feb", "02")
                .replace("Mar", "03")
                .replace("Apr", "04")
                .replace("May", "05")
                .replace("Jun", "06")
                .replace("Jul", "07")
                .replace("Aug", "08")
                .replace("Sep", "09")
                .replace("Oct", "10")
                .replace("Nov", "11")
                .replace("Dec", "12")
        html = year + monthStr + day + hour
        runData.listRunAdd(html)
        runData.mostRecentRun = html
        runData.imageCompleteInt = UtilityString.parseAndCount(htmlRunStatus, ".($param).") - 3
        runData.imageCompleteStr = runData.imageCompleteInt.toString()
        if (html != "") {
            var i = 0
            while (i < 12 && i < oldRunTimes.size) {
                year = oldRunTimes[i].parse(pattern3)
                day = oldRunTimes[i].parse(pattern4)
                hour = oldRunTimes[i].parse(pattern5)
                monthStr = oldRunTimes[i].parse(pattern6)
                monthStr = monthStr.replace("Jan", "01")
                        .replace("Feb", "02")
                        .replace("Mar", "03")
                        .replace("Apr", "04")
                        .replace("May", "05")
                        .replace("Jun", "06")
                        .replace("Jul", "07")
                        .replace("Aug", "08")
                        .replace("Sep", "09")
                        .replace("Oct", "10")
                        .replace("Nov", "11")
                        .replace("Dec", "12")
                runData.listRunAdd(year + monthStr + day + hour)
                i += 1
            }
            runData.timeStrConv = html.parse("([0-9]{2})$")
        }
        return runData
    }

    fun getImage(om: ObjectModelNoSpinner, time: String): Bitmap {
        var parentModel = ""
        when (om.model) {
            "RAP_NCEP" -> parentModel = "RAP"
            "HRRR_NCEP" -> parentModel = "HRRR"
            else -> {}
        }
        val onDemandUrl: String
        val imgUrl: String
        var sectorLocal = om.sector.replace(" ", "")
        sectorLocal = sectorLocal.replace("Full", "full")
        sectorLocal = sectorLocal.replace("CONUS", "conus")
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

    fun getAnimation(context: Context, om: ObjectModelNoSpinner): AnimationDrawable {
        if (om.spinnerTimeValue == -1) {
            return AnimationDrawable()
        }
        val timeList = om.times
        val bitmaps = (om.spinnerTimeValue until timeList.size).map { getImage(om, timeList[it].split(" ").getOrNull(0) ?: "") }
        return UtilityImgAnim.getAnimationDrawableFromBitmapList(context, bitmaps)
    }
}

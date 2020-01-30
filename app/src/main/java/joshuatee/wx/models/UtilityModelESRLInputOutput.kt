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

import java.util.Locale

import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.util.UtilityString
import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp

internal object UtilityModelEsrlInputOutput {

    private const val urlBase = "https://rapidrefresh.noaa.gov"

    fun getRunTime(model: String, param: String): RunTimeData {
        val runData = RunTimeData()
        val htmlRunstatus: String = when (model) {
            "HRRR_AK" -> ("$urlBase/alaska/").getHtml()
            // https://rapidrefresh.noaa.gov/RAP/Welcome.cgi?dsKey=rap_jet&domain=full&run_time=23+Nov+2018+-+08Z
            "RAP_NCEP" -> ("$urlBase/RAP/Welcome.cgi?dsKey=" + model.toLowerCase(Locale.US)
                    + "_jet&domain=full").getHtml()
            "RAP" -> ("$urlBase/RAP/").getHtml()
            "HRRR_NCEP" -> ("$urlBase/hrrr/HRRR/Welcome.cgi?dsKey=" + model.toLowerCase(Locale.US)
                    + "_jet&domain=full").getHtml()
            else -> ("$urlBase/" + model.toLowerCase(Locale.US) + "/" + model + "/Welcome.cgi?dsKey="
                    + model.toLowerCase(Locale.US) + "_jet&domain=full").getHtml()
        }
        val oldRunTimes: List<String>
        var html = htmlRunstatus.parse(RegExp.eslHrrrPattern1)
        oldRunTimes = htmlRunstatus.parseColumn(RegExp.eslHrrrPattern2)
        var year = html.parse(RegExp.eslHrrrPattern3)
        var day = html.parse(RegExp.eslHrrrPattern4)
        var hour = html.parse(RegExp.eslHrrrPattern5)
        var monthStr = html.parse(RegExp.eslHrrrPattern6)
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
        runData.imageCompleteInt = UtilityString.parseAndCount(htmlRunstatus, ".($param).") - 3
        runData.imageCompleteStr = runData.imageCompleteInt.toString()
        if (html != "") {
            var i = 0
            while (i < 12 && i < oldRunTimes.size) {
                year = oldRunTimes[i].parse(RegExp.eslHrrrPattern3)
                day = oldRunTimes[i].parse(RegExp.eslHrrrPattern4)
                hour = oldRunTimes[i].parse(RegExp.eslHrrrPattern5)
                monthStr = oldRunTimes[i].parse(RegExp.eslHrrrPattern6)
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

    // https://rapidrefresh.noaa.gov/RAP/for_web/rap_jet/2016091600/full/cref_sfc_f00.png
    // https://rapidrefresh.noaa.gov/HRRR/for_web/hrrr_jet/2016091607/full/1ref_sfc_f00.png

    fun getImage(om: ObjectModel, time: String): Bitmap {
        var sector = om.sector
        var paramTmp = om.currentParam
        val imgUrl: String
        val zipStr = "TZA"
        when (om.model) {
            "HRRR", "HRRR_NCEP" -> when {
                om.sectorInt == 0 -> {
                }
                om.sectorInt < 9 -> {
                    sector = "t" + om.sectorInt.toString()
                    paramTmp = paramTmp.replace("_", "_$sector")
                }
                else -> {
                    sector = "z" + (om.sectorInt - 9).toString()
                    paramTmp = paramTmp.replace("_", "_$sector")
                }
            }
            "HRRR_AK" -> {
            }
            "RAP", "RAP_NCEP" ->
                when (om.sectorInt) {
                    9 -> {
                        sector = "alaska"
                    }
                    10 -> { // AK Zoom
                        sector = "a1"
                        paramTmp = paramTmp.replace("_", "_$sector")
                    }
                    11 -> { // HI
                        sector = "r1"
                        paramTmp = paramTmp.replace("_", "_$sector")
                    }
                    in 2..8 -> {
                        sector = "t" + (om.sectorInt - 1).toString()
                        paramTmp = paramTmp.replace("_", "_$sector")
                    }
                }
        }
        val param = paramTmp
        var parentModel = om.model.replace("HRRR_AK", "alaska")
        when (om.model) {
            "RAP_NCEP" -> parentModel = "RAP"
            "HRRR_NCEP" -> parentModel = "HRRR"
            else -> {
            }
        }
        val onDemandUrl: String
        if (parentModel.contains("RAP")) {
            imgUrl = "$urlBase/" + parentModel + "/for_web/" + om.model.toLowerCase(Locale.US) +
                    "_jet/" + om.run.replace("Z", "") +
                    "/" + sector.toLowerCase(Locale.US) + "/" + param + "_f" + time + ".png"
            onDemandUrl = "$urlBase/" + parentModel + "/" +
                    "displayMapLocalDiskDateDomainZip" + zipStr + ".cgi?keys=" +
                    om.model.toLowerCase(Locale.US) + "_jet:&runtime=" + om.run.replace("Z", "") +
                    "&plot_type=" + param + "&fcst=" + time + "&time_inc=60&num_times=16&model=" +
                    "rr" + "&ptitle=" + om.model +
                    "%20Model%20Fields%20-%20Experimental&maxFcstLen=15&fcstStrLen=-1&domain=" +
                    sector.toLowerCase(Locale.US) + "&adtfn=1"

        } else {
            imgUrl = "$urlBase/hrrr/" + parentModel.toUpperCase(Locale.US) + "/for_web/" +
                    om.model.toLowerCase(Locale.US) + "_jet/" + om.run.replace("Z", "") +
                    "/" + sector.toLowerCase(Locale.US) + "/" + param + "_f" + time + ".png"
            onDemandUrl = "$urlBase/hrrr/" + parentModel.toUpperCase(Locale.US) + "/" +
                    "displayMapLocalDiskDateDomainZip" + zipStr + ".cgi?keys=" +
                    om.model.toLowerCase(Locale.US) + "_jet:&runtime=" + om.run.replace("Z", "") +
                    "&plot_type=" + param + "&fcst=" + time + "&time_inc=60&num_times=16&model=" +
                    om.model.toLowerCase(Locale.US) + "&ptitle=" + om.model +
                    "%20Model%20Fields%20-%20Experimental&maxFcstLen=15&fcstStrLen=-1&domain=" +
                    sector.toLowerCase(Locale.US) + "&adtfn=1"
        }
        onDemandUrl.getHtml()
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

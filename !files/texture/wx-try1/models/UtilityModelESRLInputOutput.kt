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

import java.util.Locale

import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.util.UtilityString
import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp

internal object UtilityModelESRLInputOutput {

    private const val urlBase = "https://rapidrefresh.noaa.gov"

    fun getRunTime(model: String): RunTimeData {
        val runData = RunTimeData()
        val htmlRunstatus: String = when (model) {
            "HRRR_AK" -> ("$urlBase/alaska/").getHtml()
            "RAP_NCEP" -> ("$urlBase/RAP/Welcome.cgi?dsKey=" + model.toLowerCase(Locale.US) + "_jet&domain=full").getHtml()
            "RAP" -> ("$urlBase/RAP/").getHtml()
            "HRRR_NCEP" -> ("$urlBase/hrrr/HRRR/Welcome.cgi?dsKey=" + model.toLowerCase(Locale.US) + "_jet&domain=full").getHtml()
            else -> ("$urlBase/" + model.toLowerCase(Locale.US) + "/" + model + "/Welcome.cgi?dsKey=" + model.toLowerCase(Locale.US) + "_jet&domain=full").getHtml()
        }
        val oldRunTimes: List<String>
        var sigHtmlTmp = htmlRunstatus.parse(RegExp.eslHrrrPattern1)
        oldRunTimes = htmlRunstatus.parseColumn(RegExp.eslHrrrPattern2)
        var year = sigHtmlTmp.parse(RegExp.eslHrrrPattern3)
        var day = sigHtmlTmp.parse(RegExp.eslHrrrPattern4)
        var hour = sigHtmlTmp.parse(RegExp.eslHrrrPattern5)
        var monthStr = sigHtmlTmp.parse(RegExp.eslHrrrPattern6)
        monthStr = monthStr.replace("Jan", "01").replace("Feb", "02").replace("Mar", "03").replace("Apr", "04")
                .replace("May", "05").replace("Jun", "06").replace("Jul", "07").replace("Aug", "08")
                .replace("Sep", "09").replace("Oct", "10").replace("Nov", "11").replace("Dec", "12")
        sigHtmlTmp = year + monthStr + day + hour
        runData.listRunAdd(sigHtmlTmp)
        runData.mostRecentRun = sigHtmlTmp
        runData.imageCompleteInt = UtilityString.parseAndCount(htmlRunstatus, ".(allfields).") - 1
        runData.imageCompleteStr = runData.imageCompleteInt.toString()
        if (sigHtmlTmp != "") {
            var i = 0
            while (i < 12 && i < oldRunTimes.size) {
                year = oldRunTimes[i].parse(RegExp.eslHrrrPattern3)
                day = oldRunTimes[i].parse(RegExp.eslHrrrPattern4)
                hour = oldRunTimes[i].parse(RegExp.eslHrrrPattern5)
                monthStr = oldRunTimes[i].parse(RegExp.eslHrrrPattern6)

                monthStr = monthStr.replace("Jan", "01").replace("Feb", "02").replace("Mar", "03").replace("Apr", "04")
                        .replace("May", "05").replace("Jun", "06").replace("Jul", "07").replace("Aug", "08")
                        .replace("Sep", "09").replace("Oct", "10").replace("Nov", "11").replace("Dec", "12")
                runData.listRunAdd(year + monthStr + day + hour)
                i += 1
            }
            runData.timeStrConv = sigHtmlTmp.parse("([0-9]{2})$")
        }
        return runData
    }

    // http://rapidrefresh.noaa.gov/RAP/for_web/rap_jet/2016091600/full/cref_sfc_f00.png
    // http://rapidrefresh.noaa.gov/HRRR/for_web/hrrr_jet/2016091607/full/1ref_sfc_f00.png

    fun getImage(model: String, sectorF: String, sectorInt: Int, paramTmpF: String, run: String, time: String): Bitmap {
        var sector = sectorF
        var paramTmp = paramTmpF
        val imgUrl: String
        val zipStr = "TZA"
        when (model) {
            "HRRR", "HRRR_NCEP" -> when {
                sectorInt == 0 -> {
                }
                sectorInt < 9 -> {
                    sector = "t" + sectorInt.toString()
                    paramTmp = paramTmp.replace("_", "_$sector")
                }
                else -> {
                    sector = "z" + (sectorInt - 9).toString()
                    paramTmp = paramTmp.replace("_", "_$sector")
                }
            }
            "HRRR_AK" -> {
            }
            "RAP", "RAP_NCEP" -> if (sectorInt == 0 || sectorInt == 1) {
            } else if (sectorInt == 9) { // AK
                sector = "alaska"
            } else if (sectorInt == 10) { // AK Zoom
                sector = "a1"
                paramTmp = paramTmp.replace("_", "_$sector")
            } else if (sectorInt == 11) { // HI
                sector = "r1"
                paramTmp = paramTmp.replace("_", "_$sector")
            } else if (sectorInt < 9) {
                sector = "t" + (sectorInt - 1).toString()
                paramTmp = paramTmp.replace("_", "_$sector")
            }
        }
        val param = paramTmp
        var parentModel = model.replace("HRRR_AK", "alaska")
        when (model) {
            "RAP_NCEP" -> parentModel = "RAP"
            "HRRR_NCEP" -> parentModel = "HRRR"
            else -> {
            }
        }
        val onDemandUrl: String
        if (parentModel.contains("RAP")) {
            imgUrl = "$urlBase/" + parentModel + "/for_web/" + model.toLowerCase(Locale.US) + "_jet/" + run.replace("Z", "") +
                    "/" + sector.toLowerCase(Locale.US) + "/" + param + "_f" + time + ".png"
            onDemandUrl = "$urlBase/" + parentModel + "/" +
                    "displayMapLocalDiskDateDomainZip" + zipStr + ".cgi?keys=" + model.toLowerCase(Locale.US) + "_jet:&runtime=" + run.replace("Z", "") +
                    "&plot_type=" + param + "&fcst=" + time + "&time_inc=60&num_times=16&model=" + model.toLowerCase(Locale.US) + "&ptitle=" + model +
                    "%20Model%20Fields%20-%20Experimental&maxFcstLen=15&fcstStrLen=-1&domain=" +
                    sector.toLowerCase(Locale.US) + "&adtfn=1"

        } else {
            imgUrl = "$urlBase/hrrr/" + parentModel.toUpperCase(Locale.US) + "/for_web/" + model.toLowerCase(Locale.US) + "_jet/" + run.replace("Z", "") +
                    "/" + sector.toLowerCase(Locale.US) + "/" + param + "_f" + time + ".png"
            onDemandUrl = "$urlBase/hrrr/" + parentModel.toUpperCase(Locale.US) + "/" +
                    "displayMapLocalDiskDateDomainZip" + zipStr + ".cgi?keys=" + model.toLowerCase(Locale.US) + "_jet:&runtime=" + run.replace("Z", "") +
                    "&plot_type=" + param + "&fcst=" + time + "&time_inc=60&num_times=16&model=" + model.toLowerCase(Locale.US) + "&ptitle=" + model +
                    "%20Model%20Fields%20-%20Experimental&maxFcstLen=15&fcstStrLen=-1&domain=" +
                    sector.toLowerCase(Locale.US) + "&adtfn=1"
        }
        onDemandUrl.getHtml()
        return imgUrl.getImage()
    }

    fun getAnimation(context: Context, model: String, sector: String, sectorInt: Int, paramTmp: String, run: String, spinnerTimeValue: Int, listTime: List<String>): AnimationDrawable {
        if (spinnerTimeValue == -1) return AnimationDrawable()
        val bmAl = (spinnerTimeValue until listTime.size).mapTo(mutableListOf()) { k ->
            getImage(model, sector, sectorInt, paramTmp, run, listTime[k].split(" ").getOrNull(0)
                    ?: "")
        }
        return UtilityImgAnim.getAnimationDrawableFromBMList(context, bmAl)
    }
}
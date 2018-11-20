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
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable

import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.util.UtilityTime
import joshuatee.wx.util.UtilityImg

import joshuatee.wx.Extensions.*

internal object UtilityModelNSSLWRFInputOutput {

    private const val baseUrl = "https://cams.nssl.noaa.gov"

    val runTime: RunTimeData
        get() {
            val runData = RunTimeData()
            val htmlRunstatus = (baseUrl).getHtml()
            val html = htmlRunstatus.parse("\\{model: \"fv3_nssl\",(rd: .[0-9]{8}\",rt: .[0-9]{4}\",)")
            val day = html.parse("rd:.(.*?),.*?").replace("\"", "")
            val time = html.parse("rt:.(.*?)00.,.*?").replace("\"", "")
            val mostRecentRun = day + time
            runData.listRunAdd(mostRecentRun)
            runData.listRunAddAll(UtilityTime.genModelRuns(mostRecentRun, 12, "yyyyMMddHH"))
            runData.mostRecentRun = mostRecentRun
            return runData
            // FIXME standardize on listRunAdd,listRunAddAll or appendListRun
        }

    fun getImage(context: Context, modelF: String, sectorF: String, param: String, runF: String, timeF: String): Bitmap {
        val time = timeF.split(" ")[0]
        val sectorIndex: Int = if (sectorF == "") {
            0
        } else {
            UtilityModelNSSLWRFInterface.sectorsLong.indexOf(sectorF)
        }
        val sector = UtilityModelNSSLWRFInterface.sectors[sectorIndex]
        val baseLayerUrl = "https://cams.nssl.noaa.gov/graphics/blank_maps/spc_$sector.png"
        var modelPostfix = "_nssl"
        var model = modelF.toLowerCase()
        if (modelF == "HRRRV3") {
            modelPostfix = ""
        }
        if (modelF == "WRF_3KM") {
            model = "wrf_nssl_3km"
            modelPostfix = ""
        }
        val year = runF.substring(0, 4)
        val month = runF.substring(4, 6)
        val day = runF.substring(6, 8)
        val hour = runF.substring(8, 10)
        val url = baseUrl + "/graphics/models/" + model + modelPostfix + "/" + year + "/" + month + "/" +
                day + "/" + hour + "00/f" + time + "00/" + param + ".spc_" + sector.toLowerCase() + ".f" + time + "00.png"
        val baseLayer = baseLayerUrl.getImage()
        val prodLayer = url.getImage()
        return UtilityImg.addColorBG(context, UtilityImg.mergeImages(context, prodLayer, baseLayer), Color.WHITE)
    }

    fun getAnimation(context: Context, model: String, sector: String, param: String, run: String, spinnerTimeValue: Int, listTime: List<String>): AnimationDrawable {
        if (spinnerTimeValue == -1) return AnimationDrawable()
        val bmAl = (spinnerTimeValue until listTime.size).mapTo(mutableListOf()) { k -> getImage(context, model, sector, param, run, listTime[k].split(" ").dropLastWhile { it.isEmpty() }[0]) }
        return UtilityImgAnim.getAnimationDrawableFromBMList(context, bmAl)
    }
}

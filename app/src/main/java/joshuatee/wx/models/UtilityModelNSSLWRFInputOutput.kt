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

    fun getImage(context: Context, om: ObjectModel, timeF: String): Bitmap {
        val time = timeF.split(" ")[0]
        val sectorIndex: Int = if (om.sector == "") {
            0
        } else {
            UtilityModelNSSLWRFInterface.sectorsLong.indexOf(om.sector)
        }
        val sector = UtilityModelNSSLWRFInterface.sectors[sectorIndex]
        val baseLayerUrl = "https://cams.nssl.noaa.gov/graphics/blank_maps/spc_$sector.png"
        var modelPostfix = "_nssl"
        var model = om.model.toLowerCase()
        if (om.model == "HRRRV3") {
            modelPostfix = ""
        }
        if (om.model == "WRF_3KM") {
            model = "wrf_nssl_3km"
            modelPostfix = ""
        }
        val year = om.run.substring(0, 4)
        val month = om.run.substring(4, 6)
        val day = om.run.substring(6, 8)
        val hour = om.run.substring(8, 10)
        val url = baseUrl + "/graphics/models/" + model + modelPostfix + "/" + year + "/" + month + "/" +
                day + "/" + hour + "00/f" + time + "00/" + om.currentParam + ".spc_" + sector.toLowerCase() + ".f" + time + "00.png"
        val baseLayer = baseLayerUrl.getImage()
        val prodLayer = url.getImage()
        return UtilityImg.addColorBG(context, UtilityImg.mergeImages(context, prodLayer, baseLayer), Color.WHITE)
    }

    fun getAnimation(context: Context, om: ObjectModel): AnimationDrawable {
        if (om.spinnerTimeValue == -1) return AnimationDrawable()
        val bmAl = (om.spinnerTimeValue until om.spTime.list.size).mapTo(mutableListOf()) {
            getImage(context, om, om.spTime.list[it].split(" ").getOrNull(0) ?: "")
        }
        return UtilityImgAnim.getAnimationDrawableFromBMList(context, bmAl)
    }
}

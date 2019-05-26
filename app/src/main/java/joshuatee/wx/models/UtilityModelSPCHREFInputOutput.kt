/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.util.UtilityTime
import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication

internal object UtilityModelSpcHrefInputOutput {

    val runTime: RunTimeData
        get() {
            val runData = RunTimeData()
            val htmlRunstatus = "${MyApplication.nwsSPCwebsitePrefix}/exper/href/".getHtml()
            val html =
                htmlRunstatus.parse("\\{model: \"href\",product: \"500mb_mean\",sector: \"conus\",(rd: .[0-9]{8}\",rt: .[0-9]{4}\",\\})")
            val day = html.parse("rd:.(.*?),.*?").replace("\"", "")
            val time = html.parse("rt:.(.*?)00.,.*?").replace("\"", "")
            val mostRecentRun = day + time
            runData.listRunAdd(mostRecentRun)
            runData.listRunAddAll(UtilityTime.genModelRuns(mostRecentRun, 12, "yyyyMMddHH"))
            runData.mostRecentRun = mostRecentRun
            return runData
        }

    fun getImage(context: Context, om: ObjectModel, time: String): Bitmap {
        val sectorIndex: Int = if (om.sector == "") {
            0
        } else {
            UtilityModelSpcHrefInterface.sectorsLong.indexOf(om.sector)
        }
        val sector = UtilityModelSpcHrefInterface.sectors[sectorIndex]
        if (om.run.length < 10) return UtilityImg.getBlankBitmap()
        val year = om.run.substring(0, 4)
        val month = om.run.substring(4, 6)
        val day = om.run.substring(6, 8)
        val hour = om.run.substring(8, 10)
        val products = om.currentParam.split(",")
        val bitmapArr = mutableListOf<Bitmap>()
        val urlArr = mutableListOf<String>()
        urlArr.add("${MyApplication.nwsSPCwebsitePrefix}/exper/href/graphics/spc_white_1050px.png")
        urlArr.add("${MyApplication.nwsSPCwebsitePrefix}/exper/href/graphics/noaa_overlay_1050px.png")
        products.forEach {
            val url = if (it.contains("cref_members")) {
                val paramArr = it.split(" ")
                "${MyApplication.nwsSPCwebsitePrefix}/exper/href/graphics/models/href/" + year + "/" + month + "/" + day + "/" + hour + "00/f0" + time + "00/" + paramArr[0] + "." + sector.toLowerCase() + ".f0" + time + "00." + paramArr[1] + ".tl00.png"
            } else {
                "${MyApplication.nwsSPCwebsitePrefix}/exper/href/graphics/models/href/" + year + "/" + month + "/" + day + "/" + hour + "00/f0" + time + "00/" + it + "." + sector.toLowerCase() + ".f0" + time + "00.png"
            }
            urlArr.add(url)
        }
        urlArr.add("${MyApplication.nwsSPCwebsitePrefix}/exper/href/graphics/blank_maps/$sector.png")
        urlArr.forEach { bitmapArr.add(it.getImage()) }
        val layers = mutableListOf<Drawable>()
        bitmapArr.forEach { layers.add(BitmapDrawable(context.resources, it)) }
        return UtilityImg.layerDrawableToBitmap(layers)
    }

    fun getAnimation(context: Context, om: ObjectModel): AnimationDrawable {
        if (om.spinnerTimeValue == -1) return AnimationDrawable()
        val bmAl = (om.spinnerTimeValue until om.spTime.list.size).mapTo(mutableListOf()) {
            getImage(context, om, om.spTime.list[it].split(" ").getOrNull(0) ?: "")
        }
        return UtilityImgAnim.getAnimationDrawableFromBMList(context, bmAl)
    }
}

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
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.util.UtilityTime
import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication

internal object UtilityModelSPCHREFInputOutput {

    val runTime: RunTimeData
        get() {
            val runData = RunTimeData()
            val htmlRunstatus = "${MyApplication.nwsSPCwebsitePrefix}/exper/href/".getHtml()
            val html = htmlRunstatus.parse("\\{model: \"href\",product: \"500mb_mean\",sector: \"conus\",(rd: .[0-9]{8}\",rt: .[0-9]{4}\",\\})")
            val day = html.parse("rd:.(.*?),.*?").replace("\"", "")
            val time = html.parse("rt:.(.*?)00.,.*?").replace("\"", "")
            val mostRecentRun = day + time
            runData.listRunAdd(mostRecentRun)
            runData.listRunAddAll(UtilityTime.genModelRuns(mostRecentRun, 12, "yyyyMMddHH"))
            runData.mostRecentRun = mostRecentRun
            return runData
        }

    fun getImage(context: Context, sector: String, run: String, time: String, param: String): Bitmap {
        if (run.length < 10) return UtilityImg.getBlankBitmap()
        val year = run.substring(0, 4)
        val month = run.substring(4, 6)
        val day = run.substring(6, 8)
        val hour = run.substring(8, 10)
        val products = param.split(",")
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

    fun getAnimation(context: Context, sector: String, run: String, spinnerTimeValue: Int, listTime: List<String>, param: String): AnimationDrawable {
        if (spinnerTimeValue == -1) return AnimationDrawable()
        val bmAl = (spinnerTimeValue until listTime.size).mapTo(mutableListOf()) { k -> getImage(context, sector, run, listTime[k].split(" ").dropLastWhile { it.isEmpty() }[0], param) }
        return UtilityImgAnim.getAnimationDrawableFromBMList(context, bmAl)
    }
}
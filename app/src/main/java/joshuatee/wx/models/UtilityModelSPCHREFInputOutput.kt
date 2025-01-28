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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import java.util.Locale
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.getHtml
import joshuatee.wx.getImage
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.parse
import joshuatee.wx.safeGet

internal object UtilityModelSpcHrefInputOutput {

    val runTime: RunTimeData
        get() {
            val runData = RunTimeData()
            val htmlRunStatus = "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/exper/href/".getHtml()
            val html =
                htmlRunStatus.parse("\\{model: \"href\",product: \"500mb_mean\",sector: \"conus\",(rd: .[0-9]{8}\",rt: .[0-9]{4}\",\\})")
            val day = html.parse("rd:.(.*?),.*?").replace("\"", "")
            val time = html.parse("rt:.(.*?)00.,.*?").replace("\"", "")
            val mostRecentRun = day + time
            runData.listRunAddAll(
                ObjectDateTime.generateModelRuns(
                    mostRecentRun,
                    12,
                    "yyyyMMddHH",
                    "yyyyMMddHH",
                    4
                )
            )
            runData.mostRecentRun = mostRecentRun
            return runData
        }

    fun getImage(context: Context, om: ObjectModel, time: String): Bitmap {
        var sectorIndex = if (om.sector == "") {
            0
        } else {
            UtilityModelSpcHrefInterface.sectorsLong.indexOf(om.sector)
        }
        if (sectorIndex == -1) {
            sectorIndex = 0
        }
        val sector = UtilityModelSpcHrefInterface.sectors.safeGet(sectorIndex)
        if (om.run.length < 10) {
            return UtilityImg.getBlankBitmap()
        }
        val year = om.run.substring(0, 4)
        val month = om.run.substring(4, 6)
        val day = om.run.substring(6, 8)
        val hour = om.run.substring(8, 10)
        val products = om.currentParam.split(",")
        val bitmaps = mutableListOf<Bitmap>()
        val urls = mutableListOf<String>()
//        val urls = mutableListOf(
//                "${GlobalVariables.nwsSPCwebsitePrefix}/exper/href/graphics/spc_white_1050px.png",
//                "${GlobalVariables.nwsSPCwebsitePrefix}/exper/href/graphics/noaa_overlay_1050px.png"
//        )
        products.forEach {
            val url = if (it.contains("cref_members")) {
                val paramArr = it.split(" ")
                "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/exper/href/graphics/models/href/" + year +
                        "/" + month + "/" + day + "/" + hour + "00/f0" + time + "00/" +
                        paramArr[0] + "." + sector.lowercase(Locale.US) + ".f0" + time +
                        "00." + paramArr[1] + ".tl00.png"
            } else {
                "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/exper/href/graphics/models/href/" + year +
                        "/" + month + "/" + day + "/" + hour + "00/f0" + time + "00/" + it +
                        "." + sector.lowercase(Locale.US) + ".f0" + time + "00.png"
            }
            if (it.contains("cref_members")) {
                val paramArr = it.split(" ")
                val infoUrl =
                    "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/exper/href/graphics/models/href/" + year +
                            "/" + month + "/" + day + "/" + hour + "00/f0" + time + "00/" +
                            paramArr[0] + "." + sector.lowercase(Locale.US) + ".f0" + time +
                            "00.png"
                urls.add(infoUrl)
            }
            urls.add(url)
        }
        if (products.contains("cref_ps")) {
            urls.add("${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/exper/href/graphics/blank_maps/$sector.ps.href.png")
        } else {
            urls.add("${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/exper/href/graphics/blank_maps/$sector.png")
        }
        urls.forEach {
            bitmaps.add(it.getImage())
        }
        val layers = mutableListOf<Drawable>()
        val resIds = listOf(R.drawable.spc_white_1050px, R.drawable.noaa_overlay_1050px)
        resIds.forEach {
            layers.add(BitmapDrawable(context.resources, UtilityImg.loadBitmap(context, it, false)))
        }
        bitmaps.forEach {
            layers.add(BitmapDrawable(context.resources, it))
        }
        return UtilityImg.layerDrawableToBitmap(layers)
    }
}

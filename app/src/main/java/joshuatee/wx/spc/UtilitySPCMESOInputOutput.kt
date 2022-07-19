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

package joshuatee.wx.spc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.Extensions.*
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.util.Utility

object UtilitySpcMesoInputOutput {

    fun getImage(context: Context, param: String, sector: String): Bitmap {
        val prefModel = "SPCMESO"
        var showRadar = Utility.readPref(context, prefModel + "_SHOW_RADAR", "false").startsWith("t")
        val showOutlook = Utility.readPref(context, prefModel + "_SHOW_OUTLOOK", "false").startsWith("t")
        val showWatchWarn = Utility.readPref(context, prefModel + "_SHOW_WATWARN", "false").startsWith("t")
        val showTopography = Utility.readPref(context, prefModel + "_SHOW_TOPO", "false").startsWith("t")
        val showCounty = Utility.readPref(context, prefModel + "_SHOW_COUNTY", "false").startsWith("t")

        val drawables = mutableListOf<Drawable>()
        val gifUrl = if (UtilitySpcMeso.imgSf.contains(param) && !showRadar) "_sf.gif" else ".gif"
        val imgUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/exper/mesoanalysis/s$sector/$param/$param$gifUrl"
        val radImgUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/exper/mesoanalysis/s$sector/rgnlrad/rgnlrad.gif"
        val outlookImgUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/exper/mesoanalysis/s$sector/otlk/otlk.gif"
        val watchWarningImgUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/exper/mesoanalysis/s$sector/warns/warns.gif"
        val topographyImgUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/exper/mesoanalysis/s$sector/topo/topo.gif"
        val countyImgUrl = "${GlobalVariables.nwsSPCwebsitePrefix}/exper/mesoanalysis/s$sector/cnty/cnty.gif"

        var bitmap = imgUrl.getImage()
        drawables.add(ColorDrawable(Color.WHITE))

        if (param == "hodo" || param.startsWith("skewt")) {
            showRadar = true
        }
        if (showCounty) {
            drawables.add(BitmapDrawable(context.resources, UtilityImg.eraseBackground(countyImgUrl.getImage(), -1)))
        }
        if (showTopography) {
            drawables.add(BitmapDrawable(context.resources, UtilityImg.eraseBackground(topographyImgUrl.getImage(), -1)))
        }
        if (showRadar) {
            val bitmapRadar = radImgUrl.getImage()
            bitmap = UtilityImg.eraseBackground(bitmap, -1)
            drawables.add(BitmapDrawable(context.resources, bitmapRadar))
        }
        drawables.add(BitmapDrawable(context.resources, bitmap))
        if (showOutlook) {
            drawables.add(BitmapDrawable(context.resources, UtilityImg.eraseBackground(outlookImgUrl.getImage(), -1)))
        }
        if (showWatchWarn) {
            drawables.add(BitmapDrawable(context.resources, UtilityImg.eraseBackground(watchWarningImgUrl.getImage(), -1)))
        }
        return UtilityImg.layerDrawableToBitmap(drawables)
    }

    fun getAnimation(context: Context, product: String, sector: String, frameCount: Int): AnimationDrawable {
        var urls = listOf<String>()
        val timeList = "${GlobalVariables.nwsSPCwebsitePrefix}/exper/mesoanalysis/new/archiveviewer.php?sector=19&parm=pmsl".getHtml().parseColumn("dattim\\[[0-9]{1,2}\\].*?=.*?([0-9]{8})")
        val delay = UtilityImg.animInterval(context)
        if (timeList.size > frameCount) {
            urls = (frameCount - 1 downTo 0).map {
                "${GlobalVariables.nwsSPCwebsitePrefix}/exper/mesoanalysis/s" + sector + "/" + product + "/" + product + "_" + timeList[it] + ".gif"
            }
        }
        return UtilityImgAnim.getAnimationDrawableFromUrlListWhiteBackground(context, urls, delay)
    }
}

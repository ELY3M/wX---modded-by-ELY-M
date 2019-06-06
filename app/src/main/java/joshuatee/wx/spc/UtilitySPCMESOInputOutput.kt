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
import joshuatee.wx.MyApplication
import joshuatee.wx.util.Utility

import kotlin.math.*

object UtilitySpcMesoInputOutput {

    fun getImage(context: Context, param: String, sector: String): Bitmap {
        val prefModel = "SPCMESO"
        val showRadar = Utility.readPref(context, prefModel + "_SHOW_RADAR", "false").startsWith("t")
        val showOutlook = Utility.readPref(context, prefModel + "_SHOW_OUTLOOK", "false").startsWith("t")
        val showWatchWarn = Utility.readPref(context, prefModel + "_SHOW_WATWARN", "false").startsWith("t")
        val showTopography = Utility.readPref(context, prefModel + "_SHOW_TOPO", "false").startsWith("t")
        val drawables = mutableListOf<Drawable>()
        val gifUrl = if (UtilitySpcMeso.imgSf.contains(param) && !showRadar) {
            "_sf.gif"
        } else {
            ".gif"
        }
        val imgUrl = "${MyApplication.nwsSPCwebsitePrefix}/exper/mesoanalysis/s$sector/$param/$param$gifUrl"
        val radImgUrl = "${MyApplication.nwsSPCwebsitePrefix}/exper/mesoanalysis/s$sector/rgnlrad/rgnlrad.gif"
        val outlookImgUrl = "${MyApplication.nwsSPCwebsitePrefix}/exper/mesoanalysis/s$sector/otlk/otlk.gif"
        val watchWarningImgUrl = "${MyApplication.nwsSPCwebsitePrefix}/exper/mesoanalysis/s$sector/warns/warns.gif"
        val topographyImgUrl = "${MyApplication.nwsSPCwebsitePrefix}/exper/mesoanalysis/s$sector/topo/topo.gif"
        var bitmap = imgUrl.getImage()
        drawables.add(ColorDrawable(Color.WHITE))
        if (showTopography) {
            drawables.add(
                BitmapDrawable(
                    context.resources,
                    UtilityImg.eraseBG(topographyImgUrl.getImage(), -1)
                )
            )
        }
        if (showRadar) {
            val bitmapradar = radImgUrl.getImage()
            bitmap = UtilityImg.eraseBG(bitmap, -1)
            drawables.add(BitmapDrawable(context.resources, bitmapradar))
        }
        drawables.add(BitmapDrawable(context.resources, bitmap))
        if (showOutlook) {
            drawables.add(
                BitmapDrawable(
                    context.resources,
                    UtilityImg.eraseBG(outlookImgUrl.getImage(), -1)
                )
            )
        }
        if (showWatchWarn) {
            drawables.add(
                BitmapDrawable(
                    context.resources,
                    UtilityImg.eraseBG(watchWarningImgUrl.getImage(), -1)
                )
            )
        }
        return UtilityImg.layerDrawableToBitmap(drawables)
    }

    fun getAnimation(
        context: Context,
        sector: String,
        param: String,
        frameCnt: Int
    ): AnimationDrawable {
        val urlAl = mutableListOf<String>()
        val timeList =
            ("${MyApplication.nwsSPCwebsitePrefix}/exper/mesoanalysis/new/archiveviewer.php?sector=19&parm=pmsl").getHtml()
                .parseColumn("dattim\\[[0-9]{1,2}\\].*?=.*?([0-9]{8})")
        val delay = UtilityImg.animInterval(context)
        if (timeList.size > frameCnt) {
            stride(
                frameCnt - 1,
                -1,
                -1
            ).mapTo(urlAl) { "${MyApplication.nwsSPCwebsitePrefix}/exper/mesoanalysis/s" + sector + "/" + param + "/" + param + "_" + timeList[it] + ".gif" }
        }
        return UtilityImgAnim.getAnimationDrawableFromUrlListWhiteBG(context, urlAl, delay)
    }

    private fun stride(start: Int, end: Int, incr: Int): IntArray {
        val arrSize = ceil((end - start).toDouble() / incr.toDouble()).toInt()
        val retArr = IntArray(arrSize)
        var j = 0
        if (start < end) {
            var i = start
            while (i < end) {
                retArr[j] = i
                j += 1
                i += incr
            }
        } else {
            var i = start
            while (i > end) {
                retArr[j] = i
                j += 1
                i += incr
            }
        }
        return retArr
    }
}





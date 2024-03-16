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

package joshuatee.wx.spc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.getHtml
import joshuatee.wx.getImage
import joshuatee.wx.parseColumn
import joshuatee.wx.ui.ObjectToolbar

object UtilitySpcMesoInputOutput {

    fun getLayers(context: Context, objectToolbar: ObjectToolbar? = null, getFunction: () -> Unit = {}): Map<SpcMesoLayerType, SpcMesoLayer> {
        val layers = mutableMapOf<SpcMesoLayerType, SpcMesoLayer>()
        SpcMesoLayerType.entries.forEach {
            layers[it] = SpcMesoLayer(context, it, objectToolbar, getFunction)
        }
        return layers
    }

    fun getImage(context: Context, param: String, sector: String, layers: Map<SpcMesoLayerType, SpcMesoLayer>): Bitmap {
        val drawables = mutableListOf<Drawable>()
        val gifUrl = if (UtilitySpcMeso.imgSf.contains(param) && !layers[SpcMesoLayerType.Radar]!!.isEnabled) {
            "_sf.gif"
        } else {
            ".gif"
        }
        val imgUrl = "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/exper/mesoanalysis/s$sector/$param/$param$gifUrl"
        val bitmap = imgUrl.getImage()
        drawables.add(ColorDrawable(Color.WHITE))
        if (param == "hodo" || param.startsWith("skewt")) {
            layers[SpcMesoLayerType.Radar]!!.isEnabled = true
        }
        listOf(
                SpcMesoLayerType.Population,
                SpcMesoLayerType.Topography,
                SpcMesoLayerType.County,
                SpcMesoLayerType.Radar,
                SpcMesoLayerType.Observations,
        ).forEach {
            if (layers[it]!!.isEnabled) {
                drawables.add(getDrawable(context, layers[it]!!.getUrl(sector)))
            }
        }
        drawables.add(BitmapDrawable(context.resources, bitmap))
        listOf(SpcMesoLayerType.Outlook, SpcMesoLayerType.Watwarn).forEach {
            if (layers[it]!!.isEnabled) {
                drawables.add(getDrawable(context, layers[it]!!.getUrl(sector)))
            }
        }
        return UtilityImg.layerDrawableToBitmap(drawables)
    }

    private fun getDrawable(context: Context, url: String): BitmapDrawable =
            BitmapDrawable(context.resources, UtilityImg.eraseBackground(url.getImage(), -1))

    fun getAnimation(product: String, sector: String, frameCount: Int): List<String> {
        val timeList = "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/exper/mesoanalysis/new/archiveviewer.php?sector=19&parm=pmsl".getHtml().parseColumn("dattim\\[[0-9]{1,2}\\].*?=.*?([0-9]{8})")
        return if (timeList.size > frameCount) {
            (frameCount - 1 downTo 0).map {
                "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/exper/mesoanalysis/s" + sector + "/" + product + "/" + product + "_" + timeList[it] + ".gif"
            }
        } else {
            emptyList()
        }
    }
}

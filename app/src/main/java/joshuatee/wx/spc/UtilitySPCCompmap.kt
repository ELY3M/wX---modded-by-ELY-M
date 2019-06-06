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
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import joshuatee.wx.MyApplication

import joshuatee.wx.external.ExternalGifDecoder
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityImg

internal object UtilitySpcCompmap {

    fun getImage(context: Context, layerString: String): Bitmap {
        val drawables = mutableListOf<Drawable>()
        val layers = layerString.split(":").dropLastWhile { it.isEmpty() }.toMutableList()
        drawables.add(ColorDrawable(Color.WHITE))
        if (layerString != "") {
            layers.indices.forEach {
                layers[it] = layers[it].replace("a", "")
                val gd = ExternalGifDecoder()
                gd.read(UtilityDownload.getInputStreamFromUrl("${MyApplication.nwsSPCwebsitePrefix}/exper/compmap/" + layers[it] + ".gif"))
                drawables.add(BitmapDrawable(context.resources, gd.bitmap))
            }
        } else {
            val gd = ExternalGifDecoder()
            gd.read(UtilityDownload.getInputStreamFromUrl("${MyApplication.nwsSPCwebsitePrefix}/exper/compmap/" + "basemap" + ".gif"))
            drawables.add(BitmapDrawable(context.resources, gd.bitmap))
        }
        return UtilityImg.layerDrawableToBitmap(drawables)
    }

    val labels = listOf(
        "IR satellite",
        "MAPS sea-level pressure (mb)",
        "2-meter temperature  (F)",
        "2-meter dewpoint temperature  (F)",
        "10m Wind Barbs",
        "CAPE/CINH",
        "HLCY/SHEAR",
        "3-hr surface pressure change",
        "Boundary layer mositure (mixing ratio) convergence",
        "K-Index and Precipitable Water (inches)",
        "12-hr Total Precipitation",
        "SFC OBS MAP",
        "Lapse rates 500-850mb",
        "850 WAA/WIND",
        "700 INFO",
        "700-500 mb layer average relative humidity",
        "500 mb height and absolute vorticity (dashed) field",
        "700-500 mb Upward Vertical Velocity",
        "300mb winds",
        "DAY 1 Outlook (94O)",
        "HPC Fronts (90F)",
        "HPC 6-hr QPF (92E)"
    )

    val urlIndex = listOf(
        "16",
        "7",
        "1",
        "0",
        "8",
        "2",
        "21",
        "3",
        "4",
        "5",
        "6",
        "9",
        "10",
        "11",
        "12",
        "13",
        "14",
        "15",
        "17",
        "18",
        "19",
        "20"
    )
}



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

package joshuatee.wx.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.radar.UtilityUSImgWX
import joshuatee.wx.radarcolorpalettes.ObjectColorPalette
import joshuatee.wx.util.UtilityFileManagement
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityLog

internal class TileObjectColorPalette(
        val colorMapLabel: String,
        val toolbar: Toolbar,
        val prefToken: String,
        context: Context,
        product: String, val builtin: Boolean
) {

    internal val bitmapWithText: Bitmap

    init {
        val oldMap: String
        val bitmap: Bitmap
        val textColor = if (builtin) Color.YELLOW else Color.WHITE
        val productAsInt = product.toIntOrNull() ?: 94
        if (UtilityFileManagement.internalFileExist(context, "colormap" + product + this.colorMapLabel)) {
            bitmapWithText = UtilityIO.bitmapFromInternalStorage(context, "colormap" + product + this.colorMapLabel)
        } else {
            oldMap = ObjectColorPalette.radarColorPalette[productAsInt]!!
            ObjectColorPalette.radarColorPalette[productAsInt] = colorMapLabel
            try {
                ObjectColorPalette.loadColorMap(context, productAsInt)
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
            bitmap = UtilityUSImgWX.bitmapForColorPalette(context, productAsInt)
            bitmapWithText = UtilityImg.drawTextToBitmap(context, bitmap, colorMapLabel, textColor)
            UtilityIO.bitmapToInternalStorage(context, bitmapWithText, "colormap$product$colorMapLabel")
            ObjectColorPalette.radarColorPalette[productAsInt] = oldMap
        }
    }
}

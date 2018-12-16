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

package joshuatee.wx.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.widget.Toolbar

import joshuatee.wx.MyApplication
import joshuatee.wx.radar.UtilityUSImgWX
import joshuatee.wx.radarcolorpalettes.UtilityColorPaletteGeneric
import joshuatee.wx.util.UtilityFileManagement
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityLog

internal class TileObjectColorPalette(
    val colorMapLabel: String,
    val tb: Toolbar,
    val prefToken: String,
    context: Context,
    prod: String,
    val builtin: Boolean
) {

    val bmPassed: Bitmap

    init {
        val oldMap: String
        val bm1: Bitmap
        var textColor = Color.WHITE
        if (builtin) textColor = Color.YELLOW
        if (UtilityFileManagement.internalFileExist(
                context,
                "colormap" + prod + this.colorMapLabel
            )
        ) {
            bmPassed = UtilityIO.bitmapFromInternalStorage(
                context,
                "colormap" + prod + this.colorMapLabel
            )
        } else {
            if (prod == "94") {
                oldMap = MyApplication.radarColorPalette[prod]!!
                MyApplication.radarColorPalette[prod] = colorMapLabel
                try {
                    UtilityColorPaletteGeneric.loadColorMap(context, "94")
                } catch (e: Exception) {
                    UtilityLog.HandleException(e)
                }
                bm1 = UtilityUSImgWX.bitmapForColorPalette(context, "N0Q")
                bmPassed = UtilityImg.drawTextToBitmap(context, bm1, colorMapLabel, textColor)
                UtilityIO.bitmapToInternalStorage(context, bmPassed, "colormap94$colorMapLabel")
                MyApplication.radarColorPalette[prod] = oldMap
            } else {
                oldMap = MyApplication.radarColorPalette[prod]!!
                MyApplication.radarColorPalette[prod] = colorMapLabel
                try {
                    UtilityColorPaletteGeneric.loadColorMap(context, "99")
                } catch (e: Exception) {
                    UtilityLog.HandleException(e)
                }
                bm1 = UtilityUSImgWX.bitmapForColorPalette(context, "N0U")
                bmPassed = UtilityImg.drawTextToBitmap(context, bm1, colorMapLabel, textColor)
                UtilityIO.bitmapToInternalStorage(context, bmPassed, "colormap99$colorMapLabel")
                MyApplication.radarColorPalette[prod] = oldMap
            }
        }
    }
}

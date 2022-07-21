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

package joshuatee.wx.radarcolorpalettes

import android.content.Context

internal object UtilityColorPalette165 {

    private const val radarColorPaletteCode = 165

    private fun generate(context: Context, code: String) {
        val objectColorPalette = ObjectColorPalette.colorMap[radarColorPaletteCode]!!
        objectColorPalette.position(0)
        val objectColorPaletteLines = mutableListOf<ObjectColorPaletteLine>()
        val text = UtilityColorPalette.getColorMapStringFromDisk(context, radarColorPaletteCode, code)
        val lines = text.split("\n").dropLastWhile { it.isEmpty() }
        lines.forEach { line ->
            if (line.contains("olor") && !line.contains("#")) {
                val items = if (line.contains(",")) {
                    line.split(",")
                } else {
                    line.split(" ")
                }
                if (items.size > 4) {
                    objectColorPaletteLines.add(ObjectColorPaletteLine(items))
                }
            }
        }
        val diff = 10
        objectColorPaletteLines.forEach {
            (0 until diff).forEach { _ ->
                objectColorPalette.putLine(it)
            }
        }
    }

    fun loadColorMap(context: Context) {
        generate(context, ObjectColorPalette.radarColorPalette[radarColorPaletteCode]!!)
    }
}

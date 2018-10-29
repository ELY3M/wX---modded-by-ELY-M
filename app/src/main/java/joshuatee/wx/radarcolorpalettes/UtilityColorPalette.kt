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

package joshuatee.wx.radarcolorpalettes

import android.content.Context

import joshuatee.wx.R
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityIO

object UtilityColorPalette {


    //TODO move those files to /wX/pal/
    //todo Where is one for SRM (code 56)

    fun getColorMapStringFromDisk(context: Context, prod: String, code: String): String {
        var cmFileInt = 0
        var text = "null"
        when (prod) {
            "94" -> when (code) {
                "AF" -> cmFileInt = R.raw.colormaprefaf
                "EAK" -> cmFileInt = R.raw.colormaprefeak
                "DKenh" -> cmFileInt = R.raw.colormaprefdkenh
                "CUST", "CODE" -> cmFileInt = R.raw.colormaprefcode
                "NSSL" -> cmFileInt = R.raw.colormaprefnssl
                "NWSD" -> cmFileInt = R.raw.colormaprefnwsd
                "COD", "CODENH" -> cmFileInt = R.raw.colormaprefcodenh
                "MENH" -> cmFileInt = R.raw.colormaprefmenh
                "ELY" -> cmFileInt = R.raw.colormapownref
                else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + prod + "_" + code, "")
            }
            "99" -> when (code) {
                "COD", "CODENH" -> cmFileInt = R.raw.colormapbvcod
                "AF" -> cmFileInt = R.raw.colormapbvaf
                "EAK" -> cmFileInt = R.raw.colormapbveak
                "ELY" -> cmFileInt = R.raw.colormapownvel
                else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + prod + "_" + code, "")
            }
            "135" -> when (code) {
                "COD", "CODENH" -> cmFileInt = R.raw.colormap135cod
                else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + prod + "_" + code, "")
            }
            "161" -> when (code) {
                "COD", "CODENH" -> cmFileInt = R.raw.colormap161cod
                else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + prod + "_" + code, "")
            }
            "163" -> when (code) {
                "COD", "CODENH" -> cmFileInt = R.raw.colormap163cod
                else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + prod + "_" + code, "")
            }
            "159" -> when (code) {
                "COD", "CODENH" -> cmFileInt = R.raw.colormap159cod
                else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + prod + "_" + code, "")
            }
            "134" -> when (code) {
                "COD", "CODENH" -> cmFileInt = R.raw.colormap134cod
                else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + prod + "_" + code, "")
            }
            "165" -> when (code) {
                "COD", "CODENH" -> cmFileInt = R.raw.colormap165cod
                else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + prod + "_" + code, "")
            }
            "172" -> when (code) {
                "COD", "CODENH" -> cmFileInt = R.raw.colormap172cod
                else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + prod + "_" + code, "")
            }
        }
        if (text == "null") {
            text = UtilityIO.readTextFile(context.resources.openRawResource(cmFileInt))
        }
        return text
    }
}





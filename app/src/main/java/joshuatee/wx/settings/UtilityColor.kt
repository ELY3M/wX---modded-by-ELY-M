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
//modded by ELY M.  

package joshuatee.wx.settings

import android.graphics.Color

import joshuatee.wx.MyApplication

internal object UtilityColor {

    fun setColor(prefVal: String) = when (prefVal) {
        "RADAR_COLOR_HW" -> if (MyApplication.blackBg) {
            Color.BLUE
        } else {
            Color.BLUE
        }
        "DRAW_TOOL_COLOR" -> Color.rgb(143, 213, 253)
        "RADAR_COLOR_HW_EXT" -> if (MyApplication.blackBg) {
            Color.BLUE
        } else {
            Color.BLUE
        }
        "RADAR_COLOR_STATE" -> Color.WHITE
        "RADAR_COLOR_TOR_WATCH" -> Color.rgb(113,0,0) //darker red than tornado warning polygon
        "RADAR_COLOR_SVR_WATCH" -> Color.BLUE
        "RADAR_COLOR_TOR" -> Color.RED
        "RADAR_COLOR_SVR" -> Color.YELLOW
        "RADAR_COLOR_EWW" -> Color.GRAY
        "RADAR_COLOR_FFW" -> Color.GREEN
        "RADAR_COLOR_SMW" -> Color.CYAN
        "RADAR_COLOR_SVS" -> Color.rgb(255, 203, 103)
        "RADAR_COLOR_SPS" -> Color.rgb(255, 204, 102)
        "RADAR_COLOR_MCD" -> Color.rgb(255, 255, 163)
        "RADAR_COLOR_WAT" -> Color.rgb(255, 255, 0)
        "RADAR_COLOR_MPD" -> Color.rgb(0, 255, 0)
        "RADAR_COLOR_LOCDOT" -> Color.WHITE
        "RADAR_COLOR_SPOTTER" -> Color.GREEN
        "RADAR_COLOR_CITY" -> Color.WHITE
        "RADAR_COLOR_LAKES" -> Color.rgb(0,0,163)
        "RADAR_COLOR_COUNTY" -> Color.rgb(75, 75, 75)
        "RADAR_COLOR_STI" -> Color.rgb(255, 255, 255)
        "RADAR_COLOR_HI" -> Color.GREEN
        "RADAR_COLOR_HI_TEXT" -> Color.WHITE
        "RADAR_COLOR_OBS" -> Color.WHITE
        "RADAR_COLOR_OBS_WINDBARBS" -> Color.WHITE
        "RADAR_COLOR_COUNTY_LABELS" -> Color.rgb(75, 75, 75)
        "NWS_ICON_TEXT_COLOR" -> Color.rgb(38, 97, 139)
        "NWS_ICON_BOTTOM_COLOR" -> Color.rgb(255, 255, 255)
        "WIDGET_TEXT_COLOR" -> Color.WHITE
        "WIDGET_HIGHLIGHT_TEXT_COLOR" -> Color.YELLOW
        "NEXRAD_RADAR_BACKGROUND_COLOR" -> Color.BLACK
        else -> Color.BLACK
    }
}

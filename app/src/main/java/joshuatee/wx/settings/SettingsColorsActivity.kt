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
//modded by ELY M.

package joshuatee.wx.settings

import android.annotation.SuppressLint
import android.os.Bundle

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.ui.BaseActivity

import kotlinx.android.synthetic.main.activity_linear_layout.*

class SettingsColorsActivity : BaseActivity() {

    private val colorObjects = mutableListOf<ObjectSettingsColorLabel>()

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        val mapColorToPref = mapOf(
                "Highway color" to "RADAR_COLOR_HW",
                "Secondary Highway color" to "RADAR_COLOR_HW_EXT",
                "State border color" to "RADAR_COLOR_STATE",
                "Tornado watch color" to "RADAR_COLOR_TOR_WATCH",
                "Severe Storm watch color" to "RADAR_COLOR_SVR_WATCH",
                "Tornado warning color" to "RADAR_COLOR_TOR",
                "Severe Storm warning color" to "RADAR_COLOR_SVR",
                "Extreme Wind warning color" to "RADAR_COLOR_EWW",
                "Flash flood warning color" to "RADAR_COLOR_FFW",
                "Special Marine warning color" to "RADAR_COLOR_SMW",
                "Severe Weather Statement color" to "RADAR_COLOR_SVS",
                "Special Weather Statement color" to "RADAR_COLOR_SPS",
                "MCD color" to "RADAR_COLOR_MCD",
                "MPD color" to "RADAR_COLOR_MPD",
                "Location dot color" to "RADAR_COLOR_LOCDOT",
                "Spotter color" to "RADAR_COLOR_SPOTTER",
                "City color" to "RADAR_COLOR_CITY",
                "Lakes color" to "RADAR_COLOR_LAKES",
                "County color" to "RADAR_COLOR_COUNTY",
                "County labels color" to "RADAR_COLOR_COUNTY_LABELS",
                "Storm tracks color" to "RADAR_COLOR_STI",
                "Hail marker color" to "RADAR_COLOR_HI",
                "Hail Text color" to "RADAR_COLOR_HI_TEXT",
                "Observations color" to "RADAR_COLOR_OBS",
                "Windbarb color" to "RADAR_COLOR_OBS_WINDBARBS",
                "Radar Legend Text Color" to "RADAR_SHOW_LEGEND_TEXTCOLOR",
                "Draw tool color" to "DRAW_TOOL_COLOR",
                "Widget Text color" to "WIDGET_TEXT_COLOR",
                "Widget Highlight Text color" to "WIDGET_HIGHLIGHT_TEXT_COLOR",
                "NWS Forecast Icon Text color" to "NWS_ICON_TEXT_COLOR",
                "NWS Forecast Icon Bottom color" to "NWS_ICON_BOTTOM_COLOR",
                "Nexrad Radar Background color" to "NEXRAD_RADAR_BACKGROUND_COLOR"
        )
        mapColorToPref.keys.asSequence().sorted()
            .mapTo(colorObjects) { ObjectSettingsColorLabel(this, it, mapColorToPref[it]!!) }
        colorObjects.forEach { ll.addView(it.card) }
    }

    override fun onRestart() {
        super.onRestart()
        setColorOnButtons()
    }

    override fun onStop() {
        GeographyType.refresh()
        PolygonType.refresh()
        super.onStop()
    }

    private fun setColorOnButtons() {
        MyApplication.initPreferences(this)
        colorObjects.forEach { it.refreshColor() }
    }
}


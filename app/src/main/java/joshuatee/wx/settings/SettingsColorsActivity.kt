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
//modded by ELY M.

package joshuatee.wx.settings

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.objects.PolygonWarning
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.PolygonWarningType
import joshuatee.wx.radar.RadarGeometry
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ColorLabel
import joshuatee.wx.ui.VBox

class SettingsColorsActivity : BaseActivity() {

    private var objectSettingsColorLabels = listOf<ColorLabel>()
    private lateinit var box: VBox

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        setTitle("Colors", "Tap on item to change color.")
        box = VBox.fromResource(this)
        box.setBackgroundColor(Color.BLACK)
        addCards()
    }

    private fun addCards() {
        val mapColorToPref = mutableMapOf(
            "Highway color" to "RADAR_COLOR_HW",
            "Secondary Highway color" to "RADAR_COLOR_HW_EXT",
            "State border color" to "RADAR_COLOR_STATE",
            "Tstorm warning color" to "RADAR_COLOR_TSTORM",
            "Tstorm watch color" to "RADAR_COLOR_TSTORM_WATCH",
            "Tornado warning color" to "RADAR_COLOR_TOR",
            "Tornado watch color" to "RADAR_COLOR_TOR_WATCH",
            "Flash flood warning color" to "RADAR_COLOR_FFW",
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
            "Wind barb color" to "RADAR_COLOR_OBS_WINDBARBS",
            "Radar Legend Text Color" to "RADAR_SHOW_LEGEND_TEXTCOLOR",
            "Draw tool color" to "DRAW_TOOL_COLOR",
            "Widget Text color" to "WIDGET_TEXT_COLOR",
            "Widget Highlight Text color" to "WIDGET_HIGHLIGHT_TEXT_COLOR",
            "NWS Forecast Icon Text color" to "NWS_ICON_TEXT_COLOR",
            "NWS Forecast Icon Bottom color" to "NWS_ICON_BOTTOM_COLOR",
            "Nexrad Radar Background color" to "NEXRAD_RADAR_BACKGROUND_COLOR"
        )
        PolygonWarning.byType.values.forEach {
            if (it.type != PolygonWarningType.FlashFloodWarning && it.type != PolygonWarningType.ThunderstormWarning && it.type != PolygonWarningType.TornadoWarning) {
                mapColorToPref[it.name + " color"] = it.prefTokenColor
            }
        }
        objectSettingsColorLabels = mapColorToPref.keys.sorted().map {
            ColorLabel(this, it, mapColorToPref[it]!!)
        }
        objectSettingsColorLabels.forEach {
            box.addWidget(it)
        }
    }

    override fun onRestart() {
        super.onRestart()
        setColorOnButtons()
    }

    override fun onStop() {
        RadarGeometry.updateConfig()
        PolygonType.refresh()
        super.onStop()
    }

    private fun setColorOnButtons() {
        MyApplication.initPreferences(this)
        objectSettingsColorLabels.forEach {
            it.refreshColor()
        }
    }
}

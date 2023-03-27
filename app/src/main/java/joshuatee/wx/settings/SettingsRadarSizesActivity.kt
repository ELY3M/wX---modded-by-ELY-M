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

import android.os.Bundle
import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.NumberPicker
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.Utility

class SettingsRadarSizesActivity : BaseActivity() {

    private lateinit var box: VBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        setTitle("Radar Line / Marker sizes", GlobalVariables.preferencesHelpTitle)
        box = VBox.fromResource(this)
        addNumberPicker()
    }

    private fun addNumberPicker() {
        val numberPickers = listOf(
            //elys mod
            NumberPicker(this, "Color Legend width", "RADAR_SHOW_LEGEND_WIDTH", R.string.showlegendwidth, 50, 1, 100),
            NumberPicker(this, "Color Legend text size", "RADAR_SHOW_LEGEND_TEXTSIZE", R.string.showlegendtextsize, 30, 1, 100),
            NumberPicker(this, "Conus Radar Zoom", "CONUS_RADAR_ZOOM", R.string.conus_radar_zoom_label, 75, 0, 530),
            NumberPicker(this, "Default line size", "RADAR_DEFAULT_LINESIZE", R.string.default_linesize_np_label, 1, 1, 15),
            //elys mod end
            NumberPicker(this, "Aviation dot size", "RADAR_AVIATION_SIZE", R.string.aviation_size_label, RadarPreferences.aviationSizeDefault, 1, 50),
            NumberPicker(this, "Convective outlook line size", "RADAR_SWO_LINESIZE", R.string.swo_linesize_np, 3, 1, 10),
            NumberPicker(this, "County line size", "RADAR_COUNTY_LINESIZE", R.string.county_linesize_np, 2, 1, 10),
            //NumberPicker(this, "Detailed observations Zoom", "RADAR_OBS_EXT_ZOOM", R.string.obs_ext_zoom_label, 7, 1, 10),
            NumberPicker(this, "Draw tool line size", "DRAWTOOL_SIZE", R.string.drawtool_size_label, 4, 1, 20),
            //elys mod - GPS Circle is NOT USED in this app.
            //NumberPicker(this, "GPS Circle line size", "RADAR_GPSCIRCLE_LINESIZE", R.string.gpscircle_linesize_np, 5, 1, 10),
            NumberPicker(this, "Location icon size", "RADAR_LOCICON_SIZE", R.string.locicon_size_np, 75, 1, 530),
            NumberPicker(this, "Location bug size", "RADAR_LOCBUG_SIZE", R.string.locbug_size_np, 75, 1, 530),
            NumberPicker(this, "Hail icon size", "RADAR_HI_SIZE", R.string.hi_size_np, 75, 1, 530),
            NumberPicker(this, "Hail Text size", "RADAR_HI_TEXT_SIZE", R.string.hi_size_np, (RadarPreferences.hiTextSize * 10).toInt(), 1, 20),
            //elys mod end
            NumberPicker(this, "Highway line size", "RADAR_HW_LINESIZE", R.string.hw_linesize_np, 2, 1, 10),
            NumberPicker(this, "Lake line size", "RADAR_LAKE_LINESIZE", R.string.lakes_linesize_np, 2, 1, 10),
            NumberPicker(this, "Location marker size", "RADAR_LOCDOT_SIZE", R.string.locdot_size_np, RadarPreferences.locationDotSizeDefault, 1, 50),
            NumberPicker(this, "MCD/MPD/Watch line size", "RADAR_WATMCD_LINESIZE", R.string.watmcd_linesize_np, RadarPreferences.watchMcdLineSizeDefault, 1, 10),
            NumberPicker(this, "Secondary road line size", "RADAR_HWEXT_LINESIZE", R.string.hwext_linesize_np, 2, 1, 10),
            NumberPicker(this, "State line size", "RADAR_STATE_LINESIZE", R.string.state_linesize_np, 2, 1, 10),
            NumberPicker(this, "Storm spotter size", "RADAR_SPOTTER_SIZE", R.string.spotter_size_label, RadarPreferences.spotterSizeDefault, 1, 50),
            NumberPicker(this, "Storm tracks line size", "RADAR_STI_LINESIZE", R.string.sti_linesize_np, 3, 1, 10),
            //elys mod
            NumberPicker(this, "TVS icon size", "RADAR_TVS_SIZE", R.string.tvs_size_np, 75, 1, 530),
            NumberPicker(this, "Userpoints icon size", "RADAR_USERPOINT_SIZE", R.string.userpoints_size_np, 75, 1, 530),
            //elys mod end
            NumberPicker(this, "Warning line size", "RADAR_WARN_LINESIZE", R.string.warn_linesize_np_label, RadarPreferences.warnLineSizeDefault, 1, 10),
            NumberPicker(this, "Wind barbs line size", "RADAR_WB_LINESIZE", R.string.wb_linesize_np, 3, 1, 10),
            NumberPicker(this, "WPC Fronts line size", "RADAR_WPC_FRONT_LINESIZE", R.string.wpc_fronts_linesize_np, 4, 1, 10),
            //NumberPicker(this, "WXOGL initial view size", "WXOGL_SIZE", R.string.wxogl_size_np, RadarPreferences.wxoglSizeDefault, 5, 25),
        )
        numberPickers.forEach {
            box.addWidget(it)
        }
    }

    // formerly onStop
    override fun onPause() {
        Utility.commitPref(this)
        MyApplication.initPreferences(this)
        super.onPause()
    }
}

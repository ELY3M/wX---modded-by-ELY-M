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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.PolygonWarning
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.PolygonWarningType
import joshuatee.wx.radar.RadarGeometry
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.CardText
import joshuatee.wx.ui.NumberPicker
import joshuatee.wx.ui.Switch
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.Utility

class SettingsRadarActivity : BaseActivity() {

    private lateinit var box: VBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        setTitle("Radar", GlobalVariables.PREFERENCES_HELP_TITLE)
        box = VBox.fromResource(this)
        addCards()
        addSwitch1()
        addSwitchGps()
        addSwitch2()
        addNumberPickers()
    }

    private fun addCards() {
        box.addWidget(CardText(this, "Colors", SettingsColorsActivity::class.java))
        box.addWidget(CardText(this, "Color Palettes (Beta)", SettingsColorPaletteListingActivity::class.java))
        box.addWidget(CardText(this, "Line / Marker sizes", SettingsRadarSizesActivity::class.java))
    }

    private fun addSwitch1() {
        box.addWidget(
                Switch(this, "Warnings (TST/TOR/FFW)", "COD_WARNINGS_DEFAULT", R.string.cod_warnings_default_label)
        )
        PolygonWarning.byType.values.forEach {
            if (it.type != PolygonWarningType.FlashFloodWarning && it.type != PolygonWarningType.ThunderstormWarning && it.type != PolygonWarningType.TornadoWarning) {
                box.addWidget(
                        Switch(this, it.name, it.prefTokenEnabled, R.string.cod_warnings_default_label)
                )
            }
        }
        val configs1 = listOf(
                Switch(this, "SPC MCD/Watches", "RADAR_SHOW_WATCH", R.string.radar_show_watch_default_label),
                Switch(this, "WPC MPDs", "RADAR_SHOW_MPD", R.string.radar_show_mpd_default_label),
                Switch(this, "Location markers", "COD_LOCDOT_DEFAULT", R.string.cod_locdot_default_label),
        )
        configs1.forEach {
            box.addWidget(it)
        }
    }

    private fun addSwitchGps() {
        val gpsSw = Switch(this, "Location marker follows GPS", "LOCDOT_FOLLOWS_GPS", R.string.locdot_follows_gps_label)
        box.addWidget(gpsSw)
        gpsSw.setOnCheckedChangeListener { compoundButton, _ ->
            RadarPreferences.locationDotFollowsGps = compoundButton.isChecked
            if (RadarPreferences.locationDotFollowsGps != Utility.readPref(
                            this,
                            "LOCDOT_FOLLOWS_GPS",
                            "false"
                    ).startsWith("t")
            ) {
                showGPSPermsDialogue()
            }
            if (compoundButton.isChecked) {
                Utility.writePref(this, "LOCDOT_FOLLOWS_GPS", "true")
            } else {
                Utility.writePref(this, "LOCDOT_FOLLOWS_GPS", "false")
            }
        }
    }

    private fun addSwitch2() {
        val configs2 = listOf(
		//elys mod
		Switch(this, "Location Heading Bug", "LOCDOT_BUG", R.string.locdot_bug_label),
		//elys mod end
                Switch(this, "Black background", "NWS_RADAR_BG_BLACK", R.string.nws_black_bg_label),
                Switch(this, "Canadian and Mexican borders", "RADAR_CAMX_BORDERS", R.string.camx_borders_label),
                Switch(this, "Center radar on location", "RADAR_CENTER_ON_LOCATION", R.string.radar_center_on_location_default_label),
                Switch(this, "Cities", "COD_CITIES_DEFAULT", R.string.cod_cities_default_label),
                Switch(this, "Counties", "RADAR_SHOW_COUNTY", R.string.show_county_label),
                Switch(this, "County labels", "RADAR_COUNTY_LABELS", R.string.show_county_labels_label),
                Switch(this, "Hail index", "RADAR_SHOW_HI", R.string.show_hi_label),
		        //elys mod
		        Switch(this, "Hail labels", "WXOGL_HAIL_LABEL", R.string.show_hi_label_label),
		        //elys mod end
                Switch(this, "Highways", "COD_HW_DEFAULT", R.string.cod_hw_default_label),
                Switch(this, "Lakes and rivers", "COD_LAKES_DEFAULT", R.string.cod_lakes_default_label),
                Switch(this, "Multi-pane will share lat/lon/zoom", "DUALPANE_SHARE_POSN", R.string.dual_pane_share_position_label),
                Switch(this, "Observations", "WXOGL_OBS", R.string.obs_label),
                Switch(this, "Remember location/product", "WXOGL_REMEMBER_LOCATION", R.string.remember_loc_label),
                Switch(this, "Screen stays on and auto refresh radar", "RADAR_AUTOREFRESH", R.string.autorefresh_label),
                Switch(this, "Secondary roads", "RADAR_HW_ENH_EXT", R.string.hw_enh_ext_label),
                Switch(this, "Show radar during a pan/drag motion", "SHOW_RADAR_WHEN_PAN", R.string.show_radar_when_pan_label),
                Switch(this, "SPC Convective Outlook Day One", "RADAR_SHOW_SWO", R.string.show_swo_label),
                Switch(this, "Spotters", "WXOGL_SPOTTERS", R.string.spotters_label),
                Switch(this, "Spotter labels", "WXOGL_SPOTTERS_LABEL", R.string.spotters_label_label),
                //elys mod
		        Switch(this, "Spotter labels Show Last names only", "WXOGL_SPOTTERS_LABEL_LASTNAME", R.string.spotters_label_lastname),
                Switch(this, "Storm tracks", "RADAR_SHOW_STI", R.string.show_sti_label),
                Switch(this, "Tornado Vortex Signature (TVS)", "RADAR_SHOW_TVS", R.string.show_tvs_label),
                Switch(this, "Wind barbs", "WXOGL_OBS_WINDBARBS", R.string.obs_windbarbs_label),
                Switch(this, "WPC Surface Fronts and Pressures", "RADAR_SHOW_WPC_FRONTS", R.string.radar_show_wpc_fronts_label),
		        //elys mod
		        Switch(this, "Enable userpoints icons on radar", "RADAR_USERPOINTS", R.string.radar_userpoints),
            	Switch(this, "Hide radar", "RADAR_HIDE_RADAR", R.string.hide_radar),
        )
        configs2.forEach {
            box.addWidget(it)
        }
    }

    private fun addNumberPickers() {
        val numberPickers = listOf(
                NumberPicker(this, "Animation speed", "ANIM_INTERVAL", R.string.animation_interval_np_label, UIPreferences.ANIMATION_INTERVAL_DEFAULT, 1, 15),
                NumberPicker(this, "GPS update interval", "RADAR_LOCATION_UPDATE_INTERVAL", R.string.gps_update_interval_label, 10, 1, 60),
                NumberPicker(this, "Refresh interval", "RADAR_REFRESH_INTERVAL", R.string.wxogl_refresh_interval_label, 3, 1, 15),
                NumberPicker(this, "Detailed observations Zoom", "RADAR_OBS_EXT_ZOOM", R.string.obs_ext_zoom_label, 7, 1, 10),
                NumberPicker(this, "Text size", "RADAR_TEXT_SIZE", R.string.text_size_label, (RadarPreferences.textSizeDefault * 10).toInt(), 4, 40),
                NumberPicker(this, "WXOGL initial view size", "WXOGL_SIZE", R.string.wxogl_size_np, RadarPreferences.wxoglSizeDefault, 5, 25),
        )
        numberPickers.forEach {
            box.addWidget(it)
        }
    }

    // formerly onStop
    override fun onPause() {
        Utility.commitPref(this)
        MyApplication.initPreferences(this)
        val restartNotif = Utility.readPref(this, "RESTART_NOTIF", "false")
        if (restartNotif == "true") {
            UtilityWXJobService.startService(this)
            Utility.writePref(this, "RESTART_NOTIF", "false")
        }
        RadarGeometry.updateConfig()
        PolygonType.refresh()
        RadarPreferences.initGenericRadarWarnings(this)
        super.onPause()
    }

    private fun showGPSPermsDialogue() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), myPermissionsAccessFineLocation)
        }
    }

    private val myPermissionsAccessFineLocation = 5001
}

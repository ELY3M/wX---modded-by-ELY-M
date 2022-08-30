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
//modded by ELY M. 

package joshuatee.wx.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.ObjectPolygonWarning
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.PolygonWarningType
import joshuatee.wx.radar.RadarGeometry
import joshuatee.wx.telecine.SettingsTelecineActivity
import joshuatee.wx.ui.*
import joshuatee.wx.ui.ObjectNumberPicker
import joshuatee.wx.util.Utility

class SettingsRadarActivity : BaseActivity() {

    private lateinit var box: VBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        setTitle("Radar", "Please tap on text for additional help.")
        box = VBox.fromResource(this)
        val textSize = UIPreferences.textSizeLarge
        val padding = UIPreferences.paddingSettings
        CardText(this, box.get(), "Colors", textSize, SettingsColorsActivity::class.java, padding)
        CardText(this, box.get(), "Color Palettes", textSize, SettingsColorPaletteListingActivity::class.java, padding)
        CardText(this, box.get(), "Line / Marker sizes", textSize, SettingsRadarSizesActivity::class.java, padding)
        CardText(this, box.get(), "Screen Recorder", textSize, SettingsTelecineActivity::class.java, padding)
        box.addWidget(
                ObjectSwitch(this, "Warnings (TST/TOR/FFW)", "COD_WARNINGS_DEFAULT", R.string.cod_warnings_default_label).get()
        )
        ObjectPolygonWarning.polygonDataByType.values.forEach {
            if (it.type != PolygonWarningType.FlashFloodWarning &&  it.type != PolygonWarningType.ThunderstormWarning && it.type != PolygonWarningType.TornadoWarning) {
                box.addWidget(
                        ObjectSwitch(this, it.name, it.prefTokenEnabled, R.string.cod_warnings_default_label).get()
                )
            }
        }
        val configs1 = listOf(
            ObjectSwitch(this, "SPC MCD/Watches", "RADAR_SHOW_WATCH", R.string.radar_show_watch_default_label),
            ObjectSwitch(this, "WPC MPDs", "RADAR_SHOW_MPD", R.string.radar_show_mpd_default_label),
            ObjectSwitch(this, "Location marker", "COD_LOCDOT_DEFAULT", R.string.cod_locdot_default_label),
        )
        configs1.forEach {
            box.addWidget(it.get())
        }
        val gpsSw = ObjectSwitch(this, "Location marker follows GPS", "LOCDOT_FOLLOWS_GPS", R.string.locdot_follows_gps_label)
        box.addWidget(gpsSw.get())
        gpsSw.setOnCheckedChangeListener { compoundButton, _ ->
            RadarPreferences.locationDotFollowsGps = compoundButton.isChecked
            if (RadarPreferences.locationDotFollowsGps != Utility.readPref(
                            this,
                            "LOCDOT_FOLLOWS_GPS",
                            "false"
                    ).startsWith("t")
            )
                showGPSPermsDialogue()

            if (compoundButton.isChecked) {
                Utility.writePref(this, "LOCDOT_FOLLOWS_GPS", "true")
            } else {
                Utility.writePref(this, "LOCDOT_FOLLOWS_GPS", "false")
            }
        }
        val configs2 = listOf(
		//elys mod
		ObjectSwitch(this, "Location Heading Bug", "LOCDOT_BUG", R.string.locdot_bug_label),
		//elys mod end
                ObjectSwitch(this, "Black background", "NWS_RADAR_BG_BLACK", R.string.nws_black_bg_label),
                ObjectSwitch(this, "Canadian and Mexican borders", "RADAR_CAMX_BORDERS", R.string.camx_borders_label),
                ObjectSwitch(this, "Center radar on location", "RADAR_CENTER_ON_LOCATION", R.string.radar_center_on_location_default_label),
                ObjectSwitch(this, "Cities", "COD_CITIES_DEFAULT", R.string.cod_cities_default_label),
	    	    //elys mod
	    	    ObjectSwitch(this, "Conus Radar", "CONUS_RADAR", R.string.conus_radar_label),
	    	    //elys mod end
                ObjectSwitch(this, "Convective Outlook Day One", "RADAR_SHOW_SWO", R.string.show_swo_label),
                ObjectSwitch(this, "Counties", "RADAR_SHOW_COUNTY", R.string.show_county_label),
                ObjectSwitch(this, "Counties use high resolution data", "RADAR_COUNTY_HIRES", R.string.county_hires_label),
                ObjectSwitch(this, "County labels", "RADAR_COUNTY_LABELS", R.string.show_county_labels_label),
                ObjectSwitch(this, "Hail index", "RADAR_SHOW_HI", R.string.show_hi_label),
		        //elys mod
		        ObjectSwitch(this, "Hail labels", "WXOGL_HAIL_LABEL", R.string.show_hi_label_label),
		        //elys mod end
                ObjectSwitch(this, "Highways", "COD_HW_DEFAULT", R.string.cod_hw_default_label),
                ObjectSwitch(this, "Launch app directly to radar", "LAUNCH_TO_RADAR", R.string.launch_to_radar_label),
                ObjectSwitch(this, "Lakes and rivers", "COD_LAKES_DEFAULT", R.string.cod_lakes_default_label),
                ObjectSwitch(this, "Multi-pane: share lat/lon/zoom", "DUALPANE_SHARE_POSN", R.string.dualpaneshareposn_label),
                ObjectSwitch(this, "Multipurpose radar icons", "WXOGL_ICONS_LEVEL2", R.string.radar_icons_level2_label),
                ObjectSwitch(this, "Observations", "WXOGL_OBS", R.string.obs_label),
                ObjectSwitch(this, "Remember location / product", "WXOGL_REMEMBER_LOCATION", R.string.rememberloc_label),
                ObjectSwitch(this, "Screen stays on and auto refresh radar", "RADAR_AUTOREFRESH", R.string.autorefresh_label),
                ObjectSwitch(this, "Secondary roads", "RADAR_HW_ENH_EXT", R.string.hw_enh_ext_label),
                ObjectSwitch(this, "Show radar during a pan/drag motion", "SHOW_RADAR_WHEN_PAN", R.string.show_radar_when_pan_label),
                ObjectSwitch(this, "SPC Convective Outlook Day One", "RADAR_SHOW_SWO", R.string.show_swo_label),
                ObjectSwitch(this, "Spotters", "WXOGL_SPOTTERS", R.string.spotters_label),
                ObjectSwitch(this, "Spotter labels", "WXOGL_SPOTTERS_LABEL", R.string.spotters_label_label),
                ObjectSwitch(this, "States use high resolution data", "RADAR_STATE_HIRES", R.string.state_hires_label),
                ObjectSwitch(this, "Storm tracks", "RADAR_SHOW_STI", R.string.show_sti_label),
                ObjectSwitch(this, "Tornado Vortex Signature", "RADAR_SHOW_TVS", R.string.show_tvs_label),
                ObjectSwitch(this, "Use JNI for radar (beta)", "RADAR_USE_JNI", R.string.radar_use_jni_label),
                ObjectSwitch(this, "Wind barbs", "WXOGL_OBS_WINDBARBS", R.string.obs_windbarbs_label),
                ObjectSwitch(this, "WPC Fronts and pressure highs and lows", "RADAR_SHOW_WPC_FRONTS", R.string.radar_show_wpc_fronts_label),
		        //elys mod
		        ObjectSwitch(this, "Enable userpoints icons on radar", "RADAR_USERPOINTS", R.string.radar_userpoints),
            	ObjectSwitch(this, "Hide radar", "RADAR_HIDE_RADAR", R.string.hide_radar),


        )
        configs2.forEach {
            box.addWidget(it.get())
        }
        val numberPickers = listOf(
            ObjectNumberPicker(this, "Animation speed", "ANIM_INTERVAL", R.string.animation_interval_np_label, UIPreferences.animationIntervalDefault, 1, 15),
			//elys mod
			//ObjectNumberPicker(this, "Color Legend width", "RADAR_SHOW_LEGEND_WIDTH", R.string.showlegendwidth, 50, 1, 100),
			//ObjectNumberPicker(this, "Color Legend text size", "RADAR_SHOW_LEGEND_TEXTSIZE", R.string.showlegendtextsize, 30, 1, 100),
			//ObjectNumberPicker(this, "Conus Radar Zoom", "CONUS_RADAR_ZOOM", R.string.conus_radar_zoom_label, 75, 0, 530),
			//ObjectNumberPicker(this, "Default line size", "RADAR_DEFAULT_LINESIZE", R.string.default_linesize_np_label, 1, 1, 15),
			//elys mod end
            ObjectNumberPicker(this, "GPS update interval", "RADAR_LOCATION_UPDATE_INTERVAL", R.string.gps_update_interval_label, 10, 1, 60),
            ObjectNumberPicker(this, "Refresh interval", "RADAR_REFRESH_INTERVAL", R.string.wxogl_refresh_interval_label, 3, 1, 15),
//            ObjectNumberPicker(this, "Aviation dot size", "RADAR_AVIATION_SIZE", R.string.aviation_size_label, RadarPreferences.aviationSizeDefault, 1, 50),
//            ObjectNumberPicker(this, "Convective outlook line size", "RADAR_SWO_LINESIZE", R.string.swo_linesize_np, 3, 1, 10),
//            ObjectNumberPicker(this, "County line size", "RADAR_COUNTY_LINESIZE", R.string.county_linesize_np, 2, 1, 10),
              ObjectNumberPicker(this, "Detailed observations Zoom", "RADAR_OBS_EXT_ZOOM", R.string.obs_ext_zoom_label, 7, 1, 10),
//            ObjectNumberPicker(this, "Draw tool line size", "DRAWTOOL_SIZE", R.string.drawtool_size_label, 4, 1, 20),
              //elys mod
              //ObjectNumberPicker(this, "GPS Circle line size", "RADAR_GPSCIRCLE_LINESIZE", R.string.gpscircle_linesize_np, 5, 1, 10),
//	          ObjectNumberPicker(this, "Location icon size", "RADAR_LOCICON_SIZE", R.string.locicon_size_np, 75, 1, 530),
//	          ObjectNumberPicker(this, "Location bug size", "RADAR_LOCBUG_SIZE", R.string.locbug_size_np, 75, 1, 530),
//            ObjectNumberPicker(this, "Hail icon size", "RADAR_HI_SIZE", R.string.hi_size_np, 75, 1, 530),
//	          ObjectNumberPicker(this, "Hail Text size", "RADAR_HI_TEXT_SIZE", R.string.hi_size_np, (RadarPreferences.hiTextSize * 10).toInt(), 1, 20),
	          //elys mod end
//            ObjectNumberPicker(this, "Highway line size", "RADAR_HW_LINESIZE", R.string.hw_linesize_np, 2, 1, 10),
//            ObjectNumberPicker(this, "Lake line size", "RADAR_LAKE_LINESIZE", R.string.lakes_linesize_np, 2, 1, 10),
//            ObjectNumberPicker(this, "Location marker size", "RADAR_LOCDOT_SIZE", R.string.locdot_size_np, RadarPreferences.locationDotSizeDefault, 1, 50),
//            ObjectNumberPicker(this, "MCD/MPD/Watch line size", "RADAR_WATMCD_LINESIZE", R.string.watmcd_linesize_np, RadarPreferences.watchMcdLineSizeDefault, 1, 10),
//            ObjectNumberPicker(this, "Secondary road line size", "RADAR_HWEXT_LINESIZE", R.string.hwext_linesize_np, 2, 1, 10),
//            ObjectNumberPicker(this, "State line size", "RADAR_STATE_LINESIZE", R.string.state_linesize_np, 2, 1, 10),
//            ObjectNumberPicker(this, "Storm spotter size", "RADAR_SPOTTER_SIZE", R.string.spotter_size_label, RadarPreferences.spotterSizeDefault, 1, 50),
//            ObjectNumberPicker(this, "Storm tracks line size", "RADAR_STI_LINESIZE", R.string.sti_linesize_np, 3, 1, 10),
//            ObjectNumberPicker(this, "Text size", "RADAR_TEXT_SIZE", R.string.text_size_label, (RadarPreferences.textSizeDefault * 10).toInt(), 4, 40),
//	          //elys mod
//            ObjectNumberPicker(this, "TVS icon size", "RADAR_TVS_SIZE", R.string.tvs_size_np, 75, 1, 530),
//            ObjectNumberPicker(this, "Userpoints icon size", "RADAR_USERPOINT_SIZE", R.string.userpoints_size_np, 75, 1, 530),
//	          //elys mod end
//            ObjectNumberPicker(this, "Warning line size", "RADAR_WARN_LINESIZE", R.string.warn_linesize_np_label, RadarPreferences.warnLineSizeDefault, 1, 10),
//            ObjectNumberPicker(this, "Wind barbs line size", "RADAR_WB_LINESIZE", R.string.wb_linesize_np, 3, 1, 10),
//            ObjectNumberPicker(this, "WPC Fronts line size", "RADAR_WPC_FRONT_LINESIZE", R.string.wpc_fronts_linesize_np, 4, 1, 10),
              ObjectNumberPicker(this, "WXOGL initial view size", "WXOGL_SIZE", R.string.wxogl_size_np, RadarPreferences.wxoglSizeDefault, 5, 25),
        )
        numberPickers.forEach {
            box.addWidget(it.get())
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
//        GeographyType.refresh()
        PolygonType.refresh()
        RadarPreferences.initGenericRadarWarnings(this)
        super.onPause()
    }

    private fun showGPSPermsDialogue() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), myPermissionsAccessFineLocation)
            }
        }
    }

    private val myPermissionsAccessFineLocation = 5001

    // FIXME decide what todo with this
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        when (requestCode) {
//            myPermissionsAccessFineLocation -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            }
//        }
    }
}

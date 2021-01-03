/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.CompoundButton

import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.telecine.SettingsTelecineActivity
import joshuatee.wx.util.Utility

import kotlinx.android.synthetic.main.activity_linear_layout.*

class SettingsRadarActivity : BaseActivity() {

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        toolbar.subtitle = "Please tap on text for additional help."
        val textSize = MyApplication.textSizeLarge
        val padding = MyApplication.paddingSettings
        ObjectCardText(this, linearLayout, "Colors", textSize, SettingsColorsActivity::class.java, padding)
        ObjectCardText(this, linearLayout, "Color Palettes", textSize, SettingsColorPaletteListingActivity::class.java, padding)
        ObjectCardText(this, linearLayout, "Screen Recorder", textSize, SettingsTelecineActivity::class.java, padding)
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Warnings (TST/TOR/FFW)",
                        "COD_WARNINGS_DEFAULT",
                        R.string.cod_warnings_default_label
                ).card
        )
        MyApplication.radarWarningPolygons.forEach {
            linearLayout.addView(
                    ObjectSettingsCheckBox(
                            this,
                            it.name,
                            it.prefTokenEnabled,
                            R.string.cod_warnings_default_label
                    ).card
            )
        }
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "SPC MCD/Watches",
                        "RADAR_SHOW_WATCH",
                        R.string.radar_show_watch_default_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "WPC MPDs",
                        "RADAR_SHOW_MPD",
                        R.string.radar_show_mpd_default_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Cities",
                        "COD_CITIES_DEFAULT",
                        R.string.cod_cities_default_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Highways",
                        "COD_HW_DEFAULT",
                        R.string.cod_hw_default_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Secondary roads",
                        "RADAR_HW_ENH_EXT",
                        R.string.hw_enh_ext_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Canadian and Mexican borders",
                        "RADAR_CAMX_BORDERS",
                        R.string.camx_borders_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Lakes and rivers",
                        "COD_LAKES_DEFAULT",
                        R.string.cod_lakes_default_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Spotters",
                        "WXOGL_SPOTTERS",
                        R.string.spotters_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Spotter labels",
                        "WXOGL_SPOTTERS_LABEL",
                        R.string.spotters_label_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Observations",
                        "WXOGL_OBS",
                        R.string.obs_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Wind barbs",
                        "WXOGL_OBS_WINDBARBS",
                        R.string.obs_windbarbs_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Location marker",
                        "COD_LOCDOT_DEFAULT",
                        R.string.cod_locdot_default_label
                ).card
        )
        val gpsSw = ObjectSettingsCheckBox(
                this,
                "Location marker follows GPS",
                "LOCDOT_FOLLOWS_GPS",
                R.string.locdot_follows_gps_label
        )
        linearLayout.addView(gpsSw.card)
        gpsSw.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, _ ->
            MyApplication.locationDotFollowsGps = compoundButton.isChecked
            if (MyApplication.locationDotFollowsGps != Utility.readPref(
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
        })
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Center radar on location",
                        "RADAR_CENTER_ON_LOCATION",
                        R.string.radar_center_on_location_default_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Location Heading Bug",
                        "LOCDOT_BUG", R.string.locdot_bug_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Black background",
                        "NWS_RADAR_BG_BLACK",
                        R.string.nws_black_bg_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Conus Radar",
                        "CONUS_RADAR",
                        R.string.conus_radar_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Counties",
                        "RADAR_SHOW_COUNTY",
                        R.string.show_county_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "County labels",
                        "RADAR_COUNTY_LABELS",
                        R.string.show_county_labels_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Counties use high resolution data",
                        "RADAR_COUNTY_HIRES",
                        R.string.county_hires_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "States use high resolution data",
                        "RADAR_STATE_HIRES",
                        R.string.state_hires_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Storm tracks",
                        "RADAR_SHOW_STI",
                        R.string.show_sti_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Show Hail index",
                        "RADAR_SHOW_HI",
                        R.string.show_hi_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                this,
                "Hail labels",
                "WXOGL_HAIL_LABEL",
                R.string.show_hi_label_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Tornado Vortex Signature",
                        "RADAR_SHOW_TVS",
                        R.string.show_tvs_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Convective Outlook Day One",
                        "RADAR_SHOW_SWO",
                        R.string.show_swo_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Screen stays on and auto refresh radar",
                        "RADAR_AUTOREFRESH",
                        R.string.autorefresh_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Multi-pane: share lat/lon/zoom",
                        "DUALPANE_SHARE_POSN",
                        R.string.dualpaneshareposn_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Remember location",
                        "WXOGL_REMEMBER_LOCATION",
                        R.string.rememberloc_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Launch app directly to radar",
                        "LAUNCH_TO_RADAR",
                        R.string.launch_to_radar_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Use JNI for radar (beta)",
                        "RADAR_USE_JNI",
                        R.string.radar_use_jni_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Enable multipurpose radar icons",
                        "WXOGL_ICONS_LEVEL2",
                        R.string.radar_icons_level2_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "WPC Fronts and pressure highs and lows",
                        "RADAR_SHOW_WPC_FRONTS",
                        R.string.radar_show_wpc_fronts_label
                ).card
        )
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Enable userpoints icons on radar",
                        "RADAR_USERPOINTS",
                        R.string.radar_userpoints
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Animation speed",
                        "ANIM_INTERVAL",
                        R.string.animation_interval_np_label,
                        MyApplication.animationIntervalDefault,
                        1,
                        15
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Default line size",
                        "RADAR_DEFAULT_LINESIZE",
                        R.string.default_linesize_np_label,
                        1,
                        1,
                        15
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Warning line size",
                        "RADAR_WARN_LINESIZE",
                        R.string.warn_linesize_np_label,
                        MyApplication.radarWarnLineSizeDefault,
                        1,
                        10
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "MCD/MPD/Watch line size",
                        "RADAR_WATMCD_LINESIZE",
                        R.string.watmcd_linesize_np,
                        MyApplication.radarWatchMcdLineSizeDefault,
                        1,
                        10
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "State line size",
                        "RADAR_STATE_LINESIZE",
                        R.string.state_linesize_np,
                        2,
                        1,
                        10
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "County line size",
                        "RADAR_COUNTY_LINESIZE",
                        R.string.county_linesize_np,
                        2,
                        1,
                        10
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Highway line size",
                        "RADAR_HW_LINESIZE",
                        R.string.hw_linesize_np,
                        2,
                        1,
                        10
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Secondary road line size",
                        "RADAR_HWEXT_LINESIZE",
                        R.string.hwext_linesize_np,
                        2,
                        1,
                        10
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Lake line size",
                        "RADAR_LAKE_LINESIZE",
                        R.string.lakes_linesize_np,
                        2,
                        1,
                        10
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "GPS Circle line size",
                        "RADAR_GPSCIRCLE_LINESIZE",
                        R.string.gpscircle_linesize_np,
                        5,
                        1,
                        10
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Storm tracks line size",
                        "RADAR_STI_LINESIZE",
                        R.string.sti_linesize_np,
                        3,
                        1,
                        10
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Wind barbs line size",
                        "RADAR_WB_LINESIZE",
                        R.string.wb_linesize_np,
                        3,
                        1,
                        10
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Convective outlook line size",
                        "RADAR_SWO_LINESIZE",
                        R.string.swo_linesize_np,
                        3,
                        1,
                        10
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Location marker size",
                        "RADAR_LOCDOT_SIZE",
                        R.string.locdot_size_np,
                        MyApplication.radarLocationDotSizeDefault,
                        1,
                        50
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Location icon size",
                        "RADAR_LOCICON_SIZE",
                        R.string.locicon_size_np,
                        75,
                        1,
                        530
                ).card
        )
	linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Location bug size",
                        "RADAR_LOCBUG_SIZE",
                        R.string.locbug_size_np,
                        75,
                        1,
                        530
                ).card
        )		
	linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Hail icon size",
                        "RADAR_HI_SIZE",
                        R.string.hi_size_np,
                        75,
                        1,
                        530
                ).card
        )
	linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Hail Text size",
                        "RADAR_HI_TEXT_SIZE",
                        R.string.hi_size_np,
                        (MyApplication.radarHiTextSize * 10).toInt(),
                        1,
                        20
                ).card
        )		
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "TVS icon size",
                        "RADAR_TVS_SIZE",
                        R.string.tvs_size_np,
                        75,
                        1,
                        530
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Userpoints icon size",
                        "RADAR_USERPOINT_SIZE",
                        R.string.userpoints_size_np,
                        75,
                        1,
                        530
                ).card
        )	
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Conus Radar Zoom",
                        "CONUS_RADAR_ZOOM",
                        R.string.conus_radar_zoom_label,
                        75,
                        0,
                        530
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "WXOGL initial view size",
                        "WXOGL_SIZE",
                        R.string.wxogl_size_np,
                        MyApplication.wxoglSizeDefault,
                        5,
                        25
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Refresh interval",
                        "RADAR_REFRESH_INTERVAL",
                        R.string.wxogl_refresh_interval_label,
                        3,
                        1,
                        15
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Storm spotter size",
                        "RADAR_SPOTTER_SIZE",
                        R.string.spotter_size_label,
                        MyApplication.radarSpotterSizeDefault,
                        1,
                        50
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Aviation dot size",
                        "RADAR_AVIATION_SIZE",
                        R.string.aviation_size_label,
                        MyApplication.radarAviationSizeDefault,
                        1,
                        50
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Text size",
                        "RADAR_TEXT_SIZE",
                        R.string.text_size_label,
                        (MyApplication.radarTextSizeDefault * 10).toInt(),
                        4,
                        40
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Draw tool line size",
                        "DRAWTOOL_SIZE",
                        R.string.drawtool_size_label,
                        4,
                        1,
                        20
                ).card
        )

        linearLayout.addView(ObjectSettingsSeekBar(this, "Color Legend width", "RADAR_SHOW_LEGEND_WIDTH", R.string.showlegendwidth, 50, 1, 100).card)
        linearLayout.addView(ObjectSettingsSeekBar(this, "Color Legend text size", "RADAR_SHOW_LEGEND_TEXTSIZE", R.string.showlegendtextsize, 30, 1, 100).card)

        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "Detailed observations Zoom",
                        "RADAR_OBS_EXT_ZOOM",
                        R.string.obs_ext_zoom_label,
                        7,
                        1,
                        10
                ).card
        )
        linearLayout.addView(
                ObjectSettingsSeekBar(
                        this,
                        "GPS update interval",
                        "RADAR_LOCATION_UPDATE_INTERVAL",
                        R.string.gps_update_interval_label,
                        10,
                        1,
                        60
                ).card
        )
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
        GeographyType.refresh()
        PolygonType.refresh()
        MyApplication.initGenericRadarWarnings(this)
        super.onPause()
    }

    private fun showGPSPermsDialogue() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), myPermissionsAccessFineLocation)
                }
            }
        }
    }

    private val myPermissionsAccessFineLocation = 5001

    // FIXME decide what todo with this
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            myPermissionsAccessFineLocation -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
    }
}

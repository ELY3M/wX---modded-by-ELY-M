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

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout

import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.telecine.SettingsTelecineActivity
import joshuatee.wx.util.Utility

class SettingsRadarActivity : BaseActivity() {

    private lateinit var cardPal94: ObjectCardText
    private lateinit var cardPal99: ObjectCardText

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        toolbar.subtitle = "Please tap on text for additional help."
        val ll: LinearLayout = findViewById(R.id.ll)
        ObjectCardText(
                this,
                ll,
                "Colors",
                MyApplication.textSizeNormal,
                SettingsColorsActivity::class.java,
                MyApplication.paddingSettings
        )
        cardPal94 = ObjectCardText(
                this,
                ll,
                resources.getString(R.string.label_settings_color_palette_94) + ": " + MyApplication.radarColorPalette["94"],
                MyApplication.textSizeNormal,
                MyApplication.paddingSettings
        )
        cardPal99 = ObjectCardText(
                this,
                ll,
                resources.getString(R.string.label_settings_color_palette_99) + ": " + MyApplication.radarColorPalette["99"],
                MyApplication.textSizeNormal,
                MyApplication.paddingSettings
        )
        ObjectCardText(
                this,
                ll,
                "Screen Recorder",
                MyApplication.textSizeNormal,
                SettingsTelecineActivity::class.java,
                MyApplication.paddingSettings
        )
        cardPal94.setOnClickListener(View.OnClickListener {
            ObjectIntent(
                    this,
                    SettingsColorPaletteActivity::class.java,
                    SettingsColorPaletteActivity.TYPE,
                    arrayOf("94")
            )
        })
        cardPal99.setOnClickListener(View.OnClickListener {
            ObjectIntent(
                    this,
                    SettingsColorPaletteActivity::class.java,
                    SettingsColorPaletteActivity.TYPE,
                    arrayOf("99")
            )
        })

        ll.addView(ObjectSettingsCheckBox(this, this, "Show TOR warnings", "TOR_WARNINGS", R.string.tor_warnings_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Show SVR warnings", "SVR_WARNINGS", R.string.svr_warnings_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Show EWW warnings", "EWW_WARNINGS", R.string.eww_warnings_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Show FFW warnings", "FFW_WARNINGS", R.string.ffw_warnings_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Show SMW warnings", "SMW_WARNINGS", R.string.smw_warnings_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Show SVS warnings", "SVS_WARNINGS", R.string.svs_warnings_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Show SPS warnings", "SPS_WARNINGS", R.string.sps_warnings_label).card)
        
	
	
	
	
	
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Show WAT/MCD",
                        "RADAR_SHOW_WATCH",
                        R.string.radar_show_watch_default_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Show MPD",
                        "RADAR_SHOW_MPD",
                        R.string.radar_show_mpd_default_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Show cities",
                        "COD_CITIES_DEFAULT",
                        R.string.cod_cities_default_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Show highways",
                        "COD_HW_DEFAULT",
                        R.string.cod_hw_default_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Hw data, show secondary roads",
                        "RADAR_HW_ENH_EXT",
                        R.string.hw_enh_ext_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Show CA/MX borders",
                        "RADAR_CAMX_BORDERS",
                        R.string.camx_borders_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Show lakes and rivers",
                        "COD_LAKES_DEFAULT",
                        R.string.cod_lakes_default_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Show spotters",
                        "WXOGL_SPOTTERS",
                        R.string.spotters_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Show spotter labels",
                        "WXOGL_SPOTTERS_LABEL",
                        R.string.spotters_label_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Show observations",
                        "WXOGL_OBS",
                        R.string.obs_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Show wind barbs",
                        "WXOGL_OBS_WINDBARBS",
                        R.string.obs_windbarbs_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Show location marker",
                        "COD_LOCDOT_DEFAULT",
                        R.string.cod_locdot_default_label
                ).card
        )
        val gpsSw = ObjectSettingsCheckBox(
                this,
                this,
                "Location marker follows GPS",
                "LOCDOT_FOLLOWS_GPS",
                R.string.locdot_follows_gps_label
        )
        ll.addView(gpsSw.card)
        gpsSw.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { compoundButton, _ ->
            MyApplication.locdotFollowsGps = compoundButton.isChecked
            if (MyApplication.locdotFollowsGps != Utility.readPref(
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
	/*
        ll.addView(ObjectSettingsCheckBox(this, this, "Location Heading Bug", "LOCDOT_BUG", R.string.locdot_bug_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Black background", "NWS_RADAR_BG_BLACK", R.string.nws_black_bg_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Show Conus Radar", "CONUS_RADAR", R.string.conus_radar_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Show counties", "RADAR_SHOW_COUNTY", R.string.show_county_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Show county labels", "RADAR_COUNTY_LABELS", R.string.show_county_labels_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Counties - use hires data", "RADAR_COUNTY_HIRES", R.string.county_hires_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "States - use hires data", "RADAR_STATE_HIRES", R.string.state_hires_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Show storm tracks", "RADAR_SHOW_STI", R.string.show_sti_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Show Hail index", "RADAR_SHOW_HI", R.string.show_hi_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Show Hail labels", "WXOGL_HAIL_LABEL", R.string.show_hi_label_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Show TVS", "RADAR_SHOW_TVS", R.string.show_tvs_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Show Day 1 Conv Otlk", "RADAR_SHOW_SWO", R.string.show_swo_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Screen on, auto refresh", "RADAR_AUTOREFRESH", R.string.autorefresh_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Multi-pane: share lat/lon/zoom", "DUALPANE_SHARE_POSN", R.string.dualpaneshareposn_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Remember location", "WXOGL_REMEMBER_LOCATION", R.string.rememberloc_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Launch app directly to radar", "LAUNCH_TO_RADAR", R.string.launch_to_radar_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Use JNI for radar (beta)", "RADAR_USE_JNI", R.string.radar_use_jni_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Enable multipurpose radar icons", "WXOGL_ICONS_LEVEL2", R.string.radar_icons_level2_label).card)
        ll.addView(ObjectSettingsCheckBox(this, this, "Enable userpoints icons on radar", "RADAR_USERPOINTS", R.string.radar_userpoints).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Animation interval", "ANIM_INTERVAL", R.string.anim_np_label, 15, 1, 15).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Default line size", "RADAR_DEFAULT_LINESIZE", R.string.default_linesize_np_label, 1, 1, 10).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Warning line size", "RADAR_WARN_LINESIZE", R.string.warn_linesize_np_label, 4, 1, 10).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Watch/MCD line size", "RADAR_WATMCD_LINESIZE", R.string.watmcd_linesize_np, 4, 1, 10).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Location dot size", "RADAR_LOCDOT_SIZE", R.string.locdot_size_np, 10, 1, 75).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Location icon size", "RADAR_LOCICON_SIZE", R.string.locicon_size_np, 75, 1, 530).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Location Bug size", "RADAR_LOCBUG_SIZE", R.string.locbug_size_np, 75, 1, 530).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Userpoints icon size", "RADAR_USERPOINT_SIZE", R.string.userpoints_size_np, 75, 1, 530).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Hail marker size", "RADAR_HI_SIZE", R.string.hi_size_np, 75, 1, 530).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Hail Text size", "RADAR_HI_TEXT_SIZE", R.string.hi_textsize_np, (MyApplication.radarTextSize * 10).toInt(), 1, 20).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "TVS icon size", "RADAR_TVS_SIZE", R.string.tvs_size_np, 75, 1, 530).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "WXOGL initial view size", "WXOGL_SIZE", R.string.wxogl_size_np, 8, 5, 25).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Refresh interval", "RADAR_REFRESH_INTERVAL", R.string.wxogl_refresh_interval_label, 3, 1, 15).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Storm spotter size", "RADAR_SPOTTER_SIZE", R.string.spotter_size_label, 4, 1, 50).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Aviation dot size", "RADAR_AVIATION_SIZE", R.string.aviation_size_label, 7, 1, 50).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Text size", "RADAR_TEXT_SIZE", R.string.text_size_label, (MyApplication.radarTextSize * 10).toInt(), 4, 20).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Draw tool line size", "DRAWTOOL_SIZE", R.string.drawtool_size_label, 4, 1, 20).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Color Legend width", "RADAR_SHOW_LEGEND_WIDTH", R.string.showlegendwidth, 50, 1, 100).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Color Legend text size", "RADAR_SHOW_LEGEND_TEXTSIZE", R.string.showlegendtextsize, 30, 1, 100).card)
        ll.addView(ObjectSettingsNumberPicker(this, this, "Detailed observations Zoom", "RADAR_OBS_EXT_ZOOM", R.string.obs_ext_zoom_label, 7, 1, 10).card)
	*/

        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Location Heading Bug",
                        "LOCDOT_BUG", R.string.locdot_bug_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Black background",
                        "NWS_RADAR_BG_BLACK",
                        R.string.nws_black_bg_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Show Conus Radar",
                        "CONUS_RADAR",
                        R.string.conus_radar_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Show counties",
                        "RADAR_SHOW_COUNTY",
                        R.string.show_county_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Show county labels",
                        "RADAR_COUNTY_LABELS",
                        R.string.show_county_labels_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Counties - use hires data",
                        "RADAR_COUNTY_HIRES",
                        R.string.county_hires_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "States - use hires data",
                        "RADAR_STATE_HIRES",
                        R.string.state_hires_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Show storm tracks",
                        "RADAR_SHOW_STI",
                        R.string.show_sti_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Show Hail index",
                        "RADAR_SHOW_HI",
                        R.string.show_hi_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                this,
                this,
                "Show Hail labels",
                "WXOGL_HAIL_LABEL",
                R.string.show_hi_label_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Show TVS",
                        "RADAR_SHOW_TVS",
                        R.string.show_tvs_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Show Day 1 Conv Otlk",
                        "RADAR_SHOW_SWO",
                        R.string.show_swo_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Screen on, auto refresh",
                        "RADAR_AUTOREFRESH",
                        R.string.autorefresh_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Multi-pane: share lat/lon/zoom",
                        "DUALPANE_SHARE_POSN",
                        R.string.dualpaneshareposn_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Remember location",
                        "WXOGL_REMEMBER_LOCATION",
                        R.string.rememberloc_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Launch app directly to radar",
                        "LAUNCH_TO_RADAR",
                        R.string.launch_to_radar_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Use JNI for radar (beta)",
                        "RADAR_USE_JNI",
                        R.string.radar_use_jni_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Enable multipurpose radar icons",
                        "WXOGL_ICONS_LEVEL2",
                        R.string.radar_icons_level2_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Enable userpoints icons on radar",
                        "RADAR_USERPOINTS",
                        R.string.radar_userpoints
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Animation interval",
                        "ANIM_INTERVAL",
                        R.string.anim_np_label,
                        15,
                        1,
                        15
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Default line size",
                        "RADAR_DEFAULT_LINESIZE",
                        R.string.default_linesize_np_label,
                        1,
                        1,
                        15
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Warning line size",
                        "RADAR_WARN_LINESIZE",
                        R.string.warn_linesize_np_label,
                        4,
                        1,
                        10
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Watch/MCD line size",
                        "RADAR_WATMCD_LINESIZE",
                        R.string.watmcd_linesize_np,
                        4,
                        1,
                        10
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "State line size",
                        "RADAR_STATE_LINESIZE",
                        R.string.state_linesize_np,
                        2,
                        1,
                        10
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "County line size",
                        "RADAR_COUNTY_LINESIZE",
                        R.string.county_linesize_np,
                        2,
                        1,
                        10
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "HW line size",
                        "RADAR_HW_LINESIZE",
                        R.string.hw_linesize_np,
                        2,
                        1,
                        10
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "HW EXT line size",
                        "RADAR_HWEXT_LINESIZE",
                        R.string.hwext_linesize_np,
                        2,
                        1,
                        10
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Lake line size",
                        "RADAR_LAKE_LINESIZE",
                        R.string.lakes_linesize_np,
                        2,
                        1,
                        10
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "GPS Circle line size",
                        "RADAR_GPSCIRCLE_LINESIZE",
                        R.string.gpscircle_linesize_np,
                        5,
                        1,
                        10
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Location marker size",
                        "RADAR_LOCDOT_SIZE",
                        R.string.locdot_size_np,
                        8,
                        1,
                        50
                ).card
        )


        ll.addView(ObjectSettingsSeekbar(this, this, "Location dot size", "RADAR_LOCDOT_SIZE", R.string.locdot_size_np, 10, 1, 75).card)
        ll.addView(ObjectSettingsSeekbar(this, this, "Location icon size", "RADAR_LOCICON_SIZE", R.string.locicon_size_np, 75, 1, 530).card)
        ll.addView(ObjectSettingsSeekbar(this, this, "Location Bug size", "RADAR_LOCBUG_SIZE", R.string.locbug_size_np, 75, 1, 530).card)
        ll.addView(ObjectSettingsSeekbar(this, this, "Userpoints icon size", "RADAR_USERPOINT_SIZE", R.string.userpoints_size_np, 75, 1, 530).card)
        ll.addView(ObjectSettingsSeekbar(this, this, "Hail marker size", "RADAR_HI_SIZE", R.string.hi_size_np, 75, 1, 530).card)
        ll.addView(ObjectSettingsSeekbar(this, this, "Hail Text size", "RADAR_HI_TEXT_SIZE", R.string.hi_textsize_np, (MyApplication.radarHiTextSize * 10).toInt(), 1, 20).card)
        ll.addView(ObjectSettingsSeekbar(this, this, "TVS icon size", "RADAR_TVS_SIZE", R.string.tvs_size_np, 75, 1, 530).card)

        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "WXOGL initial view size",
                        "WXOGL_SIZE",
                        R.string.wxogl_size_np,
                        8,
                        5,
                        25
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Refresh interval",
                        "RADAR_REFRESH_INTERVAL",
                        R.string.wxogl_refresh_interval_label,
                        3,
                        1,
                        15
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Storm spotter size",
                        "RADAR_SPOTTER_SIZE",
                        R.string.spotter_size_label,
                        4,
                        1,
                        50
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Aviation dot size",
                        "RADAR_AVIATION_SIZE",
                        R.string.aviation_size_label,
                        7,
                        1,
                        50
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Text size",
                        "RADAR_TEXT_SIZE",
                        R.string.text_size_label,
                        (MyApplication.radarTextSize * 10).toInt(),
                        4,
                        20
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Draw tool line size",
                        "DRAWTOOL_SIZE",
                        R.string.drawtool_size_label,
                        4,
                        1,
                        20
                ).card
        )

        ll.addView(ObjectSettingsSeekbar(this, this, "Color Legend width", "RADAR_SHOW_LEGEND_WIDTH", R.string.showlegendwidth, 50, 1, 100).card)
        ll.addView(ObjectSettingsSeekbar(this, this, "Color Legend text size", "RADAR_SHOW_LEGEND_TEXTSIZE", R.string.showlegendtextsize, 30, 1, 100).card)

        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Detailed observations Zoom",
                        "RADAR_OBS_EXT_ZOOM",
                        R.string.obs_ext_zoom_label,
                        7,
                        1,
                        10
                ).card
        )
    }

    override fun onStop() {
        super.onStop()
        Utility.commitPref(this)
        MyApplication.initPreferences(this)
        val restartNotif = Utility.readPref(this, "RESTART_NOTIF", "false")
        if (restartNotif == "true") {
            UtilityWXJobService.startService(this)
            Utility.writePref(this, "RESTART_NOTIF", "false")
        }
        GeographyType.refresh()
        PolygonType.refresh()
    }

    private fun showGPSPermsDialogue() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
            } else {
                if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            myPermissionsAccessFineLocation
                    )
                }
            }
        }
    }

    private val myPermissionsAccessFineLocation = 5001

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            myPermissionsAccessFineLocation -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
    }

    override fun onRestart() {
        cardPal94.setText(resources.getString(R.string.label_settings_color_palette_94) + ": " + MyApplication.radarColorPalette["94"])
        cardPal99.setText(resources.getString(R.string.label_settings_color_palette_99) + ": " + MyApplication.radarColorPalette["99"])
        super.onRestart()
    }
}

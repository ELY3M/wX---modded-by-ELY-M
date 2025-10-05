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

package joshuatee.wx.ui

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.view.Gravity
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import joshuatee.wx.MyApplication
import joshuatee.wx.radar.RadarGeometry
import joshuatee.wx.settings.NotificationPreferences
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.settings.UtilityNavDrawer
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog

class Switch(context: Activity, label: String, pref: String, strId: Int) : Widget {

    private val card = Card(context)
    private val checkBox = CheckBox(context)

    init {
        val text = Text(context)
        with(text) {
            wrap()
            setPadding(UIPreferences.paddingSettings)
            text.text = label
            val strInner = context.resources.getString(strId)
            connect { ObjectDialogue(context, strInner) }
        }
        val hbox = HBox.centered(context)
        hbox.matchParent()
        hbox.addWidget(text)
        checkBox.gravity = Gravity.CENTER_VERTICAL
        val truePrefs = listOf(
            "COD_HW_DEFAULT",
            "RADAR_SHOW_COUNTY",
            "DUALPANE_SHARE_POSN",
            "UNITS_F",
            "UNITS_M",
            "FAB_IN_MODELS",
            "NWS_TEXT_REMOVELINEBREAKS",
            "RECORD_SCREEN_SHARE",
            "COD_LOCDOT_DEFAULT",
            "UI_MAIN_SCREEN_RADAR_FAB",
            "NAV_DRAWER_MAIN_SCREEN_ON_RIGHT",
            "USE_NWS_API_SEVEN_DAY",
            "USE_NWS_API_HOURLY",
            "LIGHTNING_USE_GOES",
            "SHOW_RADAR_WHEN_PAN",
            "ALERT_BLACKOUT_TORNADO",
            "LOCK_TOOLBARS",
            "ALERT_ONLYONCE",
            "ALERT_AUTOCANCEL",
            "HOURLY_SHOW_GRAPH"
        )
        checkBox.isChecked = Utility.readPref(
            context,
            pref,
            java.lang.Boolean.toString(truePrefs.contains(pref))
        ) == "true"
                || (pref.startsWith(UtilityNavDrawer.getPrefVar("")) && Utility.readPref(
            context,
            pref,
            ""
        ) != "false")
        checkBox.setOnCheckedChangeListener { compoundButton, _ ->
            if (compoundButton.isChecked) {
                //
                // if enabled, check for notification
                //
                if (pref.contains("NOTIFICATION")) {
                    UtilityLog.d("WXRADAR", "notif turned on")
                }
                Utility.writePref(context, pref, "true")
            } else {
                if (pref == "MEDIA_CONTROL_NOTIF") {
                    if (Utility.readPref(context, "MEDIA_CONTROL_NOTIF", "").startsWith("t")) {
                        val notifier =
                            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notifier.cancel("wx_media", 1)
                    }
                }
                Utility.writePref(context, pref, "false")
            }
            if (pref == "SIMPLE_MODE") {
                if (UIPreferences.simpleMode != Utility.readPref(context, "SIMPLE_MODE", "false")
                        .startsWith("t")
                ) {
                    Utility.commitPref(context)
                    Utility.restart()
                }
            }
            if (pref == "HIDE_TOP_TOOLBAR") {
                if (UIPreferences.hideTopToolbar != Utility.readPref(
                        context,
                        "HIDE_TOP_TOOLBAR",
                        "false"
                    ).startsWith("t")
                ) {
                    Utility.commitPref(context)
                    Utility.restart()
                }
            }
            if (pref == "UI_MAIN_SCREEN_RADAR_FAB") {
                if (UIPreferences.mainScreenRadarFab != Utility.readPref(
                        context,
                        "UI_MAIN_SCREEN_RADAR_FAB",
                        "false"
                    ).startsWith("t")
                ) {
                    Utility.commitPref(context)
                    Utility.restart()
                }
            }
            if (pref == "NOTIF_TTS") {
                if (NotificationPreferences.notifTts != Utility.readPref(
                        context,
                        "NOTIF_TTS",
                        "false"
                    ).startsWith("t")
                ) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            context,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            5002
                        )
                    }
                }
            }
            when (pref) {
                "COD_WARNINGS_DEFAULT", "RADAR_SHOW_MPD", "RADAR_SHOW_WATCH" -> Utility.writePref(
                    context,
                    "RESTART_NOTIF",
                    "true"
                )

                "RADAR_STATE_HIRES", "RADAR_COUNTY_HIRES", "RADAR_HW_ENH_EXT", "RADAR_CAMX_BORDERS", "WXOGL_SPOTTERS", "WXOGL_SPOTTERS_LABEL" -> {
                    MyApplication.initPreferences(context)
                    when (pref) {
                        "RADAR_STATE_HIRES" -> RadarGeometry.initialize(context)
                        "RADAR_COUNTY_HIRES" -> RadarGeometry.initialize(context)
                        "RADAR_HW_ENH_EXT" -> RadarGeometry.initialize(context)
                        "RADAR_CAMX_BORDERS" -> RadarGeometry.initialize(context)
                    }
                }
            }
        }
        hbox.addWidget(checkBox)
        card.addLayout(hbox)
    }

    fun isChecked(value: Boolean) {
        checkBox.isChecked = value
    }

    var visibility
        get() = card.visibility
        set(value) {
            card.visibility = value
        }

    internal fun setOnCheckedChangeListener(listener: CompoundButton.OnCheckedChangeListener) {
        checkBox.setOnCheckedChangeListener(listener)
    }

    override fun getView() = card.getView()
}

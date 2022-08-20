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

package joshuatee.wx.ui

import android.app.NotificationManager
import android.content.Context
import android.view.Gravity
import android.widget.CheckBox
import android.widget.CompoundButton
import joshuatee.wx.MyApplication
import joshuatee.wx.notifications.UtilityNotification
import joshuatee.wx.radar.RadarGeometry
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.settings.UtilityNavDrawer
import joshuatee.wx.util.Utility

class ObjectSwitch(context: Context, label: String, pref: String, strId: Int) {

    private val card = Card(context)
    private val checkBox = CheckBox(context)

    init {
        val text = Text(context)
        text.wrap()
        text.setPadding(UIPreferences.paddingSettings)
        text.text = label
        val strInner = context.resources.getString(strId)
        text.connect { ObjectDialogue(context, strInner) }
        val hbox = HBox(context, Gravity.CENTER_VERTICAL)
        hbox.matchParent()
        hbox.addWidget(text.get())
        checkBox.gravity = Gravity.CENTER_VERTICAL
        val truePrefs = listOf(
                "COD_HW_DEFAULT",
                "DUALPANE_SHARE_POSN",
                "UNITS_F",
                "UNITS_M",
                "FAB_IN_MODELS",
                "NWS_TEXT_REMOVELINEBREAKS",
                "RECORD_SCREEN_SHARE",
                "PREF_PREVENT_ACCIDENTAL_EXIT",
                "COD_LOCDOT_DEFAULT",
                "UI_MAIN_SCREEN_RADAR_FAB",
                "RADAR_TOOLBAR_TRANSPARENT",
                "NAV_DRAWER_MAIN_SCREEN_ON_RIGHT",
                "USE_NWS_API_HOURLY",
                "LIGHTNING_USE_GOES",
                "USE_AWC_MOSAIC",
                "SHOW_RADAR_WHEN_PAN",
        )
        checkBox.isChecked = Utility.readPref(context, pref, java.lang.Boolean.toString(truePrefs.contains(pref))) == "true"
             || ( pref.startsWith(UtilityNavDrawer.getPrefVar("")) && Utility.readPref(context, pref, "") != "false")
        checkBox.setOnCheckedChangeListener { compoundButton, _ ->
            if (compoundButton.isChecked) {
                if (pref == "MEDIA_CONTROL_NOTIF") {
                    if (Utility.readPref(context, "MEDIA_CONTROL_NOTIF", "").startsWith("f")) {
                        UtilityNotification.createMediaControlNotification(context.applicationContext, "")
                    }
                }
                Utility.writePref(context, pref, "true")
            } else {
                if (pref == "MEDIA_CONTROL_NOTIF") {
                    if (Utility.readPref(context, "MEDIA_CONTROL_NOTIF", "").startsWith("t")) {
                        val notifier = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notifier.cancel("wx_media", 1)
                    }
                }
                Utility.writePref(context, pref, "false")
            }
            if (pref == "SIMPLE_MODE") {
                if (UIPreferences.simpleMode != Utility.readPref(context, "SIMPLE_MODE", "false").startsWith("t")) {
                    Utility.commitPref(context)
                    Utility.restart()
                }
            }
            if (pref == "HIDE_TOP_TOOLBAR") {
                if (UIPreferences.hideTopToolbar != Utility.readPref(context, "HIDE_TOP_TOOLBAR", "false").startsWith("t")) {
                    Utility.commitPref(context)
                    Utility.restart()
                }
            }
            if (pref == "UI_MAIN_SCREEN_RADAR_FAB") {
                if (UIPreferences.mainScreenRadarFab != Utility.readPref(context, "UI_MAIN_SCREEN_RADAR_FAB", "false").startsWith("t")) {
                    Utility.commitPref(context)
                    Utility.restart()
                }
            }
            when (pref) {
                "COD_WARNINGS_DEFAULT", "RADAR_SHOW_MPD", "RADAR_SHOW_WATCH" -> Utility.writePref(context, "RESTART_NOTIF", "true")
                "RADAR_STATE_HIRES", "RADAR_COUNTY_HIRES", "RADAR_HW_ENH_EXT", "RADAR_CAMX_BORDERS", "WXOGL_SPOTTERS", "WXOGL_SPOTTERS_LABEL" -> {
                    MyApplication.initPreferences(context)
                    // TODO FIXME more granular
                    when (pref) {
//                        "RADAR_STATE_HIRES" -> RadarGeometry.initialize(context, GeographyType.STATE_LINES)
//                        "RADAR_COUNTY_HIRES" -> RadarGeometry.initialize(context, GeographyType.COUNTY_LINES)
//                        "RADAR_HW_ENH_EXT" -> RadarGeometry.initialize(context, GeographyType.HIGHWAYS_EXTENDED)
//                        "RADAR_CAMX_BORDERS" -> RadarGeometry.initialize(context, GeographyType.STATE_LINES)
                        "RADAR_STATE_HIRES" -> RadarGeometry.initialize(context)
                        "RADAR_COUNTY_HIRES" -> RadarGeometry.initialize(context)
                        "RADAR_HW_ENH_EXT" -> RadarGeometry.initialize(context)
                        "RADAR_CAMX_BORDERS" -> RadarGeometry.initialize(context)
                    }
//                    GeographyType.refresh()
                }
            }
        }
        hbox.addWidget(checkBox)
        card.addLayout(hbox)
    }

    fun isChecked(value: Boolean) {
        checkBox.isChecked = value
    }

    fun get() = card.get()

    internal fun setOnCheckedChangeListener(listener: CompoundButton.OnCheckedChangeListener) {
        checkBox.setOnCheckedChangeListener(listener)
    }
}

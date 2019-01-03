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
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import joshuatee.wx.R
import joshuatee.wx.audio.SettingsPlaylistActivity
import joshuatee.wx.MyApplication
import joshuatee.wx.activitiesmisc.TextScreenActivity
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.UtilityTheme
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.*

class SettingsMainActivity : BaseActivity() {

    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        UtilityTheme.setPrimaryColor(this)
        contextg = this
        var backuprestore = UtilityBackupRestore()
        var vers = ""
        try {
            vers = packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        toolbar.subtitle = "version: $vers, Please tap on text for additional help."
        val linearLayout: LinearLayout = findViewById(R.id.ll)
        val cardAbout = ObjectCardText(this, "About wX", MyApplication.textSizeNormal)
        val cardLocations = ObjectCardText(this, "Locations", MyApplication.textSizeNormal)
        val cardsn = ObjectCardText(this, "Spotter Network Settings", MyApplication.textSizeNormal)
        val cardNotif = ObjectCardText(this, "Notifications", MyApplication.textSizeNormal)
        val cardWidgets = ObjectCardText(this, "Widgets", MyApplication.textSizeNormal)
        val cardColors = ObjectCardText(this, "Colors", MyApplication.textSizeNormal)
        val cardPL = ObjectCardText(this, "PlayList", MyApplication.textSizeNormal)
        val cardRadar = ObjectCardText(this, "Radar", MyApplication.textSizeNormal)
        val cardHS = ObjectCardText(this, "Home Screen", MyApplication.textSizeNormal)
        val cardUI = ObjectCardText(this, "User Interface", MyApplication.textSizeNormal)
        val cardCtoF = ObjectCardText(this, "Celsius to fahrenheit table", MyApplication.textSizeNormal)
        val cardDeleteFiles = ObjectCardText(this, "Delete old radar files", MyApplication.textSizeNormal)
        val cardbackuppref = ObjectCardText(this, "Backup Settings", MyApplication.textSizeNormal)
        val cardrestorepref = ObjectCardText(this, "Restore Settings", MyApplication.textSizeNormal)
        cardLocations.setOnClickListener(View.OnClickListener { ObjectIntent(contextg, SettingsLocationRecyclerViewActivity::class.java) })
        cardsn.setOnClickListener(View.OnClickListener { ObjectIntent(contextg, SettingsSpotterNetwork::class.java) })
        cardNotif.setOnClickListener(View.OnClickListener {
            ObjectIntent(
                contextg,
                SettingsNotificationsActivity::class.java
            )
        })
        cardWidgets.setOnClickListener(View.OnClickListener {
            ObjectIntent(
                contextg,
                SettingsWidgetsActivity::class.java
            )
        })
        cardColors.setOnClickListener(View.OnClickListener {
            ObjectIntent(
                contextg,
                SettingsColorsActivity::class.java
            )
        })
        cardPL.setOnClickListener(View.OnClickListener {
            ObjectIntent(
                contextg,
                SettingsPlaylistActivity::class.java
            )
        })
        cardRadar.setOnClickListener(View.OnClickListener {
            ObjectIntent(
                contextg,
                SettingsRadarActivity::class.java
            )
        })
        cardHS.setOnClickListener(View.OnClickListener {
            ObjectIntent(
                contextg,
                SettingsHomeScreenActivity::class.java
            )
        })
        cardUI.setOnClickListener(View.OnClickListener {
            ObjectIntent(
                contextg,
                SettingsUIActivity::class.java
            )
        })
        cardCtoF.setOnClickListener(View.OnClickListener {
            ObjectIntent(
                contextg,
                TextScreenActivity::class.java,
                TextScreenActivity.URL,
                arrayOf(UtilityMath.cToFTable(), "Celsius to Fahrenheit table")
            )
        })

        cardbackuppref.setOnClickListener(View.OnClickListener { backuprestore.backupPrefs(contextg) })
        cardrestorepref.setOnClickListener(View.OnClickListener { backuprestore.restorePrefs(contextg) })
        cardDeleteFiles.setOnClickListener(View.OnClickListener {
            UtilityUI.makeSnackBar(
                linearLayout,
                "Deleted old radar files: " + UtilityFileManagement.deleteCacheFiles(contextg)
            )
        })
        cardAbout.setOnClickListener(View.OnClickListener {
            ObjectIntent(
                contextg,
                TextScreenActivity::class.java,
                TextScreenActivity.URL,
                arrayOf(UtilityAlertDialog.showVersion(this, this), "About wX")
            )
        })
        linearLayout.addView(cardAbout.card)
        linearLayout.addView(cardLocations.card)
        linearLayout.addView(cardsn.card)
        linearLayout.addView(cardNotif.card)
        linearLayout.addView(cardWidgets.card)
        linearLayout.addView(cardColors.card)
        linearLayout.addView(cardPL.card)
        linearLayout.addView(cardRadar.card)
        linearLayout.addView(cardHS.card)
        linearLayout.addView(cardUI.card)
        linearLayout.addView(cardCtoF.card)
        linearLayout.addView(cardDeleteFiles.card)
        linearLayout.addView(cardbackuppref.card)
        linearLayout.addView(cardrestorepref.card)
        linearLayout.addView(ObjectSettingsCheckBox(this, this, "Check for SPC MCD/W", "CHECKSPC", R.string.checkspc_switch_label).card)
        linearLayout.addView(ObjectSettingsCheckBox(this, this, "Check for WPC MPD", "CHECKWPC", R.string.checkwpc_switch_label).card)
        linearLayout.addView(ObjectSettingsCheckBox(this, this, "Check for TOR, SVR, EWW, FFW, SVS, SMW, SPS", "CHECKTOR", R.string.checktor_switch_label).card)
        linearLayout.addView(ObjectSettingsCheckBox(this, this, "Media control notif", "MEDIA_CONTROL_NOTIF", R.string.media_control_notif_tv).card)
        linearLayout.addView(ObjectSettingsCheckBox(this, this, "Dual-pane radar from main screen", "DUALPANE_RADAR_ICON", R.string.dualpane_radar_icon_tv).card)
        linearLayout.addView(ObjectSettingsCheckBox(this, this, "Translate abbreviations", "TRANSLATE_TEXT", R.string.translate_text_label).card)
        linearLayout.addView(ObjectSettingsNumberPicker(this, this, "Refresh interval for location", "REFRESH_LOC_MIN", R.string.refresh_loc_min_np_label, 30, 0, 120).card)
        linearLayout.addView(ObjectSettingsNumberPicker(this, this, "ROAMING distance check", "ROAMING_LOCATION_DISTANCE_CHECK", R.string.roaming_location_distance_check_np_label, 5, 1, 120).card)
        linearLayout.addView(ObjectSettingsNumberPicker(this, this, "Text to speech speed", "TTS_SPEED_PREF", R.string.tts_speed_np_label, 10, 1, 20).card)
    }

    override fun onStop() {
        super.onStop()
        MyApplication.initPreferences(this)
        val restartNotif = Utility.readPref(this, "RESTART_NOTIF", "false")
        if (restartNotif == "true") {
            UtilityWXJobService.startService(this)
            Utility.writePref(this, "RESTART_NOTIF", "false")
    }
}
}

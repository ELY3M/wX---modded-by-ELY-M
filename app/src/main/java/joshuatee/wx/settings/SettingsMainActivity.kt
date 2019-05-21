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

import kotlinx.android.synthetic.main.activity_linear_layout.*

class SettingsMainActivity : BaseActivity() {

    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        UtilityTheme.setPrimaryColor(this)
        contextg = this
        val backuprestore = UtilityBackupRestore()
        val version = Utility.getVersion(this)
        toolbar.subtitle = "v$version, tap on text for additional help."
        val cardAbout = ObjectCardText(this, "About wX", MyApplication.textSizeNormal, MyApplication.paddingSettings)
        val cardLocations = ObjectCardText(
                this,
                "Locations",
                MyApplication.textSizeNormal,
                SettingsLocationRecyclerViewActivity::class.java,
	    	MyApplication.paddingSettings
        )
        val cardsn = ObjectCardText(
	this, 
	"Spotter Network Settings", 
	MyApplication.textSizeNormal,
	SettingsSpotterNetwork::class.java,
                MyApplication.paddingSettings
        )
        val cardNotif = ObjectCardText(
                this,
                "Notifications",
                MyApplication.textSizeNormal,
                SettingsNotificationsActivity::class.java,
                MyApplication.paddingSettings
        )
        val cardWidgets = ObjectCardText(
                this,
                "Widgets",
                MyApplication.textSizeNormal,
                SettingsWidgetsActivity::class.java,
                MyApplication.paddingSettings
        )
        val cardColors = ObjectCardText(
                this,
                "Colors",
                MyApplication.textSizeNormal,
                SettingsColorsActivity::class.java,
                MyApplication.paddingSettings
        )
        val cardPL = ObjectCardText(
                this,
                "PlayList",
                MyApplication.textSizeNormal,
                SettingsPlaylistActivity::class.java,
                MyApplication.paddingSettings
        )
        val cardRadar = ObjectCardText(
                this,
                "Radar",
                MyApplication.textSizeNormal,
                SettingsRadarActivity::class.java,
                MyApplication.paddingSettings
        )
        val cardHS = ObjectCardText(
                this,
                "Home Screen",
                MyApplication.textSizeNormal,
                SettingsHomeScreenActivity::class.java,
                MyApplication.paddingSettings
        )
        val cardUI = ObjectCardText(
                this,
                "User Interface",
                MyApplication.textSizeNormal,
                SettingsUIActivity::class.java,
                MyApplication.paddingSettings
        )
        val cardCtoF =
                ObjectCardText(this, "Celsius to fahrenheit table", MyApplication.textSizeNormal, MyApplication.paddingSettings)
        val cardDeleteFiles =
                ObjectCardText(this, "Delete old radar files", MyApplication.textSizeNormal, MyApplication.paddingSettings)
        val cardbackuppref = ObjectCardText(this, "Backup Settings", MyApplication.textSizeNormal, MyApplication.paddingSettings)
        val cardrestorepref = ObjectCardText(this, "Restore Settings", MyApplication.textSizeNormal, MyApplication.paddingSettings)
        cardCtoF.setOnClickListener(View.OnClickListener {
            ObjectIntent(
                    contextg,
                    TextScreenActivity::class.java,
                    TextScreenActivity.URL,
                    arrayOf(UtilityMath.celsiusToFahrenheitTable(), "Celsius to Fahrenheit table")
            )
        })
        cardbackuppref.setOnClickListener(View.OnClickListener { backuprestore.backupPrefs(contextg) })
        cardrestorepref.setOnClickListener(View.OnClickListener { backuprestore.restorePrefs(contextg) })
        cardDeleteFiles.setOnClickListener(View.OnClickListener {
            UtilityUI.makeSnackBar(
                    ll,
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
        listOf(
                cardAbout.card,
                cardLocations.card,
		cardsn.card,
                cardNotif.card,
                cardWidgets.card,
                cardColors.card,
                cardPL.card,
                cardRadar.card,
                cardHS.card,
                cardUI.card,
                cardCtoF.card,
                cardDeleteFiles.card,
		cardbackuppref.card,
		cardrestorepref.card
        ).forEach {
            ll.addView(it)
        }
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Check for SPC MCD/Watches",
                        "CHECKSPC",
                        R.string.checkspc_switch_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Check for WPC MPDs",
                        "CHECKWPC",
                        R.string.checkwpc_switch_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                "Check for TOR, SVR, EWW, FFW, SVS, SMW, SPS",
                        "CHECKTOR",
                        R.string.checktor_switch_label
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Media control notification",
                        "MEDIA_CONTROL_NOTIF",
                        R.string.media_control_notif_tv
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Dual-pane radar from main screen",
                        "DUALPANE_RADAR_ICON",
                        R.string.dualpane_radar_icon_tv
                ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Translate abbreviations",
                        "TRANSLATE_TEXT",
                        R.string.translate_text_label
            ).card
        )
        ll.addView(
                ObjectSettingsCheckBox(
                        this,
                        this,
                        "Check for Internet on startup",
                        "CHECKINTERNET",
                        R.string.checkinternet_switch_label
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Refresh interval for location",
                        "REFRESH_LOC_MIN",
                        R.string.refresh_loc_min_np_label,
                        10,
                        0,
                        120
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "ROAMING distance check",
                        "ROAMING_LOCATION_DISTANCE_CHECK",
                        R.string.roaming_location_distance_check_np_label,
                        5,
                        1,
                        120
                ).card
        )
        ll.addView(
                ObjectSettingsSeekbar(
                        this,
                        this,
                        "Text to speech speed",
                        "TTS_SPEED_PREF",
                        R.string.tts_speed_np_label,
                        10,
                        1,
                        20
                ).card
        )
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

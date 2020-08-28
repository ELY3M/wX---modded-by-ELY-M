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

import android.annotation.SuppressLint
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

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        UtilityTheme.setPrimaryColor(this)
        val backuprestore = UtilityBackupRestore()
        val version = Utility.getVersion(this)
        val textSize = MyApplication.textSizeLarge
        val padding = MyApplication.paddingSettings
        toolbar.subtitle = "$version, tap on text for additional help."
        val cardAbout = ObjectCardText(this, "About wX", textSize, padding)
        val cardLocations = ObjectCardText(this, "Locations", textSize, SettingsLocationRecyclerViewActivity::class.java, padding)
        val cardsn = ObjectCardText(this, "Spotter Network Settings", MyApplication.textSizeNormal, SettingsSpotterNetwork::class.java, MyApplication.paddingSettings)
        val cardNotif = ObjectCardText(this, "Notifications", textSize, SettingsNotificationsActivity::class.java, padding)
        val cardWidgets = ObjectCardText(this, "Widgets", textSize, SettingsWidgetsActivity::class.java, padding)
        val cardColors = ObjectCardText(this, "Colors", textSize, SettingsColorsActivity::class.java, padding)
        val cardPL = ObjectCardText(this, "PlayList", textSize, SettingsPlaylistActivity::class.java, padding)
        val cardRadar = ObjectCardText(this, "Radar", textSize, SettingsRadarActivity::class.java, padding)
        val cardHS = ObjectCardText(this, "Home Screen", textSize, SettingsHomeScreenActivity::class.java, padding)
        val cardUI = ObjectCardText(this, "User Interface", textSize, SettingsUIActivity::class.java, padding)
        val cardCtoF = ObjectCardText(this, "Celsius to fahrenheit table", textSize, padding)
        val cardDeleteFiles = ObjectCardText(this, "Delete old radar files", MyApplication.textSizeNormal, MyApplication.paddingSettings)
        val cardbackuppref = ObjectCardText(this, "Backup Settings", MyApplication.textSizeNormal, MyApplication.paddingSettings)
        val cardrestorepref = ObjectCardText(this, "Restore Settings", MyApplication.textSizeNormal, MyApplication.paddingSettings)
        cardCtoF.setOnClickListener(View.OnClickListener {
            ObjectIntent.showText(this, arrayOf(UtilityMath.celsiusToFahrenheitTable(), "Celsius to Fahrenheit table"))
        })
        cardbackuppref.setOnClickListener(View.OnClickListener { backuprestore.backupPrefs(this) })
        cardrestorepref.setOnClickListener(View.OnClickListener { backuprestore.restorePrefs(this) })
        cardDeleteFiles.setOnClickListener(View.OnClickListener {
            UtilityUI.makeSnackBar(linearLayout, "Deleted old radar files: " + UtilityFileManagement.deleteCacheFiles(this))
        })
        cardAbout.setOnClickListener(View.OnClickListener { ObjectIntent(this, SettingsAboutActivity::class.java) })
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
            linearLayout.addView(it)
        }
	
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Check for Internet on startup",
                        "CHECKINTERNET",
                        R.string.checkinternet_switch_label
                ).card
        )

    }

    override fun onStop() {
        super.onStop()
        MyApplication.initPreferences(this)
        val restartNotification = Utility.readPref(this, "RESTART_NOTIF", "false")
        if (restartNotification == "true") {
            UtilityWXJobService.startService(this)
            Utility.writePref(this, "RESTART_NOTIF", "false")
        }
    }
}

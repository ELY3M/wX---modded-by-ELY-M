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

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.LinearLayout

import joshuatee.wx.R
import joshuatee.wx.audio.SettingsPlaylistActivity
import joshuatee.wx.MyApplication
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.UtilityTheme
import joshuatee.wx.util.*

//elys mod - deletefiles keep in   
import joshuatee.wx.ui.UtilityUI

class SettingsMainActivity : BaseActivity() {

    private lateinit var linearLayout: LinearLayout

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        linearLayout = findViewById(R.id.linearLayout)
        UtilityTheme.setPrimaryColor(this)
        val backuprestore = UtilityBackupRestore()
        val version = Utility.getVersion(this)
        val textSize = UIPreferences.textSizeLarge
        val padding = UIPreferences.paddingSettings
        toolbar.subtitle = "$version, tap on text for additional help."
        val cardAbout = ObjectCardText(this, "About wX", textSize, padding)
        val cardLocations = ObjectCardText(this, "Locations", textSize, SettingsLocationRecyclerViewActivity::class.java, padding)
        val cardsn = ObjectCardText(this, "Spotter Network Settings", UIPreferences.textSizeNormal, SettingsSpotterNetwork::class.java, UIPreferences.paddingSettings)
        val cardNotif = ObjectCardText(this, "Notifications", textSize, SettingsNotificationsActivity::class.java, padding)
        val cardWidgets = ObjectCardText(this, "Widgets", textSize, SettingsWidgetsActivity::class.java, padding)
        val cardColors = ObjectCardText(this, "Colors", textSize, SettingsColorsActivity::class.java, padding)
        val cardPL = ObjectCardText(this, "PlayList", textSize, SettingsPlaylistActivity::class.java, padding)
        val cardRadar = ObjectCardText(this, "Radar", textSize, SettingsRadarActivity::class.java, padding)
        val cardHS = ObjectCardText(this, "Home Screen", textSize, SettingsHomeScreenActivity::class.java, padding)
        val cardUI = ObjectCardText(this, "User Interface", textSize, SettingsUIActivity::class.java, padding)
        val cardCtoF = ObjectCardText(this, "Celsius to fahrenheit table", textSize, padding)
        val cardDeleteFiles = ObjectCardText(this, "Delete old radar files", UIPreferences.textSizeNormal, UIPreferences.paddingSettings)
        val cardbackuppref = ObjectCardText(this, "Backup Settings", UIPreferences.textSizeNormal, UIPreferences.paddingSettings)
        val cardrestorepref = ObjectCardText(this, "Restore Settings", UIPreferences.textSizeNormal, UIPreferences.paddingSettings)
        cardCtoF.setOnClickListener {
            ObjectIntent.showText(this, arrayOf(UtilityMath.celsiusToFahrenheitTable(), "Celsius to Fahrenheit table"))
        }
        cardbackuppref.setOnClickListener { backuprestore.backupPrefs(this) }
        cardrestorepref.setOnClickListener { backuprestore.restorePrefs(this) }
        cardDeleteFiles.setOnClickListener {
            ObjectIntent.showText(this, arrayOf(UtilityFileManagement.deleteCacheFiles(this), "Deleted old radar files"))
        }
        cardAbout.setOnClickListener { ObjectIntent(this, SettingsAboutActivity::class.java) }
        listOf(
                cardAbout.get(),
                cardLocations.get(),
		cardsn.get(),
                cardNotif.get(),
                cardWidgets.get(),
                cardColors.get(),
                cardPL.get(),
                cardRadar.get(),
                cardHS.get(),
                cardUI.get(),
                cardCtoF.get(),
                cardDeleteFiles.get(),
		cardbackuppref.get(),
		cardrestorepref.get()
        ).forEach {
            linearLayout.addView(it)
        }
	
	//elys mod
        linearLayout.addView(
                ObjectSettingsCheckBox(
                        this,
                        "Check for Internet on startup",
                        "CHECKINTERNET",
                        R.string.checkinternet_switch_label
                ).get()
        )
	//

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

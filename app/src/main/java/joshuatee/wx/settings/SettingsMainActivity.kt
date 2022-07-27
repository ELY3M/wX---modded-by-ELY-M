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
import joshuatee.wx.R
import joshuatee.wx.audio.SettingsPlaylistActivity
import joshuatee.wx.MyApplication
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.Route
import joshuatee.wx.ui.*
import joshuatee.wx.util.*

class SettingsMainActivity : BaseActivity() {

    private lateinit var box: VBox

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        val version = Utility.getVersion(this)
        setTitle("Settings", "$version, tap on text for additional help.")
        box = VBox.fromResource(this)
        UtilityTheme.setPrimaryColor(this)
        val backuprestore = UtilityBackupRestore()
        val textSize = UIPreferences.textSizeLarge
        val padding = UIPreferences.paddingSettings
        toolbar.subtitle = "$version, tap on text for additional help."
        val cardAbout = CardText(this, "About wX", textSize, padding)
        val cardLocations = CardText(this, "Locations", textSize, SettingsLocationRecyclerViewActivity::class.java, padding)
        val cardsn = CardText(this, "Spotter Network Settings", textSize, SettingsSpotterNetwork::class.java, padding)
        val cardNotif = CardText(this, "Notifications", textSize, SettingsNotificationsActivity::class.java, padding)
        val cardWidgets = CardText(this, "Widgets", textSize, SettingsWidgetsActivity::class.java, padding)
        val cardColors = CardText(this, "Colors", textSize, SettingsColorsActivity::class.java, padding)
        val cardPL = CardText(this, "PlayList", textSize, SettingsPlaylistActivity::class.java, padding)
        val cardRadar = CardText(this, "Radar", textSize, SettingsRadarActivity::class.java, padding)
        val cardHS = CardText(this, "Home Screen", textSize, SettingsHomeScreenActivity::class.java, padding)
        val cardUI = CardText(this, "User Interface", textSize, SettingsUIActivity::class.java, padding)
        val cardCtoF = CardText(this, "Celsius to fahrenheit table", textSize, padding)
        val cardDeleteFiles = CardText(this, "Delete old radar files", textSize, padding)
        val cardbackuppref = CardText(this, "Backup Settings", textSize, padding)
        val cardrestorepref = CardText(this, "Restore Settings", textSize, padding)
        cardCtoF.connect {
            Route.text(this, arrayOf(UtilityMath.celsiusToFahrenheitTable(), "Celsius to Fahrenheit table"))
        }
        cardbackuppref.connect { backuprestore.backupPrefs(this) }
        cardrestorepref.connect { backuprestore.restorePrefs(this) }
        cardDeleteFiles.connect {
            Route.text(this, arrayOf(UtilityFileManagement.deleteCacheFiles(this), "Deleted old radar files"))
        }
        cardAbout.connect { Route(this, SettingsAboutActivity::class.java) }
        listOf(
                cardAbout.get(),
                cardCtoF.get(),
                cardColors.get(),
                cardHS.get(),
                cardLocations.get(),
                cardNotif.get(),
                cardPL.get(),
                cardRadar.get(),
		cardsn.get(),
                cardUI.get(),
                cardWidgets.get(),
                cardDeleteFiles.get(),
		cardbackuppref.get(),
		cardrestorepref.get()
        ).forEach {
            box.addWidget(it)
        }
	
	    //elys mod
        box.addWidget(
                ObjectSwitch(
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

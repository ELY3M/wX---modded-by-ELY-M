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

import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.Route
import joshuatee.wx.ui.*
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityFileManagement
import joshuatee.wx.util.UtilityMath

class SettingsMainActivity : BaseActivity() {

    private lateinit var box: VBox
    private var notifStatus = ""
    private lateinit var notifCard: CardText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        setTitle("Settings", Utility.getVersion(this) + ", tap on text for additional help.")
        setupUI()
    }

    private fun setupUI() {
        box = VBox.fromResource(this)
        UtilityTheme.setPrimaryColor(this)
        addCards()
        updateNotificationStatus()
    }

    private fun addCards() {
        val textSize = UIPreferences.textSizeLarge
        val padding = UIPreferences.paddingSettings
        box.addWidget(CardText(this, "About wX", textSize, SettingsAboutActivity::class.java, padding))
	//elys mod
        box.addWidget(CardText(this, "Celsius to fahrenheit table", textSize,
                { Route.text(this, UtilityMath.celsiusToFahrenheitTable(), "Celsius to Fahrenheit table") }, padding))
        box.addWidget(CardText(this, "Colors", textSize, SettingsColorsActivity::class.java, padding))
        box.addWidget(CardText(this, "Home Screen", textSize, SettingsHomeScreenActivity::class.java, padding))
        box.addWidget(CardText(this, "Locations", textSize, SettingsLocationRecyclerViewActivity::class.java, padding))
        //elys mod
	box.addWidget(CardText(this, "Spotter Network Settings", textSize, SettingsSpotterNetwork::class.java, padding))
        notifCard = CardText(this, "Notifications", textSize, SettingsNotificationsActivity::class.java, padding)
        box.addWidget(notifCard)
        box.addWidget(CardText(this, "PlayList", textSize, { Route.playlist(this) }, padding))
        box.addWidget(CardText(this, "Radar", textSize, SettingsRadarActivity::class.java, padding))
        box.addWidget(CardText(this, "User Interface", textSize, SettingsUIActivity::class.java, padding))
        box.addWidget(CardText(this, "Widgets", textSize, SettingsWidgetsActivity::class.java, padding))
        //elys mod
        box.addWidget(CardText(this, "Delete old radar files", textSize, UtilityFileManagement.deleteCacheFiles(this)::class.java, padding))
        box.addWidget(CardText(this, "Backup Settings", textSize, { UtilityBackupRestore.backupPrefs(this) }, padding))
        box.addWidget(CardText(this, "Restore Settings", textSize, { UtilityBackupRestore.restorePrefs(this) }, padding))
        box.addWidget(
            Switch(this,"Check for Internet on startup","CHECKINTERNET", R.string.checkinternet_switch_label)
        )
        //elys mod end

    }

    private fun updateNotificationStatus() {
        val notificationManagerCompat = NotificationManagerCompat.from(this)
        notifStatus = if (!notificationManagerCompat.areNotificationsEnabled()) {
            " (blocked for app)"
        } else {
            ""
        }
        notifCard.text = "Notifications$notifStatus"
    }

    override fun onResume() {
        super.onResume()
        updateNotificationStatus()
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

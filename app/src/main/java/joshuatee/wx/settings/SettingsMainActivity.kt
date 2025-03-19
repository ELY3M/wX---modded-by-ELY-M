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
//modded by ELY M.  

package joshuatee.wx.settings

import android.annotation.SuppressLint
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

    @SuppressLint("MissingSuperCall")
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
        box.addWidget(CardText(this, "About wX", SettingsAboutActivity::class.java))
	//elys mod
        box.addWidget(CardText(this, "Celsius to fahrenheit table", { Route.text(this, UtilityMath.celsiusToFahrenheitTable(), "Celsius to Fahrenheit table") }))
        box.addWidget(CardText(this, "Colors", SettingsColorsActivity::class.java))
        box.addWidget(CardText(this, "Home Screen", SettingsHomeScreenActivity::class.java))
        box.addWidget(CardText(this, "Locations", SettingsLocationRecyclerViewActivity::class.java))
        //elys mod
	box.addWidget(CardText(this, "Spotter Network Settings", SettingsSpotterNetwork::class.java))
        notifCard = CardText(this, "Notifications", SettingsNotificationsActivity::class.java)
        box.addWidget(notifCard)
        box.addWidget(CardText(this, "PlayList") { Route.playlist(this) })
        box.addWidget(CardText(this, "Radar", SettingsRadarActivity::class.java))
        box.addWidget(CardText(this, "User Interface", SettingsUIActivity::class.java))
        box.addWidget(CardText(this, "Widgets", SettingsWidgetsActivity::class.java))
        //elys mod
        box.addWidget(CardText(this, "Delete old radar files", UtilityFileManagement.deleteCacheFiles(this)::class.java))
        box.addWidget(CardText(this, "Backup Settings", { UtilityBackupRestore.backupPrefs(this) }))
        box.addWidget(CardText(this, "Restore Settings", { UtilityBackupRestore.restorePrefs(this) }))
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

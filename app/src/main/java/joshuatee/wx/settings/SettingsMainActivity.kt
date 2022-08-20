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
import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.Route
import joshuatee.wx.ui.*
import joshuatee.wx.util.*

class SettingsMainActivity : BaseActivity() {

    private lateinit var box: VBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        setTitle("Settings", Utility.getVersion(this) + ", tap on text for additional help.")
        box = VBox.fromResource(this)
        UtilityTheme.setPrimaryColor(this)
        val textSize = UIPreferences.textSizeLarge
        val padding = UIPreferences.paddingSettings
        CardText(this, box, "About wX", textSize, SettingsAboutActivity::class.java, padding)
        CardText(this, box, "Celsius to fahrenheit table", textSize,
                { Route.text(this, arrayOf(UtilityMath.celsiusToFahrenheitTable(), "Celsius to Fahrenheit table")) }, padding)
        CardText(this, box, "Colors", textSize, SettingsColorsActivity::class.java, padding)
        CardText(this, box, "Home Screen", textSize, SettingsHomeScreenActivity::class.java, padding)
        CardText(this, box, "Locations", textSize, SettingsLocationRecyclerViewActivity::class.java, padding)
        //elys mod
	CardText(this, box, "Spotter Network Settings", textSize, SettingsSpotterNetwork::class.java, padding)
        CardText(this, box, "Notifications", textSize, SettingsNotificationsActivity::class.java, padding)
        CardText(this, box, "PlayList", textSize, { Route.playlist(this) }, padding)
        CardText(this, box, "Radar", textSize, SettingsRadarActivity::class.java, padding)
        CardText(this, box, "User Interface", textSize, SettingsUIActivity::class.java, padding)
        CardText(this, box, "Widgets", textSize, SettingsWidgetsActivity::class.java, padding)
        //elys mod
        CardText(this, box, "Delete old radar files", textSize, UtilityFileManagement.deleteCacheFiles(this)::class.java, padding)
        CardText(this, box, "Backup Settings", textSize, { UtilityBackupRestore.backupPrefs(this) }, padding)
        CardText(this, box, "Restore Settings", textSize, { UtilityBackupRestore.restorePrefs(this) }, padding)
        box.addWidget(
            ObjectSwitch(this,"Check for Internet on startup","CHECKINTERNET", R.string.checkinternet_switch_label).get()
        )
        //elys mod end

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

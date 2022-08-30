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

package joshuatee.wx.settings

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import joshuatee.wx.R
import joshuatee.wx.canada.UtilityCitiesCanada
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.FutureText2
import joshuatee.wx.objects.Route
import joshuatee.wx.radar.UtilityCitiesExtended
import joshuatee.wx.ui.*
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityMap

class SettingsLocationGenericActivity : BaseActivity(), OnMenuItemClickListener {

    //
    // manual interface for searching and saving a location
    //
    // arg1 location number
    //

    companion object { const val LOC_NUM = "" }

    private var locationLat = ""
    private var locationLon = ""
    // This controls whether or not the title should update based on
    // If this is a location edit (show)
    // This is a new location (don't show)
    // This is a new location that was then saved (show)
    private var updateTitle = true // TODO FIXME remove the need for this
    private var locationLabel = ""
    private var locationNumber = ""
    private lateinit var alertRadar1Sw: ObjectSwitch
    private lateinit var alertSoundSw: ObjectSwitch
    private lateinit var alert7Day1Sw: ObjectSwitch
    private lateinit var alertCcSw: ObjectSwitch
    private lateinit var alertSw: ObjectSwitch
    private lateinit var alertMcdSw: ObjectSwitch
    private lateinit var alertSwoSw: ObjectSwitch
    private lateinit var alertSpcfwSw: ObjectSwitch
    private lateinit var alertWpcmpdSw: ObjectSwitch
    private var menuLocal: Menu? = null
    private lateinit var editTextLabel: EditText
    private lateinit var editTextLat: EditText
    private lateinit var editTextLon: EditText
    private lateinit var box: VBox
    private lateinit var relativeLayout: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_settings_location_generic, R.menu.settings_location_generic_bottom, true)
        locationNumber = intent.getStringArrayExtra(LOC_NUM)!![0]
        title = "Location $locationNumber" // TODO FIXME move into updateSubTitle and rename method
        editTextLabel = findViewById(R.id.locLabelEt)
        editTextLat = findViewById(R.id.locXEt)
        editTextLon = findViewById(R.id.locYEt)
        box = VBox.fromResource(this)
        relativeLayout = findViewById(R.id.rl)
        objectToolbarBottom.connect(this)
        ObjectFab(this, R.id.fab) { saveLocation() }
        Card(this, R.id.cv1)
        initializeDataStructures()
        addSwitches()
        updateSubTitle()
        adjustForWhiteTheme()
        hideNonUSNotifications()
    }

    private fun initializeDataStructures() {
        objectToolbarBottom.find(R.id.action_delete).isVisible = Location.numLocations > 1
        UtilityCitiesExtended.create(this)
        UtilityCitiesCanada.initialize()
        Utility.writePref(this, "LOCATION_CANADA_PROV", "")
        Utility.writePref(this, "LOCATION_CANADA_CITY", "")
        Utility.writePref(this, "LOCATION_CANADA_ID", "")
        locationLat = Utility.readPref(this, "LOC" + locationNumber + "_X", "")
        locationLon = Utility.readPref(this, "LOC" + locationNumber + "_Y", "")
        locationLabel = Utility.readPref(this, "LOC" + locationNumber + "_LABEL", "")
        editTextLat.setText(locationLat)
        editTextLon.setText(locationLon)
        editTextLabel.setText(locationLabel)
        editTextLabel.setSelection(editTextLabel.length())
    }

    private fun adjustForWhiteTheme() {
        if (UIPreferences.themeIsWhite) {
            listOf(editTextLabel, editTextLat, editTextLon).forEach {
                it.setTextColor(Color.BLACK)
                it.setHintTextColor(Color.GRAY)
            }
        }
    }

    private fun addSwitches() {
        var alertNotificationCurrent = Utility.readPref(this, "ALERT" + locationNumber + "_NOTIFICATION", "false")
        var alertNotificationRadarCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_RADAR$locationNumber", "false")
        var alertCcNotificationCurrent = Utility.readPref(this, "ALERT_CC" + locationNumber + "_NOTIFICATION", "false")
        var alert7Day1NotificationCurrent = Utility.readPref(this, "ALERT_7DAY_" + locationNumber + "_NOTIFICATION", "false")
        var alertNotificationSoundCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_SOUND$locationNumber", "false")
        var alertNotificationMcdCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_MCD$locationNumber", "false")
        var alertNotificationSwoCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_SWO$locationNumber", "false")
        var alertNotificationSpcfwCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_SPCFW$locationNumber", "false")
        var alertNotificationWpcmpdCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_WPCMPD$locationNumber", "false")
        // If this this is a new location
        if (To.int(locationNumber) == Location.numLocations + 1) {
            updateTitle = false
            locationLabel = "Location $locationNumber"
            editTextLabel.setText(locationLabel)
            editTextLabel.setSelection(editTextLabel.length())
            //
            // needed to prevent old location(deleted) data from showing up when adding new
            //
            locationLat = ""
            locationLon = ""
            alertNotificationCurrent = "false"
            alertNotificationRadarCurrent = "false"
            alertCcNotificationCurrent = "false"
            alert7Day1NotificationCurrent = "false"
            alertNotificationSoundCurrent = "false"
            alertNotificationMcdCurrent = "false"
            alertNotificationSwoCurrent = "false"
            alertNotificationSpcfwCurrent = "false"
            alertNotificationWpcmpdCurrent = "false"
        }
        // FIXME TODO refactor this, use a map based off key
        alertSw = ObjectSwitch(this,
                "Alert",
                "ALERT" + locationNumber + "_NOTIFICATION",
                R.string.alert_switch_text)
        alertSw.isChecked(alertNotificationCurrent == "true")
        alertCcSw = ObjectSwitch(this,
                "Conditions",
                "ALERT_CC" + locationNumber + "_NOTIFICATION",
                R.string.alert_cc_switch_text)
        alertCcSw.isChecked(alertCcNotificationCurrent == "true")
        alert7Day1Sw = ObjectSwitch(this,
                "7day",
                "ALERT_7DAY_" + locationNumber + "_NOTIFICATION",
                R.string.alert_7day_1_switch_text)
        alert7Day1Sw.isChecked(alert7Day1NotificationCurrent == "true")
        alertSoundSw = ObjectSwitch(this,
                "Sound",
                "ALERT_NOTIFICATION_SOUND$locationNumber",
                R.string.alert_sound_switch_text)
        alertSoundSw.isChecked(alertNotificationSoundCurrent == "true")
        alertRadar1Sw = ObjectSwitch(this,
                "Radar",
                "ALERT_NOTIFICATION_RADAR$locationNumber",
                R.string.alert_radar1_switch_text)
        alertRadar1Sw.isChecked(alertNotificationRadarCurrent == "true")
        alertMcdSw = ObjectSwitch(this,
                "SPC MCD",
                "ALERT_NOTIFICATION_MCD$locationNumber",
                R.string.alert_mcd_switch_text)
        alertMcdSw.isChecked(alertNotificationMcdCurrent == "true")
        alertSwoSw = ObjectSwitch(this,
                "SPC SWO",
                "ALERT_NOTIFICATION_SWO$locationNumber",
                R.string.alert_swo_switch_text)
        alertSwoSw.isChecked(alertNotificationSwoCurrent == "true")
        alertSpcfwSw = ObjectSwitch(this,
                "SPC FW",
                "ALERT_NOTIFICATION_SPCFW$locationNumber",
                R.string.alert_spcfw_switch_text)
        alertSpcfwSw.isChecked(alertNotificationSpcfwCurrent == "true")
        alertWpcmpdSw = ObjectSwitch(
                this,
                "WPC MPD",
                "ALERT_NOTIFICATION_WPCMPD$locationNumber",
                R.string.alert_wpcmpd_switch_text
        )
        alertWpcmpdSw.isChecked(alertNotificationWpcmpdCurrent == "true")
        listOf(
                alertSw,
                alertSoundSw,
                alertRadar1Sw,
                alertCcSw,
                alert7Day1Sw,
                alertMcdSw,
                alertSwoSw,
                alertSpcfwSw,
                alertWpcmpdSw
        ).forEach{
            box.addWidget(it.get())
        }
    }

    override fun onRestart() {
        val caProv = Utility.readPref(this, "LOCATION_CANADA_PROV", "")
        val caCity = Utility.readPref(this, "LOCATION_CANADA_CITY", "")
        val caId = Utility.readPref(this, "LOCATION_CANADA_ID", "")
        if (caProv != "" || caCity != "" || caId != "") {
            editTextLat.setText(resources.getString(R.string.settings_loc_generic_ca_x, caProv))
            editTextLon.setText(caId)
            val caLabel = "$caCity, $caProv"
            editTextLabel.setText(caLabel)
            notificationsCanada(true)
            saveLocation()
        }
        afterDelete()
        super.onRestart()
    }

    private fun afterDelete() {
        val alertNotificationCurrent = Utility.readPref(this, "ALERT" + locationNumber + "_NOTIFICATION", "false")
        val alertNotificationRadarCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_RADAR$locationNumber", "false")
        val alertCcNotificationCurrent = Utility.readPref(this, "ALERT_CC" + locationNumber + "_NOTIFICATION", "false")
        val alert7Day1NotificationCurrent = Utility.readPref(this, "ALERT_7DAY_" + locationNumber + "_NOTIFICATION", "false")
        val alertNotificationSoundCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_SOUND$locationNumber", "false")
        val alertNotificationMcdCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_MCD$locationNumber", "false")
        val alertNotificationSwoCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_SWO$locationNumber", "false")
        val alertNotificationSpcfwCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_SPCFW$locationNumber", "false")
        val alertNotificationWpcmpdCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_WPCMPD$locationNumber", "false")
        listOf(alertRadar1Sw,
                alertSoundSw,
                alert7Day1Sw,
                alertCcSw,
                alertSw,
                alertMcdSw,
                alertSwoSw,
                alertSpcfwSw,
                alertWpcmpdSw
        ).forEach{
            it.get().visibility = View.VISIBLE
        }
        // FIXME fold into forEach above
        alertSw.isChecked(alertNotificationCurrent == "true")
        alertCcSw.isChecked(alertCcNotificationCurrent == "true")
        alert7Day1Sw.isChecked(alert7Day1NotificationCurrent == "true")
        alertSoundSw.isChecked(alertNotificationSoundCurrent == "true")
        alertRadar1Sw.isChecked(alertNotificationRadarCurrent == "true")
        alertMcdSw.isChecked(alertNotificationMcdCurrent == "true")
        alertSwoSw.isChecked(alertNotificationSwoCurrent == "true")
        alertSpcfwSw.isChecked(alertNotificationSpcfwCurrent == "true")
        alertWpcmpdSw.isChecked(alertNotificationWpcmpdCurrent == "true")
        hideNonUSNotifications()
    }

    private fun saveLocation() {
        val lat = editTextLat.text.toString()
        val lon = editTextLon.text.toString()
        val label = editTextLabel.text.toString()
        FutureText2(
                this,
                { Location.save(this, locationNumber, lat, lon, label) })
                { saveStatus ->
                    ObjectPopupMessage(relativeLayout, saveStatus)
                    updateTitle = true
                    updateSubTitle()
                    if (lat.startsWith("CANADA:")) {
                        notificationsCanada(true)
                    } else {
                        notificationsCanada(false)
                    }
                }
    }

    override fun onStop() {
        super.onStop()
        val restartNotif = Utility.readPref(this, "RESTART_NOTIF", "false")
        if (restartNotif == "true") {
            UtilityWXJobService.startService(this)
            Utility.writePref(this, "RESTART_NOTIF", "false")
        }
        Location.refreshLocationData(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_location_generic, menu)
        val searchView = menu.findItem(R.id.ab_search).actionView as ArrayAdapterSearchView
        val combinedCitiesList = UtilityCitiesExtended.cityLabels.toList()
        val cityArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, combinedCitiesList)
        cityArrayAdapter.setDropDownViewResource(UIPreferences.spinnerLayout)
        searchView.setAdapter(cityArrayAdapter)
        searchView.queryHint = "Enter city here"
        searchView.setOnItemClickListener { _, _, position, _ ->
            var k = 0
            for (y in combinedCitiesList.indices) {
                if (cityArrayAdapter.getItem(position) == combinedCitiesList[y]) {
                    k = y
                    break
                }
            }
            if (k < UtilityCitiesExtended.cityLabels.size) {
                searchView.setText(cityArrayAdapter.getItem(position)!!)
                editTextLabel.setText(cityArrayAdapter.getItem(position))
                editTextLat.setText(UtilityCitiesExtended.cityLat[k].toString())
                val lonString = "-" + UtilityCitiesExtended.cityLon[k].toString()
                editTextLon.setText(lonString)
                val searchViewLocal = menuLocal!!.findItem(R.id.ab_search).actionView as SearchView
                searchViewLocal.onActionViewCollapsed()
                searchViewLocal.isIconified = true
                saveLocation()
            }
        }

        searchView.setOnQueryTextListener(object : OnQueryTextListener {

            override fun onQueryTextChange(newText: String) = false

            override fun onQueryTextSubmit(query: String): Boolean {
                editTextLabel.setText(query)
                val searchViewLocal = menuLocal!!.findItem(R.id.ab_search).actionView as SearchView
                searchViewLocal.onActionViewCollapsed()
                return false
            }
        })
        menuLocal = menu
        if (UIPreferences.themeIsWhite && UIPreferences.themeInt != R.style.MyCustomTheme_whitest_NOAB) {
            changeSearchViewTextColor(searchView)
        }
        // the SearchView's AutoCompleteTextView drop down. For some reason this wasn't working in styles.xml
        val autoCompleteTextView: SearchView.SearchAutoComplete = searchView.findViewById(R.id.search_src_text)
        when {
            UIPreferences.themeIsWhite -> {
                autoCompleteTextView.setDropDownBackgroundResource(R.drawable.dr_white)
            }
            Utility.isThemeAllBlack() -> {
                autoCompleteTextView.setDropDownBackgroundResource(R.drawable.dr_black)
            }
            else -> {
                autoCompleteTextView.setDropDownBackgroundResource(R.drawable.dr_dark_blue)
            }
        }
        return true
    }

    private fun delete() {
        Location.delete(this, locationNumber)
        UtilityWXJobService.startService(this)
        finish()
    }

    private fun showMap() {
        val lat = editTextLat.text.toString()
        val lon = editTextLon.text.toString()
        if (lat.isNotEmpty() && lon.isNotEmpty()) {
            if (Location.us(lat)) {
                Route.webView(this, UtilityMap.getUrl(lat, lon, "9"), Location.name)
            } else {
                Route.webView(this, UtilityMap.getUrlFromAddress(editTextLabel.text.toString()), Location.name)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> delete()
            R.id.action_map -> showMap()
            R.id.action_ca -> Route(this, SettingsLocationCanadaActivity::class.java)
            R.id.action_ab -> openCanadaMap("ab")
            R.id.action_bc -> openCanadaMap("bc")
            R.id.action_mb -> openCanadaMap("mb")
            R.id.action_nb -> openCanadaMap("nb")
            R.id.action_nl -> openCanadaMap("nl")
            R.id.action_ns -> openCanadaMap("ns")
            R.id.action_nt -> openCanadaMap("nt")
            R.id.action_nu -> openCanadaMap("nu")
            R.id.action_on -> openCanadaMap("on")
            R.id.action_pe -> openCanadaMap("pe")
            R.id.action_qc -> openCanadaMap("qc")
            R.id.action_sk -> openCanadaMap("sk")
            R.id.action_yt -> openCanadaMap("yt")
            R.id.action_help -> ObjectDialogue(this, resources.getString(R.string.activity_settings_generic_help))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_locate -> {
                actionGps()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun actionGps() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val xy = UtilityLocation.getGps(this)
            editTextLat.setText(xy[0].toString())
            editTextLon.setText(xy[1].toString())
            saveLocation()
        } else {
            // The ACCESS_FINE_LOCATION is denied, then I request it and manage the result in
            // onRequestPermissionsResult() using the constant myPermissionAccessFineLocation
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), myPermissionAccessFineLocation)
            }
        }
    }

    private val myPermissionAccessFineLocation = 5001

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            myPermissionAccessFineLocation -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val xy = UtilityLocation.getGps(this)
                editTextLat.setText(xy[0].toString())
                editTextLon.setText(xy[1].toString())
            }
        }
    }

    private fun notificationsCanada(hide: Boolean) {
        val visibility = if (hide) {
            View.GONE
        } else {
            View.VISIBLE
        }
        listOf(alertMcdSw,
                alertSwoSw,
                alertSpcfwSw,
                alertWpcmpdSw
        ).forEach{
            it.get().visibility = visibility
        }
    }

    private fun hideNonUSNotifications() {
        val label = editTextLat.text.toString()
        if (label.contains("CANADA")) {
            notificationsCanada(true)
        }
    }

    private fun updateSubTitle() {
        val subTitleString = "WFO: " +
                Utility.readPref(this, "NWS$locationNumber", "") +
                " - Nexrad: " + Utility.readPref(this, "RID$locationNumber", "")
        if (subTitleString != "WFO:  - Nexrad: " && updateTitle) {
            toolbar.subtitle = subTitleString
        }
    }

    private fun changeSearchViewTextColor(view: View?) {
        if (view != null) {
            if (view is TextView) {
                view.setTextColor(Color.WHITE)
            } else if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    changeSearchViewTextColor(view.getChildAt(i))
                }
            }
        }
    }

    private fun openCanadaMap(s: String) {
        Route(this, SettingsLocationCanadaMapActivity::class.java, SettingsLocationCanadaMapActivity.URL, arrayOf(s))
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_G -> if (event.isCtrlPressed) actionGps()
            KeyEvent.KEYCODE_M -> if (event.isCtrlPressed) toolbarBottom.showOverflowMenu()
            KeyEvent.KEYCODE_SLASH -> if (event.isAltPressed) ObjectDialogue(this, Utility.showLocationEditShortCuts())
            else -> super.onKeyUp(keyCode, event)
        }
        return true
    }
}

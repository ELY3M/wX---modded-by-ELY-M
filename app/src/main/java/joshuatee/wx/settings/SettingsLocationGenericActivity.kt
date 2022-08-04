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
import android.os.Build
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
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityMap

class SettingsLocationGenericActivity : BaseActivity(), OnMenuItemClickListener {

    //
    // manual interface for searching and saving a location
    //
    // arg1 location number
    //

    companion object { const val LOC_NUM = "" }

    private var locXStr = ""
    private var locYStr = ""
    private var updateTitle = true
    private var locLabelCurrent = ""
    private var locNum = ""
    private var locNumToSaveStr = ""
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
    private lateinit var locLabelEt: EditText
    private lateinit var locXEt: EditText
    private lateinit var locYEt: EditText
    private lateinit var box: VBox
    private lateinit var rl: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_settings_location_generic, R.menu.settings_location_generic_bottom, true)
        val locNumArr = intent.getStringArrayExtra(LOC_NUM)
        locNum = locNumArr!![0]
        val locNumInt = locNum.toIntOrNull() ?: 0
        title = "Location $locNum"

        locLabelEt = findViewById(R.id.locLabelEt)
        locXEt = findViewById(R.id.locXEt)
        locYEt = findViewById(R.id.locYEt)
        box = VBox.fromResource(this)
        rl = findViewById(R.id.rl)

        UtilityCitiesExtended.create(this)
        UtilityCitiesCanada.initialize()

        toolbarBottom.setOnMenuItemClickListener(this)
        Utility.writePref(this, "LOCATION_CANADA_PROV", "")
        Utility.writePref(this, "LOCATION_CANADA_CITY", "")
        Utility.writePref(this, "LOCATION_CANADA_ID", "")
        ObjectFab(this, R.id.fab) { fabSaveLocation() }
        val me = toolbarBottom.menu
        Card(this, R.id.cv1)
        locXStr = Utility.readPref(this, "LOC" + locNum + "_X", "")
        locYStr = Utility.readPref(this, "LOC" + locNum + "_Y", "")
        var alertNotificationCurrent = Utility.readPref(this, "ALERT" + locNum + "_NOTIFICATION", "false")
        var alertNotificationRadarCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_RADAR$locNum", "false")
        var alertCcNotificationCurrent = Utility.readPref(this, "ALERT_CC" + locNum + "_NOTIFICATION", "false")
        var alert7Day1NotificationCurrent = Utility.readPref(this, "ALERT_7DAY_" + locNum + "_NOTIFICATION", "false")
        var alertNotificationSoundCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_SOUND$locNum", "false")
        var alertNotificationMcdCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_MCD$locNum", "false")
        var alertNotificationSwoCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_SWO$locNum", "false")
        var alertNotificationSpcfwCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_SPCFW$locNum", "false")
        var alertNotificationWpcmpdCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_WPCMPD$locNum", "false")
        locLabelCurrent = Utility.readPref(this, "LOC" + locNum + "_LABEL", "")
        // If this this is a new location
        if (locNumInt == Location.numLocations + 1) {
            updateTitle = false
            locNumToSaveStr = Location.numLocations.toString()
            locLabelCurrent = "Location $locNum"
            //
            // needed to prevent old location(deleted) data from showing up when adding new
            //
            locXStr = ""
            locYStr = ""
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
        updateSubTitle()
        me.findItem(R.id.action_delete).isVisible = Location.numLocations > 1
        locLabelEt.setText(locLabelCurrent)
        locXEt.setText(locXStr)
        locYEt.setText(locYStr)
        if (UIPreferences.themeIsWhite) {
            locLabelEt.setTextColor(Color.BLACK)
            locXEt.setTextColor(Color.BLACK)
            locYEt.setTextColor(Color.BLACK)
            locLabelEt.setHintTextColor(Color.GRAY)
            locXEt.setHintTextColor(Color.GRAY)
            locYEt.setHintTextColor(Color.GRAY)
        }
        alertSw = ObjectSwitch(this,
                "Alert",
                "ALERT" + locNum + "_NOTIFICATION",
                R.string.alert_switch_text)
        alertSw.isChecked(alertNotificationCurrent == "true")
        alertCcSw = ObjectSwitch(this,
                "Conditions",
                "ALERT_CC" + locNum + "_NOTIFICATION",
                R.string.alert_cc_switch_text)
        alertCcSw.isChecked(alertCcNotificationCurrent == "true")
        alert7Day1Sw = ObjectSwitch(this,
                "7day",
                "ALERT_7DAY_" + locNum + "_NOTIFICATION",
                R.string.alert_7day_1_switch_text)
        alert7Day1Sw.isChecked(alert7Day1NotificationCurrent == "true")
        alertSoundSw = ObjectSwitch(this,
                "Sound",
                "ALERT_NOTIFICATION_SOUND$locNum",
                R.string.alert_sound_switch_text)
        alertSoundSw.isChecked(alertNotificationSoundCurrent == "true")
        alertRadar1Sw = ObjectSwitch(this,
                "Radar",
                "ALERT_NOTIFICATION_RADAR$locNum",
                R.string.alert_radar1_switch_text)
        alertRadar1Sw.isChecked(alertNotificationRadarCurrent == "true")
        alertMcdSw = ObjectSwitch(this,
                "SPC MCD",
                "ALERT_NOTIFICATION_MCD$locNum",
                R.string.alert_mcd_switch_text)
        alertMcdSw.isChecked(alertNotificationMcdCurrent == "true")
        alertSwoSw = ObjectSwitch(this,
                "SPC SWO",
                "ALERT_NOTIFICATION_SWO$locNum",
                R.string.alert_swo_switch_text)
        alertSwoSw.isChecked(alertNotificationSwoCurrent == "true")
        alertSpcfwSw = ObjectSwitch(this,
                "SPC FW",
                "ALERT_NOTIFICATION_SPCFW$locNum",
                R.string.alert_spcfw_switch_text)
        alertSpcfwSw.isChecked(alertNotificationSpcfwCurrent == "true")
        alertWpcmpdSw = ObjectSwitch(
                this,
                "WPC MPD",
                "ALERT_NOTIFICATION_WPCMPD$locNum",
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
        hideNonUSNotifications()
    }

    override fun onRestart() {
        val caProv = Utility.readPref(this, "LOCATION_CANADA_PROV", "")
        val caCity = Utility.readPref(this, "LOCATION_CANADA_CITY", "")
        val caId = Utility.readPref(this, "LOCATION_CANADA_ID", "")
        if (caProv != "" || caCity != "" || caId != "") {
            locXEt.setText(resources.getString(R.string.settings_loc_generic_ca_x, caProv))
            locYEt.setText(caId)
            val caLabel = "$caCity, $caProv"
            locLabelEt.setText(caLabel)
            notificationsCanada(true)
            fabSaveLocation()
        }
        afterDelete()
        super.onRestart()
    }

    private fun afterDelete() {
        val alertNotificationCurrent = Utility.readPref(this, "ALERT" + locNum + "_NOTIFICATION", "false")
        val alertNotificationRadarCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_RADAR$locNum", "false")
        val alertCcNotificationCurrent = Utility.readPref(this, "ALERT_CC" + locNum + "_NOTIFICATION", "false")
        val alert7Day1NotificationCurrent = Utility.readPref(this, "ALERT_7DAY_" + locNum + "_NOTIFICATION", "false")
        val alertNotificationSoundCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_SOUND$locNum", "false")
        val alertNotificationMcdCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_MCD$locNum", "false")
        val alertNotificationSwoCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_SWO$locNum", "false")
        val alertNotificationSpcfwCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_SPCFW$locNum", "false")
        val alertNotificationWpcmpdCurrent = Utility.readPref(this, "ALERT_NOTIFICATION_WPCMPD$locNum", "false")
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

    private fun saveLocation(locNum: String, xStr: String, yStr: String, labelStr: String) {
        FutureText2(
                this,
                { Location.save(this, locNum, xStr, yStr, labelStr) })
                { toastStr ->
                    showMessage(toastStr)
                    updateTitle = true
                    updateSubTitle()
                    if (xStr.startsWith("CANADA:")) {
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
        val inflater = menuInflater
        inflater.inflate(R.menu.settings_location_generic, menu)
        val searchView = menu.findItem(R.id.ab_search).actionView as ArrayAdapterSearchView
//        UtilityCitiesExtended.create(this)
//        UtilityCitiesCanada.initialize()
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
                locLabelEt.setText(cityArrayAdapter.getItem(position))
                locXEt.setText(UtilityCitiesExtended.cityLat[k].toString())
                val longitudeLabel = "-" + UtilityCitiesExtended.cityLon[k].toString()
                locYEt.setText(longitudeLabel)
                val searchViewLocal = menuLocal!!.findItem(R.id.ab_search).actionView as SearchView
                searchViewLocal.onActionViewCollapsed()
                searchViewLocal.isIconified = true
                val xStr = locXEt.text.toString()
                val yStr = locYEt.text.toString()
                val labelStr = locLabelEt.text.toString()
                saveLocation(locNum, xStr, yStr, labelStr)
            }
        }

        searchView.setOnQueryTextListener(object : OnQueryTextListener {

            override fun onQueryTextChange(newText: String) = false

            override fun onQueryTextSubmit(query: String): Boolean {
                locLabelEt.setText(query)
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
        Location.delete(this, locNum)
        afterDelete()
        showMessage("Deleted location: $locNum ($locLabelCurrent)")
        UtilityWXJobService.startService(this)
        var locNumIntCurrent = Location.numLocations
        locNumIntCurrent += 1
        locNumToSaveStr = locNumIntCurrent.toString()
        locNum = locNumToSaveStr
        locLabelCurrent = "Location $locNumToSaveStr"
        //
        // needed to prevent old location(deleted) data from showing up when adding new
        //
        locXStr = ""
        locYStr = ""
        locXEt.setText(locXStr)
        locYEt.setText(locYStr)
        locLabelEt.setText(locLabelCurrent)
        title = locLabelCurrent
        afterDelete()
        finish()
    }

    private fun showMap() {
        val xStr = locXEt.text.toString()
        val yStr = locYEt.text.toString()
        if (xStr.isNotEmpty() && yStr.isNotEmpty()) {
            if (Location.us(xStr)) {
                Route.webView(this, arrayOf(UtilityMap.getUrl(xStr, yStr, "9"), Location.name))
            } else {
                val addressForMap = locLabelEt.text.toString()
                Route.webView(this, arrayOf(UtilityMap.getUrlFromAddress(addressForMap), Location.name))
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
        if (Build.VERSION.SDK_INT < 23) {
            val xy = UtilityLocation.getGps(this)
            locXEt.setText(xy[0].toString())
            locYEt.setText(xy[1].toString())
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val xy = UtilityLocation.getGps(this)
                locXEt.setText(xy[0].toString())
                locYEt.setText(xy[1].toString())
                fabSaveLocation()
            } else {
                // The ACCESS_FINE_LOCATION is denied, then I request it and manage the result in
                // onRequestPermissionsResult() using the constant myPermissionAccessFineLocation
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), myPermissionAccessFineLocation)
                }
            }
        }
    }

    private val myPermissionAccessFineLocation = 5001

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            myPermissionAccessFineLocation -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val xy = UtilityLocation.getGps(this)
                locXEt.setText(xy[0].toString())
                locYEt.setText(xy[1].toString())
            }
        }
    }

    private fun notificationsCanada(hide: Boolean) {
        var visibility = View.VISIBLE
        if (hide) {
            visibility = View.GONE
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
        val label = locXEt.text.toString()
        if (label.contains("CANADA")) {
            notificationsCanada(true)
        }
    }

    private fun updateSubTitle() {
        val subTitleString = "WFO: " + Utility.readPref(this, "NWS$locNum", "") + " - Nexrad: " + Utility.readPref(this, "RID$locNum", "")
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

    // FIXME variable naming throughout
    private fun fabSaveLocation() {
        val xStr = locXEt.text.toString()
        val yStr = locYEt.text.toString()
        val labelStr = locLabelEt.text.toString()
        saveLocation(locNum, xStr, yStr, labelStr)
    }

    private fun openCanadaMap(s: String) {
        Route(this, SettingsLocationCanadaMapActivity::class.java, SettingsLocationCanadaMapActivity.URL, arrayOf(s))
    }

    private fun showMessage(string: String) {
        ObjectPopupMessage(this, rl, string)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_G -> {
                if (event.isCtrlPressed) actionGps()
                true
            }
            KeyEvent.KEYCODE_M -> {
                if (event.isCtrlPressed) toolbarBottom.showOverflowMenu()
                true
            }
            KeyEvent.KEYCODE_SLASH -> {
                if (event.isAltPressed) ObjectDialogue(this, Utility.showLocationEditShortCuts())
                true
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }
}

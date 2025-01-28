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

package joshuatee.wx.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.FutureText2
import joshuatee.wx.objects.Route
import joshuatee.wx.radar.CitiesExtended
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.Card
import joshuatee.wx.ui.FabExtended
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.PopupMessage
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityMap

class SettingsLocationGenericActivity : BaseActivity(), OnMenuItemClickListener {

    //
    // manual interface for searching and saving a location
    //
    // arg1 location number
    //

    companion object {
        const val LOC_NUM = ""
    }

    // This controls whether or not the title should update based on
    // If this is a location edit (show)
    // This is a new location (don't show)
    // This is a new location that was then saved (show)
    private var locationNumber = ""
    private var menuLocal: Menu? = null
    private lateinit var editTextLabel: EditText
    private lateinit var editTextLat: EditText
    private lateinit var editTextLon: EditText
    private lateinit var box: VBox
    private lateinit var relativeLayout: RelativeLayout
    private lateinit var notifText: AppCompatTextView
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var settingsLocationGenericSwitches: SettingsLocationGenericSwitches

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_settings_location_generic,
            R.menu.settings_location_generic_bottom,
            true
        )
        locationNumber = intent.getStringArrayExtra(LOC_NUM)!![0]
        setupUI()
        initializeDataStructures()
        settingsLocationGenericSwitches =
            SettingsLocationGenericSwitches(this, box, locationNumber, editTextLabel)
        updateSubTitle()
        adjustForWhiteTheme()
        hideNonUSNotifications()
    }

    private fun setupUI() {
        title = "Location $locationNumber" // TODO FIXME move into updateSubTitle and rename method
        editTextLabel = findViewById(R.id.locLabelEt)
        editTextLat = findViewById(R.id.locXEt)
        editTextLon = findViewById(R.id.locYEt)
        box = VBox.fromResource(this)
        relativeLayout = findViewById(R.id.rl)
        notifText = findViewById(R.id.notif_text_perm)
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
        objectToolbarBottom.connect(this)
        FabExtended(this, R.id.fab, GlobalVariables.ICON_DONE, "Save Location") { saveLocation() }
        Card(this, R.id.cv1)
    }

    private fun adjustNotificationLabel() {
        notifText.visibility = View.GONE
        val notificationManagerCompat = NotificationManagerCompat.from(this)
        if (!notificationManagerCompat.areNotificationsEnabled()) {
            notifText.visibility = View.VISIBLE
            notifText.setOnClickListener {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        }
    }

    private fun initializeDataStructures() {
        objectToolbarBottom.find(R.id.action_delete).isVisible = Location.numLocations > 1
        CitiesExtended.create(this)
        Utility.writePref(this, "LOCATION_CANADA_PROV", "")
        Utility.writePref(this, "LOCATION_CANADA_CITY", "")
        Utility.writePref(this, "LOCATION_CANADA_ID", "")
        editTextLat.setText(Utility.readPref(this, "LOC" + locationNumber + "_X", ""))
        editTextLon.setText(Utility.readPref(this, "LOC" + locationNumber + "_Y", ""))
        editTextLabel.setText(Utility.readPref(this, "LOC" + locationNumber + "_LABEL", ""))
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

    override fun onResume() {
        super.onResume()
        adjustNotificationLabel()
    }

    private fun saveLocation() {
        val lat = editTextLat.text.toString()
        val lon = editTextLon.text.toString()
        val label = editTextLabel.text.toString()
        FutureText2(
            { Location.save(this, locationNumber, lat, lon, label) })
        { saveStatus ->
            PopupMessage(relativeLayout, saveStatus, PopupMessage.SHORT)
            updateSubTitle()
            if (lat.startsWith("CANADA:")) {
                settingsLocationGenericSwitches.notificationsCanada(true)
            } else {
                settingsLocationGenericSwitches.notificationsCanada(false)
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
        val combinedCitiesList = CitiesExtended.labels.toList()
        val cityArrayAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, combinedCitiesList)
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
            if (k < CitiesExtended.labels.size) {
                searchView.setText(cityArrayAdapter.getItem(position)!!)
                editTextLabel.setText(cityArrayAdapter.getItem(position))
                editTextLat.setText(CitiesExtended.lat[k].toString())
                val lonString = "-" + CitiesExtended.lon[k].toString()
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
//        val autoCompleteTextView: SearchView.SearchAutoComplete = searchView.findViewById(androidx.constraintlayout.widget.R.id.search_src_text)
        val autoCompleteTextView =
            searchView.findViewById<AutoCompleteTextView>(androidx.constraintlayout.widget.R.id.search_src_text)
        when {
            UIPreferences.themeIsWhite -> {
                autoCompleteTextView.setDropDownBackgroundResource(R.drawable.dr_white)
            }

            UtilityUI.isThemeAllBlack() -> {
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
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> delete()
            R.id.action_map -> showMap()
            R.id.action_help -> ObjectDialogue(
                this,
                resources.getString(R.string.activity_settings_generic_help)
            )

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
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val xy = UtilityLocation.getGps(this)
            editTextLat.setText(xy[0].toString())
            editTextLon.setText(xy[1].toString())
            saveLocation()
        } else {
            // The ACCESS_FINE_LOCATION is denied, then I request it and manage the result in
            // onRequestPermissionsResult() using the constant myPermissionAccessFineLocation
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    myPermissionAccessFineLocation
                )
            }
        }
    }

    private val myPermissionAccessFineLocation = 5001

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            myPermissionAccessFineLocation -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val xy = UtilityLocation.getGps(this)
                editTextLat.setText(xy[0].toString())
                editTextLon.setText(xy[1].toString())
            }
        }
    }

    private fun hideNonUSNotifications() {
        val label = editTextLat.text.toString()
        if (label.contains("CANADA")) {
            settingsLocationGenericSwitches.notificationsCanada(true)
        }
    }

    private fun updateSubTitle() {
        val subTitleString = "WFO: " +
                Utility.readPref(this, "NWS$locationNumber", "") +
                " - Nexrad: " + Utility.readPref(this, "RID$locationNumber", "")
        if (subTitleString != "WFO:  - Nexrad: " && (To.int(locationNumber) != Location.numLocations + 1)) {
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

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_G -> if (event.isCtrlPressed) actionGps()
            KeyEvent.KEYCODE_M -> if (event.isCtrlPressed) toolbarBottom.showOverflowMenu()
            KeyEvent.KEYCODE_SLASH -> if (event.isAltPressed) ObjectDialogue(
                this,
                Utility.showLocationEditShortCuts()
            )

            else -> super.onKeyUp(keyCode, event)
        }
        return true
    }
}

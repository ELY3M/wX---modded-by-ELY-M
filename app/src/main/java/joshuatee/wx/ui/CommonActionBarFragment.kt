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

package joshuatee.wx.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.R
import joshuatee.wx.activitiesmisc.*
import joshuatee.wx.audio.SettingsPlaylistActivity
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.audio.UtilityVoiceCommand
import joshuatee.wx.canada.*
import joshuatee.wx.objects.Route
import joshuatee.wx.settings.*
import joshuatee.wx.settings.UtilityHomeScreen

open class CommonActionBarFragment : AppCompatActivity(), OnMenuItemClickListener {

    //
    // All activities that need the common action bad extend this activity
    // Provides access to vis, nexrad, AFD, hourly, radar mosaic, map of statewide alerts, observations
    // settings, and about
    //

    protected lateinit var view: View

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.cab, menu)
        return true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
	    /*
	    //elys mod - not removing this - ELY M. 
            R.id.action_forecast_webpage -> Route(
                this,
                WebView::class.java,
                WebView.URL,
                arrayOf(
                    "http://forecast.weather.gov/MapClick.php?lon=" + Location.latLon.lonString + "&lat=" + Location.latLon.latString,
                    "Local forecast"
                )
            )
	    */
            R.id.action_alert -> {
                if (Location.isUS) {
                    Route.usAlerts(this)
                } else {
                    Route(this, CanadaAlertsActivity::class.java)
                }
            }
            R.id.action_observations -> Route.observations(this)
            R.id.action_playlist -> Route(this, SettingsPlaylistActivity::class.java)
            R.id.action_soundings -> if (Location.isUS) {
                Route.sounding(this)
            }
            R.id.action_cloud -> openVis()
            R.id.action_radar -> openNexradRadar(this)
            R.id.action_forecast -> openHourly()
            R.id.action_afd -> openAfd()
            R.id.action_dashboard -> openDashboard()
            R.id.action_spotters -> Route(this, SpottersActivity::class.java)
            R.id.action_settings -> openSettings()
            R.id.action_radar_mosaic -> Route.radarMosaic(this)
            R.id.action_vr -> {
                if (UtilityTts.mediaPlayer != null && UtilityTts.mediaPlayer!!.isPlaying) {
                    UtilityTts.mediaPlayer!!.stop()
                    UtilityTts.ttsIsPaused = true
                }
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US")
                try {
                    startForResult.launch(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show()
                }
            }
            //elys mod - not removing about - ELY M.
            R.id.action_about -> Route(this, SettingsAboutActivity::class.java)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val thingsYouSaid = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ObjectPopupMessage(view, thingsYouSaid!![0])
            val string = thingsYouSaid[0]
            UtilityVoiceCommand.processCommand(this, string, Location.rid, Location.wfo, Location.state)
        }
    }

    fun openNexradRadar(context: Context) {
        if (!UIPreferences.dualpaneRadarIcon) {
            Route.radar(context, arrayOf(Location.rid, ""))
        } else {
            Route.radarMultiPane(context, arrayOf(Location.rid, "", "2"))
        }
    }

    fun openAfd() {
        Route.wfoText(this)
    }

    fun openSettings() {
        Route.settings(this)
    }

    fun openVis() {
        Route.vis(this)
    }

    fun openDashboard() {
        if (Location.isUS) {
            Route.severeDash(this)
        } else {
            Route(this, CanadaAlertsActivity::class.java)
        }
    }

    fun openHourly() {
        Route.hourly(this)
    }

    fun openActivity(context: Context, activityName: String) {
        Route(context,
                UtilityHomeScreen.HM_CLASS[activityName]!!,
                UtilityHomeScreen.HM_CLASS_ID[activityName]!!,
                UtilityHomeScreen.HM_CLASS_ARGS[activityName]!!)
    }
}

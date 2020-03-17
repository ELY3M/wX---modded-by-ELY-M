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

package joshuatee.wx

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.activitiesmisc.*
import joshuatee.wx.audio.SettingsPlaylistActivity
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.audio.UtilityVoiceCommand
import joshuatee.wx.canada.*
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.AwcRadarMosaicActivity
import joshuatee.wx.radar.USNwsMosaicActivity
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.radar.WXGLRadarActivityMultiPane
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.SettingsMainActivity
import joshuatee.wx.spc.SpcSoundingsActivity
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility
import joshuatee.wx.vis.GoesActivity
import joshuatee.wx.settings.SettingsAboutActivity

open class CommonActionBarFragment : AppCompatActivity(), OnMenuItemClickListener {

    //
    // All activities that need the common action bad extend this activity
    // Provides access to vis, nexrad, AFD, hourly, radar mosaic, map of statewide alerts, observations
    // settings, and about
    //

    private val requestOk = 1
    lateinit var view: View

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.cab, menu)
        return true
    }

    /*override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.action_alert)
        if (Location.isUS) {
            item.title = "US Alerts"
        } else {
            item.title = "Canadian Alerts"
        }
        return super.onPrepareOptionsMenu(menu)
    }*/

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
	    //not removing this - ELY M. 
            R.id.action_forecast_webpage -> ObjectIntent(
                this,
                WebView::class.java,
                WebView.URL,
                arrayOf(
                    "http://forecast.weather.gov/MapClick.php?lon=" + Location.latLon.lonString + "&lat=" + Location.latLon.latString,
                    "Local forecast"
                )
            )
            R.id.action_alert -> {
                if (Location.isUS) {
                    ObjectIntent(
                            this,
                            USWarningsWithRadarActivity::class.java,
                            USWarningsWithRadarActivity.URL,
                            arrayOf(
                                    ".*?Tornado Warning.*?|.*?Severe Thunderstorm Warning.*?|.*?Flash Flood Warning.*?",
                                    "us"
                            )
                    )
                } else {
                    ObjectIntent(this, CanadaAlertsActivity::class.java)
                }
            }
            R.id.action_observations -> {
                if (Location.isUS) {
                    ObjectIntent(
                            this,
                            ImageCollectionActivity::class.java,
                            ImageCollectionActivity.TYPE,
                            arrayOf("OBSERVATIONS")
                    )
                } else {
                    ObjectIntent(
                            this,
                            ImageShowActivity::class.java,
                            ImageShowActivity.URL,
                            arrayOf(
                                    "http://weather.gc.ca/data/wxoimages/wocanmap0_e.jpg",
                                    "Observations"
                            )
                    )
                }
            }
            R.id.action_playlist -> {
                ObjectIntent(this, SettingsPlaylistActivity::class.java)
            }
            R.id.action_soundings -> {
                if (Location.isUS) ObjectIntent(
                        this,
                        SpcSoundingsActivity::class.java,
                        SpcSoundingsActivity.URL,
                        arrayOf(Location.wfo, "")
                )
            }
            R.id.action_cloud -> openVis()
            R.id.action_radar -> openNexradRadar(this)
            R.id.action_forecast -> openHourly()
            R.id.action_afd -> openAfd()
            R.id.action_dashboard -> openDashboard()
            R.id.action_spotters -> ObjectIntent(this, SpottersActivity::class.java)
            R.id.action_settings -> openSettings()
            R.id.action_radar_mosaic -> {
                if (Location.isUS) {
                    if (!UIPreferences.useAwcRadarMosaic) {
                        ObjectIntent(
                                this,
                                USNwsMosaicActivity::class.java,
                                USNwsMosaicActivity.URL,
                                arrayOf("location")
                        )
                    } else {
                        ObjectIntent(
                                this,
                                AwcRadarMosaicActivity::class.java,
                                AwcRadarMosaicActivity.URL,
                                arrayOf("")
                        )
                    }
                } else {
                    val prov = Utility.readPref(
                            this,
                            "NWS" + Location.currentLocationStr + "_STATE",
                            ""
                    )
                    ObjectIntent(
                            this,
                            CanadaRadarActivity::class.java,
                            CanadaRadarActivity.RID,
                            arrayOf(UtilityCanada.getECSectorFromProv(prov), "rad")
                    )
                }
            }
            R.id.action_vr -> {
                if (UtilityTts.mMediaPlayer != null && UtilityTts.mMediaPlayer!!.isPlaying) {
                    UtilityTts.mMediaPlayer!!.stop()
                    UtilityTts.ttsIsPaused = true
                }
                val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US")
                try {
                    startActivityForResult(i, requestOk)
                } catch (e: Exception) {
                    Toast.makeText(
                            this,
                            "Error initializing speech to text engine.",
                            Toast.LENGTH_LONG
                    ).show()
                }
            }
            //not removing about - ELY M.
            R.id.action_about -> ObjectIntent(this, SettingsAboutActivity::class.java)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestOk && resultCode == Activity.RESULT_OK) {
            val thingsYouSaid = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            UtilityUI.makeSnackBar(view, thingsYouSaid!![0])
            val addrStrTmp = thingsYouSaid[0]
            UtilityVoiceCommand.processCommand(
                    this,
                    view,
                    addrStrTmp,
                    Location.rid,
                    Location.wfo,
                    Location.state
            )
        }
    }

    fun openNexradRadar(context: Context) {
        if (Location.isUS) {
            if (!UIPreferences.dualpaneRadarIcon) {
                ObjectIntent(
                        context,
                        WXGLRadarActivity::class.java,
                        WXGLRadarActivity.RID,
                        arrayOf(Location.rid, "")
                )
            } else {
                ObjectIntent(
                        context,
                        WXGLRadarActivityMultiPane::class.java,
                        WXGLRadarActivityMultiPane.RID,
                        arrayOf(Location.rid, "", "2")
                )
            }
        } else {
            ObjectIntent(
                    context,
                    CanadaRadarActivity::class.java,
                    CanadaRadarActivity.RID,
                    arrayOf(Location.rid, "rad")
            )
        }
    }

    fun openAfd() {
        if (Location.isUS) {
            ObjectIntent(
                    this,
                    AfdActivity::class.java,
                    AfdActivity.URL,
                    arrayOf(Location.wfo, "")
            )
        } else {
            ObjectIntent(this, CanadaTextActivity::class.java)
        }
    }

    fun openSettings() {
        ObjectIntent(this, SettingsMainActivity::class.java)
    }

    fun openVis() {
        if (Location.isUS) {
            ObjectIntent(this, GoesActivity::class.java, GoesActivity.RID, arrayOf(""))
        } else {
            ObjectIntent(
                    this,
                    CanadaRadarActivity::class.java,
                    CanadaRadarActivity.RID,
                    arrayOf(Location.rid, "vis")
            )
        }
    }

    fun openDashboard() {
        if (Location.isUS) {
            ObjectIntent(this, SevereDashboardActivity::class.java)
        } else {
            ObjectIntent(this, CanadaAlertsActivity::class.java)
        }
    }

    fun openHourly() {
        if (Location.isUS) {
            ObjectIntent(
                    this,
                    HourlyActivity::class.java,
                    HourlyActivity.LOC_NUM,
                    Location.currentLocationStr
            )
        } else {
            ObjectIntent(
                    this,
                    CanadaHourlyActivity::class.java,
                    CanadaHourlyActivity.LOC_NUM,
                    Location.currentLocationStr
            )
        }
    }

    fun openActivity(context: Context, activityName: String) {
        ObjectIntent(
                context,
                MyApplication.HM_CLASS[activityName]!!,
                MyApplication.HM_CLASS_ID[activityName]!!,
                MyApplication.HM_CLASS_ARGS[activityName]!!
        )
    }
}

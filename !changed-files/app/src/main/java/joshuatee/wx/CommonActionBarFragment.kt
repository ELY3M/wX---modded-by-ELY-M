/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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

package joshuatee.wx

import android.app.Activity
import android.content.Context
import java.util.Locale
import android.speech.RecognizerIntent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import joshuatee.wx.activitiesmisc.*

import joshuatee.wx.radar.USNWSMosaicActivity
import joshuatee.wx.audio.UtilityTTS
import joshuatee.wx.audio.UtilityVoiceCommand
import joshuatee.wx.canada.CanadaAlertsActivity
import joshuatee.wx.canada.CanadaHourlyActivity
import joshuatee.wx.canada.CanadaRadarActivity
import joshuatee.wx.canada.CanadaTextActivity
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.radar.WXGLRadarActivityMultiPane
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.SettingsMainActivity
import joshuatee.wx.audio.SettingsPlaylistActivity
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.spc.SPCSoundingsActivity
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityAlertDialog
import joshuatee.wx.vis.GOES16Activity

open class CommonActionBarFragment : AppCompatActivity(), OnMenuItemClickListener {

    // All activities that need the common action bad extend this activity
    // Provides access to vis, nexrad, AFD, hourly, rad mosiac, map of statewide alerts, observations
    // settings, and about
    //

    private val requestOk = 1
    protected lateinit var helpMi: MenuItem
    protected val helpStr: String = "Help is on"
    lateinit var view: View

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.cab, menu)
        helpMi = menu.findItem(R.id.action_help)
        if (MyApplication.helpMode) helpMi.title = helpStr
        return true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_help -> {
                if (MyApplication.helpMode) {
                    MyApplication.helpMode = false
                    UtilityUI.makeSnackBar(view, "Help mode is now disabled. Select again to turn on.")
                    helpMi.title = "Help"
                } else {
                    MyApplication.helpMode = true
                    UtilityUI.makeSnackBar(view, "Help mode is now enabled. Select again to turn off. Tap any icon to see help text.")
                    helpMi.title = helpStr
                }
            }
            R.id.action_forecast_webpage -> ObjectIntent(this, WebscreenABModels::class.java, WebscreenABModels.URL, arrayOf("http://forecast.weather.gov/MapClick.php?lon=" + Location.latLon.lonString + "&lat=" + Location.latLon.latString, "Local forecast"))
            R.id.action_alert -> {
                if (MyApplication.helpMode) {
                    showHelpCAB(item.itemId)
                } else {
                    if (Location.isUS) {
                        ObjectIntent(this, USWarningsWithRadarActivity::class.java, USWarningsWithRadarActivity.URL, arrayOf(".*?Tornado Warning.*?|.*?Severe Thunderstorm Warning.*?|.*?Flash Flood Warning.*?|.*?Special Marine Warning.*?|.*?Severe Weather Statement.*?|.*?Special Weather Statement.*?", "us"))
                    } else {
                        ObjectIntent(this, CanadaAlertsActivity::class.java)
                    }
                }
            }
            R.id.action_observations -> {
                if (MyApplication.helpMode) {
                    showHelpCAB(item.itemId)
                } else {
                    if (Location.isUS) {
                        ObjectIntent(this, ObservationsActivity::class.java, ObservationsActivity.LOC, arrayOf(Location.state.toLowerCase(Locale.US)))
                    } else {
                        ObjectIntent(this, ImageShowActivity::class.java, ImageShowActivity.URL, arrayOf("http://weather.gc.ca/data/wxoimages/wocanmap0_e.jpg", "Observations"))
                    }
                }
            }
            R.id.action_playlist -> {
                if (MyApplication.helpMode) {
                    showHelpCAB(item.itemId)
                } else {
                    ObjectIntent(this, SettingsPlaylistActivity::class.java)
                }
            }
            R.id.action_soundings -> {
                if (MyApplication.helpMode) {
                    showHelpCAB(item.itemId)
                } else {
                    if (Location.isUS) ObjectIntent(this, SPCSoundingsActivity::class.java, SPCSoundingsActivity.URL, arrayOf(Location.wfo, ""))
                }
            }
            R.id.action_cloud -> openVis(item.itemId)
            R.id.action_radar -> openNexradRadar(this, item.itemId)
            R.id.action_forecast -> {
                if (MyApplication.helpMode) {
                    showHelpCAB(item.itemId)
                } else {
                    if (Location.isUS) {
                        ObjectIntent(this, HourlyActivity::class.java, HourlyActivity.LOC_NUM, Location.currentLocationStr)
                    } else {
                        ObjectIntent(this, CanadaHourlyActivity::class.java, CanadaHourlyActivity.LOC_NUM, Location.currentLocationStr)
                    }
                }
            }
            R.id.action_afd -> openAfd(item.itemId)
            R.id.action_dashboard -> openDashboard(item.itemId)
            R.id.action_spotters -> ObjectIntent(this, SpottersActivity::class.java)
            R.id.action_settings -> openSettings(item.itemId)
            R.id.action_radar_mosaic -> {
                if (MyApplication.helpMode) {
                    showHelpCAB(item.itemId)
                } else {
                    if (Location.isUS) {
                        ObjectIntent(this, USNWSMosaicActivity::class.java, USNWSMosaicActivity.URL, arrayOf("location"))
                    } else {
                        val prov = Utility.readPref(this, "NWS" + Location.currentLocationStr + "_STATE", "")
                        ObjectIntent(this, CanadaRadarActivity::class.java, CanadaRadarActivity.RID, arrayOf(UtilityCanada.getECSectorFromProv(prov), "rad"))
                    }
                }
            }
            R.id.action_vr -> {
                if (MyApplication.helpMode) {
                    showHelpCAB(item.itemId)
                } else {
                    if (UtilityTTS.mMediaPlayer != null && UtilityTTS.mMediaPlayer!!.isPlaying) {
                        UtilityTTS.mMediaPlayer!!.stop()
                        UtilityTTS.ttsIsPaused = true
                    }
                    val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                    i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US")
                    try {
                        startActivityForResult(i, requestOk)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error initializing speech to text engine.", Toast.LENGTH_LONG).show()
                    }
                }
            }
            R.id.action_about -> UtilityAlertDialog.showVersion(this, this)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun showHelpCAB(helpItem: Int) {
        when (helpItem) {
            R.id.action_alert -> showHelpTextCAB(resources.getString(R.string.help_uswarn))
            R.id.action_cloud -> showHelpTextCAB(resources.getString(R.string.help_cloud))
            R.id.action_radar -> showHelpTextCAB(resources.getString(R.string.help_radar))
            R.id.action_afd -> showHelpTextCAB(resources.getString(R.string.help_afd))
            R.id.action_dashboard -> showHelpTextCAB(resources.getString(R.string.help_severe_dashboard))
            R.id.action_forecast -> showHelpTextCAB(resources.getString(R.string.help_hourly_forecast))
            R.id.action_settings -> showHelpTextCAB(resources.getString(R.string.help_settings))
            R.id.action_radar_mosaic -> showHelpTextCAB(resources.getString(R.string.help_goes_radar_mosaic))
            R.id.action_vr -> showHelpTextCAB(resources.getString(R.string.help_vr))
            R.id.action_playlist -> showHelpTextCAB(resources.getString(R.string.help_playlist))
            R.id.action_soundings -> showHelpTextCAB(resources.getString(R.string.help_soundings))
        }
    }

    private fun showHelpTextCAB(helpStr: String) = UtilityAlertDialog.showHelpText(helpStr, this)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestOk && resultCode == Activity.RESULT_OK) {
            val thingsYouSaid = data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            UtilityUI.makeSnackBar(view, thingsYouSaid[0])
            val addrStrTmp = thingsYouSaid[0]
            UtilityVoiceCommand.processCommand(this, view, addrStrTmp, Location.rid, Location.wfo, Location.state)
        }
    }

    fun openNexradRadar(context: Context, itemID: Int) {
        if (MyApplication.helpMode) {
            showHelpCAB(itemID)
        } else {
            if (Location.isUS) {
                if (!UIPreferences.dualpaneRadarIcon) {
                    ObjectIntent(context, WXGLRadarActivity::class.java, WXGLRadarActivity.RID, arrayOf(Location.rid, ""))
                } else {
                    ObjectIntent(context, WXGLRadarActivityMultiPane::class.java, WXGLRadarActivityMultiPane.RID, arrayOf(Location.rid, "", "2"))
                }
            } else {
                ObjectIntent(context, CanadaRadarActivity::class.java, CanadaRadarActivity.RID, arrayOf(Location.rid, "rad"))
            }
        }
    }

    fun openAfd(itemID: Int) {
        if (MyApplication.helpMode) {
            showHelpCAB(itemID)
        } else {
            if (Location.isUS) {
                ObjectIntent(this, AFDActivity::class.java, AFDActivity.URL, arrayOf(Location.wfo, ""))
            } else {
                ObjectIntent(this, CanadaTextActivity::class.java)
            }
        }
    }

    fun openSettings(itemID: Int) {
        if (MyApplication.helpMode) {
            showHelpCAB(itemID)
        } else {
            ObjectIntent(this, SettingsMainActivity::class.java)
        }
    }

    fun openVis(itemID: Int) {
        if (MyApplication.helpMode) {
            showHelpCAB(itemID)
        } else {
            if (Location.isUS) {
                ObjectIntent(this, GOES16Activity::class.java, GOES16Activity.RID, arrayOf(""))
            } else {
                ObjectIntent(this, CanadaRadarActivity::class.java, CanadaRadarActivity.RID, arrayOf(Location.rid, "vis"))
            }
        }
    }

    fun openDashboard(itemID: Int) {
        if (MyApplication.helpMode) {
            showHelpCAB(itemID)
        } else {
            if (Location.isUS) {
                ObjectIntent(this, SevereDashboardActivity::class.java)
            } else {
                ObjectIntent(this, CanadaAlertsActivity::class.java)
            }
        }
    }
}
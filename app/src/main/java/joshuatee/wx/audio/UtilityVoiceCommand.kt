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

package joshuatee.wx.audio

import java.util.Locale

import android.content.Context
import android.view.View

import joshuatee.wx.settings.Location
import joshuatee.wx.spc.SpcMesoActivity
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.util.Utility
import joshuatee.wx.wpc.WpcTextProductsActivity

object UtilityVoiceCommand {

    fun processCommand(context: Context, view: View, vrStringOriginal: String, radarSiteArg: String, wfoOriginal: String, state: String): Boolean {
        var vrString = vrStringOriginal
        var radarSite = radarSiteArg
        var wfo = wfoOriginal
        var gotHit = true
        val tokens = MyApplication.space.split(vrString)
        if (vrString.contains("radar")) {
            var validRid = true
            if (tokens.size > 1) {
                radarSite = tokens[1].toUpperCase(Locale.US)
                if (Utility.getRadarSiteName(radarSite) == "") {
                    validRid = false
                }
            }
            if (validRid) ObjectIntent.showRadar(context, arrayOf(radarSite, state))
        } else if (vrString.contains("AFD") || vrString.contains("text")) {
            if (tokens.size > 1) {
                wfo = tokens[1].toUpperCase(Locale.US)
            }
            if (wfo == "WPC") {
                ObjectIntent(context, WpcTextProductsActivity::class.java, WpcTextProductsActivity.URL, arrayOf("pmdspd", "Short Range Forecast Discussion", "sound"))
            } else {
                ObjectIntent.showWfoText(context, arrayOf(wfo, "AFD", "sound"))
            }
        } else if (vrString.contains("cloud")) {
            ObjectIntent.showVis(context)
        } else if (vrString.toUpperCase(Locale.US).contains("SPC")) {
            if (tokens.size > 1) {
                when {
                    vrString.contains("1") -> ObjectIntent.showSpcSwo(context, arrayOf("1", "sound"))
                    vrString.contains("2") -> ObjectIntent.showSpcSwo(context, arrayOf("2", "sound"))
                    vrString.contains("3") -> ObjectIntent.showSpcSwo(context, arrayOf("3", "sound"))
                    else -> ObjectIntent.showSpcSwo(context, arrayOf("4-8", "sound"))
                }
            } else {
                ObjectIntent.showSpcSwo(context, arrayOf("1", "sound"))
            }
        } else if (vrString.contains("day one")) {
            ObjectIntent.showSpcSwo(context, arrayOf("1", "sound"))
        } else if (vrString.contains("day 2")) {
            ObjectIntent.showSpcSwo(context, arrayOf("2", "sound"))
        } else if (vrString.contains("day 3")) {
            ObjectIntent.showSpcSwo(context, arrayOf("3", "sound"))
        } else if (vrString.contains("day 4")) {
            ObjectIntent.showSpcSwo(context, arrayOf("4-8", "sound"))
        } else if (vrString.contains("add")) {
            vrString = vrString.replace("add location", "")
            ObjectIntent.showLocationEdit(context, arrayOf((Location.numLocations + 1).toString(), vrString))
        } else if (vrString.contains("edit")) {
            vrString = vrString.replace("edit location", "")
            ObjectIntent.showLocationEdit(context, arrayOf(Location.currentLocationStr, vrString))
        } else if (vrString.contains("mosaic")) {
            ObjectIntent.showRadarMosaic(context)
        } else if (vrString.contains("map")) {
            if (tokens.size > 1) {
                when (tokens[1]) {
                    "pressure" -> {
                        Utility.writePref(context, "SPCMESO_LAST_USED", "pmsl")
                        Utility.writePref(context, "SPCMESO_LAST_USED_LABEL", "MSL Pressure/Wind")
                    }
                    "500" -> {
                        Utility.writePref(context, "SPCMESO_LAST_USED", "500mb")
                        Utility.writePref(context, "SPCMESO_LAST_USED_LABEL", "500mb Analysis")
                    }
                    "cape" -> {
                        Utility.writePref(context, "SPCMESO_LAST_USED", "mucp")
                        Utility.writePref(context, "SPCMESO_LAST_USED_LABEL", "MUCAPE / LPL Height")
                    }
                    "supercell" -> {
                        Utility.writePref(context, "SPCMESO_LAST_USED", "scp")
                        Utility.writePref(context, "SPCMESO_LAST_USED_LABEL", "Supercell Composite")
                    }
                    "wind" -> {
                        Utility.writePref(context, "SPCMESO_LAST_USED", "eshr")
                        Utility.writePref(context, "SPCMESO_LAST_USED_LABEL", "Effective Shear")
                    }
                    else -> {
                        Utility.writePref(context, "SPCMESO_LAST_USED", "pmsl")
                        Utility.writePref(context, "SPCMESO_LAST_USED_LABEL", "MSL Pressure/Wind")
                    }
                }
            }
            ObjectIntent(context, SpcMesoActivity::class.java, SpcMesoActivity.INFO, arrayOf("", "1", "SPCMESO"))
        } else if (vrString.contains("forecast")) {
            val forecast = Utility.readPref(context, "FCST", "")
            UtilityTts.synthesizeTextAndPlay(context, forecast, "7day")
        } else if (vrString.contains("download playlist")) {
            UtilityUI.makeSnackBar(view, "Download initiated")
            ObjectIntent(context, DownloadPlaylistService::class.java, DownloadPlaylistService.URL, "true")
        } else if (vrString.contains("playlist")) {
            UtilityTts.synthesizeTextAndPlayPlaylist(context, 1)
        } else {
            gotHit = false
        }
        return gotHit
    }
}

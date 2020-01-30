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

import joshuatee.wx.activitiesmisc.AfdActivity
import joshuatee.wx.radar.USNwsMosaicActivity
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.spc.SpcMesoActivity
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.MyApplication
import joshuatee.wx.activitiesmisc.WebscreenAB
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.settings.SettingsLocationGenericActivity
import joshuatee.wx.spc.SpcSwoActivity
import joshuatee.wx.util.Utility
import joshuatee.wx.vis.GoesActivity
import joshuatee.wx.wpc.WpcTextProductsActivity

object UtilityVoiceCommand {

    fun processCommand(
            context: Context,
            view: View,
            vrStringF: String,
            radarSiteArg: String,
            nws1CurrentF: String,
            nws1StateCurrent: String
    ): Boolean {
        var vrString = vrStringF
        var radarSite = radarSiteArg
        var nws1Current = nws1CurrentF
        var gotHit = true
        val tokens = MyApplication.space.split(vrString)
        if (vrString.contains("radar")) {
            var validRid = true
            if (tokens.size > 1) {
                radarSite = tokens[1].toUpperCase(Locale.US)
                if (Utility.readPrefWithNull(context, "RID_LOC_$radarSite", null) == null) {
                    validRid = false
                }
            }
            if (validRid) ObjectIntent(
                    context,
                    WXGLRadarActivity::class.java,
                    WXGLRadarActivity.RID,
                    arrayOf(radarSite, nws1StateCurrent)
            )
        } else if (vrString.contains("AFD") || vrString.contains("text")) {
            if (tokens.size > 1) {
                nws1Current = tokens[1].toUpperCase(Locale.US)
            }
            if (nws1Current == "WPC") {
                ObjectIntent(
                        context,
                        WpcTextProductsActivity::class.java,
                        WpcTextProductsActivity.URL,
                        arrayOf("pmdspd", "Short Range Forecast Discussion", "sound")
                )
            } else {
                ObjectIntent(
                        context,
                        AfdActivity::class.java,
                        AfdActivity.URL,
                        arrayOf(nws1Current, "AFD", "sound")
                )
            }
        } else if (vrString.contains("cloud")) {
            ObjectIntent(
                    context,
                    GoesActivity::class.java,
                    GoesActivity.RID,
                    arrayOf("")
            )
        } else if (vrString.toUpperCase(Locale.US).contains("SPC")) {
            if (tokens.size > 1) {
                when {
                    vrString.contains("1") -> ObjectIntent(
                            context,
                            SpcSwoActivity::class.java,
                            SpcSwoActivity.NO,
                            arrayOf("1", "sound")
                    )
                    vrString.contains("2") -> ObjectIntent(
                            context,
                            SpcSwoActivity::class.java,
                            SpcSwoActivity.NO,
                            arrayOf("2", "sound")
                    )
                    vrString.contains("3") -> ObjectIntent(
                            context,
                            SpcSwoActivity::class.java,
                            SpcSwoActivity.NO,
                            arrayOf("3", "sound")
                    )
                    else -> ObjectIntent(
                            context,
                            SpcSwoActivity::class.java,
                            SpcSwoActivity.NO,
                            arrayOf("4-8", "sound")
                    )
                }
            } else {
                ObjectIntent(
                        context,
                        SpcSwoActivity::class.java,
                        SpcSwoActivity.NO,
                        arrayOf("1", "sound")
                )
            }
        } else if (vrString.contains("day one")) {
            ObjectIntent(
                    context,
                    SpcSwoActivity::class.java,
                    SpcSwoActivity.NO,
                    arrayOf("1", "sound")
            )
        } else if (vrString.contains("day 2")) {
            ObjectIntent(
                    context,
                    SpcSwoActivity::class.java,
                    SpcSwoActivity.NO,
                    arrayOf("2", "sound")
            )
        } else if (vrString.contains("day 3")) {
            ObjectIntent(
                    context,
                    SpcSwoActivity::class.java,
                    SpcSwoActivity.NO,
                    arrayOf("3", "sound")
            )
        } else if (vrString.contains("day 4")) {
            ObjectIntent(
                    context,
                    SpcSwoActivity::class.java,
                    SpcSwoActivity.NO,
                    arrayOf("4-8", "sound")
            )
        } else if (vrString.contains("add")) {
            vrString = vrString.replace("add location", "")
            ObjectIntent(
                    context,
                    SettingsLocationGenericActivity::class.java,
                    SettingsLocationGenericActivity.LOC_NUM,
                    arrayOf((Location.numLocations + 1).toString(), vrString)
            )
        } else if (vrString.contains("edit")) {
            vrString = vrString.replace("edit location", "")
            ObjectIntent(
                    context,
                    SettingsLocationGenericActivity::class.java,
                    SettingsLocationGenericActivity.LOC_NUM,
                    arrayOf(Location.currentLocationStr, vrString)
            )
        } else if (vrString.contains("mosaic")) {
            ObjectIntent(
                    context,
                    USNwsMosaicActivity::class.java,
                    USNwsMosaicActivity.URL,
                    arrayOf("")
            )
        } else if (vrString.contains("Twitter")) {
            var stateCodeCurrent: String = Utility.readPref(context, "STATE_CODE", "")
            if (tokens.size > 1) {
                vrString = vrString.replace("Twitter ", "")
                vrString = vrString.replace(" ", "+")
                stateCodeCurrent = Utility.readPref(context, "STATE_LOOKUP_$vrString", "")
            }
            val twitterStateId = Utility.readPref(context, "STATE_TW_ID_$stateCodeCurrent", "")
            ObjectIntent(
                    context,
                    WebscreenAB::class.java,
                    WebscreenAB.URL,
                    arrayOf(
                            "<a class=\"twitter-timeline\" data-dnt=\"true\" href=\"https://twitter.com/search?q=%23" + stateCodeCurrent.toLowerCase(
                                    Locale.US
                            ) + "wx\" data-widget-id=\"" + twitterStateId + "\" data-chrome=\"noscrollbar noheader nofooter noborders  \" data-tweet-limit=20>Tweets about \"#" + stateCodeCurrent.toLowerCase(
                                    Locale.US
                            ) + "wx\"</a><script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+\"://platform.twitter.com/widgets.js\";fjs.parentNode.insertBefore(js,fjs);}}(document,\"script\",\"twitter-wjs\");</script>",
                            "twitter: " + stateCodeCurrent.toLowerCase(Locale.US)
                    )
            )
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
            ObjectIntent(
                    context,
                    SpcMesoActivity::class.java,
                    SpcMesoActivity.INFO,
                    arrayOf("", "1", "SPCMESO")
            )
        } else if (vrString.contains("forecast")) {
            val fcstStr = Utility.readPref(context, "FCST", "")
            UtilityTts.synthesizeTextAndPlay(context, fcstStr, "7day")
        } else if (vrString.contains("download playlist")) {
            UtilityUI.makeSnackBar(view, "Download initiated")
            ObjectIntent(
                    context,
                    DownloadPlaylistService::class.java,
                    DownloadPlaylistService.URL,
                    "true"
            )
        } else if (vrString.contains("playlist")) {
            UtilityTts.synthesizeTextAndPlayPlaylist(context, 1)
        } else {
            gotHit = false
        }
        return gotHit
    }
}

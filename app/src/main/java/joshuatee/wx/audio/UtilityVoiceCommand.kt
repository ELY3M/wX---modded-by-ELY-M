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

package joshuatee.wx.audio

import java.util.Locale
import android.content.Context
import joshuatee.wx.settings.Location
import joshuatee.wx.common.RegExp
import joshuatee.wx.objects.Route
import joshuatee.wx.radar.RadarSites
import joshuatee.wx.util.Utility

object UtilityVoiceCommand {

    fun processCommand(context: Context, vrStringOriginal: String): Boolean {
        var radarSite = Location.rid
        var wfo = Location.wfo
        var gotHit = true
        val tokens = RegExp.space.split(vrStringOriginal)
        when {
            vrStringOriginal.contains("radar") -> {
                var validRid = true
                if (tokens.size > 1) {
                    radarSite = tokens[1].uppercase(Locale.US)
                    if (RadarSites.getName(radarSite) == "") {
                        validRid = false
                    }
                }
                if (validRid) {
                    Route.radar(context, arrayOf(radarSite, "STATE NOT USED"))
                }
            }

            vrStringOriginal.contains("AFD") || vrStringOriginal.contains("text") -> {
                if (tokens.size > 1) {
                    wfo = tokens[1].uppercase(Locale.US)
                }
                if (wfo == "WPC") {
                    Route.wpcTextWithSound(context, "pmdspd", "Short Range Forecast Discussion")
                } else {
                    Route.wfoText(context, arrayOf(wfo, "AFD", "sound"))
                }
            }

            vrStringOriginal.contains("cloud") -> Route.vis(context)
            vrStringOriginal.uppercase(Locale.US).contains("SPC") -> {
                if (tokens.size > 1) {
                    when {
                        vrStringOriginal.contains("1") -> Route.spcSwo(context, "1", "sound")
                        vrStringOriginal.contains("2") -> Route.spcSwo(context, "2", "sound")
                        vrStringOriginal.contains("3") -> Route.spcSwo(context, "3", "sound")
                        else -> Route.spcSwo(context, "4-8", "sound")
                    }
                } else {
                    Route.spcSwo(context, "1", "sound")
                }
            }

            vrStringOriginal.contains("day one") -> Route.spcSwo(context, "1", "sound")
            vrStringOriginal.contains("day 2") -> Route.spcSwo(context, "2", "sound")
            vrStringOriginal.contains("day 3") -> Route.spcSwo(context, "3", "sound")
            vrStringOriginal.contains("day 4") -> Route.spcSwo(context, "4-8", "sound")
            vrStringOriginal.contains("add") -> Route.locationEdit(
                context,
                (Location.numLocations + 1).toString()
            )

            vrStringOriginal.contains("edit") -> Route.locationEdit(
                context,
                Location.currentLocationStr
            )

            vrStringOriginal.contains("mosaic") -> Route.radarMosaic(context)
            vrStringOriginal.contains("map") -> {
                if (tokens.size > 1) {
                    when (tokens[1]) {
                        "pressure" -> {
                            Utility.writePref(context, "SPCMESO_LAST_USED", "pmsl")
                            Utility.writePref(
                                context,
                                "SPCMESO_LAST_USED_LABEL",
                                "MSL Pressure/Wind"
                            )
                        }

                        "500" -> {
                            Utility.writePref(context, "SPCMESO_LAST_USED", "500mb")
                            Utility.writePref(context, "SPCMESO_LAST_USED_LABEL", "500mb Analysis")
                        }

                        "cape" -> {
                            Utility.writePref(context, "SPCMESO_LAST_USED", "mucp")
                            Utility.writePref(
                                context,
                                "SPCMESO_LAST_USED_LABEL",
                                "MUCAPE / LPL Height"
                            )
                        }

                        "supercell" -> {
                            Utility.writePref(context, "SPCMESO_LAST_USED", "scp")
                            Utility.writePref(
                                context,
                                "SPCMESO_LAST_USED_LABEL",
                                "Supercell Composite"
                            )
                        }

                        "wind" -> {
                            Utility.writePref(context, "SPCMESO_LAST_USED", "eshr")
                            Utility.writePref(context, "SPCMESO_LAST_USED_LABEL", "Effective Shear")
                        }

                        else -> {
                            Utility.writePref(context, "SPCMESO_LAST_USED", "pmsl")
                            Utility.writePref(
                                context,
                                "SPCMESO_LAST_USED_LABEL",
                                "MSL Pressure/Wind"
                            )
                        }
                    }
                }
                Route.spcMeso(context)
            }

            vrStringOriginal.contains("forecast") -> {
                val forecast = Utility.readPref(context, "FCST", "")
                Route.textPlaySound(
                    context,
                    forecast,
                    "Forecast for " + Location.latLon.prettyPrint()
                )
            }

            vrStringOriginal.contains("playlist") -> UtilityTts.synthesizeTextAndPlayPlaylist(
                context,
                1
            )

            else -> {
                gotHit = false
            }
        }
        return gotHit
    }
}

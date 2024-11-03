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

import android.content.Context
import android.content.Intent
import joshuatee.wx.MyApplication
import joshuatee.wx.misc.RtmaActivity
import joshuatee.wx.misc.USAlertsActivity
import joshuatee.wx.models.ModelsGenericActivity
import joshuatee.wx.nhc.NhcActivity
import joshuatee.wx.objects.Route
import joshuatee.wx.radar.RadarMosaicNwsActivity
import joshuatee.wx.radar.WXGLRadarActivityMultiPane
import joshuatee.wx.spc.SpcSwoActivity
import joshuatee.wx.spc.SpcStormReportsActivity
import joshuatee.wx.spc.SpcMesoActivity
import joshuatee.wx.spc.SpcSoundingsActivity
import joshuatee.wx.spc.SpcThunderStormOutlookActivity
import joshuatee.wx.ui.CardHSImage
import joshuatee.wx.util.SoundingSites
import joshuatee.wx.vis.GoesActivity
import joshuatee.wx.wpc.NationalImagesActivity
import joshuatee.wx.wpc.NationalTextActivity
import java.util.Locale

internal object UtilityHomeScreen {

    val classes = mutableMapOf<String, Class<*>>()
    val classArgs = mutableMapOf<String, Array<String>>()
    val classId = mutableMapOf<String, String>()

    val localChoicesText = listOf(
        "CC: Current Conditions",
        "CC2: Current Conditions with image",
        "HAZ: Hazards",
        "7DAY2: 7 Day Forecast with images",
        "AFDLOC: Area Forecast Discussion",
        "HWOLOC: Hazardous Weather Outlook",
        "VFDLOC: Aviation only Area Forecast Discussion",
        "HOURLY: Hourly Forecast",
        "CTOF: Celsius to Fahrenheit table"
    )

    val localChoicesImg = listOf(
        "RADAR: Local NEXRAD Radar",
        "WEATHERSTORY: Local NWS Weather Story",
        "WFOWARNINGS: Local NWS Office Warnings",
        "RTMA_DEW: Real-Time Mesoscale Analysis Dew Point",
        "RTMA_TEMP: Real-Time Mesoscale Analysis Temperature",
        "RTMA_WIND: Real-Time Mesoscale Analysis Wind"
    )

    fun launch(context: Context, homeScreenImageCards: List<CardHSImage>) {
        homeScreenImageCards.indices.forEach { ii ->
            val cl = classes[homeScreenImageCards[ii].product]
            val id = classId[homeScreenImageCards[ii].product]
            val argsOrig = classArgs[homeScreenImageCards[ii].product]
            homeScreenImageCards[ii].connect {
                if (argsOrig != null) {
                    val args = argsOrig.copyOf(argsOrig.size)
                    args.indices.forEach { z ->
                        if (args[z] == "WFO_FOR_SND") // Check that this is not needed TODO FIXME
                            args[z] = SoundingSites.sites.getNearest(Location.latLon)
//                            args[z] = UtilityLocation.getNearestSoundingSite(LatLon(Location.x, Location.y))
                        if (args[z] == "WFO_FOR_GOES")
                            args[z] = Location.wfo.lowercase(Locale.US)
                        if (args[z] == "STATE_LOWER")
                            args[z] = Location.state.lowercase(Locale.US)
                        if (args[z] == "STATE_UPPER")
                            args[z] = Location.state
                        if (args[z] == "RID_FOR_CA")
                            args[z] = Location.rid
                    }
                    if (cl != null && id != null) {
                        val intent = Intent(MyApplication.appContext, cl)
                        intent.putExtra(id, args)
                        context.startActivity(intent)
                    }
                } else {
                    Route.vis(context)
                }
            }
        }
    }

    fun setupMap() {
        (1..3).forEach {
            val number = it.toString()
            val token = "SWOD$number"
            classes[token] = SpcSwoActivity::class.java
            classArgs[token] = arrayOf(number, "")
            classId[token] = SpcSwoActivity.NUMBER
        }
        (1..6).forEach {
            val number = it.toString()
            val token = "SPCMESO$number"
            classes[token] = SpcMesoActivity::class.java
            classArgs[token] = arrayOf(token, "1", "SPCMESO")
            classId[token] = SpcMesoActivity.INFO
        }

        classes["RADAR_DUAL_PANE"] = WXGLRadarActivityMultiPane::class.java
        classId["RADAR_DUAL_PANE"] = WXGLRadarActivityMultiPane.RID
        classArgs["RADAR_DUAL_PANE"] = arrayOf("", "", "2")

        classes["RADAR_QUAD_PANE"] = WXGLRadarActivityMultiPane::class.java
        classId["RADAR_QUAD_PANE"] = WXGLRadarActivityMultiPane.RID
        classArgs["RADAR_QUAD_PANE"] = arrayOf("", "", "4")

        classes["WPCIMG"] = NationalImagesActivity::class.java
        classId["WPCIMG"] = NationalImagesActivity.URL
        classArgs["WPCIMG"] = arrayOf()

        classes["WPCTEXT"] = NationalTextActivity::class.java
        classId["WPCTEXT"] = NationalTextActivity.URL
        classArgs["WPCTEXT"] = arrayOf("pmdspd", "Short Range Forecast Discussion")

        classes["NHC"] = NhcActivity::class.java
        classId["NHC"] = ""
        classArgs["NHC"] = arrayOf()

        classes["MODEL_NCEP"] = ModelsGenericActivity::class.java
        classId["MODEL_NCEP"] = ModelsGenericActivity.INFO
        classArgs["MODEL_NCEP"] = arrayOf("1", "NCEP", "NCEP")

        classes["SPC_TST"] = SpcThunderStormOutlookActivity::class.java
        classArgs["SPC_TST"] = arrayOf("")
        classId["SPC_TST"] = ""

        classes["STRPT"] = SpcStormReportsActivity::class.java
        classArgs["STRPT"] = arrayOf("today")
        classId["STRPT"] = SpcStormReportsActivity.DAY

        classes["LTG"] = GoesActivity::class.java
        classArgs["LTG"] = arrayOf("CONUS", "21")
        classId["LTG"] = GoesActivity.RID

        classes["CONUSWV"] = GoesActivity::class.java
        classArgs["CONUSWV"] = arrayOf("CONUS", "09")
        classId["CONUSWV"] = GoesActivity.RID

        classes["VIS_CONUS"] = GoesActivity::class.java
        classArgs["VIS_CONUS"] = arrayOf("CONUS", "02")
        classId["VIS_CONUS"] = GoesActivity.RID

        classes["GOES16"] = GoesActivity::class.java
        classArgs["GOES16"] = arrayOf("")
        classId["GOES16"] = GoesActivity.RID

        classes["SND"] = SpcSoundingsActivity::class.java
        classArgs["SND"] = arrayOf("WFO_FOR_SND", "")
        classId["SND"] = SpcSoundingsActivity.URL

        classes["OBS"] = SpcSoundingsActivity::class.java
        classArgs["OBS"] = arrayOf("STATE_LOWER", "")
        classId["OBS"] = SpcSoundingsActivity.URL

        classes["RTMA_DEW"] = RtmaActivity::class.java
        classArgs["RTMA_DEW"] = arrayOf("2m_dwpt")
        classId["RTMA_DEW"] = RtmaActivity.RID

        classes["RTMA_TEMP"] = RtmaActivity::class.java
        classArgs["RTMA_TEMP"] = arrayOf("2m_temp")
        classId["RTMA_TEMP"] = RtmaActivity.RID

        classes["RTMA_WIND"] = RtmaActivity::class.java
        classArgs["RTMA_WIND"] = arrayOf("10m_wnd")
        classId["RTMA_WIND"] = RtmaActivity.RID

        classes["RAD_2KM"] = RadarMosaicNwsActivity::class.java
        classArgs["RAD_2KM"] = arrayOf("")
        classId["RAD_2KM"] = RadarMosaicNwsActivity.URL

        listOf(
            "FMAP",
            "FMAPD2",
            "FMAPD3",
            "FMAP12",
            "FMAP24",
            "FMAP36",
            "FMAP48",
            "FMAP72",
            "FMAP96",
            "FMAP120",
            "FMAP144",
            "FMAP168",
            "FMAP3D",
            "FMAP4D",
            "FMAP5D",
            "FMAP6D",
            "WPC_ANALYSIS",
            "QPF1",
            "QPF2",
            "QPF3",
            "QPF1-2",
            "QPF1-3",
            "QPF4-5",
            "QPF6-7",
            "QPF1-5",
            "QPF1-7"
        ).forEach {
            classes[it] = NationalImagesActivity::class.java
            classArgs[it] = arrayOf("HS", it)
            classId[it] = NationalImagesActivity.URL
        }
        listOf(
            "USWARN",
            "AKWARN",
            "HIWARN"
        ).forEach {
            classes[it] = USAlertsActivity::class.java
            classArgs[it] = arrayOf(
                ".*?Tornado Warning.*?|.*?Severe Thunderstorm Warning.*?|.*?Flash Flood Warning.*?",
                "us"
            )
            classId[it] = USAlertsActivity.URL
        }
        listOf(
            "NHC2ATL",
            "NHC5ATL",
            "NHC2EPAC",
            "NHC5EPAC",
            "NHC2CPAC",
            "NHC5CPAC"
        ).forEach {
            classes[it] = NhcActivity::class.java
            classId[it] = ""
            classArgs[it] = arrayOf()
        }
    }
}

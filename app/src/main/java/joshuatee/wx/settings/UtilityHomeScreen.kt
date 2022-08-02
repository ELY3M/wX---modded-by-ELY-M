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

package joshuatee.wx.settings

import joshuatee.wx.activitiesmisc.LightningActivity
import joshuatee.wx.activitiesmisc.USWarningsWithRadarActivity
import joshuatee.wx.models.ModelsGenericActivity
import joshuatee.wx.nhc.NhcActivity
import joshuatee.wx.radar.AwcRadarMosaicActivity
import joshuatee.wx.radar.RadarMosaicNwsActivity
import joshuatee.wx.radar.WXGLRadarActivityMultiPane
import joshuatee.wx.spc.*
import joshuatee.wx.vis.GoesActivity
import joshuatee.wx.wpc.WpcImagesActivity
import joshuatee.wx.wpc.WpcTextProductsActivity

internal object UtilityHomeScreen {

    val HM_CLASS = mutableMapOf<String, Class<*>>()
    val HM_CLASS_ARGS = mutableMapOf<String, Array<String>>()
    val HM_CLASS_ID = mutableMapOf<String, String>()
    
    val localChoicesText = listOf(
            "CC: Current Conditions",
            "CC2: Current Conditions with image",
            "HAZ: Hazards",
            "7DAY2: 7 Day Forecast with images",
            "AFDLOC: Area Forecast Discussion",
            "HWOLOC: Hazardous Weather Outlook",
            "VFDLOC: Aviation only Area Forecast Discussion",
            "HOURLY: Hourly Forecast",
            "CTOF: Celsius to Fahrenheit table",
	    "AURORA: Aurora Forecast"
    )
    val localChoicesImg = listOf(
            "RADAR: Local NEXRAD Radar",
            "WEATHERSTORY: Local NWS Weather Story",
            "WFOWARNINGS: Local NWS Office Warnings"
    )
    val localChoicesWeb = listOf(
            "7DAY: 7 Day Forecast",
    )

    fun setupMap() {
        (1..3).forEach {
            val number = it.toString()
            val token = "SWOD$number"
            HM_CLASS[token] = SpcSwoActivity::class.java
            HM_CLASS_ARGS[token] = arrayOf(number, "")
            HM_CLASS_ID[token] = SpcSwoActivity.NUMBER
        }
        (1..6).forEach {
            val number = it.toString()
            val token = "SPCMESO$number"
            HM_CLASS[token] = SpcMesoActivity::class.java
            HM_CLASS_ARGS[token] = arrayOf(token, "1", "SPCMESO")
            HM_CLASS_ID[token] = SpcMesoActivity.INFO
        }

        HM_CLASS["RADAR_DUAL_PANE"] = WXGLRadarActivityMultiPane::class.java
        HM_CLASS_ID["RADAR_DUAL_PANE"] = WXGLRadarActivityMultiPane.RID
        HM_CLASS_ARGS["RADAR_DUAL_PANE"] = arrayOf("", "", "2")

        HM_CLASS["RADAR_QUAD_PANE"] = WXGLRadarActivityMultiPane::class.java
        HM_CLASS_ID["RADAR_QUAD_PANE"] = WXGLRadarActivityMultiPane.RID
        HM_CLASS_ARGS["RADAR_QUAD_PANE"] = arrayOf("", "", "4")

        HM_CLASS["WPCIMG"] = WpcImagesActivity::class.java
        HM_CLASS_ID["WPCIMG"] = WpcImagesActivity.URL
        HM_CLASS_ARGS["WPCIMG"] = arrayOf()

        HM_CLASS["WPCTEXT"] = WpcTextProductsActivity::class.java
        HM_CLASS_ID["WPCTEXT"] = WpcTextProductsActivity.URL
        HM_CLASS_ARGS["WPCTEXT"] = arrayOf("pmdspd", "Short Range Forecast Discussion")

        HM_CLASS["NHC"] = NhcActivity::class.java
        HM_CLASS_ID["NHC"] = ""
        HM_CLASS_ARGS["NHC"] = arrayOf()

        HM_CLASS["MODEL_NCEP"] = ModelsGenericActivity::class.java
        HM_CLASS_ID["MODEL_NCEP"] = ModelsGenericActivity.INFO
        HM_CLASS_ARGS["MODEL_NCEP"] = arrayOf("1", "NCEP", "NCEP")

        HM_CLASS["SPC_TST"] = SpcThunderStormOutlookActivity::class.java
        HM_CLASS_ARGS["SPC_TST"] = arrayOf("")
        HM_CLASS_ID["SPC_TST"] = ""

        HM_CLASS["STRPT"] = SpcStormReportsActivity::class.java
        HM_CLASS_ARGS["STRPT"] = arrayOf("today")
        HM_CLASS_ID["STRPT"] = SpcStormReportsActivity.DAY

        if (UIPreferences.lightningUseGoes) {
            HM_CLASS["LTG"] = GoesActivity::class.java
            HM_CLASS_ARGS["LTG"] = arrayOf("CONUS", "21")
            HM_CLASS_ID["LTG"] = GoesActivity.RID
        } else {
            HM_CLASS["LTG"] = LightningActivity::class.java
            HM_CLASS_ARGS["LTG"] = arrayOf("")
            HM_CLASS_ID["LTG"] = ""
        }

        HM_CLASS["CONUSWV"] = GoesActivity::class.java
        HM_CLASS_ARGS["CONUSWV"] = arrayOf("CONUS", "09")
        HM_CLASS_ID["CONUSWV"] = GoesActivity.RID

        HM_CLASS["VIS_CONUS"] = GoesActivity::class.java
        HM_CLASS_ARGS["VIS_CONUS"] = arrayOf("CONUS", "02")
        HM_CLASS_ID["VIS_CONUS"] = GoesActivity.RID

        HM_CLASS["GOES16"] = GoesActivity::class.java
        HM_CLASS_ARGS["GOES16"] = arrayOf("")
        HM_CLASS_ID["GOES16"] = GoesActivity.RID

        HM_CLASS["SND"] = SpcSoundingsActivity::class.java
        HM_CLASS_ARGS["SND"] = arrayOf("WFO_FOR_SND", "")
        HM_CLASS_ID["SND"] = SpcSoundingsActivity.URL

        HM_CLASS["OBS"] = SpcSoundingsActivity::class.java
        HM_CLASS_ARGS["OBS"] = arrayOf("STATE_LOWER", "")
        HM_CLASS_ID["OBS"] = SpcSoundingsActivity.URL

        if (UIPreferences.useAwcMosaic) {
            HM_CLASS["RAD_2KM"] = AwcRadarMosaicActivity::class.java
            HM_CLASS_ARGS["RAD_2KM"] = arrayOf("")
            HM_CLASS_ID["RAD_2KM"] = AwcRadarMosaicActivity.URL
        } else {
            HM_CLASS["RAD_2KM"] = RadarMosaicNwsActivity::class.java
            HM_CLASS_ARGS["RAD_2KM"] = arrayOf("")
            HM_CLASS_ID["RAD_2KM"] = RadarMosaicNwsActivity.URL
        }

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
            HM_CLASS[it] = WpcImagesActivity::class.java
            HM_CLASS_ARGS[it] = arrayOf("HS", it)
            HM_CLASS_ID[it] = WpcImagesActivity.URL
        }
        listOf(
            "USWARN",
            "AKWARN",
            "HIWARN"
        ).forEach {
            HM_CLASS[it] = USWarningsWithRadarActivity::class.java
            HM_CLASS_ARGS[it] = arrayOf(".*?Tornado Warning.*?|.*?Severe Thunderstorm Warning.*?|.*?Flash Flood Warning.*?", "us")
            HM_CLASS_ID[it] = USWarningsWithRadarActivity.URL
        }
        listOf(
            "NHC2ATL",
            "NHC5ATL",
            "NHC2EPAC",
            "NHC5EPAC",
            "NHC2CPAC",
            "NHC5CPAC"
        ).forEach {
            HM_CLASS[it] = NhcActivity::class.java
            HM_CLASS_ID[it] = ""
            HM_CLASS_ARGS[it] = arrayOf()
        }
    }
}

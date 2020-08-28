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

package joshuatee.wx.settings

import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.activitiesmisc.LightningActivity
import joshuatee.wx.activitiesmisc.USWarningsWithRadarActivity
import joshuatee.wx.canada.CanadaRadarActivity
import joshuatee.wx.models.ModelsGenericActivity
import joshuatee.wx.nhc.NhcActivity
import joshuatee.wx.radar.AwcRadarMosaicActivity
import joshuatee.wx.radar.USNwsMosaicActivity
import joshuatee.wx.radar.WXGLRadarActivityMultiPane
import joshuatee.wx.spc.*
import joshuatee.wx.vis.GoesActivity
import joshuatee.wx.wpc.WpcImagesActivity
import joshuatee.wx.wpc.WpcTextProductsActivity

internal object UtilityHomeScreen {

    val localChoicesText = listOf(
            "CC: Current Conditions",
            "CC2: Current Conditions with image",
            "HAZ: Hazards",
            "7DAY2: 7 Day Forecast with images",
            "AFDLOC: Area Forecast Discussion",
            "HWOLOC: Hazardous Weather Outlook",
            "VFDLOC: Aviation only Area Forecast Discussion",
            "SUNMOON: Sun/Moon Data",
            "HOURLY: Hourly Forecast",
            "CTOF: Celsius to Fahrenheit table"
    )
    val localChoicesImg = listOf(
            "RADAR: Local NEXRAD Radar",
            "CARAIN: Local CA Radar",
            "WEATHERSTORY: Local NWS Weather Story",
            "WFOWARNINGS: Local NWS Office Warnings"
    )

    fun setupMap() {
        (1..3).forEach {
            val number = it.toString()
            val token = "SWOD$number"
            MyApplication.HM_CLASS[token] = SpcSwoActivity::class.java
            MyApplication.HM_CLASS_ARGS[token] = arrayOf(number, "")
            MyApplication.HM_CLASS_ID[token] = SpcSwoActivity.NUMBER
        }
        (1..6).forEach {
            val number = it.toString()
            val token = "SPCMESO$number"
            MyApplication.HM_CLASS[token] = SpcMesoActivity::class.java
            MyApplication.HM_CLASS_ARGS[token] = arrayOf(token, "1", "SPCMESO")
            MyApplication.HM_CLASS_ID[token] = SpcMesoActivity.INFO
        }

        MyApplication.HM_CLASS["RADAR_DUAL_PANE"] = WXGLRadarActivityMultiPane::class.java
        MyApplication.HM_CLASS_ID["RADAR_DUAL_PANE"] = WXGLRadarActivityMultiPane.RID
        MyApplication.HM_CLASS_ARGS["RADAR_DUAL_PANE"] = arrayOf("", "", "2")

        MyApplication.HM_CLASS["RADAR_QUAD_PANE"] = WXGLRadarActivityMultiPane::class.java
        MyApplication.HM_CLASS_ID["RADAR_QUAD_PANE"] = WXGLRadarActivityMultiPane.RID
        MyApplication.HM_CLASS_ARGS["RADAR_QUAD_PANE"] = arrayOf("", "", "4")

        MyApplication.HM_CLASS["WPCIMG"] = WpcImagesActivity::class.java
        MyApplication.HM_CLASS_ID["WPCIMG"] = WpcImagesActivity.URL
        MyApplication.HM_CLASS_ARGS["WPCIMG"] = arrayOf()

        MyApplication.HM_CLASS["WPCTEXT"] = WpcTextProductsActivity::class.java
        MyApplication.HM_CLASS_ID["WPCTEXT"] = WpcTextProductsActivity.URL
        MyApplication.HM_CLASS_ARGS["WPCTEXT"] = arrayOf("pmdspd", "Short Range Forecast Discussion")

        MyApplication.HM_CLASS["NHC"] = NhcActivity::class.java
        MyApplication.HM_CLASS_ID["NHC"] = ""
        MyApplication.HM_CLASS_ARGS["NHC"] = arrayOf()

        MyApplication.HM_CLASS["MODEL_NCEP"] = ModelsGenericActivity::class.java
        MyApplication.HM_CLASS_ID["MODEL_NCEP"] = ModelsGenericActivity.INFO
        MyApplication.HM_CLASS_ARGS["MODEL_NCEP"] = arrayOf("1", "NCEP", "NCEP")

        MyApplication.HM_CLASS["SPC_TST"] = SpcThunderStormOutlookActivity::class.java
        MyApplication.HM_CLASS_ARGS["SPC_TST"] = arrayOf("")
        MyApplication.HM_CLASS_ID["SPC_TST"] = ""

        MyApplication.HM_CLASS["STRPT"] = SpcStormReportsActivity::class.java
        MyApplication.HM_CLASS_ARGS["STRPT"] = arrayOf("today")
        MyApplication.HM_CLASS_ID["STRPT"] = SpcStormReportsActivity.NO

        MyApplication.HM_CLASS["LTG"] = LightningActivity::class.java
        MyApplication.HM_CLASS_ARGS["LTG"] = arrayOf("")
        MyApplication.HM_CLASS_ID["LTG"] = ""

        MyApplication.HM_CLASS["CONUSWV"] = GoesActivity::class.java
        MyApplication.HM_CLASS_ARGS["CONUSWV"] = arrayOf("CONUS", "09")
        MyApplication.HM_CLASS_ID["CONUSWV"] = GoesActivity.RID

        MyApplication.HM_CLASS["VIS_CONUS"] = GoesActivity::class.java
        MyApplication.HM_CLASS_ARGS["VIS_CONUS"] = arrayOf("CONUS", "02")
        MyApplication.HM_CLASS_ID["VIS_CONUS"] = GoesActivity.RID

        MyApplication.HM_CLASS["GOES16"] = GoesActivity::class.java
        MyApplication.HM_CLASS_ARGS["GOES16"] = arrayOf("")
        MyApplication.HM_CLASS_ID["GOES16"] = GoesActivity.RID

        MyApplication.HM_CLASS["SND"] = SpcSoundingsActivity::class.java
        MyApplication.HM_CLASS_ARGS["SND"] = arrayOf("WFO_FOR_SND", "")
        MyApplication.HM_CLASS_ID["SND"] = SpcSoundingsActivity.URL

        MyApplication.HM_CLASS["OBS"] = SpcSoundingsActivity::class.java
        MyApplication.HM_CLASS_ARGS["OBS"] = arrayOf("STATE_LOWER", "")
        MyApplication.HM_CLASS_ID["OBS"] = SpcSoundingsActivity.URL

        MyApplication.HM_CLASS["CARAIN"] = CanadaRadarActivity::class.java
        MyApplication.HM_CLASS_ARGS["CARAIN"] = arrayOf("RID_FOR_CA", "rad")
        MyApplication.HM_CLASS_ID["CARAIN"] = CanadaRadarActivity.RID

        // FIXME this one should be removed
        MyApplication.HM_CLASS["RAD_1KM"] = SpcSoundingsActivity::class.java
        MyApplication.HM_CLASS_ARGS["RAD_1KM"] = arrayOf("1km", "rad", "ONEK", "STATE_UPPER")
        MyApplication.HM_CLASS_ID["RAD_1KM"] = SpcSoundingsActivity.URL

        if (!UIPreferences.useAwcRadarMosaic) {
            MyApplication.HM_CLASS["RAD_2KM"] = USNwsMosaicActivity::class.java
            MyApplication.HM_CLASS_ARGS["RAD_2KM"] = arrayOf("location")
            MyApplication.HM_CLASS_ID["RAD_2KM"] = USNwsMosaicActivity.URL
        } else {
            MyApplication.HM_CLASS["RAD_2KM"] = AwcRadarMosaicActivity::class.java
            MyApplication.HM_CLASS_ARGS["RAD_2KM"] = arrayOf("")
            MyApplication.HM_CLASS_ID["RAD_2KM"] = AwcRadarMosaicActivity.URL
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
            MyApplication.HM_CLASS[it] = WpcImagesActivity::class.java
            MyApplication.HM_CLASS_ARGS[it] = arrayOf("HS", it)
            MyApplication.HM_CLASS_ID[it] = WpcImagesActivity.URL
        }
        listOf(
                "USWARN",
                "AKWARN",
                "HIWARN"
        ).forEach {
            MyApplication.HM_CLASS[it] = USWarningsWithRadarActivity::class.java
            MyApplication.HM_CLASS_ARGS[it] = arrayOf(".*?Tornado Warning.*?|.*?Severe Thunderstorm Warning.*?|.*?Flash Flood Warning.*?", "us")
            MyApplication.HM_CLASS_ID[it] = USWarningsWithRadarActivity.URL
        }
        listOf(
                "NHC2ATL",
                "NHC5ATL",
                "NHC2EPAC",
                "NHC5EPAC",
                "NHC2CPAC",
                "NHC5CPAC"
        ).forEach {
            MyApplication.HM_CLASS[it] = NhcActivity::class.java
            MyApplication.HM_CLASS_ID[it] = ""
            MyApplication.HM_CLASS_ARGS[it] = arrayOf()
        }
    }
}

/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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

import joshuatee.wx.MyApplication
import joshuatee.wx.activitiesmisc.LightningActivity
import joshuatee.wx.canada.CanadaRadarActivity
import joshuatee.wx.radar.USNWSMosaicActivity
import joshuatee.wx.spc.SPCMesoActivity
import joshuatee.wx.spc.SPCSWOActivity
import joshuatee.wx.spc.SPCSoundingsActivity
import joshuatee.wx.spc.SPCStormReportsActivity
import joshuatee.wx.vis.GOES16Activity
import joshuatee.wx.vis.USNWSGOESActivity
import joshuatee.wx.wpc.WPCImagesActivity

internal object UtilityHomeScreen {

    fun setupMap() {
        (1..3).forEach {
            val number = it.toString()
            val token = "SWOD$number"
            MyApplication.HM_CLASS[token] = SPCSWOActivity::class.java
            MyApplication.HM_CLASS_ARGS[token] = arrayOf(number, "")
            MyApplication.HM_CLASS_ID[token] = SPCSWOActivity.NO
        }
        (1..6).forEach {
            val number = it.toString()
            val token = "SPCMESO$number"
            MyApplication.HM_CLASS[token] = SPCMesoActivity::class.java
            MyApplication.HM_CLASS_ARGS[token] = arrayOf(token, number, "SPCMESO")
            MyApplication.HM_CLASS_ID[token] = SPCMesoActivity.INFO
        }

        MyApplication.HM_CLASS["STRPT"] = SPCStormReportsActivity::class.java
        MyApplication.HM_CLASS_ARGS["STRPT"] = arrayOf("today")
        MyApplication.HM_CLASS_ID["STRPT"] = SPCStormReportsActivity.NO

        MyApplication.HM_CLASS["LTG"] = LightningActivity::class.java
        MyApplication.HM_CLASS_ARGS["LTG"] = arrayOf("")
        MyApplication.HM_CLASS_ID["LTG"] = LightningActivity.URL

        MyApplication.HM_CLASS["CONUSWV"] = GOES16Activity::class.java
        MyApplication.HM_CLASS_ARGS["CONUSWV"] = arrayOf("CONUS", "09")
        MyApplication.HM_CLASS_ID["CONUSWV"] = GOES16Activity.RID

        MyApplication.HM_CLASS["VIS_CONUS"] = GOES16Activity::class.java
        MyApplication.HM_CLASS_ARGS["VIS_CONUS"] = arrayOf("CONUS", "02")
        MyApplication.HM_CLASS_ID["VIS_CONUS"] = GOES16Activity.RID

        MyApplication.HM_CLASS["GOES16"] = GOES16Activity::class.java
        MyApplication.HM_CLASS_ARGS["GOES16"] = arrayOf("")
        MyApplication.HM_CLASS_ID["GOES16"] = GOES16Activity.RID

        MyApplication.HM_CLASS["SND"] = SPCSoundingsActivity::class.java
        MyApplication.HM_CLASS_ARGS["SND"] = arrayOf("WFO_FOR_SND", "")
        MyApplication.HM_CLASS_ID["SND"] = SPCSoundingsActivity.URL

        MyApplication.HM_CLASS["OBS"] = SPCSoundingsActivity::class.java
        MyApplication.HM_CLASS_ARGS["OBS"] = arrayOf("STATE_LOWER", "")
        MyApplication.HM_CLASS_ID["OBS"] = SPCSoundingsActivity.URL

        MyApplication.HM_CLASS["VIS_1KM"] = USNWSGOESActivity::class.java
        MyApplication.HM_CLASS_ARGS["VIS_1KM"] = arrayOf("nws", "WFO_FOR_GOES")
        MyApplication.HM_CLASS_ID["VIS_1KM"] = USNWSGOESActivity.RID

        MyApplication.HM_CLASS["CARAIN"] = CanadaRadarActivity::class.java
        MyApplication.HM_CLASS_ARGS["CARAIN"] = arrayOf("RID_FOR_CA", "rad")
        MyApplication.HM_CLASS_ID["CARAIN"] = CanadaRadarActivity.RID

        MyApplication.HM_CLASS["RAD_1KM"] = SPCSoundingsActivity::class.java
        MyApplication.HM_CLASS_ARGS["RAD_1KM"] = arrayOf("1km", "rad", "ONEK", "STATE_UPPER")
        MyApplication.HM_CLASS_ID["RAD_1KM"] = SPCSoundingsActivity.URL

        MyApplication.HM_CLASS["RAD_2KM"] = USNWSMosaicActivity::class.java
        MyApplication.HM_CLASS_ARGS["RAD_2KM"] = arrayOf("location")
        MyApplication.HM_CLASS_ID["RAD_2KM"] = USNWSMosaicActivity.URL

        MyApplication.HM_CLASS["VIS_2KM"] = USNWSGOESActivity::class.java
        MyApplication.HM_CLASS_ARGS["VIS_2KM"] = arrayOf("nws", "WFO_FOR_GOES", "mosaic")
        MyApplication.HM_CLASS_ID["VIS_2KM"] = USNWSGOESActivity.RID

        MyApplication.HM_CLASS["WV_2KM"] = USNWSGOESActivity::class.java
        MyApplication.HM_CLASS_ARGS["WV_2KM"] = arrayOf("nws", "WFO_FOR_GOES", "mosaic", "wv")
        MyApplication.HM_CLASS_ID["WV_2KM"] = USNWSGOESActivity.RID

        MyApplication.HM_CLASS["IR_2KM"] = USNWSGOESActivity::class.java
        MyApplication.HM_CLASS_ARGS["IR_2KM"] = arrayOf("nws", "WFO_FOR_GOES", "mosaic", "ir2")
        MyApplication.HM_CLASS_ID["IR_2KM"] = USNWSGOESActivity.RID

        MyApplication.HM_CLASS["QPF1"] = WPCImagesActivity::class.java
        MyApplication.HM_CLASS_ARGS["QPF1"] = arrayOf("")
        MyApplication.HM_CLASS_ID["QPF1"] = WPCImagesActivity.URL

        MyApplication.HM_CLASS["FMAP"] = WPCImagesActivity::class.java
        MyApplication.HM_CLASS_ARGS["FMAP"] = arrayOf("")
        MyApplication.HM_CLASS_ID["FMAP"] = WPCImagesActivity.URL

        listOf("QPF2", "QPF3", "QPF1-2", "QPF1-3", "QPF4-5", "QPF6-7", "QPF1-5", "QPF1-7").forEach {
            MyApplication.HM_CLASS[it] = WPCImagesActivity::class.java
            MyApplication.HM_CLASS_ARGS[it] = arrayOf("")
            MyApplication.HM_CLASS_ID[it] = WPCImagesActivity.URL
        }

        listOf(
            "FMAP12",
            "FMAP24",
            "FMAP36",
            "FMAP48",
            "FMAP3D",
            "FMAP4D",
            "FMAP5D",
            "FMAP6D"
        ).forEach {
            MyApplication.HM_CLASS[it] = WPCImagesActivity::class.java
            MyApplication.HM_CLASS_ARGS[it] = arrayOf("")
            MyApplication.HM_CLASS_ID[it] = WPCImagesActivity.URL
        }
    }
}

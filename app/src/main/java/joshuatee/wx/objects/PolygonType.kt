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
//modded by ELY M. 

package joshuatee.wx.objects

import android.graphics.Color
import joshuatee.wx.MyApplication

enum public class PolygonType constructor(
        var color: Int,
        private val typeAsString: String,
        var pref: Boolean,
        var size: Float
) {

    MCD(MyApplication.radarColorMcd, "MCD", MyApplication.radarWatMcd, 0.0f),
    MPD(MyApplication.radarColorMpd, "MPD", MyApplication.radarMpd, 0.0f),
    WATCH(MyApplication.radarColorSvrWatch, "WATCH", MyApplication.radarWatMcd, 0.0f),
    WATCH_TORNADO(
            MyApplication.radarColorTorWatch,
            "WATCH_TORNADO",
            MyApplication.radarWatMcd,
            0.0f
    ),
    TOR(MyApplication.radarColorTor, "TOR", MyApplication.radarTorWarnings, 0.0f),
    SVR(MyApplication.radarColorSvr, "SVR", MyApplication.radarSvrWarnings, 0.0f),
    EWW(MyApplication.radarColorEww, "EWW", MyApplication.radarEwwWarnings, 0.0f),
    FFW(MyApplication.radarColorFfw, "FFW", MyApplication.radarFfwWarnings, 0.0f),
    SMW(MyApplication.radarColorSmw, "SMW", MyApplication.radarSmwWarnings, 0.0f),
    SVS(MyApplication.radarColorSvs, "SVS", MyApplication.radarSvsWarnings, 0.0f),
    SPS(MyApplication.radarColorSps, "SVS", MyApplication.radarSpsWarnings, 0.0f),
    SPOTTER(
            MyApplication.radarColorSpotter,
            "SPOTTER",
            MyApplication.radarSpotters,
            MyApplication.radarSpotterSize.toFloat()
    ),
    SPOTTER_LABELS(
            MyApplication.radarColorSpotter,
            "SPOTTER_LABELS",
            MyApplication.radarSpottersLabel,
            0.0f
    ),
    WIND_BARB_GUSTS(Color.RED, "WIND_BARB_GUSTS", MyApplication.radarObsWindbarbs, MyApplication.radarWbLinesize.toFloat()),
    WIND_BARB(
            MyApplication.radarColorObsWindbarbs,
            "WIND_BARB",
            MyApplication.radarObsWindbarbs,
            MyApplication.radarWbLinesize.toFloat()
            //MyApplication.radarAviationSize.toFloat()
    ),
    WIND_BARB_CIRCLE(
            MyApplication.radarColorObsWindbarbs,
            "WIND_BARB_CIRCLE",
            MyApplication.radarObsWindbarbs,
            MyApplication.radarAviationSize.toFloat()
    ),
    LOCDOT(
            MyApplication.radarColorLocdot,
            "LOCDOT",
            MyApplication.radarLocDot,
            MyApplication.radarLocdotSize.toFloat()
    ),
    STI(MyApplication.radarColorSti, "STI", MyApplication.radarSti, MyApplication.radarStiLinesize.toFloat()),
    TVS(
            MyApplication.radarColorTor,
            "TVS",
            MyApplication.radarTvs,
            MyApplication.radarTvsSize.toFloat()
    ),
    HI(
            MyApplication.radarColorHi,
            "HI",
            MyApplication.radarHi,
            MyApplication.radarHiSize.toFloat()
    ),
    HAIL_LABELS(
    	MyApplication.radarColorHi, 
	"HAILSIZE_LABELS", 
	MyApplication.radarHailSizeLabel, 
	0.0f
    ),
    OBS(MyApplication.radarColorObs, "OBS", MyApplication.radarObs, 0.0f),
    SWO(MyApplication.radarColorHi, "SWO", MyApplication.radarSwo, MyApplication.radarSwoLinesize.toFloat()),
    USERPOINTS(0, "USERPOINTS", MyApplication.radarUserPoints, 0.0f),
    CONUS(0, "CONUS", MyApplication.radarConusRadar, 0.0f),
    NONE(0, "", false, 0.0f);

    override fun toString(): String = typeAsString

    companion object {
        fun refresh() {

            MCD.pref = MyApplication.radarWatMcd
            MPD.pref = MyApplication.radarMpd
            WATCH.pref = MyApplication.radarWatMcd
            WATCH_TORNADO.pref = MyApplication.radarWatMcd
            TOR.pref = MyApplication.radarTorWarnings
            SVR.pref = MyApplication.radarSvrWarnings
            EWW.pref = MyApplication.radarEwwWarnings
            FFW.pref = MyApplication.radarFfwWarnings
            SMW.pref = MyApplication.radarSmwWarnings
            SVS.pref = MyApplication.radarSvsWarnings
            SPS.pref = MyApplication.radarSpsWarnings
            SPOTTER.pref = MyApplication.radarSpotters
            SPOTTER_LABELS.pref = MyApplication.radarSpottersLabel
            WIND_BARB_GUSTS.pref = MyApplication.radarObsWindbarbs
            WIND_BARB.pref = MyApplication.radarObsWindbarbs
            WIND_BARB_CIRCLE.pref = MyApplication.radarObsWindbarbs
            LOCDOT.pref = MyApplication.radarLocDot
            STI.pref = MyApplication.radarSti
            TVS.pref = MyApplication.radarTvs
            HI.pref = MyApplication.radarHi
            OBS.pref = MyApplication.radarObs
            SWO.pref = MyApplication.radarSwo
            USERPOINTS.pref = MyApplication.radarUserPoints
            CONUS.pref = MyApplication.radarConusRadar
            MCD.color = MyApplication.radarColorMcd
            MPD.color = MyApplication.radarColorMpd
            WATCH.color = MyApplication.radarColorSvrWatch
            WATCH_TORNADO.color = MyApplication.radarColorTorWatch
            TOR.color = MyApplication.radarColorTor
            SVR.color = MyApplication.radarColorSvr
            EWW.color = MyApplication.radarColorEww
            FFW.color = MyApplication.radarColorFfw
            SMW.color = MyApplication.radarColorSmw
            SVS.color = MyApplication.radarColorSvs
            SPS.color = MyApplication.radarColorSps
            SPOTTER.color = MyApplication.radarColorSpotter
            WIND_BARB_GUSTS.color = Color.RED
            WIND_BARB.color = MyApplication.radarColorObsWindbarbs
            WIND_BARB_CIRCLE.color = MyApplication.radarColorObsWindbarbs
            LOCDOT.color = MyApplication.radarColorLocdot
            STI.color = MyApplication.radarColorSti
            TVS.color = MyApplication.radarColorTor
            HI.color = MyApplication.radarColorHi
            OBS.color = MyApplication.radarColorObs
            SWO.color = MyApplication.radarColorHi
        }
    }
}



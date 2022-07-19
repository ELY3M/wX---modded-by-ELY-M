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

package joshuatee.wx.objects

import android.graphics.Color
import joshuatee.wx.settings.RadarPreferences

enum class PolygonType constructor(var color: Int, private val typeAsString: String, var pref: Boolean, var size: Float) {

    MCD(RadarPreferences.radarColorMcd, "MCD", RadarPreferences.radarWatMcd, RadarPreferences.radarWatchMcdLineSize),
    MPD(RadarPreferences.radarColorMpd, "MPD", RadarPreferences.radarMpd, RadarPreferences.radarWatchMcdLineSize),
    WATCH(RadarPreferences.radarColorTstormWatch, "WATCH", RadarPreferences.radarWatMcd, RadarPreferences.radarWatchMcdLineSize),
    WATCH_TORNADO(RadarPreferences.radarColorTorWatch, "WATCH_TORNADO", RadarPreferences.radarWatMcd, RadarPreferences.radarWatchMcdLineSize),
    TST(RadarPreferences.radarColorTstorm, "TST", RadarPreferences.radarWarnings, RadarPreferences.radarWarnLineSize),
    TOR(RadarPreferences.radarColorTor, "TOR", RadarPreferences.radarWarnings, RadarPreferences.radarWarnLineSize),
    FFW(RadarPreferences.radarColorFfw, "FFW", RadarPreferences.radarWarnings, RadarPreferences.radarWarnLineSize),
    SPOTTER(RadarPreferences.radarColorSpotter, "SPOTTER", RadarPreferences.radarSpotters, RadarPreferences.radarSpotterSize.toFloat()),
    SPOTTER_LABELS(RadarPreferences.radarColorSpotter, "SPOTTER_LABELS", RadarPreferences.radarSpottersLabel, 0.0f),
    WIND_BARB_GUSTS(Color.RED, "WIND_BARB_GUSTS", RadarPreferences.radarObsWindbarbs, RadarPreferences.radarWbLineSize.toFloat()),
    WIND_BARB(RadarPreferences.radarColorObsWindbarbs, "WIND_BARB", RadarPreferences.radarObsWindbarbs, RadarPreferences.radarWbLineSize.toFloat()),
    WIND_BARB_CIRCLE(RadarPreferences.radarColorObsWindbarbs, "WIND_BARB_CIRCLE", RadarPreferences.radarObsWindbarbs, RadarPreferences.radarAviationSize.toFloat()),
    LOCDOT(RadarPreferences.radarColorLocdot, "LOCDOT", RadarPreferences.radarLocDot, RadarPreferences.radarLocdotSize.toFloat()),
    STI(RadarPreferences.radarColorSti, "STI", RadarPreferences.radarSti, RadarPreferences.radarStiLineSize.toFloat()),
    TVS(RadarPreferences.radarColorTor, "TVS", RadarPreferences.radarTvs, RadarPreferences.radarTvsSize.toFloat()),
    HI(RadarPreferences.radarColorHi, "HI", RadarPreferences.radarHi, RadarPreferences.radarHiSize.toFloat()),
    HAIL_LABELS(RadarPreferences.radarColorHiText, "HAILSIZE_LABELS", RadarPreferences.radarHailSizeLabel, RadarPreferences.radarHiTextSize),
    OBS(RadarPreferences.radarColorObs, "OBS", RadarPreferences.radarObs, 0.0f),
    SWO(RadarPreferences.radarColorHi, "SWO", RadarPreferences.radarSwo, RadarPreferences.radarSwoLineSize.toFloat()),
    USERPOINTS(0, "USERPOINTS", RadarPreferences.radarUserPoints, 0.0f),
    CONUS(0, "CONUS", RadarPreferences.radarConusRadar, 0.0f),
    NONE(0, "", false, 0.0f);

    override fun toString() = typeAsString

    companion object {
        fun refresh() {

            MCD.pref = RadarPreferences.radarWatMcd
            MPD.pref = RadarPreferences.radarMpd
            WATCH.pref = RadarPreferences.radarWatMcd
            WATCH_TORNADO.pref = RadarPreferences.radarWatMcd
            TST.pref = RadarPreferences.radarWarnings
            TOR.pref = RadarPreferences.radarWarnings
            FFW.pref = RadarPreferences.radarWarnings
            SPOTTER.pref = RadarPreferences.radarSpotters
            SPOTTER_LABELS.pref = RadarPreferences.radarSpottersLabel
            WIND_BARB_GUSTS.pref = RadarPreferences.radarObsWindbarbs
            WIND_BARB_GUSTS.size = RadarPreferences.radarWbLineSize.toFloat()
            WIND_BARB.pref = RadarPreferences.radarObsWindbarbs
            WIND_BARB.size = RadarPreferences.radarWbLineSize.toFloat()
            WIND_BARB_CIRCLE.pref = RadarPreferences.radarObsWindbarbs
            LOCDOT.pref = RadarPreferences.radarLocDot
            STI.pref = RadarPreferences.radarSti
            TVS.pref = RadarPreferences.radarTvs
            HI.pref = RadarPreferences.radarHi
	    HAIL_LABELS.pref = RadarPreferences.radarHailSizeLabel
            OBS.pref = RadarPreferences.radarObs
            SWO.pref = RadarPreferences.radarSwo
            USERPOINTS.pref = RadarPreferences.radarUserPoints
            CONUS.pref = RadarPreferences.radarConusRadar
            MCD.color = RadarPreferences.radarColorMcd
            MPD.color = RadarPreferences.radarColorMpd
            WATCH.color = RadarPreferences.radarColorTstormWatch
            WATCH_TORNADO.color = RadarPreferences.radarColorTorWatch
            TST.color = RadarPreferences.radarColorTstorm
            TOR.color = RadarPreferences.radarColorTor
            FFW.color = RadarPreferences.radarColorFfw
            SPOTTER.color = RadarPreferences.radarColorSpotter
            WIND_BARB_GUSTS.color = Color.RED
            WIND_BARB.color = RadarPreferences.radarColorObsWindbarbs
            WIND_BARB_CIRCLE.color = RadarPreferences.radarColorObsWindbarbs
            LOCDOT.color = RadarPreferences.radarColorLocdot
            STI.color = RadarPreferences.radarColorSti
            STI.size = RadarPreferences.radarStiLineSize.toFloat()
            TVS.color = RadarPreferences.radarColorTor
            HI.color = RadarPreferences.radarColorHi
            OBS.color = RadarPreferences.radarColorObs
            SWO.color = RadarPreferences.radarColorHi
            SWO.size = RadarPreferences.radarSwoLineSize.toFloat()
            MCD.size = RadarPreferences.radarWatchMcdLineSize
            MPD.size = RadarPreferences.radarWatchMcdLineSize
            WATCH.size = RadarPreferences.radarWatchMcdLineSize
            WATCH_TORNADO.size = RadarPreferences.radarWatchMcdLineSize
            TST.size = RadarPreferences.radarWarnLineSize
            TOR.size = RadarPreferences.radarWarnLineSize
            FFW.size = RadarPreferences.radarWarnLineSize
        }
    }
}

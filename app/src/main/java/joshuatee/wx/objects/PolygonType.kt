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
//modded by ELY M. 

package joshuatee.wx.objects

import android.graphics.Color
import joshuatee.wx.settings.RadarPreferences

enum class PolygonType(
    var color: Int,
    private val typeAsString: String,
    var pref: Boolean,
    var size: Float
) {

    MCD(
        RadarPreferences.colorMcd,
        "MCD",
        RadarPreferences.watMcd,
        RadarPreferences.watchMcdLineSize
    ),
    MPD(RadarPreferences.colorMpd, "MPD", RadarPreferences.mpd, RadarPreferences.watchMcdLineSize),
    WATCH(
        RadarPreferences.colorTstormWatch,
        "WATCH",
        RadarPreferences.watMcd,
        RadarPreferences.watchMcdLineSize
    ),
    WATCH_TORNADO(
        RadarPreferences.colorTorWatch,
        "WATCH_TORNADO",
        RadarPreferences.watMcd,
        RadarPreferences.watchMcdLineSize
    ),
    TST(
        RadarPreferences.colorTstorm,
        "TST",
        RadarPreferences.warnings,
        RadarPreferences.warnLineSize
    ),
    TOR(RadarPreferences.colorTor, "TOR", RadarPreferences.warnings, RadarPreferences.warnLineSize),
    FFW(RadarPreferences.colorFfw, "FFW", RadarPreferences.warnings, RadarPreferences.warnLineSize),
    SPOTTER(
        RadarPreferences.colorSpotter,
        "SPOTTER",
        RadarPreferences.spotters,
        RadarPreferences.spotterSize.toFloat()
    ),
    SPOTTER_LABELS(
        RadarPreferences.colorSpotter,
        "SPOTTER_LABELS",
        RadarPreferences.spottersLabel,
        0.0f
    ),
    WIND_BARB_GUSTS(
        Color.RED,
        "WIND_BARB_GUSTS",
        RadarPreferences.obsWindbarbs,
        RadarPreferences.wbLineSize.toFloat()
    ),
    WIND_BARB(
        RadarPreferences.colorObsWindbarbs,
        "WIND_BARB",
        RadarPreferences.obsWindbarbs,
        RadarPreferences.wbLineSize.toFloat()
    ),
    WIND_BARB_CIRCLE(
        RadarPreferences.colorObsWindbarbs,
        "WIND_BARB_CIRCLE",
        RadarPreferences.obsWindbarbs,
        RadarPreferences.aviationSize.toFloat()
    ),
    LOCDOT(
        RadarPreferences.colorLocdot,
        "LOCDOT",
        RadarPreferences.locDot,
        RadarPreferences.locdotSize.toFloat()
    ),
    STI(
        RadarPreferences.colorSti,
        "STI",
        RadarPreferences.sti,
        RadarPreferences.stiLineSize.toFloat()
    ),
    TVS(RadarPreferences.colorTor, "TVS", RadarPreferences.tvs, RadarPreferences.tvsSize.toFloat()),
    HI(
        RadarPreferences.colorHi,
        "HI",
        RadarPreferences.hailIndex,
        RadarPreferences.hiSize.toFloat()
    ),
    //elys mod
    HAIL_LABELS(RadarPreferences.colorHiText, "HAILSIZE_LABELS", RadarPreferences.hailSizeLabel, RadarPreferences.hiTextSize),
    OBS(RadarPreferences.colorObs, "OBS", RadarPreferences.obs, 0.0f),
    SWO(
        RadarPreferences.colorHi,
        "SWO",
        RadarPreferences.swo,
        RadarPreferences.swoLineSize.toFloat()
    ),
    WPC_FRONTS(
        RadarPreferences.colorHi,
        "WPC_FRONTS",
        RadarPreferences.wpcFronts,
        RadarPreferences.wpcFrontLineSize.toFloat()
    ),
    USERPOINTS(0, "USERPOINTS", RadarPreferences.userPoints, 0.0f),
    NONE(0, "", false, 0.0f);

    override fun toString() = typeAsString

    companion object {
        fun refresh() {
            MCD.pref = RadarPreferences.watMcd
            MPD.pref = RadarPreferences.mpd
            WATCH.pref = RadarPreferences.watMcd
            WATCH_TORNADO.pref = RadarPreferences.watMcd
            WPC_FRONTS.pref = RadarPreferences.wpcFronts
            TST.pref = RadarPreferences.warnings
            TOR.pref = RadarPreferences.warnings
            FFW.pref = RadarPreferences.warnings
            SPOTTER.pref = RadarPreferences.spotters
            SPOTTER_LABELS.pref = RadarPreferences.spottersLabel
            WIND_BARB_GUSTS.pref = RadarPreferences.obsWindbarbs
            WIND_BARB_GUSTS.size = RadarPreferences.wbLineSize.toFloat()
            WIND_BARB.pref = RadarPreferences.obsWindbarbs
            WIND_BARB.size = RadarPreferences.wbLineSize.toFloat()
            WIND_BARB_CIRCLE.pref = RadarPreferences.obsWindbarbs
            LOCDOT.pref = RadarPreferences.locDot
            STI.pref = RadarPreferences.sti
            TVS.pref = RadarPreferences.tvs
            HI.pref = RadarPreferences.hailIndex
	        HAIL_LABELS.pref = RadarPreferences.hailSizeLabel
            OBS.pref = RadarPreferences.obs
            SWO.pref = RadarPreferences.swo
            USERPOINTS.pref = RadarPreferences.userPoints
            MCD.color = RadarPreferences.colorMcd
            MPD.color = RadarPreferences.colorMpd
            WATCH.color = RadarPreferences.colorTstormWatch
            WATCH_TORNADO.color = RadarPreferences.colorTorWatch
            TST.color = RadarPreferences.colorTstorm
            TOR.color = RadarPreferences.colorTor
            FFW.color = RadarPreferences.colorFfw
            SPOTTER.color = RadarPreferences.colorSpotter
            WIND_BARB_GUSTS.color = Color.RED
            WIND_BARB.color = RadarPreferences.colorObsWindbarbs
            WIND_BARB_CIRCLE.color = RadarPreferences.colorObsWindbarbs
            LOCDOT.color = RadarPreferences.colorLocdot
            STI.color = RadarPreferences.colorSti
            STI.size = RadarPreferences.stiLineSize.toFloat()
            TVS.color = RadarPreferences.colorTor
            HI.color = RadarPreferences.colorHi
            OBS.color = RadarPreferences.colorObs
            SWO.color = RadarPreferences.colorHi
            SWO.size = RadarPreferences.swoLineSize.toFloat()
            MCD.size = RadarPreferences.watchMcdLineSize
            MPD.size = RadarPreferences.watchMcdLineSize
            WATCH.size = RadarPreferences.watchMcdLineSize
            WATCH_TORNADO.size = RadarPreferences.watchMcdLineSize
            WPC_FRONTS.size = RadarPreferences.watchMcdLineSize
            TST.size = RadarPreferences.warnLineSize
            TOR.size = RadarPreferences.warnLineSize
            FFW.size = RadarPreferences.warnLineSize
        }
    }
}

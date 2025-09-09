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

package joshuatee.wx.settings

import android.content.Context
import android.graphics.Color
import joshuatee.wx.objects.PolygonWarning
import joshuatee.wx.objects.PolygonWatch
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.radar.Metar
import joshuatee.wx.radar.UtilitySpotter
import joshuatee.wx.radar.SwoDayOne
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility

object RadarPreferences {

    //elys mod
    var sn_key = ""
    var sn_locationreport = false		
    var showRadarWhenPan = true
    var warnings = false
    var locationDotFollowsGps = false
    var locdotBug = false
    var dualpaneshareposn = false
    var spotters = false
    var spottersLabel = false
    var spottersLabelLastName = false
    var hailSizeLabel = false
    var obs = false
    var obsWindbarbs = false
    var swo = false
    var fire = false
    var userPoints = false
    var cities = false
    var locDot = false
    var countyLabels = false
    var countyHires = false
    var stateHires = false
    var watMcd = false
    var mpd = false
    var sti = false
    var hailIndex = false
    var tvs = false
    var hideRadar = false
    var showLegend = true
    var showLegendWidth = 0
    var showLegendTextSize = 0
    var drawToolSize = 0
    var obsExtZoom = 0
    var spotterSize = 0
    var aviationSize = 0
    var textSize = 0.0f
    var textSizeDefault = 1.0f
    var userPointSize = 0
    var locdotSize = 0
    var locIconSize = 0
    var locBugSize = 0
    var hiSize = 0
    var hiTextSize = 0f
    var tvsSize = 0
    var defaultLinesize = 0
    var warnLineSize = 5.0f
    const val WARNING_LINE_SIZE_DEFAULT = 5
    var watchMcdLineSize = 4.0f
    var watchMcdLineSizeDefault = 4
    var iconsLevel2 = false
    var gpsCircleLineSize = 0
    var stiLineSize = 0
    var swoLineSize = 0
    var wbLineSize = 0
    var wpcFrontLineSize = 0
    var wxoglCenterOnLocation = false
    var wpcFronts = false
    var spotterSizeDefault = 4
    var aviationSizeDefault = 7
    var locationDotSizeDefault = 8

    var colorTstorm = 0
    var colorTstormWatch = 0
    var colorTor = 0
    var colorTorWatch = 0
    var colorFfw = 0
    var colorMcd = 0
    var colorMpd = 0
    var colorLocdot = 0
    var colorSpotter = 0
    var colorCity = 0
    var colorSti = 0
    var colorHi = 0
    //elys mod
    var colorHiText = 0
    var colorObs = 0
    var colorObsWindbarbs = 0
    var colorCountyLabels = 0
    //elys mod
    var showLegendTextColor = 0
    var nexradBackgroundColor = 0

    var uiAnimIconFrames = "rid"
    var useJni = false
    var drawToolColor = 0

    var wxoglSize = 0
    var wxoglSizeDefault = 13
    var wxoglRememberLocation = false
    var wxoglRadarAutoRefresh = false
    var locationUpdateInterval = 10

    var wxoglZoom = 0.0f
    var wxoglRid = ""
    var wxoglProd = ""
    var wxoglX = 0.0f
    var wxoglY = 0.0f

    fun initialize(context: Context) {
        initializeColors(context)
	//elys mod
        sn_key = Utility.readPref(context, "SN_KEY", "")
        sn_locationreport = Utility.readPref(context, "SN_LOCATIONREPORT", "false").startsWith("t")
        showRadarWhenPan = Utility.readPref(context, "SHOW_RADAR_WHEN_PAN", "true").startsWith("t")
        wpcFronts = Utility.readPref(context, "RADAR_SHOW_WPC_FRONTS", "false").startsWith("t")
        locationUpdateInterval = Utility.readPrefInt(context, "RADAR_LOCATION_UPDATE_INTERVAL", 10)
        warnings = Utility.readPref(context, "COD_WARNINGS_DEFAULT", "false").startsWith("t")
        locationDotFollowsGps = Utility.readPref(context, "LOCDOT_FOLLOWS_GPS", "false").startsWith("t")
        locdotBug = Utility.readPref(context, "LOCDOT_BUG", "false").startsWith("t")
        dualpaneshareposn = Utility.readPref(context, "DUALPANE_SHARE_POSN", "true").startsWith("t")
        spotters = Utility.readPref(context, "WXOGL_SPOTTERS", "false").startsWith("t")
        spottersLabel = Utility.readPref(context, "WXOGL_SPOTTERS_LABEL", "false").startsWith("t")
        spottersLabelLastName = Utility.readPref(context, "WXOGL_SPOTTERS_LABEL_LASTNAME", "false").startsWith("t")
        hailSizeLabel = Utility.readPref(context, "WXOGL_HAIL_LABEL", "false").startsWith("t")
        obs = Utility.readPref(context, "WXOGL_OBS", "false").startsWith("t")
        obsWindbarbs = Utility.readPref(context, "WXOGL_OBS_WINDBARBS", "false").startsWith("t")
        swo = Utility.readPref(context, "RADAR_SHOW_SWO", "false").startsWith("t")
        fire = Utility.readPref(context, "RADAR_SHOW_FIRE", "false").startsWith("t")
        userPoints = Utility.readPref(context, "RADAR_USERPOINTS", "false").startsWith("t")
        cities = Utility.readPref(context, "COD_CITIES_DEFAULT", "").startsWith("t")
        locDot = Utility.readPref(context, "COD_LOCDOT_DEFAULT", "true").startsWith("t")
        watMcd = Utility.readPref(context, "RADAR_SHOW_WATCH", "false").startsWith("t")
        mpd = Utility.readPref(context, "RADAR_SHOW_MPD", "false").startsWith("t")
        sti = Utility.readPref(context, "RADAR_SHOW_STI", "false").startsWith("t")
        hailIndex = Utility.readPref(context, "RADAR_SHOW_HI", "false").startsWith("t")
        tvs = Utility.readPref(context, "RADAR_SHOW_TVS", "false").startsWith("t")
        countyLabels = Utility.readPref(context, "RADAR_COUNTY_LABELS", "false").startsWith("t")
        countyHires = Utility.readPref(context, "RADAR_COUNTY_HIRES", "false").startsWith("t")
        stateHires = Utility.readPref(context, "RADAR_STATE_HIRES", "false").startsWith("t")
        iconsLevel2 = Utility.readPref(context, "WXOGL_ICONS_LEVEL2", "false").startsWith("t")
        hideRadar = Utility.readPref(context, "RADAR_HIDE_RADAR", "false").startsWith("t")
        showLegend = Utility.readPref(context, "RADAR_SHOW_LEGEND", "false").startsWith("t")
        showLegendWidth = Utility.readPrefInt(context, "RADAR_SHOW_LEGEND_WIDTH", 50)
        showLegendTextSize = Utility.readPrefInt(context, "RADAR_SHOW_LEGEND_TEXTSIZE", 30)
        wxoglCenterOnLocation = Utility.readPref(context, "RADAR_CENTER_ON_LOCATION", "false").startsWith("t")
        drawToolSize = Utility.readPrefInt(context, "DRAWTOOL_SIZE", 6) //was 6
        if (UtilityUI.isTablet()) {
            spotterSizeDefault = 2
            aviationSizeDefault = 3
            locationDotSizeDefault = 4
        }
        obsExtZoom = Utility.readPrefInt(context, "RADAR_OBS_EXT_ZOOM", 7)
        spotterSize = Utility.readPrefInt(context, "RADAR_SPOTTER_SIZE", spotterSizeDefault)
        aviationSize = Utility.readPrefInt(context, "RADAR_AVIATION_SIZE", aviationSizeDefault)
        textSize = Utility.readPrefFloat(context, "RADAR_TEXT_SIZE", textSizeDefault)
        userPointSize = Utility.readPrefInt(context, "RADAR_USERPOINT_SIZE", 100)
        locdotSize = Utility.readPrefInt(context, "RADAR_LOCDOT_SIZE", locationDotSizeDefault)
        locIconSize = Utility.readPrefInt(context, "RADAR_LOCICON_SIZE", 100)
        locBugSize = Utility.readPrefInt(context, "RADAR_LOCBUG_SIZE", 100)
        hiSize = Utility.readPrefInt(context, "RADAR_HI_SIZE", 75)
        hiTextSize = Utility.readPrefFloat(context, "RADAR_HI_TEXT_SIZE", 1.0f)
        tvsSize = Utility.readPrefInt(context, "RADAR_TVS_SIZE", 75)
        defaultLinesize = Utility.readPrefInt(context, "RADAR_DEFAULT_LINESIZE", 1)
        warnLineSize = Utility.readPrefInt(context, "RADAR_WARN_LINESIZE", WARNING_LINE_SIZE_DEFAULT).toFloat()
        watchMcdLineSize = Utility.readPrefInt(context, "RADAR_WATMCD_LINESIZE", watchMcdLineSizeDefault).toFloat()
        gpsCircleLineSize = Utility.readPrefInt(context, "RADAR_GPSCIRCLE_LINESIZE", 5)
        stiLineSize = Utility.readPrefInt(context, "RADAR_STI_LINESIZE", 3)
        swoLineSize = Utility.readPrefInt(context, "RADAR_SWO_LINESIZE", 3)
        wbLineSize = Utility.readPrefInt(context, "RADAR_WB_LINESIZE", 3)
        wpcFrontLineSize = Utility.readPrefInt(context, "RADAR_WPC_FRONT_LINESIZE", 4)
        uiAnimIconFrames = Utility.readPref(context, "UI_ANIM_ICON_FRAMES", "10")
        useJni = Utility.readPref(context, "RADAR_USE_JNI", "false").startsWith("t")
        drawToolColor = Utility.readPrefInt(context, "DRAW_TOOL_COLOR", Color.rgb(143, 213, 253))

        if (UtilityUI.isTablet()) {
            wxoglSizeDefault = 8
        }
        wxoglSize = Utility.readPrefInt(context, "WXOGL_SIZE", wxoglSizeDefault)
        wxoglRememberLocation = Utility.readPref(context, "WXOGL_REMEMBER_LOCATION", "false").startsWith("t")
        wxoglRadarAutoRefresh = Utility.readPref(context, "RADAR_AUTOREFRESH", "false").startsWith("t")
        wxoglZoom = Utility.readPrefFloat(context, "WXOGL_ZOOM", wxoglSize.toFloat() / 10.0f)
        wxoglRid = Utility.readPref(context, "WXOGL_RID", "")
        wxoglProd = Utility.readPref(context, "WXOGL_PROD", "N0Q")
        wxoglX = Utility.readPrefFloat(context, "WXOGL_X", 0.0f)
        wxoglY = Utility.readPrefFloat(context, "WXOGL_Y", 0.0f)

        resetTimerOnRadarPolygons()
    }

    private fun initializeColors(context: Context) {
        colorTstorm = Utility.readPrefInt(context, "RADAR_COLOR_TSTORM", Color.YELLOW)
        colorTstormWatch = Utility.readPrefInt(context, "RADAR_COLOR_TSTORM_WATCH", Color.BLUE)
        colorTor = Utility.readPrefInt(context, "RADAR_COLOR_TOR", Color.RED)
        colorTorWatch = Utility.readPrefInt(context, "RADAR_COLOR_TOR_WATCH", Color.rgb(113, 0, 0)) //dark red for watch lines
        colorFfw = Utility.readPrefInt(context, "RADAR_COLOR_FFW", Color.GREEN)
        colorMcd = Utility.readPrefInt(context, "RADAR_COLOR_MCD", Color.rgb(255, 255, 163))
        colorMpd = Utility.readPrefInt(context, "RADAR_COLOR_MPD", Color.GREEN)
        colorLocdot = Utility.readPrefInt(context, "RADAR_COLOR_LOCDOT", Color.WHITE)
        colorSpotter = Utility.readPrefInt(context, "RADAR_COLOR_SPOTTER", Color.GREEN)
        colorCity = Utility.readPrefInt(context, "RADAR_COLOR_CITY", Color.WHITE)
        colorSti = Utility.readPrefInt(context, "RADAR_COLOR_STI", Color.WHITE)
        colorHi = Utility.readPrefInt(context, "RADAR_COLOR_HI", Color.GREEN)
        colorHiText = Utility.readPrefInt(context, "RADAR_COLOR_HI_TEXT", Color.GREEN)
        colorObs = Utility.readPrefInt(context, "RADAR_COLOR_OBS", Color.rgb(255, 255, 255))
        colorObsWindbarbs = Utility.readPrefInt(context, "RADAR_COLOR_OBS_WINDBARBS", Color.WHITE)
        colorCountyLabels = Utility.readPrefInt(context, "RADAR_COLOR_COUNTY_LABELS", Color.rgb(75, 75, 75))
        showLegendTextColor = Utility.readPrefInt(context, "RADAR_SHOW_LEGEND_TEXTCOLOR", Color.WHITE)
        nexradBackgroundColor = Utility.readPrefInt(context, "NEXRAD_RADAR_BACKGROUND_COLOR", Color.rgb(0, 0, 0))
    }

    private fun resetTimerOnRadarPolygons() {
        PolygonWatch.byType[PolygonType.MCD]?.timer?.resetTimer()
        PolygonWatch.byType[PolygonType.MPD]?.timer?.resetTimer()
        PolygonWatch.byType[PolygonType.WATCH]?.timer?.resetTimer()
        PolygonWarning.byType.values.forEach {
            it.timer.resetTimer()
        }
        Metar.timer.resetTimer()
        UtilitySpotter.timer.resetTimer()
        SwoDayOne.timer.resetTimer()
    }
}

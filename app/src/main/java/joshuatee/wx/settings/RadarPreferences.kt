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
//not removing color options for geo stuff 

package joshuatee.wx.settings

import android.content.Context
import android.graphics.Color
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.ObjectPolygonWarning
import joshuatee.wx.objects.ObjectPolygonWatch
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.radar.*
import joshuatee.wx.radar.UtilityMetar
import joshuatee.wx.radar.UtilitySwoDayOne
import joshuatee.wx.ui.UtilityUI

object RadarPreferences {

	
    //elys mod
    var sn_key = ""
    var sn_locationreport = false
		
    //
    // Radar Preferences
    //
    var conusRadar = false
    var conusRadarZoom = 75 //was 173
    var showRadarWhenPan = true
    var warnings = false
    var locationDotFollowsGps = false
    var locdotBug = false
    var dualpaneshareposn = false
    var spotters = false
    var spottersLabel = false
    var hailSizeLabel = false
    var obs = false
    var obsWindbarbs = false
    var swo = false
    var userPoints = false
    var cities = false
    var hw = false
    var locDot = false
    var lakes = false
    var county = false
    var countyLabels = false
    var countyHires = false
    var stateHires = false
    var watMcd = false
    var mpd = false
    var sti = false
    var hi = false
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
    const val warnLineSizeDefault = 5
    var watchMcdLineSize = 4.0f
    var watchMcdLineSizeDefault = 4
    var hwEnhExt = false
    var camxBorders = false
    var iconsLevel2 = false
    var stateLineSize = 0
    var countyLineSize = 0
    var hwLineSize = 0
    var hwExtLineSize = 0
    var lakeLineSize = 0
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

    var colorHw = 0
    var colorHwExt = 0
    var colorState = 0
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
    var colorLakes = 0
    var colorCounty = 0
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
    var blackBg = false


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

    fun initRadarPreferences() {
	
	//elys mod
        sn_key = getInitialPreferenceString("SN_KEY", "")
        sn_locationreport = getInitialPreference("SN_LOCATIONREPORT", "")
	showRadarWhenPan = getInitialPreference("SHOW_RADAR_WHEN_PAN", "true")		
        wpcFronts = getInitialPreference("RADAR_SHOW_WPC_FRONTS", "false")
        locationUpdateInterval = getInitialPreference("RADAR_LOCATION_UPDATE_INTERVAL", 10)
        conusRadar = getInitialPreference("CONUS_RADAR", "false")
        conusRadarZoom = getInitialPreference("CONUS_RADAR_ZOOM", 75)
        warnings = getInitialPreference("COD_WARNINGS_DEFAULT", "false")
        locationDotFollowsGps = getInitialPreference("LOCDOT_FOLLOWS_GPS", "false")
        locdotBug = getInitialPreference("LOCDOT_BUG", "false")
        dualpaneshareposn = getInitialPreference("DUALPANE_SHARE_POSN", "true")
        spotters = getInitialPreference("WXOGL_SPOTTERS", "false")
        spottersLabel = getInitialPreference("WXOGL_SPOTTERS_LABEL", "false")
        hailSizeLabel = getInitialPreference("WXOGL_HAIL_LABEL", "false")
        obs = getInitialPreference("WXOGL_OBS", "false")
        obsWindbarbs = getInitialPreference("WXOGL_OBS_WINDBARBS", "false")
        swo = getInitialPreference("RADAR_SHOW_SWO", "false")
        userPoints = getInitialPreference("RADAR_USERPOINTS", "false")
        cities = getInitialPreference("COD_CITIES_DEFAULT", "")
        hw = getInitialPreference("COD_HW_DEFAULT", "true")
        locDot = getInitialPreference("COD_LOCDOT_DEFAULT", "true")
        lakes = getInitialPreference("COD_LAKES_DEFAULT", "false")
        county = getInitialPreference("RADAR_SHOW_COUNTY", "true")
        watMcd = getInitialPreference("RADAR_SHOW_WATCH", "false")
        mpd = getInitialPreference("RADAR_SHOW_MPD", "false")
        sti = getInitialPreference("RADAR_SHOW_STI", "false")
        hi = getInitialPreference("RADAR_SHOW_HI", "false")
        tvs = getInitialPreference("RADAR_SHOW_TVS", "false")
        hwEnhExt = getInitialPreference("RADAR_HW_ENH_EXT", "false")
        camxBorders = getInitialPreference("RADAR_CAMX_BORDERS", "false")
        countyLabels = getInitialPreference("RADAR_COUNTY_LABELS", "false")
        countyHires = getInitialPreference("RADAR_COUNTY_HIRES", "false")
        stateHires = getInitialPreference("RADAR_STATE_HIRES", "false")
        iconsLevel2 = getInitialPreference("WXOGL_ICONS_LEVEL2", "false")
        hideRadar = getInitialPreference("RADAR_HIDE_RADAR", "false")
        showLegend = getInitialPreference("RADAR_SHOW_LEGEND", "false")
        showLegendWidth = getInitialPreference("RADAR_SHOW_LEGEND_WIDTH", 50)
        showLegendTextSize = getInitialPreference("RADAR_SHOW_LEGEND_TEXTSIZE", 30)
        wxoglCenterOnLocation = getInitialPreference("RADAR_CENTER_ON_LOCATION", "false")
        drawToolSize = getInitialPreference("DRAWTOOL_SIZE", 4)

        if (UtilityUI.isTablet()) {
            spotterSizeDefault = 2
            aviationSizeDefault = 3
            locationDotSizeDefault = 4
        }

        obsExtZoom = getInitialPreference("RADAR_OBS_EXT_ZOOM", 7)
        spotterSize = getInitialPreference("RADAR_SPOTTER_SIZE", spotterSizeDefault)
        aviationSize = getInitialPreference("RADAR_AVIATION_SIZE", aviationSizeDefault)
        textSize = getInitialPreference("RADAR_TEXT_SIZE", textSizeDefault)
        userPointSize = getInitialPreference("RADAR_USERPOINT_SIZE", 100)
        locdotSize = getInitialPreference("RADAR_LOCDOT_SIZE", locationDotSizeDefault)
        locIconSize = getInitialPreference("RADAR_LOCICON_SIZE", 100)
        locBugSize = getInitialPreference("RADAR_LOCBUG_SIZE", 100)
        hiSize = getInitialPreference("RADAR_HI_SIZE", 75)
        hiTextSize = getInitialPreference("RADAR_HI_TEXT_SIZE", 1.0f)
        tvsSize = getInitialPreference("RADAR_TVS_SIZE", 75)
        defaultLinesize = getInitialPreference("RADAR_DEFAULT_LINESIZE", 1)
        warnLineSize = getInitialPreference("RADAR_WARN_LINESIZE", warnLineSizeDefault).toFloat()
        watchMcdLineSize = getInitialPreference("RADAR_WATMCD_LINESIZE", watchMcdLineSizeDefault).toFloat()
        stateLineSize = getInitialPreference("RADAR_STATE_LINESIZE", 2)
        countyLineSize = getInitialPreference("RADAR_COUNTY_LINESIZE", 2)
        hwLineSize = getInitialPreference("RADAR_HW_LINESIZE", 2)
        hwExtLineSize = getInitialPreference("RADAR_HWEXT_LINESIZE", 2)
        lakeLineSize = getInitialPreference("RADAR_LAKE_LINESIZE", 2)
        gpsCircleLineSize = getInitialPreference("RADAR_GPSCIRCLE_LINESIZE", 5)
        stiLineSize = getInitialPreference("RADAR_STI_LINESIZE", 3)
        swoLineSize = getInitialPreference("RADAR_SWO_LINESIZE", 3)
        wbLineSize = getInitialPreference("RADAR_WB_LINESIZE", 3)
        wpcFrontLineSize = getInitialPreference("RADAR_WPC_FRONT_LINESIZE", 4)

        uiAnimIconFrames = getInitialPreferenceString("UI_ANIM_ICON_FRAMES", "10")
        useJni = getInitialPreference("RADAR_USE_JNI", "false")
        drawToolColor = getInitialPreference("DRAW_TOOL_COLOR", Color.rgb(255, 0, 0))
        blackBg = getInitialPreference("NWS_RADAR_BG_BLACK", "")

        if (UtilityUI.isTablet()) {
            wxoglSizeDefault = 8
        }
        wxoglSize = getInitialPreference("WXOGL_SIZE", wxoglSizeDefault)
        wxoglRememberLocation = getInitialPreference("WXOGL_REMEMBER_LOCATION", "false")
        wxoglRadarAutoRefresh = getInitialPreference("RADAR_AUTOREFRESH", "false")
        wxoglZoom = MyApplication.preferences.getFloat("WXOGL_ZOOM", wxoglSize.toFloat() / 10.0f)
        wxoglRid = getInitialPreferenceString("WXOGL_RID", "")
        wxoglProd = getInitialPreferenceString("WXOGL_PROD", "N0Q")
        wxoglX = getInitialPreference("WXOGL_X", 0.0f)
        wxoglY = getInitialPreference("WXOGL_Y", 0.0f)

        resetTimerOnRadarPolygons()
    }

    fun radarGeometrySetColors() {
        colorHw = getInitialPreference("RADAR_COLOR_HW", Color.BLUE)
        colorHwExt = getInitialPreference("RADAR_COLOR_HW_EXT", Color.BLUE)
        colorState = getInitialPreference("RADAR_COLOR_STATE", Color.WHITE)
        colorTstorm = getInitialPreference("RADAR_COLOR_TSTORM", Color.YELLOW)
        colorTstormWatch = getInitialPreference("RADAR_COLOR_TSTORM_WATCH", Color.BLUE)
        colorTor = getInitialPreference("RADAR_COLOR_TOR", Color.RED)
        colorTorWatch = getInitialPreference("RADAR_COLOR_TOR_WATCH", Color.rgb(113, 0, 0)) //dark red for watch lines
        colorFfw = getInitialPreference("RADAR_COLOR_FFW", Color.GREEN)
        colorMcd = getInitialPreference("RADAR_COLOR_MCD", Color.rgb(255, 255, 163))
        colorMpd = getInitialPreference("RADAR_COLOR_MPD", Color.GREEN)
        colorLocdot = getInitialPreference("RADAR_COLOR_LOCDOT", Color.WHITE)
        colorSpotter = getInitialPreference("RADAR_COLOR_SPOTTER", Color.GREEN)
        colorCity = getInitialPreference("RADAR_COLOR_CITY", Color.WHITE)
        colorLakes = getInitialPreference("RADAR_COLOR_LAKES", Color.rgb(0, 0, 163))
        colorCounty = getInitialPreference("RADAR_COLOR_COUNTY", Color.rgb(75, 75, 75))
        colorSti = getInitialPreference("RADAR_COLOR_STI", Color.WHITE)
        colorHi = getInitialPreference("RADAR_COLOR_HI", Color.GREEN)
        colorHiText = getInitialPreference("RADAR_COLOR_HI_TEXT", Color.GREEN)
        colorObs = getInitialPreference("RADAR_COLOR_OBS", Color.rgb(255, 255, 255))
        colorObsWindbarbs = getInitialPreference("RADAR_COLOR_OBS_WINDBARBS", Color.WHITE)
        colorCountyLabels = getInitialPreference("RADAR_COLOR_COUNTY_LABELS", Color.rgb(75, 75, 75))
        showLegendTextColor = getInitialPreference("RADAR_SHOW_LEGEND_TEXTCOLOR", Color.WHITE)
        nexradBackgroundColor = getInitialPreference("NEXRAD_RADAR_BACKGROUND_COLOR", Color.rgb(0, 0, 0))

    }

    fun initGenericRadarWarnings(context: Context) {
        ObjectPolygonWarning.load(context)
    }

    private fun resetTimerOnRadarPolygons() {
        ObjectPolygonWatch.polygonDataByType[PolygonType.MCD]?.timer?.resetTimer()
        ObjectPolygonWatch.polygonDataByType[PolygonType.MPD]?.timer?.resetTimer()
        ObjectPolygonWatch.polygonDataByType[PolygonType.WATCH]?.timer?.resetTimer()
        ObjectPolygonWarning.polygonDataByType.values.forEach {
            it.timer.resetTimer()
        }
        UtilityMetar.timer.resetTimer()
        UtilitySpotter.timer.resetTimer()
        UtilitySwoDayOne.timer.resetTimer()
    }

    private fun getInitialPreference(pref: String, initValue: Int) = MyApplication.preferences.getInt(pref, initValue)

    private fun getInitialPreference(pref: String, initValue: Float) = MyApplication.preferences.getFloat(pref, initValue)

    private fun getInitialPreference(pref: String, initValue: String) = (MyApplication.preferences.getString(pref, initValue) ?: initValue).startsWith("t")

    private fun getInitialPreferenceString(pref: String, initValue: String) = MyApplication.preferences.getString(pref, initValue) ?: initValue
}

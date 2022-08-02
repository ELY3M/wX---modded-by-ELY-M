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

import android.content.Context
import android.graphics.Color
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.ObjectPolygonWarning
import joshuatee.wx.objects.ObjectPolygonWatch
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.PolygonWarningType
import joshuatee.wx.radar.*
import joshuatee.wx.radar.UtilityDownloadWarnings
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
    var radarConusRadar = false
    var radarConusRadarZoom = 75 //was 173
    var radarWarnings = false
    var locationDotFollowsGps = false
    var locdotBug = false
    var dualpaneshareposn = false
    var radarSpotters = false
    var radarSpottersLabel = false
    var radarHailSizeLabel = false
    var radarObs = false
    var radarObsWindbarbs = false
    var radarSwo = false
    var radarUserPoints = false
    var radarCities = false
    var radarHw = false
    var radarLocDot = false
    var radarLakes = false
    var radarCounty = false
    var radarCountyLabels = false
    var radarCountyHires = false
    var radarStateHires = false
    var radarWatMcd = false
    var radarMpd = false
    var radarSti = false
    var radarHi = false
    var radarTvs = false
    var radarShowRadar = true
    var radarShowLegend = false
    var radarShowLegendWidth = 0
    var radarShowLegendTextSize = 0
    var drawToolSize = 0
    var radarObsExtZoom = 0
    var radarSpotterSize = 0
    var radarAviationSize = 0
    var radarTextSize = 0.0f
    var radarTextSizeDefault = 1.0f
    var radarUserPointSize = 0
    var radarLocdotSize = 0
    var radarLocIconSize = 0
    var radarLocBugSize = 0
    var radarHiSize = 0
    var radarHiTextSize = 0f
    var radarTvsSize = 0
    var radarDefaultLinesize = 0
    var radarWarnLineSize = 5.0f
    const val radarWarnLineSizeDefault = 5
    var radarWatchMcdLineSize = 4.0f
    var radarWatchMcdLineSizeDefault = 4
    var radarHwEnhExt = false
    var radarCamxBorders = false
    var radarIconsLevel2 = false
    var radarStateLineSize = 0
    var radarCountyLineSize = 0
    var radarHwLineSize = 0
    var radarHwExtLineSize = 0
    var radarLakeLineSize = 0
    var radarGpsCircleLineSize = 0
    var radarStiLineSize = 0
    var radarSwoLineSize = 0
    var radarWbLineSize = 0
    var wxoglCenterOnLocation = false
    var radarShowWpcFronts = false
    var radarSpotterSizeDefault = 4
    var radarAviationSizeDefault = 7
    var radarLocationDotSizeDefault = 8

    var radarColorHw = 0
    var radarColorHwExt = 0
    var radarColorState = 0
    var radarColorTstorm = 0
    var radarColorTstormWatch = 0
    var radarColorTor = 0
    var radarColorTorWatch = 0
    var radarColorFfw = 0
    var radarColorMcd = 0
    var radarColorMpd = 0
    var radarColorLocdot = 0
    var radarColorSpotter = 0
    var radarColorCity = 0
    var radarColorLakes = 0
    var radarColorCounty = 0
    var radarColorSti = 0
    var radarColorHi = 0
    //elys mod
    var radarColorHiText = 0
    var radarColorObs = 0
    var radarColorObsWindbarbs = 0
    var radarColorCountyLabels = 0
    //elys mod
    var radarShowLegendTextColor = 0
    var nexradRadarBackgroundColor = 0
	
    var radarWarningPolygons = mutableListOf<ObjectPolygonWarning>()

    var uiAnimIconFrames = "rid"
    var radarUseJni = false
    var drawToolColor = 0
    var blackBg = false


    var wxoglSize = 0
    var wxoglSizeDefault = 13
    var wxoglRememberLocation = false
    var wxoglRadarAutoRefresh = false
    var radarLocationUpdateInterval = 10

    var wxoglZoom = 0.0f
    var wxoglRid = ""
    var wxoglProd = ""
    var wxoglX = 0.0f
    var wxoglY = 0.0f

    fun initRadarPreferences() {
	
	//elys mod
        sn_key = getInitialPreferenceString("SN_KEY", "")
        sn_locationreport = getInitialPreference("SN_LOCATIONREPORT", "")
			
        radarShowWpcFronts = getInitialPreference("RADAR_SHOW_WPC_FRONTS", "false")
        radarLocationUpdateInterval = getInitialPreference("RADAR_LOCATION_UPDATE_INTERVAL", 10)
        radarConusRadar = getInitialPreference("CONUS_RADAR", "false")
        radarConusRadarZoom = getInitialPreference("CONUS_RADAR_ZOOM", 75)
        radarWarnings = getInitialPreference("COD_WARNINGS_DEFAULT", "false")
        locationDotFollowsGps = getInitialPreference("LOCDOT_FOLLOWS_GPS", "false")
        locdotBug = getInitialPreference("LOCDOT_BUG", "false")
        dualpaneshareposn = getInitialPreference("DUALPANE_SHARE_POSN", "true")
        radarSpotters = getInitialPreference("WXOGL_SPOTTERS", "false")
        radarSpottersLabel = getInitialPreference("WXOGL_SPOTTERS_LABEL", "false")
        radarHailSizeLabel = getInitialPreference("WXOGL_HAIL_LABEL", "false")
        radarObs = getInitialPreference("WXOGL_OBS", "false")
        radarObsWindbarbs = getInitialPreference("WXOGL_OBS_WINDBARBS", "false")
        radarSwo = getInitialPreference("RADAR_SHOW_SWO", "false")
        radarUserPoints = getInitialPreference("RADAR_USERPOINTS", "false")
        radarCities = getInitialPreference("COD_CITIES_DEFAULT", "")
        radarHw = getInitialPreference("COD_HW_DEFAULT", "true")
        radarLocDot = getInitialPreference("COD_LOCDOT_DEFAULT", "true")
        radarLakes = getInitialPreference("COD_LAKES_DEFAULT", "false")
        radarCounty = getInitialPreference("RADAR_SHOW_COUNTY", "true")
        radarWatMcd = getInitialPreference("RADAR_SHOW_WATCH", "false")
        radarMpd = getInitialPreference("RADAR_SHOW_MPD", "false")
        radarSti = getInitialPreference("RADAR_SHOW_STI", "false")
        radarHi = getInitialPreference("RADAR_SHOW_HI", "false")
        radarTvs = getInitialPreference("RADAR_SHOW_TVS", "false")
        radarHwEnhExt = getInitialPreference("RADAR_HW_ENH_EXT", "false")
        radarCamxBorders = getInitialPreference("RADAR_CAMX_BORDERS", "false")
        radarCountyLabels = getInitialPreference("RADAR_COUNTY_LABELS", "false")
        radarCountyHires = getInitialPreference("RADAR_COUNTY_HIRES", "false")
        radarStateHires = getInitialPreference("RADAR_STATE_HIRES", "false")
        radarIconsLevel2 = getInitialPreference("WXOGL_ICONS_LEVEL2", "false")
        radarShowRadar = getInitialPreference("RADAR_SHOW_RADAR", "false")
        radarShowLegend = getInitialPreference("RADAR_SHOW_LEGEND", "false")
        radarShowLegendWidth = getInitialPreference("RADAR_SHOW_LEGEND_WIDTH", 50)
        radarShowLegendTextSize = getInitialPreference("RADAR_SHOW_LEGEND_TEXTSIZE", 30)
        wxoglCenterOnLocation = getInitialPreference("RADAR_CENTER_ON_LOCATION", "false")
        drawToolSize = getInitialPreference("DRAWTOOL_SIZE", 4)

        if (UtilityUI.isTablet()) {
            radarSpotterSizeDefault = 2
            radarAviationSizeDefault = 3
            radarLocationDotSizeDefault = 4
        }

        radarObsExtZoom = getInitialPreference("RADAR_OBS_EXT_ZOOM", 7)
        radarSpotterSize = getInitialPreference("RADAR_SPOTTER_SIZE", radarSpotterSizeDefault)
        radarAviationSize = getInitialPreference("RADAR_AVIATION_SIZE", radarAviationSizeDefault)
        radarTextSize = getInitialPreference("RADAR_TEXT_SIZE", radarTextSizeDefault)
        radarUserPointSize = getInitialPreference("RADAR_USERPOINT_SIZE", 100)
        radarLocdotSize = getInitialPreference("RADAR_LOCDOT_SIZE", radarLocationDotSizeDefault)
        radarLocIconSize = getInitialPreference("RADAR_LOCICON_SIZE", 100)
        radarLocBugSize = getInitialPreference("RADAR_LOCBUG_SIZE", 100)
        radarHiSize = getInitialPreference("RADAR_HI_SIZE", 75)
        radarHiTextSize = getInitialPreference("RADAR_HI_TEXT_SIZE", 1.0f)
        radarTvsSize = getInitialPreference("RADAR_TVS_SIZE", 75)
        radarDefaultLinesize = getInitialPreference("RADAR_DEFAULT_LINESIZE", 1)
        radarWarnLineSize = getInitialPreference("RADAR_WARN_LINESIZE", radarWarnLineSizeDefault).toFloat()
        radarWatchMcdLineSize = getInitialPreference("RADAR_WATMCD_LINESIZE", radarWatchMcdLineSizeDefault).toFloat()
        radarStateLineSize = getInitialPreference("RADAR_STATE_LINESIZE", 2)
        radarCountyLineSize = getInitialPreference("RADAR_COUNTY_LINESIZE", 2)
        radarHwLineSize = getInitialPreference("RADAR_HW_LINESIZE", 2)
        radarHwExtLineSize = getInitialPreference("RADAR_HWEXT_LINESIZE", 2)
        radarLakeLineSize = getInitialPreference("RADAR_LAKE_LINESIZE", 2)
        radarGpsCircleLineSize = getInitialPreference("RADAR_GPSCIRCLE_LINESIZE", 5)
        radarStiLineSize = getInitialPreference("RADAR_STI_LINESIZE", 3)
        radarSwoLineSize = getInitialPreference("RADAR_SWO_LINESIZE", 3)
        radarWbLineSize = getInitialPreference("RADAR_WB_LINESIZE", 3)

        uiAnimIconFrames = getInitialPreferenceString("UI_ANIM_ICON_FRAMES", "10")
        radarUseJni = getInitialPreference("RADAR_USE_JNI", "false")
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
        radarColorHw = getInitialPreference("RADAR_COLOR_HW", Color.BLUE)
        radarColorHwExt = getInitialPreference("RADAR_COLOR_HW_EXT", Color.BLUE)
        radarColorState = getInitialPreference("RADAR_COLOR_STATE", Color.WHITE)
        radarColorTstorm = getInitialPreference("RADAR_COLOR_TSTORM", Color.YELLOW)
        radarColorTstormWatch = getInitialPreference("RADAR_COLOR_TSTORM_WATCH", Color.BLUE)
        radarColorTor = getInitialPreference("RADAR_COLOR_TOR", Color.RED)
        radarColorTorWatch = getInitialPreference("RADAR_COLOR_TOR_WATCH", Color.rgb(113, 0, 0)) //dark red for watch lines
        radarColorFfw = getInitialPreference("RADAR_COLOR_FFW", Color.GREEN)
        radarColorMcd = getInitialPreference("RADAR_COLOR_MCD", Color.rgb(255, 255, 163))
        radarColorMpd = getInitialPreference("RADAR_COLOR_MPD", Color.GREEN)
        radarColorLocdot = getInitialPreference("RADAR_COLOR_LOCDOT", Color.WHITE)
        radarColorSpotter = getInitialPreference("RADAR_COLOR_SPOTTER", Color.GREEN)
        radarColorCity = getInitialPreference("RADAR_COLOR_CITY", Color.WHITE)
        radarColorLakes = getInitialPreference("RADAR_COLOR_LAKES", Color.rgb(0, 0, 163))
        radarColorCounty = getInitialPreference("RADAR_COLOR_COUNTY", Color.rgb(75, 75, 75))
        radarColorSti = getInitialPreference("RADAR_COLOR_STI", Color.WHITE)
        radarColorHi = getInitialPreference("RADAR_COLOR_HI", Color.GREEN)
        radarColorHiText = getInitialPreference("RADAR_COLOR_HI_TEXT", Color.GREEN)
        radarColorObs = getInitialPreference("RADAR_COLOR_OBS", Color.rgb(255, 255, 255))
        radarColorObsWindbarbs = getInitialPreference("RADAR_COLOR_OBS_WINDBARBS", Color.WHITE)
        radarColorCountyLabels = getInitialPreference("RADAR_COLOR_COUNTY_LABELS", Color.rgb(75, 75, 75))
        radarShowLegendTextColor = getInitialPreference("RADAR_SHOW_LEGEND_TEXTCOLOR", Color.WHITE)
        nexradRadarBackgroundColor = getInitialPreference("NEXRAD_RADAR_BACKGROUND_COLOR", Color.rgb(0, 0, 0))

    }

    fun initGenericRadarWarnings(context: Context) {
        // FIXME don't need both, prefer top one shared with iOS
        ObjectPolygonWarning.load(context)
        radarWarningPolygons.clear()
        PolygonWarningType.values().forEach {
            radarWarningPolygons.add(ObjectPolygonWarning(context, it))
        }
    }

    private fun resetTimerOnRadarPolygons() {
//        UtilityDownloadMcd.timer.resetTimer()
        ObjectPolygonWatch.polygonDataByType[PolygonType.MCD]?.timer?.resetTimer()
        ObjectPolygonWatch.polygonDataByType[PolygonType.MPD]?.timer?.resetTimer()
        ObjectPolygonWatch.polygonDataByType[PolygonType.WATCH]?.timer?.resetTimer()
        UtilityDownloadWarnings.timer.resetTimer()
        UtilityDownloadWarnings.timerSevereDashboard.resetTimer()
//        UtilityDownloadWatch.timer.resetTimer()
        UtilityMetar.timer.resetTimer()
        UtilitySpotter.timer.resetTimer()
        UtilitySwoDayOne.timer.resetTimer()
    }

    private fun getInitialPreference(pref: String, initValue: Int) = MyApplication.preferences.getInt(pref, initValue)

    private fun getInitialPreference(pref: String, initValue: Float) = MyApplication.preferences.getFloat(pref, initValue)

    private fun getInitialPreference(pref: String, initValue: String) = (MyApplication.preferences.getString(pref, initValue) ?: initValue).startsWith("t")

    private fun getInitialPreferenceString(pref: String, initValue: String) = MyApplication.preferences.getString(pref, initValue) ?: initValue

}

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
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.core.content.ContextCompat
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.FavoriteType
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityFavorites

object UIPreferences {

    var deviceScale = 0.0f
    var cardCorners = 0.0f
    var padding = 0
    var paddingSettings = 0
    var paddingSmall = 0
    var lLpadding = 0.0f
    var actionBarHeight = 0
    var refreshLocMin = 0
    var translateText = false
    var recordScreenShare = false
    var backgroundColor = Color.BLACK //elys mod
    var colorNotif = 0
    var colorBlack = 0
    var colorOffwhiteToolbar = 0
    var dualpaneRadarIcon = false
    var homescreenTextLength = 0
    var tilesPerRow = 3
    var tilesPerRowDefault = 3
    var themeStr = ""
    var themeInt = 11 //elys mod
    val themes = listOf(
        "blue",
        "mixedBlue",
        "darkBlue",
        "black",
        "allBlack",
        "green",
        "gray",
        "white",
        "whiteNew",
        "allWhite",
        "orange",
	"BlackAqua",
	"BlackNeonGreen"	    
    )
    var textHighlightColor = 0
    var textSmallThemeColor = 0
    var themeIsWhite = false //elys mod
    var hideTopToolbar = false
    var mainScreenRadarFab = false //elys mod - I hate fab buttons
    var locfragDontShowIcons = false
    var nwsIconSizeDefault = 20
    var nwsIconSize = 0
    var normalTextSizeDefault = 16
    var normalTextSize = 16
    var navDrawerMainScreen = false
    var navDrawerMainScreenOnRight = true
    var useNwsApi = true
    var useNwsApiForHourly = true
    var tabHeaders = arrayOf("", "", "")
    var widgetPreventTap = false
    var fullscreenMode = false
    var lockToolbars = false
    var unitsM = false
    var unitsF = false
    var hourlyShowAMPM = false
    var widgetTextColor = 0
    var widgetHighlightTextColor = 0
    var widgetNexradSize = 0
    var nwsIconTextColor = 0
    var nwsIconBottomColor = 0
    var cardElevation = 0.0f
    var fabElevation = 0.0f
    var fabElevationDepressed = 0.0f
    var elevationPref = 0.0f
    var elevationPrefDefault = 5
	//elys mod 
	var checkinternet = false
    var simpleMode = false
    var checkspc = false
    var checkwpc = false
    var checktor = false
    var vrButton = false
    var homescreenFav = ""
    var isNexradOnMainScreen = false
    var favorites = mutableMapOf<FavoriteType, String>()
    var wfoTextFav = ""
    var wpcTextFav = ""
    var spotterFav = ""
    var playlistStr = ""
    var widgetCCShow7Day = false
    var primaryColor = 0
    var spinnerLayout = R.layout.spinner_row_blue
    var textSizeSmall = 0.0f
    var textSizeNormal = 0.0f
    var textSizeLarge = 0.0f
    var hourlyShowGraph = true
    var mainScreenRefreshToTop = false
    const val ANIMATION_INTERVAL_DEFAULT = 8
    const val HOMESCREEN_FAVORITE_DEFAULT = "TXT-CC2:TXT-HAZ:OGL-RADAR:TXT-7DAY2"
    var settingsUIVisitedNeedRefresh = false

    fun initialize(context: Context, dm: DisplayMetrics) {
        useNwsApi = Utility.readPref(context, "USE_NWS_API_SEVEN_DAY", "true").startsWith("t")
        useNwsApiForHourly = Utility.readPref(context, "USE_NWS_API_HOURLY", "true").startsWith("t")
        navDrawerMainScreen =
            Utility.readPref(context, "NAV_DRAWER_MAIN_SCREEN", "false").startsWith("t")
        navDrawerMainScreenOnRight =
            Utility.readPref(context, "NAV_DRAWER_MAIN_SCREEN_ON_RIGHT", "true").startsWith("t")
        if (UtilityUI.isTablet()) {
            normalTextSizeDefault = 18
        }
        normalTextSize =
            Utility.readPrefInt(context, "TEXTVIEW_FONT_SIZE", normalTextSizeDefault) // 14 16 21
        textSizeSmall = UtilityUI.spToPx(normalTextSize - 2, context)
        textSizeNormal = UtilityUI.spToPx(normalTextSize, context)
        textSizeLarge = UtilityUI.spToPx(normalTextSize + 5, context)
        locfragDontShowIcons = Utility.readPref(context, "UI_LOCFRAG_NO_ICONS", "false").startsWith("t")
        //elys mod - I fucking hate fabs!!!!   
	mainScreenRadarFab = Utility.readPref(context, "UI_MAIN_SCREEN_RADAR_FAB", "false").startsWith("t") //elys mods - I hate fab buttons
        homescreenTextLength = Utility.readPrefInt(context, "HOMESCREEN_TEXT_LENGTH_PREF", 500)
        refreshLocMin = Utility.readPrefInt(context, "REFRESH_LOC_MIN", 10)
        translateText = Utility.readPref(context, "TRANSLATE_TEXT", "false").startsWith("t")
        recordScreenShare = Utility.readPref(context, "RECORD_SCREEN_SHARE", "true").startsWith("t")
        dualpaneRadarIcon =
            Utility.readPref(context, "DUALPANE_RADAR_ICON", "false").startsWith("t")
        hideTopToolbar = Utility.readPref(context, "HIDE_TOP_TOOLBAR", "false").startsWith("t")
        colorNotif = ContextCompat.getColor(context, R.color.primary_dark_blue)
        colorBlack = ContextCompat.getColor(context, R.color.black)
        colorOffwhiteToolbar = ContextCompat.getColor(context, R.color.offwhite_toolbar)
        if (UtilityUI.isTablet()) {
            tilesPerRowDefault = 5
        }
        tilesPerRow = Utility.readPrefInt(context, "UI_TILES_PER_ROW", tilesPerRowDefault)
        themeStr = Utility.readPref(context, "THEME_BLUE", "BlackAqua")
        themeInt = UtilityUI.theme(themeStr)
        if (themeInt == R.style.MyCustomTheme_white_NOAB || themeInt == R.style.MyCustomTheme_whiter_NOAB || themeInt == R.style.MyCustomTheme_whitest_NOAB) {
            textSmallThemeColor = Color.GRAY
            textHighlightColor = Color.rgb(14, 71, 161)
            backgroundColor = Color.BLACK
            themeIsWhite = true
        } else {
            textSmallThemeColor = Color.LTGRAY
            textHighlightColor = Color.YELLOW
            backgroundColor = Color.WHITE
            themeIsWhite = false
        }
        tabHeaders[0] = Utility.readPref(context, "TAB1_HEADER", "LOCAL")
        tabHeaders[1] = Utility.readPref(context, "TAB2_HEADER", "SPC")
        tabHeaders[2] = Utility.readPref(context, "TAB3_HEADER", "MISC")
        widgetPreventTap = Utility.readPref(context, "UI_WIDGET_PREVENT_TAP", "").startsWith("t")
        fullscreenMode = Utility.readPref(context, "FULLSCREEN_MODE", "false").startsWith("t")
        lockToolbars = Utility.readPref(context, "LOCK_TOOLBARS", "true").startsWith("t")
        unitsM = Utility.readPref(context, "UNITS_M", "true").startsWith("t")
        unitsF = Utility.readPref(context, "UNITS_F", "true").startsWith("t")
        hourlyShowAMPM = Utility.readPref(context, "HOURLY_SHOW_AM_PM", "false").startsWith("t")
        widgetTextColor = Utility.readPrefInt(context, "WIDGET_TEXT_COLOR", Color.WHITE)
        widgetHighlightTextColor =
            Utility.readPrefInt(context, "WIDGET_HIGHLIGHT_TEXT_COLOR", Color.YELLOW)
        widgetNexradSize = Utility.readPrefInt(context, "WIDGET_NEXRAD_SIZE", 10)
        nwsIconTextColor =
            Utility.readPrefInt(context, "NWS_ICON_TEXT_COLOR", Color.rgb(38, 97, 139))
        nwsIconBottomColor =
            Utility.readPrefInt(context, "NWS_ICON_BOTTOM_COLOR", Color.rgb(255, 255, 255))
        elevationPref =
            Utility.readPrefInt(context, "ELEVATION_PREF", elevationPrefDefault).toFloat()
        elevationPref =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, elevationPref, MyApplication.dm)
        cardElevation = elevationPref
        fabElevation = elevationPref
        fabElevationDepressed = elevationPref * 2
		//elys mod 
		checkinternet = Utility.readPref(context, "CHECKINTERNET", "false").startsWith("t")
        simpleMode = Utility.readPref(context, "SIMPLE_MODE", "false").startsWith("t")
        checkspc = Utility.readPref(context, "CHECKSPC", "false").startsWith("t")
        checkwpc = Utility.readPref(context, "CHECKWPC", "false").startsWith("t")
        checktor = Utility.readPref(context, "CHECKTOR", "false").startsWith("t")
        vrButton = Utility.readPref(context, "VR_BUTTON", "false").startsWith("t")
        if (UtilityUI.isTablet()) {
            nwsIconSizeDefault = 6
        }
        nwsIconSize = Utility.readPrefInt(context, "NWS_ICON_SIZE_PREF", nwsIconSizeDefault)
        homescreenFav = Utility.readPref(context, "HOMESCREEN_FAV", HOMESCREEN_FAVORITE_DEFAULT)
        isNexradOnMainScreen = homescreenFav.contains("OGL-RADAR") || homescreenFav.contains("NXRD")
        FavoriteType.entries.forEach {
            favorites[it] = Utility.readPref(
                context,
                UtilityFavorites.getPrefToken(it),
                GlobalVariables.PREFERENCE_SEPARATOR
            )
        }
        spotterFav = Utility.readPref(context, "SPOTTER_FAV", "")
        wfoTextFav = Utility.readPref(context, "WFO_TEXT_FAV", "AFD")
        wpcTextFav = Utility.readPref(context, "WPC_TEXT_FAV", "pmdspd")
        playlistStr = Utility.readPref(context, "PLAYLIST", "")
        widgetCCShow7Day =
            Utility.readPref(context, "WIDGET_CC_DONOTSHOW_7_DAY", "true").startsWith("t")
        hourlyShowGraph = Utility.readPref(context, "HOURLY_SHOW_GRAPH", "true").startsWith("t")
        mainScreenRefreshToTop =
            Utility.readPref(context, "MAIN_SCREEN_SCROLL_TOP_RESTART", "false").startsWith("t")
        primaryColor = Utility.readPrefInt(context, "MYAPP_PRIMARY_COLOR", 0)
        spinnerLayout = if (themeIsWhite) {
            R.layout.spinner_row_white
        } else {
            R.layout.spinner_row_blue
        }

        val res = context.resources
        deviceScale = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 1f,
            MyApplication.dm
        )
        padding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            res.getDimension(R.dimen.padding_dynamic_tv),
            MyApplication.dm
        ).toInt()
        paddingSettings = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            res.getDimension(R.dimen.padding_dynamic_tv_settings),
            MyApplication.dm
        ).toInt()
        paddingSmall = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            res.getDimension(R.dimen.padding_dynamic_tv_small),
            MyApplication.dm
        ).toInt()
        lLpadding = res.getDimension(R.dimen.padding_ll)

        cardCorners = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            Utility.readPrefInt(context, "CARD_CORNER_RADIUS", 0).toFloat(),
            dm
        )

        val tv = TypedValue()
        if (context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight =
                TypedValue.complexToDimensionPixelSize(tv.data, res.displayMetrics)
        }
    }
}

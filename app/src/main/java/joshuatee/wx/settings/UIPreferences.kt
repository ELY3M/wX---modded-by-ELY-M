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

    // These are set in myapp still
//    const val TELECINE_SWITCH_SHOW_TOUCHES = false
    var telecineVideoSizePercentage = 0
    var telecineSwitchShowCountdown = false
    var telecineSwitchRecordingNotification = false
    var deviceScale = 0.0f
    var cardCorners = 0.0f
    var padding = 0
    var paddingSettings = 0
    var paddingSmall = 0
    var lLpadding = 0.0f
    var actionBarHeight = 0
    var refreshLocMin = 0
    var translateText = false
    private var nwsTextRemovelinebreaks = false
    var recordScreenShare = false
    var backgroundColor = Color.BLACK //elys mod
    var colorNotif = 0
    var colorBlack = 0
    var colorOffwhiteToolbar = 0
    var dualpaneRadarIcon = false
    var homescreenTextLength = 0
    var radarToolbarTransparent = true
    var radarStatusBarTransparent = false
    var radarImmersiveMode = false
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

    fun initPreferences(context: Context) {
        useNwsApi = Utility.readPref(context, "USE_NWS_API_SEVEN_DAY", "true").startsWith("t")
        useNwsApiForHourly = Utility.readPref(context, "USE_NWS_API_HOURLY", "true").startsWith("t")
        navDrawerMainScreen = Utility.readPref(context, "NAV_DRAWER_MAIN_SCREEN", "false").startsWith("t")
        navDrawerMainScreenOnRight = Utility.readPref(context, "NAV_DRAWER_MAIN_SCREEN_ON_RIGHT", "true").startsWith("t")
        if (UtilityUI.isTablet()) {
            normalTextSizeDefault = 18
        }
        normalTextSize = Utility.readPrefInt(context, "TEXTVIEW_FONT_SIZE", normalTextSizeDefault) // 14 16 21
        textSizeSmall = UtilityUI.spToPx(normalTextSize - 2, context)
        textSizeNormal = UtilityUI.spToPx(normalTextSize, context)
        textSizeLarge = UtilityUI.spToPx(normalTextSize + 5, context)
        locfragDontShowIcons = Utility.readPref(context, "UI_LOCFRAG_NO_ICONS", "false").startsWith("t")
        mainScreenRadarFab = Utility.readPref(context, "UI_MAIN_SCREEN_RADAR_FAB", "false").startsWith("t") //elys mods - I hate fab buttons
        homescreenTextLength = Utility.readPrefInt(context, "HOMESCREEN_TEXT_LENGTH_PREF", 500)
        refreshLocMin = Utility.readPrefInt(context, "REFRESH_LOC_MIN", 10)
        translateText = Utility.readPref(context, "TRANSLATE_TEXT", "false").startsWith("t")
        nwsTextRemovelinebreaks = Utility.readPref(context, "NWS_TEXT_REMOVELINEBREAKS", "true").startsWith("t")
        recordScreenShare = Utility.readPref(context, "RECORD_SCREEN_SHARE", "true").startsWith("t")
        dualpaneRadarIcon = Utility.readPref(context, "DUALPANE_RADAR_ICON", "false").startsWith("t")
        hideTopToolbar = Utility.readPref(context, "HIDE_TOP_TOOLBAR", "false").startsWith("t")
        colorNotif = ContextCompat.getColor(context, R.color.primary_dark_blue)
        colorBlack = ContextCompat.getColor(context, R.color.black)
        colorOffwhiteToolbar = ContextCompat.getColor(context, R.color.offwhite_toolbar)
        radarToolbarTransparent = Utility.readPref(context, "RADAR_TOOLBAR_TRANSPARENT", "true").startsWith("t")
        radarStatusBarTransparent = Utility.readPref(context, "RADAR_STATUSBAR_TRANSPARENT", "false").startsWith("t")
        radarImmersiveMode = Utility.readPref(context, "RADAR_IMMERSIVE_MODE", "false").startsWith("t")
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
        widgetPreventTap = getInitialPreference("UI_WIDGET_PREVENT_TAP", "")
        fullscreenMode = getInitialPreference("FULLSCREEN_MODE", "false")
        lockToolbars = getInitialPreference("LOCK_TOOLBARS", "true")
        unitsM = getInitialPreference("UNITS_M", "true")
        unitsF = getInitialPreference("UNITS_F", "true")
        widgetTextColor = getInitialPreference("WIDGET_TEXT_COLOR", Color.WHITE)
        widgetHighlightTextColor = getInitialPreference("WIDGET_HIGHLIGHT_TEXT_COLOR", Color.YELLOW)
        widgetNexradSize = getInitialPreference("WIDGET_NEXRAD_SIZE", 10)
        nwsIconTextColor = getInitialPreference("NWS_ICON_TEXT_COLOR", Color.rgb(38, 97, 139))
        nwsIconBottomColor = getInitialPreference("NWS_ICON_BOTTOM_COLOR", Color.rgb(255, 255, 255))
        elevationPref = getInitialPreference("ELEVATION_PREF", elevationPrefDefault).toFloat()
        elevationPref = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, elevationPref, MyApplication.dm)
        cardElevation = elevationPref
        fabElevation = elevationPref
        fabElevationDepressed = elevationPref * 2
		//elys mod 
		checkinternet = getInitialPreference("CHECKINTERNET", "false")
        simpleMode = getInitialPreference("SIMPLE_MODE", "false")
        checkspc = getInitialPreference("CHECKSPC", "false")
        checkwpc = getInitialPreference("CHECKWPC", "false")
        checktor = getInitialPreference("CHECKTOR", "false")
        vrButton = getInitialPreference("VR_BUTTON", "false")
        if (UtilityUI.isTablet()) {
            nwsIconSizeDefault = 6
        }
        nwsIconSize = MyApplication.preferences.getInt("NWS_ICON_SIZE_PREF", nwsIconSizeDefault)
        homescreenFav = getInitialPreferenceString("HOMESCREEN_FAV", HOMESCREEN_FAVORITE_DEFAULT)
        isNexradOnMainScreen = homescreenFav.contains("OGL-RADAR") || homescreenFav.contains("NXRD")
        FavoriteType.entries.forEach {
            favorites[it] = getInitialPreferenceString(UtilityFavorites.getPrefToken(it), GlobalVariables.PREFERENCE_SEPARATOR)
        }
        spotterFav = getInitialPreferenceString("SPOTTER_FAV", "")
        wfoTextFav = getInitialPreferenceString("WFO_TEXT_FAV", "AFD")
        wpcTextFav = getInitialPreferenceString("WPC_TEXT_FAV", "pmdspd")
        playlistStr = getInitialPreferenceString("PLAYLIST", "")
        widgetCCShow7Day = getInitialPreference("WIDGET_CC_DONOTSHOW_7_DAY", "true")
        hourlyShowGraph = getInitialPreference("HOURLY_SHOW_GRAPH", "true")
        mainScreenRefreshToTop = getInitialPreference("MAIN_SCREEN_SCROLL_TOP_RESTART", "false")
        primaryColor = MyApplication.preferences.getInt("MYAPP_PRIMARY_COLOR", 0)
        spinnerLayout = if (themeIsWhite) {
            R.layout.spinner_row_white
        } else {
            R.layout.spinner_row_blue
        }
    }

    private fun getInitialPreference(pref: String, initValue: Int): Int =
            MyApplication.preferences.getInt(pref, initValue)

    private fun getInitialPreference(pref: String, initValue: String): Boolean =
            (MyApplication.preferences.getString(pref, initValue) ?: initValue).startsWith("t")

    private fun getInitialPreferenceString(pref: String, initValue: String): String =
            MyApplication.preferences.getString(pref, initValue) ?: initValue
}

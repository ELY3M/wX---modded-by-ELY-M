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
import android.util.TypedValue
import androidx.core.content.ContextCompat
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility

object UIPreferences {

    // These are set in myapp still
    const val telecineSwitchShowTouches = false
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

    //
    var refreshLocMin = 0
    var translateText = false
    var nwsTextRemovelinebreaks = false
    var recordScreenShare = false
    var prefPreventAccidentalExit = false
    var fabInModels = false
    var backgroundColor = Color.WHITE
    var colorNotif = 0
    var colorBlack = 0
    var colorOffwhiteToolbar = 0
    var dualpaneRadarIcon = false
    var homescreenTextLength = 0
    var mediaControlNotif = false
    var radarToolbarTransparent = true
    var radarStatusBarTransparent = false
    var radarImmersiveMode = false
    var tilesPerRow = 3
    var tilesPerRowDefault = 3
    var themeStr = ""
    var themeInt = 0
    var textHighlightColor = 0
    var textSmallThemeColor = 0
    var themeIsWhite = false //elys mod
    var hideTopToolbar = false
    var mainScreenRadarFab = true
    var locfragDontShowIcons = false
    var nwsIconSizeDefault = 20
    var nwsIconSize = 0

    var normalTextSizeDefault = 16
    var normalTextSize = 16
    var navDrawerMainScreen = false
    var navDrawerMainScreenOnRight = true
    var useNwsApi = false
    var useNwsApiForHourly = true
    var lightningUseGoes = true
    var useAwcMosaic = true
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

    var iconsEvenSpaced = false
    var simpleMode = false
    var checkspc = false
    var checkwpc = false
    var checktor = false
    var vrButton = false

    var homescreenFav = ""
    var locDisplayImg = false

    var wfoFav = ""
    var ridFav = ""
    var sndFav = ""
    var srefFav = ""
    var spcMesoFav = ""
    var caRidFav = ""
    var nwsTextFav = ""
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

    const val animationIntervalDefault = 8
    const val HOMESCREEN_FAV_DEFAULT = "TXT-CC2:TXT-HAZ:OGL-RADAR:TXT-7DAY2"

    fun initPreferences(context: Context) {



        useNwsApi = Utility.readPref(context, "USE_NWS_API_SEVEN_DAY", "false").startsWith("t")
        useNwsApiForHourly = Utility.readPref("USE_NWS_API_HOURLY", "true").startsWith("t")
        lightningUseGoes = Utility.readPref("LIGHTNING_USE_GOES", "true").startsWith("t")
        useAwcMosaic = Utility.readPref("USE_AWC_MOSAIC", "true").startsWith("t")
        navDrawerMainScreen = Utility.readPref(context, "NAV_DRAWER_MAIN_SCREEN", "false").startsWith("t")
        navDrawerMainScreenOnRight = Utility.readPref(context, "NAV_DRAWER_MAIN_SCREEN_ON_RIGHT", "true").startsWith("t")
        if (UtilityUI.isTablet()) {
            normalTextSizeDefault = 18
        }
        normalTextSize = Utility.readPref(context, "TEXTVIEW_FONT_SIZE", normalTextSizeDefault) // 14 16 21
        textSizeSmall = UtilityUI.spToPx(normalTextSize - 2, context)
        textSizeNormal = UtilityUI.spToPx(normalTextSize, context)
        textSizeLarge = UtilityUI.spToPx(normalTextSize + 5, context)
        locfragDontShowIcons = Utility.readPref(context, "UI_LOCFRAG_NO_ICONS", "false").startsWith("t")
        mainScreenRadarFab = Utility.readPref(context, "UI_MAIN_SCREEN_RADAR_FAB", "true").startsWith("t")
        homescreenTextLength = Utility.readPref(context, "HOMESCREEN_TEXT_LENGTH_PREF", 500)
        refreshLocMin = Utility.readPref(context, "REFRESH_LOC_MIN", 10)
        translateText = Utility.readPref(context, "TRANSLATE_TEXT", "false").startsWith("t")
        nwsTextRemovelinebreaks = Utility.readPref(context, "NWS_TEXT_REMOVELINEBREAKS", "true").startsWith("t")
        recordScreenShare = Utility.readPref(context, "RECORD_SCREEN_SHARE", "true").startsWith("t")
        prefPreventAccidentalExit = Utility.readPref(context, "PREF_PREVENT_ACCIDENTAL_EXIT", "true").startsWith("t")
        dualpaneRadarIcon = Utility.readPref(context, "DUALPANE_RADAR_ICON", "false").startsWith("t")
        fabInModels = Utility.readPref(context, "FAB_IN_MODELS", "true").startsWith("t")
        hideTopToolbar = Utility.readPref(context, "HIDE_TOP_TOOLBAR", "false").startsWith("t")
        colorNotif = ContextCompat.getColor(context, R.color.primary_dark_blue)
        colorBlack = ContextCompat.getColor(context, R.color.black)
        colorOffwhiteToolbar = ContextCompat.getColor(context, R.color.offwhite_toolbar)
        mediaControlNotif = Utility.readPref(context, "MEDIA_CONTROL_NOTIF", "false").startsWith("t")
        radarToolbarTransparent = Utility.readPref(context, "RADAR_TOOLBAR_TRANSPARENT", "true").startsWith("t")
        radarStatusBarTransparent = Utility.readPref(context, "RADAR_STATUSBAR_TRANSPARENT", "false").startsWith("t")
        radarImmersiveMode = Utility.readPref(context, "RADAR_IMMERSIVE_MODE", "false").startsWith("t")
        if (UtilityUI.isTablet()) {
            tilesPerRowDefault = 5
        }
        tilesPerRow = Utility.readPref(context, "UI_TILES_PER_ROW", tilesPerRowDefault)
        themeStr = Utility.readPref(context, "THEME_BLUE", "whiteNew")
        themeInt = Utility.theme(themeStr)
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

        tabHeaders[0] = Utility.readPref("TAB1_HEADER", "LOCAL")
        tabHeaders[1] = Utility.readPref("TAB2_HEADER", "SPC")
        tabHeaders[2] = Utility.readPref("TAB3_HEADER", "MISC")

        widgetPreventTap = getInitialPreference("UI_WIDGET_PREVENT_TAP", "")
        fullscreenMode = getInitialPreference("FULLSCREEN_MODE", "false")
        lockToolbars = getInitialPreference("LOCK_TOOLBARS", "false")
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

        iconsEvenSpaced = getInitialPreference("UI_ICONS_EVENLY_SPACED", "false")
        simpleMode = getInitialPreference("SIMPLE_MODE", "false")
        checkspc = getInitialPreference("CHECKSPC", "false")
        checkwpc = getInitialPreference("CHECKWPC", "false")
        checktor = getInitialPreference("CHECKTOR", "false")
        vrButton = getInitialPreference("VR_BUTTON", "false")

        if (UtilityUI.isTablet()) {
            nwsIconSizeDefault = 6
        }
        nwsIconSize = MyApplication.preferences.getInt("NWS_ICON_SIZE_PREF", nwsIconSizeDefault)

        homescreenFav = getInitialPreferenceString("HOMESCREEN_FAV", HOMESCREEN_FAV_DEFAULT)
        locDisplayImg = homescreenFav.contains("OGL-RADAR") || homescreenFav.contains("NXRD")

        wfoFav = getInitialPreferenceString("WFO_FAV", GlobalVariables.prefSeparator)
        ridFav = getInitialPreferenceString("RID_FAV", GlobalVariables.prefSeparator)
        sndFav = getInitialPreferenceString("SND_FAV", GlobalVariables.prefSeparator)
        srefFav = getInitialPreferenceString("SREF_FAV", GlobalVariables.prefSeparator)
        spcMesoFav = getInitialPreferenceString("SPCMESO_FAV", GlobalVariables.prefSeparator)
        caRidFav = getInitialPreferenceString("RID_CA_FAV", GlobalVariables.prefSeparator)
        nwsTextFav = getInitialPreferenceString("NWS_TEXT_FAV", GlobalVariables.prefSeparator)
        spotterFav = getInitialPreferenceString("SPOTTER_FAV", "")
        wfoTextFav = getInitialPreferenceString("WFO_TEXT_FAV", "AFD")
        wpcTextFav = getInitialPreferenceString("WPC_TEXT_FAV", "pmdspd")
        playlistStr = getInitialPreferenceString("PLAYLIST", "")

        widgetCCShow7Day = getInitialPreference("WIDGET_CC_DONOTSHOW_7_DAY", "true")

        primaryColor = MyApplication.preferences.getInt("MYAPP_PRIMARY_COLOR", 0)
        spinnerLayout = if (themeIsWhite) {
            R.layout.spinner_row_white
        } else {
            R.layout.spinner_row_blue
        }

    }

    private fun getInitialPreference(pref: String, initValue: Int) = MyApplication.preferences.getInt(pref, initValue)

    private fun getInitialPreference(pref: String, initValue: String) = (MyApplication.preferences.getString(pref, initValue) ?: initValue).startsWith("t")

    private fun getInitialPreferenceString(pref: String, initValue: String) = MyApplication.preferences.getString(pref, initValue) ?: initValue

}

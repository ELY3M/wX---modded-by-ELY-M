/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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
//radar fab to false

package joshuatee.wx

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility

object UIPreferences {

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
    //private var smallTextTheme = 0
    var textHighlightColor = 0
    var textSmallThemeColor = 0
    var themeIsWhite = true
    var hideTopToolbar = false
    var mainScreenRadarFab = false
    var locfragDontShowIcons = false
    var useAwcRadarMosaic = true
    var nwsIconSizeDefault = 20
    var normalTextSizeDefault = 16
    var normalTextSize = 16
    var navDrawerMainScreen = false
    var navDrawerMainScreenOnRight = true
    var useNwsApi = false
    var useNwsApiForHourly = true

    fun initPreferences(context: Context) {
        useNwsApi = Utility.readPref(context, "USE_NWS_API_SEVEN_DAY", "false").startsWith("t")
        useNwsApiForHourly = Utility.readPref("USE_NWS_API_HOURLY", "true").startsWith("t")
        navDrawerMainScreen = Utility.readPref(context, "NAV_DRAWER_MAIN_SCREEN", "false").startsWith("t")
        navDrawerMainScreenOnRight = Utility.readPref(context, "NAV_DRAWER_MAIN_SCREEN_ON_RIGHT", "true").startsWith("t")
        if (UtilityUI.isTablet()) {
            normalTextSizeDefault = 18
        }
        normalTextSize = Utility.readPref(context, "TEXTVIEW_FONT_SIZE", normalTextSizeDefault) // 14 16 21
        MyApplication.textSizeSmall = UtilityUI.spToPx(normalTextSize - 2, context)
        MyApplication.textSizeNormal = UtilityUI.spToPx(normalTextSize, context)
        MyApplication.textSizeLarge = UtilityUI.spToPx(normalTextSize + 5, context)
        // useAwcRadarMosaic = Utility.readPref(context, "USE_AWC_RADAR_MOSAIC", "false").startsWith("t")
        locfragDontShowIcons = Utility.readPref(context, "UI_LOCFRAG_NO_ICONS", "false").startsWith("t")
        mainScreenRadarFab = Utility.readPref(context, "UI_MAIN_SCREEN_RADAR_FAB", "false").startsWith("t")
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
            //smallTextTheme = android.R.style.TextAppearance_Small_Inverse
            textSmallThemeColor = Color.GRAY
            textHighlightColor = Color.rgb(14, 71, 161)
            backgroundColor = Color.BLACK
            themeIsWhite = true
        } else {
            //smallTextTheme = android.R.style.TextAppearance_Small
            textSmallThemeColor = Color.LTGRAY
            textHighlightColor = Color.YELLOW
            backgroundColor = Color.WHITE
            themeIsWhite = false
        }
    }
}


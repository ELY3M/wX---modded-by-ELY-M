/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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

package joshuatee.wx

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat

import joshuatee.wx.util.Utility

object UIPreferences {

    var refreshLocMin: Int = 0
    var translateText: Boolean = false
    var nwsTextRemovelinebreaks: Boolean = false
    var recordScreenShare: Boolean = false
    var prefPreventAccidentalExit: Boolean = false
    var fabInModels: Boolean = false
    var backgroundColor: Int = Color.WHITE
    var colorNotif: Int = 0
    var colorBlack: Int = 0
    var colorOffwhiteToolbar: Int = 0
    var dualpaneRadarIcon: Boolean = false
    var homescreenTextLength: Int = 0
    var mediaControlNotif: Boolean = false
    var radarToolbarTransparent: Boolean = true
    var radarImmersiveMode: Boolean = false
    var tilesPerRow: Int = 3
    var themeStr: String = ""
    var themeInt: Int = 0
    var smallTextTheme: Int = 0
    var textHighlightColor: Int = 0
    var highlightColorStr: String = ""
    var themeIsWhite: Boolean = true
    var hideTopToolbar: Boolean = false
    var mainScreenRadarFab: Boolean = false

    fun initPreferences(context: Context) {
        mainScreenRadarFab = Utility.readPref(context, "UI_MAIN_SCREEN_RADAR_FAB", "false").startsWith("t")
        homescreenTextLength = Utility.readPref(context, "HOMESCREEN_TEXT_LENGTH_PREF", 500)
        refreshLocMin = Utility.readPref(context, "REFRESH_LOC_MIN", 10)
        translateText = Utility.readPref(context, "TRANSLATE_TEXT", "").startsWith("t")
        nwsTextRemovelinebreaks = Utility.readPref(context, "NWS_TEXT_REMOVELINEBREAKS", "true").startsWith("t")
        recordScreenShare = Utility.readPref(context, "RECORD_SCREEN_SHARE", "true").startsWith("t")
        prefPreventAccidentalExit = Utility.readPref(context, "PREF_PREVENT_ACCIDENTAL_EXIT", "true").startsWith("t")
        dualpaneRadarIcon = Utility.readPref(context, "DUALPANE_RADAR_ICON", "false").startsWith("t")
        fabInModels = Utility.readPref(context, "FAB_IN_MODELS", "true").startsWith("t")
        hideTopToolbar = Utility.readPref(context, "HIDE_TOP_TOOLBAR", "false").startsWith("t")
        colorNotif = ContextCompat.getColor(context, R.color.primary_dark_blue)
        colorBlack = ContextCompat.getColor(context, R.color.black)
        colorOffwhiteToolbar = ContextCompat.getColor(context, R.color.offwhite_toolbar)
        mediaControlNotif = Utility.readPref(context, "MEDIA_CONTROL_NOTIF", "").startsWith("t")
        radarToolbarTransparent = Utility.readPref(context, "RADAR_TOOLBAR_TRANSPARENT", "true").startsWith("t")
        radarImmersiveMode = Utility.readPref(context, "RADAR_IMMERSIVE_MODE", "false").startsWith("t")
        tilesPerRow = Utility.readPref(context, "UI_TILES_PER_ROW", tilesPerRow)
        //themeStr = Utility.readPref(context, "THEME_BLUE", "blue")
        themeStr = Utility.readPref(context, "THEME_BLUE", "whiteNew")
        themeInt = Utility.theme(themeStr)
        if (themeInt == R.style.MyCustomTheme_white_NOAB || themeInt == R.style.MyCustomTheme_whiter_NOAB) {
            smallTextTheme = android.R.style.TextAppearance_Small_Inverse
            textHighlightColor = Color.BLUE
            backgroundColor = Color.BLACK
            highlightColorStr = "blue"
            themeIsWhite = true
        } else {
            smallTextTheme = android.R.style.TextAppearance_Small
            textHighlightColor = Color.YELLOW
            backgroundColor = Color.WHITE
            highlightColorStr = "yellow"
            themeIsWhite = false
        }
    }
}

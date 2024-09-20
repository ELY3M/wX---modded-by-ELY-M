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

package joshuatee.wx.ui

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.TypedValue
import android.view.WindowInsets
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.swap
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import kotlin.math.pow
import kotlin.math.sqrt


object UtilityUI {

    fun moveUp(
        context: Context,
        prefToken: String,
        itemList: MutableList<String>,
        pos: Int
    ): String {
        if (pos != 0) {
            itemList.swap(pos, pos - 1)
        } else {
            itemList.swap(0, itemList.lastIndex)
        }
        var value = ""
        itemList.forEach { item ->
            value += ":" + item.split(";").dropLastWhile { it.isEmpty() }[0]
        }
        Utility.writePref(context, prefToken, value)
        return value
    }

    fun moveDown(
        context: Context,
        prefToken: String,
        itemList: MutableList<String>,
        pos: Int
    ): String {
        if (pos != itemList.lastIndex) {
            itemList.swap(pos, pos + 1)
        } else {
            itemList.swap(0, itemList.lastIndex)
        }
        var value = ""
        itemList.forEach { item ->
            value += ":" + item.split(";").dropLastWhile { it.isEmpty() }[0]
        }
        Utility.writePref(context, prefToken, value)
        return value
    }

    // distance tool, image map
    fun statusBarHeight(activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= 30 && activity.window.decorView.rootWindowInsets != null) {
            val windowInsets: WindowInsets = activity.window.decorView.rootWindowInsets
            val statusBarHeight = windowInsets.getInsets(WindowInsets.Type.statusBars()).top
            UtilityLog.d("wxsize2", statusBarHeight.toString())
            statusBarHeight
        } else {
            val resourceId =
                activity.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                UtilityLog.d(
                    "wxsize1",
                    activity.resources.getDimensionPixelSize(resourceId).toString()
                )
                activity.resources.getDimensionPixelSize(resourceId)
            } else {
                UtilityLog.d("wxsize0", "error in getting status bar height")
                0
            }
        }
    }

    fun spToPx(sp: Int, context: Context): Float =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp.toFloat(),
            context.resources.displayMetrics
        )

    fun isTablet(): Boolean {
        val displayMetrics = MyApplication.dm
        val width = displayMetrics.widthPixels / displayMetrics.densityDpi
        val height = displayMetrics.heightPixels / displayMetrics.densityDpi
        val screenDiagonal = sqrt(width.toDouble().pow(2.0) + height.toDouble().pow(2.0))
        return screenDiagonal >= 7.0
    }

    fun screenWidthAbs(): Int {
        val displayMetrics = MyApplication.dm
        return displayMetrics.widthPixels
    }

    fun isLandScape(context: Context): Boolean =
        context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    fun theme(themeStr: String): Int = when {
        themeStr.startsWith("blue") -> R.style.MyCustomTheme_NOAB
        themeStr == "black" -> R.style.MyCustomTheme_Holo_Dark_NOAB
        themeStr == "allBlack" -> R.style.MyCustomTheme_Holo_Darkest_NOAB
        themeStr.startsWith("green") -> R.style.MyCustomTheme_Green_NOAB
        themeStr.startsWith("gray") -> R.style.MyCustomTheme_Gray_NOAB
        themeStr.startsWith("darkBlue") -> R.style.MyCustomTheme_DarkBlue_NOAB
        themeStr.startsWith("mixedBlue") -> R.style.MyCustomTheme_mixedBlue_NOAB
        themeStr == "white" -> R.style.MyCustomTheme_white_NOAB
        themeStr.startsWith("whiteNew") -> R.style.MyCustomTheme_whiter_NOAB
        themeStr.startsWith("allWhite") -> R.style.MyCustomTheme_whitest_NOAB
        themeStr.startsWith("orange") -> R.style.MyCustomTheme_orange_NOAB
        themeStr.startsWith("BlackAqua") -> R.style.MyCustomTheme_BlackAqua
        themeStr.startsWith("BlackNeonGreen") -> R.style.MyCustomTheme_BlackNeonGreen
        themeStr.startsWith("WhiteToolbar") -> R.style.MyCustomTheme_white_NOAB
        else -> R.style.MyCustomTheme_NOAB
    }

    fun isThemeMaterial3(): Boolean = when (UIPreferences.themeInt) {
        R.style.MyCustomTheme_whiter_NOAB -> true
        R.style.MyCustomTheme_NOAB -> true
        R.style.MyCustomTheme_Green_NOAB -> true
        R.style.MyCustomTheme_Gray_NOAB -> true
        R.style.MyCustomTheme_orange_NOAB -> true
        R.style.MyCustomTheme_Holo_Dark_NOAB -> true
        R.style.MyCustomTheme_Holo_Darkest_NOAB -> true
        R.style.MyCustomTheme_mixedBlue_NOAB -> true
        R.style.MyCustomTheme_DarkBlue_NOAB -> true
        R.style.MyCustomTheme_whitest_NOAB -> true
        R.style.MyCustomTheme_BlackAqua -> true
        R.style.MyCustomTheme_BlackNeonGreen -> true
        else -> false
    }

    fun isThemeAllBlack(): Boolean =
        UIPreferences.themeInt == R.style.MyCustomTheme_Holo_Dark_NOAB || UIPreferences.themeInt == R.style.MyCustomTheme_Holo_Darkest_NOAB

    fun isThemeAllWhite(): Boolean =
        UIPreferences.themeInt == R.style.MyCustomTheme_whitest_NOAB

    fun isThemeLight(): Boolean =
        UIPreferences.themeInt == R.style.MyCustomTheme_whitest_NOAB || UIPreferences.themeInt == R.style.MyCustomTheme_whiter_NOAB
                || UIPreferences.themeInt == R.style.MyCustomTheme_white_NOAB
}

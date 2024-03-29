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

package joshuatee.wx.ui

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import joshuatee.wx.swap
import joshuatee.wx.MyApplication
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import kotlin.math.pow
import kotlin.math.sqrt


object UtilityUI {

    fun immersiveMode(activity: Activity) {
        if (UIPreferences.radarImmersiveMode) {
            WindowCompat.setDecorFitsSystemWindows(activity.window, false)
            WindowInsetsControllerCompat(activity.window, activity.window.decorView).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    fun moveUp(context: Context, prefToken: String, itemList: MutableList<String>, pos: Int): String {
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

    fun moveDown(context: Context, prefToken: String, itemList: MutableList<String>, pos: Int): String {
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

    fun statusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            UtilityLog.d("wxsize", context.resources.getDimensionPixelSize(resourceId).toString())
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            UtilityLog.d("wxsize", "error in getting status bar height")
            0
        }
    }

    // only used to size multipane radar
    fun navigationBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        // assume no nav bar in Android 13
//        if (Build.VERSION.SDK_INT > 32) {
//            return 0
//        }
        return if (resourceId > 0) {
            // context.resources.getDimensionPixelSize(resourceId)
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, context.resources.getDimensionPixelSize(resourceId).toFloat(), MyApplication.dm).toInt()
        } else {
            0
        }
    }

    fun spToPx(sp: Int, context: Context): Float =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), context.resources.displayMetrics)

    fun isTablet(): Boolean {
        val displayMetrics = MyApplication.dm
        val width = displayMetrics.widthPixels / displayMetrics.densityDpi
        val height = displayMetrics.heightPixels / displayMetrics.densityDpi
        val screenDiagonal = sqrt(width.toDouble().pow(2.0) + height.toDouble().pow(2.0))
        return screenDiagonal >= 7.0
    }

//    fun screenHeightAbs(): Int {
//        val displayMetrics = MyApplication.dm
//        return displayMetrics.heightPixels
//    }

    fun screenWidthAbs(): Int {
        val displayMetrics = MyApplication.dm
        return displayMetrics.widthPixels
    }

    fun isLandScape(context: Context): Boolean =
            context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

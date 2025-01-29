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

package joshuatee.wx.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.text.Html
import androidx.preference.PreferenceManager
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.radar.NexradRenderUI
import joshuatee.wx.settings.UtilityNavDrawer
import joshuatee.wx.ui.UtilityUI
import kotlin.system.exitProcess
//elys mod - for checking internet
import android.net.ConnectivityManager
import joshuatee.wx.ui.ObjectDialogue

object Utility {

    private fun showDiagnostics(activity: Activity): String {
        var diagnostics = ""
        diagnostics += MyApplication.dm.widthPixels.toString() + " Screen width" + GlobalVariables.newline
        diagnostics += MyApplication.dm.heightPixels.toString() + " Screen height" + GlobalVariables.newline
        var landScape = false
        if (activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            landScape = true
        }
        diagnostics += landScape.toString() + " Landscape" + GlobalVariables.newline
        diagnostics += "Homescreen navdrawer list: " + UtilityNavDrawer.getNavDrawerTokenList(
            activity
        ) + GlobalVariables.newline
        return diagnostics
    }

    fun getVersion(context: Context): String =
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""

    @SuppressLint("ApplySharedPref")
    fun commitPref(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.commit()
    }

    fun writePref(context: Context, key: String, value: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun writePrefWithNull(context: Context, key: String, value: String?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun writePrefInt(context: Context, key: String, value: Int) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun writePrefFloat(context: Context, key: String, value: Float) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    fun writePrefLong(context: Context, key: String, value: Long) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun writePref(key: String, value: String) {
        MyApplication.editor.putString(key, value)
        MyApplication.editor.apply()
    }

    fun readPref(context: Context, key: String, value: String): String =
        PreferenceManager.getDefaultSharedPreferences(context).getString(key, value)!!

    fun readPrefInt(context: Context, key: String, value: Int): Int =
        PreferenceManager.getDefaultSharedPreferences(context).getInt(key, value)

    fun readPrefFloat(context: Context, key: String, value: Float): Float =
        PreferenceManager.getDefaultSharedPreferences(context).getFloat(key, value)

    fun readPrefLong(context: Context, key: String, value: Long): Long =
        PreferenceManager.getDefaultSharedPreferences(context).getLong(key, value)

    fun readPrefWithNull(context: Context, key: String, value: String?): String? =
        PreferenceManager.getDefaultSharedPreferences(context).getString(key, value)

    @SuppressLint("ApplySharedPref")
    fun removePref(context: Context, key: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().remove(key).commit()
    }

    // FIXME deprecate these
    fun readPref(key: String, value: String): String =
        MyApplication.preferences.getString(key, value)!!

    fun fromHtml(source: String): String =
        Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY).toString()

    fun safeGet(list: List<String>, index: Int): String = if (list.size <= index || index < 0) {
        ""
    } else {
        list[index]
    }

    fun safeGet(list: Array<String>, index: Int): String = if (list.size <= index) {
        ""
    } else {
        list[index]
    }

    fun showVersion(activity: Activity): String {
        var s = activity.resources.getString(R.string.about_wx) +
                GlobalVariables.newline + "Version " + getVersion(activity) + GlobalVariables.newline + GlobalVariables.newline +
                "Use alt-? on the main screen and in nexrad radar to show keyboard shortcuts"
        s += GlobalVariables.newline + GlobalVariables.newline + "Diagnostics information:" + GlobalVariables.newline
        s += readPref(
            activity,
            "JOBSERVICE_TIME_LAST_RAN",
            ""
        ) + "  Last background update" + GlobalVariables.newline
        s += NexradRenderUI.getLastRadarTime(activity) + "  Last radar update" + GlobalVariables.newline
        s += showDiagnostics(activity)
        s += "Tablet: " + UtilityUI.isTablet().toString() + GlobalVariables.newline
        s += "Notification Cancel String: " + readPref(
            activity,
            "NOTIF_STR",
            ""
        ) + GlobalVariables.newline
        return s
    }

    fun showMainScreenShortCuts(): String =
        "Ctrl-r: Nexrad radar" + GlobalVariables.newline +
                "Ctrl-m: Show submenu" + GlobalVariables.newline +
                "Ctrl-d: Severe Dashboard" + GlobalVariables.newline +
                "Ctrl-c: Goes Viewer" + GlobalVariables.newline +
                "Ctrl-a: Local text product viewer" + GlobalVariables.newline +
                "Ctrl-p: Settings" + GlobalVariables.newline +
                "Ctrl-2: Dual Pane Radar" + GlobalVariables.newline +
                "Ctrl-4: Quad Pane Radar" + GlobalVariables.newline +
                "Ctrl-s: SPC Convective Outlook Summary" + GlobalVariables.newline +
                "Ctrl-z: SPC Mesoanalysis" + GlobalVariables.newline +
                "Ctrl-n: NCEP Models" + GlobalVariables.newline +
                "Ctrl-h: Hourly" + GlobalVariables.newline +
                "Ctrl-o: NHC" + GlobalVariables.newline +
                "Ctrl-l: Show locations" + GlobalVariables.newline +
                "Ctrl-i: National images" + GlobalVariables.newline +
                "Ctrl-t: National text discussions" + GlobalVariables.newline +
                "Ctrl-j: Previous tab" + GlobalVariables.newline +
                "Ctrl-k: Next tab" + GlobalVariables.newline

    fun showRadarShortCuts(): String =
        "Ctrl-l: Show map" + GlobalVariables.newline +
                "Ctrl-m: Show submenu" + GlobalVariables.newline +
                "Ctrl-a: Animate / stop animate" + GlobalVariables.newline +
                "Ctrl-r: Show reflectivity" + GlobalVariables.newline +
                "Ctrl-v: Show velocity" + GlobalVariables.newline +
                "Ctrl-f: Toggle favorite" + GlobalVariables.newline +
                "Ctrl-2: Show dual pane radar" + GlobalVariables.newline +
                "Ctrl-4: Show quad pane radar" + GlobalVariables.newline +
                "Ctrl-UpArrow: Zoom out" + GlobalVariables.newline +
                "Ctrl-DownArrow: Zoom in" + GlobalVariables.newline +
                "Arrow keys: pan radar" + GlobalVariables.newline +
                "Reload key: reload radar" + GlobalVariables.newline

    fun showWfoTextShortCuts(): String =
        "Ctrl-l: Show map" + GlobalVariables.newline +
                "Ctrl-m: Show submenu" + GlobalVariables.newline +
                "Ctrl-f: Toggle favorite" + GlobalVariables.newline +
                "Ctrl-p: Play audio - TTS" + GlobalVariables.newline +
                "Ctrl-s: Stop audio - TTS" + GlobalVariables.newline +
                "Ctrl-d: Show navigation drawer" + GlobalVariables.newline

    fun showLocationEditShortCuts(): String =
        "Ctrl-g: Use GPS to find location" + GlobalVariables.newline + "Ctrl-m: Show submenu" + GlobalVariables.newline

    fun restart() {
        exitProcess(0)
    }
    //elys mod    
    fun checkInternet(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        if (netInfo != null && netInfo.isConnected) {
            UtilityLog.d("wx-elys", "network state = true")
        } else {
            UtilityLog.d("wx-elys", "network state = false")
            ObjectDialogue(context, "No Network Connection.\nCheck your internet on your device!")
        }
    } 
}

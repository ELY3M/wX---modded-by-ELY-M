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
//modded by ELY M. 

package joshuatee.wx.util

import android.os.Build
import android.preference.PreferenceManager
import android.text.Html
import android.content.Context

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.settings.Location

import joshuatee.wx.Extensions.*
import joshuatee.wx.radar.LatLon

object Utility {

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

    fun writePref(context: Context, key: String, value: Int) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun writePref(context: Context, key: String, value: Float) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    fun writePref(context: Context, key: String, value: Long) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun writePref(key: String, value: String) {
        MyApplication.editor.putString(key, value)
        MyApplication.editor.apply()
    }

    //fun writePref(key: String, value: Int){
    //    MyApplication.editor.putInt(key, value)
    //    MyApplication.editor.apply()
    //}

    //fun writePref(key: String, value: Float){
    //    MyApplication.editor.putFloat(key, value)
    //    MyApplication.editor.apply()
    //}

    //fun writePref(key: String, value: Long){
    //    MyApplication.editor.putLong(key, value)
    //    MyApplication.editor.apply()
    //}

    fun readPref(context: Context, key: String, value: String): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(key, value)
    }

    fun readPref(context: Context, key: String, value: Int): Int {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getInt(key, value)
    }

    fun readPref(context: Context, key: String, value: Float): Float {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getFloat(key, value)
    }

    fun readPref(context: Context, key: String, value: Long): Long {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getLong(key, value)
    }

    fun readPrefWithNull(context: Context, key: String, value: String?): String? {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(key, value)
    }

    fun removePref(context: Context, key: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().remove(key).commit()
    }

    // FIXME deprecate these
    fun readPref(key: String, value: String): String =
        MyApplication.preferences.getString(key, value)

    fun theme(themeStr: String): Int = when {
        themeStr.startsWith("blue") -> R.style.MyCustomTheme_NOAB
        themeStr.startsWith("black") -> R.style.MyCustomTheme_Holo_Dark_NOAB
        themeStr.startsWith("green") -> R.style.MyCustomTheme_Green_NOAB
        themeStr.startsWith("gray") -> R.style.MyCustomTheme_Gray_NOAB
        themeStr.startsWith("darkBlue") -> R.style.MyCustomTheme_DarkBlue_NOAB
        themeStr.startsWith("mixedBlue") -> R.style.MyCustomTheme_mixedBlue_NOAB
        themeStr == "white" -> R.style.MyCustomTheme_white_NOAB
        themeStr.startsWith("whiteNew") -> R.style.MyCustomTheme_whiter_NOAB
        themeStr.startsWith("orange") -> R.style.MyCustomTheme_orange_NOAB
        themeStr.startsWith("BlackAqua") -> R.style.MyCustomTheme_BlackAqua_NOAB
        themeStr.startsWith("WhiteToolbar") -> R.style.MyCustomTheme_white_NOAB
        else -> R.style.MyCustomTheme_NOAB
    }

    private fun getCurrentConditionsCanada(locNum: Int): ObjectForecastPackage {
        val html = UtilityCanada.getLocationHtml(Location.getLatLon(locNum))
        val objCC = ObjectForecastPackageCurrentConditions.createForCanada(html)
        return ObjectForecastPackage(objCC)
    }

    private fun getCurrentConditionsUSV2(context: Context, locNum: Int): ObjectForecastPackage {
        val objCC = ObjectForecastPackageCurrentConditions(context, locNum)
        return ObjectForecastPackage(objCC)
    }

    fun getCurrentHazards(locNum: Int): ObjectForecastPackageHazards {
        return if (Location.isUS(locNum)) {
            ObjectForecastPackageHazards(locNum)
        } else {
            val html = UtilityCanada.getLocationHtml(Location.getLatLon(locNum))
            ObjectForecastPackageHazards.createForCanada(html)
        }
    }

    fun getCurrentSevenDay(locNum: Int): ObjectForecastPackage7Day {
        return if (Location.isUS(locNum)) {
            val sevenDayJson = UtilityDownloadNWS.get7DayJSON(Location.getLatLon(locNum))
            ObjectForecastPackage7Day(locNum, sevenDayJson)
        } else {
            val html = UtilityCanada.getLocationHtml(Location.getLatLon(locNum))
            ObjectForecastPackage7Day(locNum, html)
        }
    }

    fun getCurrentSevenDay(location: LatLon): ObjectForecastPackage7Day {
        val sevenDayJson = UtilityDownloadNWS.get7DayJSON(location)
        return ObjectForecastPackage7Day(-1, sevenDayJson)
    }

    fun getCurrentHazards(location: LatLon): ObjectForecastPackageHazards {
        return ObjectForecastPackageHazards(location)
    }

    fun getCurrentConditionsV2byLatLon(context: Context, location: LatLon): ObjectForecastPackage {
        val objCC = ObjectForecastPackageCurrentConditions(context, location)
        return ObjectForecastPackage(objCC)
    }

    fun getCurrentConditionsV2(context: Context, locNum: Int): ObjectForecastPackage =
        if (Location.isUS(locNum)) {
            getCurrentConditionsUSV2(context, locNum)
        } else {
            getCurrentConditionsCanada(locNum)
        }

    fun getHazards(url: String): String =
        url.parse("<!-- AddThis Button END --> {3}<hr /><br />(.*?)</div>")

    fun fromHtml(source: String): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY).toString()
    } else {
        Html.fromHtml(source).toString()
    }

    fun safeGet(list: List<String>, index: Int): String {
        return if (list.size <= index) {
            ""
        } else {
            list[index]
        }
    }
}



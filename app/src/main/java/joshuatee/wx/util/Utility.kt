/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import android.net.ConnectivityManager
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.settings.Location

import joshuatee.wx.Extensions.*
import joshuatee.wx.GlobalArrays
import joshuatee.wx.radar.LatLon
import joshuatee.wx.util.UtilityAlertDialog.showDialogueWithContext
import joshuatee.wx.radar.UtilityRadar
object Utility {

    fun checkInternet(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        if (netInfo != null && netInfo.isConnected) {
            UtilityLog.d("wx", "network state = true")

        } else {
            UtilityLog.d("wx", "network state = false")
            showDialogueWithContext("No Network Connection.\nCheck your internet on your device!", context)
            ///UtilityAlertDialog.showDialogBox("No Network Connection", R.drawable.wx, "Check your internet on your device!", context)
        }

    }
    
    fun getRadarSiteName(radarSite: String): String {
        return UtilityRadar.radarIdToName[radarSite] ?: ""
    }

   /* fun getRadarSiteLatLon(radarSite: String): LatLon {
        val lat = UtilityRadar.radarSiteToLat[radarSite] ?: ""
        val lon = UtilityRadar.radarSiteToLon[radarSite] ?: ""
        return LatLon(lat, lon)
    }*/

    fun getRadarSiteX(radarSite: String): String {
        return UtilityRadar.radarSiteToLat[radarSite] ?: ""
    }

    fun getRadarSiteY(radarSite: String): String {
        return UtilityRadar.radarSiteToLon[radarSite] ?: ""
    }

    /*fun getWfoSiteName(wfo: String): String {
        return UtilityRadar.wfoIdToName[wfo] ?: ""
    }

    fun getWfoSiteLatLon(wfo: String): LatLon {
        val lat = UtilityRadar.wfoSitetoLat[wfo] ?: ""
        val lon = UtilityRadar.wfoSitetoLon[wfo] ?: ""
        return LatLon(lat, lon)
    }*/

    /*fun getSoundingSiteLatLon(wfo: String): LatLon {
        val lat = UtilityRadar.soundingSiteToLat[wfo] ?: ""
        val lon = "-" + (UtilityRadar.soundingSiteToLon[wfo] ?: "")
        return LatLon(lat, lon)
    }*/

  /*  fun getSoundingSiteName(wfo: String): String {
        var site = UtilityRadar.wfoIdToName[wfo] ?: ""
        if (site == "") {
            site = UtilityRadar.soundingIdToName[wfo] ?: ""
        }
        return site
    }

    fun generateSoundingNameList(): List<String> {
        val list = mutableListOf<String>()
        GlobalArrays.soundingSites.sorted()
        GlobalArrays.soundingSites.forEach {
            list.add(it + ": " + getSoundingSiteName(it))
        }
        return list
    }*/

    fun getVersion(context: Context): String {
        var vers = ""
        try {
            vers = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return vers
    }

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
        return preferences.getString(key, value)!!
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
            MyApplication.preferences.getString(key, value)!!

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

    private fun getCurrentConditionsUS(context: Context, locNum: Int): ObjectForecastPackage {
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

    fun getCurrentConditionsByLatLon(context: Context, location: LatLon): ObjectForecastPackage {
        val objCC = ObjectForecastPackageCurrentConditions(context, location)
        return ObjectForecastPackage(objCC)
    }

    fun getCurrentConditions(context: Context, locNum: Int): ObjectForecastPackage =
            if (Location.isUS(locNum)) {
                getCurrentConditionsUS(context, locNum)
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



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

package joshuatee.wx

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import android.util.DisplayMetrics
import android.util.TypedValue
import java.util.concurrent.TimeUnit
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.ObjectPolygonWatch
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.radar.RadarGeometry
import joshuatee.wx.radar.WXGLNexrad
import joshuatee.wx.radarcolorpalettes.ColorPalettes
import joshuatee.wx.radarcolorpalettes.ObjectColorPalette
import joshuatee.wx.settings.*
import joshuatee.wx.settings.UtilityHomeScreen
import okhttp3.Interceptor
import okhttp3.OkHttpClient

//
// Class that has methods that run at app start and store critical preference data structures
//
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        editor = preferences.edit()
        preferencesTelecine = getSharedPreferences("telecine", Context.MODE_PRIVATE)
        contentResolverLocal = contentResolver
        val res = resources
        dm = res.displayMetrics

        UIPreferences.deviceScale = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, dm)
        UIPreferences.padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, res.getDimension(R.dimen.padding_dynamic_tv), dm).toInt()
        UIPreferences.paddingSettings = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, res.getDimension(R.dimen.padding_dynamic_tv_settings), dm).toInt()
        UIPreferences.paddingSmall = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, res.getDimension(R.dimen.padding_dynamic_tv_small), dm).toInt()
        UIPreferences.lLpadding = res.getDimension(R.dimen.padding_ll)
        val tv = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            UIPreferences.actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, res.displayMetrics)
        }

        initPreferences(this)
        Location.refreshLocationData(this)
        UtilityTts.loadTts(applicationContext)
        loadGeomAndColorBuffers(this)
    }

    companion object {

        private val okHttp3Interceptor = Interceptor { chain ->
            val request = chain.request()
            var response = chain.proceed(request)
            var tryCount = 0
            while (!response.isSuccessful && tryCount < 3) {
                tryCount += 1
                response.close()
                response = chain.proceed(request)
            }
            response
        }
        val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(okHttp3Interceptor)
        .build()

        lateinit var preferences: SharedPreferences
        private lateinit var preferencesTelecine: SharedPreferences
        lateinit var editor: SharedPreferences.Editor
        lateinit var dm: DisplayMetrics
        lateinit var appContext: Context
        var contentResolverLocal: ContentResolver? = null

        fun initPreferences(context: Context) {
            RadarPreferences.initRadarPreferences()
            UIPreferences.initPreferences(context)
            UtilityHomeScreen.setupMap()
            RadarPreferences.radarGeometrySetColors()
            NotificationPreferences.initPreferences()
            WXGLNexrad.colorPaletteProducts.forEach {
                ObjectColorPalette.radarColorPalette[it] = getInitialPreferenceString("RADAR_COLOR_PALETTE_$it", "CODENH")
                ObjectColorPalette.radarColorPaletteList[it] = getInitialPreferenceString("RADAR_COLOR_PALETTE_" + it + "_LIST", "")
            }
            UIPreferences.cardCorners = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, preferences.getInt("CARD_CORNER_RADIUS", 0).toFloat(), dm)
            UIPreferences.telecineVideoSizePercentage = preferencesTelecine.getInt("video-size", 100)
            UIPreferences.telecineSwitchShowCountdown = preferencesTelecine.getBoolean("show-countdown", false)
            UIPreferences.telecineSwitchRecordingNotification = preferencesTelecine.getBoolean("recording-notification", false)
            Location.currentLocationStr = getInitialPreferenceString("CURRENT_LOC_FRAGMENT", "1")

            ObjectPolygonWatch.load(context)
        }

        fun loadGeomAndColorBuffers(context: Context) {
            if (!loadedBuffers) {
                initBuffers(context)
            }
            PolygonType.refresh()
        }

        private var loadedBuffers = false
        private fun initBuffers(context: Context) {
            loadedBuffers = true
            ColorPalettes.initialize(context)
            RadarPreferences.initGenericRadarWarnings(context)
            GeographyType.values().forEach {
                RadarGeometry.initRadarGeometryByType(context, it)
            }
            GeographyType.refresh()
        }

        private fun getInitialPreferenceString(pref: String, initValue: String) = preferences.getString(pref, initValue) ?: initValue
    }
}

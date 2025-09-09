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
import joshuatee.wx.objects.PolygonWatch
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.PolygonWarning
import joshuatee.wx.radar.Metar
import joshuatee.wx.radar.RadarGeometry
import joshuatee.wx.radar.NexradUtil
import joshuatee.wx.radarcolorpalettes.ColorPalettes
import joshuatee.wx.radarcolorpalettes.ColorPalette
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.NotificationPreferences
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.settings.UtilityHomeScreen
import joshuatee.wx.settings.UtilityStorePreferences
import joshuatee.wx.util.Utility
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.acra.BuildConfig
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

//
// Class that has methods that run at app start and store critical preference data structures
//
class MyApplication : Application() {

/*
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.KEY_VALUE_LIST

            val cal: Calendar = Calendar.getInstance()
            val format = SimpleDateFormat("MM-dd-yy---h.mm.ss.a")
            format.setTimeZone(TimeZone.getTimeZone("America/Chicago"))
            val nowDateTime = format.format(cal.getTime())


            mailSender {
                //required
                mailTo = "money@mboca.com"
                //defaults to true
                reportAsFile = true
                //defaults to ACRA-report.stacktrace
                reportFileName = "Crash_$nowDateTime.txt"
                //defaults to "<applicationId> Crash Report"
                subject = "wX App Crashed!"
                //defaults to empty
                body = ""
            }
        }
    }
*/


    
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        editor = preferences.edit()
        val res = resources
        dm = res.displayMetrics
        if (Utility.readPrefWithNull(this, "LOC1_LABEL", null) == null) {
            UtilityStorePreferences.setDefaults(this)
        }
        initPreferences(this)
        Metar.initialize(this)
        Location.refresh(this)
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

        //        val httpClientUnsafe = HttpUnsafe.getUnsafeOkHttpClient()
        lateinit var preferences: SharedPreferences
        lateinit var editor: SharedPreferences.Editor
        lateinit var dm: DisplayMetrics
        lateinit var appContext: Context
        private var loadedBuffers = false

        fun initPreferences(context: Context) {
            RadarPreferences.initialize(context)
            UIPreferences.initialize(context, dm)
            UtilityHomeScreen.setupMap()
            NotificationPreferences.initialize(context)
            ColorPalette.initialize(context)
            PolygonWatch.load(context)
        }

        fun loadGeomAndColorBuffers(context: Context) {
            if (!loadedBuffers) {
                loadedBuffers = true
                ColorPalettes.initialize(context)
                PolygonWarning.load(context)
                RadarGeometry.initialize(context)
            }
            PolygonType.refresh()
        }
    }
}

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

package joshuatee.wx

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Environment
import androidx.preference.PreferenceManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.TypedValue
import joshuatee.wx.audio.UtilityTts

import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

import joshuatee.wx.notifications.UtilityNotificationTextProduct
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.ObjectPolygonWarning
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.PolygonWarningType
import joshuatee.wx.radar.*
import joshuatee.wx.radar.UtilityDownloadMcd
import joshuatee.wx.radar.UtilityDownloadMpd
import joshuatee.wx.radar.UtilityDownloadWarnings
import joshuatee.wx.radar.UtilityDownloadWatch
import joshuatee.wx.radarcolorpalettes.ObjectColorPalette
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UtilityHomeScreen
import joshuatee.wx.ui.ObjectImagesCollection
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.UtilityCities
import joshuatee.wx.util.UtilityLog
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

//
// Class that has methods that run at app start and store critical preference data structures
//

class MyApplication : Application() {

    // FIXME numerous camelCase opportunities below
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        editor = preferences.edit()
        preferencesTelecine = getSharedPreferences("telecine", Context.MODE_PRIVATE)
        contentResolverLocal = contentResolver
        val res = resources
        dm = res.displayMetrics
        deviceScale = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, dm)
        padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, res.getDimension(R.dimen.padding_dynamic_tv), dm).toInt()
        paddingSettings = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, res.getDimension(R.dimen.padding_dynamic_tv_settings), dm).toInt()
        paddingSmall = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, res.getDimension(R.dimen.padding_dynamic_tv_small), dm).toInt()
        // FIXME needed? dup in UIpref
        val normalTextSize = getInitialPreference("TEXTVIEW_FONT_SIZE", 16) // 14 16 21
        textSizeSmall = UtilityUI.spToPx(normalTextSize - 2, this)
        textSizeNormal = UtilityUI.spToPx(normalTextSize, this)
        textSizeLarge = UtilityUI.spToPx(normalTextSize + 5, this)
        lLpadding = res.getDimension(R.dimen.padding_ll)
        // Calculate ActionBar height
        val tv = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, res.displayMetrics)
        }
        initPreferences(this)
        primaryColor = preferences.getInt("MYAPP_PRIMARY_COLOR", 0)
        Location.refreshLocationData(this)
        System.setProperty("http.keepAlive", "false")
        newline = System.getProperty("line.separator") ?: "\n"
        spinnerLayout = if (UIPreferences.themeIsWhite) {
            R.layout.spinner_row_white
        } else {
            R.layout.spinner_row_blue
        }
        //
        // Most HTTP downloads will use the structures setup below
        //
        val okHttp3Interceptor = Interceptor { chain ->
            val request = chain.request()
            var response = chain.proceed(request)
            var tryCount = 0
            while (!response.isSuccessful && tryCount < 3) {
                tryCount += 1
                // https://github.com/square/okhttp/issues/4986
                response.close()
                response = chain.proceed(request)
            }
            response
        }
        httpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(okHttp3Interceptor)
                .build()
        UtilityCities.initialize()
        if (notifTts) {
            UtilityTts.initTts(applicationContext)
            UtilityLog.d("wx", "DEBUG: TTS init for notif" )
        }
        if (!loadedBuffers) {
            initBuffers(this)
        }
        imageCollectionMap = ObjectImagesCollection.initialize()
        PolygonType.refresh()
    }

    companion object {
        //
        // Helper variables referenced in numerous other classes
        //
        const val packageNameAsString = "joshuatee.wx"
        const val packageNameFileNameAsString = "joshuatee_wx"
        const val emailAsString = "elymbmx@gmail.com"

        val TAG: String = "joshuatee MyApplication"
        val FilesPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wX/"
        val PalFilesPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wX/pal/"
        val BackupFilesPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wXBackup/"
        val BackupPalFilesPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wXBackup/pal/"
        const val nwsSPCwebsitePrefix = "https://www.spc.noaa.gov"
        const val nwsWPCwebsitePrefix = "https://www.wpc.ncep.noaa.gov"
        const val nwsAWCwebsitePrefix = "https://www.aviationweather.gov"
        const val nwsGraphicalWebsitePrefix = "https://graphical.weather.gov"
        const val nwsCPCNcepWebsitePrefix = "https://www.cpc.ncep.noaa.gov"
        const val nwsGoesWebsitePrefix = "https://www.goes.noaa.gov"
        const val nwsOpcWebsitePrefix = "https://ocean.weather.gov"
        const val nwsNhcWebsitePrefix = "https://www.nhc.noaa.gov"
        // const val nwsRadarWebsitePrefix = "https://radar.weather.gov"
        const val nwsMagNcepWebsitePrefix = "https://mag.ncep.noaa.gov"
        const val goes16Url = "https://cdn.star.nesdis.noaa.gov"
        const val goes16AnimUrl = "https://www.star.nesdis.noaa.gov"
        const val nwsApiUrl = "https://api.weather.gov"
        const val nwsSwpcWebSitePrefix = "https://services.swpc.noaa.gov"
        const val canadaEcSitePrefix = "https://weather.gc.ca"
        const val tgftpSitePrefix = "https://tgftp.nws.noaa.gov"
        const val nwsWeatherGov = "https://w1.weather.gov"
        const val prefSeparator = " : : :"
        const val sep = "ABC123"
        const val pre2Pattern = "<pre>(.*?)</pre>"
        var uiAnimIconFrames = "rid"
        const val WIDGET_FILE_BAK = "BAK"
        val HM_CLASS = mutableMapOf<String, Class<*>>()
        val HM_CLASS_ARGS = mutableMapOf<String, Array<String>>()
        val HM_CLASS_ID = mutableMapOf<String, String>()
        var imageCollectionMap = mutableMapOf<String, ObjectImagesCollection>()
        init {
            UtilityHomeScreen.setupMap()
        }
        var primaryColor = 0
        var deviceScale = 0f
        var httpClient: OkHttpClient? = null
        lateinit var preferences: SharedPreferences
        private lateinit var preferencesTelecine: SharedPreferences
        lateinit var editor: SharedPreferences.Editor
        lateinit var dm: DisplayMetrics
        lateinit var appContext: Context
        var mediaNotifTtsTitle = ""
        const val notificationStrSep = ","
        var cardElevation = 0.0f
        var fabElevation = 0.0f
        var fabElevationDepressed = 0.0f
        var elevationPref = 0.0f
        var elevationPrefDefault = 5
        var textSizeSmall = 0.0f
        var textSizeNormal = 0.0f
        var textSizeLarge = 0.0f
        var cardCorners = 0.0f
        var lLpadding = 0.0f
        var telecineVideoSizePercentage = 0
        var telecineSwitchShowCountdown = false
        var telecineSwitchRecordingNotification = false
        var telecineSwitchShowTouches = false
        var tileDownsize = false
        var iconsEvenSpaced = false
        var simpleMode = false
        var checkspc = false
        var checkwpc = false
        var checktor = false
        var checkinternet = false
        var vrButton = false
        var radarUseJni = false
        var contentResolverLocal: ContentResolver? = null
        var blackBg = false
        var widgetPreventTap = false
        var fullscreenMode = false
        var lockToolbars = false
        var unitsM = false
        var unitsF = false
        var locDisplayImg = false
        var alertOnlyOnce = false
        var drawToolColor = 0
        var widgetTextColor = 0
        var widgetHighlightTextColor = 0
        var widgetNexradSize = 0
        var widgetCCShow7Day = false
        var nwsIconTextColor = 0
        var nwsIconBottomColor = 0
        var nexradRadarBackgroundColor = 0
        var wxoglSize = 0
        var wxoglSizeDefault = 13
        var wxoglRememberLocation = false
        var wxoglRadarAutoRefresh = false
        var wfoFav = ""
        var ridFav = ""
        var sndFav = ""
        var srefFav = ""
        var spcMesoFav = ""
        var caRidFav = ""
        //var spcmesoLabelFav = ""
        var nwsTextFav = ""
        //var radarColorPalette = mutableMapOf<String, String>()
        var radarColorPalette = mutableMapOf<Int, String>()
        var radarColorPaletteList = mutableMapOf<Int, String>()
        var notifSoundUri = ""
        var homescreenFav = ""
        const val HOMESCREEN_FAV_DEFAULT = "TXT-CC2:TXT-HAZ:OGL-RADAR:TXT-7DAY2"
        var alertNotificationSoundTornadoCurrent = false
        var alertNotificationSoundSpcmcd = false
        var alertNotificationSoundWpcmpd = false
        //var alertNotificationSoundNhcEpac = false
        var alertNotificationSoundNhcAtl = false
        var alertNotificationSoundSpcwat = false
        var alertNotificationSoundSpcswo = false
        var alertNotificationSoundTextProd = false
        var notifSoundRepeat = false
        var notifTts = false
        var alertBlackoutAmCurrent = 0
        var alertBlackoutPmCurrent = 0
        var alertTornadoNotification = false
        var alertSpcMcdNotification = false
        var alertSpcWatchNotification = false
        var alertSpcSwoNotification = false
        var alertSpcSwoSlightNotification = false
        var alertWpcMpdNotification = false
        var alertBlackoutTornado = false
        var alertNhcEpacNotification = false
        var alertNhcAtlNotification = false
        var alertAutocancel = false
        var alertBlackout = false
        const val DEGREE_SYMBOL = "\u00B0"
        var playlistStr = ""
        var notifTextProdStr = ""
        var newline = ""
        var space: Pattern = Pattern.compile(" ")
        var colon: Pattern = Pattern.compile(":")
        var slash: Pattern = Pattern.compile("/")
        var comma: Pattern = Pattern.compile(",")
        var spinnerLayout = R.layout.spinner_row_blue
        var actionBarHeight = 0
        var wxoglZoom = 0.0f
        var wxoglRid = ""
        var wxoglProd = ""
        var wxoglX = 0.0f
        var wxoglY = 0.0f
        val severeDashboardTor = DataStorage("SEVERE_DASHBOARD_TOR")
        val severeDashboardTst = DataStorage("SEVERE_DASHBOARD_TST")
        val severeDashboardFfw = DataStorage("SEVERE_DASHBOARD_FFW")
        val severeDashboardWat = DataStorage("SEVERE_DASHBOARD_WAT")
        val severeDashboardMcd = DataStorage("SEVERE_DASHBOARD_MCD")
        val severeDashboardMpd = DataStorage("SEVERE_DASHBOARD_MPD")
        val watchLatLon = DataStorage("WATCH_LATLON")
        val watchLatLonTor = DataStorage("WATCH_LATLON_TOR")
        val watchNoList = DataStorage("WATCH_NO_LIST")
        val watchLatLonList = DataStorage("WATCH_LATLON_LIST")
        val mcdLatLon = DataStorage("MCD_LATLON")
        val mcdNoList = DataStorage("MCD_NO_LIST")
        val mpdLatLon = DataStorage("MPD_LATLON")
        val mpdNoList = DataStorage("MPD_NO_LIST")
        var wfoTextFav = ""
        var wpcTextFav = ""
        var spotterFav = ""
        var sn_key = ""
        var sn_locationreport = false
        const val ICON_ALERT_2 = R.drawable.ic_report_24dp
        const val ICON_MAP = R.drawable.ic_public_24dp
        const val ICON_MPD = R.drawable.ic_brightness_7_24dp
        const val ICON_TORNADO = R.drawable.ic_flash_off_24dp
        const val ICON_MCD = R.drawable.ic_directions_24dp
        const val ICON_ACTION = R.drawable.ic_play_arrow_24dp
        const val ICON_ALERT = R.drawable.ic_warning_24dp
        const val ICON_RADAR = R.drawable.ic_flash_on_24dp
        const val ICON_RADAR_WHITE = R.drawable.ic_flash_on_24dp_white
        // const val ICON_RADAR_BLACK = R.drawable.ic_flash_on_24dp_black

        const val ICON_FORECAST = R.drawable.ic_place_24dp
        const val ICON_CURRENT = R.drawable.ic_info_outline_24dp
        // const val ICON_CURRENT_BLACK = R.drawable.ic_info_outline_24dp_black
        const val ICON_CURRENT_WHITE = R.drawable.ic_info_outline_24dp_white

        const val ICON_NHC_1 = R.drawable.ic_brightness_auto_24dp
        //const val ICON_NHC_2 = R.drawable.ic_brightness_medium_24dp
        const val ICON_DELETE = R.drawable.ic_delete_24dp
        const val ICON_DELETE_WHITE = R.drawable.ic_delete_24dp_white

        const val STAR_ICON = R.drawable.ic_star_24dp
        const val STAR_OUTLINE_ICON = R.drawable.ic_star_outline_24dp
        const val STAR_ICON_WHITE = R.drawable.ic_star_24dp_white
        const val STAR_OUTLINE_ICON_WHITE = R.drawable.ic_star_outline_24dp_white

        //const val ICON_PLAY = R.drawable.ic_play_arrow_24dp
        const val ICON_PLAY_WHITE = R.drawable.ic_play_arrow_24dp_white

        //const val ICON_STOP = R.drawable.ic_stop_24dp
        const val ICON_STOP_WHITE = R.drawable.ic_stop_24dp_white

        const val ICON_MIC = R.drawable.ic_mic_24dp
        const val ICON_PAUSE = R.drawable.ic_pause_24dp
        const val ICON_PAUSE_WHITE = R.drawable.ic_pause_24dp_white

        const val ICON_PAUSE_PRESSED = R.drawable.ic_pause_white_24dp
        const val ICON_PAUSE_PRESSED_BLUE = R.drawable.ic_pause_blue_24dp
        const val ICON_ADD = R.drawable.ic_add_box_24dp
        const val ICON_ADD2 = R.drawable.ic_add2_box_24dp_white

        //const val ICON_BACK = R.drawable.ic_keyboard_arrow_left_24dp
        //const val ICON_FORWARD = R.drawable.ic_keyboard_arrow_right_24dp
        const val ICON_SKIP_BACK = R.drawable.ic_skip_previous_24dp
        const val ICON_SKIP_FORWARD = R.drawable.ic_skip_next_24dp
        var spchrrrZoom = 0.0f
        var spchrrrX = 0.0f
        var spchrrrY = 0.0f
        var wpcgefsZoom = 0.0f
        var wpcgefsX = 0.0f
        var wpcgefsY = 0.0f
        var nwsIconSize = 0
        var padding = 0
        var paddingSettings = 0
        var paddingSmall = 0
        var tabHeaders = arrayOf("", "", "")
        var radarLocationUpdateInterval = 10

        //
        // Legacy forecast support
        //
        var utilUS_weather_summary_pattern: Pattern = Pattern.compile(".*?weather-summary=(.*?)/>.*?")
        var utilUS_period_name_pattern: Pattern = Pattern.compile(".*?period-name=(.*?)>.*?")
        // var utilUS_headline_pattern: Pattern = Pattern.compile(".*?headline=(.*?)>.*?")
        // var utilUS_hazardtexturl_pattern: Pattern = Pattern.compile(".*?<hazardTextURL>(.*?)</hazardTextURL>.*?")
        var xml_value_pattern: Pattern = Pattern.compile("<value>")

        fun initPreferences(context: Context) {
            initRadarPreferences()
            UIPreferences.initPreferences(context)
            radarGeometrySetColors()
            listOf(94, 99, 134, 135, 159, 161, 163, 165, 172).forEach {
                radarColorPalette[it] = getInitialPreferenceString("RADAR_COLOR_PALETTE_$it", "CODENH")
            }
            cardCorners = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, preferences.getInt("CARD_CORNER_RADIUS", 0).toFloat(), dm)
            telecineVideoSizePercentage = preferencesTelecine.getInt("video-size", 100)
            telecineSwitchShowCountdown = preferencesTelecine.getBoolean("show-countdown", false)
            telecineSwitchRecordingNotification = preferencesTelecine.getBoolean("recording-notification", false)
            telecineSwitchShowTouches = false
            vrButton = getInitialPreference("VR_BUTTON", "false")
            radarUseJni = getInitialPreference("RADAR_USE_JNI", "false")
            tileDownsize = getInitialPreference("TILE_IMAGE_DOWNSIZE", "false")
            iconsEvenSpaced = getInitialPreference("UI_ICONS_EVENLY_SPACED", "false")
            simpleMode = getInitialPreference("SIMPLE_MODE", "false")
            checkspc = getInitialPreference("CHECKSPC", "false")
            checkwpc = getInitialPreference("CHECKWPC", "false")
            checktor = getInitialPreference("CHECKTOR", "false")
            if (UtilityUI.isTablet()) {
                UIPreferences.nwsIconSizeDefault = 6
            }
            nwsIconSize = preferences.getInt("NWS_ICON_SIZE_PREF", UIPreferences.nwsIconSizeDefault)
            uiAnimIconFrames = getInitialPreferenceString("UI_ANIM_ICON_FRAMES", "10")
            blackBg = getInitialPreference("NWS_RADAR_BG_BLACK", "")
            widgetPreventTap = getInitialPreference("UI_WIDGET_PREVENT_TAP", "")
            fullscreenMode = getInitialPreference("FULLSCREEN_MODE", "false")
            lockToolbars = getInitialPreference("LOCK_TOOLBARS", "false")
            alertOnlyOnce = getInitialPreference("ALERT_ONLYONCE", "false")
            unitsM = getInitialPreference("UNITS_M", "true")
            unitsF = getInitialPreference("UNITS_F", "true")
            drawToolColor = getInitialPreference("DRAW_TOOL_COLOR", Color.rgb(255, 0, 0))
            widgetTextColor = getInitialPreference("WIDGET_TEXT_COLOR", Color.WHITE)
            widgetHighlightTextColor = getInitialPreference("WIDGET_HIGHLIGHT_TEXT_COLOR", Color.YELLOW)
            widgetNexradSize = getInitialPreference("WIDGET_NEXRAD_SIZE", 10)
            widgetCCShow7Day = getInitialPreference("WIDGET_CC_DONOTSHOW_7_DAY", "true")
            nwsIconTextColor = getInitialPreference("NWS_ICON_TEXT_COLOR", Color.rgb(38, 97, 139))
            nwsIconBottomColor = getInitialPreference("NWS_ICON_BOTTOM_COLOR", Color.rgb(255, 255, 255))
            nexradRadarBackgroundColor = getInitialPreference("NEXRAD_RADAR_BACKGROUND_COLOR", Color.rgb(0, 0, 0))
            if (UtilityUI.isTablet()) {
                wxoglSizeDefault = 8
            }
            wxoglSize = getInitialPreference("WXOGL_SIZE", wxoglSizeDefault)
            wxoglRememberLocation = getInitialPreference("WXOGL_REMEMBER_LOCATION", "false")
            wxoglRadarAutoRefresh = getInitialPreference("RADAR_AUTOREFRESH", "false")
            wfoFav = getInitialPreferenceString("WFO_FAV", prefSeparator)
            ridFav = getInitialPreferenceString("RID_FAV", prefSeparator)
            sndFav = getInitialPreferenceString("SND_FAV", prefSeparator)
            srefFav = getInitialPreferenceString("SREF_FAV", prefSeparator)
            spcMesoFav = getInitialPreferenceString("SPCMESO_FAV", prefSeparator)
            caRidFav = getInitialPreferenceString("RID_CA_FAV", prefSeparator)
            //spcmesoLabelFav = getInitialPreferenceString("SPCMESO_LABEL_FAV", prefSeparator)
            nwsTextFav = getInitialPreferenceString("NWS_TEXT_FAV", prefSeparator)
            notifSoundUri = getInitialPreferenceString("NOTIF_SOUND_URI", "")
            if (notifSoundUri == "") notifSoundUri = Settings.System.DEFAULT_NOTIFICATION_URI.toString()
            sn_key = getInitialPreferenceString("SN_KEY", "")
            sn_locationreport = getInitialPreference("SN_LOCATIONREPORT", "")
            spotterFav = getInitialPreferenceString("SPOTTER_FAV", "")
            homescreenFav = getInitialPreferenceString("HOMESCREEN_FAV", HOMESCREEN_FAV_DEFAULT)
            locDisplayImg = homescreenFav.contains("OGL-RADAR") || homescreenFav.contains("NXRD")
            alertNotificationSoundTornadoCurrent = getInitialPreference("ALERT_NOTIFICATION_SOUND_TORNADO", "false")
            alertNotificationSoundSpcmcd = getInitialPreference("ALERT_NOTIFICATION_SOUND_SPCMCD", "false")
            alertNotificationSoundWpcmpd = getInitialPreference("ALERT_NOTIFICATION_SOUND_WPCMPD", "false")
            //alertNotificationSoundNhcEpac = getInitialPreference("ALERT_NOTIFICATION_SOUND_NHC_EPAC", "false")
            alertNotificationSoundNhcAtl = getInitialPreference("ALERT_NOTIFICATION_SOUND_NHC_ATL", "false")
            alertNotificationSoundSpcwat = getInitialPreference("ALERT_NOTIFICATION_SOUND_SPCWAT", "false")
            alertNotificationSoundSpcswo = getInitialPreference("ALERT_NOTIFICATION_SOUND_SPCSWO", "false")
            alertNotificationSoundTextProd = getInitialPreference("ALERT_NOTIFICATION_SOUND_TEXT_PROD", "false")
            notifSoundRepeat = getInitialPreference("NOTIF_SOUND_REPEAT", "false")
            notifTts = getInitialPreference("NOTIF_TTS", "false")
            alertBlackoutAmCurrent = getInitialPreference("ALERT_BLACKOUT_AM", -1)
            alertBlackoutPmCurrent = getInitialPreference("ALERT_BLACKOUT_PM", -1)
            alertTornadoNotification = getInitialPreference("ALERT_TORNADO_NOTIFICATION", "false")
            alertSpcMcdNotification = getInitialPreference("ALERT_SPCMCD_NOTIFICATION", "false")
            alertSpcWatchNotification = getInitialPreference("ALERT_SPCWAT_NOTIFICATION", "false")
            alertSpcSwoNotification = getInitialPreference("ALERT_SPCSWO_NOTIFICATION", "false")
            alertSpcSwoSlightNotification = getInitialPreference("ALERT_SPCSWO_SLIGHT_NOTIFICATION", "false")
            alertWpcMpdNotification = getInitialPreference("ALERT_WPCMPD_NOTIFICATION", "false")
            alertBlackoutTornado = getInitialPreference("ALERT_BLACKOUT_TORNADO", "false")
            alertNhcEpacNotification = getInitialPreference("ALERT_NHC_EPAC_NOTIFICATION", "false")
            alertNhcAtlNotification = getInitialPreference("ALERT_NHC_ATL_NOTIFICATION", "false")
            alertAutocancel = getInitialPreference("ALERT_AUTOCANCEL", "false")
            alertBlackout = getInitialPreference("ALERT_BLACKOUT", "false")
            playlistStr = getInitialPreferenceString("PLAYLIST", "")
            notifTextProdStr = getInitialPreferenceString(UtilityNotificationTextProduct.PREF_TOKEN, "")
            WXGLNexrad.colorPaletteProducts.forEach {
                radarColorPaletteList[it] = getInitialPreferenceString("RADAR_COLOR_PALETTE_" + it + "_LIST", "")
            }
            wxoglZoom = preferences.getFloat("WXOGL_ZOOM", wxoglSize.toFloat() / 10.0f)
            wxoglRid = getInitialPreferenceString("WXOGL_RID", "")
            wxoglProd = getInitialPreferenceString("WXOGL_PROD", "N0Q")
            wxoglX = getInitialPreference("WXOGL_X", 0.0f)
            wxoglY = getInitialPreference("WXOGL_Y", 0.0f)
            Location.currentLocationStr = getInitialPreferenceString("CURRENT_LOC_FRAGMENT", "1")
            severeDashboardTor.update(context)
            severeDashboardTst.update(context)
            severeDashboardFfw.update(context)
            severeDashboardWat.update(context)
            severeDashboardMcd.update(context)
            severeDashboardMpd.update(context)
            watchNoList.update(context)
            watchLatLonList.update(context)
            watchLatLon.update(context)
            watchLatLonTor.update(context)
            mcdLatLon.update(context)
            mcdNoList.update(context)
            mpdLatLon.update(context)
            mpdNoList.update(context)
            wfoTextFav = getInitialPreferenceString("WFO_TEXT_FAV", "AFD")
            wpcTextFav = getInitialPreferenceString("WPC_TEXT_FAV", "pmdspd")
            spchrrrZoom = getInitialPreference("SPCHRRR_ZOOM", 1.0f)
            spchrrrX = getInitialPreference("SPCHRRR_X", 0.5f)
            spchrrrY = getInitialPreference("SPCHRRR_Y", 0.5f)
            wpcgefsZoom = getInitialPreference("WPCGEFS_ZOOM", 1.0f)
            wpcgefsX = getInitialPreference("WPCGEFS_X", 0.5f)
            wpcgefsY = getInitialPreference("WPCGEFS_Y", 0.5f)
            elevationPref = getInitialPreference("ELEVATION_PREF", elevationPrefDefault).toFloat()
            elevationPref = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, elevationPref, dm)
            cardElevation = elevationPref
            fabElevation = elevationPref
            fabElevationDepressed = elevationPref * 2
            tabHeaders[0] = getInitialPreferenceString("TAB1_HEADER", "LOCAL")
            tabHeaders[1] = getInitialPreferenceString("TAB2_HEADER", "SPC")
            tabHeaders[2] = getInitialPreferenceString("TAB3_HEADER", "MISC")
        }

        val locations = mutableListOf<Location>()

        // source for county and state lines
        // http://www2.census.gov/geo/tiger/GENZ2010/
        // gz_2010_us_040_00_20m.kml
        //
        // cb_2014_us_county_20m.kml
        // https://www.census.gov/geo/maps-data/data/kml/kml_counties.html
        // ./android-sdk-linux/platform-tools/adb pull /data/data/joshuatee.wx/files/statev3.bin

        val colorMap = mutableMapOf<Int, ObjectColorPalette>()
        var loadedBuffers = false

        fun initBuffers(context: Context) {
            loadedBuffers = true
            ColorPalettes.initialize(context)
            initRadarGeometryAll(context)
            GeographyType.refresh()
        }

        private const val canadaResId = R.raw.ca
        private const val mexicoResId = R.raw.mx
        private const val caCnt = 161792
        private const val mxCnt = 151552
        var stateRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
        var countState = 200000 // v3 205748
        var hwRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
        var countHw = 862208 // on disk size 3448832 yields  862208
        var hwExtRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
        const val countHwExt = 770048 // on disk 3080192 yields 770048
        private const val hwExtFileResId = R.raw.hwv4ext // 2016_04_06
        var lakesRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
        const val countLakes = 503808 // was 14336 + 489476
        var countyRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
        var countCounty = 212992 // file on disk is 851968, should be
        private var hwFileResId = R.raw.hwv4
        private const val lakesFileResId = R.raw.lakesv3
        private var countyFileResId = R.raw.county
        var radarColorHw = 0
        var radarColorHwExt = 0
        var radarColorState = 0
        var radarColorTstorm = 0
        var radarColorTstormWatch = 0
        var radarColorTor = 0
        var radarColorTorWatch = 0
        var radarColorFfw = 0
        var radarColorMcd = 0
        var radarColorMpd = 0
        var radarColorLocdot = 0
        var radarColorSpotter = 0
        var radarColorCity = 0
        var radarColorLakes = 0
        var radarColorCounty = 0
        var radarColorSti = 0
        var radarColorHi = 0
        var radarColorHiText = 0
        var radarColorObs = 0
        var radarColorObsWindbarbs = 0
        var radarColorCountyLabels = 0
        var radarShowLegendTextColor = 0
        var radarWarningPolygons = mutableListOf<ObjectPolygonWarning>()
        const val animationIntervalDefault = 8

        private fun radarGeometrySetColors() {
            radarColorHw = getInitialPreference("RADAR_COLOR_HW", Color.BLUE)
            radarColorHwExt = getInitialPreference("RADAR_COLOR_HW_EXT", Color.BLUE)
            radarColorState = getInitialPreference("RADAR_COLOR_STATE", Color.WHITE)
            radarColorTstorm = getInitialPreference("RADAR_COLOR_TSTORM", Color.YELLOW)
            radarColorTstormWatch = getInitialPreference("RADAR_COLOR_TSTORM_WATCH", Color.BLUE)
            radarColorTor = getInitialPreference("RADAR_COLOR_TOR", Color.RED)
            radarColorTorWatch = getInitialPreference("RADAR_COLOR_TOR_WATCH", Color.rgb(113, 0, 0)) //dark red for watch lines
            radarColorFfw = getInitialPreference("RADAR_COLOR_FFW", Color.GREEN)
            radarColorMcd = getInitialPreference("RADAR_COLOR_MCD", Color.rgb(255, 255, 163))
            radarColorMpd = getInitialPreference("RADAR_COLOR_MPD", Color.GREEN)
            radarColorLocdot = getInitialPreference("RADAR_COLOR_LOCDOT", Color.WHITE)
            radarColorSpotter = getInitialPreference("RADAR_COLOR_SPOTTER", Color.GREEN)
            radarColorCity = getInitialPreference("RADAR_COLOR_CITY", Color.WHITE)
            radarColorLakes = getInitialPreference("RADAR_COLOR_LAKES", Color.rgb(0, 0, 163))
            radarColorCounty = getInitialPreference("RADAR_COLOR_COUNTY", Color.rgb(75, 75, 75))
            radarColorSti = getInitialPreference("RADAR_COLOR_STI", Color.WHITE)
            radarColorHi = getInitialPreference("RADAR_COLOR_HI", Color.GREEN)
            radarColorHiText = getInitialPreference("RADAR_COLOR_HI_TEXT", Color.GREEN)
            radarColorObs = getInitialPreference("RADAR_COLOR_OBS", Color.rgb(255, 255, 255))
            radarColorObsWindbarbs = getInitialPreference("RADAR_COLOR_OBS_WINDBARBS", Color.WHITE)
            radarColorCountyLabels = getInitialPreference("RADAR_COLOR_COUNTY_LABELS", Color.rgb(75, 75, 75))
            radarShowLegendTextColor = getInitialPreference("RADAR_SHOW_LEGEND_TEXTCOLOR", Color.WHITE)
        }

        private fun initRadarGeometryAll(context: Context) {
            initGenericRadarWarnings(context)
            GeographyType.values().forEach { initRadarGeometryByType(context, it) }
        }

        fun initGenericRadarWarnings(context: Context) {
            // FIXME don't need both, prefer top one shared with iOS
            ObjectPolygonWarning.load(context)
            radarWarningPolygons.clear()
            PolygonWarningType.values().forEach { radarWarningPolygons.add(ObjectPolygonWarning(context, it)) }
        }

        fun initRadarGeometryByType(context: Context, type: GeographyType) {
            if (!radarHwEnh) {
                hwFileResId = R.raw.hw
                countHw = 112640
            }
            var stateLinesFileResId = R.raw.statev2
            countState = 205748
            if (radarStateHires) {
                stateLinesFileResId = R.raw.statev3
                countState = 1166552
            }
            if (radarCamxBorders) countState += caCnt + mxCnt
            if (radarCountyHires) {
                countyFileResId = R.raw.countyv2
                countCounty = 820852
            }
            val fileIds = listOf(
                    lakesFileResId,
                    hwFileResId,
                    countyFileResId,
                    stateLinesFileResId,
                    canadaResId,
                    mexicoResId,
                    hwExtFileResId
            )
            val countArr = listOf(countLakes, countHw, countCounty, countState, caCnt, mxCnt, countHwExt)
            val prefArr = listOf(true, true, true, true, radarCamxBorders, radarCamxBorders, radarHwEnhExt)
            when (type) {
                GeographyType.STATE_LINES -> {
                    stateRelativeBuffer = ByteBuffer.allocateDirect(4 * countState)
                    stateRelativeBuffer.order(ByteOrder.nativeOrder())
                    stateRelativeBuffer.position(0)
                    listOf(3, 4, 5).forEach { loadBuffer(context, fileIds[it], stateRelativeBuffer, countArr[it], prefArr[it]) }
                }
                GeographyType.HIGHWAYS -> {
                    hwRelativeBuffer = ByteBuffer.allocateDirect(4 * countHw)
                    hwRelativeBuffer.order(ByteOrder.nativeOrder())
                    hwRelativeBuffer.position(0)
                    for (s in intArrayOf(1)) { loadBuffer(context, fileIds[s], hwRelativeBuffer, countArr[s], prefArr[s]) }
                }
                GeographyType.HIGHWAYS_EXTENDED -> {
                    if (radarHwEnhExt) {
                        hwExtRelativeBuffer = ByteBuffer.allocateDirect(4 * countHwExt)
                        hwExtRelativeBuffer.order(ByteOrder.nativeOrder())
                        hwExtRelativeBuffer.position(0)
                    }
                    for (s in intArrayOf(6)) { loadBuffer(context, fileIds[s], hwExtRelativeBuffer, countArr[s], prefArr[s]) }
                }
                GeographyType.LAKES -> {
                    lakesRelativeBuffer = ByteBuffer.allocateDirect(4 * countLakes)
                    lakesRelativeBuffer.order(ByteOrder.nativeOrder())
                    lakesRelativeBuffer.position(0)
                    val s = 0
                    loadBuffer(context, fileIds[s], lakesRelativeBuffer, countArr[s], prefArr[s])
                }
                GeographyType.COUNTY_LINES -> {
                    countyRelativeBuffer = ByteBuffer.allocateDirect(4 * countCounty)
                    countyRelativeBuffer.order(ByteOrder.nativeOrder())
                    countyRelativeBuffer.position(0)
                    val s = 2
                    loadBuffer(context, fileIds[s], countyRelativeBuffer, countArr[s], prefArr[s])
                }
                else -> {}
            }
        }

        private fun loadBuffer(context: Context, fileID: Int, byteBuffer: ByteBuffer, count: Int, isEnabled: Boolean) {
            if (isEnabled) {
                try {
                    val inputStream = context.resources.openRawResource(fileID)
                    val dataInputStream = DataInputStream(BufferedInputStream(inputStream))
                    for (index in 0 until count) {
                        byteBuffer.putFloat(dataInputStream.readFloat())
                    }
                    dataInputStream.close()
                    inputStream.close()
                } catch (e: IOException) {
                    UtilityLog.handleException(e)
                }
            }
        }

        //
        // Radar Preferences
        //
        const val nwsRadarPub = "https://tgftp.nws.noaa.gov/"
        const val nwsRadarLevel2Pub = "https://nomads.ncep.noaa.gov/pub/data/nccf/radar/nexrad_level2/"
        //conus radar for zoom out
        //const val nwsConusRadar = "https://radar.weather.gov/ridge/Conus/RadarImg/latest_radaronly.gif";
        //const val nwsConusRadarGfw = "https://radar.weather.gov/ridge/Conus/RadarImg/latest_radaronly.gfw"

        //const val nwsConusRadar = "https://atlas.niu.edu/analysis/radar/CONUS/usrad_current_b.gif"  //backup url
        const val nwsConusRadar = "https://www.aviationweather.gov/data/obs/radar/rad_rala_us.gif"
        const val conusImageName = "conus.gif"

        //testing
        const val nwsConusRadarNew = "https://opengeo.ncep.noaa.gov/geoserver/conus/conus_bref_raw/ows?service=wms&version=1.3.0&request=GetCapabilities"


        var radarConusRadar = false
        var radarConusRadarZoom = 75 //was 173
        var radarWarnings = false
        var locationDotFollowsGps = false
        var locdotBug = false
        var dualpaneshareposn = false
        var radarSpotters = false
        var radarSpottersLabel = false
        var radarHailSizeLabel = false
        var radarObs = false
        var radarObsWindbarbs = false
        var radarSwo = false
        var radarUserPoints = false
        var radarCities = false
        var radarHw = false
        var radarLocDot = false
        var radarLakes = false
        var radarCounty = false
        var radarCountyLabels = false
        private var radarCountyHires = false
        private var radarStateHires = false
        var radarWatMcd = false
        var radarMpd = false
        var radarSti = false
        var radarHi = false
        var radarTvs = false
        var radarShowRadar = true
        var radarShowLegend = false
        var radarShowLegendWidth = 0
        var radarShowLegendTextSize = 0
        var drawToolSize = 0
        var radarObsExtZoom = 0
        var radarSpotterSize = 0
        var radarAviationSize = 0
        var radarTextSize = 0.0f
        var radarTextSizeDefault = 1.0f
        var radarUserPointSize = 0
        var radarLocdotSize = 0
        var radarLocIconSize = 0
        var radarLocBugSize = 0
        var radarHiSize = 0
        var radarHiTextSize = 0f
        var radarTvsSize = 0
        var radarDefaultLinesize = 0
        var radarWarnLineSize = 5.0f
        const val radarWarnLineSizeDefault = 5
        var radarWatchMcdLineSize = 4.0f
        var radarWatchMcdLineSizeDefault = 4
        private var radarHwEnh = true
        var radarHwEnhExt = false
        private var radarCamxBorders = false
        var radarIconsLevel2 = false
        var radarStateLineSize = 0
        var radarCountyLineSize = 0
        var radarHwLineSize = 0
        var radarHwExtLineSize = 0
        var radarLakeLineSize = 0
        var radarGpsCircleLineSize = 0
        var radarStiLineSize = 0
        var radarSwoLineSize = 0
        var radarWbLineSize = 0
        var wxoglCenterOnLocation = false
        var radarShowWpcFronts = false
        var radarSpotterSizeDefault = 4
        var radarAviationSizeDefault = 7
        var radarLocationDotSizeDefault = 8

        private fun initRadarPreferences() {
            radarShowWpcFronts = getInitialPreference("RADAR_SHOW_WPC_FRONTS", "false")
            radarLocationUpdateInterval = getInitialPreference("RADAR_LOCATION_UPDATE_INTERVAL", 10)
            radarConusRadar = getInitialPreference("CONUS_RADAR", "false")
            radarConusRadarZoom = getInitialPreference("CONUS_RADAR_ZOOM", 75)
            radarWarnings = getInitialPreference("COD_WARNINGS_DEFAULT", "false")
            locationDotFollowsGps = getInitialPreference("LOCDOT_FOLLOWS_GPS", "false")
            locdotBug = getInitialPreference("LOCDOT_BUG", "false")
            dualpaneshareposn = getInitialPreference("DUALPANE_SHARE_POSN", "true")
            radarSpotters = getInitialPreference("WXOGL_SPOTTERS", "false")
            radarSpottersLabel = getInitialPreference("WXOGL_SPOTTERS_LABEL", "false")
            radarHailSizeLabel = getInitialPreference("WXOGL_HAIL_LABEL", "false")
            radarObs = getInitialPreference("WXOGL_OBS", "false")
            radarObsWindbarbs = getInitialPreference("WXOGL_OBS_WINDBARBS", "false")
            radarSwo = getInitialPreference("RADAR_SHOW_SWO", "false")
            radarUserPoints = getInitialPreference("RADAR_USERPOINTS", "false")
            radarCities = getInitialPreference("COD_CITIES_DEFAULT", "")
            radarHw = getInitialPreference("COD_HW_DEFAULT", "true")
            radarLocDot = getInitialPreference("COD_LOCDOT_DEFAULT", "true")
            radarLakes = getInitialPreference("COD_LAKES_DEFAULT", "false")
            radarCounty = getInitialPreference("RADAR_SHOW_COUNTY", "true")
            radarWatMcd = getInitialPreference("RADAR_SHOW_WATCH", "false")
            radarMpd = getInitialPreference("RADAR_SHOW_MPD", "false")
            radarSti = getInitialPreference("RADAR_SHOW_STI", "false")
            radarHi = getInitialPreference("RADAR_SHOW_HI", "false")
            radarTvs = getInitialPreference("RADAR_SHOW_TVS", "false")
            radarHwEnhExt = getInitialPreference("RADAR_HW_ENH_EXT", "false")
            radarCamxBorders = getInitialPreference("RADAR_CAMX_BORDERS", "false")
            radarCountyLabels = getInitialPreference("RADAR_COUNTY_LABELS", "false")
            radarCountyHires = getInitialPreference("RADAR_COUNTY_HIRES", "false")
            radarStateHires = getInitialPreference("RADAR_STATE_HIRES", "false")
            radarIconsLevel2 = getInitialPreference("WXOGL_ICONS_LEVEL2", "false")
            radarShowRadar = getInitialPreference("RADAR_SHOW_RADAR", "false")
            radarShowLegend = getInitialPreference("RADAR_SHOW_LEGEND", "false")
            radarShowLegendWidth = getInitialPreference("RADAR_SHOW_LEGEND_WIDTH", 50)
            radarShowLegendTextSize = getInitialPreference("RADAR_SHOW_LEGEND_TEXTSIZE", 30)
            wxoglCenterOnLocation = getInitialPreference("RADAR_CENTER_ON_LOCATION", "false")
            drawToolSize = getInitialPreference("DRAWTOOL_SIZE", 4)

            if (UtilityUI.isTablet()) {
                radarSpotterSizeDefault = 2
                radarAviationSizeDefault = 3
                radarLocationDotSizeDefault = 4
            }

            radarObsExtZoom = getInitialPreference("RADAR_OBS_EXT_ZOOM", 7)
            radarSpotterSize = getInitialPreference("RADAR_SPOTTER_SIZE", radarSpotterSizeDefault)
            radarAviationSize = getInitialPreference("RADAR_AVIATION_SIZE", radarAviationSizeDefault)
            radarTextSize = getInitialPreference("RADAR_TEXT_SIZE", radarTextSizeDefault)
            radarUserPointSize = getInitialPreference("RADAR_USERPOINT_SIZE", 100)
            radarLocdotSize = getInitialPreference("RADAR_LOCDOT_SIZE", radarLocationDotSizeDefault)
            radarLocIconSize = getInitialPreference("RADAR_LOCICON_SIZE", 100)
            radarLocBugSize = getInitialPreference("RADAR_LOCBUG_SIZE", 100)
            radarHiSize = getInitialPreference("RADAR_HI_SIZE", 75)
            radarHiTextSize = getInitialPreference("RADAR_HI_TEXT_SIZE", 1.0f)
            radarTvsSize = getInitialPreference("RADAR_TVS_SIZE", 75)
            radarDefaultLinesize = getInitialPreference("RADAR_DEFAULT_LINESIZE", 1)
            radarWarnLineSize = getInitialPreference("RADAR_WARN_LINESIZE", radarWarnLineSizeDefault).toFloat()
            radarWatchMcdLineSize = getInitialPreference("RADAR_WATMCD_LINESIZE", radarWatchMcdLineSizeDefault).toFloat()
            radarStateLineSize = getInitialPreference("RADAR_STATE_LINESIZE", 2)
            radarCountyLineSize = getInitialPreference("RADAR_COUNTY_LINESIZE", 2)
            radarHwLineSize = getInitialPreference("RADAR_HW_LINESIZE", 2)
            radarHwExtLineSize = getInitialPreference("RADAR_HWEXT_LINESIZE", 2)
            radarLakeLineSize = getInitialPreference("RADAR_LAKE_LINESIZE", 2)
            radarGpsCircleLineSize = getInitialPreference("RADAR_GPSCIRCLE_LINESIZE", 5)
            radarStiLineSize = getInitialPreference("RADAR_STI_LINESIZE", 3)
            radarSwoLineSize = getInitialPreference("RADAR_SWO_LINESIZE", 3)
            radarWbLineSize = getInitialPreference("RADAR_WB_LINESIZE", 3)
            resetTimerOnRadarPolygons()
        }

        private fun resetTimerOnRadarPolygons() {
            UtilityDownloadMcd.timer.resetTimer()
            UtilityDownloadMpd.timer.resetTimer()
            UtilityDownloadWarnings.timer.resetTimer()
            UtilityDownloadWarnings.timerSevereDashboard.resetTimer()
            UtilityDownloadWatch.timer.resetTimer()
            UtilityMetar.timer.resetTimer()
            UtilitySpotter.timer.resetTimer()
            UtilitySwoDayOne.timer.resetTimer()
        }

        private fun getInitialPreference(pref: String, initValue: Int) = preferences.getInt(pref, initValue)

        private fun getInitialPreference(pref: String, initValue: Float) = preferences.getFloat(pref, initValue)

        private fun getInitialPreference(pref: String, initValue: String) = (preferences.getString(pref, initValue) ?: initValue).startsWith("t")

        private fun getInitialPreferenceString(pref: String, initValue: String) = preferences.getString(pref, initValue) ?: initValue
    } // end companion object
}

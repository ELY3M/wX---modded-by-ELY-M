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

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.TypedValue

import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

import joshuatee.wx.audio.UtilityTTS
import joshuatee.wx.notifications.UtilityNotificationTextProduct
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.radarcolorpalettes.ObjectColorPalette
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UtilityHomeScreen
import joshuatee.wx.util.UtilityCities
import joshuatee.wx.util.UtilityLog
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MyApplication : Application() {

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    //override fun onLowMemory() {
    //super.onLowMemory()
    //}

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        editor = preferences.edit()
        preferencesTelecine = getSharedPreferences("telecine", Context.MODE_PRIVATE)
        contentResolverLocal = contentResolver
        comma = Pattern.compile(",")
        bang = Pattern.compile("!")
        space = Pattern.compile(" ")
        colon = Pattern.compile(":")
        colonSpace = Pattern.compile(": ")
        semicolon = Pattern.compile(";")
        period = Pattern.compile("\\.")
        slash = Pattern.compile("/")
        val res = resources
        dm = res.displayMetrics
        deviceScale = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, dm)
        padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, res.getDimension(R.dimen.padding_dynamic_tv), dm).toInt()
        paddingSmall = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, res.getDimension(R.dimen.padding_dynamic_tv_small), dm).toInt()
        textSizeSmall = res.getDimension(R.dimen.listitem_text)
        textSizeNormal = res.getDimension(R.dimen.normal_text)
        textSizeLarge = res.getDimension(R.dimen.large_text)
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
        newline = System.getProperty("line.separator")
        spinnerLayout = if (UIPreferences.themeIsWhite) {
            R.layout.spinner_row_white
        } else {
            R.layout.spinner_row_blue
        }
        val okhttp3Interceptor = Interceptor { chain ->
            val request = chain.request()
            var response = chain.proceed(request)
            var tryCount = 0
            while (!response.isSuccessful && tryCount < 3) {
                tryCount += 1
                response = chain.proceed(request)
            }
            response
        }
        httpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(okhttp3Interceptor)
                .build()
        UtilityTTS.initTTS(applicationContext)
        UtilityCities.initCitiesArray()
        if (!loadedBuffers) {
            initBuffers(this)
        }
    }

    companion object {

        val TAG: String = "joshuatee MyApplication"
        val FilesPath: String = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wX/"
        const val nwsSPCwebsitePrefix: String = "https://www.spc.noaa.gov"
        const val nwsWPCwebsitePrefix: String = "https://www.wpc.ncep.noaa.gov"
        const val prefSeperator: String = " : : :"
        var uiAnimIconFrames: String = "rid"
        // FIXME remove in favor of notificationStrSep
        //const val NOTIF_STR_SEP = ","
        const val WIDGET_FILE_BAK: String = "BAK"
        val HM_CLASS: MutableMap<String, Class<*>> = mutableMapOf()
        val HM_CLASS_ARGS: MutableMap<String, Array<String>> = mutableMapOf()
        val HM_CLASS_ID: MutableMap<String, String> = mutableMapOf()

        init {
            UtilityHomeScreen.setupMap()
        }

        var primaryColor: Int = 0
        const val TEXTVIEW_MAGIC_FUDGE_FACTOR: Float = 4.05f
        var deviceScale: Float = 0f
        var httpClient: OkHttpClient? = null
        // FIXME make private

        lateinit var preferences: SharedPreferences
        //private lateinit var preferences: SharedPreferences

        private lateinit var preferencesTelecine: SharedPreferences

        //private lateinit var editor: SharedPreferences.Editor
        lateinit var editor: SharedPreferences.Editor

        lateinit var dm: DisplayMetrics
        lateinit var appContext: Context

        var mediaNotifTtsTitle: String = ""
        const val notificationStrSep: String = ","
        var cardElevation: Float = 0f
        var fabElevation: Float = 0f
        var fabElevationDepressed: Float = 0f
        var elevationPref: Float = 0f
        var textSizeSmall: Float = 0f
        var textSizeNormal: Float = 0f
        var textSizeLarge: Float = 0f
        var cardCorners: Float = 0f
        var lLpadding: Float = 0f
        var telecineVideoSizePercentage: Int = 0
        var telecineSwitchShowCountdown: Boolean = false
        var telecineSwitchRecordingNotification: Boolean = false
        var telecineSwitchShowTouches: Boolean = false
        var tileDownsize: Boolean = false
        var iconsEvenSpaced: Boolean = false
        var helpMode: Boolean = false
        var simpleMode: Boolean = false
        var checkspc: Boolean = false
        var checkwpc: Boolean = false
        var checktor: Boolean = false
        var vrButton: Boolean = false
        var radarUseJni: Boolean = false
        var contentResolverLocal: ContentResolver? = null
        var blackBg: Boolean = false
        var fullscreenMode: Boolean = false
        var lockToolbars: Boolean = false
        var unitsM: Boolean = false
        var unitsF: Boolean = false
        var locDisplayImg: Boolean = false
        var comma: Pattern = Pattern.compile("")
        var bang: Pattern = Pattern.compile("")
        var alertOnlyonce: Boolean = false
        var drawToolColor: Int = 0
        var widgetTextColor: Int = 0
        var widgetHighlightTextColor: Int = 0
        var widgetNexradSize: Int = 0
        var widgetCCShow7Day: Boolean = false
        var nwsIconTextColor: Int = 0
        var nwsIconBottomColor: Int = 0
        var nexradRadarBackgroundColor: Int = 0
        var wxoglSize: Int = 0
        var wxoglRememberLocation: Boolean = false
        var wxoglLocationAutorefresh: Boolean = false
        var wxoglkeepscreenon: Boolean = false
        var wfoFav: String = ""
        var ridFav: String = ""
        var sndFav: String = ""
        var srefFav: String = ""
        var spcmesoFav: String = ""
        var spcmesoLabelFav: String = ""
        var nwsTextFav: String = ""
        var radarColorPalette: MutableMap<String, String> = mutableMapOf()
        var notifSoundUri: String = ""
        var homescreenFav: String = ""
        const val HOMESCREEN_FAV_DEFAULT: String = "TXT-CC2:TXT-HAZ:OGL-RADAR:TXT-7DAY2"
        const val HOMESCREEN_FAV_DEFAULT_CA: String = "TXT-CC2:TXT-HAZ:IMG-CARAIN:TXT-7DAY2"
        var alertNotificationSoundTornadoCurrent: Boolean = false
        var alertNotificationSoundSpcmcd: Boolean = false
        var alertNotificationSoundWpcmpd: Boolean = false
        var alertNotificationSoundNhcEpac: Boolean = false
        var alertNotificationSoundNhcAtl: Boolean = false
        var alertNotificationSoundSpcwat: Boolean = false
        var alertNotificationSoundSpcswo: Boolean = false
        var alertNotificationSoundTextProd: Boolean = false
        var notifSoundRepeat: Boolean = false
        var notifTts: Boolean = false
        var alertBlackoutAmCurrent: Int = 0
        var alertBlackoutPmCurrent: Int = 0
        var alertTornadoNotificationCurrent: Boolean = false
        var alertSpcmcdNotificationCurrent: Boolean = false
        var alertSpcwatNotificationCurrent: Boolean = false
        var alertSpcswoNotificationCurrent: Boolean = false
        var alertSpcswoSlightNotificationCurrent: Boolean = false
        var alertWpcmpdNotificationCurrent: Boolean = false
        var alertBlackoutTornadoCurrent: Boolean = false
        var alertNhcEpacNotificationCurrent: Boolean = false
        var alertNhcAtlNotificationCurrent: Boolean = false
        var alertAutocancel: Boolean = false
        var alertBlackout: Boolean = false
        const val DEGREE_SYMBOL: String = "\u00B0"
        var playlistStr: String = ""
        var notifTextProdStr: String = ""
        var radarColorPalette94List: String = ""
        var radarColorPalette99List: String = ""
        var newline: String = ""
        var space: Pattern = Pattern.compile("")
        var colon: Pattern = Pattern.compile("")
        var colonSpace: Pattern = Pattern.compile("")
        var semicolon: Pattern = Pattern.compile("")
        var period: Pattern = Pattern.compile("")
        var slash: Pattern = Pattern.compile("")
        var spinnerLayout: Int = R.layout.spinner_row_blue
        var actionBarHeight: Int = 0
        var wxoglZoom: Float = 0f
        var wxoglRid: String = ""
        var wxoglProd: String = ""
        var wxoglX: Float = 0f
        var wxoglY: Float = 0f
        val severeDashboardTor: DataStorage = DataStorage("SEVERE_DASHBOARD_TOR")
        val severeDashboardTst: DataStorage = DataStorage("SEVERE_DASHBOARD_TST")
        val severeDashboardFfw: DataStorage = DataStorage("SEVERE_DASHBOARD_FFW")
        val severeDashboardSmw: DataStorage = DataStorage("SEVERE_DASHBOARD_SMW")
        val severeDashboardSvs: DataStorage = DataStorage("SEVERE_DASHBOARD_SVS")
        val severeDashboardSps: DataStorage = DataStorage("SEVERE_DASHBOARD_SPS")
        val severeDashboardWat: DataStorage = DataStorage("SEVERE_DASHBOARD_WAT")
        val severeDashboardMcd: DataStorage = DataStorage("SEVERE_DASHBOARD_MCD")
        val severeDashboardMpd: DataStorage = DataStorage("SEVERE_DASHBOARD_MPD")
        val watchLatlon: DataStorage = DataStorage("WATCH_LATLON")
        val watchLatlonTor: DataStorage = DataStorage("WATCH_LATLON_TOR")
        val mcdLatlon: DataStorage = DataStorage("MCD_LATLON")
        val mcdNoList: DataStorage = DataStorage("MCD_NO_LIST")
        val mpdLatlon: DataStorage = DataStorage("MPD_LATLON")
        val mpdNoList: DataStorage = DataStorage("MPD_NO_LIST")
        var wfoTextFav: String = ""
        var wpcTextFav: String = ""
        var spotterFav: String = ""
        var sn_key: String = ""
        var sn_locationreport: Boolean = false
        const val ICON_ALERT_2: Int = R.drawable.ic_report_24dp
        const val ICON_MAP: Int = R.drawable.ic_public_24dp
        const val ICON_MPD: Int = R.drawable.ic_brightness_7_24dp
        const val ICON_TORNADO: Int = R.drawable.ic_flash_off_24dp
        const val ICON_MCD: Int = R.drawable.ic_directions_24dp
        const val ICON_ACTION: Int = R.drawable.ic_play_arrow_24dp
        const val ICON_ALERT: Int = R.drawable.ic_warning_24dp
        const val ICON_RADAR: Int = R.drawable.ic_flash_on_24dp
        const val ICON_FORECAST: Int = R.drawable.ic_place_24dp
        const val ICON_CURRENT: Int = R.drawable.ic_info_outline_24dp
        const val ICON_NHC_1: Int = R.drawable.ic_brightness_auto_24dp
        const val ICON_NHC_2: Int = R.drawable.ic_brightness_medium_24dp
        const val ICON_DELETE: Int = R.drawable.ic_delete_24dp
        const val STAR_ICON: Int = R.drawable.ic_star_24dp
        const val STAR_OUTLINE_ICON: Int = R.drawable.ic_star_outline_24dp
        const val ICON_PLAY: Int = R.drawable.ic_play_arrow_24dp
        const val ICON_STOP: Int = R.drawable.ic_stop_24dp
        const val ICON_MIC: Int = R.drawable.ic_mic_24dp
        const val ICON_PAUSE: Int = R.drawable.ic_pause_24dp
        const val ICON_PAUSE_PRESSED: Int = R.drawable.ic_pause_white_24dp
        const val ICON_ARROW_UP: Int = R.drawable.ic_keyboard_arrow_up_24dp
        const val ICON_ARROW_DOWN: Int = R.drawable.ic_keyboard_arrow_down_24dp
        const val ICON_ADD: Int = R.drawable.ic_add_box_24dp
        const val ICON_BACK: Int = R.drawable.ic_keyboard_arrow_left_24dp
        const val ICON_FORWARD: Int = R.drawable.ic_keyboard_arrow_right_24dp
        const val ICON_SKIP_BACK: Int = R.drawable.ic_skip_previous_24dp
        const val ICON_SKIP_FORWARD: Int = R.drawable.ic_skip_next_24dp
        const val MD_COMP: String = "<center>No Mesoscale Discussions are currently in effect."
        const val WATCH_COMP: String = "<center><strong>No watches are currently valid"
        const val MPD_COMP: String = "No MPDs are currently in effect."
        const val currentConditionsViaMetar: Boolean = true
        var spchrrrZoom: Float = 0f
        var spchrrrX: Float = 0f
        var spchrrrY: Float = 0f
        var wpcgefsZoom: Float = 0f
        var wpcgefsX: Float = 0f
        var wpcgefsY: Float = 0f
        var spcsseoZoom: Float = 0f
        var spcsseoX: Float = 0f
        var spcsseoY: Float = 0f
        var goesVisZoom: Float = 0f
        var goesVisX: Float = 0f
        var goesVisY: Float = 0f
        var goesVisSector: String = ""
        var nwsIconSize: Int = 0
        var padding: Int = 0
        var paddingSmall: Int = 0
        var tabHeaders: Array<String> = arrayOf("", "", "", "")

        fun initPreferences(context: Context) {
            initRadarPreferences()
            UIPreferences.initPreferences(context)
            radarGeometrySetColors()
            listOf(94, 99, 134, 135, 159, 161, 163, 165, 172).forEach { radarColorPalette[it.toString()] = preferences.getString("RADAR_COLOR_PALETTE_" + it.toString(), "CODENH") }
            cardCorners = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, preferences.getInt("CARD_CORNER_RADIUS", 0).toFloat(), dm)
            telecineVideoSizePercentage = preferencesTelecine.getInt("video-size", 100)
            telecineSwitchShowCountdown = preferencesTelecine.getBoolean("show-countdown", false)
            telecineSwitchRecordingNotification = preferencesTelecine.getBoolean("recording-notification", false)
            telecineSwitchShowTouches = false
            vrButton = preferences.getString("VR_BUTTON", "false").startsWith("t")
            radarUseJni = preferences.getString("RADAR_USE_JNI", "false").startsWith("t")
            tileDownsize = preferences.getString("TILE_IMAGE_DOWNSIZE", "false").startsWith("t")
            iconsEvenSpaced = preferences.getString("UI_ICONS_EVENLY_SPACED", "true").startsWith("t")
            simpleMode = preferences.getString("SIMPLE_MODE", "").startsWith("t")
            checkspc = preferences.getString("CHECKSPC", "false").startsWith("t")
            checkwpc = preferences.getString("CHECKWPC", "false").startsWith("t")
            checktor = preferences.getString("CHECKTOR", "false").startsWith("t")
            nwsIconSize = preferences.getInt("NWS_ICON_SIZE_PREF", 24)
            uiAnimIconFrames = preferences.getString("UI_ANIM_ICON_FRAMES", "6")
            blackBg = preferences.getString("NWS_RADAR_BG_BLACK", "").startsWith("t")
            fullscreenMode = preferences.getString("FULLSCREEN_MODE", "false").startsWith("t")
            lockToolbars = preferences.getString("LOCK_TOOLBARS", "false").startsWith("t")
            alertOnlyonce = preferences.getString("ALERT_ONLYONCE", "false").startsWith("t")
            unitsM = preferences.getString("UNITS_M", "true").startsWith("t")
            unitsF = preferences.getString("UNITS_F", "true").startsWith("t")
            drawToolColor = preferences.getInt("DRAW_TOOL_COLOR", Color.rgb(143, 213, 253))
            widgetTextColor = preferences.getInt("WIDGET_TEXT_COLOR", Color.WHITE)
            widgetHighlightTextColor = preferences.getInt("WIDGET_HIGHLIGHT_TEXT_COLOR", Color.YELLOW)
            widgetNexradSize = preferences.getInt("WIDGET_NEXRAD_SIZE", 10)
            widgetCCShow7Day = preferences.getString("WIDGET_CC_DONOTSHOW_7_DAY", "true").startsWith("t")
            nwsIconTextColor = preferences.getInt("NWS_ICON_TEXT_COLOR", Color.rgb(38, 97, 139))
            nwsIconBottomColor = preferences.getInt("NWS_ICON_BOTTOM_COLOR", Color.rgb(255, 255, 255))
            nexradRadarBackgroundColor = preferences.getInt("NEXRAD_RADAR_BACKGROUND_COLOR", Color.rgb(0, 0, 0))
            wxoglSize = preferences.getInt("WXOGL_SIZE", 8)
            wxoglRememberLocation = preferences.getString("WXOGL_REMEMBER_LOCATION", "false").startsWith("t")
            wxoglLocationAutorefresh = preferences.getString("LOCATION_AUTOREFRESH", "false").startsWith("t")
            wxoglkeepscreenon = preferences.getString("KEEP_SCREEN_ON", "false").startsWith("t")
            wfoFav = preferences.getString("WFO_FAV", prefSeperator)
            ridFav = preferences.getString("RID_FAV", prefSeperator)
            sndFav = preferences.getString("SND_FAV", prefSeperator)
            srefFav = preferences.getString("SREF_FAV", prefSeperator)
            spcmesoFav = preferences.getString("SPCMESO_FAV", prefSeperator)
            spcmesoLabelFav = preferences.getString("SPCMESO_LABEL_FAV", prefSeperator)
            nwsTextFav = preferences.getString("NWS_TEXT_FAV", prefSeperator)
            notifSoundUri = preferences.getString("NOTIF_SOUND_URI", "")
            if (notifSoundUri == "") {
                notifSoundUri = Settings.System.DEFAULT_NOTIFICATION_URI.toString()
            }
            sn_key = preferences.getString("SN_KEY", "");
            sn_locationreport = preferences.getString("SN_LOCATIONREPORT", "").startsWith("t")
            spotterFav = preferences.getString("SPOTTER_FAV", "")
            homescreenFav = preferences.getString("HOMESCREEN_FAV", HOMESCREEN_FAV_DEFAULT)
            locDisplayImg = homescreenFav.contains("OGL-RADAR") || homescreenFav.contains("NXRD")
            alertNotificationSoundTornadoCurrent = preferences.getString("ALERT_NOTIFICATION_SOUND_TORNADO", "").startsWith("t")
            alertNotificationSoundSpcmcd = preferences.getString("ALERT_NOTIFICATION_SOUND_SPCMCD", "").startsWith("t")
            alertNotificationSoundWpcmpd = preferences.getString("ALERT_NOTIFICATION_SOUND_WPCMPD", "").startsWith("t")
            alertNotificationSoundNhcEpac = preferences.getString("ALERT_NOTIFICATION_SOUND_NHC_EPAC", "").startsWith("t")
            alertNotificationSoundNhcAtl = preferences.getString("ALERT_NOTIFICATION_SOUND_NHC_ATL", "").startsWith("t")
            alertNotificationSoundSpcwat = preferences.getString("ALERT_NOTIFICATION_SOUND_SPCWAT", "").startsWith("t")
            alertNotificationSoundSpcswo = preferences.getString("ALERT_NOTIFICATION_SOUND_SPCSWO", "").startsWith("t")
            alertNotificationSoundTextProd = preferences.getString("ALERT_NOTIFICATION_SOUND_TEXT_PROD", "").startsWith("t")
            notifSoundRepeat = preferences.getString("NOTIF_SOUND_REPEAT", "").startsWith("t")
            notifTts = preferences.getString("NOTIF_TTS", "").startsWith("t")
            alertBlackoutAmCurrent = preferences.getInt("ALERT_BLACKOUT_AM", -1)
            alertBlackoutPmCurrent = preferences.getInt("ALERT_BLACKOUT_PM", -1)
            alertTornadoNotificationCurrent = preferences.getString("ALERT_TORNADO_NOTIFICATION", "").startsWith("t")
            alertSpcmcdNotificationCurrent = preferences.getString("ALERT_SPCMCD_NOTIFICATION", "").startsWith("t")
            alertSpcwatNotificationCurrent = preferences.getString("ALERT_SPCWAT_NOTIFICATION", "").startsWith("t")
            alertSpcswoNotificationCurrent = preferences.getString("ALERT_SPCSWO_NOTIFICATION", "").startsWith("t")
            alertSpcswoSlightNotificationCurrent = preferences.getString("ALERT_SPCSWO_SLIGHT_NOTIFICATION", "").startsWith("t")
            alertWpcmpdNotificationCurrent = preferences.getString("ALERT_WPCMPD_NOTIFICATION", "").startsWith("t")
            alertBlackoutTornadoCurrent = preferences.getString("ALERT_BLACKOUT_TORNADO", "").startsWith("t")
            alertNhcEpacNotificationCurrent = preferences.getString("ALERT_NHC_EPAC_NOTIFICATION", "").startsWith("t")
            alertNhcAtlNotificationCurrent = preferences.getString("ALERT_NHC_ATL_NOTIFICATION", "").startsWith("t")
            alertAutocancel = preferences.getString("ALERT_AUTOCANCEL", "false").startsWith("t")
            alertBlackout = preferences.getString("ALERT_BLACKOUT", "").startsWith("t")
            playlistStr = preferences.getString("PLAYLIST", "")
            notifTextProdStr = preferences.getString(UtilityNotificationTextProduct.PREF_TOKEN, "")
            radarColorPalette94List = preferences.getString("RADAR_COLOR_PALETTE_94_LIST", "")
            radarColorPalette99List = preferences.getString("RADAR_COLOR_PALETTE_99_LIST", "")
            wxoglZoom = preferences.getFloat("WXOGL_ZOOM", wxoglSize.toFloat() / 10.0f)
            wxoglRid = preferences.getString("WXOGL_RID", "")
            wxoglProd = preferences.getString("WXOGL_PROD", "N0Q")
            wxoglX = preferences.getFloat("WXOGL_X", 0.0f)
            wxoglY = preferences.getFloat("WXOGL_Y", 0.0f)
            Location.currentLocationStr = preferences.getString("CURRENT_LOC_FRAGMENT", "1")
            severeDashboardTor.update(context)
            severeDashboardTst.update(context)
            severeDashboardFfw.update(context)
            severeDashboardSmw.update(context)
            severeDashboardSvs.update(context)
            severeDashboardSps.update(context)
            severeDashboardWat.update(context)
            severeDashboardMcd.update(context)
            severeDashboardMpd.update(context)
            watchLatlon.update(context)
            watchLatlonTor.update(context)
            mcdLatlon.update(context)
            mcdNoList.update(context)
            mpdLatlon.update(context)
            mpdNoList.update(context)
            wfoTextFav = preferences.getString("WFO_TEXT_FAV", "AFD")
            wpcTextFav = preferences.getString("WPC_TEXT_FAV", "pmdspd")
            spchrrrZoom = preferences.getFloat("SPCHRRR_ZOOM", 1.0f)
            spchrrrX = preferences.getFloat("SPCHRRR_X", 0.5f)
            spchrrrY = preferences.getFloat("SPCHRRR_Y", 0.5f)
            wpcgefsZoom = preferences.getFloat("WPCGEFS_ZOOM", 1.0f)
            wpcgefsX = preferences.getFloat("WPCGEFS_X", 0.5f)
            wpcgefsY = preferences.getFloat("WPCGEFS_Y", 0.5f)
            spcsseoZoom = preferences.getFloat("SPCSSEO_ZOOM", 1.0f)
            spcsseoX = preferences.getFloat("SPCSSEO_X", 0.5f)
            spcsseoY = preferences.getFloat("SPCSSEO_Y", 0.5f)
            goesVisZoom = preferences.getFloat("GOESVIS_ZOOM", 1.0f)
            goesVisX = preferences.getFloat("GOESVIS_X", 0.5f)
            goesVisY = preferences.getFloat("GOESVIS_Y", 0.5f)
            goesVisSector = preferences.getString("GOESVIS_SECTOR", "")
            elevationPref = preferences.getInt("ELEVATION_PREF", 0).toFloat()
            elevationPref = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, elevationPref, dm)
            cardElevation = elevationPref
            fabElevation = elevationPref
            fabElevationDepressed = elevationPref * 2
            tabHeaders[0] = preferences.getString("TAB1_HEADER", "LOCAL")
            tabHeaders[1] = preferences.getString("TAB2_HEADER", "SPC")
            tabHeaders[2] = preferences.getString("TAB3_HEADER", "MISC")
            tabHeaders[3] = preferences.getString("TAB4_HEADER", "IMAGES")
        }

        // FIXME move to Location
        //private val locations = mutableListOf<Location>()
        val locations: MutableList<Location> = mutableListOf()

        // source for county and state lines
        // http://www2.census.gov/geo/tiger/GENZ2010/
        // gz_2010_us_040_00_20m.kml
        //
        // cb_2014_us_county_20m.kml
        // https://www.census.gov/geo/maps-data/data/kml/kml_counties.html
        // ./android-sdk-linux/platform-tools/adb pull /data/data/joshuatee.joshuatee.modded.joshuatee.modded.wx/files/statev3.bin

        val colorMap: MutableMap<Int, ObjectColorPalette> = mutableMapOf()
        var loadedBuffers: Boolean = false

        fun initBuffers(context: Context) {
            UtilityLog.d(TAG, "initBuffers ran")
            loadedBuffers = true
            ColorPalettes.init(context)
            initRadarGeometryAll(context)
            GeographyType.refresh()
        }

        private const val caResid = R.raw.ca
        private const val mxResid = R.raw.mx
        private const val caCnt = 161792
        private const val mxCnt = 151552

        var stateRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
        var countState: Int = 200000 // v3 205748
        var hwRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
        var countHw: Int = 862208 // on disk size 3448832 yields  862208
        var hwExtRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
        const val countHwExt: Int = 770048 // on disk 3080192 yields 770048
        private const val hwExtFileResid = R.raw.hwv4ext // 2016_04_06
        var lakesRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
        const val countLakes: Int = 503808 // was 14336 + 489476
        var countyRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
        var countCounty: Int = 212992 // file on disk is 851968, should be
        private var hwFileResid = R.raw.hwv4
        private const val lakesFileResid = R.raw.lakesv3
        private var countyFileResid = R.raw.county

        var radarColorHw: Int = 0
        var radarColorHwExt: Int = 0
        var radarColorState: Int = 0
        var radarColorTstorm: Int = 0
        var radarColorTstormWatch: Int = 0
        var radarColorTor: Int = 0
        var radarColorTorWatch: Int = 0
        var radarColorFfw: Int = 0
        var radarColorSmw: Int = 0
        var radarColorSvs: Int = 0
        var radarColorSps: Int = 0
        var radarColorMcd: Int = 0
        var radarColorMpd: Int = 0
        var radarColorLocdot: Int = 0
        var radarColorSpotter: Int = 0
        var radarColorCity: Int = 0
        var radarColorLakes: Int = 0
        var radarColorCounty: Int = 0
        var radarColorSti: Int = 0
        var radarColorHi: Int = 0
        var radarColorObs: Int = 0
        var radarColorObsWindbarbs: Int = 0
        var radarColorCountyLabels: Int = 0

        private fun radarGeometrySetColors() {
            radarColorHw = preferences.getInt("RADAR_COLOR_HW", Color.BLUE)
            radarColorHwExt = preferences.getInt("RADAR_COLOR_HW_EXT", Color.BLUE)
            radarColorState = preferences.getInt("RADAR_COLOR_STATE", Color.WHITE)
            radarColorTstorm = preferences.getInt("RADAR_COLOR_TSTORM", Color.YELLOW)
            radarColorTstormWatch = preferences.getInt("RADAR_COLOR_TSTORM_WATCH", Color.BLUE)
            radarColorTor = preferences.getInt("RADAR_COLOR_TOR", Color.RED)
            radarColorTorWatch = preferences.getInt("RADAR_COLOR_TOR_WATCH", Color.RED)
            radarColorFfw = preferences.getInt("RADAR_COLOR_FFW", Color.GREEN)
            radarColorSmw = preferences.getInt("RADAR_COLOR_SMW", Color.CYAN)
            radarColorSvs = preferences.getInt("RADAR_COLOR_SVS", Color.rgb(255, 203, 103))
            radarColorSps = preferences.getInt("RADAR_COLOR_SPS", Color.rgb(255, 204, 102))
            radarColorMcd = preferences.getInt("RADAR_COLOR_MCD", Color.rgb(255, 255, 163))
            radarColorMpd = preferences.getInt("RADAR_COLOR_MPD", Color.rgb(0, 255, 0))
            radarColorLocdot = preferences.getInt("RADAR_COLOR_LOCDOT", Color.WHITE)
            radarColorSpotter = preferences.getInt("RADAR_COLOR_SPOTTER", Color.GREEN)
            radarColorCity = preferences.getInt("RADAR_COLOR_CITY", Color.WHITE)
            radarColorLakes = preferences.getInt("RADAR_COLOR_LAKES", Color.rgb(0, 0, 163))
            radarColorCounty = preferences.getInt("RADAR_COLOR_COUNTY", Color.rgb(75, 75, 75))
            radarColorSti = preferences.getInt("RADAR_COLOR_STI", Color.WHITE)
            radarColorHi = preferences.getInt("RADAR_COLOR_HI", Color.GREEN)
            radarColorObs = preferences.getInt("RADAR_COLOR_OBS", Color.WHITE)
            radarColorObsWindbarbs = preferences.getInt("RADAR_COLOR_OBS_WINDBARBS", Color.WHITE)
            radarColorCountyLabels = preferences.getInt("RADAR_COLOR_COUNTY_LABELS", Color.rgb(75, 75, 75))
        }

        private fun initRadarGeometryAll(context: Context) = GeographyType.values().forEach { initRadarGeometryByType(context, it) }

        fun initRadarGeometryByType(context: Context, type: GeographyType) {
            if (!radarHwEnh) {
                hwFileResid = R.raw.hw
                countHw = 112640
            }
            var stateLinesFileResid = R.raw.statev2
            countState = 205748
            if (radarStateHires) {
                stateLinesFileResid = R.raw.statev3
                countState = 1166552
            }
            if (radarCamxBorders) {
                countState += caCnt + mxCnt
            }
            if (radarCountyHires) {
                countyFileResid = R.raw.countyv2
                countCounty = 820852
            }
            val fileidArr = listOf(lakesFileResid, hwFileResid, countyFileResid, stateLinesFileResid, caResid, mxResid, hwExtFileResid)
            val countArr = listOf(countLakes, countHw, countCounty, countState, caCnt, mxCnt, countHwExt)
            val prefArr = listOf(true, true, true, true, radarCamxBorders, radarCamxBorders, radarHwEnhExt)
            when (type) {
                GeographyType.STATE_LINES -> {
                    stateRelativeBuffer = ByteBuffer.allocateDirect(4 * countState)
                    stateRelativeBuffer.order(ByteOrder.nativeOrder())
                    stateRelativeBuffer.position(0)
                    listOf(3, 4, 5).forEach { loadBuffer(context, fileidArr[it], stateRelativeBuffer, countArr[it], prefArr[it]) }
                }
                GeographyType.HIGHWAYS -> {
                    hwRelativeBuffer = ByteBuffer.allocateDirect(4 * countHw)
                    hwRelativeBuffer.order(ByteOrder.nativeOrder())
                    hwRelativeBuffer.position(0)
                    for (s in intArrayOf(1)) {
                        loadBuffer(context, fileidArr[s], hwRelativeBuffer, countArr[s], prefArr[s])
                    }
                }
                GeographyType.HIGHWAYS_EXTENDED -> {
                    if (radarHwEnhExt) {
                        hwExtRelativeBuffer = ByteBuffer.allocateDirect(4 * countHwExt)
                        hwExtRelativeBuffer.order(ByteOrder.nativeOrder())
                        hwExtRelativeBuffer.position(0)
                    }
                    for (s in intArrayOf(6)) {
                        loadBuffer(context, fileidArr[s], hwExtRelativeBuffer, countArr[s], prefArr[s])
                    }
                }
                GeographyType.LAKES -> {
                    lakesRelativeBuffer = ByteBuffer.allocateDirect(4 * countLakes)
                    lakesRelativeBuffer.order(ByteOrder.nativeOrder())
                    lakesRelativeBuffer.position(0)
                    val s = 0
                    loadBuffer(context, fileidArr[s], lakesRelativeBuffer, countArr[s], prefArr[s])
                }
                GeographyType.COUNTY_LINES -> {
                    countyRelativeBuffer = ByteBuffer.allocateDirect(4 * countCounty)
                    countyRelativeBuffer.order(ByteOrder.nativeOrder())
                    countyRelativeBuffer.position(0)
                    val s = 2
                    loadBuffer(context, fileidArr[s], countyRelativeBuffer, countArr[s], prefArr[s])
                }
                else -> {
                }
            }
        }

        private fun loadBuffer(context: Context, fileID: Int, bb: ByteBuffer, count: Int, pref: Boolean) {
            if (pref) {
                try {
                    val inputStream = context.resources.openRawResource(fileID)
                    val dis = DataInputStream(BufferedInputStream(inputStream))
                    (0 until count).forEach { _ -> bb.putFloat(dis.readFloat()) }
                    dis.close()
                    inputStream.close()
                } catch (e: IOException) {
                    UtilityLog.HandleException(e)
                }
            }
        }

        // Radar Preferences
        const val NWS_RADAR_PUB: String = "http://tgftp.nws.noaa.gov/" //(Official current URL, problem with cricket but so does cp.ncep now )
        //public static final String NWS_RADAR_PUB = "http://tgftp.cp.ncep.noaa.gov/";
        const val nwsRadarLevel2Pub: String = "http://nomads.ncep.noaa.gov/pub/data/nccf/radar/nexrad_level2/"
        //conus radar for zoom out
        const val NWS_CONUS_RADAR: String = "https://radar.weather.gov/ridge/Conus/RadarImg/latest_radaronly.gif";
        const val NWS_CONUS_RADAR_GFW: String = "https://radar.weather.gov/ridge/Conus/RadarImg/latest_radaronly.gfw"
        var radarTorWarnings: Boolean = true
        var radarTstWarnings: Boolean = true
        var radarFfwWarnings: Boolean = true
        var radarSmwWarnings: Boolean = true
        var radarSvsWarnings: Boolean = true
        var radarSpsWarnings: Boolean = false
        var locdotFollowsGps: Boolean = false
        var dualpaneshareposn: Boolean = false
        var radarSpotters: Boolean = false
        var radarSpottersLabel: Boolean = false
        var radarObs: Boolean = false
        var radarObsWindbarbs: Boolean = false
        var radarSwo: Boolean = false

        var radarCities: Boolean = false
        var radarHw: Boolean = false
        var radarLocDot: Boolean = false
        var radarLakes: Boolean = false
        var radarCounty: Boolean = false
        var radarCountyLabels: Boolean = false
        private var radarCountyHires: Boolean = false
        private var radarStateHires: Boolean = false
        var radarWatMcd: Boolean = false
        var radarMpd: Boolean = false
        var radarSti: Boolean = false
        var radarHi: Boolean = false
        var radarTvs: Boolean = false

        var radarShowLegend: Boolean = false
        var drawtoolSize: Int = 0
        var radarObsExtZoom: Int = 0
        var radarSpotterSize: Int = 0
        var radarAviationSize: Int = 0
        var radarTextSize: Float = 0f
        var radarLocdotSize: Int = 0
        var radarHiSize: Int = 0
        var radarTvsSize: Int = 0
        var radarWarnLinesize: Int = 0
        var radarWatmcdLinesize: Int = 0
        private var radarHwEnh: Boolean = true
        var radarHwEnhExt: Boolean = false
        private var radarCamxBorders: Boolean = false
        var radarIconsLevel2: Boolean = false

        private fun initRadarPreferences() {
            radarTorWarnings = preferences.getString("TOR_WARNINGS", "false").startsWith("t")
            radarTstWarnings = preferences.getString("TST_WARNINGS", "false").startsWith("t")
            radarFfwWarnings = preferences.getString("FFW_WARNINGS", "false").startsWith("t")
            radarSmwWarnings = preferences.getString("SMW_WARNINGS", "false").startsWith("t")
            radarSvsWarnings = preferences.getString("SVS_WARNINGS", "false").startsWith("t")
            radarSpsWarnings = preferences.getString("SPS_WARNINGS", "false").startsWith("t")
            locdotFollowsGps = preferences.getString("LOCDOT_FOLLOWS_GPS", "false").startsWith("t")
            dualpaneshareposn = preferences.getString("DUALPANE_SHARE_POSN", "true").startsWith("t")
            radarSpotters = preferences.getString("WXOGL_SPOTTERS", "false").startsWith("t")
            radarSpottersLabel = preferences.getString("WXOGL_SPOTTERS_LABEL", "false").startsWith("t")
            radarObs = preferences.getString("WXOGL_OBS", "false").startsWith("t")
            radarObsWindbarbs = preferences.getString("WXOGL_OBS_WINDBARBS", "false").startsWith("t")
            radarSwo = preferences.getString("RADAR_SHOW_SWO", "false").startsWith("t")
            radarCities = preferences.getString("COD_CITIES_DEFAULT", "").startsWith("t")
            radarHw = preferences.getString("COD_HW_DEFAULT", "true").startsWith("t")
            radarLocDot = preferences.getString("COD_LOCDOT_DEFAULT", "true").startsWith("t")
            radarLakes = preferences.getString("COD_LAKES_DEFAULT", "false").startsWith("t")
            radarCounty = preferences.getString("RADAR_SHOW_COUNTY", "true").startsWith("t")
            radarWatMcd = preferences.getString("RADAR_SHOW_WATCH", "false").startsWith("t")
            radarMpd = preferences.getString("RADAR_SHOW_MPD", "false").startsWith("t")
            radarSti = preferences.getString("RADAR_SHOW_STI", "false").startsWith("t")
            radarHi = preferences.getString("RADAR_SHOW_HI", "false").startsWith("t")
            radarTvs = preferences.getString("RADAR_SHOW_TVS", "false").startsWith("t")
            //radarHwEnh = preferences.getString("RADAR_HW_ENH", "false").startsWith("t")
            //radarHwEnh = preferences.getString("RADAR_HW_ENH", "true").startsWith("t")
            radarHwEnhExt = preferences.getString("RADAR_HW_ENH_EXT", "false").startsWith("t")
            radarCamxBorders = preferences.getString("RADAR_CAMX_BORDERS", "false").startsWith("t")
            radarCountyLabels = preferences.getString("RADAR_COUNTY_LABELS", "false").startsWith("t")
            radarCountyHires = preferences.getString("RADAR_COUNTY_HIRES", "false").startsWith("t")
            radarStateHires = preferences.getString("RADAR_STATE_HIRES", "false").startsWith("t")
            radarIconsLevel2 = preferences.getString("WXOGL_ICONS_LEVEL2", "false").startsWith("t")

            radarShowLegend = preferences.getString("RADAR_SHOW_LEGEND", "false").startsWith("t")
            drawtoolSize = preferences.getInt("DRAWTOOL_SIZE", 4)
            radarObsExtZoom = preferences.getInt("RADAR_OBS_EXT_ZOOM", 7)
            radarSpotterSize = preferences.getInt("RADAR_SPOTTER_SIZE", 4)
            radarAviationSize = preferences.getInt("RADAR_AVIATION_SIZE", 7)
            radarTextSize = preferences.getFloat("RADAR_TEXT_SIZE", 1.0f)
            radarLocdotSize = preferences.getInt("RADAR_LOCDOT_SIZE", 8)
            radarHiSize = preferences.getInt("RADAR_HI_SIZE", 8)
            radarTvsSize = preferences.getInt("RADAR_TVS_SIZE", 8)
            radarWarnLinesize = preferences.getInt("RADAR_WARN_LINESIZE", 2)
            radarWatmcdLinesize = preferences.getInt("RADAR_WATMCD_LINESIZE", 2)
        }


    } // end companion object
}

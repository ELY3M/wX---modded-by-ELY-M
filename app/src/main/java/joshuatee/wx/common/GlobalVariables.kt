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
//Modded by ELY M.  

package joshuatee.wx.common

import android.os.Environment
import joshuatee.wx.R
import java.util.regex.Pattern

object GlobalVariables {

    val newline: String = System.lineSeparator()

    const val PACKAGE_NAME = "elys.joshuatee.wx"
    const val EMAIL = "elymbmx@gmail.com"
    const val HTTP_USER_AGENT = "Android $PACKAGE_NAME $EMAIL"

    const val NWS_SPC_WEBSITE_PREFIX = "https://www.spc.noaa.gov"
    const val NWS_WPC_WEBSITE_PREFIX = "https://www.wpc.ncep.noaa.gov"
    const val NWS_AWC_WEBSITE_PREFIX = "https://www.aviationweather.gov"
    const val NWS_GRAPHICAL_WEBSITE_PREFIX = "https://graphical.weather.gov"
    const val NWS_CPC_NCEP_WEBSITE_PREFIX = "https://www.cpc.ncep.noaa.gov"
    const val NWS_OPC_WEBSITE_PREFIX = "https://ocean.weather.gov"
    const val NWS_NHC_WEBSITE_PREFIX = "https://www.nhc.noaa.gov"
    const val NWS_MAG_NCEP_WEBSITE_PREFIX = "https://mag.ncep.noaa.gov"
    const val GOES16_URL = "https://cdn.star.nesdis.noaa.gov"
    const val GOES16_ANIM_URL = "https://www.star.nesdis.noaa.gov"
    const val NWS_API_URL = "https://api.weather.gov"
    const val NWS_SWPC_WEBSITE_PREFIX = "https://services.swpc.noaa.gov"
    const val TGFTP_WEBSITE_PREFIX = "https://tgftp.nws.noaa.gov"
    const val SEP = "ABC123"
    const val PRE2_PATTERN = "<pre>(.*?)</pre>"

    val ICON_ALERT_2 = R.drawable.ic_report_24dp
    val ICON_MAP = R.drawable.ic_public_24dp_white
    val ICON_MPD = R.drawable.ic_brightness_7_24dp
    val ICON_TORNADO = R.drawable.ic_flash_off_24dp
    val ICON_MCD = R.drawable.ic_directions_24dp
    val ICON_ACTION = R.drawable.ic_play_arrow_24dp
    val ICON_ALERT = R.drawable.ic_warning_24dp
    val ICON_RADAR = R.drawable.ic_flash_on_24dp
    val ICON_RADAR_WHITE = R.drawable.ic_flash_on_24dp_white
    val ICON_FORECAST = R.drawable.ic_place_24dp
    val ICON_CURRENT = R.drawable.ic_info_outline_24dp
    val ICON_CURRENT_WHITE = R.drawable.ic_info_outline_24dp_white
    val ICON_NHC_1 = R.drawable.ic_brightness_auto_24dp
    val ICON_DELETE_WHITE = R.drawable.ic_delete_24dp_white
    val STAR_ICON = R.drawable.ic_star_24dp
    val STAR_OUTLINE_ICON = R.drawable.ic_star_outline_24dp
    val STAR_ICON_WHITE = R.drawable.ic_star_24dp_white
    val STAR_OUTLINE_ICON_WHITE = R.drawable.ic_star_outline_24dp_white
    val ICON_PLAY = R.drawable.ic_play_arrow_24dp
    val ICON_PLAY_WHITE = R.drawable.ic_play_arrow_24dp_white
    val ICON_STOP = R.drawable.ic_stop_24dp
    val ICON_STOP_WHITE = R.drawable.ic_stop_24dp_white
    val ICON_PAUSE = R.drawable.ic_pause_24dp
    val ICON_PAUSE_WHITE = R.drawable.ic_pause_24dp_white
    val ICON_PAUSE_PRESSED = R.drawable.ic_pause_white_24dp
    val ICON_PAUSE_PRESSED_BLUE = R.drawable.ic_pause_blue_24dp
    val ICON_ADD = R.drawable.ic_add_box_24dp
    val ICON_ADD2 = R.drawable.ic_add2_box_24dp_white
    val ICON_DONE = R.drawable.ic_done_24dp
    val ICON_PLAYLIST = R.drawable.ic_playlist_play_24dp
    val ICON_REORDER = R.drawable.ic_reorder_24dp

    const val DEGREE_SYMBOL = "\u00B0"
    const val PREFERENCE_SEPARATOR = " : : :"
    const val WIDGET_FILE_BAK = "BAK"
    const val PREFERENCES_HELP_TITLE = "Please tap on text for additional help."

    //
    // Legacy forecast support
    //
    val utilUSWeatherSummaryPattern: Pattern = Pattern.compile(".*?weather-summary=(.*?)/>.*?")
    val utilUSPeriodNamePattern: Pattern = Pattern.compile(".*?period-name=(.*?)>.*?")
    val xmlValuePattern: Pattern = Pattern.compile("<value>")
    
    
    
    //elys mod
    val FilesPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wX/"
    val PalFilesPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wX/pal/"
    val BackupFilesPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wXBackup/"
    val BackupPalFilesPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wXBackup/pal/"
	//end of elys mod
    
    
    
    
}

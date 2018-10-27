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

package joshuatee.wx.util

import android.content.Context
import joshuatee.wx.MyApplication

object UtilityDownloadRadar {

    // FIXME make URLs global static

    fun getPolygonVTEC(context: Context) {
        MyApplication.severeDashboardTor.valueSet(context, UtilityDownloadNWS.getNWSStringFromURL("https://api.weather.gov/alerts/active?event=Tornado%20Warning"))
        MyApplication.severeDashboardSvr.valueSet(context, UtilityDownloadNWS.getNWSStringFromURL("https://api.weather.gov/alerts/active?event=Severe%20Thunderstorm%20Warning"))
        MyApplication.severeDashboardEww.valueSet(context, UtilityDownloadNWS.getNWSStringFromURL("https://api.weather.gov/alerts/active?event=Extreme%20Wind%20Warning"))
        MyApplication.severeDashboardFfw.valueSet(context, UtilityDownloadNWS.getNWSStringFromURL("https://api.weather.gov/alerts/active?event=Flash%20Flood%20Warning"))
        MyApplication.severeDashboardSmw.valueSet(context, UtilityDownloadNWS.getNWSStringFromURL("https://api.weather.gov/alerts/active?event=Special%20Marine%20Warning"))
        MyApplication.severeDashboardSvs.valueSet(context, UtilityDownloadNWS.getNWSStringFromURL("https://api.weather.gov/alerts/active?event=Severe%20Weather%20Statement"))
        MyApplication.severeDashboardSps.valueSet(context, UtilityDownloadNWS.getNWSStringFromURL("https://api.weather.gov/alerts/active?event=Special%20Weather%20Statement"))

    }
}

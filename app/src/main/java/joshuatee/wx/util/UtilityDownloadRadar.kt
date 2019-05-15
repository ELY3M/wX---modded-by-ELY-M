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

import android.content.Context
import joshuatee.wx.Extensions.getHtml
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.PolygonWarningType

object UtilityDownloadRadar {

    private const val baseUrl = "https://api.weather.gov/alerts/active?event="

    private const val torUrl = baseUrl + "Tornado%20Warning"
    private const val svrURl = baseUrl + "Severe%20Thunderstorm%20Warning"
    private const val ffwUrl = baseUrl + "Flash%20Flood%20Warning"
    private const val ewwUrl = baseUrl + "Extreme%20Wind%20Warning"
    private const val smwUrl = baseUrl + "Special%20Marine%20Warning"
    private const val svsUrl = baseUrl + "Severe%20Weather%20Statement"
    private const val spsUrl = baseUrl + "Special%20Weather%20Statement"
    //private const val spsUrl = "http://192.168.1.113/nws/SPS-12-31-18-5.09pm.txt" //for testing
    // Below is for testing
    //val ffwUrl = baseUrl + "Flood%20Warning"


    fun getPolygonVTEC(context: Context) {
        MyApplication.severeDashboardTor.valueSet(
                context,
                UtilityDownloadNws.getNwsStringFromUrlNoAcceptHeader(torUrl)
        )
        MyApplication.severeDashboardSvr.valueSet(
                context,
                UtilityDownloadNws.getNwsStringFromUrlNoAcceptHeader(svrURl)
        )
        MyApplication.severeDashboardFfw.valueSet(
                context,
                UtilityDownloadNws.getNwsStringFromUrlNoAcceptHeader(ffwUrl)
        )
        MyApplication.severeDashboardEww.valueSet(
                context,
                UtilityDownloadNws.getNwsStringFromUrlNoAcceptHeader(ewwUrl)
        )
        MyApplication.severeDashboardSmw.valueSet(
                context,
                UtilityDownloadNws.getNwsStringFromUrlNoAcceptHeader(smwUrl)
        )
        MyApplication.severeDashboardSvs.valueSet(
                context,
                UtilityDownloadNws.getNwsStringFromUrlNoAcceptHeader(svsUrl)
        )
        MyApplication.severeDashboardSps.valueSet(
                context,
                UtilityDownloadNws.getNwsStringFromUrlNoAcceptHeader(spsUrl)
        )

    }
    
    fun getVtecTor(): String {
        return UtilityDownloadNws.getNwsStringFromUrlNoAcceptHeader(torUrl)
    }
    fun getVtecSvr(): String {
        return UtilityDownloadNws.getNwsStringFromUrlNoAcceptHeader(svrURl)
    }
    fun getVtecFfw(): String {
        return UtilityDownloadNws.getNwsStringFromUrlNoAcceptHeader(ffwUrl)
    }
    fun getVtecEww(): String {
        return UtilityDownloadNws.getNwsStringFromUrlNoAcceptHeader(ewwUrl)
    }
    fun getVtecSmw(): String {
        return UtilityDownloadNws.getNwsStringFromUrlNoAcceptHeader(smwUrl)
    }
    fun getVtecSvs(): String {
        return UtilityDownloadNws.getNwsStringFromUrlNoAcceptHeader(svsUrl)
    }
    fun getSps(): String {
        return UtilityDownloadNws.getNwsStringFromUrlNoAcceptHeader(spsUrl)
    }
    
    fun getVtecByType(type: PolygonWarningType): String {
        return UtilityDownloadNws.getNwsStringFromUrlNoAcceptHeader(baseUrl + type.urlToken)
    }

    fun getMcd(): String {
        return "${MyApplication.nwsSPCwebsitePrefix}/products/md/".getHtml()
    }

    fun getMpd(): String {
        return "${MyApplication.nwsWPCwebsitePrefix}/metwatch/metwatch_mpd.php".getHtml()
    }

    fun getWatch(): String {
        return "${MyApplication.nwsSPCwebsitePrefix}/products/watch/".getHtml()
    }
}

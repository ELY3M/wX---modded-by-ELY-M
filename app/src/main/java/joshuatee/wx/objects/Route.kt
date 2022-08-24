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

package joshuatee.wx.objects

import android.content.Context
import android.content.Intent
import android.net.Uri
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.activitiesmisc.*
import joshuatee.wx.audio.SettingsPlaylistActivity
import joshuatee.wx.canada.CanadaAlertsActivity
import joshuatee.wx.canada.CanadaHourlyActivity
import joshuatee.wx.canada.CanadaTextActivity
import joshuatee.wx.models.ModelsGenericActivity
import joshuatee.wx.models.ModelsSpcHrefActivity
import joshuatee.wx.models.ModelsSpcHrrrActivity
import joshuatee.wx.models.ModelsSpcSrefActivity
import joshuatee.wx.nhc.NhcActivity
import joshuatee.wx.nhc.NhcStormActivity
import joshuatee.wx.nhc.ObjectNhcStormDetails
import joshuatee.wx.radar.*
import joshuatee.wx.settings.*
import joshuatee.wx.spc.*
import joshuatee.wx.util.Utility
import joshuatee.wx.vis.GoesActivity
import joshuatee.wx.wpc.WpcImagesActivity
import joshuatee.wx.wpc.WpcRainfallForecastActivity
import joshuatee.wx.wpc.WpcRainfallForecastSummaryActivity
import joshuatee.wx.wpc.WpcTextProductsActivity

//
// Used to start another activity
//

class Route() {

    constructor(context: Context, clazz: Class<*>, url: String, stringArray: Array<String>) : this() {
        val intent = Intent(context, clazz)
        intent.putExtra(url, stringArray)
        context.startActivity(intent)
    }

    constructor(context: Context, clazz: Class<*>, url: String, string: String) : this() {
        val intent = Intent(context, clazz)
        intent.putExtra(url, string)
        context.startActivity(intent)
    }

    constructor(context: Context, standardAction: String, url: Uri) : this() {
        val intent = Intent(standardAction, url)
        context.startActivity(intent)
    }

    constructor(context: Context, clazz: Class<*>) : this() {
        val intent = Intent(context, clazz)
        context.startActivity(intent)
    }

    companion object {

        // TODO FIXME alphabetize

        fun alerts(context: Context) {
            if (Location.isUS) {
                usAlerts(context)
            } else {
                Route(context, CanadaAlertsActivity::class.java)
            }
        }

        fun nhcStorm(context: Context, stormData: ObjectNhcStormDetails) {
            val intent = Intent(context, NhcStormActivity::class.java)
            intent.putExtra(NhcStormActivity.URL, stormData)
            context.startActivity(intent)
        }

        fun mcd(context: Context, array: Array<String>) {
            Route(context, SpcMcdWatchShowActivity::class.java, SpcMcdWatchShowActivity.NUMBER, array)
        }

        fun sounding(context: Context) {
            Route(context, SpcSoundingsActivity::class.java, SpcSoundingsActivity.URL, arrayOf(Location.wfo, ""))
        }

        fun wfoText(context: Context) {
            if (Location.isUS) {
                Route(context, WfoTextActivity::class.java, WfoTextActivity.URL, arrayOf(Location.wfo, ""))
            } else {
                Route(context, CanadaTextActivity::class.java)
            }
        }

        fun wfoText(context: Context, array: Array<String>) {
            Route(context, WfoTextActivity::class.java, WfoTextActivity.URL, array)
        }

        fun hourly(context: Context) {
            if (Location.isUS) {
                Route(context, HourlyActivity::class.java, HourlyActivity.LOC_NUM, Location.currentLocationStr)
            } else {
                Route(context, CanadaHourlyActivity::class.java, CanadaHourlyActivity.LOC_NUM, Location.currentLocationStr)
            }
        }

        fun hazard(context: Context, array: Array<String>) {
            Route(context, USAlertsDetailActivity::class.java, USAlertsDetailActivity.URL, array)
        }

        fun goesFd(context: Context) {
            Route(context, ImageCollectionActivity::class.java, ImageCollectionActivity.TYPE, arrayOf("GOESFD"))
        }

        fun forecast(context: Context, array: Array<String>) {
            Route(context, ForecastActivity::class.java, ForecastActivity.URL, array)
        }

        fun lightning(context: Context) {
            if (UIPreferences.lightningUseGoes) {
                Route(context, GoesActivity::class.java, GoesActivity.RID, arrayOf("CONUS", "23"))
            } else {
                Route(context, LightningActivity::class.java)
            }
        }

        fun opc(context: Context) {
            Route(context, ImageCollectionActivity::class.java, ImageCollectionActivity.TYPE, arrayOf("OPC"))
        }

        fun vis(context: Context) {
            Route(context, GoesActivity::class.java, GoesActivity.RID, arrayOf(""))
        }

        fun visNhc(context: Context, url: String) {
            Route(context, GoesActivity::class.java, GoesActivity.RID, arrayOf(url, ""))
        }

        fun visWv(context: Context) {
            Route(context, GoesActivity::class.java, GoesActivity.RID, arrayOf("CONUS", "09"))
        }

        fun usAlerts(context: Context) {
            Route(
                    context,
                    USWarningsWithRadarActivity::class.java,
                    USWarningsWithRadarActivity.URL,
                    arrayOf(".*?Tornado Warning.*?|.*?Severe Thunderstorm Warning.*?|.*?Flash Flood Warning.*?", "us")
            )
        }

        fun radarMosaic(context: Context) {
            if (UIPreferences.useAwcMosaic) {
                Route(context, AwcRadarMosaicActivity::class.java, AwcRadarMosaicActivity.URL, arrayOf(""))
            } else {
                Route(context, RadarMosaicNwsActivity::class.java, RadarMosaicNwsActivity.URL, arrayOf(""))
            }
//            ObjectIntent(context, RadarMosaicNwsActivity::class.java, RadarMosaicNwsActivity.URL, arrayOf("CONUS"))
        }

        fun spcCompmap(context: Context) {
            Route(context, SpcCompmapActivity::class.java)
        }

        fun spcFireOutlookSummary(context: Context) {
            Route(context, SpcFireOutlookSummaryActivity::class.java)
        }

        fun spcFireOutlookByDay(context: Context, index: Int) {
            Route(context, SpcFireOutlookActivity::class.java, SpcFireOutlookActivity.NUMBER, arrayOf(index.toString()))
        }

        fun spcHref(context: Context) {
            Route(context, ModelsSpcHrefActivity::class.java, "", arrayOf("1", "SPCHREF", "SPC HREF"))
        }

        fun spcHrrr(context: Context) {
            Route(context, ModelsSpcHrrrActivity::class.java, "", arrayOf("1", "SPCHRRR", "SPC HRRR"))
        }

        fun spcMeso(context: Context) {
            Route(context, SpcMesoActivity::class.java, SpcMesoActivity.INFO, arrayOf("", "1", "SPCMESO"))
        }

        fun spcSref(context: Context) {
            Route(context, ModelsSpcSrefActivity::class.java, ModelsSpcSrefActivity.INFO, arrayOf("1", "SPCSREF", "SPCSREF"))
        }

        fun spcStormReports(context: Context) {
            Route(context, SpcStormReportsActivity::class.java, SpcStormReportsActivity.DAY, arrayOf("today"))
        }

        fun spcSwo(context: Context, array: Array<String>) {
            Route(context, SpcSwoActivity::class.java, SpcSwoActivity.NUMBER, array)
        }

        fun spcSwoDay1(context: Context) {
            spcSwo(context, arrayOf("1", ""))
        }

        fun spcSwoDay2(context: Context) {
            spcSwo(context, arrayOf("2", ""))
        }

        fun spcSwoDay3(context: Context) {
            spcSwo(context, arrayOf("3", ""))
        }

        fun spcSwoDay48(context: Context) {
            spcSwo(context, arrayOf("4-8", ""))
        }

        fun spcSwoSummary(context: Context) {
            Route(context, SpcSwoSummaryActivity::class.java )
        }

        fun spcTstorm(context: Context) {
            Route(context, SpcThunderStormOutlookActivity::class.java)
        }

        fun model(context: Context, array: Array<String>) {
            Route(context, ModelsGenericActivity::class.java, ModelsGenericActivity.INFO, array)
        }

        fun modelEsrl(context: Context) {
            model(context, arrayOf("1", "ESRL", "ESRL"))
        }

        fun modelNcep(context: Context) {
            model(context, arrayOf("1", "NCEP", "NCEP"))
        }

        fun modelNsslWrf(context: Context) {
            model(context, arrayOf("1", "NSSL", "NSSL"))
        }

        fun nhc(context: Context) {
            Route(context, NhcActivity::class.java)
        }

        fun observations(context: Context) {
            if (Location.isUS) {
                Route(context, ImageCollectionActivity::class.java, ImageCollectionActivity.TYPE, arrayOf("OBSERVATIONS"))
            } else {
                image(context, arrayOf("http://weather.gc.ca/data/wxoimages/wocanmap0_e.jpg", "Observations"))
            }
        }

        fun obsSites(context: Context) {
            Route(context, NwsObsSitesActivity::class.java)
        }

        fun settings(context: Context) {
            Route(context, SettingsMainActivity::class.java)
        }

        fun settingsRadar(context: Context) {
            Route(context, SettingsRadarActivity::class.java)
        }

        fun radar(context: Context, array: Array<String>) {
            Route(context, WXGLRadarActivity::class.java, WXGLRadarActivity.RID, array)
        }

        fun radarMainScreen(context: Context) {
            if (!UIPreferences.dualpaneRadarIcon) {
                radar(context, arrayOf(Location.rid, ""))
            } else {
                radarMultiPane(context, arrayOf(Location.rid, "", "2"))
            }
        }

//        fun radarNew(context: Context, array: Array<String>) {
//            Route(context, WXGLRadarActivityNew::class.java, WXGLRadarActivityNew.RID, array)
//        }

        fun radarBySite(context: Context, radarSite: String) {
            val radarLabel = Utility.getRadarSiteName(radarSite)
            val state = radarLabel.split(",")[0]
            radar(context, arrayOf(radarSite, state, "N0Q", ""))
        }

        fun radarMultiPane(context: Context, array: Array<String>) {
            Route(context, WXGLRadarActivityMultiPane::class.java, WXGLRadarActivityMultiPane.RID, array)
        }

        fun radarMultiPane2(context: Context) {
            radarMultiPane(context, arrayOf(Location.rid, "", "2"))
        }

        fun radarMultiPane4(context: Context) {
            radarMultiPane(context, arrayOf(Location.rid, "", "4"))
        }

        fun image(context: Context, array: Array<String>) {
            Route(context, ImageShowActivity::class.java, ImageShowActivity.URL, array)
        }

        fun favoriteAdd(context: Context, array: Array<String>) {
            Route(context, FavAddActivity::class.java, FavAddActivity.TYPE, array)
        }

        fun favoriteRemove(context: Context, array: Array<String>) {
            Route(context, FavRemoveActivity::class.java, FavRemoveActivity.TYPE, array)
        }

        fun text(context: Context, array: Array<String>) {
            Route(context, TextScreenActivity::class.java, TextScreenActivity.URL, array)
        }

        fun locationEdit(context: Context, array: Array<String>) {
            Route(context, SettingsLocationGenericActivity::class.java, SettingsLocationGenericActivity.LOC_NUM, array)
        }

        fun playlist(context: Context) {
            Route(context, SettingsPlaylistActivity::class.java)
        }

        fun severeDash(context: Context) {
            Route(context, SevereDashboardActivity::class.java)
        }

        fun severeDashMainScreen(context: Context) {
            if (Location.isUS) {
                severeDash(context)
            } else {
                Route(context, CanadaAlertsActivity::class.java)
            }
        }

        fun spotters(context: Context) {
            Route(context, SpottersActivity::class.java)
        }

        fun web(context: Context, url: String) {
            Route(context, Intent.ACTION_VIEW, Uri.parse(url))
        }

        fun webView(context: Context, array: Array<String>) {
            Route(context, WebView::class.java, WebView.URL, array)
        }

        fun webViewTwitterStates(context: Context) {
            Route(context, WebViewTwitter::class.java)
        }

        fun webViewTwitterTornado(context: Context) {
            webView(context, arrayOf("https://mobile.twitter.com/hashtag/tornado", "#tornado"))
        }

        fun wpcGefs(context: Context) {
            model(context, arrayOf("1", "WPCGEFS", "WPC"))
        }

        fun wpcImages(context: Context) {
            Route(context, WpcImagesActivity::class.java, "", arrayOf())
        }

        fun wpcRainfallSummary(context: Context) {
            Route(context, WpcRainfallForecastSummaryActivity::class.java)
        }

        fun wpcRainfallByDay(context: Context, dayIndex: String) {
            Route(context, WpcRainfallForecastActivity::class.java, WpcRainfallForecastActivity.NUMBER, arrayOf(dayIndex))
        }

        fun wpcText(context: Context) {
            Route(context, WpcTextProductsActivity::class.java, WpcTextProductsActivity.URL, arrayOf("pmdspd", "Short Range Forecast Discussion"))
        }

        fun wpcText(context: Context, array: Array<String>) {
            Route(context, WpcTextProductsActivity::class.java, WpcTextProductsActivity.URL, array)
        }
    }
}

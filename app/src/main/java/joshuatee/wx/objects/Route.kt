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

package joshuatee.wx.objects

import android.content.Context
import android.content.Intent
import android.net.Uri
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.misc.*
import joshuatee.wx.audio.SettingsPlaylistActivity
import joshuatee.wx.models.ModelsGenericActivity
import joshuatee.wx.models.ModelsSpcHrefActivity
import joshuatee.wx.nhc.NhcActivity
import joshuatee.wx.nhc.NhcStormActivity
import joshuatee.wx.nhc.NhcStormDetails
import joshuatee.wx.radar.RadarMosaicActivity
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.radar.WXGLRadarActivityMultiPane
import joshuatee.wx.settings.FavAddActivity
import joshuatee.wx.settings.FavRemoveActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.SettingsColorPickerActivity
import joshuatee.wx.settings.SettingsLocationGenericActivity
import joshuatee.wx.settings.SettingsMainActivity
import joshuatee.wx.settings.SettingsRadarActivity
import joshuatee.wx.space.UtilityAurora
import joshuatee.wx.spc.LsrByWfoActivity
import joshuatee.wx.spc.SpcCompmapActivity
import joshuatee.wx.spc.SpcFireOutlookActivity
import joshuatee.wx.spc.SpcFireOutlookSummaryActivity
import joshuatee.wx.spc.SpcMcdWatchShowActivity
import joshuatee.wx.spc.SpcMesoActivity
import joshuatee.wx.spc.SpcSoundingsActivity
import joshuatee.wx.spc.SpcStormReportsActivity
import joshuatee.wx.spc.SpcThunderStormOutlookActivity
import joshuatee.wx.spc.SpcSwoActivity
import joshuatee.wx.spc.SpcSwoSummaryActivity
import joshuatee.wx.vis.GoesActivity
import joshuatee.wx.wpc.NationalImagesActivity
import joshuatee.wx.wpc.RainfallOutlookActivity
import joshuatee.wx.wpc.RainfallOutlookSummaryActivity
import joshuatee.wx.wpc.NationalTextActivity
import androidx.core.net.toUri

//
// Used to start another activity
//

class Route() {

    constructor(
        context: Context,
        clazz: Class<*>,
        url: String,
        stringArray: Array<String>
    ) : this() {
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
            Route(
                context,
                USAlertsActivity::class.java,
                USAlertsActivity.URL,
                arrayOf(
                    ".*?Tornado Warning.*?|.*?Severe Thunderstorm Warning.*?|.*?Flash Flood Warning.*?",
                    "us"
                )
            )
        }

        fun colorPicker(context: Context, pref: String, label: String) {
            Route(
                context,
                SettingsColorPickerActivity::class.java,
                SettingsColorPickerActivity.INFO,
                arrayOf(pref, label)
            )
        }

        fun favoriteAdd(context: Context, type: FavoriteType) {
            Route(context, FavAddActivity::class.java, FavAddActivity.TYPE, arrayOf(type.name))
        }

        fun favoriteRemove(context: Context, type: FavoriteType) {
            Route(
                context,
                FavRemoveActivity::class.java,
                FavRemoveActivity.TYPE,
                arrayOf(type.name)
            )
        }

        fun forecast(context: Context, latLon: LatLon) {
            Route(
                context,
                ForecastActivity::class.java,
                ForecastActivity.URL,
                arrayOf(latLon.latString, latLon.lonString)
            )
        }

        fun goesFd(context: Context) {
            Route(
                context,
                ImageCollectionActivity::class.java,
                ImageCollectionActivity.TYPE,
                arrayOf("GOESFD")
            )
        }

        fun hazard(context: Context, url: String) {
            Route(context, AlertsDetailActivity::class.java, AlertsDetailActivity.URL, arrayOf(url))
        }

        fun hourly(context: Context) {
            if (Location.isUS) {
                Route(
                    context,
                    HourlyActivity::class.java,
                    HourlyActivity.LOC_NUM,
                    Location.currentLocationStr
                )
            }
        }

        fun image(context: Context, url: String, title: String) {
            Route(
                context,
                ImageShowActivity::class.java,
                ImageShowActivity.URL,
                arrayOf(url, title)
            )
        }

        fun lightning(context: Context) {
            Route(context, GoesActivity::class.java, GoesActivity.RID, arrayOf("CONUS", "23"))
        }

        fun locationEdit(context: Context, locationNumber: String) {
            Route(
                context,
                SettingsLocationGenericActivity::class.java,
                SettingsLocationGenericActivity.LOC_NUM,
                arrayOf(locationNumber)
            )
        }

        fun lsrByWfo(context: Context) {
            Route(
                context,
                LsrByWfoActivity::class.java,
                LsrByWfoActivity.URL,
                arrayOf(Location.wfo, "LSR")
            )
        }

        fun mcd(context: Context, number: String, type: String) {
            Route(
                context,
                SpcMcdWatchShowActivity::class.java,
                SpcMcdWatchShowActivity.NUMBER,
                arrayOf(number, type)
            )
        }

        fun model(context: Context, numberPanes: String, prefToken: String, title: String) {
            Route(
                context,
                ModelsGenericActivity::class.java,
                ModelsGenericActivity.INFO,
                arrayOf(numberPanes, prefToken, title)
            )
        }

        fun modelEsrl(context: Context) {
            model(context, "1", "ESRL", "ESRL")
        }

        fun modelNcep(context: Context) {
            model(context, "1", "NCEP", "NCEP")
        }

        fun modelNsslWrf(context: Context) {
            model(context, "1", "NSSL", "NSSL")
        }

        fun nhc(context: Context) {
            Route(context, NhcActivity::class.java)
        }

        fun nhcStorm(context: Context, stormData: NhcStormDetails) {
            val intent = Intent(context, NhcStormActivity::class.java)
            intent.putExtra(NhcStormActivity.URL, stormData)
            context.startActivity(intent)
        }

        fun observations(context: Context) {
            Route(
                context,
                ImageCollectionActivity::class.java,
                ImageCollectionActivity.TYPE,
                arrayOf("OBSERVATIONS")
            )
        }

        fun obsSites(context: Context) {
            Route(context, NwsObsSitesActivity::class.java)
        }

        fun opc(context: Context) {
            Route(
                context,
                ImageCollectionActivity::class.java,
                ImageCollectionActivity.TYPE,
                arrayOf("OPC")
            )
        }

        fun playlist(context: Context) {
            Route(context, SettingsPlaylistActivity::class.java)
        }

        fun radar(context: Context, array: Array<String>) {
            Route(context, WXGLRadarActivity::class.java, WXGLRadarActivity.RID, array)
        }

        fun radarMainScreen(context: Context) {
            if (!UIPreferences.dualpaneRadarIcon) {
                radar(context, arrayOf(Location.rid, ""))
            } else {
                radarMultiPane(context, arrayOf(Location.rid, "STATE NOT USED", "2"))
            }
        }

        fun radarBySite(context: Context, radarSite: String) {
//            val radarLabel = UtilityLocation.getRadarSiteName(radarSite)
            radar(context, arrayOf(radarSite, "STATE NOT USED", "N0Q", ""))
        }

        fun radarWithOneSpotter(context: Context, radarSite: String, spotter: String) {
            radar(context, arrayOf(radarSite, "STATE NOT USED", "N0Q", "", spotter))
        }

        fun radarMultiPane(context: Context, array: Array<String>) {
            Route(
                context,
                WXGLRadarActivityMultiPane::class.java,
                WXGLRadarActivityMultiPane.RID,
                array
            )
        }

        fun radarMultiPane2(context: Context) {
            radarMultiPane(context, arrayOf(Location.rid, "STATE NOT USED", "2"))
        }

        fun radarMultiPane4(context: Context) {
            radarMultiPane(context, arrayOf(Location.rid, "STATE NOT USED", "4"))
        }

        fun radarMosaic(context: Context) {
            Route(
                context,
                RadarMosaicActivity::class.java,
                RadarMosaicActivity.URL,
                arrayOf("")
            )
        }

        fun rtma(context: Context) {
            Route(context, RtmaActivity::class.java, RtmaActivity.RID, arrayOf("2m_temp"))
        }

        fun settings(context: Context) {
            Route(context, SettingsMainActivity::class.java)
        }

        fun settingsRadar(context: Context) {
            Route(context, SettingsRadarActivity::class.java)
        }

        fun severeDashboard(context: Context) {
            Route(context, SevereDashboardActivity::class.java)
        }

        // TODO FIXME 1st arg seems unused, changed 2nd arg from "" to site
        fun sounding(context: Context, site: String = "") {
            Route(
                context,
                SpcSoundingsActivity::class.java,
                SpcSoundingsActivity.URL,
                arrayOf(Location.wfo, site)
            )
        }

        fun spcCompmap(context: Context) {
            Route(context, SpcCompmapActivity::class.java)
        }

        fun spcFireOutlookSummary(context: Context) {
            Route(context, SpcFireOutlookSummaryActivity::class.java)
        }

        fun spcFireOutlookByDay(context: Context, index: Int) {
            Route(
                context,
                SpcFireOutlookActivity::class.java,
                SpcFireOutlookActivity.NUMBER,
                arrayOf(index.toString())
            )
        }

        fun spcHref(context: Context) {
            Route(
                context,
                ModelsSpcHrefActivity::class.java,
                "",
                arrayOf("1", "SPCHREF", "SPC HREF")
            )
        }

        fun spcHrefDualPane(context: Context) {
            Route(
                context,
                ModelsSpcHrefActivity::class.java,
                "",
                arrayOf("2", "SPCHREF", "SPC HREF")
            )
        }

        fun spcHrrr(context: Context) {
            model(context, "1", "SPCHRRR", "SPC HRRR")
        }

        fun spcMeso(context: Context) {
            Route(
                context,
                SpcMesoActivity::class.java,
                SpcMesoActivity.INFO,
                arrayOf("", "1", "SPCMESO")
            )
        }

        fun spcMesoDualPane(context: Context) {
            Route(
                context,
                SpcMesoActivity::class.java,
                SpcMesoActivity.INFO,
                arrayOf("", "2", "SPCMESO")
            )
        }

        fun spcMesoBySector(context: Context, sector: String) {
            Route(
                context,
                SpcMesoActivity::class.java,
                SpcMesoActivity.INFO,
                arrayOf("", "1", "SPCMESO", sector)
            )
        }

        fun spcSref(context: Context) {
            Route(
                context,
                ModelsSpcHrefActivity::class.java,
                ModelsSpcHrefActivity.INFO,
                arrayOf("1", "SPCSREF", "SPCSREF")
            )
        }

        fun spcSrefDualPane(context: Context) {
            Route(
                context,
                ModelsSpcHrefActivity::class.java,
                ModelsSpcHrefActivity.INFO,
                arrayOf("2", "SPCSREF", "SPCSREF")
            )
        }

        fun spcStormReports(context: Context) {
            Route(
                context,
                SpcStormReportsActivity::class.java,
                SpcStormReportsActivity.DAY,
                arrayOf("today")
            )
        }

        fun spcSwo(context: Context, day: String, sound: String = "") {
            Route(context, SpcSwoActivity::class.java, SpcSwoActivity.NUMBER, arrayOf(day, sound))
        }

        fun spcSwoDay1(context: Context) {
            spcSwo(context, "1")
        }

        fun spcSwoDay2(context: Context) {
            spcSwo(context, "2")
        }

        fun spcSwoDay3(context: Context) {
            spcSwo(context, "3")
        }

        fun spcSwoDay48(context: Context) {
            spcSwo(context, "4-8")
        }

        fun spcSwoSummary(context: Context) {
            Route(context, SpcSwoSummaryActivity::class.java)
        }

        fun spcTstorm(context: Context) {
            Route(context, SpcThunderStormOutlookActivity::class.java)
        }

        fun spotters(context: Context) {
            Route(context, SpottersActivity::class.java)
        }

        // url could be a chunk of text
        fun text(context: Context, url: String, title: String) {
            Route(
                context,
                TextScreenActivity::class.java,
                TextScreenActivity.URL,
                arrayOf(url, title)
            )
        }

        // url could be a chunk of text
        fun textPlaySound(context: Context, url: String, title: String) {
            Route(
                context,
                TextScreenActivity::class.java,
                TextScreenActivity.URL,
                arrayOf(url, title, "sound")
            )
        }

        fun wfoText(context: Context) {
            if (Location.isUS) {
                Route(
                    context,
                    WfoTextActivity::class.java,
                    WfoTextActivity.URL,
                    arrayOf(Location.wfo, "")
                )
            }
        }

        fun wfoTextBySector(context: Context, sector: String) {
            if (Location.isUS) {
                Route(
                    context,
                    WfoTextActivity::class.java,
                    WfoTextActivity.URL,
                    arrayOf(sector, "")
                )
            }
        }

        // used by voice recognition for sound on activity invocation
        fun wfoText(context: Context, array: Array<String>) {
            Route(context, WfoTextActivity::class.java, WfoTextActivity.URL, array)
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

        fun visBySector(context: Context, sector: String) {
            Route(context, GoesActivity::class.java, GoesActivity.RID, arrayOf(sector, "GEOCOLOR"))
        }

        fun web(context: Context, url: String) {
            Route(context, Intent.ACTION_VIEW, url.toUri())
        }

        fun webView(context: Context, url: String, title: String) {
            Route(context, WebView::class.java, WebView.URL, arrayOf(url, title))
        }

        fun webView(context: Context, url: String, title: String, extended: String) {
            Route(context, WebView::class.java, WebView.URL, arrayOf(url, title, extended))
        }

        fun wpcGefs(context: Context) {
            model(context, "1", "WPCGEFS", "WPC")
        }

        fun wpcImages(context: Context) {
            Route(context, NationalImagesActivity::class.java, "", arrayOf())
        }

        fun wpcRainfallSummary(context: Context) {
            Route(context, RainfallOutlookSummaryActivity::class.java)
        }

        fun wpcRainfallByDay(context: Context, dayIndex: String) {
            Route(
                context,
                RainfallOutlookActivity::class.java,
                RainfallOutlookActivity.NUMBER,
                arrayOf(dayIndex)
            )
        }

        fun wpcText(context: Context) {
            Route(
                context,
                NationalTextActivity::class.java,
                NationalTextActivity.URL,
                arrayOf("pmdspd", "Short Range Forecast Discussion")
            )
        }

        fun wpcText(context: Context, product: String) {
            Route(
                context,
                NationalTextActivity::class.java,
                NationalTextActivity.URL,
                arrayOf(product)
            )
        }

        fun wpcTextWithSound(context: Context, product: String, label: String) {
            Route(
                context,
                NationalTextActivity::class.java,
                NationalTextActivity.URL,
                arrayOf(product, label, "sound")
            )
        }
        //elys mod - for radar longpress menu
        fun vis00(context: Context) {
            Route(context, GoesActivity::class.java, GoesActivity.RID, arrayOf("CONUS", "00"))
        }
        //elys mod - for longpress in radar
        fun radarMosaicConus(context: Context) {
                Route(context, RadarMosaicActivity::class.java, RadarMosaicActivity.URL, arrayOf("CONUS"))
        }
		//elys mod - for Aurora Forecast   
        fun auroraForecast(context: Context) {
            Route(context, UtilityAurora::class.java, "", arrayOf())
        }		
    }
}

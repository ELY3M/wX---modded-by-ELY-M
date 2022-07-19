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
import joshuatee.wx.canada.CanadaHourlyActivity
import joshuatee.wx.canada.CanadaTextActivity
import joshuatee.wx.models.ModelsGenericActivity
import joshuatee.wx.nhc.NhcStormActivity
import joshuatee.wx.nhc.ObjectNhcStormDetails
import joshuatee.wx.radar.AwcRadarMosaicActivity
import joshuatee.wx.radar.RadarMosaicNwsActivity
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.radar.WXGLRadarActivityMultiPane
import joshuatee.wx.settings.*
import joshuatee.wx.spc.SpcMcdWatchShowActivity
import joshuatee.wx.spc.SpcSoundingsActivity
import joshuatee.wx.spc.SpcStormReportsActivity
import joshuatee.wx.spc.SpcSwoActivity
import joshuatee.wx.util.Utility
import joshuatee.wx.vis.GoesActivity
import joshuatee.wx.wpc.WpcTextProductsActivity

//
// Used to start another activity
//

class ObjectIntent() {

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

        fun showWpcText(context: Context, array: Array<String>) {
            ObjectIntent(context, WpcTextProductsActivity::class.java, WpcTextProductsActivity.URL, array)
        }

        fun showNhcStorm(context: Context, stormData: ObjectNhcStormDetails) {
            val intent = Intent(context, NhcStormActivity::class.java)
            intent.putExtra(NhcStormActivity.URL, stormData)
            context.startActivity(intent)
        }

        fun showMcd(context: Context, array: Array<String>) {
            ObjectIntent(context, SpcMcdWatchShowActivity::class.java, SpcMcdWatchShowActivity.NUMBER, array)
        }

        fun showSounding(context: Context) {
            ObjectIntent(context, SpcSoundingsActivity::class.java, SpcSoundingsActivity.URL, arrayOf(Location.wfo, ""))
        }

        fun showWfoText(context: Context) {
            if (Location.isUS) {
                ObjectIntent(context, WfoTextActivity::class.java, WfoTextActivity.URL, arrayOf(Location.wfo, ""))
            } else {
                ObjectIntent(context, CanadaTextActivity::class.java)
            }
        }

        fun showWfoText(context: Context, array: Array<String>) {
            ObjectIntent(context, WfoTextActivity::class.java, WfoTextActivity.URL, array)
        }

        fun showHourly(context: Context) {
            if (Location.isUS) {
                ObjectIntent(context, HourlyActivity::class.java, HourlyActivity.LOC_NUM, Location.currentLocationStr)
            } else {
                ObjectIntent(context, CanadaHourlyActivity::class.java, CanadaHourlyActivity.LOC_NUM, Location.currentLocationStr)
            }
        }

        fun showHazard(context: Context, array: Array<String>) {
            ObjectIntent(context, USAlertsDetailActivity::class.java, USAlertsDetailActivity.URL, array)
        }

        fun showVis(context: Context) {
            ObjectIntent(context, GoesActivity::class.java, GoesActivity.RID, arrayOf(""))
        }

        fun showVisNhc(context: Context, url: String) {
            ObjectIntent(context, GoesActivity::class.java, GoesActivity.RID, arrayOf(url, ""))
        }

        fun showObservations(context: Context) {
            if (Location.isUS) {
                ObjectIntent(context, ImageCollectionActivity::class.java, ImageCollectionActivity.TYPE, arrayOf("OBSERVATIONS"))
            } else {
                showImage(context, arrayOf("http://weather.gc.ca/data/wxoimages/wocanmap0_e.jpg", "Observations"))
            }
        }

        fun showUsAlerts(context: Context) {
            ObjectIntent(
                    context,
                    USWarningsWithRadarActivity::class.java,
                    USWarningsWithRadarActivity.URL,
                    arrayOf(".*?Tornado Warning.*?|.*?Severe Thunderstorm Warning.*?|.*?Flash Flood Warning.*?", "us")
            )
        }

        fun showRadarMosaic(context: Context) {
            if (UIPreferences.useAwcMosaic) {
                ObjectIntent(context, AwcRadarMosaicActivity::class.java, AwcRadarMosaicActivity.URL, arrayOf(""))
            } else {
                ObjectIntent(context, RadarMosaicNwsActivity::class.java, RadarMosaicNwsActivity.URL, arrayOf(""))
            }
//            ObjectIntent(context, RadarMosaicNwsActivity::class.java, RadarMosaicNwsActivity.URL, arrayOf("CONUS"))
        }

        fun showSpcStormReports(context: Context) {
            ObjectIntent(context, SpcStormReportsActivity::class.java, SpcStormReportsActivity.NO, arrayOf("today"))
        }

        fun showSpcSwo(context: Context, array: Array<String>) {
            ObjectIntent(context, SpcSwoActivity::class.java, SpcSwoActivity.NUMBER, array)
        }

        fun showModel(context: Context, array: Array<String>) {
            ObjectIntent(context, ModelsGenericActivity::class.java, ModelsGenericActivity.INFO, array)
        }

        fun showSettings(context: Context) {
            ObjectIntent(context, SettingsMainActivity::class.java)
        }

        fun showWeb(context: Context, url: String) {
            ObjectIntent(context, Intent.ACTION_VIEW, Uri.parse(url))
        }

        fun showWebView(context: Context, array: Array<String>) {
            ObjectIntent(context, WebView::class.java, WebView.URL, array)
        }

        fun showRadar(context: Context, array: Array<String>) {
            ObjectIntent(context, WXGLRadarActivity::class.java, WXGLRadarActivity.RID, array)
        }

        fun showRadarBySite(context: Context, radarSite: String) {
            val radarLabel = Utility.getRadarSiteName(radarSite)
            val state = radarLabel.split(",")[0]
            showRadar(context, arrayOf(radarSite, state, "N0Q", ""))
        }

        fun showRadarMultiPane(context: Context, array: Array<String>) {
            ObjectIntent(context, WXGLRadarActivityMultiPane::class.java, WXGLRadarActivityMultiPane.RID, array)
        }

        fun showImage(context: Context, array: Array<String>) {
            ObjectIntent(context, ImageShowActivity::class.java, ImageShowActivity.URL, array)
        }

        fun favoriteAdd(context: Context, array: Array<String>) {
            ObjectIntent(context, FavAddActivity::class.java, FavAddActivity.TYPE, array)
        }

        fun favoriteRemove(context: Context, array: Array<String>) {
            ObjectIntent(context, FavRemoveActivity::class.java, FavRemoveActivity.TYPE, array)
        }

        fun showText(context: Context, array: Array<String>) {
            ObjectIntent(context, TextScreenActivity::class.java, TextScreenActivity.URL, array)
        }

        fun showLocationEdit(context: Context, array: Array<String>) {
            ObjectIntent(context, SettingsLocationGenericActivity::class.java, SettingsLocationGenericActivity.LOC_NUM, array)
        }
    }
}

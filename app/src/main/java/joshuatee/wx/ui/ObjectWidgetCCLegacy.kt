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

package joshuatee.wx.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import joshuatee.wx.*
import joshuatee.wx.activitiesmisc.*
import joshuatee.wx.canada.CanadaAlertsActivity
import joshuatee.wx.canada.CanadaHourlyActivity
import joshuatee.wx.canada.CanadaRadarActivity
import joshuatee.wx.canada.CanadaTextActivity
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.spc.SPCSoundingsActivity
import joshuatee.wx.spc.UtilitySPC
import joshuatee.wx.util.*
import joshuatee.wx.vis.USNWSGOESActivity
import java.util.*

import java.util.regex.Pattern

class ObjectWidgetCCLegacy(context: Context, allWidgetIds: IntArray) {

    val remoteViews: RemoteViews = RemoteViews(context.packageName, R.layout.widget_layout)
    private val actionCc = "actionCc"
    private val actionSd = "actionSd"
    private val actionHazard = "actionHazard"
    private val actionCloud = "actionCloud"
    private val actionRadar = "actionRadar"
    private val actionAfd = "actionAfd"
    private val actionHourly = "actionHourly"
    private val actionAlert = "actionAlert"
    private val actionDashboard = "actionDashboard"

    init {
        val widgetLocNum = Utility.readPref(context, "WIDGET_LOCATION", "1")
        val cc = Utility.readPref(context, "CC_WIDGET", "No data")
        val sd = Utility.readPref(context, "7DAY_WIDGET", "No data")
        val updtime = Utility.readPref(context, "UPDTIME_WIDGET", "No data")
        val sdExt = Utility.readPref(context, "7DAY_EXT_WIDGET", "No data")
        val hazardRaw = Utility.readPref(context, "HAZARD_RAW_WIDGET", "No data")
        val nws1Current = Utility.readPref(context, "NWS$widgetLocNum", "")
        val nwsLocation = Utility.readPref(context, "NWS_LOCATION_$nws1Current", "")
        val nwsLocationArr = nwsLocation.split(",").dropLastWhile { it.isEmpty() }
        val nws1StateCurrent = nwsLocationArr.getOrNull(0) ?: ""
        val rid1 = Location.getRid(context, widgetLocNum)
        val locLabel = Utility.readPref(context, "LOC" + widgetLocNum + "_LABEL", "")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            remoteViews.setImageViewResource(R.id.b_radar, R.drawable.ic_flash_on_24dp)
            remoteViews.setImageViewResource(R.id.b_cloud, R.drawable.ic_cloud_24dp)
            remoteViews.setImageViewResource(R.id.b_afd, R.drawable.ic_info_outline_24dp)
            remoteViews.setImageViewResource(R.id.b_hourly, R.drawable.ic_place_24dp)
            remoteViews.setImageViewResource(R.id.b_alert, R.drawable.ic_warning_24dp)
            remoteViews.setImageViewResource(R.id.b_dash, R.drawable.ic_report_24dp)
        } else {
            val img = listOf(R.drawable.ic_flash_on_24dp, R.drawable.ic_cloud_24dp, R.drawable.ic_info_outline_24dp, R.drawable.ic_place_24dp, R.drawable.ic_warning_24dp, R.drawable.ic_report_24dp)
            val btn = listOf(R.id.b_radar, R.id.b_cloud, R.id.b_afd, R.id.b_hourly, R.id.b_alert, R.id.b_dash)
            img.indices.forEach { ObjectFab.fabSetResDrawable(context, remoteViews, btn[it], img[it]) }
        }
        remoteViews.setTextViewText(R.id.cc, cc)
        remoteViews.setTextViewText(R.id.updtime, updtime)
        var hazardSum = ""
        // FIXME legacy matcher
        try {
            val p = Pattern.compile("<h3>(.*?)</h3>")
            val m = p.matcher(hazardRaw)
            while (m.find()) {
                hazardSum += MyApplication.newline + m.group(1)
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        hazardSum = hazardSum.replace(("^" + MyApplication.newline).toRegex(), "")
        if (hazardSum != "")
            remoteViews.setViewVisibility(R.id.hazard, View.VISIBLE)
        else
            remoteViews.setViewVisibility(R.id.hazard, View.GONE)
        remoteViews.setTextViewText(R.id.hazard, hazardSum)
        remoteViews.setTextViewText(R.id.forecast, sd)
        remoteViews.setTextViewText(R.id.widget_time, "Updated: " + UtilityTime.getDateAsString("h:mm a")) // "%k:%M:%S"
        UtilityWidget.setupIntent(context, remoteViews, SPCSoundingsActivity::class.java, R.id.cc, SPCSoundingsActivity.URL, arrayOf(nws1Current, ""), actionCc)
        UtilityWidget.setupIntent(context, remoteViews, TextScreenActivity::class.java, R.id.forecast, TextScreenActivity.URL, arrayOf(sdExt, locLabel), actionSd)
        var hazardsExt = Utility.getHazards(hazardRaw)
        hazardsExt = hazardsExt.replace("<hr /><br />", "")
        UtilityWidget.setupIntent(context, remoteViews, TextScreenActivity::class.java, R.id.hazard, TextScreenActivity.URL, arrayOf(hazardsExt, "Local Hazards"), actionHazard)
        // radar
        if (Location.isUS(widgetLocNum)) {
            UtilityWidget.setupIntent(context, remoteViews, WXGLRadarActivity::class.java, R.id.b_radar, WXGLRadarActivity.RID, arrayOf(rid1, nws1StateCurrent), actionRadar)
        } else {
            UtilityWidget.setupIntent(context, remoteViews, CanadaRadarActivity::class.java, R.id.b_radar, CanadaRadarActivity.RID, arrayOf(rid1, "rad"), actionRadar)
        }
        // local alerts ( or nat for CA )
        if (Location.isUS(widgetLocNum)) {
            UtilityWidget.setupIntent(context, remoteViews, USWarningsWithRadarActivity::class.java, R.id.b_alert, USWarningsWithRadarActivity.URL,
                    arrayOf(".*?Tornado Warning.*?|.*?Severe Thunderstorm Warning.*?|.*?Flash Flood Warning.*?|.*?Special Marine Warning.*?|.*?Severe Weather Statement.*?|.*?Special Weather Statement.*?", "us"), actionAlert)
        } else {
            UtilityWidget.setupIntent(context, remoteViews, CanadaAlertsActivity::class.java, R.id.b_alert, actionAlert)
        }
        // Hourly
        if (Location.isUS(widgetLocNum)) {
            UtilityWidget.setupIntent(context, remoteViews, HourlyActivity::class.java, R.id.b_hourly, HourlyActivity.LOC_NUM, widgetLocNum, actionHourly)
        } else {
            UtilityWidget.setupIntent(context, remoteViews, CanadaHourlyActivity::class.java, R.id.b_hourly, CanadaHourlyActivity.LOC_NUM, widgetLocNum, actionHourly)
        }
        // AFD
        if (Location.isUS(widgetLocNum)) {
            UtilityWidget.setupIntent(context, remoteViews, AFDActivity::class.java, R.id.b_afd, AFDActivity.URL, arrayOf(nws1Current, ""), actionAfd)
        } else {
            UtilityWidget.setupIntent(context, remoteViews, CanadaTextActivity::class.java, R.id.b_afd, actionAfd)
        }
        UtilityWidget.setupIntent(context, remoteViews, SevereDashboardActivity::class.java, R.id.b_dash, actionDashboard)
        // cloud icon - vis
        if (Location.isUS(widgetLocNum)) {
            UtilityWidget.setupIntent(context, remoteViews, USNWSGOESActivity::class.java, R.id.b_cloud, USNWSGOESActivity.RID, arrayOf("nws", nws1Current.toLowerCase(Locale.US)), actionCloud)
        } else {
            UtilityWidget.setupIntent(context, remoteViews, CanadaRadarActivity::class.java, R.id.b_cloud, CanadaRadarActivity.RID, arrayOf(rid1, "vis"), actionCloud)
        }
        val updateIntent = Intent()
        updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        updateIntent.putExtra(Widget.WIDGET_IDS_KEY, allWidgetIds)
        val pendingIntentWidgetTime = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.widget_time, pendingIntentWidgetTime)
        val tabStr = UtilitySPC.checkSPC(context)
        remoteViews.setViewVisibility(R.id.tab, View.VISIBLE)
        remoteViews.setTextViewText(R.id.tab, tabStr[0] + "   " + tabStr[1])
    }
}



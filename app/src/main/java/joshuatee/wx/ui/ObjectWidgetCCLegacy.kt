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
import joshuatee.wx.spc.SpcSoundingsActivity
import joshuatee.wx.spc.UtilitySpc
import joshuatee.wx.util.*
import joshuatee.wx.vis.GoesActivity

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
        val widgetLocationNumber = Utility.readPref(context, "WIDGET_LOCATION", "1")
        val cc = Utility.readPref(context, "CC_WIDGET", "No data")
        val sd = Utility.readPref(context, "7DAY_WIDGET", "No data")
        val updateTime = Utility.readPref(context, "UPDTIME_WIDGET", "No data")
        val sdExt = Utility.readPref(context, "7DAY_EXT_WIDGET", "No data")
        val hazardRaw = Utility.readPref(context, "HAZARD_RAW_WIDGET", "No data")
        val wfo = Utility.readPref(context, "NWS$widgetLocationNumber", "")
        val radarSite = Location.getRid(context, widgetLocationNumber)
        val locLabel = Utility.readPref(context, "LOC" + widgetLocationNumber + "_LABEL", "")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            remoteViews.setImageViewResource(R.id.b_radar, R.drawable.ic_flash_on_24dp)
            remoteViews.setImageViewResource(R.id.b_cloud, R.drawable.ic_cloud_24dp)
            remoteViews.setImageViewResource(R.id.b_afd, R.drawable.ic_info_outline_24dp)
            remoteViews.setImageViewResource(R.id.b_hourly, R.drawable.ic_place_24dp)
            remoteViews.setImageViewResource(R.id.b_alert, R.drawable.ic_warning_24dp)
            remoteViews.setImageViewResource(R.id.b_dash, R.drawable.ic_report_24dp)
        } else {
            val img = listOf(
                    R.drawable.ic_flash_on_24dp,
                    R.drawable.ic_cloud_24dp,
                    R.drawable.ic_info_outline_24dp,
                    R.drawable.ic_place_24dp,
                    R.drawable.ic_warning_24dp,
                    R.drawable.ic_report_24dp
            )
            val btn = listOf(
                    R.id.b_radar,
                    R.id.b_cloud,
                    R.id.b_afd,
                    R.id.b_hourly,
                    R.id.b_alert,
                    R.id.b_dash
            )
            img.indices.forEach {
                UtilityUI.setResDrawable(
                        context,
                        remoteViews,
                        btn[it],
                        img[it]
                )
            }
        }
        remoteViews.setTextViewText(R.id.cc, cc)
        remoteViews.setTextViewText(R.id.updtime, updateTime)
        var hazardSum = ""
        // FIXME legacy matcher
        try {
            val p = Pattern.compile("<h3>(.*?)</h3>")
            val m = p.matcher(hazardRaw)
            while (m.find()) {
                hazardSum += MyApplication.newline + m.group(1)
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        hazardSum = hazardSum.replace(("^" + MyApplication.newline).toRegex(), "")
        if (hazardSum != "")
            remoteViews.setViewVisibility(R.id.hazard, View.VISIBLE)
        else
            remoteViews.setViewVisibility(R.id.hazard, View.GONE)
        remoteViews.setTextViewText(R.id.hazard, hazardSum)
        remoteViews.setTextViewText(R.id.forecast, sd)
        remoteViews.setTextViewText(
                R.id.widget_time,
                "Updated: " + UtilityTime.getDateAsString("h:mm a")
        ) // "%k:%M:%S"
        UtilityWidget.setupIntent(
                context,
                remoteViews,
                SpcSoundingsActivity::class.java,
                R.id.cc,
                SpcSoundingsActivity.URL,
                arrayOf(wfo, ""),
                actionCc
        )
        UtilityWidget.setupIntent(
                context,
                remoteViews,
                TextScreenActivity::class.java,
                R.id.forecast,
                TextScreenActivity.URL,
                arrayOf(sdExt, locLabel),
                actionSd
        )
        var hazardsExt = Utility.getHazards(hazardRaw)
        hazardsExt = hazardsExt.replace("<hr /><br />", "")
        UtilityWidget.setupIntent(
                context,
                remoteViews,
                TextScreenActivity::class.java,
                R.id.hazard,
                TextScreenActivity.URL,
                arrayOf(hazardsExt, "Local Hazards"),
                actionHazard
        )
        // radar
        if (Location.isUS(widgetLocationNumber)) {
            UtilityWidget.setupIntent(
                    context,
                    remoteViews,
                    WXGLRadarActivity::class.java,
                    R.id.b_radar,
                    WXGLRadarActivity.RID,
                    arrayOf(radarSite),
                    actionRadar
            )
        } else {
            UtilityWidget.setupIntent(
                    context,
                    remoteViews,
                    CanadaRadarActivity::class.java,
                    R.id.b_radar,
                    CanadaRadarActivity.RID,
                    arrayOf(radarSite, "rad"),
                    actionRadar
            )
        }
        // local alerts ( or nat for CA )
        if (Location.isUS(widgetLocationNumber)) {
            UtilityWidget.setupIntent(
                    context,
                    remoteViews,
                    USWarningsWithRadarActivity::class.java,
                    R.id.b_alert,
                    USWarningsWithRadarActivity.URL,
                    arrayOf(
                            ".*?Tornado Warning.*?|.*?Severe Thunderstorm Warning.*?|.*?Flash Flood Warning.*?",
                            "us"
                    ),
                    actionAlert
            )
        } else {
            UtilityWidget.setupIntent(
                    context,
                    remoteViews,
                    CanadaAlertsActivity::class.java,
                    R.id.b_alert,
                    actionAlert
            )
        }
        // Hourly
        if (Location.isUS(widgetLocationNumber)) {
            UtilityWidget.setupIntent(
                    context,
                    remoteViews,
                    HourlyActivity::class.java,
                    R.id.b_hourly,
                    HourlyActivity.LOC_NUM,
                    widgetLocationNumber,
                    actionHourly
            )
        } else {
            UtilityWidget.setupIntent(
                    context,
                    remoteViews,
                    CanadaHourlyActivity::class.java,
                    R.id.b_hourly,
                    CanadaHourlyActivity.LOC_NUM,
                    widgetLocationNumber,
                    actionHourly
            )
        }
        // AFD
        if (Location.isUS(widgetLocationNumber)) {
            UtilityWidget.setupIntent(
                    context,
                    remoteViews,
                    AfdActivity::class.java,
                    R.id.b_afd,
                    AfdActivity.URL,
                    arrayOf(wfo, ""),
                    actionAfd
            )
        } else {
            UtilityWidget.setupIntent(
                    context,
                    remoteViews,
                    CanadaTextActivity::class.java,
                    R.id.b_afd,
                    actionAfd
            )
        }
        UtilityWidget.setupIntent(
                context,
                remoteViews,
                SevereDashboardActivity::class.java,
                R.id.b_dash,
                actionDashboard
        )
        // cloud icon - vis
        if (Location.isUS(widgetLocationNumber)) {
            UtilityWidget.setupIntent(
                    context,
                    remoteViews,
                    GoesActivity::class.java,
                    R.id.b_cloud,
                    GoesActivity.RID,
                    arrayOf(""),
                    actionCloud
            )
        } else {
            UtilityWidget.setupIntent(
                    context,
                    remoteViews,
                    CanadaRadarActivity::class.java,
                    R.id.b_cloud,
                    CanadaRadarActivity.RID,
                    arrayOf(radarSite, "vis"),
                    actionCloud
            )
        }
        val updateIntent = Intent()
        updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        updateIntent.putExtra(Widget.WIDGET_IDS_KEY, allWidgetIds)
        val pendingIntentWidgetTime =
                PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.widget_time, pendingIntentWidgetTime)
        val tabStr = UtilitySpc.checkSpc()
        remoteViews.setViewVisibility(R.id.tab, View.VISIBLE)
        remoteViews.setTextViewText(R.id.tab, tabStr[0] + "   " + tabStr[1])
    }
}



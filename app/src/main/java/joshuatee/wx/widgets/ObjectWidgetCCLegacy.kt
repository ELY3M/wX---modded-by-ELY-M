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

package joshuatee.wx.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import joshuatee.wx.R
import joshuatee.wx.Widget
import joshuatee.wx.misc.HourlyActivity
import joshuatee.wx.misc.SevereDashboardActivity
import joshuatee.wx.misc.TextScreenActivity
import joshuatee.wx.misc.USAlertsActivity
import joshuatee.wx.misc.WfoTextActivity
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.spc.SpcSoundingsActivity
import joshuatee.wx.spc.UtilitySpc
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityUS
import joshuatee.wx.vis.GoesActivity
import java.util.regex.Pattern

class ObjectWidgetCCLegacy(context: Context, allWidgetIds: IntArray) {

    private val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout)
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
        remoteViews.setImageViewResource(R.id.b_radar, R.drawable.ic_flash_on_24dp_white2)
        remoteViews.setImageViewResource(R.id.b_cloud, R.drawable.ic_cloud_24dp_white)
        remoteViews.setImageViewResource(R.id.b_afd, R.drawable.ic_info_outline_24dp_white)
        remoteViews.setImageViewResource(R.id.b_hourly, R.drawable.ic_place_24dp_white)
        remoteViews.setImageViewResource(R.id.b_alert, R.drawable.ic_warning_24dp_white)
        remoteViews.setImageViewResource(R.id.b_dash, R.drawable.ic_report_24dp_white)
        remoteViews.setTextViewText(R.id.cc, cc)
        remoteViews.setTextViewText(R.id.updtime, updateTime)
        var hazardSum = ""
        // FIXME legacy matcher
        try {
            val p = Pattern.compile("<h3>(.*?)</h3>")
            val m = p.matcher(hazardRaw)
            while (m.find()) {
                hazardSum += GlobalVariables.newline + m.group(1)
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        hazardSum = hazardSum.replace(("^" + GlobalVariables.newline).toRegex(), "")
        if (hazardSum != "") {
            remoteViews.setViewVisibility(R.id.hazard, View.VISIBLE)
        } else {
            remoteViews.setViewVisibility(R.id.hazard, View.GONE)
        }
        remoteViews.setTextViewText(R.id.hazard, hazardSum)
        remoteViews.setTextViewText(R.id.forecast, sd)
        remoteViews.setTextViewText(R.id.widget_time, "Updated: " + ObjectDateTime.getDateAsString("h:mm a")) // "%k:%M:%S"
        UtilityWidget.setupIntent(context, remoteViews, SpcSoundingsActivity::class.java, R.id.cc, SpcSoundingsActivity.URL, arrayOf(wfo, ""), actionCc)
        UtilityWidget.setupIntent(context, remoteViews, TextScreenActivity::class.java, R.id.forecast, TextScreenActivity.URL, arrayOf(sdExt, locLabel), actionSd)
        var hazardsExt = UtilityUS.getHazardsCCLegacy(hazardRaw)
        hazardsExt = hazardsExt.replace("<hr /><br />", "")
        UtilityWidget.setupIntent(context, remoteViews, TextScreenActivity::class.java, R.id.hazard, TextScreenActivity.URL, arrayOf(hazardsExt, "Local Hazards"), actionHazard)
        UtilityWidget.setupIntent(context, remoteViews, WXGLRadarActivity::class.java, R.id.b_radar, WXGLRadarActivity.RID, arrayOf(radarSite), actionRadar)
        // local alerts
        if (Location.isUS(widgetLocationNumber)) {
            UtilityWidget.setupIntent(
                    context,
                    remoteViews,
                    USAlertsActivity::class.java,
                    R.id.b_alert,
                    USAlertsActivity.URL,
                    arrayOf(".*?Tornado Warning.*?|.*?Severe Thunderstorm Warning.*?|.*?Flash Flood Warning.*?", "us"),
                    actionAlert
            )
        }
        // Hourly
        if (Location.isUS(widgetLocationNumber)) {
            UtilityWidget.setupIntent(context, remoteViews, HourlyActivity::class.java, R.id.b_hourly, HourlyActivity.LOC_NUM, widgetLocationNumber, actionHourly)
        }
        // AFD
        if (Location.isUS(widgetLocationNumber)) {
            UtilityWidget.setupIntent(context, remoteViews, WfoTextActivity::class.java, R.id.b_afd, WfoTextActivity.URL, arrayOf(wfo, ""), actionAfd)
        }
        UtilityWidget.setupIntent(context, remoteViews, SevereDashboardActivity::class.java, R.id.b_dash, actionDashboard)
        // cloud icon - vis
        UtilityWidget.setupIntent(context, remoteViews, GoesActivity::class.java, R.id.b_cloud, GoesActivity.RID, arrayOf(""), actionCloud)
        val updateIntent = Intent()
        updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        updateIntent.putExtra(Widget.WIDGET_IDS_KEY, allWidgetIds)
        val pendingIntentWidgetTime = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        remoteViews.setOnClickPendingIntent(R.id.widget_time, pendingIntentWidgetTime)
        val tabStr = UtilitySpc.checkSpc()
        remoteViews.setViewVisibility(R.id.tab, View.VISIBLE)
        remoteViews.setTextViewText(R.id.tab, tabStr[0] + "   " + tabStr[1])
    }

    fun get() = remoteViews
}

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

package joshuatee.wx.settings

import android.content.Context
import joshuatee.wx.objects.LatLon
import joshuatee.wx.util.Utility
import java.util.Locale
import joshuatee.wx.parse
import joshuatee.wx.radar.RadarSites
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityDownloadNws
import joshuatee.wx.util.UtilityString
import joshuatee.wx.util.WfoSites

object Location {

    var locations = listOf<ObjectLocation>()
    private var numLocations = 1
    var currentLocation = 0
        private set

    // used only on the main screen in the dialogue to choose the location
    // needs to remain a mutable list
    val listOf = mutableListOf<String>()

    fun getNumberOfLocations(): Int = numLocations

    fun us(xStr: String): Boolean = if (xStr.isNotEmpty()) {
        Character.isDigit(xStr[0])
    } else {
        true
    }

    private fun checkCurrentLocationValidity(context: Context) {
        if (currentLocation >= locations.size) {
            setCurrentLocationStr(context, (locations.lastIndex + 1).toString())
        }
    }

    private fun initNumLocations(context: Context) {
        val numberOfLocations = Utility.readPrefInt(context, "LOC_NUM_INT", 1)
        setNumLocations(context, numberOfLocations)
    }

    private fun setNumLocations(context: Context, numberOfLocations: Int) {
        numLocations = numberOfLocations
        Utility.writePrefInt(context, "LOC_NUM_INT", numberOfLocations)
    }

    val currentLocationStr: String
        get() = (currentLocation + 1).toString()

    val wfo get() = locations.getOrNull(currentLocation)?.wfo ?: "DTX"

    val radarSite get() = locations.getOrNull(currentLocation)?.rid ?: "DTX"

    private val x get() = locations.getOrNull(currentLocation)?.x ?: "0.0"

    private val y get() = locations.getOrNull(currentLocation)?.y ?: "-0.0"

    val latLon get() = LatLon(x, y)

    val name get() = locations.getOrNull(currentLocation)?.name ?: ""

    fun getName(locNum: Int): String = locations.getOrNull(locNum)?.name ?: "0.0"

    fun getX(locNum: Int): String = locations.getOrNull(locNum)?.x ?: "0.0"

    fun getY(locNum: Int): String = locations.getOrNull(locNum)?.y ?: "-0.0"

    fun getRadarSite(locNum: Int): String = locations.getOrNull(locNum)?.rid ?: "DTX"

    fun getRadarSite(context: Context, locNum: String): String =
        Utility.readPref(context, "RID$locNum", "")

    fun getWfo(locNum: Int): String = locations.getOrNull(locNum)?.wfo ?: "DTX"

    fun getObservation(locNum: Int): String = locations.getOrNull(locNum)?.observation ?: ""

    fun getLatLon(locNum: Int): LatLon = LatLon(getX(locNum), getY(locNum))

    fun getIdentifier(locNum: Int): String {
        val lat = locations.getOrNull(locNum)?.x ?: ""
        val lon = locations.getOrNull(locNum)?.y ?: ""
        return "LAT" + lat + "LON" + lon
    }

    fun isUS(locationNumber: Int): Boolean = locations.getOrNull(locationNumber)?.isUS ?: true

    val isUS get() = isUS(currentLocation)

    fun refresh(context: Context) {
        initNumLocations(context)
        locations = (0 until getNumberOfLocations()).map { ObjectLocation(context, it) }
        listOf.clear()
        listOf.addAll(locations.map { it.name } + "Add Location...")
        setCurrentLocationStr(context, Utility.readPref(context, "CURRENT_LOC_FRAGMENT", "1"))
        checkCurrentLocationValidity(context)
    }

    private fun getWfoRadarSiteFromPoint(location: LatLon): List<String> {
        val pointData = UtilityDownloadNws.getLocationPointData(location)
        // "cwa": "IWX",
        // "radarStation": "KGRR"
        val wfo = pointData.parse("\"cwa\": \"(.*?)\"")
        var radarStation = pointData.parse("\"radarStation\": \"(.*?)\"")
        radarStation = UtilityString.getLastXChars(radarStation, 3)
        return listOf(wfo, radarStation)
    }

    fun save(context: Context, latLon: LatLon, name: String = latLon.toString()): String =
        save(
            context,
            (getNumberOfLocations() + 1).toString(),
            latLon.latString,
            latLon.lonString,
            name
        )

    fun save(context: Context, locNum: String, x: String, y: String, label: String): String {
        if (x == "" || y == "" || label == "") {
            return "Location label, latitude, and longitude all must have valid values, please try again."
        }
        val locNumInt = To.int(locNum)
        val locNumIntCurrent = getNumberOfLocations()
        val locNumToSave = if (locNumInt == locNumIntCurrent + 1) {
            locNumInt
        } else {
            locNumIntCurrent
        }
        Utility.writePref(context, "LOC" + locNum + "_X", x)
        Utility.writePref(context, "LOC" + locNum + "_Y", y)
        Utility.writePref(context, "LOC" + locNum + "_LABEL", label)
        setNumLocations(context, locNumToSave)
        val wfoAndRadar = getWfoRadarSiteFromPoint(LatLon(x, y))
        var wfo = wfoAndRadar[0]
        var radarSite = wfoAndRadar[1]
        if (wfo == "") {
            wfo = WfoSites.sites.getNearest(LatLon(x, y))
        }
        if (radarSite == "" || radarSite == "LIX") {
            radarSite = RadarSites.getNearestCode(LatLon(x, y))
        }
        Utility.writePref(context, "RID$locNum", radarSite.uppercase(Locale.US))
        Utility.writePref(context, "NWS$locNum", wfo.uppercase(Locale.US))
        refresh(context)
        setCurrentLocationStr(context, locNum)
        return "Saving location $locNum as $label ($x,$y) " + wfo.uppercase(Locale.US) + "(" + radarSite.uppercase(
            Locale.US
        ) + ")"
    }

    fun setCurrentLocationStr(context: Context, locNum: String) {
        Utility.writePref(context, "CURRENT_LOC_FRAGMENT", locNum)
        currentLocation = To.int(locNum) - 1
    }

    fun delete(context: Context, locToDeleteStr: String) {
        val locToDeleteInt = To.int(locToDeleteStr)
        val locNumIntCurrent = getNumberOfLocations()
        val locNumIntCurrentStr = locNumIntCurrent.toString()
        if (locToDeleteInt == locNumIntCurrent) {
            setNumLocations(context, locNumIntCurrent - 1)
        } else {
            var i = locToDeleteInt
            while (i < locNumIntCurrent) {
                val j = i + 1
                val jStr = j.toString()
                val iStr = i.toString()
                val locObsCurrent = Utility.readPref(context, "LOC" + jStr + "_OBSERVATION", "")
                val locXCurrent = Utility.readPref(context, "LOC" + jStr + "_X", "")
                val locYCurrent = Utility.readPref(context, "LOC" + jStr + "_Y", "")
                val locLabelCurrent = Utility.readPref(context, "LOC" + jStr + "_LABEL", "")
                val nwsCurrent = Utility.readPref(context, "NWS$jStr", "")
                val ridCurrent = Utility.readPref(context, "RID$jStr", "")
                val alertNotificationCurrent =
                    Utility.readPref(context, "ALERT" + jStr + "_NOTIFICATION", "false")
                val alertNotificationRadarCurrent =
                    Utility.readPref(context, "ALERT_NOTIFICATION_RADAR$jStr", "false")
                val alertCcNotificationCurrent =
                    Utility.readPref(context, "ALERT_CC" + jStr + "_NOTIFICATION", "false")
                val alert7Day1NotificationCurrent =
                    Utility.readPref(context, "ALERT_7DAY_" + jStr + "_NOTIFICATION", "false")
                val alertNotificationSoundCurrent =
                    Utility.readPref(context, "ALERT_NOTIFICATION_SOUND$jStr", "false")
                val alertNotificationMcdCurrent =
                    Utility.readPref(context, "ALERT_NOTIFICATION_MCD$jStr", "false")
                val alertNotificationSwoCurrent =
                    Utility.readPref(context, "ALERT_NOTIFICATION_SWO$jStr", "false")
                val alertNotificationSpcfwCurrent =
                    Utility.readPref(context, "ALERT_NOTIFICATION_SPCFW$jStr", "false")
                val alertNotificationWpcmpdCurrent =
                    Utility.readPref(context, "ALERT_NOTIFICATION_WPCMPD$jStr", "false")
                Utility.writePref(
                    context,
                    "ALERT" + iStr + "_NOTIFICATION",
                    alertNotificationCurrent
                )
                Utility.writePref(
                    context,
                    "ALERT_CC" + iStr + "_NOTIFICATION",
                    alertCcNotificationCurrent
                )
                Utility.writePref(
                    context,
                    "ALERT_7DAY_" + iStr + "_NOTIFICATION",
                    alert7Day1NotificationCurrent
                )
                Utility.writePref(
                    context,
                    "ALERT_NOTIFICATION_SOUND$iStr",
                    alertNotificationSoundCurrent
                )
                Utility.writePref(
                    context,
                    "ALERT_NOTIFICATION_MCD$iStr",
                    alertNotificationMcdCurrent
                )
                Utility.writePref(
                    context,
                    "ALERT_NOTIFICATION_SWO$iStr",
                    alertNotificationSwoCurrent
                )
                Utility.writePref(
                    context,
                    "ALERT_NOTIFICATION_SPCFW$iStr",
                    alertNotificationSpcfwCurrent
                )
                Utility.writePref(
                    context,
                    "ALERT_NOTIFICATION_WPCMPD$iStr",
                    alertNotificationWpcmpdCurrent
                )
                Utility.writePref(
                    context,
                    "ALERT_NOTIFICATION_RADAR$iStr",
                    alertNotificationRadarCurrent
                )
                Utility.writePref(context, "LOC" + iStr + "_OBSERVATION", locObsCurrent)
                Utility.writePref(context, "LOC" + iStr + "_X", locXCurrent)
                Utility.writePref(context, "LOC" + iStr + "_Y", locYCurrent)
                Utility.writePref(context, "LOC" + iStr + "_LABEL", locLabelCurrent)
                Utility.writePref(context, "NWS$iStr", nwsCurrent)
                Utility.writePref(context, "RID$iStr", ridCurrent)
                setNumLocations(context, locNumIntCurrent - 1)
                i += 1
            }
        }
        // blank out for next loc add
        Utility.writePref(context, "ALERT" + locNumIntCurrentStr + "_NOTIFICATION", "false")
        Utility.writePref(context, "ALERT_CC" + locNumIntCurrentStr + "_NOTIFICATION", "false")
        Utility.writePref(context, "ALERT_7DAY_" + locNumIntCurrentStr + "_NOTIFICATION", "false")
        Utility.writePref(context, "ALERT_NOTIFICATION_SOUND$locNumIntCurrentStr", "false")
        Utility.writePref(context, "ALERT_NOTIFICATION_RADAR$locNumIntCurrentStr", "false")
        Utility.writePref(context, "ALERT_NOTIFICATION_MCD$locNumIntCurrentStr", "false")
        Utility.writePref(context, "ALERT_NOTIFICATION_SWO$locNumIntCurrentStr", "false")
        Utility.writePref(context, "ALERT_NOTIFICATION_SPCFW$locNumIntCurrentStr", "false")
        Utility.writePref(context, "ALERT_NOTIFICATION_WPCMPD$locNumIntCurrentStr", "false")
        Utility.writePref(context, "LOC" + locNumIntCurrentStr + "_X", "")
        Utility.writePref(context, "LOC" + locNumIntCurrentStr + "_Y", "")
        val locFragCurrentInt = currentLocation
        if (locToDeleteInt == (locFragCurrentInt + 1)) {
            Utility.writePref(context, "CURRENT_LOC_FRAGMENT", "1")
            setCurrentLocationStr(context, "1")
        } else if (locFragCurrentInt > locToDeleteInt) {
            val shiftNum = (locFragCurrentInt - 1).toString()
            Utility.writePref(context, "CURRENT_LOC_FRAGMENT", shiftNum)
            setCurrentLocationStr(context, shiftNum)
        }
        val widgetLocNum = Utility.readPref(context, "WIDGET_LOCATION", "1")
        val widgetLocNumInt = To.int(widgetLocNum)
        if (locToDeleteInt == widgetLocNumInt) {
            Utility.writePref(context, "WIDGET_LOCATION", "1")
        } else if (widgetLocNumInt > locToDeleteInt) {
            val shiftNum = (widgetLocNumInt - 1).toString()
            Utility.writePref(context, "WIDGET_LOCATION", shiftNum)
        }
        refresh(context)
    }
}

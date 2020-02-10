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

package joshuatee.wx.settings

import android.content.Context
import joshuatee.wx.MyApplication
import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.objects.LatLonStr
import joshuatee.wx.radar.LatLon
import joshuatee.wx.util.Utility
import java.util.*
import joshuatee.wx.Extensions.*
import joshuatee.wx.GlobalDictionaries
import joshuatee.wx.util.UtilityString

// implement up/down mini fab in settings
// hide fabs if only one location
// if up arrow selected , read in  j ( as B )  and j-1 ( as A ) provided j > 0
// if down arrow selected , read in j ( as A ) and j+1 ( as B )  provided j < num_loc-2
// handle corner cases later
// if up selected, write out A(j) and B(j-1)
// if down selected, write out A(j+1) and B(j)

class Location(val context: Context, locNumInt: Int) {

    val x: String
    val y: String
    val name: String
    private val countyCurrent: String
    private val zoneCurrent: String
    val wfo: String
    val rid: String
    val state: String
    private val nwsStateCurrent: String
    private val alertNotificationCurrent: String
    private val alertNotificationRadarCurrent: String
    private val alertCcNotificationCurrent: String
    private val alertSevenDayNotificationCurrent: String
    private val alertNotificationSoundCurrent: String
    private val alertNotificationMcdCurrent: String
    private val alertNotificationSwoCurrent: String
    private val alertNotificationSpcfwCurrent: String
    private val alertNotificationWpcmpdCurrent: String
    private val raw: String
    private val dst: String
    val isUS: Boolean
    var observation = ""
    private val prefNumberString: String

    init {
        val jStr = (locNumInt + 1).toString()
        prefNumberString = jStr
        x = Utility.readPref(context, "LOC" + jStr + "_X", "0.0")
        y = Utility.readPref(context, "LOC" + jStr + "_Y", "0.0")
        name = Utility.readPref(context, "LOC" + jStr + "_LABEL", "")
        countyCurrent = Utility.readPref(context, "COUNTY$jStr", "")
        zoneCurrent = Utility.readPref(context, "ZONE$jStr", "")
        wfo = Utility.readPref(context, "NWS$jStr", "")
        rid = Utility.readPref(context, "RID$jStr", "")
        nwsStateCurrent = Utility.readPref(context, "NWS" + jStr + "_STATE", "")
        alertNotificationCurrent = Utility.readPref(context, "ALERT" + jStr + "_NOTIFICATION", "false")
        alertNotificationRadarCurrent = Utility.readPref(context, "ALERT_NOTIFICATION_RADAR$jStr", "false")
        alertCcNotificationCurrent = Utility.readPref(context, "ALERT_CC" + jStr + "_NOTIFICATION", "false")
        alertSevenDayNotificationCurrent = Utility.readPref(context, "ALERT_7DAY_" + jStr + "_NOTIFICATION", "false")
        alertNotificationSoundCurrent = Utility.readPref(context, "ALERT_NOTIFICATION_SOUND$jStr", "false")
        alertNotificationMcdCurrent = Utility.readPref(context, "ALERT_NOTIFICATION_MCD$jStr", "false")
        alertNotificationSwoCurrent = Utility.readPref(context, "ALERT_NOTIFICATION_SWO$jStr", "false")
        alertNotificationSpcfwCurrent = Utility.readPref(context, "ALERT_NOTIFICATION_SPCFW$jStr", "false")
        alertNotificationWpcmpdCurrent = Utility.readPref(context, "ALERT_NOTIFICATION_WPCMPD$jStr", "false")
        raw = Utility.readPref(context, "LOC" + jStr + "_TIMERAW", "")
        dst = Utility.readPref(context, "LOC" + jStr + "_TIMEDST", "")
        state = Utility.getWfoSiteName(wfo).split(",")[0]
        observation = Utility.readPref(context, "LOC" + jStr + "_OBSERVATION", "")
        isUS = us(x)
        addToListOfNames(name)
    }

    fun saveLocationToNewSlot(newLocNumInt: Int) {
        val iStr = (newLocNumInt + 1).toString()
        Utility.writePref(context, "LOC" + iStr + "_TIMERAW", raw)
        Utility.writePref(context, "LOC" + iStr + "_TIMEDST", dst)
        Utility.writePref(context, "ALERT" + iStr + "_NOTIFICATION", alertNotificationCurrent)
        Utility.writePref(context, "ALERT_CC" + iStr + "_NOTIFICATION", alertCcNotificationCurrent)
        Utility.writePref(context, "ALERT_7DAY_" + iStr + "_NOTIFICATION", alertSevenDayNotificationCurrent)
        Utility.writePref(context, "ALERT_NOTIFICATION_SOUND$iStr", alertNotificationSoundCurrent)
        Utility.writePref(context, "ALERT_NOTIFICATION_MCD$iStr", alertNotificationMcdCurrent)
        Utility.writePref(context, "ALERT_NOTIFICATION_SWO$iStr", alertNotificationSwoCurrent)
        Utility.writePref(context, "ALERT_NOTIFICATION_SPCFW$iStr", alertNotificationSpcfwCurrent)
        Utility.writePref(context, "ALERT_NOTIFICATION_WPCMPD$iStr", alertNotificationWpcmpdCurrent)
        Utility.writePref(context, "ALERT_NOTIFICATION_RADAR$iStr", alertNotificationRadarCurrent)
        Utility.writePref(context, "LOC" + iStr + "_X", x)
        Utility.writePref(context, "LOC" + iStr + "_Y", y)
        Utility.writePref(context, "LOC" + iStr + "_LABEL", name)
        Utility.writePref(context, "COUNTY$iStr", countyCurrent)
        Utility.writePref(context, "ZONE$iStr", zoneCurrent)
        Utility.writePref(context, "NWS$iStr", wfo)
        Utility.writePref(context, "RID$iStr", rid)
        Utility.writePref(context, "NWS" + iStr + "_STATE", nwsStateCurrent)
        Utility.writePref(context, "LOC" + iStr + "_OBSERVATION", observation)
        refreshLocationData(context)
    }

    fun updateObservation(observation: String) {
        this.observation = observation
        Utility.writePref(context, "LOC" + prefNumberString + "_OBSERVATION", observation)
    }

    val notification: Boolean get() = alertNotificationCurrent.startsWith("t")

    val notificationRadar: Boolean get() = alertNotificationRadarCurrent.startsWith("t")

    val ccNotification: Boolean get() = alertCcNotificationCurrent.startsWith("t")

    val sevenDayNotification: Boolean get() = alertSevenDayNotificationCurrent.startsWith("t")

    val sound: Boolean get() = alertNotificationSoundCurrent.startsWith("t")

    val notificationMcd: Boolean get() = alertNotificationMcdCurrent.startsWith("t")

    val notificationSwo: Boolean get() = alertNotificationSwoCurrent.startsWith("t")

    val notificationSpcFw: Boolean get() = alertNotificationSpcfwCurrent.startsWith("t")

    val notificationWpcMpd: Boolean get() = alertNotificationWpcmpdCurrent.startsWith("t")

    companion object {
        var numLocations: Int = 1
        var currentLocation: Int = 0
            private set

        val listOf: MutableList<String> = mutableListOf()

        fun us(xStr: String): Boolean = if (xStr.isNotEmpty()) {
            Character.isDigit(xStr[0])
        } else {
            true
        }

        fun addToListOfNames(name: String) {
            listOf.add(name)
        }

        fun checkCurrentLocationValidity() {
            if (currentLocation >= MyApplication.locations.size) {
                currentLocation = MyApplication.locations.lastIndex
                currentLocationStr = (currentLocation + 1).toString()
            }
        }

        private fun clearListOfNames() {
            listOf.clear()
        }

        private fun initNumLocations(context: Context) {
            val numberOfLocations = Utility.readPref(context, "LOC_NUM_INT", 1)
            setNumLocations(context, numberOfLocations)
        }

        private fun setNumLocations(context: Context, numLocations: Int) {
            Location.numLocations = numLocations
            Utility.writePref(context, "LOC_NUM_INT", numLocations)
        }

        var currentLocationStr: String
            get() = (currentLocation + 1).toString()
            set(currentLocationStr) {
                currentLocation = (currentLocationStr.toIntOrNull() ?: 0) - 1
            }

        val state: String get() = MyApplication.locations.getOrNull(currentLocation)?.state ?: "MI"

        val wfo: String get() = MyApplication.locations.getOrNull(currentLocation)?.wfo ?: "DTX"

        val rid: String get() = MyApplication.locations.getOrNull(currentLocation)?.rid ?: "DTX"

        val x: String get() = MyApplication.locations.getOrNull(currentLocation)?.x ?: "0.0"

        val y: String get() = MyApplication.locations.getOrNull(currentLocation)?.y ?: "-0.0"

        val latLon: LatLon get() = LatLon(x, y)

        val name: String get() = MyApplication.locations.getOrNull(currentLocation)?.name ?: ""

        fun getName(locNum: Int): String = MyApplication.locations.getOrNull(locNum)?.name ?: "0.0"

        fun getX(locNum: Int): String = MyApplication.locations.getOrNull(locNum)?.x ?: "0.0"

        fun getY(locNum: Int): String = MyApplication.locations.getOrNull(locNum)?.y ?: "-0.0"

        fun getRid(locNum: Int): String = MyApplication.locations.getOrNull(locNum)?.rid ?: "DTX"

        fun getWfo(locNum: Int): String = MyApplication.locations.getOrNull(locNum)?.wfo ?: "DTX"

        fun getObservation(locNum: Int): String = MyApplication.locations.getOrNull(locNum)?.observation ?: ""

        fun getLatLon(locNum: Int): LatLon = LatLon(getX(locNum), getY(locNum))

        fun getIdentifier(locNum: Int): String {
            val lat = MyApplication.locations.getOrNull(locNum)?.x ?: ""
            val lon = MyApplication.locations.getOrNull(locNum)?.y ?: ""
            return "LAT" + lat + "LON" + lon
        }

        val locationIndex: Int get() = currentLocation

        fun isUS(locationNumber: Int): Boolean =
            MyApplication.locations.getOrNull(locationNumber)?.isUS
                ?: true

        fun isUS(locationNumberString: String): Boolean =
            MyApplication.locations[locationNumberString.toInt() - 1].isUS

        val isUS: Boolean get() = MyApplication.locations.getOrNull(currentLocation)?.isUS ?: true

        fun getRid(context: Context, locNum: String): String =
            Utility.readPref(context, "RID$locNum", "")

        fun refreshLocationData(context: Context) {
            initNumLocations(context)
            MyApplication.locations.clear()
            clearListOfNames()
            (0 until numLocations).mapTo(MyApplication.locations) { Location(context, it) }
            addToListOfNames(ADD_LOC_STR)
            checkCurrentLocationValidity()
        }

        private fun getWfoRadarSiteFromPoint(location: LatLon): List<String> {
            val pointData = ("https://api.weather.gov/points/" + location.latString + "," + location.lonString).getNwsHtml()
            // "cwa": "IWX",
            // "radarStation": "KGRR"
            val wfo = pointData.parse("\"cwa\": \"(.*?)\"")
            var radarStation = pointData.parse("\"radarStation\": \"(.*?)\"")
            radarStation = UtilityString.getLastXChars(radarStation, 3)
            return listOf(wfo, radarStation)
        }

        fun locationSave(
            context: Context,
            locNum: String,
            xStr: String,
            yStr: String,
            labelStr: String
        ): String {
            if (xStr == "" || yStr == "" || labelStr == "") {
                return "Location label, latitude, and longitude all must have valid values, please try again."
            }
            val locNumToSave: Int
            val locNumInt = locNum.toIntOrNull() ?: 0
            val locNumIntCurrent = numLocations
            locNumToSave = if (locNumInt == locNumIntCurrent + 1) {
                locNumInt
            } else {
                locNumIntCurrent
            }
            Utility.writePref(context, "LOC" + locNum + "_X", xStr)
            Utility.writePref(context, "LOC" + locNum + "_Y", yStr)
            Utility.writePref(context, "LOC" + locNum + "_LABEL", labelStr)
            var wfo = ""
            var radarSite = ""
            if (us(xStr)) {
                setNumLocations(context, locNumToSave)
                val wfoAndRadar =  getWfoRadarSiteFromPoint(LatLon(xStr, yStr))
                wfo = wfoAndRadar[0]
                radarSite = wfoAndRadar[1]
                if (wfo == "") {
                    wfo = UtilityLocation.getNearestOffice( "WFO", LatLon(xStr, yStr)).toLowerCase(Locale.US)
                }
                if (radarSite == "") {
                    radarSite = UtilityLocation.getNearestOffice( "RADAR", LatLon(xStr, yStr))
                }
                // CT shows mosaic not nexrad so the old way is needed
                if (radarSite == "") {
                    radarSite = GlobalDictionaries.wfoToRadarSite[wfo.toUpperCase(Locale.US)] ?: ""
                }
                Utility.writePref(context, "RID$locNum", radarSite.toUpperCase(Locale.US))
                Utility.writePref(context, "NWS$locNum", wfo.toUpperCase(Locale.US)
                )
            } else if (xStr.contains("CANADA")) {
                var tmpLatLon = LatLonStr()
                if (xStr.length < 12) {
                    // if we are here then the user used the submenu
                    // need to calculate lat/lon first as get rid is now coded to parse on ":" for both x/y
                    // first check if the label is present in the database
                    if (UtilityCanada.isLabelPresent(labelStr)) {
                        tmpLatLon = UtilityCanada.getLatLonFromLabel(labelStr)
                    }
                }
                var prov = ""
                val parseProv = xStr.split(":").dropLastWhile { it.isEmpty() }
                if (parseProv.isNotEmpty()) prov = parseProv[1]
                var id = ""
                val parseId = yStr.split(":").dropLastWhile { it.isEmpty() }
                if (parseId.isNotEmpty()) {
                    id = parseId[0]
                }
                if (xStr.length > 12) {
                    tmpLatLon.latStr = parseProv[2]
                    tmpLatLon.lonStr = parseId[1]
                }
                Utility.writePref(
                    context,
                    "LOC" + locNum + "_X",
                    "CANADA" + ":" + prov + ":" + tmpLatLon.latStr
                )
                Utility.writePref(context, "LOC" + locNum + "_Y", id + ":" + tmpLatLon.lonStr)
                setNumLocations(context, locNumToSave)
                radarSite = UtilityCanada.getRadarSite(xStr, yStr)
                Utility.writePref(context, "RID$locNum", radarSite.toUpperCase(Locale.US))
                Utility.writePref(context, "NWS" + locNum + "_STATE", prov)
                Utility.writePref(context, "ZONE$locNum", "")
                Utility.writePref(context, "COUNTY$locNum", "")
                Utility.writePref(context, "NWS$locNum", "")
            }
            refreshLocationData(context)
            setCurrentLocationStr(context, locNum)
            return "Saving location $locNum as $labelStr ($xStr,$yStr) " + wfo.toUpperCase(
                Locale.US
            ) + "(" + radarSite.toUpperCase(Locale.US) + ")"
        }

        private fun setCurrentLocationStr(context: Context, locNum: String) {
            Utility.writePref(context, "CURRENT_LOC_FRAGMENT", locNum)
            currentLocationStr = locNum
        }

        fun deleteLocation(context: Context, locToDeleteStr: String) {
            val locToDeleteInt = locToDeleteStr.toIntOrNull() ?: 0
            val locNumIntCurrent = numLocations
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
                    val nwsStateCurrent = Utility.readPref(context, "NWS" + jStr + "_STATE", "")
                    val alertNotificationCurrent = Utility.readPref(context, "ALERT" + jStr + "_NOTIFICATION", "false")
                    val alertNotificationRadarCurrent = Utility.readPref(context, "ALERT_NOTIFICATION_RADAR$jStr", "false")
                    val alertCcNotificationCurrent = Utility.readPref(context, "ALERT_CC" + jStr + "_NOTIFICATION", "false")
                    val alert7Day1NotificationCurrent = Utility.readPref(context, "ALERT_7DAY_" + jStr + "_NOTIFICATION", "false")
                    val alertNotificationSoundCurrent = Utility.readPref(context, "ALERT_NOTIFICATION_SOUND$jStr", "false")
                    val alertNotificationMcdCurrent = Utility.readPref(context, "ALERT_NOTIFICATION_MCD$jStr", "false")
                    val alertNotificationSwoCurrent = Utility.readPref(context, "ALERT_NOTIFICATION_SWO$jStr", "false")
                    val alertNotificationSpcfwCurrent = Utility.readPref(context, "ALERT_NOTIFICATION_SPCFW$jStr", "false")
                    val alertNotificationWpcmpdCurrent = Utility.readPref(context, "ALERT_NOTIFICATION_WPCMPD$jStr", "false")
                    val raw = Utility.readPref(context, "LOC" + jStr + "_TIMERAW", "")
                    val dst = Utility.readPref(context, "LOC" + jStr + "_TIMEDST", "")
                    Utility.writePref(context, "LOC" + iStr + "_TIMERAW", raw)
                    Utility.writePref(context, "LOC" + iStr + "_TIMEDST", dst)
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
                    Utility.writePref(context, "NWS" + iStr + "_STATE", nwsStateCurrent)
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
            val locFragCurrentInt = currentLocation
            if (locToDeleteInt == (locFragCurrentInt + 1)) {
                Utility.writePref(context, "CURRENT_LOC_FRAGMENT", "1")
                currentLocationStr = "1"
            } else if (locFragCurrentInt > locToDeleteInt) {
                val shiftNum = (locFragCurrentInt - 1).toString()
                Utility.writePref(context, "CURRENT_LOC_FRAGMENT", shiftNum)
                currentLocationStr = shiftNum
            }
            val widgetLocNum = Utility.readPref(context, "WIDGET_LOCATION", "1")
            val widgetLocNumInt = widgetLocNum.toIntOrNull() ?: 0
            if (locToDeleteInt == widgetLocNumInt) {
                Utility.writePref(context, "WIDGET_LOCATION", "1")
            } else if (widgetLocNumInt > locToDeleteInt) {
                val shiftNum = (widgetLocNumInt - 1).toString()
                Utility.writePref(context, "WIDGET_LOCATION", shiftNum)
            }
            refreshLocationData(context)
        }

        private const val ADD_LOC_STR = "Add Location..."
    }
}



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

import joshuatee.wx.Extensions.parse
import joshuatee.wx.radar.RID
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityTimeSunMoon
import java.time.*
import java.time.format.DateTimeFormatter

class ObjectDateTime() {

    var dateTime: LocalDateTime = LocalDateTime.now()

    constructor(dateTime: LocalDateTime) : this() {
        this.dateTime = dateTime
    }

    constructor(hours: Int, minutes: Int) : this() {
        this.dateTime = LocalDate.now().atTime(hours, minutes)
    }

    override fun toString(): String = dateTime.toString()

//    fun addDays(d: Long) {
//        dateTime = dateTime.plusDays(d)
//    }

    fun addHours(d: Long) {
        dateTime = dateTime.plusHours(d)
    }

    // toSeconds() changed to getSeconds()
    fun isAfter(dt: ObjectDateTime): Boolean = Duration.between(dateTime, dt.get()).seconds < 0

    fun isBefore(dt: ObjectDateTime): Boolean = Duration.between(dateTime, dt.get()).seconds > 0

    fun format(pattern: String): String = dateTime.format(DateTimeFormatter.ofPattern(pattern))

    // same as ZoneOffset.UTC
    // milliseconds since epoch
    fun toEpochMilli(): Long = dateTime.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli()

    val now: LocalDateTime get() = LocalDateTime.now()

    fun get(): LocalDateTime = dateTime

    companion object {

        fun parse(timeString: String, pattern: String): ObjectDateTime = try {
            ObjectDateTime(LocalDateTime.parse(timeString, DateTimeFormatter.ofPattern(pattern)))
        } catch (e: Exception) {
            UtilityLog.d("wx", "failed to parse:$timeString $pattern")
            UtilityLog.handleException(e)
            ObjectDateTime()
        }

        private fun nowUtc(): ObjectDateTime = ObjectDateTime(getCurrentTimeInUTC())

        fun from(year: Int, month: Int, day: Int): ObjectDateTime = ObjectDateTime(LocalDateTime.of(year, month, day, 0, 0))

        fun fromObs(time: String): ObjectDateTime {
            // time comes in as follows 2018.02.11 2353 UTC
            // https://en.wikipedia.org/wiki/ISO_8601
            val returnTime = time.trim()
                                    .replace(" UTC", "")
                                    .replace(".", "")
                                    .replace(" ", " ") + "00"
            // time should now be as "20220225T095300.000Z"
            // text has a timezone "Z" so 2nd arg is null
            // time converted to the following to parse 20220226 115300
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss")
            val dateTime: LocalDateTime = try {
                LocalDateTime.parse(returnTime, formatter)
            } catch (e: Exception) {
                UtilityLog.handleException(e)
                getCurrentTimeInUTC()
            }
            return ObjectDateTime(dateTime)
        }

        fun getCurrentTimeInUTC(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)

        // is t1 greater then t2 by m minutes
        fun timeDifference(t1: LocalDateTime, t2: LocalDateTime, m: Int): Boolean = Duration.between(t1, t2).toMinutes() > m * -1

        //
        // Misc
        //

        // UtilityWidget / WeatherDataProviderObserver / LocationFragment / UtilityModels
        // BackgroundFetch / ObjectPendingIntents / UtilityNotification / UtilityNotificationSpc
        // DownloadTimer / WXGLRadarActivity / WXGLRadarActivityMultiPane / WXGLRadarActivityNew
        // RecordingSession / TelecineService / ObjectDatePicker / TouchImageView2
        fun currentTimeMillis(): Long = System.currentTimeMillis() // Long

        // ForecastActivity / UtilityTimeSunMoon
        fun gmtTime(): String = nowUtc().format("MM/dd/yyyy HH:mm")

        // SpotterReportsActivity
        fun gmtTime(format: String): String = nowUtc().format(format)

        // UtilityPlayList / SettingsColorPaletteEditor / ObjectWidgetCCLegacy / UtilityShare
        fun getDateAsString(format: String): String = ObjectDateTime().format(format)

        // this class / AlertReceiver / WXJobService / NexradRenderUI
        fun getCurrentLocalTimeAsString(): String = ObjectDateTime().format("yyyy-MM-dd HH:mm:ss")

        // hourly old / UtilityRadarUI
        fun getYear(): Int = ObjectDateTime().dateTime.year

        private fun getHour(): Int = ObjectDateTime().dateTime.hour

        // UtilityModels
        // fun offsetFromUtcInSeconds(): Int = ZonedDateTime.now().offset.totalSeconds

        // UtilityModelWpcGefsInputOutput
        fun currentHourInUtc(): Int = OffsetDateTime.now(ZoneOffset.UTC).hour

        // UtilityNotificationUtils
        val currentHourIn24: Int
            get() {
                return getHour()
            }

        fun convertFromUtcForMetar(time: String): String = try {
            val ccTime = parse(time.trim(), "yyyy.MM.dd HHmm")
            val localZone = ZoneId.systemDefault()
            val localTime = ccTime.get().atZone(ZoneOffset.UTC).withZoneSameInstant(localZone)
            val formatter = DateTimeFormatter.ofPattern("MM-dd h:mm a")
            localTime.format(formatter)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
            time
        }

        //
        // Nexrad Radar
        //
        fun radarTime(volumeScanDate: Short, volumeScanTime: Int): String {
            val sec = ((volumeScanDate - 1) * 60 * 60 * 24 + volumeScanTime).toLong()
            val milli = sec * 1000
            val nowFromMilli = ObjectDateTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(milli), ZoneId.systemDefault()))
            return nowFromMilli.format("E MMM dd HH:mm:ss YYYY") // + ZoneId.systemDefault().toString()
        }

        // WAS Tue Nov 08 12:32:53 EST 2022
        // NOW Tue Nov 08 12:32:53 2022
        // return a time string based on Level 2 radar data
        fun radarTimeL2(days: Short, milliSeconds: Int): String {
            val sec = (days - 1).toLong() * 24 * 3600 * 1000 + milliSeconds
            val nowFromMilli = ObjectDateTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(sec), ZoneId.systemDefault()))
            return nowFromMilli.format("E MMM dd HH:mm:ss YYYY") // + ZoneId.systemDefault().toString()
        }

        fun isRadarTimeOld(radarTime: String): Boolean {
            val radarTimeComponents = radarTime.split(":")
            if (radarTimeComponents.size < 3) {
                return false
            }
            val radarTimeHours = To.int(radarTimeComponents[0])
            val radarTimeMinutes = To.int(radarTimeComponents[1])
            val radarTimeTotalMinutes = radarTimeHours * 60 + radarTimeMinutes
            val currentTime = getCurrentLocalTimeAsString().split(" ")[1]
            val currentTimeComponents = currentTime.split(":")
            if (currentTimeComponents.size < 3) {
                return false
            }
            val currentTimeHours = To.int(currentTimeComponents[0])
            val currentTimeMinutes = To.int(currentTimeComponents[1])
            val currentTimeTotalMinutes = currentTimeHours * 60 + currentTimeMinutes
            if (currentTimeTotalMinutes < 30) {
                return false
            }
            if (radarTimeTotalMinutes > currentTimeTotalMinutes) {
                return true
            }
            if (radarTimeTotalMinutes < (currentTimeTotalMinutes - 20)) {
                return true
            }
            return false
        }

        fun isVtecCurrent(vtec: String): Boolean {
            // example 190512T1252Z-190512T1545Z
            val vtecTimeRange = vtec.parse("-([0-9]{6}T[0-9]{4})Z")
            val vtecTime = decodeVtecTime(vtecTimeRange)
            val currentTime = decodeVtecTime(getGmtTimeForVtec())
            return currentTime.isBefore(vtecTime)
        }

        private fun decodeVtecTime(timeRange: String): ObjectDateTime = try {
            val dateTime = LocalDateTime.parse(timeRange, DateTimeFormatter.ofPattern("yyMMdd'T'HHmm"))
            ObjectDateTime(dateTime)
        } catch (e: Exception) {
            val objectDateTime = nowUtc()
            objectDateTime.addHours(1)
            objectDateTime
        }

        private fun getGmtTimeForVtec(): String = nowUtc().format("yyMMdd'T'HHmm")

        // UtilityHourly
        fun translateTimeForHourly(originalTime: String): String {
            val timeNoTz = originalTime.split("-").dropLast(1).joinToString("-")
            val objectDateTime = parse(timeNoTz, "yyyy'-'MM'-'dd'T'HH':'mm':'ss")
            return objectDateTime.format("E HH")
        }

        //
        // Models
        //
        // used by SPC HREF, ESRL, and NSSL WRF
        fun generateModelRuns(time: String, hours: Int, fromPattern: String, toPattern: String, totalNumber: Int): List<String> {
            val listRun = mutableListOf<String>()
            val currentTime = parse(time, fromPattern)
            listRun.add(currentTime.format(toPattern))
            for (it in 0 until totalNumber) {
                currentTime.addHours(-1 * hours.toLong())
                listRun.add(currentTime.format(toPattern))
            }
            return listRun
        }

        // used in ObjectMetar for CC icon
        fun isDaytime(location: RID): Boolean {
            val sunTimes = UtilityTimeSunMoon.getSunriseSunsetFromObs(location)
            val sunRiseDate = sunTimes[0]
            val sunSetDate = sunTimes[1]
            val currentTime  = ObjectDateTime()
            val fallsBetween = currentTime.isAfter(sunRiseDate) && currentTime.isBefore(sunSetDate)
            val currentTimeTomorrow = ObjectDateTime()
            currentTimeTomorrow.addHours(24)
            val fallsBetweenTomorrow = currentTimeTomorrow.isAfter(sunRiseDate) && currentTimeTomorrow.isBefore(sunSetDate)
            return fallsBetween || fallsBetweenTomorrow
        }
    }
}

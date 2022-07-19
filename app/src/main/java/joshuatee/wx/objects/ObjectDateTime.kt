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

import joshuatee.wx.util.UtilityLog
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class ObjectDateTime {

    var dateTime: LocalDateTime = LocalDateTime.now()

    companion object {

        fun fromObs(time: String): ObjectDateTime {
            // time comes in as follows 2018.02.11 2353 UTC
            // https://en.wikipedia.org/wiki/ISO_8601
            var returnTime = time.trim()
            returnTime = returnTime.replace(" UTC", "")
            returnTime = returnTime.replace(".", "")
            // returnTime = returnTime.replace(" ", "T") + "00.000Z"
            returnTime = returnTime.replace(" ", " ") + "00"
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
            val objectDateTime = ObjectDateTime()
            objectDateTime.dateTime = dateTime
            return objectDateTime
        }

        fun getCurrentTimeInUTC(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)

        // is t1 greater then t2 by m minutes
        fun timeDifference(t1: LocalDateTime, t2: LocalDateTime, m: Int): Boolean {
            val duration = Duration.between(t1, t2)
            // minutes between from and to
            return duration.toMinutes() > m * -1
        }
    }
}

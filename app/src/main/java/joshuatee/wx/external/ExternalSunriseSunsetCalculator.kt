/*
 * Copyright 2008-2009 Mike Reedell / LuckyCatLabs.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// https://github.com/mikereedell/sunrisesunsetlib-java

package joshuatee.wx.external

import java.util.Calendar
import java.util.TimeZone

class ExternalSunriseSunsetCalculator(val location: ExternalSunriseLocation, timeZone: TimeZone) {

    private val calculator = ExternalSolarEventCalculator(location, timeZone)

    /**
     * Returns the official sunrise (90deg 50', 90.8333deg) for the given date.
     *
     * @param date
     * `Calendar` object containing the date to compute the official sunrise for.
     * @return the official sunrise time as a Calendar
     */
    fun getOfficialSunriseCalendarForDate(date: Calendar): Calendar = calculator.computeSunriseCalendar(ExternalZenith.OFFICIAL, date)!!

    /**
     * Returns the official sunrise (90deg 50', 90.8333deg) for the given date.
     *
     * @param date
     * `Calendar` object containing the date to compute the official sunset for.
     * @return the official sunset time as a Calendar
     */
    fun getOfficialSunsetCalendarForDate(date: Calendar): Calendar = calculator.computeSunsetCalendar(ExternalZenith.OFFICIAL, date)!!
}

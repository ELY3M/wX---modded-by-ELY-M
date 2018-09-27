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

//import ExternalSolarEventCalculator;
//import ExternalSunriseLocation;

/**
 * Public interface for getting the various types of sunrise/sunset.
 */
class ExternalSunriseSunsetCalculator
/**
 * Constructs a new `SunriseSunsetCalculator` with the given `ExternalSunriseLocation`
 *
 * @param ExternalSunriseLocation
 * `ExternalSunriseLocation` object containing the Latitude/Longitude of the ExternalSunriseLocation to compute
 * the sunrise/sunset for.
 * @param timeZoneIdentifier
 * String identifier for the timezone to compute the sunrise/sunset times in. In the form
 * "America/New_York". Please see the zi directory under the JDK installation for supported
 * time zones.
 */
/*public ExternalSunriseSunsetCalculator(ExternalSunriseLocation ExternalSunriseLocation, String timeZoneIdentifier) {
        this.ExternalSunriseLocation = ExternalSunriseLocation;
        this.calculator = new ExternalSolarEventCalculator(ExternalSunriseLocation, timeZoneIdentifier);
    }*/

/**
 * Constructs a new `SunriseSunsetCalculator` with the given `ExternalSunriseLocation`
 *
 * @param ExternalSunriseLocation
 * `ExternalSunriseLocation` object containing the Latitude/Longitude of the ExternalSunriseLocation to compute
 * the sunrise/sunset for.
 * @param timeZone
 * timezone to compute the sunrise/sunset times in.
 */
(
        /**
         * Computes the sunrise for an arbitrary declination.
         *
         * @param latitude
         * @param longitude
         * Coordinates for the ExternalSunriseLocation to compute the sunrise/sunset for.
         * @param timeZone
         * timezone to compute the sunrise/sunset times in.
         * @param date
         * `Calendar` object containing the date to compute the official sunset for.
         * @param degrees
         * Angle under the horizon for which to compute sunrise. For example, "civil sunrise"
         * corresponds to 6 degrees.
         * @return the requested sunset time as a `Calendar` object.
         */

        /* public static Calendar getSunrise(double latitude, double longitude, TimeZone timeZone, Calendar date, double degrees) {
    	ExternalSolarEventCalculator solarEventCalculator = new ExternalSolarEventCalculator(new ExternalSunriseLocation(latitude, longitude), timeZone);
        return solarEventCalculator.computeSunriseCalendar(new ExternalZenith(90 - degrees), date);
    }*/

        /**
         * Computes the sunset for an arbitrary declination.
         *
         * @param latitude
         * @param longitude
         * Coordinates for the ExternalSunriseLocation to compute the sunrise/sunset for.
         * @param timeZone
         * timezone to compute the sunrise/sunset times in.
         * @param date
         * `Calendar` object containing the date to compute the official sunset for.
         * @param degrees
         * Angle under the horizon for which to compute sunrise. For example, "civil sunset"
         * corresponds to 6 degrees.
         * @return the requested sunset time as a `Calendar` object.
         */

        /* public static Calendar getSunset(double latitude, double longitude, TimeZone timeZone, Calendar date, double degrees) {
    	ExternalSolarEventCalculator solarEventCalculator = new ExternalSolarEventCalculator(new ExternalSunriseLocation(latitude, longitude), timeZone);
        return solarEventCalculator.computeSunsetCalendar(new ExternalZenith(90 - degrees), date);
    }*/

        /**
         * Returns the ExternalSunriseLocation where the sunrise/sunset is calculated for.
         *
         * @return `ExternalSunriseLocation` object representing the ExternalSunriseLocation of the computed sunrise/sunset.
         */
        val location: ExternalSunriseLocation, timeZone: TimeZone) {

    private val calculator: ExternalSolarEventCalculator = ExternalSolarEventCalculator(location, timeZone)

    /**
     * Returns the astronomical (108deg) sunrise for the given date.
     *
     * @param date
     * `Calendar` object containing the date to compute the astronomical sunrise for.
     * @return the astronomical sunrise time in HH:MM (24-hour clock) form.
     */
    /* public String getAstronomicalSunriseForDate(Calendar date) {
        return calculator.computeSunriseTime(ExternalZenith.ASTRONOMICAL, date);
    }*/

    /**
     * Returns the astronomical (108deg) sunrise for the given date.
     *
     * @param date
     * `Calendar` object containing the date to compute the astronomical sunrise for.
     * @return the astronomical sunrise time as a Calendar
     */
    /*public Calendar getAstronomicalSunriseCalendarForDate(Calendar date) {
        return calculator.computeSunriseCalendar(ExternalZenith.ASTRONOMICAL, date);
    }*/

    /**
     * Returns the astronomical (108deg) sunset for the given date.
     *
     * @param date
     * `Calendar` object containing the date to compute the astronomical sunset for.
     * @return the astronomical sunset time in HH:MM (24-hour clock) form.
     */
    /*public String getAstronomicalSunsetForDate(Calendar date) {
        return calculator.computeSunsetTime(ExternalZenith.ASTRONOMICAL, date);
    }*/

    /**
     * Returns the astronomical (108deg) sunset for the given date.
     *
     * @param date
     * `Calendar` object containing the date to compute the astronomical sunset for.
     * @return the astronomical sunset time as a Calendar
     */
    /* public Calendar getAstronomicalSunsetCalendarForDate(Calendar date) {
        return calculator.computeSunsetCalendar(ExternalZenith.ASTRONOMICAL, date);
    }*/

    /**
     * Returns the nautical (102deg) sunrise for the given date.
     *
     * @param date
     * `Calendar` object containing the date to compute the nautical sunrise for.
     * @return the nautical sunrise time in HH:MM (24-hour clock) form.
     */
    /* public String getNauticalSunriseForDate(Calendar date) {
        return calculator.computeSunriseTime(ExternalZenith.NAUTICAL, date);
    }*/

    /**
     * Returns the nautical (102deg) sunrise for the given date.
     *
     * @param date
     * `Calendar` object containing the date to compute the nautical sunrise for.
     * @return the nautical sunrise time as a Calendar
     */
    /* public Calendar getNauticalSunriseCalendarForDate(Calendar date) {
        return calculator.computeSunriseCalendar(ExternalZenith.NAUTICAL, date);
    }*/

    /**
     * Returns the nautical (102deg) sunset for the given date.
     *
     * @param date
     * `Calendar` object containing the date to compute the nautical sunset for.
     * @return the nautical sunset time in HH:MM (24-hour clock) form.
     */
    /* public String getNauticalSunsetForDate(Calendar date) {
        return calculator.computeSunsetTime(ExternalZenith.NAUTICAL, date);
    }*/

    /**
     * Returns the nautical (102deg) sunset for the given date.
     *
     * @param date
     * `Calendar` object containing the date to compute the nautical sunset for.
     * @return the nautical sunset time as a Calendar
     */
    /*public Calendar getNauticalSunsetCalendarForDate(Calendar date) {
        return calculator.computeSunsetCalendar(ExternalZenith.NAUTICAL, date);
    }*/

    /**
     * Returns the civil sunrise (twilight, 96deg) for the given date.
     *
     * @param date
     * `Calendar` object containing the date to compute the civil sunrise for.
     * @return the civil sunrise time in HH:MM (24-hour clock) form.
     */
    /* public String getCivilSunriseForDate(Calendar date) {
        return calculator.computeSunriseTime(ExternalZenith.CIVIL, date);
    }*/

    /**
     * Returns the civil sunrise (twilight, 96deg) for the given date.
     *
     * @param date
     * `Calendar` object containing the date to compute the civil sunrise for.
     * @return the civil sunrise time as a Calendar
     */
    /* public Calendar getCivilSunriseCalendarForDate(Calendar date) {
        return calculator.computeSunriseCalendar(ExternalZenith.CIVIL, date);
    }*/

    /**
     * Returns the civil sunset (twilight, 96deg) for the given date.
     *
     * @param date
     * `Calendar` object containing the date to compute the civil sunset for.
     * @return the civil sunset time in HH:MM (24-hour clock) form.
     */
    /*public String getCivilSunsetForDate(Calendar date) {
        return calculator.computeSunsetTime(ExternalZenith.CIVIL, date);
    }*/

    /**
     * Returns the civil sunset (twilight, 96deg) for the given date.
     *
     * @param date
     * `Calendar` object containing the date to compute the civil sunset for.
     * @return the civil sunset time as a Calendar
     */
    /*public Calendar getCivilSunsetCalendarForDate(Calendar date) {
        return calculator.computeSunsetCalendar(ExternalZenith.CIVIL, date);
    }*/

    /**
     * Returns the official sunrise (90deg 50', 90.8333deg) for the given date.
     *
     * @param date
     * `Calendar` object containing the date to compute the official sunrise for.
     * @return the official sunrise time in HH:MM (24-hour clock) form.
     */
    /*public String getOfficialSunriseForDate(Calendar date) {
        return calculator.computeSunriseTime(ExternalZenith.OFFICIAL, date);
    }*/

    /**
     * Returns the official sunrise (90deg 50', 90.8333deg) for the given date.
     *
     * @param date
     * `Calendar` object containing the date to compute the official sunrise for.
     * @return the official sunrise time as a Calendar
     */
    fun getOfficialSunriseCalendarForDate(date: Calendar): Calendar {
        return calculator.computeSunriseCalendar(ExternalZenith.OFFICIAL, date)!!
    }

    /**
     * Returns the official sunrise (90deg 50', 90.8333deg) for the given date.
     *
     * @param date
     * `Calendar` object containing the date to compute the official sunset for.
     * @return the official sunset time in HH:MM (24-hour clock) form.
     */
    /* public String getOfficialSunsetForDate(Calendar date) {
        return calculator.computeSunsetTime(ExternalZenith.OFFICIAL, date);
    }*/

    /**
     * Returns the official sunrise (90deg 50', 90.8333deg) for the given date.
     *
     * @param date
     * `Calendar` object containing the date to compute the official sunset for.
     * @return the official sunset time as a Calendar
     */
    fun getOfficialSunsetCalendarForDate(date: Calendar): Calendar {
        return calculator.computeSunsetCalendar(ExternalZenith.OFFICIAL, date)!!
    }
}

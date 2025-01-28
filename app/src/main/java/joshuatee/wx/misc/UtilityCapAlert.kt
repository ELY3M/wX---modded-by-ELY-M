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

package joshuatee.wx.misc

import joshuatee.wx.parseMultiple

object UtilityCapAlert {

    fun times(capAlert: CapAlert): List<String> {
        val title: String
        val startTime: String
        var endTime = ""
        val wfo: String
        if (!capAlert.url.contains("urn:oid")) {
            if (capAlert.title.contains("until")) {
                val items =
                    capAlert.title.parseMultiple("(.*?) issued (.*?) until (.*?) by (.*?)$", 4)
                title = items[0]
                startTime = items[1]
                endTime = items[2]
                wfo = items[3]
            } else {
                val items = capAlert.title.parseMultiple("(.*?) issued (.*?) by (.*?)$", 3)
                title = items[0]
                startTime = items[1]
                wfo = items[2]
            }
        } else {
            when {
                capAlert.title.contains("expiring") -> {
                    val items = capAlert.title.parseMultiple(
                        "(.*?) issued (.*?) expiring (.*?) by (.*?)$",
                        4
                    )
                    title = items[0]
                    startTime = items[1]
                    endTime = items[2]
                    wfo = items[3]
                }

                capAlert.title.contains("until") -> {
                    val items =
                        capAlert.title.parseMultiple("(.*?) issued (.*?) until (.*?) by (.*?)$", 4)
                    title = items[0]
                    startTime = items[1]
                    endTime = items[2]
                    wfo = items[3]
                }

                else -> {
                    val items = capAlert.title.parseMultiple("(.*?) issued (.*?) by (.*?)$", 3)
                    title = items[0]
                    startTime = items[1]
                    wfo = items[2]
                }
            }
        }
        return listOf(startTime, endTime, title, wfo)
    }

    fun timesForCard(capAlert: CapAlert): List<String> {
        val title: String
        val startTime: String
        val endTime: String
        if (capAlert.title.contains("until")) {
            val items = capAlert.title.parseMultiple("(.*?) issued (.*?) until (.*?) by (.*?)$", 4)
            title = items[0]
            startTime = items[1]
            endTime = items[2]
        } else {
            val items = capAlert.title.parseMultiple("(.*?) issued (.*?) by (.*?)$", 3)
            title = items[0]
            startTime = items[1]
            endTime = ""
        }
        return listOf(startTime, endTime, title)
    }
}

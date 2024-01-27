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
//modded by ELY M. 

package joshuatee.wx.radar

import android.content.Context
import joshuatee.wx.getHtmlWithNewLine
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.DistanceUnit
import joshuatee.wx.objects.DownloadTimer
import joshuatee.wx.objects.LatLon
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityLog
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Suppress("SpellCheckingInspection")
object UtilitySpotter {

    internal val spotterList = mutableListOf<Spotter>()
    private val reportsList = mutableListOf<SpotterReport>()
    val timer = DownloadTimer("SPOTTER")
    internal var x = DoubleArray(1)
        private set
    internal var y = DoubleArray(1)
        private set

    private var Spotterlistbydist: String = ""

    // http://www.spotternetwork.org/feeds/csv.txt
    //
    //#uniq,icon,live camera,reportAt,lat,lon,callsign,active,moving,dir,phone,email,freq,note,first,last
    //2817;;1;;0;;2016-03-21 23:16:53;;37.6776390;;-97.2631760;;K0WFI;;1;;0;;0;
    //#uniq,icon,live camera,reportAt,lat,lon,callsign,active,moving,dir,phone,email,freq,note,first,last
    //2817;;1;;0;;2016-03-21 23:16:53;;37.6776390;;-97.2631760;;K0WFI;;1;;0;;0;;3163045901;;cox.net;;146.610-146.940/scannin;;K0WFI  ICTSkyWarn/Sedgwick Co. CERT;;f;;l
    //35960;;1;;0;;2016-03-21 23:16:56;;35.0608444;;-92.4547577;;;;1;;1;;105;;5735867445;;@yahoo.com;;;;IM is on yahoo ;;f;;l
    // strip out storm reports at bottom
    // thanks Landei
    // http://stackoverflow.com/questions/6720236/sorting-an-arraylist-of-objects-by-last-name-and-firstname-in-java

    fun get(): List<Spotter> {
        if (timer.isRefreshNeeded()) {
            spotterList.clear()
            reportsList.clear()
            val lats = mutableListOf<String>()
            val lons = mutableListOf<String>()
            var html = ("http://www.spotternetwork.org/feeds/csv.txt").getHtmlWithNewLine()
            val reportData = html.replace(".*?#storm reports".toRegex(), "")
            process(reportData)
            html = html.replace("#storm reports.*?$".toRegex(), "")
            val lines = html.split(GlobalVariables.newline).dropLastWhile { it.isEmpty() }
            lines.forEach { line ->
                val items = line.split(";;").dropLastWhile { it.isEmpty() }
                if (items.size > 15) {
                    spotterList.add(
                            Spotter(
                                items[0],
			items[1], 
			items[2], 
			items[3], 
			items[4], 
			items[5], 
			items[6], 
			items[7], 
			items[8], 
			items[9], 
			items[10], 
			items[11], 
			items[12], 
			items[13], 
			items[14], 
			items[15]
                            )
                    )
                    lats.add(items[4])
                    lons.add(items[5])
                }
            }
            if (lats.size == lons.size) {
                x = DoubleArray(lats.size)
                y = DoubleArray(lats.size)
                lats.indices.forEach {
                    x[it] = To.double(lats[it])
                    y[it] = To.double(lons[it]) * -1.0
                }
            } else {
                x = DoubleArray(1)
                y = DoubleArray(1)
                x[0] = 0.0
                y[0] = 0.0
            }
        }
        return spotterList
    }

    // need to return an array of x ( lat ) and an array of y ( lon ) where long is positive
    private fun process(text: String) {
        val lines = text.split(GlobalVariables.newline).dropLastWhile { it.isEmpty() }
        lines.forEach { line ->
            val items = line.split(";;").dropLastWhile { it.isEmpty() }
            if (items.size > 10 && !items[0].startsWith("#")) {
                reportsList.add(
		SpotterReport(
		items[9], 
		items[10], 
		items[5], 
		items[6], 
		items[8], 
		items[0], 
		items[3], 
		items[2], 
		items[7]
		)
		)

            }
        }
    }


//LatLon.distance(LatLon(locX, locY), LatLon(pointX, pointY), DistanceUnit.MILE)
//#uniq,icon,live camera,reportAt,lat,lon,callsign,active,moving,dir,phone,email,freq,note,first,last
    fun findClosestSpotter(location: LatLon): String {
        var text = ""
        var SpotterInfoString = ""
        val spotterinfo = mutableListOf<Spotter>()
        val html = ("http://www.spotternetwork.org/feeds/csv.txt").getHtmlWithNewLine()
        val reportData = html.replace(".*?#storm reports".toRegex(), "")
        process(reportData)
        text = html.replace("#storm reports.*?$".toRegex(), "")
        val lines = text.split(GlobalVariables.newline).dropLastWhile { it.isEmpty() }
        lines.forEach { line->
            val items = line.split(";;").dropLastWhile { it.isEmpty() }
            if (items.size > 15) {
                spotterinfo.add(
		Spotter(
		items[0], 
		items[1], 
		items[2], 
		items[3], 
		items[4], 
		items[5], 
		items[6], 
		items[7], 
		items[8], 
		items[9], 
		items[10], 
		items[11], 
		items[12], 
		items[13], 
		items[14], 
		items[15]
		)
		)
            }
        }

        var shortestDistance = 10.0
        var currentDistance: Double
        var bestSpotter = -1
        var idleTime: String

        spotterinfo.indices.forEach {
            currentDistance = LatLon.distance(location, LatLon(spotterinfo[it].lat, spotterinfo[it].lon), DistanceUnit.MILE)
            if (currentDistance < shortestDistance) {
                shortestDistance = currentDistance
                bestSpotter = it
                idleTime = countTime(spotterinfo[it].reportAt)
                SpotterInfoString =
                        "Name: "+spotterinfo[it].firstName +" "+spotterinfo[it].lastName+"\nLocation: "+spotterinfo[it].lat+" "+spotterinfo[it].lon+"\nReport at: "+spotterinfo[it].reportAt+"\nIdle Time: "+idleTime+"\nEmail: "+spotterinfo[it].email+"\nPhone: "+spotterinfo[it].phone+"\nCallsign: "+spotterinfo[it].callsign+"\nFreq: "+spotterinfo[it].freq+"\nNote: "+spotterinfo[it].note+"\n============================\n"



            }
        }
        UtilityLog.d("wx-elys", "Spotter Info: "+SpotterInfoString)
        return if (bestSpotter == -1) {
            "Spotter Info not available!"
        } else {
            SpotterInfoString
        }
    }


fun countTime(reportAt: String): String {
    //get now date in GMT zone
    val cal: Calendar = Calendar.getInstance()
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    //force all times to be GMT
    format.setTimeZone(TimeZone.getTimeZone("GMT"))

    val nowDateTime = format.format(cal.getTime())
    val reporttime = format.parse(reportAt)
    val now = format.parse(nowDateTime)

    UtilityLog.d("wx-elys", "Spotter reportAt: "+reportAt)
    UtilityLog.d("wx-elys", "Spotter Now Time: "+nowDateTime)
    UtilityLog.d("wx-elys", "Spotter reporttime: "+reporttime)
    UtilityLog.d("wx-elys", "Spotter now: "+now)

    //in milliseconds
    val diff = now.getTime() - reporttime.getTime()
    val diffSeconds = diff / 1000 % 60
    val diffMinutes = diff / (60 * 1000) % 60
    val diffHours = diff / (60 * 60 * 1000) % 24
    val diffDays = diff / (24 * 60 * 60 * 1000)
    val idleDateTime = "$diffDays days, $diffHours hrs, $diffMinutes mins, $diffSeconds secs"
    UtilityLog.d("wx-elys", "Spotter Idle Time: "+idleDateTime)
    return idleDateTime
}

    val reports: List<SpotterReport>
        get() = reportsList

}

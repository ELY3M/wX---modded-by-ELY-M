/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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

package joshuatee.wx.radar

import android.content.Context
import joshuatee.wx.Extensions.getHtmlSep
import joshuatee.wx.objects.DistanceUnit

//import java.util.Collections
//import java.util.Comparator

object UtilitySpotter {
    internal var spotterList = mutableListOf<Spotter>()
    private var reportsList = mutableListOf<SpotterReports>()
    private var initialized = false
    private var lastRefresh = 0.toLong()
    private const val REFRESH_LOC_MIN = 5
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
    val spotterData: MutableList<Spotter>
        get() {
            var currentTime = System.currentTimeMillis()
            val currentTimeSec = currentTime / 1000
            val refreshIntervalSec = (REFRESH_LOC_MIN * 60).toLong()
            if (currentTimeSec > lastRefresh + refreshIntervalSec || !initialized) {
                spotterList = mutableListOf()
                reportsList = mutableListOf()
                val latAl = mutableListOf<String>()
                val lonAl = mutableListOf<String>()
                var html = ("http://www.spotternetwork.org/feeds/csv.txt").getHtmlSep()
                val reportData = html.replace(".*?#storm reports".toRegex(), "")
                Spotterlistbydist = html
                processReportsData(reportData)
                html = html.replace("#storm reports.*?$".toRegex(), "")
                val htmlArr = html.split("<br>").dropLastWhile { it.isEmpty() }
                var tmpArr: List<String>
                htmlArr.forEach { line ->
                    tmpArr = line.split(";;").dropLastWhile { it.isEmpty() }
                    if (tmpArr.size > 15) {
                        spotterList.add(Spotter(tmpArr[0], tmpArr[1], tmpArr[2], tmpArr[3], tmpArr[4], tmpArr[5], tmpArr[6], tmpArr[7], tmpArr[8], tmpArr[9], tmpArr[10], tmpArr[11], tmpArr[12], tmpArr[13], tmpArr[14], tmpArr[15]))
                        latAl.add(tmpArr[4])
                        lonAl.add(tmpArr[5])
                    }
                }
                /*Collections.sort(spotterList, Comparator<Spotter> { p1, p2 ->
                    val res = p1.lastName.compareTo(p2.lastName, ignoreCase = true)
                    if (res != 0) {
                        return@Comparator res
                    }
                    p1.firstName.compareTo(p2.firstName, ignoreCase = true)
                })*/
                // if we need this use Kotlin instead: var sortedList = list.sortedWith(compareBy({ it.customProperty }))
                if (latAl.size == lonAl.size) {
                    x = DoubleArray(latAl.size)
                    y = DoubleArray(latAl.size)
                    latAl.indices.forEach {
                        x[it] = latAl[it].toDoubleOrNull() ?: 0.0
                        y[it] = -1.0 * (lonAl[it].toDoubleOrNull() ?: 0.0)
                    }
                } else {
                    x = DoubleArray(1)
                    y = DoubleArray(1)
                    x[0] = 0.0
                    y[0] = 0.0
                }
                initialized = true
                currentTime = System.currentTimeMillis()
                lastRefresh = currentTime / 1000
            }
            return spotterList
        }

    // need to return an array of x ( lat ) and an array of y ( lon ) where long is positive

    private fun processReportsData(txt: String) {
        val htmlArr = txt.split("<br>").dropLastWhile { it.isEmpty() }
        var tmpArr: List<String>
        htmlArr.forEach { line ->
            tmpArr = line.split(";;").dropLastWhile { it.isEmpty() }
            if (tmpArr.size > 10 && !tmpArr[0].startsWith("#")) {
                reportsList.add(SpotterReports(tmpArr[9], tmpArr[10], tmpArr[5], tmpArr[6], tmpArr[8], tmpArr[0], tmpArr[3], tmpArr[2], tmpArr[7]))

            }
        }
    }

//LatLon.distance(LatLon(locX, locY), LatLon(pointX, pointY), DistanceUnit.MILE)
//#uniq,icon,live camera,reportAt,lat,lon,callsign,active,moving,dir,phone,email,freq,note,first,last

    fun findClosestSpotter( location: LatLon): String {
        var text = Spotterlistbydist
        var SpotterInfoString = ""
        val spotterinfo = mutableListOf<Spotter>()
        text = text.replace("#storm reports.*?$".toRegex(), "")
        val htmlArr = text.split("<br>").dropLastWhile { it.isEmpty() }
        var tmpArr: List<String>
        htmlArr.forEach { line->
            tmpArr = line.split(";;").dropLastWhile { it.isEmpty() }
            if (tmpArr.size > 15) {
                spotterinfo.add(Spotter(tmpArr[0], tmpArr[1], tmpArr[2], tmpArr[3], tmpArr[4], tmpArr[5], tmpArr[6], tmpArr[7], tmpArr[8], tmpArr[9], tmpArr[10], tmpArr[11], tmpArr[12], tmpArr[13], tmpArr[14], tmpArr[15]))
            }
        }

        var shortestDistance = 4.13
        var currentDistance: Double
        var bestSpotter = -1

        spotterinfo.indices.forEach {
            //Log.i(TAG, "checking dist for: " + spotterinfo[it].firstName + " " + spotterinfo[it].lastName)
            currentDistance = LatLon.distance(location, LatLon(spotterinfo[it].lat, spotterinfo[it].lon), DistanceUnit.MILE)
            if (currentDistance < shortestDistance) {
                shortestDistance = currentDistance
                bestSpotter = it

                SpotterInfoString =
                        "Name: "+spotterinfo[it].firstName +" "+spotterinfo[it].lastName+"\nLocation: "+spotterinfo[it].lat+" "+spotterinfo[it].lon+"\nReport at: "+spotterinfo[it].reportAt+"\nEmail: "+spotterinfo[it].email+"\nPhone: "+spotterinfo[it].phone+"\nCallsign: "+spotterinfo[it].callsign+"\nFreq: "+spotterinfo[it].freq+"\nNote: "+spotterinfo[it].note+"\n============================\n"



            }
        }
        return if (bestSpotter == -1) {
            "Spotter Info not available!"
        } else {
            SpotterInfoString
        }


    }


    val spotterReports: List<SpotterReports>
        get() = reportsList
}

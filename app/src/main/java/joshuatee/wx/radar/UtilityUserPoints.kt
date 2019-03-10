/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import android.preference.PreferenceManager
import com.beust.klaxon.PathMatcher
import joshuatee.wx.objects.DistanceUnit
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import java.util.regex.Pattern


object UtilityUserPoints {
    internal var userPointsList = mutableListOf<Userpoints>()
    internal var name = ""
    internal var x: Double = 0.0
    internal var y: Double = 0.0



    fun getUserPoints(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        //return preferences.getString(key, value)!!
        preferences?.all?.forEach {
            val pattern = Pattern.compile("USERPOINTS_.*?")
            val m = pattern.matcher(it.key)
            if (m.find()) {
                //System.out.println("TAG " + m.group())
                UtilityLog.d("wx","Found Userpoint values: "+ it.key + ": " + it.value)
                //val getLatLon = it.value
                //val getLatLon = listOf<String>()
                val getLatLon: String = it.value.toString()
                //we need to split up the it.value//
                val latlonArr = getLatLon.split("_")
                x = latlonArr[0].toDouble()
                y = latlonArr[1].toDouble()
                userPointsList.add(Userpoints(it.key, x, y * -1.0))
                UtilityLog.d("wx","Added Userpoint to list: "+ it.key + ": x: " + x + " y: "+y)

            } else {
                //UtilityLog.d("wx","Userpoint Not found!!!")
            }




        }

    }


    fun addUserPoint(context: Context, location: LatLon) {
        //val userpointData = mutableListOf<Userpoints>()
        //LatLon.distance(location, LatLon(spotterinfo[it].lat, spotterinfo[it].lon), DistanceUnit.MILE)
        Utility.writePref(context, "USERPOINTS_"+location.lat + "_" + location.lon, ""+location.lat + "_" + location.lon)

        UtilityLog.d("wx", "UserPoint Added: lat: "+location.lat+" lon: "+location.lon)


    }



    fun deleteUserPoint(location: LatLon) {

        val userpointData = mutableListOf<Userpoints>()
        var tmpArr: List<String>

        //LatLon.distance(location, LatLon(spotterinfo[it].lat, spotterinfo[it].lon), DistanceUnit.MILE)
        var shortestDistance = 13.0
        var currentDistance: Double
        var bestUserPoint = -1

        currentDistance = LatLon.distance(location, LatLon(location.lat, location.lon), DistanceUnit.MILE)
        if (currentDistance < shortestDistance) {

            UtilityLog.d("wx", "UserPoint deleted: lat: "+location.lat+" lon: "+location.lon)

        }



    }

    fun findClosestSpotter(location: LatLon): String {
        var text = ""
        var SpotterInfoString = ""
        val spotterinfo = mutableListOf<Spotter>()
        text = text.replace("#storm reports.*?$".toRegex(), "")
        val htmlArr = text.split("<br>").dropLastWhile { it.isEmpty() }
        var tmpArr: List<String>
        htmlArr.forEach { line->
            tmpArr = line.split(";;").dropLastWhile { it.isEmpty() }
            if (tmpArr.size > 15) {
                spotterinfo.add(
		Spotter(
		tmpArr[0], 
		tmpArr[1], 
		tmpArr[2], 
		tmpArr[3], 
		tmpArr[4], 
		tmpArr[5], 
		tmpArr[6], 
		tmpArr[7], 
		tmpArr[8], 
		tmpArr[9], 
		tmpArr[10], 
		tmpArr[11], 
		tmpArr[12], 
		tmpArr[13], 
		tmpArr[14], 
		tmpArr[15]
		)
		)
            }
        }

        var shortestDistance = 13.0
        var currentDistance: Double
        var bestSpotter = -1

        spotterinfo.indices.forEach {
            currentDistance = LatLon.distance(location, LatLon(spotterinfo[it].lat, spotterinfo[it].lon), DistanceUnit.MILE)
            if (currentDistance < shortestDistance) {
                shortestDistance = currentDistance
                bestSpotter = it
                SpotterInfoString =
                        "Name: "+spotterinfo[it].firstName +" "+spotterinfo[it].lastName+"\nLocation: "+spotterinfo[it].lat+" "+spotterinfo[it].lon+"\nReport at: "+spotterinfo[it].reportAt+"\nEmail: "+spotterinfo[it].email+"\nPhone: "+spotterinfo[it].phone+"\nCallsign: "+spotterinfo[it].callsign+"\nFreq: "+spotterinfo[it].freq+"\nNote: "+spotterinfo[it].note+"\n============================\n"



            }
        }
        UtilityLog.d("wx", "Spotter Info: "+SpotterInfoString)
        return if (bestSpotter == -1) {
            "Spotter Info not available!"
        } else {
            SpotterInfoString
        }


    }



}

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
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.DistanceUnit
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import java.util.regex.Pattern


object UtilityUserPoints {
    internal var userPointsList = mutableListOf<Userpoints>()
    internal var name = ""
    private var initialized = false
    private var tmpsize = 1
    private const val REFRESH_LOC_MIN = 1
    private var lastRefresh = 0.toLong()
    internal var x = DoubleArray(1)
        private set
    internal var y = DoubleArray(1)
        private set





    val userPointsData: MutableList<Userpoints>
        get() {
            var currentTime = System.currentTimeMillis()
            val currentTimeSec = currentTime / 1000
            //val refreshIntervalSec = (REFRESH_LOC_MIN * 60).toLong()
            //val currentTimeSec = currentTime / 1000
            val refreshIntervalSec = (REFRESH_LOC_MIN * 60).toLong()
            if (currentTimeSec > lastRefresh + refreshIntervalSec || !initialized) {
                userPointsList = mutableListOf()
                val latAl = mutableListOf<String>()
                val lonAl = mutableListOf<String>()
                val preferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.appContext)
                preferences?.all?.forEach {
                    val pattern = Pattern.compile("USERPOINTS=.*?")
                    val m = pattern.matcher(it.key)
                    val keyString = it.key
                    if (m.find()) {
                        //UtilityLog.d("userpoint", "Found Userpoint values: " + it.key + ": " + it.value)
                        val getLatLon: String = it.value.toString()
                        var tmpArr: List<String>
                        getLatLon.forEach { line ->
                            tmpArr = getLatLon.split("_").dropLastWhile { it.isEmpty() }
                            if (tmpArr.size > tmpsize) {
                                UtilityLog.d("userpoint", "Got Userpoint values: " + keyString + ": " + it.value)
                                userPointsList.add(
                                        Userpoints(
                                                keyString,
                                                tmpArr[0],
                                                tmpArr[1]
                                        )
                                )
                                latAl.add(tmpArr[0])
                                lonAl.add(tmpArr[1])
                                    }
                                }


                                if (latAl.size == lonAl.size) {
                                    x = DoubleArray(latAl.size)
                                    y = DoubleArray(latAl.size)
                                    latAl.indices.forEach {
                                        x[it] = latAl[it].toDoubleOrNull() ?: 0.0
                                        y[it] = -1.0 * (lonAl[it].toDoubleOrNull() ?: 0.0)
                                        UtilityLog.d("userpoint", "Userpoint latlon: x: " + x[it] + " y: " + x[it])
                                    }

                                } else {
                                    UtilityLog.d("userpoint", "Userpoint latlon: set to 0.0 :(")
                                    x = DoubleArray(1)
                                    y = DoubleArray(1)
                                    x[0] = 0.0
                                    y[0] = 0.0
                                }
                                initialized = true
                                currentTime = System.currentTimeMillis()
                                lastRefresh = currentTime / 1000
                            }
                        }
            }
            return userPointsList
        }




    fun addUserPoint(context: Context, oglr: WXGLRender, glv: WXGLSurfaceView, location: LatLon) {
        Utility.writePref(context, "USERPOINTS="+location.lat + "=" + location.lon, ""+location.lat + "_" + location.lon)
        UtilityLog.d("userpoint", "UserPoint Added: lat: "+location.lat+" lon: "+location.lon)
        //oglr.constructUserPoints()
        //glv.requestRender()
        //val get = WXGLRadarActivity()
        //get.getContent()


    }



    fun deleteUserPoint(context: Context, oglr: WXGLRender, glv: WXGLSurfaceView, location: LatLon): String {
        var userPointInfoString = ""
        val userPointInfo = mutableListOf<Userpoints>()

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences?.all?.forEach {
            val pattern = Pattern.compile("USERPOINTS=.*?")
            val m = pattern.matcher(it.key)
            if (m.find()) {
                //UtilityLog.d("userpoint", "Found Userpoint values: " + it.key + ": " + it.value)
                val keyString = it.key
                val getLatLon: String = it.value.toString()
                var tmpArr: List<String>
                getLatLon.forEach { line ->
                    tmpArr = getLatLon.split("_").dropLastWhile { it.isEmpty() }
                    if (tmpArr.size > tmpsize) {
                        UtilityLog.d("userpoint", "Got Userpoint values: " + keyString + ": " + it.value)
                        userPointInfo.add(
                                Userpoints(
                                        keyString,
                                        tmpArr[0],
                                        tmpArr[1]
                                )
                        )
                    }

                }
            }
        }

        var shortestDistance = 13.0
        var currentDistance: Double
        var bestSpotter = -1

        userPointInfo.indices.forEach {
            currentDistance = LatLon.distance(location, LatLon(userPointInfo[it].lat, userPointInfo[it].lon), DistanceUnit.MILE)
            if (currentDistance < shortestDistance) {
                shortestDistance = currentDistance
                bestSpotter = it

                userPointInfoString = "UserPoint Deleted: \nkey: "+userPointInfo[it].key+"\nlat: "+userPointInfo[it].lat+"\n lon: "+userPointInfo[it].lon+"\n"
                Utility.removePref(context, userPointInfo[it].key)
                //oglr.constructUserPoints()
                //glv.requestRender()
                //val get = WXGLRadarActivity()
                //get.getContent()


            }

        }
        UtilityLog.d("userpoint", "UserPoint Info: "+userPointInfoString)
        return if (bestSpotter == -1) {
            "I cant find your userpoint to delete."
        } else {
            userPointInfoString
        }


    }


    fun deleteAllUserPoints(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences?.all?.forEach {
            val pattern = Pattern.compile("USERPOINTS=.*?")
            val m = pattern.matcher(it.key)
            if (m.find()) {
                UtilityLog.d("userpoint", "Deleted Userpoint (all): " + it.key + ": " + it.value)
                Utility.removePref(context, it.key)


            }
        }

    }


    fun findClosestUserPoint(context: Context, location: LatLon): String {
        var userPointInfoString = ""
        val userPointInfo = mutableListOf<Userpoints>()

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences?.all?.forEach {
            val pattern = Pattern.compile("USERPOINTS=.*?")
            val m = pattern.matcher(it.key)
            val keyString = it.key
            if (m.find()) {
                //UtilityLog.d("userpoint", "Found Userpoint values: " + it.key + ": " + it.value)
                val getLatLon: String = it.value.toString()
                var tmpArr: List<String>
                getLatLon.forEach { line ->
                    tmpArr = getLatLon.split("_").dropLastWhile { it.isEmpty() }
                    if (tmpArr.size > tmpsize) {
                        UtilityLog.d("userpoint", "Got Userpoint values: " + keyString + ": " + it.value)
                        userPointInfo.add(
                                Userpoints(
                                        keyString,
                                        tmpArr[0],
                                        tmpArr[1]
                                )
                        )
                    }
                }
            }
        }

        var shortestDistance = 13.0
        var currentDistance: Double
        var bestSpotter = -1

        userPointInfo.indices.forEach {
            currentDistance = LatLon.distance(location, LatLon(userPointInfo[it].lat, userPointInfo[it].lon), DistanceUnit.MILE)
            if (currentDistance < shortestDistance) {
                shortestDistance = currentDistance
                bestSpotter = it
                userPointInfoString = "UserPoint Info:\nlat: "+userPointInfo[it].lat+"\n lon: "+userPointInfo[it].lon+"\n"



            }
        }
        UtilityLog.d("userpoint", "UserPoint Info: "+userPointInfoString)
        return if (bestSpotter == -1) {
            "UserPoint Info not available!"
        } else {
            userPointInfoString
        }


    }

}

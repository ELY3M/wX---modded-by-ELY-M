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

package joshuatee.wx.radar

import android.content.Context
import joshuatee.wx.MyApplication
import joshuatee.wx.util.Utility
import kotlin.math.*
import joshuatee.wx.util.UtilityMath
import joshuatee.wx.Extensions.*
import joshuatee.wx.objects.DownloadTimer

// Data file - https://www.wpc.ncep.noaa.gov/basicwx/coded_srp.txt
// Decoder - https://www.wpc.ncep.noaa.gov/basicwx/read_coded_fcst_bull.shtml
// Image - https://www.wpc.ncep.noaa.gov/basicwx/basicwx_ndfd.php

/*
 
 CODED SURFACE FRONTAL POSITIONS FORECAST
 NWS WEATHER PREDICTION CENTER COLLEGE PARK MD
 1117 AM EST FRI DEC 20 2019
 
 SURFACE PROG VALID 201912201800Z
 HIGHS 1043 5010795 1036 3750811 1036 4061092 1026 3121240
 LOWS 1000 5301138 1021 4610937 1005 4931216 1005 4151308 1021 3351009
 STNRY 2310761 2230786 2210811 2220827 2220838
 STNRY 4050725 4010749 3990769 4020794 4050809 4080816
 WARM 4610937 4610923 4560905 4480890 4340873 4220854 4140836
 4070815
 COLD 4620937 4570948 4570961 4600977
 WARM 5251080 5251064 5171042 4951027 4791013 4680995 4600978
 COLD 4931216 4701239 4501262 4391277 4251295
 STNRY 4151307 4001313 3851326
 STNRY 5251080 5241105 5271130 5301139
 STNRY 5301139 5331149 5401174 5451199 5531223 5631241
 TROF 2220879 2530886 2750886 2970879
 TROF 4581084 4441085 4251096
 TROF 3941043 3671043 3391055
 TROF 4600938 4350945 4160956 3940974 3760988 3630996 3511002
 3251012 2991024 2871026
 WARM 4161307 4211300 4261294
 TROF 5361348 5171317 4971300 4801292
 TROF 5241139 4991127 4721125
 TROF 3171112 2901103 2711092 2491077
 
 SURFACE PROG VALID 201912210000Z
 HIGHS 1035 3971077 1042 4900775 1023 3181216
 LOWS 1005 5011189 1005 4051297 1000 5271115 1021 4740914 1022 3420997
 COLD 4730916 4690932 4700953 4730971
 STNRY 2310757 2280774 2250791 2260806 2300825
 
 */

object UtilityWpcFronts {
    var pressureCenters = mutableListOf<PressureCenter>()
    var fronts = mutableListOf<Fronts>()
    private var timer = DownloadTimer("WPC FRONTS")

    private fun addColdFrontTriangles(front: Fronts, tokens: List<String>) {
        val length = 0.4
        var startIndex = 0
        var indexIncrement = 1
        if (front.type == FrontTypeEnum.OCFNT) {
            startIndex = 1
            indexIncrement = 2
        }
        for (index in startIndex until tokens.size step indexIncrement) {
            val coordinates = parseLatLon(tokens[index])
            if (index < (tokens.size - 1)) {
                val coordinates2 = parseLatLon(tokens[index + 1])
                val distance = UtilityMath.distanceOfLine(coordinates[0], coordinates[1], coordinates2[0], coordinates2[1])
                val numberOfTriangles = floor(distance / length).toInt()
                // construct two lines which will consist of adding 4 points
                for (pointNumber in 1 until numberOfTriangles step 2) {
                    val x1 = coordinates[0] + ((coordinates2[0] - coordinates[0]) * length * pointNumber) / distance
                    val y1 = coordinates[1] + ((coordinates2[1] - coordinates[1]) * length * pointNumber) / distance
                    val x3 = coordinates[0] + ((coordinates2[0] - coordinates[0]) * length * (pointNumber + 1)) / distance
                    val y3 = coordinates[1] + ((coordinates2[1] - coordinates[1]) * length * (pointNumber + 1)) / distance
                    val p2 = UtilityMath.computeTipPoint(x1, y1, x3, y3, true)
                    val x2 = p2[0]
                    val y2 = p2[1]
                    front.coordinates.add(LatLon(x1, y1))
                    front.coordinates.add(LatLon(x2, y2))
                    front.coordinates.add(LatLon(x2, y2))
                    front.coordinates.add(LatLon(x3, y3))
                }
            }
        }
    }

    private fun addWarmFrontSemicircles(front: Fronts, tokens: List<String>) {
        var length = 0.4 // size of triangle
        var startIndex = 0
        var indexIncrement = 1
        if (front.type == FrontTypeEnum.OCFNT) {
            startIndex = 2
            indexIncrement = 2
            length = 0.2
        }
        for (index in startIndex until tokens.size step indexIncrement) {
            val coordinates = parseLatLon(tokens[index])
            if (index < (tokens.size - 1)) {
                val coordinates2 = parseLatLon(tokens[index + 1])
                val distance = UtilityMath.distanceOfLine(coordinates[0], coordinates[1], coordinates2[0], coordinates2[1])
                val numberOfTriangles = floor(distance / length).toInt()
                // construct two lines which will consist of adding 4 points
                for (pointNumber in 1 until numberOfTriangles step 4) {
                    val x1 = coordinates[0] + ((coordinates2[0] - coordinates[0]) * length * pointNumber) / distance
                    val y1 = coordinates[1] + ((coordinates2[1] - coordinates[1]) * length * pointNumber) / distance
                    val center1 = coordinates[0] + ((coordinates2[0] - coordinates[0]) * length * (pointNumber + 0.5)) / distance
                    val center2 = coordinates[1] + ((coordinates2[1] - coordinates[1]) * length * (pointNumber + 0.5)) / distance
                    val x3 = coordinates[0] + ((coordinates2[0] - coordinates[0]) * length * (pointNumber + 1)) / distance
                    val y3 = coordinates[1] + ((coordinates2[1] - coordinates[1]) * length * (pointNumber + 1)) / distance
                    front.coordinates.add(LatLon(x1, y1))
                    val slices = 20
                    val step = PI / slices
                    val rotation = 1.0
                    val xDiff = x3 - x1
                    val yDiff = y3 - y1
                    val angle = atan2(yDiff, xDiff) * 180.0 / PI
                    val sliceStart = ((slices * angle) / 180.0).toInt()
                    for (i in sliceStart until slices + sliceStart + 1 step 1) {
                        val x = rotation * length * cos(step * i) + center1
                        val y = rotation * length * sin(step * i) + center2
                        front.coordinates.add(LatLon(x, y))
                        front.coordinates.add(LatLon(x, y))
                    }
                    front.coordinates.add(LatLon(x3, y3))
                }
            }
        }
    }

    private fun addFrontDataStnryWarm(front: Fronts, tokens: List<String>) {
        tokens.indices.forEach { index ->
            val coordinates = parseLatLon(tokens[index])
            // effectively the first and last values are not there
            if (index != 0 && index != (tokens.size - 1)) {
                front.coordinates.add(LatLon(coordinates[0], coordinates[1]))
            }
        }
    }

    private fun addFrontDataTrof(front: Fronts, tokens: List<String>) {
        val fraction = 0.8
        for (index in 0 until tokens.size - 1 step 1) {
            val coordinates = parseLatLon(tokens[index])
            front.coordinates.add(LatLon(coordinates[0], coordinates[1]))
            val oldCoordinates = parseLatLon(tokens[index + 1])
            val coord = UtilityMath.computeMiddishPoint(
                    coordinates[0],
                    coordinates[1],
                    oldCoordinates[0],
                    oldCoordinates[1],
                    fraction
            )
            front.coordinates.add(LatLon(coord[0], coord[1]))
        }
    }

    private fun addFrontData(front: Fronts, tokens: List<String>) {
        tokens.indices.forEach { index ->
            val coordinates = parseLatLon(tokens[index])
            front.coordinates.add(LatLon(coordinates[0], coordinates[1]))
            if (index != 0 && index != (tokens.size - 1)) {
                front.coordinates.add(LatLon(coordinates[0], coordinates[1]))
            }
        }
    }

    private fun parseLatLon(string: String): List<Double> {
        return if (string.length != 7) {
            listOf(0.0, 0.0)
        } else {
            val lat = (string.substring(0, 2) + "." + string.substring(2, 3)).toDoubleOrNull() ?: 0.0
            val lon: Double = if (string[3] == '0') {
                (string.substring(4, 6) + "." + string.substring(6, 7)).toDoubleOrNull() ?: 0.0
            } else {
                (string.substring(3, 6) + "." + string.substring(6, 7)).toDoubleOrNull() ?: 0.0
            }
            listOf(lat, lon)
        }
    }

    fun get(context: Context) {
        if (timer.isRefreshNeeded(context)) {
            pressureCenters = mutableListOf()
            fronts = mutableListOf()
            val urlBlob = MyApplication.nwsWPCwebsitePrefix + "/basicwx/coded_srp.txt"
            var html = urlBlob.getHtmlSep()
            html = html.replace("<br>", MyApplication.newline)
            html = html.replace(MyApplication.newline, MyApplication.sep)
            val timestamp = html.parseFirst("SURFACE PROG VALID ([0-9]{12}Z)")
            Utility.writePref("WPC_FRONTS_TIMESTAMP", timestamp)
            html = html.parseFirst("SURFACE PROG VALID [0-9]{12}Z(.*?)" +
                    MyApplication.sep +
                    " " +
                    MyApplication.sep)
            html = html.replace(MyApplication.sep, MyApplication.newline)
            val lines = html.split(MyApplication.newline).toMutableList()
            for (index in lines.indices) {
                if (index < lines.size - 1) {
                    // Handle lines that wrap around, check to see if lines don't start
                    // with a known character
                    if (lines[index + 1][0] != 'H'
                            && lines[index + 1][0] != 'L'
                            && lines[index + 1][0] != 'C'
                            && lines[index + 1][0] != 'S'
                            && lines[index + 1][0] != 'O'
                            && lines[index + 1][0] != 'T'
                            && lines[index + 1][0] != 'W') {
                        lines[index] =  lines[index]  + lines[index + 1]
                        if (index < lines.size - 2
                                &&lines[index + 2][0] != 'H'
                                && lines[index + 2][0] != 'L'
                                && lines[index + 2][0] != 'C'
                                && lines[index + 2][0] != 'S'
                                && lines[index + 2][0] != 'O'
                                && lines[index + 2][0] != 'T'
                                && lines[index + 2][0] != 'W') {
                            lines[index] =  lines[index]  + lines[index + 2]
                        }
                    }
                }
                val tokens = lines[index].trim().split(" ").toMutableList()
                if (tokens.size > 1) {
                    val type = tokens[0]
                    tokens.removeAt(0)
                    when (type) {
                        "HIGHS" -> {
                            for (typeIndex in 0 until tokens.size step 2) {
                                if (typeIndex + 1 < tokens.size) {
                                    val coordinates = parseLatLon(tokens[typeIndex + 1])
                                    pressureCenters.add(PressureCenter(PressureCenterTypeEnum.HIGH,
                                            tokens[typeIndex], coordinates[0], coordinates[1]))
                                }
                            }
                        }
                        "LOWS" -> {
                            for (typeIndex in 0 until tokens.size step 2) {
                                if (typeIndex + 1 < tokens.size) {
                                    val coordinates = parseLatLon(tokens[typeIndex + 1])
                                    pressureCenters.add(PressureCenter(PressureCenterTypeEnum.LOW,
                                            tokens[typeIndex], coordinates[0], coordinates[1]))
                                }
                            }
                        }
                        "COLD" -> {
                            val front = Fronts(FrontTypeEnum.COLD)
                            addFrontData(front, tokens)
                            addColdFrontTriangles(front, tokens)
                            fronts.add(front)
                        }
                        "STNRY" -> {
                            val front = Fronts(FrontTypeEnum.STNRY)
                            addFrontData(front, tokens)
                            fronts.add(front)
                            val frontStWarm = Fronts(FrontTypeEnum.STNRY_WARM)
                            addFrontDataStnryWarm(frontStWarm, tokens)
                            fronts.add(frontStWarm)
                        }
                        "WARM" -> {
                            val front = Fronts(FrontTypeEnum.WARM)
                            addFrontData(front, tokens)
                            addWarmFrontSemicircles(front, tokens)
                            fronts.add(front)
                        }
                        "TROF" -> {
                            val front = Fronts(FrontTypeEnum.TROF)
                            addFrontDataTrof(front, tokens)
                            fronts.add(front)
                        }
                        "OCFNT" -> {
                            val front = Fronts(FrontTypeEnum.OCFNT)
                            addFrontData(front, tokens)
                            addColdFrontTriangles(front, tokens)
                            addWarmFrontSemicircles(front, tokens)
                            fronts.add(front)
                        }
                        else -> {
                        }
                    }
                }
            }
        }
    }
}

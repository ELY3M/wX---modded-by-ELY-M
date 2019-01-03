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

package joshuatee.wx.vis

import android.content.Context
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.util.Utility

object UtilityUSImgNWSGOESMercator {

    private val GOES_HASH = mapOf(
        "MOB" to "30.70,87.55",
        "BMX" to "33.00,86.75",
        "HUN" to "34.60,86.90",
        "JAN" to "32.50,90.18",
        "MEG" to "35.20,89.75",
        "OHX" to "35.80,86.50",
        "MRX" to "35.90,83.70",
        "PAH" to "37.50,89.00",
        "LMK" to "37.65,85.70",
        "JKL" to "37.40,83.65",
        "ILX" to "39.90,89.50",
        "LOT" to "41.60,88.40",
        "IWX" to "41.40,85.60",
        "IND" to "39.55,86.50",
        "ILN" to "39.60,83.80",
        "CLE" to "41.40,81.90",
        "DTX" to "42.70,83.30",
        "GRR" to "43.25,85.75",
        "APX" to "45.20,84.70",
        "MQT" to "46.80,87.85",
        "MKX" to "43.30,88.90",
        "GRB" to "45.10,88.66",
        "ARX" to "43.90,91.45",
        "DLH" to "47.10,92.10",
        "MPX" to "44.85,93.80",
        "FGF" to "47.75,97.05",
        "BIS" to "47.50,101.50",
        "ABR" to "44.75,99.20",
        "UNR" to "44.40,102.90",
        "FSD" to "43.50,97.25",
        "LBF" to "41.70,100.66",
        "OAX" to "41.40,96.80",
        "GID" to "40.50,98.80",
        "MHX" to "35.15,76.55",
        "ILM" to "33.90,78.70",
        "RAH" to "35.70,79.00",
        "TAE" to "30.30,84.50",
        "JAX" to "30.40,82.00",
        "TBW" to "27.75,82.30",
        "MLB" to "28.25,80.60",
        "MFL" to "26.10,80.90",
        "EYW" to "24.60,81.60",
        "TAE" to "30.25,84.50",
        "FFC" to "33.45,83.90",
        "CAE" to "33.80,81.05",
        "CHS" to "32.28,80.80",
        "GSP" to "35.20,82.20",
        "OKX" to "41.00,73.25",
        "ALY" to "42.80,73.90",
        "BUF" to "43.20,77.50",
        "BGM" to "42.20,75.68",
        "LWX" to "38.90,77.80",
        "AKQ" to "37.20,76.69",
        "RNK" to "37.10,80.20",
        "BOX" to "42.00,71.66",
        "PHI" to "39.80,74.75",
        "PBZ" to "40.25,80.40",
        "CTP" to "40.75,77.90",
        "RLX" to "38.40,81.5",
        "BTV" to "44.2,73.38",
        "GYX" to "44.2,70.6",
        "CAR" to "45.66,68.5",
        "PUB" to "38.10,104.60",
        "BOU" to "39.80,104.40",
        "GJT" to "39.00,108.75",
        "RIW" to "43.00,108.90",
        "CYS" to "42.20,105.25",
        "BYZ" to "45.65,107.75",
        "GGW" to "47.80,106.50",
        "TFX" to "46.66,111.85",
        "MSO" to "46.60,114.50",
        "PIH" to "43.40,113.08",
        "BOI" to "43.60,117.00",
        "OTX" to "47.50,118.50",
        "SEW" to "47.75,123.80",
        "PQR" to "45.00,124.00",
        "PDT" to "45.50,120.20",
        "MFR" to "42.50,122.90",
        "TWC" to "32.45,111.36",
        "PSR" to "33.17,113.15",
        "FGZ" to "35.15,111.23",
        "VEF" to "36.25,115.90",
        "REV" to "39.75,119.60",
        "LKN" to "40.10,116.65",
        "SLC" to "39.75,112.80",
        "MAF" to "31.20,102.75",
        "EPZ" to "32.00,107.00",
        "LUB" to "33.80,101.55",
        "AMA" to "35.80,101.55",
        "SJT" to "31.85,100.37",
        "FWD" to "32.20,97.00",
        "EWX" to "29.75,99.27",
        "CRP" to "28.00,98.18",
        "BRO" to "26.60,98.18",
        "HGX" to "29.75,95.60",
        "ABQ" to "34.80,106.25",
        "DDC" to "38.00,100.36",
        "GLD" to "39.33,101.50",
        "TOP" to "39.10,96.50",
        "ICT" to "38.10,97.10",
        "DMX" to "42.00,93.80",
        "DVN" to "41.40,90.90",
        "OUN" to "35.20,98.30",
        "TSA" to "35.40,95.40",
        "EAX" to "39.33,94.08",
        "SGF" to "37.50,93.18",
        "LSX" to "38.66,90.75",
        "LZK" to "34.80,92.60",
        "SHV" to "32.75,93.85",
        "LCH" to "29.90,93.08",
        "LIX" to "29.70,90.08",
        "HI" to "20.53,155.94",
        "EKA" to "40.40,124.55",
        "HNX" to "36.40,119.40",
        "LOX" to "34.50,120.15",
        "STO" to "39.20,121.50",
        "SGX" to "33.75,117.40",
        "MTR" to "37.25,122.85"
    )

    private fun getGOESCenterNWS(ridF: String) = GOES_HASH[ridF] ?: "0.0,0.0"

    fun getMercatorNumbers(context: Context, rid1: String): ProjectionNumbers {
        var scaleFactor = 41.40
        var xStr: String
        var yStr: String
        val xImageCenterPixels = 360.0  // 720 wide, 480 tall ( NWS Goes )
        val yImageCenterPixels = 240.0  // 720 wide, 480 tall ( NWS Goes )
        when (rid1) {
            "MW" -> {
                xStr = "39.00"
                yStr = "92.00"
            }
            "GL" -> {
                xStr = "45.00"
                yStr = "85.00"
                scaleFactor = 39.00
            }
            "SE" -> {
                xStr = "29.00"
                yStr = "84.90"
                scaleFactor = 37.00
            }
            "NE" -> {
                xStr = "44.00"
                yStr = "75.00"
                scaleFactor = 45.00
            }
            "NP" -> {
                xStr = "46.00"
                yStr = "103.00"
                scaleFactor = 46.00
            }
            "CP" -> {
                xStr = "39.00"
                yStr = "103.00"
            }
            "SC" -> {
                xStr = "30.00"
                yStr = "99.00"
                scaleFactor = 37.00
            }
            "NW" -> {
                xStr = "46.00"
                yStr = "118.00"
                scaleFactor = 46.00
            }
            "MA" -> {
                xStr = "38.00"
                yStr = "80.00"
            }
            "WC" -> {
                xStr = "39.00"
                yStr = "118.00"
            }
            "SW" -> {
                xStr = "30.00"
                yStr = "114.00"
                scaleFactor = 37.00
            }
            else -> {
                xStr = Utility.readPref(context, "RID_" + rid1 + "_X", "0.00")
                yStr = Utility.readPref(context, "RID_" + rid1 + "_Y", "0.00")
                val coords = getGOESCenterNWS(rid1).split(",")
                if (coords[0] != "0.0" && coords[1] != "0.0") {
                    xStr = coords[0]
                    yStr = coords[1]
                }
                when (rid1) {
                    "HI" -> scaleFactor = 35.00
                    "SLCa" -> scaleFactor = 70.00
                    "TFX" -> scaleFactor = 70.00
                    "MSO" -> scaleFactor = 70.00
                    "MAFa" -> scaleFactor = 82.00
                    "ABQa" -> scaleFactor = 82.00
                    "OUNa" -> scaleFactor = 82.00
                    "HGX" -> scaleFactor = 82.00
                    "TSAa" -> scaleFactor = 82.00
                    "MQTa" -> scaleFactor = 82.00
                    "DLH" -> scaleFactor = 82.00
                    "FGFa" -> scaleFactor = 82.00
                    "BIS" -> scaleFactor = 82.00
                    "UNRa" -> scaleFactor = 82.00
                    "LSX" -> scaleFactor = 82.00
                    "BYZa" -> scaleFactor = 82.00
                    "BOIa" -> scaleFactor = 82.00
                    "OTX" -> scaleFactor = 82.00
                    "SEWa" -> scaleFactor = 82.00
                    "PQRa" -> scaleFactor = 82.00
                    "PDTa" -> scaleFactor = 82.00
                    "LKNa" -> scaleFactor = 82.00
                    "CAR" -> scaleFactor = 82.00
                    "FGZ" -> scaleFactor = 82.00
                    "GJT" -> scaleFactor = 82.00
                    "APXa" -> scaleFactor = 82.00
                    "EKA" -> scaleFactor = 82.00
                    "HNXa" -> scaleFactor = 82.00
                    "STO" -> scaleFactor = 82.00
                    "MTR" -> scaleFactor = 82.00
                    "GYXa" -> scaleFactor = 82.00
                    "VEF" -> scaleFactor = 82.00
                    "REV" -> scaleFactor = 82.00
                    else -> scaleFactor = 130.00 // default scale for northern states
                }
            }
        }

        when (rid1) {
            "SLC" -> scaleFactor = 64.00
            "ABQ", "OUN", "MAF" -> scaleFactor = 77.00
            "TSA" -> scaleFactor = 78.00
            "HNX" -> scaleFactor = 80.00
            "LSX" -> scaleFactor = 82.00
            "LKN" -> scaleFactor = 85.00
            "RIW", "PQR", "BOI" -> scaleFactor = 88.00
            "PDT", "UNR", "SEW", "BYZ", "FGFa", "GYX", "CAR", "APX" -> scaleFactor = 91.00
            "MQT" -> scaleFactor = 93.60
            "CRP" -> scaleFactor = 115.00
            "HGX" -> scaleFactor = 73.00
            "DLH", "BIS", "OTX", "PDTa", "FGF" -> scaleFactor = 95.00
            "MPX", "ARX", "ABR" -> scaleFactor = 135.00
            "GGW" -> scaleFactor = 140.00
            "MFL", "TBW", "MLB", "EYW", "LUB", "BMX", "TAE", "MOB", "LIX", "LCH", "SHV", "FWD", "SJT", "EWX", "TWC", "PSR", "SGX" -> scaleFactor =
                    110.00
            "FFC", "CHS", "CAE", "GSP", "ILM", "MHX", "HUN", "MEG", "LZK", "PUB", "LOX" -> scaleFactor =
                    115.00
            "RAH" -> scaleFactor = 118.00
            "SGF", "PAH", "LMK", "JKL", "RNK", "AKQ", "DDC", "ICT" -> scaleFactor = 120.00
            "PHI", "LWX", "RLX", "ILN", "IND", "ILX", "CTP", "PBZ", "LSXa", "EAX", "TOP", "GLD", "BOU" -> scaleFactor =
                    125.00
        }
        return ProjectionNumbers(scaleFactor, xStr, yStr, xImageCenterPixels, yImageCenterPixels)
    }
}

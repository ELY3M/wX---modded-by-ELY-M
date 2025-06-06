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

package joshuatee.wx.radar

import joshuatee.wx.objects.LatLon
import joshuatee.wx.objects.Site
import joshuatee.wx.objects.Sites

@Suppress("SpellCheckingInspection")
object RadarSites {

    fun getName(radarSite: String): String = names[radarSite] ?: ""

    fun getLatLon(radarSite: String): LatLon {
        val lat = lat[radarSite] ?: ""
        val lon = "-" + (lon[radarSite] ?: "")
        return LatLon(lat, lon)
    }

    private fun nexradRadarCodes(): List<String> {
        val radars = mutableListOf<String>()
        for (radar in sites.codeList) {
//            if (!radar.startsWith("T")) {
            if (radar.length == 3) {
                radars.add(radar)
            }
        }
        return radars
    }

    private fun tdwrRadarCodes(): List<String> {
        val radars = mutableListOf<String>()
        for (radar in sites.codeList) {
            if (radar.length == 4) {
//            if (radar.startsWith("T")) {
                radars.add(radar)
            }
        }
        return radars
    }

    fun nexradRadars(): List<String> {
        val radars = mutableListOf<String>()
        for (radar in sites.nameList) {
            val tokens = radar.split(":")
//            if (!radar.startsWith("T") && radar[3] == ':') {
            if (tokens[0].length == 3) {
                radars.add(radar)
            }
        }
        return radars
    }

    fun tdwrRadars(): List<String> {
        val radars = mutableListOf<String>()
        for (radar in sites.nameList) {
            val tokens = radar.split(":")
            if (tokens[0].length == 4) {
                radars.add(radar)
            }
        }
        return radars
    }

    fun getNearest(
        location: LatLon,
        count: Int,
        includeTdwr: Boolean = true
    ): List<Site> {
        val radarSites = mutableListOf<Site>()
        val allRadars = nexradRadarCodes().toMutableList()
        if (includeTdwr) {
            allRadars += tdwrRadarCodes()
        }
        allRadars.forEach {
            val latLon = getLatLon(it)
            radarSites.add(
                Site.fromLatLon(
                    it,
                    latLon,
                    LatLon.distance(location, latLon)
                )
            )
        }
        radarSites.sortBy { it.distance }
        return radarSites.slice(0 until count)
    }

    fun getNearestCode(
        location: LatLon,
        includeTdwr: Boolean = false
    ): String = getNearest(location, 5, includeTdwr)[0].codeName

    private val names = mapOf(
        "JUA" to "PR, San Juan",
        "CBW" to "ME, Loring AFB",
        "GYX" to "ME, Portland",
        "CXX" to "VT, Burlington",
        "BOX" to "MA, Boston",
        "ENX" to "NY, Albany",
        "BGM" to "NY, Binghamton",
        "BUF" to "NY, Buffalo",
        "TYX" to "NY, Montague",
        "OKX" to "NY, New York City",
        "DOX" to "DE, Dover AFB",
        "DIX" to "PA, Philadelphia",
        "PBZ" to "PA, Pittsburgh",
        "CCX" to "PA, State College",
        "RLX" to "WV, Charleston",
        "AKQ" to "VA, Norfolk/Richmond",
        "FCX" to "VA, Roanoke",
        "LWX" to "VA, Sterling",
        "MHX" to "NC, Morehead City",
        "RAX" to "NC, Raleigh/Durham",
        "LTX" to "NC, Wilmington",
        "CLX" to "SC, Charleston",
        "CAE" to "SC, Columbia",
        "GSP" to "SC, Greer",
        "FFC" to "GA, Atlanta",
        "VAX" to "GA, Moody AFB",
        "JGX" to "GA, Robins AFB",
        "EVX" to "FL, Eglin AFB",
        "JAX" to "FL, Jacksonville",
        "BYX" to "FL, Key West",
        "MLB" to "FL, Melbourne",
        "AMX" to "FL, Miami",
        "TLH" to "FL, Tallahassee",
        "TBW" to "FL, Tampa",
        "BMX" to "AL, Birmingham",
        "EOX" to "AL, Fort Rucker",
        "HTX" to "AL, Huntsville",
        "MXX" to "AL, Maxwell AFB",
        "MOB" to "AL, Mobile",
        "DGX" to "MS, Brandon/Jackson",
        "GWX" to "MS, Columbus AFB",
        "MRX" to "TN, Knoxville/Tri Cities",
        "NQA" to "TN, Memphis",
        "OHX" to "TN, Nashville",
        "HPX" to "KY, Fort Campbell",
        "JKL" to "KY, Jackson",
        "LVX" to "KY, Louisville",
        "PAH" to "KY, Paducah",
        "ILN" to "OH, Wilmington",
        "CLE" to "OH, Cleveland",
        "DTX" to "MI, Detroit/Pontiac",
        "APX" to "MI, Gaylord",
        "GRR" to "MI, Grand Rapids",
        "MQT" to "MI, Marquette",
        "VWX" to "IN, Evansville",
        "IND" to "IN, Indianapolis",
        "IWX" to "IN, North Webster",
        "LOT" to "IL, Chicago",
        "ILX" to "IL, Lincoln",
        "GRB" to "WI, Green Bay",
        "ARX" to "WI, La Crosse",
        "MKX" to "WI, Milwaukee",
        "DLH" to "MN, Duluth",
        "MPX" to "MN, Minneapolis/St. Paul",
        "DVN" to "IA, Davenport",
        "DMX" to "IA, Des Moines",
        "EAX" to "MO, Kansas City",
        "SGF" to "MO, Springfield",
        "LSX" to "MO, St. Louis",
        "SRX" to "AR, Fort Smith",
        "LZK" to "AR, Little Rock",
        "POE" to "LA, Fort Polk",
        "HDC" to "LA, Hammond",
        "LCH" to "LA, Lake Charles",
//        "LIX" to "LA, New Orleans (deprecated)",
        "SHV" to "LA, Shreveport",
        "AMA" to "TX, Amarillo",
        "EWX" to "TX, Austin/San Antonio",
        "BRO" to "TX, Brownsville",
        "CRP" to "TX, Corpus Christi",
        "FWS" to "TX, Dallas/Ft. Worth",
        "DYX" to "TX, Dyess AFB",
        "EPZ" to "TX, El Paso",
        "GRK" to "TX, Fort Hood",
        "HGX" to "TX, Houston/Galveston",
        "DFX" to "TX, Laughlin AFB",
        "LBB" to "TX, Lubbock",
        "MAF" to "TX, Midland/Odessa",
        "SJT" to "TX, San Angelo",
        "FDR" to "OK, Frederick",
        "TLX" to "OK, Oklahoma City",
        "INX" to "OK, Tulsa",
        "VNX" to "OK, Vance AFB",
        "DDC" to "KS, Dodge City",
        "GLD" to "KS, Goodland",
        "TWX" to "KS, Topeka",
        "ICT" to "KS, Wichita",
        "UEX" to "NE, Grand Island/Hastings",
        "LNX" to "NE, North Platte",
        "OAX" to "NE, Omaha",
        "ABR" to "SD, Aberdeen",
        "UDX" to "SD, Rapid City",
        "FSD" to "SD, Sioux Falls",
        "BIS" to "ND, Bismarck",
        "MVX" to "ND, Grand Forks (Mayville)",
        "MBX" to "ND, Minot AFB",
        "BLX" to "MT, Billings",
        "GGW" to "MT, Glasgow",
        "TFX" to "MT, Great Falls",
        "MSX" to "MT, Missoula",
        "CYS" to "WY, Cheyenne",
        "RIW" to "WY, Riverton",
        "FTG" to "CO, Denver",
        "GJX" to "CO, Grand Junction",
        "PUX" to "CO, Pueblo",
        "ABX" to "NM, Albuquerque",
        "FDX" to "NM, Cannon AFB",
        "HDX" to "NM, Holloman AFB",
        "FSX" to "AZ, Flagstaff",
        "IWA" to "AZ, Phoenix",
        "EMX" to "AZ, Tucson",
        "YUX" to "AZ, Yuma",
        "ICX" to "UT, Cedar City",
        "MTX" to "UT, Salt Lake City",
        "CBX" to "ID, Boise",
        "SFX" to "ID, Pocatello/Idaho Falls",
        "LRX" to "NV, Elko",
        "ESX" to "NV, Las Vegas",
        "RGX" to "NV, Reno",
        "BBX" to "CA, Beale AFB",
        "EYX" to "CA, Edwards AFB",
        "BHX" to "CA, Eureka",
        "VTX" to "CA, Los Angeles",
        "DAX" to "CA, Sacramento",
        "NKX" to "CA, San Diego",
        "MUX" to "CA, San Francisco",
        "HNX" to "CA, San Joaquin Valley",
        "SOX" to "CA, Santa Ana Mountains",
        "VBX" to "CA, Vandenberg AFB",
        "HKI" to "HI, Kauai",
        "HKM" to "HI, Kohala",
        "HMO" to "HI, Molokai",
        "HWA" to "HI, South Shore",
        "MAX" to "OR, Medford",
        "PDT" to "OR, Pendleton",
        "RTX" to "OR, Portland",
        "LGX" to "WA, Langley Hill",
        "ATX" to "WA, Seattle/Tacoma",
        "OTX" to "WA, Spokane",
        "ABC" to "AK, Bethel",
        "APD" to "AK, Fairbanks/Pedro Dome",
        "AHG" to "AK, Kenai",
        "AKC" to "AK, King Salmon",
        "AIH" to "AK, Middleton Island",
        "AEC" to "AK, Nome",
        "ACG" to "AK, Sitka/Biorka Island",
        "GUA" to "GU, Andersen AFB",
//        "PLA" to "NA, Lajes Field, Azores", // TODO FIXME
//        "KJK" to "NA, Kunsan Air Base, South Korea",
//        "KSG" to "NA, Camp Humphreys, South Korea",
//        "ODN" to "NA, Kadena Air Base, Japan",

        "TLVE" to "OH, Cleveland",
        "TADW" to "MD, Andrews Air Force Base",
        "TATL" to "GA, Atlanta",
        "TBWI" to "MD, Baltimore/Wash",
        "TBOS" to "MA, Boston",
        "TCLT" to "NC, Charlotte",
        "TMDW" to "IL, Chicago Midway",
        "TORD" to "IL, Chicago O'Hare",
        "TCMH" to "OH, Columbus",
        "TCVG" to "OH, Covington",
        "TDAL" to "TX, Dallas Love Field",
        "TDFW" to "TX, Dallas/Ft. Worth",
        "TDAY" to "OH, Dayton",
        "TDEN" to "CO, Denver",
        "TDTW" to "MI, Detroit",
        "TIAD" to "VA, Dulles",
        "TFLL" to "FL, Fort Lauderdale",
        "THOU" to "TX, Houston Hobby",
        "TIAH" to "TX, Houston International",
        "TIDS" to "IN, Indianapolis",
        "TMCI" to "MO, Kansas City",
        "TLAS" to "NV, Las Vegas",
        "TSDF" to "KY, Louisville",
        "TMEM" to "TN, Memphis",
        "TMIA" to "FL, Miami",
        "TMKE" to "WI, Milwaukee",
        "TMSP" to "MN, Minneapolis",
        "TBNA" to "TN, Nashville",
        "TMSY" to "LA, New Orleans",
        "TJFK" to "NY, New York City",
        "TEWR" to "NJ, Newark",
        "TOKC" to "OK, Oklahoma City",
        "TMCO" to "FL, Orlando International",
        "TPHL" to "PA, Philadelphia",
        "TPHX" to "AZ, Phoenix",
        "TPIT" to "PA, Pittsburgh",
        "TRDU" to "NC, Raleigh Durham",
        "TSLC" to "UT, Salt Lake City",
        "TSJU" to "PR, San Juan",
        "TSTL" to "MO, St Louis",
        "TTPA" to "FL, Tampa Bay",
        "TTUL" to "OK, Tulsa",
        "TDCA" to "MD, Washington National",
        "TPBI" to "FL, West Palm Beach",
        "TICH" to "KS, Wichita"
    )

    private val lat = mapOf(
        "TYX" to "43.756",
        "ABR" to "45.456",
        "ENX" to "42.586",
        "ABX" to "35.15",
        "FDR" to "34.362",
        "AMA" to "35.233",
        "AHG" to "60.726",
        "GUA" to "13.456",
        "FFC" to "33.363",
        "EWX" to "29.704",
        "BBX" to "39.496",
        "ABC" to "60.792",
        "BLX" to "45.854",
        "BGM" to "42.2",
        "BMX" to "33.171",
        "BIS" to "46.771",
        "CBX" to "43.49",
        "BOX" to "41.956",
        "BRO" to "25.916",
        "BUF" to "42.949",
        "CXX" to "44.511",
        "FDX" to "34.634",
        "ICX" to "37.591",
        "CLX" to "32.655",
        "RLX" to "38.311",
        "CYS" to "41.152",
        "LOT" to "41.604",
        "ILN" to "39.42",
        "CLE" to "41.413",
        "CAE" to "33.949",
        "GWX" to "33.897",
        "CRP" to "27.784",
        "FWS" to "32.573",
        "DVN" to "41.612",
        "FTG" to "39.786",
        "DMX" to "41.731",
        "DTX" to "42.7",
        "DDC" to "37.761",
        "DOX" to "38.826",
        "DLH" to "46.837",
        "DYX" to "32.538",
        "EYX" to "35.098",
        "EVX" to "30.565",
        "EPZ" to "31.873",
        "LRX" to "40.74",
        "BHX" to "40.499",
        "APD" to "65.035",
        "FSX" to "34.574",
        "HPX" to "36.737",
        "GRK" to "30.722",
        "POE" to "31.155",
        "HDC" to "30.519",
        "EOX" to "31.46",
        "SRX" to "35.29",
        "IWX" to "41.359",
        "APX" to "44.906",
        "GGW" to "48.206",
        "GLD" to "39.367",
        "MVX" to "47.528",
        "GJX" to "39.062",
        "GRR" to "42.894",
        "TFX" to "47.46",
        "GRB" to "44.499",
        "GSP" to "34.883",
        "UEX" to "40.321",
        "HDX" to "33.077",
        "CBW" to "46.039",
        "HGX" to "29.472",
        "HTX" to "34.931",
        "IND" to "39.708",
        "JKL" to "37.591",
        "JAX" to "30.485",
        "EAX" to "38.81",
        "BYX" to "24.597",
        "AKC" to "58.68",
        "MRX" to "36.168",
        "ARX" to "43.823",
        "LCH" to "30.125",
        "ESX" to "35.701",
        "DFX" to "29.273",
        "ILX" to "40.15",
        "LZK" to "34.836",
        "VTX" to "34.412",
        "LVX" to "37.975",
        "LBB" to "33.654",
        "MQT" to "46.531",
        "MXX" to "32.537",
        "MAX" to "42.081",
        "MLB" to "28.113",
        "NQA" to "35.345",
        "AMX" to "25.611",
        "AIH" to "59.46",
        "MAF" to "31.943",
        "MKX" to "42.968",
        "MPX" to "44.849",
        "MBX" to "48.393",
        "MSX" to "47.041",
        "MOB" to "30.679",
        "HMO" to "21.133",
        "VAX" to "30.89",
        "MHX" to "34.776",
        "OHX" to "36.247",
//            "LIX" to "30.337",
//        "LIX" to "0.0",
        "OKX" to "40.865",
        "AEC" to "64.512",
        "AKQ" to "36.984",
        "LNX" to "41.958",
        "TLX" to "35.333",
        "OAX" to "41.32",
        "PAH" to "37.068",
        "PDT" to "45.691",
        "DIX" to "39.947",
        "IWA" to "33.289",
        "PBZ" to "40.532",
        "SFX" to "43.106",
        "GYX" to "43.891",
        "RTX" to "45.715",
        "PUX" to "38.46",
        "RAX" to "35.665",
        "UDX" to "44.125",
        "RGX" to "39.754",
        "RIW" to "43.066",
        "FCX" to "37.024",
        "JGX" to "32.675",
        "DAX" to "38.501",
        "LSX" to "38.699",
        "MTX" to "41.263",
        "SJT" to "31.371",
        "NKX" to "32.919",
        "MUX" to "37.155",
        "HNX" to "36.314",
        "JUA" to "18.116",
        "SOX" to "33.818",
        "ATX" to "48.195",
        "SHV" to "32.451",
        "FSD" to "43.588",
        "ACG" to "56.853",
        "HKI" to "21.894",
        "HWA" to "19.095",
        "OTX" to "47.681",
        "SGF" to "37.235",
        "CCX" to "40.923",
        "LWX" to "38.976",
        "TLH" to "30.398",
        "TBW" to "27.705",
        "TWX" to "38.997",
        "EMX" to "31.894",
        "INX" to "36.175",
        "VNX" to "36.741",
        "VBX" to "34.839",
        "ICT" to "37.654",
        "LTX" to "33.989",
        "YUX" to "32.495",
        "LGX" to "47.116",
        "DGX" to "32.28",
        "VWX" to "38.26",
        "HKM" to "20.125",

        "TDTW" to "42.11111",  // Detroit (DTW), MI TDTW DTX 772
        "TADW" to "5", //  Andrews Air Force Base (ADW), MD TADW LWX 346
        "TATL" to "33.646193", // Atlanta (ATL), GA TATL FFC 1,075
        "TBNA" to "35.979079", // Nashville (BNA), TN TBNA OHX 817
        "TBOS" to "42.15806", // Boston (BOS), MA TBOS BOX 264
        "TBWI" to "39.09056", // Baltimore/Wash (BWI), MD TBWI LWX 342
        "TLVE" to "41.289372", // Cleveland (CLE), OH TLVE CLE 931
        "TCLT" to "35.337269", // Charlotte (CLT), NC TCLT GSP 871
        "TCMH" to "40.00611", // Columbus (CMH), OH TCMH ILN 1,148
        "TCVG" to "38.89778", // Covington (CVG), OH TCVG ILN 1,053
        "TDAL" to "32.92494", // Dallas Love Field (DAL), TX TDAL FWD 622
        "TDAY" to "40.021376", // Dayton (DAY), OH TDAY ILN 1,019
        "TDCA" to "38.758853", // Washington National (DCA), MD TDCA LWX 345
        "TDEN" to "39.72722", // Denver (DEN), CO TDEN BOU 5,701
        "TDFW" to "33.064286", // Dallas/Ft. Worth (DFW), TX TDFW FWD 585
        "TEWR" to "40.593397", // Newark (EWR), NJ TEWR OKX 136
        "TFLL" to "26.142601", // Fort Lauderdale (FLL), FL TFLL MFL 120
        "THOU" to "29.515852", // Houston Hobby (HOU), TX THOU HGX 116
        "TIAD" to "39.083667", // Dulles (IAD), VA TIAD LWX 473
        "TIAH" to "30.06472", // Houston International (IAH), TX TIAH HGX 253
        "TICH" to "37.506844", // Wichita (ICT), KS TICH ICT 1,350
        "TIDS" to "39.636556", // Indianapolis (IND), IN TIDS IND 847
        "TJFK" to "40.588633", // New York City (JFK), NY TJFK OKX 112
        "TLAS" to "36.144", // Las Vegas (LAS), NV TLAS VEF 2,058
        "TMCI" to "39.49861", // Kansas City (MCI), MO TMCI EAX 1,090
        "TMCO" to "28.343125", // Orlando International (MCO), FL TMCO MLB 169
        "TMDW" to "41.6514", // Chicago Midway (MDW), IL TMDW LOT 763
        "TMEM" to "34.896044", // Memphis (MEM), TN TMEM MEG 483
        "TMIA" to "25.757083", // Miami (MIA), FL TMIA MFL 125
        "TMKE" to "42.81944", // Milwaukee (MKE), WI TMKE MKX 933
        "TMSP" to "44.870902", // Minneapolis (MSP), MN TMSP MPX 1,120
        "TMSY" to "30.021389", // New Orleans (MSY), LA TMSY LIX 99
        "TOKC" to "35.27611", // Oklahoma City (OKC), OK TOKC OUN 1,308
        "TORD" to "41.796589", // Chicago O'Hare (ORD), IL TORD LOT 744
        "TPBI" to "26.687812", // West Palm Beach (PBI), FL TPBI MFL 133
        "TPHL" to "39.950061", // Philadelphia (PHL), PA TPHL PHI 153
        "TPHX" to "33.420352", // Phoenix (PHX), AZ TPHX PSR 1,089
        "TPIT" to "40.501066", // Pittsburgh (PIT), PA TPIT PBZ 1,386
        "TRDU" to "36.001401", // Raleigh Durham (RDU), NC TRDU RAH 515
        "TSDF" to "38.04581", // Louisville (SDF), KY TSDF LMK 731
        "TSJU" to "18.47394", // San Juan (SJU), PR TSJU SJU 157
        "TSLC" to "40.967222", // Salt Lake City (SLC), UT TSLC SLC 4,295
        "TSTL" to "38.804691", // St Louis (STL), MO TSTL LSX 647
        "TTPA" to "27.85867", // Tampa Bay (TPA), FL TTPA TBW 93
        "TTUL" to "36.070184", // Tulsa (TUL), OK TTUL TSA 823

//        "latest" to "36.105", // nws conus
//        "centgrtlakes" to "42.127",
//        "uppermissvly" to "42.75",
//        "northrockies" to "42.75",
//        "northeast" to "42.425",
//        "pacnorthwest" to "42.425",
//        "pacsouthwest" to "35.15",
//        "southrockies" to "32.5",
//        "southplains" to "31.6",
//        "southmissvly" to "31.6",
//        "southeast" to "29.86",
//        "hawaii" to "19.91"
    )

    private val lon = mapOf(
        "TYX" to "75.68",
        "ABR" to "98.413",
        "ENX" to "74.064",
        "ABX" to "106.824",
        "FDR" to "98.977",
        "AMA" to "101.709",
        "AHG" to "151.351",
        "GUA" to "144.811",
        "FFC" to "84.566",
        "EWX" to "98.029",
        "BBX" to "121.632",
        "ABC" to "161.876",
        "BLX" to "108.607",
        "BGM" to "75.985",
        "BMX" to "86.77",
        "BIS" to "100.76",
        "CBX" to "116.236",
        "BOX" to "71.137",
        "BRO" to "97.419",
        "BUF" to "78.737",
        "CXX" to "73.166",
        "FDX" to "103.619",
        "ICX" to "112.862",
        "CLX" to "81.042",
        "RLX" to "81.723",
        "CYS" to "104.806",
        "LOT" to "88.085",
        "ILN" to "83.822",
        "CLE" to "81.86",
        "CAE" to "81.119",
        "GWX" to "88.329",
        "CRP" to "97.511",
        "FWS" to "97.303",
        "DVN" to "90.581",
        "FTG" to "104.546",
        "DMX" to "93.723",
        "DTX" to "83.472",
        "DDC" to "99.969",
        "DOX" to "75.44",
        "DLH" to "92.21",
        "DYX" to "99.254",
        "EYX" to "117.561",
        "EVX" to "85.922",
        "EPZ" to "106.698",
        "LRX" to "116.803",
        "BHX" to "124.292",
        "APD" to "147.501",
        "FSX" to "111.198",
        "HPX" to "87.285",
        "GRK" to "97.383",
        "POE" to "92.976",
        "HDC" to "90.407",
        "EOX" to "85.459",
        "SRX" to "94.362",
        "IWX" to "85.7",
        "APX" to "84.72",
        "GGW" to "106.625",
        "GLD" to "101.7",
        "MVX" to "97.325",
        "GJX" to "108.214",
        "GRR" to "85.545",
        "TFX" to "111.385",
        "GRB" to "88.111",
        "GSP" to "82.22",
        "UEX" to "98.442",
        "HDX" to "106.12",
        "CBW" to "67.806",
        "HGX" to "95.079",
        "HTX" to "86.084",
        "IND" to "86.28",
        "JKL" to "83.313",
        "JAX" to "81.702",
        "EAX" to "94.264",
        "BYX" to "81.703",
        "AKC" to "156.627",
        "MRX" to "83.402",
        "ARX" to "91.191",
        "LCH" to "93.216",
        "ESX" to "114.891",
        "DFX" to "100.28",
        "ILX" to "89.337",
        "LZK" to "92.262",
        "VTX" to "119.179",
        "LVX" to "85.944",
        "LBB" to "101.814",
        "MQT" to "87.548",
        "MXX" to "85.79",
        "MAX" to "122.717",
        "MLB" to "80.654",
        "NQA" to "89.873",
        "AMX" to "80.413",
        "AIH" to "146.303",
        "MAF" to "102.189",
        "MKX" to "88.551",
        "MPX" to "93.565",
        "MBX" to "100.864",
        "MSX" to "113.986",
        "MOB" to "88.24",
        "HMO" to "157.18",
        "VAX" to "83.002",
        "MHX" to "76.876",
        "OHX" to "86.563",
//            "LIX" to "89.825",
//        "LIX" to "0.0",
        "OKX" to "72.864",
        "AEC" to "165.293",
        "AKQ" to "77.008",
        "LNX" to "100.576",
        "TLX" to "97.278",
        "OAX" to "96.367",
        "PAH" to "88.772",
        "PDT" to "118.853",
        "DIX" to "74.411",
        "IWA" to "111.67",
        "PBZ" to "80.218",
        "SFX" to "112.686",
        "GYX" to "70.256",
        "RTX" to "122.965",
        "PUX" to "104.181",
        "RAX" to "78.49",
        "UDX" to "102.83",
        "RGX" to "119.462",
        "RIW" to "108.477",
        "FCX" to "80.274",
        "JGX" to "83.351",
        "DAX" to "121.678",
        "LSX" to "90.683",
        "MTX" to "112.448",
        "SJT" to "100.492",
        "NKX" to "117.041",
        "MUX" to "121.898",
        "HNX" to "119.632",
        "JUA" to "66.078",
        "SOX" to "117.636",
        "ATX" to "122.496",
        "SHV" to "93.841",
        "FSD" to "96.729",
        "ACG" to "135.528",
        "HKI" to "159.552",
        "HWA" to "155.569",
        "OTX" to "117.626",
        "SGF" to "93.4",
        "CCX" to "78.004",
        "LWX" to "77.487",
        "TLH" to "84.329",
        "TBW" to "82.402",
        "TWX" to "96.232",
        "EMX" to "110.63",
        "INX" to "95.564",
        "VNX" to "98.128",
        "VBX" to "120.398",
        "ICT" to "97.443",
        "LTX" to "78.429",
        "YUX_X" to "32.495",
        "YUX" to "114.656",
        "LGX" to "124.107",
        "DGX" to "89.984",
        "VWX" to "87.724",
        "HKM" to "155.778",

        "TDTW" to "83.515",
        "TADW" to "5",
        "TATL" to "84.262233",
        "TBNA" to "86.661691",
        "TBOS" to "70.93389",
        "TBWI" to "76.63",
        "TLVE" to "82.007419",
        "TCLT" to "80.885006",
        "TCMH" to "82.71556",
        "TCVG" to "84.58028",
        "TDAL" to "96.968473",
        "TDAY" to "84.123077",
        "TDCA" to "76.961837",
        "TDEN" to "104.52639",
        "TDFW" to "96.915554",
        "TEWR" to "74.270164",
        "TFLL" to "80.34382",
        "THOU" to "95.241692",
        "TIAD" to "77.529224",
        "TIAH" to "95.5675",
        "TICH" to "97.437228",
        "TIDS" to "86.435286",
        "TJFK" to "73.880303",
        "TLAS" to "115.007",
        "TMCI" to "94.74167",
        "TMCO" to "81.324674",
        "TMDW" to "87.7294",
        "TMEM" to "89.992727",
        "TMIA" to "80.491076",
        "TMKE" to "88.04611",
        "TMSP" to "92.932257",
        "TMSY" to "90.402919",
        "TOKC" to "97.51",
        "TORD" to "87.857628",
        "TPBI" to "80.272931",
        "TPHL" to "75.069979",
        "TPHX" to "112.16318",
        "TPIT" to "80.486586",
        "TRDU" to "78.697942",
        "TSDF" to "85.610641",
        "TSJU" to "66.17891",
        "TSLC" to "111.929722",
        "TSTL" to "90.488558",
        "TTPA" to "82.51755",
        "TTUL" to "95.826313",

//        "latest" to "97.141",
//        "centgrtlakes" to "84.544",
//        "uppermissvly" to "97.10",
//        "northrockies" to "108.60",
//        "northeast" to "74.10",
//        "pacnorthwest" to "120.10",
//        "pacsouthwest" to "120.15",
//        "southrockies" to "110.50",
//        "southplains" to "100.00",
//        "southmissvly" to "90.00",
//        "southeast" to "82.658",
//        "hawaii" to "157.55"
    )

    val sites: Sites = Sites(names, lat, lon, false)
}

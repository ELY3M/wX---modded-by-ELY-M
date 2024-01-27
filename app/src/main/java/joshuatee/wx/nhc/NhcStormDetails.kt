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

package joshuatee.wx.nhc

import java.io.Serializable
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityMath
import java.util.Locale

class NhcStormDetails(
        var name: String,
        movementDir: String,
        movementSpeed: String,
        var pressure: String,
        var binNumber: String,
        var stormId: String,
        lastUpdate: String,
        var classification: String,
        var lat: String,
        var lon: String,
        var intensity: String,
        var status: String,
        advisoryUrl: String,
        val advisoryIssuanceNumber: String,
//        var forecastAdvisoryUrl: String,
//        var forecastDiscussionUrl: String,
//        var windSpeedProbabilitiesUrl: String
) : Serializable {

    var center = "$lat $lon"
    var dateTime = lastUpdate
    var movement = UtilityMath.convertWindDir(To.double(movementDir)) + " at " + movementSpeed + " mph"
    var baseUrl: String
    var goesUrl: String
    var advisoryNumber = advisoryUrl.split("/").last().replace(".shtml", "")

    init {
        val modBinNumber = stormId.substring(0, 4).uppercase()
        baseUrl = "https://www.nhc.noaa.gov/storm_graphics/" + modBinNumber.replace("AL", "AT") + "/" + stormId.uppercase(Locale.US)
        goesUrl = "https://cdn.star.nesdis.noaa.gov/FLOATER/data/" + stormId.uppercase(Locale.US) + "/GEOCOLOR/latest.jpg"
    }

    fun forTopHeader(): String = "$movement, $pressure mb, $intensity mph"

    fun summaryForNotification(): String = name + " " + classification + GlobalVariables.newline + center + GlobalVariables.newline +
            movement + GlobalVariables.newline + pressure + " mb" + GlobalVariables.newline + UtilityMath.knotsToMph(intensity) + " mph"
}

/*
https://www.weather.gov/media/notification/scn20-25tropical_javascrpt.pdf
https://www.nhc.noaa.gov/productexamples/NHC_JSON_Sample.json
https://www.nhc.noaa.gov/CurrentStorms.json
{
	"activeStorms": [
		{
			"id": "al012020",
			"binNumber": "AT1",
			"name": "Arthur",
			"classification": "TS",
			"intensity": "35",
			"pressure": "1006",
			"latitude": "29.6N",
			"longitude": "77.7W",
			"latitudeNumeric": 29.6,
			"longitudeNumeric": -77.7,
			"movementDir": 20,
			"movementSpeed": 13,
			"lastUpdate": "2020-05-17T06:00:00.000Z",
			"publicAdvisory": {
				"advNum": "000",
				"issuance": "2020-05-17T06:00:00.000Z",
				"url": "https://www.nhc.noaa.gov/text/MIATCPAT1.shtml"
			},
			"forecastAdvisory": {
				"advNum": "002",
				"issuance": "2020-05-17T03:00:00.000Z",
				"url": "https://www.nhc.noaa.gov/text/MIATCMAT1.shtml"
			},
			"windSpeedProbabilities": {
				"advNum": "002",
				"issuance": "2020-05-17T03:00:00.000Z",
				"url": "https://www.nhc.noaa.gov/text/MIAPWSAT1.shtml"
			},
			"forecastDiscussion": {
				"advNum": "002",
				"issuance": "2020-05-17T03:00:00.000Z",
				"url": "https://www.nhc.noaa.gov/text/MIATCDAT1.shtml"
			},
			"forecastGraphics": {
				"advNum": "002A",
				"issuance": "2020-05-17T06:00:00.000Z",
				"url": "https://www.nhc.noaa.gov/graphics_at1.shtml"
			},
			"forecastTrack": {
				"advNum": "002A",
				"issuance": "2020-05-17T06:00:00.000Z",
				"zipFile": "https://www.nhc.noaa.gov/gis/forecast/archive/al012020_5day_002A.zip",
				"kmzFile": "https://www.nhc.noaa.gov/storm_graphics/api/AL012020_002Aadv_TRACK.kmz"
			},
			"windWatchesWarnings": {
				"advNum": "002A",
				"issuance": "2020-05-17T06:00:00.000Z",
				"zipFile": "https://www.nhc.noaa.gov/gis/forecast/archive/al012020_5day_002A.zip",
				"kmzFile": "https://www.nhc.noaa.gov/storm_graphics/api/AL012020_002Aadv_WW.kmz"
			},
			"trackCone": {
				"advNum": "002A",
				"issuance": "2020-05-17T06:00:00.000Z",
				"zipFile": "https://www.nhc.noaa.gov/gis/forecast/archive/al012020_5day_002A.zip",
				"kmzFile": "https://www.nhc.noaa.gov/storm_graphics/api/AL012020_002Aadv_CONE.kmz"
			},
			"initialWindExtent": {
				"advNum": "002A",
				"issuance": "2020-05-17T06:00:00.000Z",
				"zipFile": "https://www.nhc.noaa.gov/gis/forecast/archive/al012020_fcst_002A.zip",
				"kmzFile": "https://www.nhc.noaa.gov/storm_graphics/api/AL012020_initialradii_002Aadv.kmz"
			},
			"forecastWindRadiiGIS": {
				"advNum": "002A",
				"issuance": "2020-05-17T06:00:00.000Z",
				"zipFile": "https://www.nhc.noaa.gov/gis/forecast/archive/al012020_fcst_002A.zip",
				"kmzFile": "https://www.nhc.noaa.gov/storm_graphics/api/AL012020_forecastradii_002Aadv.kmz"
			},
			"bestTrackGIS": {
				"zipFile": "https://www.nhc.noaa.gov/gis/best_track/al012020_best_track.zip",
				"kmzFile": "https://www.nhc.noaa.gov/gis/best_track/al012020_best_track.kmz"
			},
			"earliestArrivalTimeTSWindsGIS": {
				"advNum": "002",
				"issuance": "2020-05-17T03:00:00.000Z",
				"kmzFile": "https://www.nhc.noaa.gov/storm_graphics/api/AL012020_002adv_earliest_reasonable_toa_34.kmz"
			},
			"mostLikelyTimeTSWindsGIS": {
				"advNum": "002",
				"issuance": "2020-05-17T03:00:00.000Z",
				"kmzFile": "https://www.nhc.noaa.gov/storm_graphics/api/AL012020_002adv_most_likely_toa_34.kmz"
			},
			"windSpeedProbabilitiesGIS": {
				"issuance": "2020-05-17T00:00:00.000Z",
				"zipFile5km": "https://www.nhc.noaa.gov/gis/forecast/archive/2020051700_wsp_120hr5km.zip",
				"zipFile0p5deg": "https://www.nhc.noaa.gov/gis/forecast/archive/2020051700_wsp_120hrhalfDeg.zip",
				"kmzFile34kt": "https://www.nhc.noaa.gov/gis/forecast/archive/2020051700_wsp34knt120hr_5km.kmz",
				"kmzFile50kt": "https://www.nhc.noaa.gov/gis/forecast/archive/2020051700_wsp50knt120hr_5km.kmz",
				"kmzFile64kt": "https://www.nhc.noaa.gov/gis/forecast/archive/2020051700_wsp64knt120hr_5km.kmz"
			},
			"stormSurgeWatchWarningGIS": null,
			"potentialStormSurgeFloodingGIS": null
		}
	]
}

*/

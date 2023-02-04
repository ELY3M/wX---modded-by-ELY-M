/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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

import joshuatee.wx.Extensions.*

object UtilityAwcRadarMosaic {

    private const val baseUrl = "https://www.aviationweather.gov/data/obs/"

    val sectors = listOf(
        "us",
        "alb",
        "bwi",
        "clt",
        "tpa",
        "dtw",
        "evv",
        "mgm",
        "msp",
        "lit",
        "pir",
        "ict",
        "aus",
        "cod",
        "den",
        "abq",
        "lws",
        "wmc",
        "las",
        "ak",
        "hi",
        "carib"
    )

    val labels = listOf(
        "CONUS US",
        "Albany NY",
        "Baltimore MD",
        "Charlotte NC",
        "Tampa FL",
        "Detroit MI",
        "Evansville IN",
        "Montgomery AL",
        "Minneapolis MN",
        "Little Rock AR",
        "Pierre SD",
        "Wichita KS",
        "Austin TX",
        "Cody WY",
        "Denver CO",
        "Albuquerque NM",
        "Lewiston ID",
        "Winnemuca NV",
        "Las Vegas NV",
        "Alaska",
        "Hawaii",
        "Caribbean"
    )

//    val cityToLatLon = mapOf(
//        "Albany NY" to LatLon(42.65, -73.75),
//        "Baltimore MD" to LatLon(39.29, -76.60),
//        "Charlotte NC" to LatLon(35.22, -80.84),
//        "Tampa FL" to LatLon(27.96, -82.45),
//        "Detroit MI" to LatLon(42.33, -83.04),
//        "Evansville IN" to LatLon(37.97, -87.55),
//        "Montgomery AL" to LatLon(32.36, -86.29),
//        "Minneapolis MN" to LatLon(44.98, -93.25),
//        "Little Rock AR" to LatLon(34.74, -92.28),
//        "Pierre SD" to LatLon(44.36, -100.33),
//        "Wichita KS" to LatLon(37.69, -97.31),
//        "Austin TX" to LatLon(30.28, -97.73),
//        "Cody WY" to LatLon(44.52, -109.05),
//        "Denver CO" to LatLon(39.74, -104.99),
//        "Albuquerque NM" to LatLon(35.10, -106.62),
//        "Lewiston ID" to LatLon(46.41, -117.01),
//        "Winnemuca NV" to LatLon(40.97, -117.73),
//        "Las Vegas NV" to LatLon(36.11, -115.17),
//        "Alaska" to LatLon(59.199, -150.605),
//        "Hawaii" to LatLon(19.8181, -156.5595)
//    )

    // https://www.aviationweather.gov/data/obs/radar/rad_rala_msp.gif
    // https://www.aviationweather.gov/data/obs/radar/rad_tops-18_alb.gif
    // https://www.aviationweather.gov/data/obs/radar/rad_cref_bwi.gif

    fun get(sector: String, product: String): String {
        var baseAddOn = "radar/"
        var imageType = ".gif"
        if (product.contains("sat_")) {
            baseAddOn = "sat/us/"
            imageType = ".jpg"
        }
        return baseUrl + baseAddOn + product + "_" + sector + imageType
    }

    fun getAnimation(sector: String, product: String): List<String> {
        // image_url[14] = "/data/obs/radar/20190131/22/20190131_2216_rad_rala_dtw.gif";
        // https://www.aviationweather.gov/satellite/plot?region=us&type=wv
        var baseAddOn = "radar/"
        var baseAddOnTopUrl = "radar/"
        var imageType = ".gif"
        var topUrlAddOn = ""
        if (product.contains("sat_")) {
            baseAddOnTopUrl = "satellite/"
            baseAddOn = "sat/us/"
            imageType = ".jpg"
            topUrlAddOn = "&type=" + product.replace("sat_", "")
        } else if (product.startsWith("rad_")) {
            baseAddOnTopUrl = "radar/"
            baseAddOn = "radar/"
            imageType = ".gif"
            topUrlAddOn = "&type=" + product.replace("rad_", "")
        }
        val productUrl = "https://www.aviationweather.gov/" + baseAddOnTopUrl + "plot?region=" + sector + topUrlAddOn
        val html = productUrl.getHtml()
        val urls = html.parseColumn(
                "image_url.[0-9]{1,2}. = ./data/obs/" + baseAddOn + "([0-9]{8}/[0-9]{2}/[0-9]{8}_[0-9]{4}_" + product + "_"
                        + sector
                        + imageType + ")."
        )
        return urls.map { baseUrl + baseAddOn + it }
    }
}

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

package joshuatee.wx.vis

import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.getHtml
import joshuatee.wx.objects.LatLon
import joshuatee.wx.parse
import joshuatee.wx.parseColumn
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.util.UtilityString

object UtilityGoes {

    fun getNearest(latLon: LatLon): String = UtilityLocation.getNearest(latLon, sectorToLatLon)

    private fun getImageFileName(sector: String): String {
        val size = sizeMap[sector] ?: "latest"
        return "$size.jpg"
    }

    fun getImageGoesFloater(url: String, product: String): String =
        url.replace("GEOCOLOR", product)

    fun getImage(product: String, sector: String): String {
        var sectorLocal = if (sector == "FD" || sector == "CONUS" || sector == "CONUS-G17") {
            sector
        } else {
            "SECTOR/$sector"
        }
        var satellite = "GOES19"
        if (sectorsInGoes17.contains(sector)) {
            satellite = "GOES17"
            if (sector == "CONUS-G17") {
                sectorLocal = "CONUS"
            }
            if (sector == "FD-G17") {
                sectorLocal = "FD"
            }
        }
        // https://cdn.star.nesdis.noaa.gov/GOES16/ABI/SECTOR/cgl/03/
        // https://cdn.star.nesdis.noaa.gov/GOES16/ABI/SECTOR/cgl/12/latest.jpg
        // https://cdn.star.nesdis.noaa.gov/GOES17/ABI/CONUS/GEOCOLOR/1250x750.jpg
        // https://cdn.star.nesdis.noaa.gov/GOES16/ABI/CONUS/GEOCOLOR/1250x750.jpg
        var url =
            GlobalVariables.GOES16_URL + "/" + satellite + "/ABI/" + sectorLocal + "/" + product + "/" + getImageFileName(
                sector
            )
        if (product == "GLM") {
            url = url.replace("ABI", "GLM")
                .replace("$sectorLocal/GLM", "$sectorLocal/EXTENT3")
        }
        return url
    }

    // https://www.star.nesdis.noaa.gov/GOES/sector_band.php?sat=G17&sector=ak&band=GEOCOLOR&length=12
    // https://www.star.nesdis.noaa.gov/GOES/sector_band.php?sat=G16&sector=cgl&band=GEOCOLOR&length=12
    fun getAnimation(product: String, sector: String, frameCount: Int): List<String> {
        val frameCountString = frameCount.toString()
        val satellite = if (sectorsInGoes17.contains(sector)) {
            "G17"
        } else {
            "G19"
        }
        val productLocal = product.replace("GLM", "EXTENT3")
        val url = when (sector) {
            // https://www.star.nesdis.noaa.gov/GOES/fulldisk_band.php?sat=G17&band=GEOCOLOR&length=12
            "FD", "FD-G17" -> GlobalVariables.GOES16_ANIM_URL + "/GOES/fulldisk_band.php?sat=$satellite&band=$productLocal&length=$frameCountString"
            "CONUS", "CONUS-G17" -> GlobalVariables.GOES16_ANIM_URL + "/GOES/conus_band.php?sat=$satellite&band=$productLocal&length=$frameCountString"
            else -> GlobalVariables.GOES16_ANIM_URL + "/GOES/sector_band.php?sat=$satellite&sector=$sector&band=$productLocal&length=$frameCountString"
        }
        val html = url.getHtml()
            .replace("\n", "")
            .replace("\r", "")
        val imageHtml = html.parse("animationImages = \\[(.*?)\\];")
        return imageHtml.parseColumn("'(https.*?jpg)'")
    }

    fun getAnimationGoesFloater(product: String, url: String, frameCount: Int): List<String> {
        val baseUrl = url.replace("GEOCOLOR", product).replace("latest.jpg", "")
        val html = baseUrl.getHtml()
        val urlList = UtilityString.parseColumn(
            html.replace("\r\n", " "),
            "<a href=\"([^\\s]*?1000x1000.jpg)\">"
        )
        val returnList = mutableListOf<String>()
        if (urlList.size > frameCount) {
            ((urlList.size - frameCount) until urlList.size).forEach {
                returnList.add(baseUrl + urlList[it])
            }
        }
        return returnList
    }

    val labels = listOf(
        "00 True color daytime, multispectral IR at night",
        "00.47 um (Band 1) Blue - Visible",
        "00.64 um (Band 2) Red - Visible",
        "00.86 um (Band 3) Veggie - Near IR",
        "01.37 um (Band 4) Cirrus - Near IR",
        "01.6 um (Band 5) Snow/Ice - Near IR",
        "02.2 um (Band 6) Cloud Particle - Near IR",
        "03.9 um (Band 7) Shortwave Window - IR",
        "06.2 um (Band 8) Upper-Level Water Vapor - IR",
        "06.9 um (Band 9) Mid-Level Water Vapor - IR",
        "07.3 um (Band 10) Lower-level Water Vapor - IR",
        "08.4 um (Band 11) Cloud Top - IR",
        "09.6 um (Band 12) Ozone - IR",
        "10.3 um (Band 13) Clean Longwave Window - IR",
        "11.2 um (Band 14) Longwave Window - IR",
        "12.3 um (Band 15) Dirty Longwave Window - IR",
        "13.3 um (Band 16) CO2 Longwave - IR",
        "AirMass - RGB composite based on the data from IR and WV",
        "Sandwich RGB - Bands 3 and 13 combo",
        "Day Cloud Phase",
        "Night Microphysics",
        "Fire Temperature",
        "Dust RGB",
        "GLM FED+GeoColor",
        "DMW"
    )

    val codes = listOf(
        "GEOCOLOR",
        "01",
        "02",
        "03",
        "04",
        "05",
        "06",
        "07",
        "08",
        "09",
        "10",
        "11",
        "12",
        "13",
        "14",
        "15",
        "16",
        "AirMass",
        "Sandwich",
        "DayCloudPhase",
        "NightMicrophysics",
        "FireTemperature",
        "Dust",
        "GLM",
        "DMW"
    )

    private val sectorsInGoes17 = listOf(
        "CONUS-G17",
        "FD-G17",
        "ak",
        "cak",
        "sea",
        "hi",
        "pnw",
        "psw",
        "tpw",
        "tsp",
        "wus",
        "np"
    )

    val sectorToName = mapOf(
        "FD" to "Full Disk: GOES-EAST",
        "FD-G17" to " Full Disk: GOES-WEST",
        "CONUS" to "CONUS: GOES-EAST",
        "CONUS-G17" to "PACUS: GOES-WEST",
        "pnw" to "Pacific Northwest",
        "nr" to "Northern Rockies",
        "umv" to "Upper Mississippi Valley",
        "cgl" to "Central Great Lakes",
        "ne" to "Northeast",
        "psw" to "Pacific Southwest",
        "sr" to "Southern Rockies",
        "sp" to "Southern Plains",
        "smv" to "Southern Mississippi Valley",
        "se" to "Southeast",
        "gm" to "Gulf of Mexico",
        "car" to "Caribbean",
        "eus" to "U.S. Atlantic Coast",
        "pr" to "Puerto Rico",
        "cam" to "Central America",
        "taw" to "Tropical Atlantic",
        "ak" to "Alaska",
        "cak" to "Central Alaska",
        "sea" to "Southeastern Alaska",
        "hi" to "Hawaii",
        "wus" to "US Pacific Coast",
        "tpw" to "Tropical Pacific",
        "tsp" to "South Pacific",
        "eep" to "Eastern Pacific",
        "np" to "Northern Pacific",
        "na" to "Northern Atlantic",
        "can" to "Canada",
        "mex" to "Mexico",
        "nsa" to "South America (north)",
        "ssa" to "South America (south)"
    )

    private val sizeMap = mapOf(
        "CONUS-G17" to "1250x750",
        "CONUS" to "1250x750",
        "FD" to "1808x1808",
        "FD-G17" to "1808x1808",
        "gm" to "1000x1000",
        "car" to "1000x1000",
        "eus" to "1000x1000",
        "taw" to "1800x1080",
        "tpw" to "1800x1080",
        "tsp" to "1800x1080",
        "can" to "1125x560",
        "mex" to "1000x1000",
        "cam" to "1000x1000",
        "eep" to "1800x1080",
        "wus" to "1000x1000",
        "nsa" to "1800x1080",
        "ssa" to "1800x1080",
        "np" to "1800x1080",
        "na" to "1800x1080",
        "ak" to "1000x1000",
        "cak" to "1200x1200",
        "sea" to "1200x1200",
        "hi" to "1200x1200"
    )

    private val sectorToLatLon = mapOf(
        "cgl" to LatLon(39.123405, -82.532938),  // wellston, Oh
        "ne" to LatLon(39.360611, -74.431877),   // Atlantic City, NJ
        "umv" to LatLon(40.622777, -93.934116),  // Lamoni, IA
        "pnw" to LatLon(41.589703, -119.858865), // Vya, NV
        "psw" to LatLon(38.524448, -118.623611), // Hawthorne, NV
        "nr" to LatLon(41.139980, -104.820244),  // Cheyenne, Wy
        "sr" to LatLon(34.653376, -108.677852),  // Fence Lake, NM
        "sp" to LatLon(31.463787, -96.058022),   // Buffalo, TX
        "smv" to LatLon(31.326460, -89.289658),  // Hattiesburg, MS
        "se" to LatLon(30.332184, -81.655647),   // Jacksonville, FL
        "pr" to LatLon(18.22919, -66.03470),     // Caguas, PR
        "hi" to LatLon(20.764, -155.333),        // Hawaii
        "tpw" to LatLon(7.074, -129.370),        // Tropical Pacific (best for American Samoa)
    )
}

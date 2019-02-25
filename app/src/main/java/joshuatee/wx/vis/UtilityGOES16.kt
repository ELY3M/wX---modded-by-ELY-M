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
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable

import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication
import joshuatee.wx.util.UtilityLog

object UtilityGOES16 {

    fun getImage(product: String, sector: String): Bitmap {
        var sectorLocal = "SECTOR/$sector"
        if (sector == "FD" || sector == "CONUS" || sector == "CONUS-G17") {
            sectorLocal = sector
        }
        var satellite = "GOES16"
        if (sectorsInGoes17.contains(sector)) {
            satellite = "GOES17"
            if (sector == "CONUS-G17") {
                sectorLocal = "CONUS"
            }
        }
        // https://cdn.star.nesdis.noaa.gov/GOES16/ABI/SECTOR/cgl/03/
        // https://cdn.star.nesdis.noaa.gov/GOES16/ABI/SECTOR/cgl/12/latest.jpg
        // https://cdn.star.nesdis.noaa.gov/GOES17/ABI/CONUS/GEOCOLOR/1250x750.jpg
        // https://cdn.star.nesdis.noaa.gov/GOES16/ABI/CONUS/GEOCOLOR/1250x750.jpg
        val url =
            MyApplication.goes16Url + "/" + satellite + "/ABI/" + sectorLocal + "/" + product + "/latest.jpg"
        UtilityLog.d("wx", url)
        return url.getImage()
    }

    // https://www.star.nesdis.noaa.gov/GOES/sector_band.php?sat=G17&sector=ak&band=GEOCOLOR&length=12
    // https://www.star.nesdis.noaa.gov/GOES/sector_band.php?sat=G16&sector=cgl&band=GEOCOLOR&length=12
    fun getAnimation(
        context: Context,
        product: String,
        sector: String,
        frameCount: Int
    ): AnimationDrawable {
        val frameCountString = frameCount.toString()
        var satellite = "G16"
        if (sectorsInGoes17.contains(sector)) {
            satellite = "G17"
        }
        val url = when (sector) {
            "FD" -> MyApplication.goes16AnimUrl + "/GOES/GOES16_FullDisk_Band.php?band=$product&length=$frameCountString"
            "CONUS", "CONUS-G17" -> MyApplication.goes16AnimUrl + "/GOES/conus_band.php?sat=$satellite&band=$product&length=$frameCountString"
            else -> MyApplication.goes16AnimUrl + "/GOES/sector_band.php?sat=$satellite&sector=$sector&band=$product&length=$frameCountString"
        }
        val html = url.getHtml().replace("\n", "").replace("\r", "")
        val imageHtml = html.parse("animationImages = \\[(.*?)\\];")
        val imageUrls = imageHtml.parseColumn("'(https.*?jpg)'")
        val bitmaps = imageUrls.map { it.getImage() }
        return UtilityImgAnim.getAnimationDrawableFromBMList(
            context,
            bitmaps,
            UtilityImg.animInterval(context)
        )

    }

    val labels: List<String> = listOf(
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
        "13.3 um (Band 16) CO2 Longwave - IR"
    )

    val codes: List<String> = listOf(
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
        "16"
    )

    private val sectorsInGoes17: List<String> = listOf(
        "CONUS-G17",
        "ak",
        "hi",
        "pnw",
        "psw",
        "tpw",
        "wus",
        "eep"
    )
}


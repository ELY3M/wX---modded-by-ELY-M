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

package joshuatee.wx.vis

import android.content.Context
import android.graphics.drawable.AnimationDrawable

import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim

import joshuatee.wx.Extensions.*

object UtilityGOES16 {

    const val size: String = "600x600"

    private val sizeMap = mapOf(
            "CONUS" to "2500x1500",
            "FD" to "1808x1808",
            "gm" to "1000x1000",
            "car" to "1000x1000",
            "eus" to "1000x1000",
            "taw" to "1800x1080"
    )

    private fun getImageSize(sector: String) = sizeMap[sector] ?: "1200x1200"

    fun getUrl(product: String, sector: String): List<String> {
        val url = when (sector) {
            "FD" -> "https://www.star.nesdis.noaa.gov/GOES/GOES16_FullDisk.php"
            "CONUS" -> "https://www.star.nesdis.noaa.gov/GOES/GOES16_CONUS.php"
            else -> "https://www.star.nesdis.noaa.gov/GOES/GOES16_sectors.php?sector=$sector"
        }
        var sectorLocal = "SECTOR/$sector"
        if (sector == "FD" || sector == "CONUS") {
            sectorLocal = sector
        }
        val html = url.getHtml()
        val parseString = "href=.(https://cdn.star.nesdis.noaa.gov/GOES16/ABI/" + sectorLocal + "/" + product + "/[0-9]{11}_GOES16-ABI-" + sector + "-" + product + "-" + getImageSize(sector) + ".jpg).>"
        val imgUrl = html.parse(parseString)
        val timeStamp = imgUrl.parse("$product/([0-9]{11})_GOES16-ABI-$sector")
        return listOf(imgUrl, timeStamp)
    }

    fun getAnimation(context: Context, product: String, sector: String, frameCnt: Int): AnimationDrawable {
        val frameCount = frameCnt.toString()
        val url = when (sector) {
            "FD" -> "https://www.star.nesdis.noaa.gov/GOES/GOES16_FullDisk_Band.php?band=$product&length=$frameCount"
            "CONUS" -> "https://www.star.nesdis.noaa.gov/GOES/GOES16_CONUS_Band.php?band=$product&length=$frameCount"
            else -> "https://www.star.nesdis.noaa.gov/GOES/GOES16_sector_band.php?sector=$sector&band=$product&length=$frameCount"
        }
        val html = url.getHtml().replace("\n", "").replace("\r", "")
        val imageHtml = html.parse("animationImages = \\[(.*?)\\];")
        val imageUrls = imageHtml.parseColumn("'(https.*?jpg)'")
        val bitmaps = imageUrls.map { it.getImage() }
        return UtilityImgAnim.getAnimationDrawableFromBMList(context, bitmaps, UtilityImg.animInterval(context))
    }

    val labelToCode: Map<String, String> = mapOf(
            "00 True color daytime, multispectral IR at night" to "GEOCOLOR",
            "00.47 um (Band 1) Blue - Visible" to "01",
            "00.64 um (Band 2) Red - Visible" to "02",
            "00.86 um (Band 3) Veggie - Near IR" to "03",
            "01.37 um (Band 4) Cirrus - Near IR" to "04",
            "01.6 um (Band 5) Snow/Ice - Near IR" to "05",
            "02.2 um (Band 6) Cloud Particle - Near IR" to "06",
            "03.9 um (Band 7) Shortwave Window - IR" to "07",
            "06.2 um (Band 8) Upper-Level Water Vapor - IR" to "08",
            "06.9 um (Band 9) Mid-Level Water Vapor - IR" to "09",
            "07.3 um (Band 10) Lower-level Water Vapor - IR" to "10",
            "08.4 um (Band 11) Cloud Top - IR" to "11",
            "09.6 um (Band 12) Ozone - IR" to "12",
            "10.3 um (Band 13) Clean Longwave Window - IR" to "13",
            "11.2 um (Band 14) Longwave Window - IR" to "14",
            "12.3 um (Band 15) Dirty Longwave Window - IR" to "15",
            "13.3 um (Band 16) CO2 Longwave - IR" to "16"
    )
}


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

package joshuatee.wx.radar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable

import joshuatee.wx.util.UtilityImgAnim

import joshuatee.wx.Extensions.*

object UtilityAwcRadarMosaic {

    private const val baseUrl: String = "https://www.aviationweather.gov/data/obs/"

    val sectors: List<String> = listOf(
        "us",
        "alb",
        "bwi",
        "clt",
        "tpa",
        "dtw",
        "evv",
        "mgm",
        "lit",
        "pir",
        "ict",
        "aus",
        "cod",
        "den",
        "abq",
        "lws",
        "wmc",
        "las"
    )

    val labels: List<String> = listOf(
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
        "Las Vegas NV"
    )

    // https://www.aviationweather.gov/data/obs/radar/rad_rala_msp.gif
    // https://www.aviationweather.gov/data/obs/radar/rad_tops-18_alb.gif
    // https://www.aviationweather.gov/data/obs/radar/rad_cref_bwi.gif

    fun get(sector: String, product: String): Bitmap {
        var baseAddOn = "radar/"
        var imageType = ".gif"
        if (product.contains("sat_")) {
            baseAddOn = "sat/us/"
            imageType = ".jpg"
        }
        val url = baseUrl + baseAddOn + product + "_" + sector + imageType
        return url.getImage()
    }

    fun getAnimation(context: Context, sector: String, product: String): AnimationDrawable {
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
        val bitmaps = urls.map { (baseUrl + baseAddOn + it).getImage() }
        return UtilityImgAnim.getAnimationDrawableFromBMList(context, bitmaps)
    }
}

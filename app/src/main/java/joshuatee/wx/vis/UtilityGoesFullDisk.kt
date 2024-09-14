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

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim

@Suppress("SpellCheckingInspection")
internal object UtilityGoesFullDisk {

    // https://www.ospo.noaa.gov/products/imagery/meteosat.html
    // https://www.ospo.noaa.gov/products/imagery/meteosatio.html
    // https://www.ospo.noaa.gov/products/imagery/fulldisk.html

    // Animation starters
    // https://www.ssd.noaa.gov/eumet/eatl/txtfiles/bd_names.txt
    // https://www.ssd.noaa.gov/eumet/neatl/txtfiles/
    // https://www.ssd.noaa.gov/eumet/indiano/txtfiles/
    //
    // for Himawari
    // Latest - https://www.ospo.noaa.gov/dimg/jma/fd/rb/10.gif
    // Oldest - https://www.ospo.noaa.gov/dimg/jma/fd/rb/10.gif

    val labels = listOf(
        "Eastern Atlantic Infrared",
        "Eastern Atlantic Infrared 2",
        "Eastern Atlantic Visible",
        "Eastern Atlantic Water Vapor",
        "Eastern Atlantic IR channel 4",
        "Eastern Atlantic AVN",
        "Eastern Atlantic Dvorak",
        "Eastern Atlantic JSL",
        "Eastern Atlantic RGB",
        "Eastern Atlantic Funktop",
        "Eastern Atlantic Rainbow",

        "Northeast Atlantic Infrared",
        "Northeast Atlantic Infrared 2",
        "Northeast Atlantic Visible",
        "Northeast Atlantic Water Vapor",
        "Northeast Atlantic IR channel 4",
        "Northeast Atlantic AVN",
        "Northeast Atlantic Dvorak",
        "Northeast Atlantic JSL",
        "Northeast Atlantic RGB",
        "Northeast Atlantic Funktop",
        "Northeast Atlantic Rainbow",

        "India Ocean Infrared",
        "India Ocean Infrared 2",
        "India Ocean Visible",
        "India Ocean Water Vapor",
        "India Ocean IR channel 4",
        "India Ocean AVN",
        "India Ocean Dvorak",
        "India Ocean JSL",
        "India Ocean RGB",
        "India Ocean Funktop",
        "India Ocean Rainbow",

        "Himawari Infrared",
        "Himawari IR, Ch. 4",
        "Himawari Water Vapor",
        "Himawari Water Vapor (Blue)",
        "Himawari Visible",
        "Himawari AVN Infrared",
        "Himawari Funktop Infrared",
        "Himawari RBTop Infrared, Ch. 4",

        "Himawari, West Central Pacific Infrared",
        "Himawari, West Central Pacific Visible",
        "Himawari, West Central Pacific Water Vapor",
        "Himawari, West Central Pacific Enhanced IR",

        "Himawari, Northwest Pacific Wide View Infrared",
        "Himawari, Northwest Pacific Wide View IR2",
        "Himawari, Northwest Pacific Wide View Visible",
        "Himawari, Northwest Pacific Wide View Water Vapor",

        "Himawari, West Pacific Infrared",
        "Himawari, West Pacific IR2",
        "Himawari, West Pacific Visible",
        "Himawari, West Pacific Water Vapor",

        "Himawari, Central Pacific IR2",
        "Himawari, Central Pacific Visible",
        "Himawari, Central Pacific Water Vapor",
        "Himawari, Central Pacific IR channel 4",
        "Himawari, Central Pacific AVN",
        "Himawari, Central Pacific Dvorak",
        "Himawari, Central Pacific JSL",
        "Himawari, Central Pacific RGB",
        "Himawari, Central Pacific Funktop",
        "Himawari, Central Pacific Rainbow",

//        "Himawari, Tropical West Pacific IR2",
//        "Himawari, Tropical West Pacific Visible",
//        "Himawari, Tropical West Pacific Water Vapor",
        "Himawari, Tropical West Pacific IR channel 4",
        "Himawari, Tropical West Pacific AVN",
        "Himawari, Tropical West Pacific Dvorak",
        "Himawari, Tropical West Pacific JSL",
        "Himawari, Tropical West Pacific RGB",
        "Himawari, Tropical West Pacific Funktop",
        "Himawari, Tropical West Pacific Rainbow",

        "Himawari, NorthWest Pacific IR channel 4",
        "Himawari, NorthWest Pacific AVN",
        "Himawari, NorthWest Pacific Dvorak",
        "Himawari, NorthWest Pacific JSL",
        "Himawari, NorthWest Pacific RGB",
        "Himawari, NorthWest Pacific Funktop",
        "Himawari, NorthWest Pacific Rainbow",

        "Himawari, Palau",
        "Himawari, Yap",
        "Himawari, Marianas",
        "Himawari, Chuuk",
        "Himawari, Pohnpei/Kosrae",
        "Himawari, Marshall Islands",
        "Himawari, Philippines",

        // TODO FIXME https://www.ospo.noaa.gov/products/imagery/sohemi.html

        "Himawari, Fiji",
        "Himawari, Tuvalu/Wallis and Futuna",
        "Himawari, Solomon Islands",
        "Himawari, Vanuatu and New Caledonia",
        "Himawari, Samoa",
        "Himawari, Tonga and Niue",
        "Himawari, New Guinea",
        "Himawari, New Zealand",
    )

    val urls = listOf(
        "https://www.ssd.noaa.gov/eumet/eatl/rb.jpg",
        "https://www.ssd.noaa.gov/eumet/eatl/ir2.jpg",
        "https://www.ssd.noaa.gov/eumet/eatl/vis.jpg",
        "https://www.ssd.noaa.gov/eumet/eatl/wv.jpg",
        "https://www.ssd.noaa.gov/eumet/eatl/ir4.jpg",
        "https://www.ssd.noaa.gov/eumet/eatl/avn.jpg",
        "https://www.ssd.noaa.gov/eumet/eatl/bd.jpg",
        "https://www.ssd.noaa.gov/eumet/eatl/jsl.jpg",
        "https://www.ssd.noaa.gov/eumet/eatl/rgb.jpg",
        "https://www.ssd.noaa.gov/eumet/eatl/ft.jpg",
        "https://www.ssd.noaa.gov/eumet/eatl/rb.jpg",

        "https://www.ssd.noaa.gov/eumet/neatl/rb.jpg",
        "https://www.ssd.noaa.gov/eumet/neatl/ir2.jpg",
        "https://www.ssd.noaa.gov/eumet/neatl/vis.jpg",
        "https://www.ssd.noaa.gov/eumet/neatl/wv.jpg",
        "https://www.ssd.noaa.gov/eumet/neatl/ir4.jpg",
        "https://www.ssd.noaa.gov/eumet/neatl/avn.jpg",
        "https://www.ssd.noaa.gov/eumet/neatl/bd.jpg",
        "https://www.ssd.noaa.gov/eumet/neatl/jsl.jpg",
        "https://www.ssd.noaa.gov/eumet/neatl/rgb.jpg",
        "https://www.ssd.noaa.gov/eumet/neatl/ft.jpg",
        "https://www.ssd.noaa.gov/eumet/neatl/rb.jpg",

        "https://www.ssd.noaa.gov/eumet/indiano/rb.jpg",
        "https://www.ssd.noaa.gov/eumet/indiano/ir2.jpg",
        "https://www.ssd.noaa.gov/eumet/indiano/vis.jpg",
        "https://www.ssd.noaa.gov/eumet/indiano/wv.jpg",
        "https://www.ssd.noaa.gov/eumet/indiano/ir4.jpg",
        "https://www.ssd.noaa.gov/eumet/indiano/avn.jpg",
        "https://www.ssd.noaa.gov/eumet/indiano/bd.jpg",
        "https://www.ssd.noaa.gov/eumet/indiano/jsl.jpg",
        "https://www.ssd.noaa.gov/eumet/indiano/rgb.jpg",
        "https://www.ssd.noaa.gov/eumet/indiano/ft.jpg",
        "https://www.ssd.noaa.gov/eumet/indiano/rb.jpg",

        "https://www.ospo.noaa.gov/dimg/jma/fd/rb/10.gif",
        "https://www.ospo.noaa.gov/dimg/jma/fd/ir4/10.gif",
        "https://www.ospo.noaa.gov/dimg/jma/fd/wv/10.gif",
        "https://www.ospo.noaa.gov/dimg/jma/fd/wvblue/10.gif",
        "https://www.ospo.noaa.gov/dimg/jma/fd/vis/10.gif",
        "https://www.ospo.noaa.gov/dimg/jma/fd/avn/10.gif",
        "https://www.ospo.noaa.gov/dimg/jma/fd/ft/10.gif",
        "https://www.ospo.noaa.gov/dimg/jma/fd/rbtop/10.gif",

        "https://www.ospo.noaa.gov/Products/imagery/guam/GUAMIR.JPG",
        "https://www.ospo.noaa.gov/Products/imagery/guam/GUAMVS.JPG",
        "https://www.ospo.noaa.gov/Products/imagery/guam/GUAMWV.JPG",
        "https://www.ospo.noaa.gov/Products/imagery/guam/GUAMCOL.JPG",

        "https://www.ssd.noaa.gov/jma/nwpac/rb.gif",
        "https://www.ssd.noaa.gov/jma/nwpac/ir2.gif",
        "https://www.ssd.noaa.gov/jma/nwpac/vis.gif",
        "https://www.ssd.noaa.gov/jma/nwpac/wv.gif",

        "https://www.ssd.noaa.gov/jma/wpac/rb.gif",
        "https://www.ssd.noaa.gov/jma/wpac/ir2.gif",
        "https://www.ssd.noaa.gov/jma/wpac/vis.gif",
        "https://www.ssd.noaa.gov/jma/wpac/wv.gif",

        "https://www.ssd.noaa.gov/jma/wcpac/ir2.gif",
        "https://www.ssd.noaa.gov/jma/wcpac/vis.gif",
        "https://www.ssd.noaa.gov/jma/wcpac/wv.gif",
        "https://www.ssd.noaa.gov/jma/wcpac/ir4.gif",
        "https://www.ssd.noaa.gov/jma/wcpac/avn.gif",
        "https://www.ssd.noaa.gov/jma/wcpac/bd.gif",
        "https://www.ssd.noaa.gov/jma/wcpac/jsl.gif",
        "https://www.ssd.noaa.gov/jma/wcpac/rgb.jpg",
        "https://www.ssd.noaa.gov/jma/wcpac/ft.gif",
        "https://www.ssd.noaa.gov/jma/wcpac/rb.gif",

//        "https://www.ssd.noaa.gov/jma/twpac/ir2.gif",
//        "https://www.ssd.noaa.gov/jma/twpac/vis.gif",
//        "https://www.ssd.noaa.gov/jma/twpac/wv.gif",
        "https://www.ssd.noaa.gov/jma/twpac/ir4.gif",
        "https://www.ssd.noaa.gov/jma/twpac/avn.gif",
        "https://www.ssd.noaa.gov/jma/twpac/bd.gif",
        "https://www.ssd.noaa.gov/jma/twpac/jsl.gif",
        "https://www.ssd.noaa.gov/jma/twpac/rgb.jpg",
        "https://www.ssd.noaa.gov/jma/twpac/ft.gif",
        "https://www.ssd.noaa.gov/jma/twpac/rb.gif",

        "https://www.ssd.noaa.gov/jma/nwpac/ir4.gif",
        "https://www.ssd.noaa.gov/jma/nwpac/avn.gif",
        "https://www.ssd.noaa.gov/jma/nwpac/bd.gif",
        "https://www.ssd.noaa.gov/jma/nwpac/jsl.gif",
        "https://www.ssd.noaa.gov/jma/nwpac/rgb.jpg",
        "https://www.ssd.noaa.gov/jma/nwpac/ft.gif",
        "https://www.ssd.noaa.gov/jma/nwpac/rb.gif",

        "https://www.ospo.noaa.gov/Products/imagery/guam/GUAMB.JPG",
        "https://www.ospo.noaa.gov/Products/imagery/guam/GUAMC.JPG",
        "https://www.ospo.noaa.gov/Products/imagery/guam/GUAMD.JPG",
        "https://www.ospo.noaa.gov/Products/imagery/guam/GUAME.JPG",
        "https://www.ospo.noaa.gov/Products/imagery/guam/GUAMF.JPG",
        "https://www.ospo.noaa.gov/Products/imagery/guam/GUAMG.JPG",
        "https://www.ospo.noaa.gov/Products/imagery/sohemi/PHILVIS.JPG",

        // TODO FIXME https://www.ospo.noaa.gov/products/imagery/sohemi.html

        "https://www.ospo.noaa.gov/Products/imagery/sohemi/SHGMSB.JPG",
        "https://www.ospo.noaa.gov/Products/imagery/sohemi/SHGMSC.JPG",
        "https://www.ospo.noaa.gov/Products/imagery/sohemi/SHGMSD.JPG",
        "https://www.ospo.noaa.gov/Products/imagery/sohemi/SHGMSF.JPG",
        "https://www.ospo.noaa.gov/img/samoa_.PNG",
        "https://cdn.star.nesdis.noaa.gov/GOES18/ABI/SECTOR/tsp/Sandwich/900x540.jpg",
        "https://www.ospo.noaa.gov/Products/imagery/sohemi/SHGMSG.JPG",
        "https://www.ospo.noaa.gov/Products/imagery/sohemi/NEWZVIS.JPG",
    )

    fun getAnimation(context: Context, urlOriginal: String): AnimationDrawable {
        if (urlOriginal.contains("jma") && urlOriginal.endsWith("10.gif")) {
            val url = urlOriginal.replace("10.gif", "")
            val count = 10
            val urls = (1 until count + 1).map { "$url$it.gif" }
            return UtilityImgAnim.getAnimationDrawableFromUrlList(
                context,
                urls,
                UtilityImg.animInterval(context)
            )
        } else if (!urlOriginal.contains("GUAM") && !urlOriginal.contains("https://www.ospo.noaa.gov/Products/imagery/sohemi")) {
            val product = urlOriginal.split("/").last().split(".").first()
            val urlBase = urlOriginal.replace("$product.jpg", "").replace("$product.gif", "")
            val urlFile = urlBase + "txtfiles/" + product + "_names.txt"
            val html = UtilityIO.getHtml(urlFile)
            val lines = html.split(GlobalVariables.newline)
            val urls = mutableListOf<String>()
            lines.forEach {
                val tokens = it.split(" ")
                urls.add(urlBase + tokens.first())
            }
            return UtilityImgAnim.getAnimationDrawableFromUrlList(
                context,
                urls,
                UtilityImg.animInterval(context)
            )
        } else { // TODO FIXME Guam https://www.ospo.noaa.gov/Products_imagery/guam/guamloops/palau.cfg
            return AnimationDrawable()
        }
    }
}

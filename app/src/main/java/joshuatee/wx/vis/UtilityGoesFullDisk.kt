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
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim

@Suppress("SpellCheckingInspection")
internal object UtilityGoesFullDisk {

    val labels = listOf(
            "Meteosat Infrared",
            "Meteosat Visible",
            "Meteosat India Ocean Infrared",
            "Meteosat India Ocean Visible",
            "Himawari-8 Infrared",
            "Himawari-8 IR, Ch. 4",
            "Himawari-8 Water Vapor",
            "Himawari-8 Water Vapor (Blue)",
            "Himawari-8 Visible",
            "Himawari-8 AVN Infrared",
            "Himawari-8 Funktop Infrared",
            "Himawari-8 RBTop Infrared, Ch. 4"
    )

    val urls = listOf(
            "${GlobalVariables.NWS_GOES_WEBSITE_PREFIX}/FULLDISK/GMIR.JPG",
            "${GlobalVariables.NWS_GOES_WEBSITE_PREFIX}/FULLDISK/GMVS.JPG",
            "${GlobalVariables.NWS_GOES_WEBSITE_PREFIX}/FULLDISK/GIIR.JPG",
            "${GlobalVariables.NWS_GOES_WEBSITE_PREFIX}/FULLDISK/GIVS.JPG",
            "${GlobalVariables.NWS_GOES_WEBSITE_PREFIX}/dimg/jma/fd/rb/10.gif",
            "${GlobalVariables.NWS_GOES_WEBSITE_PREFIX}/dimg/jma/fd/ir4/10.gif",
            "${GlobalVariables.NWS_GOES_WEBSITE_PREFIX}/dimg/jma/fd/wv/10.gif",
            "${GlobalVariables.NWS_GOES_WEBSITE_PREFIX}/dimg/jma/fd/wvblue/10.gif",
            "${GlobalVariables.NWS_GOES_WEBSITE_PREFIX}/dimg/jma/fd/vis/10.gif",
            "${GlobalVariables.NWS_GOES_WEBSITE_PREFIX}/dimg/jma/fd/avn/10.gif",
            "${GlobalVariables.NWS_GOES_WEBSITE_PREFIX}/dimg/jma/fd/ft/10.gif",
            "${GlobalVariables.NWS_GOES_WEBSITE_PREFIX}/dimg/jma/fd/rbtop/10.gif"
    )

    fun getAnimation(context: Context, urlOriginal: String): AnimationDrawable {
        val url = urlOriginal.replace("10.gif", "")
        val count = 10
        val urls = (1 until count + 1).map { "$url$it.gif" }
        return UtilityImgAnim.getAnimationDrawableFromUrlList(context, urls, UtilityImg.animInterval(context))
    }
}

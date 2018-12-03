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
import joshuatee.wx.MyApplication

import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim

internal object UtilityNWSGOESFullDisk {

    val labels = listOf(
            "GOES West Infrared",
            "GOES West Visible",
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
            "${MyApplication.nwsGoesWebsitePrefix}/FULLDISK/GWIR.JPG",
            "${MyApplication.nwsGoesWebsitePrefix}/FULLDISK/GWVS.JPG",
            "${MyApplication.nwsGoesWebsitePrefix}/FULLDISK/GMIR.JPG",
            "${MyApplication.nwsGoesWebsitePrefix}/FULLDISK/GMVS.JPG",
            "${MyApplication.nwsGoesWebsitePrefix}/FULLDISK/GIIR.JPG",
            "${MyApplication.nwsGoesWebsitePrefix}/FULLDISK/GIVS.JPG",
            "${MyApplication.nwsGoesWebsitePrefix}/dimg/jma/fd/rb/10.gif",
            "${MyApplication.nwsGoesWebsitePrefix}/dimg/jma/fd/ir4/10.gif",
            "${MyApplication.nwsGoesWebsitePrefix}/dimg/jma/fd/wv/10.gif",
            "${MyApplication.nwsGoesWebsitePrefix}/dimg/jma/fd/wvblue/10.gif",
            "${MyApplication.nwsGoesWebsitePrefix}/dimg/jma/fd/vis/10.gif",
            "${MyApplication.nwsGoesWebsitePrefix}/dimg/jma/fd/avn/10.gif",
            "${MyApplication.nwsGoesWebsitePrefix}/dimg/jma/fd/ft/10.gif",
            "${MyApplication.nwsGoesWebsitePrefix}/dimg/jma/fd/rbtop/10.gif"
    )

    fun getAnimation(context: Context, urlF: String): AnimationDrawable {
        val url = urlF.replace("10.gif", "")
        val frameCntInt = 10
        val urlAl = (1 until frameCntInt + 1).mapTo(mutableListOf()) { "$url$it.gif" }
        return UtilityImgAnim.getAnimationDrawableFromURLList(context, urlAl, UtilityImg.animInterval(context))
    }
}


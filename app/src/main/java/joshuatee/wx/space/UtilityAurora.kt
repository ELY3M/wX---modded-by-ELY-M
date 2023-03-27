/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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
//made by ELY M.

package joshuatee.wx.space

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import joshuatee.wx.MyApplication
import joshuatee.wx.common.GlobalVariables

import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim

internal object UtilityAurora {

    val labels = listOf(
            "Aurora Forecast - North",
            "Aurora Forecast - South",
            "Estimated Planetary K index"
    )

    val urls = listOf(
        "${GlobalVariables.nwsSwpcWebSitePrefix}/images/animations/ovation/north/latest.jpg",
        "${GlobalVariables.nwsSwpcWebSitePrefix}/images/animations/ovation/south/latest.jpg",
        "${GlobalVariables.nwsSwpcWebSitePrefix}/images/planetary-k-index.gif"
    )

/*
    fun getAnimation(context: Context, urlOriginal: String): AnimationDrawable {
        val url = urlOriginal.replace("10.gif", "")
        val count = 10
        val urls = (1 until count + 1).map { "$url$it.gif" }
        return UtilityImgAnim.getAnimationDrawableFromUrlList(context, urls, UtilityImg.animInterval(context))
    }
*/

	
}



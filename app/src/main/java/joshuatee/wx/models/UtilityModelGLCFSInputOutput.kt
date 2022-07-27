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

package joshuatee.wx.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityImgAnim

internal object UtilityModelGlcfsInputOutput {

    fun getImage(om: ObjectModel, timeOriginal: String): Bitmap {
        var time = timeOriginal.replace("00", "0")
        val timeInt = To.int(time)
        if (timeInt > 9) {
            time = time.replace(Regex("^0"), "")
        }
        return "https://www.glerl.noaa.gov/res/glcfs/lakes/cur/${om.currentParam}-$time.gif".getImage()
    }

    fun getAnimation(context: Context, om: ObjectModel): AnimationDrawable {
        if (om.spinnerTimeValue == -1) {
            return AnimationDrawable()
        }
        val bitmaps = (om.spinnerTimeValue until om.times.size).map { getImage(om, om.times[it].split(" ").getOrNull(0) ?: "") }
        return UtilityImgAnim.getAnimationDrawableFromBitmapList(context, bitmaps)
    }
}

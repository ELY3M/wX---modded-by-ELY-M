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

package joshuatee.wx.ui

import android.content.Context
import android.graphics.Bitmap

import joshuatee.wx.util.UtilityImg

class ObjectCardHSImage(context: Context, val product: String) : ObjectCardImage(context) {

    init {
        img = TouchImageView2(context)
        img.layoutParams = layoutParams
        card.addView(img)
    }

    fun setImage(bitmap: Bitmap) {
        UtilityImg.resizeViewSetImgInCard(bitmap, img)
    }
}



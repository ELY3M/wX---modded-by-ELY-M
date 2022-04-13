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
import android.widget.ImageView
import joshuatee.wx.Extensions.setPadding
import joshuatee.wx.MyApplication
import joshuatee.wx.fragments.UtilityLocationFragment

class ObjectImageView(context: Context) {

    val imageView = ImageView(context)

    fun setImage(bitmap: Bitmap) {
        imageView.setImageBitmap(bitmap)
        imageView.setPadding(MyApplication.paddingSmall)
        val layoutParams = imageView.layoutParams
        val imageSize = UtilityLocationFragment.setNwsIconSize()
        layoutParams.width = imageSize
        layoutParams.height = imageSize
        imageView.layoutParams = layoutParams
    }
}

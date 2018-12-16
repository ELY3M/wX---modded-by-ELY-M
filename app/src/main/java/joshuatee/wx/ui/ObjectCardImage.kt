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

package joshuatee.wx.ui

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.TableLayout
import androidx.cardview.widget.CardView

import joshuatee.wx.util.UtilityImg

open class ObjectCardImage {

    private val objCard: ObjectCard
    private val context: Context
    var img: TouchImageView2
        internal set
    internal val lparams = TableLayout.LayoutParams(
        TableLayout.LayoutParams.WRAP_CONTENT,
        TableLayout.LayoutParams.WRAP_CONTENT
    )

    constructor(context: Context, bitmap: Bitmap) {
        this.context = context
        objCard = ObjectCard(context)
        img = TouchImageView2(context)
        img.layoutParams = lparams
        UtilityImg.resizeViewSetImgInCard(bitmap, img)
        objCard.addView(img)
    }

    constructor(context: Context) {
        this.context = context
        img = TouchImageView2(context)
        objCard = ObjectCard(context)
    }

    open fun setImage(bitmap: Bitmap) {
        img = TouchImageView2(context)
        img.layoutParams = lparams
        UtilityImg.resizeViewSetImgInCard(bitmap, img)
        objCard.addView(img)
    }

    fun resetZoom() {
        img.resetZoom()
    }

    val card: CardView get() = objCard.card

    fun setVisibility(visibility: Int) {
        objCard.setVisibility(visibility)
    }

    fun setOnClickListener(fn: View.OnClickListener) {
        img.setOnClickListener(fn)
    }
}



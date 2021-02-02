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
import android.view.View
import android.widget.LinearLayout
import android.widget.TableLayout
import androidx.appcompat.widget.Toolbar

import joshuatee.wx.util.UtilityImg

open class ObjectCardImage {

    private val objectCard: ObjectCard
    private val context: Context
    var img: TouchImageView2
        internal set
    internal val layoutParams = TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT)

    constructor(context: Context, bitmap: Bitmap) {
        this.context = context
        objectCard = ObjectCard(context)
        img = TouchImageView2(context)
        img.layoutParams = layoutParams
        UtilityImg.resizeViewSetImgInCard(bitmap, img)
        objectCard.addView(img)
    }

    constructor(context: Context, linearLayout: LinearLayout, bitmap: Bitmap, numberAcross: Int = 1) {
        this.context = context
        objectCard = ObjectCard(context)
        img = TouchImageView2(context)
        img.layoutParams = layoutParams
        UtilityImg.resizeViewSetImgInCard(bitmap, img, numberAcross)
        objectCard.addView(img)
        linearLayout.addView(card)
    }

    constructor(context: Context, linearLayout: LinearLayout, toolbar: Toolbar, bitmap: Bitmap) {
        this.context = context
        objectCard = ObjectCard(context)
        img = TouchImageView2(context)
        img.layoutParams = layoutParams
        UtilityImg.resizeViewSetImgInCard(bitmap, img)
        objectCard.addView(img)
        linearLayout.addView(card)
        setOnClickListener { UtilityToolbar.showHide(toolbar) }
    }

    constructor(context: Context, linearLayout: LinearLayout, toolbar: Toolbar, toolbarBottom: Toolbar, bitmap: Bitmap) {
        this.context = context
        objectCard = ObjectCard(context)
        img = TouchImageView2(context)
        img.layoutParams = layoutParams
        UtilityImg.resizeViewSetImgInCard(bitmap, img)
        objectCard.addView(img)
        linearLayout.addView(card)
        setOnClickListener { UtilityToolbar.showHide(toolbar, toolbarBottom) }
    }

    constructor(context: Context) {
        this.context = context
        img = TouchImageView2(context)
        objectCard = ObjectCard(context)
    }

    constructor(context: Context, linearLayout: LinearLayout) {
        this.context = context
        img = TouchImageView2(context)
        objectCard = ObjectCard(context)
        linearLayout.addView(card)
    }

    open fun setImage(bitmap: Bitmap, numberAcross: Int = 1) {
        img = TouchImageView2(context)
        img.layoutParams = layoutParams
        UtilityImg.resizeViewSetImgInCard(bitmap, img, numberAcross)
        objectCard.addView(img)
    }

    fun resetZoom() = img.resetZoom()

    val card get() = objectCard.card

    var visibility
        get() = objectCard.visibility
        set(newValue) { objectCard.visibility = newValue }

    fun setOnClickListener(fn: View.OnClickListener) = img.setOnClickListener(fn)
}



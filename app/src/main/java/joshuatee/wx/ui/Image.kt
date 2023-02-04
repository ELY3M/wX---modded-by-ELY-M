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

package joshuatee.wx.ui

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.TableLayout
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.util.UtilityImg

open class Image : Widget {

    protected val card: Card
    private val context: Context
    var img: TouchImageView2
        internal set
    internal val layoutParams = TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT)
    var bitmap = UtilityImg.getBlankBitmap()

    constructor(context: Context) {
        this.context = context
        img = TouchImageView2(context)
        card = Card(context)
    }

    constructor(context: Context, bitmap: Bitmap) {
        this.context = context
        card = Card(context)
        img = TouchImageView2(context)
        img.layoutParams = layoutParams
        UtilityImg.resizeViewSetImgInCard(bitmap, img)
        card.addWidget(img)
    }

    constructor(context: Context, bitmap: Bitmap, numberAcross: Int = 1) {
        this.context = context
        card = Card(context)
        img = TouchImageView2(context)
        img.layoutParams = layoutParams
        UtilityImg.resizeViewSetImgInCard(bitmap, img, numberAcross)
        card.addWidget(img)
    }

    constructor(context: Context, toolbar: Toolbar, bitmap: Bitmap) {
        this.context = context
        card = Card(context)
        img = TouchImageView2(context)
        img.layoutParams = layoutParams
        UtilityImg.resizeViewSetImgInCard(bitmap, img)
        card.addWidget(img)
        connect { UtilityToolbar.showHide(toolbar) }
    }

    open fun set(bitmap: Bitmap, numberAcross: Int = 1) {
        img = TouchImageView2(context)
        img.layoutParams = layoutParams
        UtilityImg.resizeViewSetImgInCard(bitmap, img, numberAcross)
        card.addWidget(img)
        this.bitmap = bitmap
    }

    open fun set2(bitmap: Bitmap, numberAcross: Int = 1) {
        card.removeAllViews()
        img = TouchImageView2(context)
        img.layoutParams = layoutParams
        UtilityImg.resizeViewSetImgInCard(bitmap, img, numberAcross)
        card.addWidget(img)
        this.bitmap = bitmap
    }

    fun resetZoom() {
        img.resetZoom()
    }

    override fun getView() = card.getView()

    var visibility
        get() = card.visibility
        set(newValue) { card.visibility = newValue }

    fun connect(fn: View.OnClickListener) {
        img.setOnClickListener(fn)
    }
}

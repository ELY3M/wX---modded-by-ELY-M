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

package joshuatee.wx.ui

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.TableLayout
import joshuatee.wx.MyApplication
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.util.UtilityImg

open class Image : Widget {

    protected val card: Card
    private val context: Context
    protected var imageView: TouchImageView2
    internal val layoutParams = TableLayout.LayoutParams(
        TableLayout.LayoutParams.WRAP_CONTENT,
        TableLayout.LayoutParams.WRAP_CONTENT
    )
    var bitmap = UtilityImg.getBlankBitmap()

    constructor(context: Context) {
        this.context = context
        card = Card(context)
        imageView = TouchImageView2(context)
    }

    constructor(context: Context, bitmap: Bitmap, numberAcross: Int = 1) : this(context) {
        this.bitmap = bitmap
        imageView.layoutParams = layoutParams
        resizeViewSetImgInCard(numberAcross)
        card.addWidget(imageView)
    }

    open fun set(bitmap: Bitmap, numberAcross: Int = 1) {
        this.bitmap = bitmap
        imageView = TouchImageView2(context)
        imageView.layoutParams = layoutParams
        resizeViewSetImgInCard(numberAcross)
        card.addWidget(imageView)
    }

    open fun set2(bitmap: Bitmap, numberAcross: Int = 1) {
        this.bitmap = bitmap
        card.removeAllViews()
        imageView = TouchImageView2(context)
        imageView.layoutParams = layoutParams
        resizeViewSetImgInCard(numberAcross)
        card.addWidget(imageView)
    }

    fun resetZoom() {
        imageView.resetZoom()
    }

    var visibility
        get() = card.visibility
        set(newValue) {
            card.visibility = newValue
        }

    fun connect(fn: View.OnClickListener) {
        imageView.setOnClickListener(fn)
    }

    protected fun resizeViewSetImgInCard(numberAcross: Int = 1) {
        val layoutParams = imageView.layoutParams
        layoutParams.width =
            (MyApplication.dm.widthPixels - (UIPreferences.lLpadding * 2).toInt()) / numberAcross
        layoutParams.height =
            ((MyApplication.dm.widthPixels - (UIPreferences.lLpadding * 2).toInt()) * bitmap.height / bitmap.width) / numberAcross
        imageView.layoutParams = layoutParams
        imageView.setImageBitmap(bitmap)
    }

    override fun getView() = card.getView()
}

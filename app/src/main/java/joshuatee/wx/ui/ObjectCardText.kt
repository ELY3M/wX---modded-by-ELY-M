/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView

import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.audio.UtilityTTSTranslations
import joshuatee.wx.objects.ObjectIntent

class ObjectCardText(private val context: Context) {

    private val objCard = ObjectCard(context)
    val tv: TextView = TextView(context)
    var padding = MyApplication.padding

    init {
        textViewSetup(this)
        tv.setTextIsSelectable(true)
        tv.isFocusable = false
        objCard.addView(tv)
    }

    constructor(
            context: Context,
            linearLayout: LinearLayout
    ) : this(context) {
        linearLayout.addView(card)
    }

    constructor(
            context: Context,
            linearLayout: LinearLayout,
            toolbar: Toolbar,
            toolbarBottom: Toolbar
    ) : this(context) {
        linearLayout.addView(card)
        setOnClickListener(View.OnClickListener {
            UtilityToolbar.showHide(
                    toolbar,
                    toolbarBottom
            )
        })
    }

    constructor(
            context: Context,
            linearLayout: LinearLayout,
            toolbar: Toolbar,
            toolbarBottom: Toolbar,
            text: String
    ) : this(context) {
        linearLayout.addView(card)
        setOnClickListener(View.OnClickListener {
            UtilityToolbar.showHide(
                    toolbar,
                    toolbarBottom
            )
        })
        setText(text)
    }

    constructor(
            context: Context,
            linearLayout: LinearLayout,
            toolbar: Toolbar
    ) : this(context) {
        linearLayout.addView(card)
        setOnClickListener(View.OnClickListener {
            UtilityToolbar.showHide(
                    toolbar
            )
        })
    }

    constructor(context: Context, text: String) : this(context) {
        tv.text = text
        tv.isFocusable = false
    }

    constructor(context: Context, linearLayout: LinearLayout, text: String) : this(context) {
        tv.text = text
        tv.isFocusable = false
        linearLayout.addView(card)
    }

    constructor(context: Context, text: String, textSize: Float) : this(context, text) {
        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        tv.isFocusable = false
    }

    constructor(context: Context, text: String, textSize: Float, padding: Int) : this(context, text) {
        tv.text = text
        tv.setPadding(padding, padding, padding, padding)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        tv.isFocusable = false
    }

    constructor(context: Context, linearLayout: LinearLayout, text: String, textSize: Float) : this(
            context,
            text,
            textSize
    ) {
        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        tv.isFocusable = false
        linearLayout.addView(card)
    }

    constructor(context: Context, linearLayout: LinearLayout, text: String, textSize: Float, padding: Int) : this(
            context,
            text,
            textSize
    ) {
        this.padding = padding
        tv.setPadding(padding, padding, padding, padding)
        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        tv.isFocusable = false
        linearLayout.addView(card)
    }

    constructor(context: Context, text: String, textSize: Float, clazz: Class<*>, padding: Int) : this(
            context,
            text
    ) {
        this.padding = padding
        tv.setPadding(padding, padding, padding, padding)
        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        tv.isFocusable = false
        setOnClickListener(View.OnClickListener {
            ObjectIntent(
                    context,
                    clazz
            )
        })
    }

    constructor(context: Context, text: String, textSize: Float, clazz: Class<*>) : this(
            context,
            text
    ) {
        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        tv.isFocusable = false
        setOnClickListener(View.OnClickListener {
            ObjectIntent(
                    context,
                    clazz
            )
        })
    }

    constructor(
            context: Context,
            linearLayout: LinearLayout,
            text: String,
            textSize: Float,
            clazz: Class<*>
    ) : this(
            context,
            text,
            textSize,
            clazz
    ) {
        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        tv.isFocusable = false
        linearLayout.addView(card)
        setOnClickListener(View.OnClickListener {
            ObjectIntent(
                    context,
                    clazz
            )
        })
    }

    constructor(
            context: Context,
            linearLayout: LinearLayout,
            text: String,
            textSize: Float,
            clazz: Class<*>,
            padding: Int
    ) : this(
            context,
            text,
            textSize,
            clazz
    ) {
        this.padding = padding
        tv.setPadding(padding, padding, padding, padding)
        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        tv.isFocusable = false
        linearLayout.addView(card)
        setOnClickListener(View.OnClickListener {
            ObjectIntent(
                    context,
                    clazz
            )
        })
    }

    fun setTextAndTranslate(text: String) {
        val localText = if (UIPreferences.translateText) {
            UtilityTTSTranslations.tranlasteAbbrevForVisual(text)
        } else {
            text
        }
        tv.text = localText
    }

    fun setText(text: String) {
        tv.text = text
    }

    fun setText(text: CharSequence) {
        tv.text = text.toString()
    }

    fun center() {
        tv.gravity = Gravity.CENTER
    }

    fun setTextColor(color: Int) {
        tv.setTextColor(color)
    }

    fun setTextSize(type: Int, size: Float) {
        tv.setTextSize(type, size)
    }

    fun lightText() {
        tv.setTextAppearance(context, UIPreferences.smallTextTheme)
    }

    val card: CardView get() = objCard.card

    fun setVisibility(visibility: Int) {
        objCard.setVisibility(visibility)
    }

    fun setOnClickListener(fn: View.OnClickListener) {
        tv.setOnClickListener(fn)
    }

    companion object {
        fun textViewSetup(tvTmp: TextView) {
            tvTmp.setPadding(
                    MyApplication.padding,
                    MyApplication.padding,
                    MyApplication.padding,
                    MyApplication.padding
            )
            tvTmp.gravity = Gravity.START
            tvTmp.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
            tvTmp.setTextColor(UIPreferences.backgroundColor)
        }

        fun textViewSetup(objCardText: ObjectCardText) {
            objCardText.tv.setPadding(
                    objCardText.padding,
                    objCardText.padding,
                    objCardText.padding,
                    objCardText.padding
            )
            objCardText.tv.gravity = Gravity.START
            objCardText.tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
            objCardText.tv.setTextColor(UIPreferences.backgroundColor)
        }
    }
}



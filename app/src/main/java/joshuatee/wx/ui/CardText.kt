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
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.Extensions.setPadding
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.audio.UtilityTtsTranslations
import joshuatee.wx.objects.Route
import joshuatee.wx.objects.TextSize

class CardText(context: Context) {

    private val card = Card(context)
    private val tv = TextView(context)
    private var padding = UIPreferences.padding

    init {
        textViewSetup(this)
        tv.setTextIsSelectable(true)
        tv.isFocusable = false
        card.addView(tv)
    }

    constructor(context: Context, box: VBox) : this(context) {
        box.addWidget(get())
    }

    constructor(context: Context, box: VBox, toolbar: Toolbar, toolbarBottom: Toolbar) : this(context) {
        box.addWidget(get())
        connect { UtilityToolbar.showHide(toolbar, toolbarBottom) }
    }

    constructor(context: Context, box: VBox, toolbar: Toolbar, toolbarBottom: Toolbar, textValue: String) : this(context) {
        box.addWidget(get())
        connect { UtilityToolbar.showHide(toolbar, toolbarBottom) }
        text = textValue
    }

    constructor(context: Context, box: VBox, toolbar: Toolbar) : this(context) {
        box.addWidget(get())
        connect { UtilityToolbar.showHide(toolbar) }
    }

    constructor(context: Context, text: String) : this(context) {
        tv.text = text
        tv.isFocusable = false
    }

    constructor(context: Context, box: VBox, text: String) : this(context) {
        tv.text = text
        tv.isFocusable = false
        box.addWidget(get())
    }

    constructor(context: Context, text: String, textSize: Float) : this(context, text) {
        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        tv.isFocusable = false
    }

    constructor(context: Context, text: String, textSize: Float, padding: Int) : this(context, text) {
        tv.text = text
        tv.setPadding(padding)
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        tv.isFocusable = false
    }

    constructor(context: Context, box: LinearLayout, text: String, textSize: Float) : this(
            context,
            text,
            textSize
    ) {
        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        tv.isFocusable = false
        box.addView(get())
    }

    constructor(context: Context, box: LinearLayout, text: String, textSize: TextSize) : this(
            context,
            text
    ) {
        refreshTextSize(textSize)
        tv.isFocusable = false
        box.addView(get())
    }

    constructor(context: Context, box: LinearLayout, text: String, textSize: Float, padding: Int) : this(
            context,
            text,
            textSize
    ) {
        this.padding = padding
        tv.setPadding(padding)
        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        tv.isFocusable = false
        box.addView(get())
    }

    constructor(context: Context, text: String, textSize: Float, clazz: Class<*>, padding: Int) : this(
            context,
            text
    ) {
        this.padding = padding
        tv.setPadding(padding)
        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        tv.isFocusable = false
        connect { Route(context, clazz) }
    }

    constructor(context: Context, text: String, textSize: Float, clazz: Class<*>) : this(
            context,
            text
    ) {
        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        tv.isFocusable = false
        connect { Route(context, clazz) }
    }

    constructor(context: Context, box: LinearLayout, text: String, textSize: Float, clazz: Class<*>) : this(
            context,
            text,
            textSize,
            clazz
    ) {
        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        tv.isFocusable = false
        box.addView(get())
        connect { Route(context, clazz) }
    }

    constructor(context: Context, box: LinearLayout, text: String, textSize: Float, clazz: Class<*>, padding: Int) : this(
            context,
            text,
            textSize,
            clazz
    ) {
        this.padding = padding
        tv.setPadding(padding)
        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        tv.isFocusable = false
        box.addView(get())
        connect { Route(context, clazz) }
    }

    fun setPaddingAmount(padding: Int) {
        this.padding = padding
        tv.setPadding(padding)
    }

    fun setTextAndTranslate(text: String) {
        val localText = if (UIPreferences.translateText) UtilityTtsTranslations.translateAbbreviationsForVisual(text) else text
        tv.text = localText
    }

    fun setText1(s: String) {
        text = s
    }

    var text
        get() = tv.text.toString()
        set(newValue) { tv.text = newValue }

    fun center() {
        tv.gravity = Gravity.CENTER
    }

    fun setTextColor(color: Int) {
        tv.setTextColor(color)
    }

    fun setTextSize(type: Int, size: Float) {
        tv.setTextSize(type, size)
    }

    fun typefaceMono() {
        tv.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
    }

    fun typefaceDefault() {
        tv.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    fun get() = card.get()

    var visibility
        get() = card.visibility
        set(newValue) { card.visibility = newValue }

    fun connect(fn: View.OnClickListener) {
        tv.setOnClickListener(fn)
    }

    fun refreshTextSize(size: TextSize) = when (size) {
            TextSize.SMALL -> tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, UIPreferences.textSizeSmall)
            TextSize.MEDIUM -> tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, UIPreferences.textSizeNormal)
            TextSize.LARGE -> tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, UIPreferences.textSizeLarge)
        }

    fun setupHazard() {
        setPaddingAmount(UIPreferences.paddingSettings)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, UIPreferences.textSizeNormal)
        setTextColor(UIPreferences.textHighlightColor)
    }

    companion object {

        fun textViewSetup(cardText: CardText) {
            cardText.tv.setPadding(cardText.padding)
            cardText.tv.gravity = Gravity.START
            cardText.tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, UIPreferences.textSizeSmall)
            cardText.tv.setTextColor(UIPreferences.backgroundColor)
        }
    }
}

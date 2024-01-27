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
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout

class HBox(val context: Context) {

    private val linearLayout = LinearLayout(context)

    init {
        linearLayout.orientation = LinearLayout.HORIZONTAL
    }

    constructor(context: Context, parentView: LinearLayout) : this(context) {
        parentView.addView(linearLayout)
    }

    constructor(context: Context, gravity: Int) : this(context) {
        this.gravity = gravity
    }

    fun addWidget(child: View) {
        linearLayout.addView(child)
    }

    fun addWidget(child: Widget) {
        linearLayout.addView(child.getView())
    }

    fun addLayout(objectLinearLayout: VBox) {
        linearLayout.addView(objectLinearLayout.get())
    }

    fun addLayout(child: HBox) {
        linearLayout.addView(child.get())
    }

    fun addWidgets(children: List<Widget>) {
        children.forEach {
            linearLayout.addView(it.getView())
        }
    }

    fun matchParent() {
        linearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    }

    fun wrap() {
        linearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    fun makeVertical() {
        linearLayout.orientation = LinearLayout.VERTICAL
    }

    fun get() = linearLayout

    var visibility
        get() = linearLayout.visibility
        set(value) {
            linearLayout.visibility = value
        }

    var isBaselineAligned
        get() = linearLayout.isBaselineAligned
        set(value) {
            linearLayout.isBaselineAligned = value
        }

    var orientation
        get() = linearLayout.orientation
        set(value) {
            linearLayout.orientation = value
        }

    private var gravityBacking = Gravity.START

    var gravity
        get() = gravityBacking
        set(value) {
            linearLayout.gravity = value
            gravityBacking = value
        }
}

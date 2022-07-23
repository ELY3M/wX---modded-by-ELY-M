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
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout

class VBox(val context: Context) {

    private val linearLayout = LinearLayout(context)

    init {
        linearLayout.orientation = LinearLayout.VERTICAL
    }

    constructor(context: Context, parentView: LinearLayout) : this(context) {
        linearLayout.orientation = LinearLayout.VERTICAL
        parentView.addView(linearLayout)
    }

    constructor(context: Context, orientation: Int, parentView: LinearLayout) : this(context) {
        linearLayout.orientation = orientation
        parentView.addView(linearLayout)
    }

//    constructor(context: Context, orientation: Int) : this(context) {
//        this.orientation = orientation
//    }
//
//    constructor(context: Context, orientation: Int, gravity: Int) : this(context, orientation) {
//        this.gravity = gravity
//    }

    constructor(context: Context, gravity: Int) : this(context) {
        linearLayout.orientation = LinearLayout.VERTICAL
        this.gravity = gravity
    }

    fun removeAllViewsInLayout() {
        linearLayout.removeAllViewsInLayout()
    }

    fun addWidget(child: View) {
        linearLayout.addView(child)
    }

    fun addLayout(child: LinearLayout) {
        linearLayout.addView(child)
    }

    fun addViews(children: List<View>) {
        children.forEach {
            linearLayout.addView(it)
        }
    }

    fun removeAllViews() {
        linearLayout.removeAllViews()
    }

    fun matchParent() {
        linearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    }

    fun wrap() {
        linearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    fun get() = linearLayout

    var visibility
        get() = linearLayout.visibility
        set(value) { linearLayout.visibility = value }

//    var isBaselineAligned
//        get() = linearLayout.isBaselineAligned
//        set(value) { linearLayout.isBaselineAligned = value }

    var orientation
        get() = linearLayout.orientation
        set(value) { linearLayout.orientation = value }

    private var gravityBacking = Gravity.START

    var gravity
        get() = gravityBacking
        set(value) {
            linearLayout.gravity = value
            gravityBacking = value
        }
}

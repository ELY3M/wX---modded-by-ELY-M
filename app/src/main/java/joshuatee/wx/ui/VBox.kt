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

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import joshuatee.wx.MyApplication
import joshuatee.wx.R

class VBox(val context: Context) : Widget {

    private var linearLayout = LinearLayout(context)

    init {
        linearLayout.orientation = LinearLayout.VERTICAL
    }

    constructor(context: Context, orientation: Int, parentView: LinearLayout) : this(context) {
        linearLayout.orientation = orientation
        parentView.addView(linearLayout)
    }

    companion object {
        fun fromResource(activity: Activity): VBox {
            val box = VBox(activity)
            box.linearLayout = activity.findViewById(R.id.linearLayout)
            return box
        }

        fun fromViewResource(view: View): VBox {
            val box = VBox(MyApplication.appContext)
            box.linearLayout = view.findViewById(R.id.linearLayout)
            return box
        }
    }

    constructor(context: Context, gravity: Int) : this(context) {
        linearLayout.orientation = LinearLayout.VERTICAL
        this.gravity = gravity
    }

    // assumes you are doing this in onLayout and thus does not call requestLayout
    // removeAllViews does call requestLayout
    fun removeChildren() {
        linearLayout.removeAllViewsInLayout()
    }

    fun addWidget(child: View) {
        linearLayout.addView(child)
    }

    fun addWidget(child: Widget) {
        linearLayout.addView(child.getView())
    }

    fun addLayout(child: HBox) {
        linearLayout.addView(child.get())
    }

    fun addLayout(child: VBox) {
        linearLayout.addView(child.get())
    }

    fun addWidgets(children: List<Widget>) {
        children.forEach {
            linearLayout.addView(it.getView())
        }
    }

    fun removeChildrenAndLayout() {
        linearLayout.removeAllViews()
    }

    fun removeView(view: View) {
        linearLayout.removeView(view)
    }

    fun setBackgroundColor(color: Int) {
        linearLayout.setBackgroundColor(color)
    }

    fun makeHorizontal() {
        linearLayout.orientation = LinearLayout.HORIZONTAL
    }

    fun matchParent() {
        linearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    }

    fun wrap() {
        linearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    fun get() = linearLayout

    override fun getView() = linearLayout

    var visibility
        get() = linearLayout.visibility
        set(value) { linearLayout.visibility = value }

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

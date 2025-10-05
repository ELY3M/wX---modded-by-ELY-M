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

import android.app.Activity
import android.util.TypedValue
import android.widget.EditText

class TextEdit(activity: Activity, resourceId: Int) : Widget {

    private val tv: EditText = activity.findViewById(resourceId)

    var text
        get() = tv.text.toString()
        set(value) {
            tv.setText(value)
        }

    fun setTextSize(size: Float) {
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
    }

    fun setHintTextColor(color: Int) {
        tv.setHintTextColor(color)
    }

    fun setTextColor(color: Int) {
        tv.setTextColor(color)
    }

    override fun getView() = tv
}

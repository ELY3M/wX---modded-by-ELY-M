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

import android.app.Activity
import android.content.Context
import android.view.View
import joshuatee.wx.util.ImageMap
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.MyApplication

class ObjectImageMap(val activity: Activity, val context: Context, resId: Int, val toolbar: Toolbar, val toolbarBottom: Toolbar, private val viewArr: List<View>) {

    var map: ImageMap = activity.findViewById(resId)

    init {
        map.visibility = View.GONE
    }

    fun toggleMap() {
        if (map.visibility == View.GONE) {
            setupMap()
        } else {
            map.visibility = View.GONE
            viewArr.forEach { it.visibility = View.VISIBLE }
        }
    }

    private fun setupMap() {
        viewArr.forEach { it.visibility = View.GONE }
        setupImageMap(context, map, toolbar, toolbarBottom)
        map.visibility = View.VISIBLE
    }

    fun addOnImageMapClickedHandler(h: ImageMap.OnImageMapClickedHandler) {
        map.addOnImageMapClickedHandler(h)
    }

    companion object {
        fun setupImageMap(context: Context, map: ImageMap, toolbar: Toolbar, toolbarBottom: Toolbar) {
            val paramsIv = map.layoutParams
            paramsIv.height = MyApplication.dm.heightPixels - toolbar.height - toolbarBottom.height - UtilityUI.statusBarHeight(context)
            paramsIv.width = MyApplication.dm.widthPixels
            map.layoutParams = paramsIv
        }
    }
}


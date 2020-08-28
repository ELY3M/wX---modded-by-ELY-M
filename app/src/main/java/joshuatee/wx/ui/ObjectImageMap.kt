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

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import joshuatee.wx.util.ImageMap
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.MyApplication
import joshuatee.wx.util.Utility

class ObjectImageMap(
    val activity: Activity,
    val context: Context,
    resId: Int,
    val toolbar: Toolbar,
    val toolbarBottom: Toolbar,
    private val views: List<View>
) {

    val map: ImageMap = activity.findViewById(resId)
    var isRadarWithTransparent = false

    init { map.visibility = View.GONE }

    fun toggleMap() {
        val toolbarAlpha = toolbar.background.alpha
        if (toolbarAlpha == 0) isRadarWithTransparent = true
        if (map.visibility == View.GONE) {
            setupMap()
            if (isRadarWithTransparent) {
                if (Utility.isThemeAllWhite()) {
                    toolbar.setBackgroundColor(Color.BLACK)
                    toolbarBottom.setBackgroundColor(Color.BLACK)
                }
                toolbar.background.mutate().alpha = 255
                toolbarBottom.background.mutate().alpha = 255
            }
        } else {
            map.visibility = View.GONE
            views.forEach { it.visibility = View.VISIBLE }
            if (isRadarWithTransparent) UtilityToolbar.transparentToolbars(toolbar, toolbarBottom)
        }
    }

    fun hideMap() {
        //val toolbarAlpha = toolbar.background.alpha
        //if (toolbarAlpha == 0) isRadarWithTransparent = true
        if (map.visibility != View.GONE) {
            map.visibility = View.GONE
            views.forEach { it.visibility = View.VISIBLE }
            if (isRadarWithTransparent) UtilityToolbar.transparentToolbars(toolbar, toolbarBottom)
        }
    }

    private fun setupMap() {
        views.forEach { it.visibility = View.GONE }
        setupImageMap(context, toolbar, toolbarBottom)
        map.visibility = View.VISIBLE
    }

    private fun addOnImageMapClickedHandler(h: ImageMap.OnImageMapClickedHandler) = map.addOnImageMapClickedHandler(h)

    fun addClickHandler(fn: (String) -> Unit, mapFn: (Int) -> String) {
        addOnImageMapClickedHandler(object : ImageMap.OnImageMapClickedHandler {
            override fun onImageMapClicked(id: Int, im2: ImageMap) {
                im2.visibility = View.GONE
                if (isRadarWithTransparent) UtilityToolbar.transparentToolbars(toolbar, toolbarBottom)
                fn(mapFn(id))
            }

            override fun onBubbleClicked(id: Int) {}
        })
    }

    private fun setupImageMap(context: Context, toolbar: Toolbar, toolbarBottom: Toolbar) {
        val layoutParams = map.layoutParams
        layoutParams.height = MyApplication.dm.heightPixels - toolbar.height - toolbarBottom.height - UtilityUI.statusBarHeight(context)
        layoutParams.width = MyApplication.dm.widthPixels
        map.layoutParams = layoutParams
    }
}



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
import android.graphics.Color
import android.view.View
import joshuatee.wx.util.ImageMap
import joshuatee.wx.MyApplication
import joshuatee.wx.radar.NexradRenderTextObject

class ObjectImageMap(
    val activity: Activity,
    resId: Int,
    val toolbar: ObjectToolbar,
    val toolbarBottom: ObjectToolbar,
    private val views: List<View>
) {

    private val map: ImageMap = activity.findViewById(resId)
    private var isRadarWithTransparent = false

    init {
        map.visibility = View.GONE
    }

    fun toggleMap() {
        val toolbarAlpha = toolbar.background.alpha
        if (toolbarAlpha == 0) {
            isRadarWithTransparent = true
        }
        if (map.visibility == View.GONE) {
            setupMap()
            if (isRadarWithTransparent) {
                if (UtilityUI.isThemeAllWhite()) {
                    toolbar.setBackgroundColor(Color.BLACK)
                    toolbarBottom.setBackgroundColor(Color.BLACK)
                }
                toolbar.background.mutate().alpha = 255
                toolbarBottom.background.mutate().alpha = 255
            }
        } else {
            map.visibility = View.GONE
            views.forEach {
                it.visibility = View.VISIBLE
            }
            if (isRadarWithTransparent) {
                UtilityToolbar.transparentToolbars(toolbar, toolbarBottom)
            }
        }
    }

    fun hideMap() {
        if (map.visibility != View.GONE) {
            map.visibility = View.GONE
            views.forEach {
                it.visibility = View.VISIBLE
            }
            if (isRadarWithTransparent) {
                UtilityToolbar.transparentToolbars(toolbar, toolbarBottom)
            }
        }
    }

    fun showMap(wxglTextObjects: List<NexradRenderTextObject>) {
        toggleMap()
        if (visibility != View.GONE) {
            NexradRenderTextObject.hideLabels(wxglTextObjects)
        } else {
            NexradRenderTextObject.showLabels(wxglTextObjects)
        }
    }

    private fun setupMap() {
        views.forEach {
            it.visibility = View.GONE
        }
        setupImageMap(toolbar, toolbarBottom)
        map.visibility = View.VISIBLE
    }

    private fun addOnImageMapClickedHandler(h: ImageMap.OnImageMapClickedHandler) {
        map.addOnImageMapClickedHandler(h)
    }

    fun connect(fn: (String) -> Unit, mapFn: (Int) -> String) {
        addOnImageMapClickedHandler { id, im2 ->
            im2.visibility = View.GONE
            if (isRadarWithTransparent) {
                UtilityToolbar.transparentToolbars(toolbar, toolbarBottom)
            }
            fn(mapFn(id))
        }
    }

    private fun setupImageMap(toolbar: ObjectToolbar, toolbarBottom: ObjectToolbar) {
        val layoutParams = map.layoutParams
        layoutParams.height =
            MyApplication.dm.heightPixels - toolbar.height - toolbarBottom.height - UtilityUI.statusBarHeight(
                activity
            )
        layoutParams.width = MyApplication.dm.widthPixels
        map.layoutParams = layoutParams
    }

    var visibility
        get() = map.visibility
        set(value) {
            map.visibility = value
        }
}

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
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.drawable.AnimationDrawable
import android.view.View
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg

class TouchImage {

    private var img: TouchImageView2
    private val context: Context
    var imageLoaded = false
    private var firstRun = false
    private var prefTokenIdx = ""
    private var drw: NavDrawer? = null
    var bitmap = UtilityImg.getBlankBitmap()

    constructor(context: Context) {
        img = TouchImageView2(context)
        this.context = context
    }

    constructor(activity: Activity, resourceId: Int) {
        img = activity.findViewById(resourceId)
        this.context = activity
    }

    constructor(activity: Activity, toolbar: Toolbar, resourceId: Int) : this(activity, resourceId) {
        connectClick { UtilityToolbar.showHide(toolbar) }
    }

    constructor(activity: Activity, toolbar: Toolbar, toolbarBottom: Toolbar, resourceId: Int) : this(activity, resourceId) {
        connectClick { UtilityToolbar.showHide(toolbar, toolbarBottom) }
    }

    constructor(activity: Activity, resourceId: Int, drw: NavDrawer, prefTokenIdx: String) {
        img = activity.findViewById(resourceId)
        this.context = activity
        this.drw = drw
        this.prefTokenIdx = prefTokenIdx
    }

    constructor(activity: Activity, toolbar: Toolbar, toolbarBottom: Toolbar, resourceId: Int, drw: NavDrawer, prefTokenIdx: String
    ) : this(activity, resourceId, drw, prefTokenIdx) {
        connectClick { UtilityToolbar.showHide(toolbar, toolbarBottom) }
    }

    constructor(activity: Activity, toolbar: Toolbar, resourceId: Int, drw: NavDrawer, prefTokenIdx: String
    ) : this(activity, resourceId, drw, prefTokenIdx) {
        connectClick { UtilityToolbar.showHide(toolbar) }
    }

    var visibility
        get() = img.visibility
        set(value) {
            img.visibility = value
        }

    val currentZoom
        get() = img.currentZoom

    val scrollPosition: PointF?
        get() = img.scrollPosition

    fun set(bitmap: Bitmap) {
        img.setImageBitmap(bitmap)
        imageLoaded = true
        if (prefTokenIdx != "" && drw != null) {
            Utility.writePrefInt(context, prefTokenIdx, drw!!.index)
        }
        this.bitmap = bitmap
    }

    fun connectClick(listener: View.OnClickListener) {
        img.setOnClickListener(listener)
    }

    fun setImageDrawable(animDrawable: AnimationDrawable) {
        img.setImageDrawable(animDrawable)
    }

    fun resetZoom() {
        img.resetZoom()
    }

    fun setMaxZoom(zoom: Float) {
        img.maxZoom = zoom
    }

    fun setZoom(zoom: Float) {
        img.setZoom(zoom)
    }

    fun setZoom(image: TouchImage) {
        img.setZoom(image.currentZoom)
    }

    fun setZoom(x: Float, y: Float, z: Float) {
        img.setZoom(x, y, z)
    }

    fun connect(navDrawer: NavDrawer, fn: () -> Unit) {
        img.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onSwipeLeft() {
                if (img.currentZoom < 1.01f) {
                    showNextImg(navDrawer, fn)
                }
            }

            override fun onSwipeRight() {
                if (img.currentZoom < 1.01f) {
                    showPrevImg(navDrawer, fn)
                }
            }
        })
    }

    fun connect2(left: () -> Unit, right: () -> Unit) {
        img.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onSwipeLeft() {
                if (img.currentZoom < 1.01f) {
                    left()
                }
            }

            override fun onSwipeRight() {
                if (img.currentZoom < 1.01f) {
                    right()
                }
            }
        })
    }

    fun showNextImg(drw: NavDrawer, fn: () -> Unit) {
        drw.index += 1
        if (drw.index == drw.getUrlCount()) {
            drw.index = 0
        }
        fn()
    }

    fun showPrevImg(drw: NavDrawer, fn: () -> Unit) {
        drw.index -= 1
        if (drw.index == -1) {
            drw.index = drw.getUrlCount() - 1
        }
        fn()
    }

    fun connect(fn: OnSwipeTouchListener) {
        img.setOnTouchListener(fn)
    }

    fun connect2(fn: () -> Unit) {
        img.setOnTouchImageViewListener { fn() }
    }

    fun firstRun(pref: String) {
        if (!firstRun) {
            img.setZoom(pref)
            firstRun = true
        }
    }

    fun imgRestorePosnZoom(prefStr: String) {
        setZoom(
                Utility.readPrefFloat(context, prefStr + "_ZOOM", 1.0f),
                Utility.readPrefFloat(context, prefStr + "_X", 0.5f),
                Utility.readPrefFloat(context, prefStr + "_Y", 0.5f)
        )
    }

    fun imgSavePosnZoom(prefStr: String) {
        if (imageLoaded) {
            val poi = img.scrollPosition
            var z = img.currentZoom
            if (poi != null) {
                var x = poi.x
                var y = poi.y
                if (x.isNaN()) {
                    x = 1.0f
                }
                if (y.isNaN()) {
                    y = 1.0f
                }
                if (z.isNaN()) {
                    z = 1.0f
                }
                Utility.writePrefFloat(context, prefStr + "_X", x)
                Utility.writePrefFloat(context, prefStr + "_Y", y)
                Utility.writePrefFloat(context, prefStr + "_ZOOM", z)
            }
        }
    }

    fun get() = img
}

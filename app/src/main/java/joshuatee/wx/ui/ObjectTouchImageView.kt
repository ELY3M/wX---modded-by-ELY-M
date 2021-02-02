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
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import android.view.View
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.MyApplication
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg

class ObjectTouchImageView {

    var img: TouchImageView2
    private val context: Context
    private var imageLoaded: Boolean = false
    var firstRun: Boolean = false
    private var prefTokenIdx = ""
    var drw: ObjectNavDrawer? = null

    constructor(activity: Activity, context: Context, resourceId: Int) {
        img = activity.findViewById(resourceId)
        this.context = context
    }

    constructor(activity: Activity, context: Context, toolbar: Toolbar, resourceId: Int) : this(activity, context, resourceId) {
        setOnClickListener {
            UtilityToolbar.showHide(toolbar)
        }
    }

    constructor(activity: Activity, context: Context, toolbar: Toolbar, toolbarBottom: Toolbar, resourceId: Int) : this(activity, context, resourceId) {
        setOnClickListener {
            UtilityToolbar.showHide(toolbar, toolbarBottom)
        }
    }

    constructor(activity: Activity, context: Context, resourceId: Int, drw: ObjectNavDrawer, prefTokenIdx: String) {
        img = activity.findViewById(resourceId)
        this.context = context
        this.drw = drw
        this.prefTokenIdx = prefTokenIdx
    }

    constructor(activity: Activity, context: Context, toolbar: Toolbar, toolbarBottom: Toolbar, resourceId: Int, drw: ObjectNavDrawer, prefTokenIdx: String
    ) : this(activity, context, resourceId, drw, prefTokenIdx) {
        setOnClickListener { UtilityToolbar.showHide(toolbar, toolbarBottom) }
    }

    constructor(activity: Activity, context: Context, toolbar: Toolbar, resourceId: Int, drw: ObjectNavDrawer, prefTokenIdx: String
    ) : this(activity, context, resourceId, drw, prefTokenIdx) {
        setOnClickListener { UtilityToolbar.showHide(toolbar) }
    }

    fun setBitmap(bitmap: Bitmap) {
        img.setImageBitmap(bitmap)
        imageLoaded = true
        if (prefTokenIdx != "" && drw != null) Utility.writePref(context, prefTokenIdx, drw!!.index)
    }

    fun setOnClickListener(listener: View.OnClickListener) = img.setOnClickListener(listener)

    fun setImageDrawable(animDrawable: AnimationDrawable) = img.setImageDrawable(animDrawable)

    fun resetZoom() = img.resetZoom()

    fun setMaxZoom(zoom: Float) { img.maxZoom = zoom }

    fun setZoom(zoom: Float) = img.setZoom(zoom)

    fun setListener(context: Context, drw: ObjectNavDrawer, fn: () -> Unit) {
        img.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onSwipeLeft() {
                if (img.currentZoom < 1.01f) UtilityImg.showNextImg(drw, fn)
            }

            override fun onSwipeRight() {
                if (img.currentZoom < 1.01f) UtilityImg.showPrevImg(drw, fn)
            }
        })
    }

    fun firstRunSetZoomPosn(pref: String) {
        if (!firstRun) {
            img.setZoom(pref)
            firstRun = true
        }
    }

    fun imgSavePosnZoom(context: Context, prefStr: String) {
        if (imageLoaded) {
            val poi = img.scrollPosition
            var z = img.currentZoom
            if (poi != null) {
                var x = poi.x
                var y = poi.y
                if (x.isNaN()) x = 1.0f
                if (y.isNaN()) y = 1.0f
                if (z.isNaN()) z = 1.0f
                Utility.writePref(context, prefStr + "_X", x)
                Utility.writePref(context, prefStr + "_Y", y)
                Utility.writePref(context, prefStr + "_ZOOM", z)
                when (prefStr) {
                    "SPCHRRR" -> {
                        MyApplication.spchrrrZoom = z
                        MyApplication.spchrrrX = x
                        MyApplication.spchrrrY = y
                    }
                    "WPCGEFS1" -> {
                        MyApplication.wpcgefsZoom = z
                        MyApplication.wpcgefsX = x
                        MyApplication.wpcgefsY = y
                    }
                    else -> {
                    }
                }
            }
        }
    }
}



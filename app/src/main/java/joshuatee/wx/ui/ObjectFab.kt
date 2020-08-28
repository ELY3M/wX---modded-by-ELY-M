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
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences

class ObjectFab {

    val fab: FloatingActionButton

    constructor(activity: Activity, context: Context, resId: Int, fn: View.OnClickListener) {
        fab = activity.findViewById(resId)
        setupFab(context)
        setOnClickListener(fn)
    }

    constructor(activity: Activity, context: Context, resId: Int, iconID: Int, fn: View.OnClickListener) {
        fab = activity.findViewById(resId)
        setupFab(context, iconID)
        setOnClickListener(fn)
    }

    fun setOnClickListener(fn: View.OnClickListener) = fab.setOnClickListener(fn)

    var visibility
        get() = fab.visibility
        set(newValue) {
            when (newValue) {
                View.GONE -> fab.hide()
                View.VISIBLE -> fab.show()
            }
        }

    fun fabSetResDrawable(context: Context, resourceDrawable: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val drawable = ContextCompat.getDrawable(context, resourceDrawable)
            fab.setImageDrawable(drawable)
        } else {
            val drawable = ContextCompat.getDrawable(context, resourceDrawable)!!
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            fab.setImageBitmap(bitmap)
        }
    }

    private fun setupFab(context: Context, icon: Int) {
        if (UIPreferences.themeIsWhite) fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue_accent))
        fabSetResDrawable(context, icon)
        fab.elevation = MyApplication.fabElevation
        fab.translationZ = MyApplication.fabElevationDepressed
    }

    private fun setupFab(context: Context) {
        if (UIPreferences.themeIsWhite) fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue_accent))
        fab.elevation = MyApplication.fabElevation
        fab.translationZ = MyApplication.fabElevationDepressed
    }
}



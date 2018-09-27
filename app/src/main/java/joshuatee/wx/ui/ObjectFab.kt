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
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatDrawableManager
import android.view.View
import android.widget.RemoteViews

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences

class ObjectFab {

    val fab: FloatingActionButton

    constructor(activity: Activity, context: Context, resId: Int) {
        fab = activity.findViewById(resId)
        setupFAB(context, fab)
    }

    constructor(activity: Activity, context: Context, resId: Int, iconID: Int) {
        fab = activity.findViewById(resId)
        setupFAB(context, fab, iconID)
    }

    fun setOnClickListener(fn: View.OnClickListener) {
        fab.setOnClickListener(fn)
    }

    fun setVisibility(vis: Int) {
        fab.visibility = vis
    }

    companion object {

        private fun setupFAB(context: Context, fab: FloatingActionButton) {
            if (UIPreferences.themeIsWhite) {
                fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue_accent))
            }
            if (android.os.Build.VERSION.SDK_INT > 20) {
                fab.elevation = MyApplication.fabElevation
                fab.translationZ = MyApplication.fabElevationDepressed
            }
        }

        fun fabSetResDrawable(context: Context, fab: RemoteViews, ib: Int, resdraw: Int) {
            val wrappedContext = ContextWrapper(context)
            val d = ContextCompat.getDrawable(wrappedContext, resdraw)!!
            val b = Bitmap.createBitmap(d.intrinsicWidth, d.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val c = Canvas(b)
            d.setBounds(0, 0, c.width, c.height)
            d.draw(c)
            fab.setImageViewBitmap(ib, b)
        }

        fun fabSetResDrawable(context: Context, fab: FloatingActionButton, resdraw: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fab.setImageDrawable(ContextCompat.getDrawable(context, resdraw))
            } else {
                val d = AppCompatDrawableManager.get().getDrawable(context, resdraw)
                val b = Bitmap.createBitmap(d.intrinsicWidth, d.intrinsicHeight, Bitmap.Config.ARGB_8888)
                val c = Canvas(b)
                d.setBounds(0, 0, c.width, c.height)
                d.draw(c)
                fab.setImageBitmap(b)
            }
        }

        private fun setupFAB(context: Context, fab: FloatingActionButton, icon: Int) {
            if (UIPreferences.themeIsWhite) fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue_accent))
            fabSetResDrawable(context, fab, icon)
            if (android.os.Build.VERSION.SDK_INT > 20) {
                fab.elevation = MyApplication.fabElevation
                fab.translationZ = MyApplication.fabElevationDepressed
            }
        }
    }
}



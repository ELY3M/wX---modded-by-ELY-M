/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import android.graphics.Color
import android.os.Build
import com.google.android.material.snackbar.Snackbar
import androidx.cardview.widget.CardView
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.util.Utility
import android.util.TypedValue

object UtilityUI {

    // called from LOCFRAG and ObjectSettingsSpinner
    fun setupSpinner(spinner: Spinner, light: Boolean) {
        var tint = ColorStateList.valueOf(UIPreferences.colorBlack)
        if (light) {
            tint = ColorStateList.valueOf(UIPreferences.colorOffwhiteToolbar)
        }
        if (Build.VERSION.SDK_INT > 20) {
            spinner.backgroundTintList = tint
        }
    }

    fun setResDrawable(context: Context, fab: RemoteViews, ib: Int, resdraw: Int) {
        val wrappedContext = ContextWrapper(context)
        val d = ContextCompat.getDrawable(wrappedContext, resdraw)!!
        val b =
            Bitmap.createBitmap(d.intrinsicWidth, d.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        d.setBounds(0, 0, c.width, c.height)
        d.draw(c)
        fab.setImageViewBitmap(ib, b)
    }

    fun makeToastLegacy(context: Context, msg: String) {
        val view = View.inflate(context, R.layout.toast, null)
        val ll: LinearLayout = view.findViewById(R.id.toast_layout_root)
        val text: TextView = ll.findViewById(R.id.text)
        text.text = msg
        val toast = Toast(context.applicationContext)
        toast.duration = Toast.LENGTH_LONG
        toast.view = ll
        toast.show()
    }

    fun makeSnackBar(view: View, message: String) {
        val snack = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
        snack.setActionTextColor(Color.YELLOW)
        snack.setAction("DISMISS") { snack.dismiss() }
        val viewSnack = snack.view
        val tv = viewSnack.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        val fgColor = Color.WHITE
        val bgColor = Color.BLACK
        tv.setTextColor(fgColor)
        viewSnack.setBackgroundColor(bgColor)
        snack.show()
    }

    fun immersiveMode(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 19 && UIPreferences.radarImmersiveMode) {
            activity.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
    }

    fun cardViewSetup(cv: CardView) {
        cv.setCardBackgroundColor(UtilityTheme.primaryColorFromSelectedTheme)
        cv.cardElevation = MyApplication.cardElevation
        cv.setContentPadding(2, 2, 2, 2)
        cv.radius = MyApplication.cardCorners
        cv.useCompatPadding = true
        cv.preventCornerOverlap = true
    }

    fun moveUp(context: Context, prefToken: String, ridArr: MutableList<String>, pos: Int): String {
        if (pos != 0) {
            val tmp = ridArr[pos - 1]
            ridArr[pos - 1] = ridArr[pos]
            ridArr[pos] = tmp
        } else {
            val tmp = ridArr.last()
            //ridArr[ridArr.size - 1] = ridArr[pos]
            ridArr[ridArr.lastIndex] = ridArr[pos]
            ridArr[0] = tmp
        }
        var ridFav = ""
        ridArr.indices.forEach {
            ridFav = ridFav + ":" + MyApplication.semicolon.split(ridArr[it])[0]
        }
        Utility.writePref(context, prefToken, ridFav)
        return ridFav
    }

    fun moveDown(
        context: Context,
        prefToken: String,
        ridArr: MutableList<String>,
        pos: Int
    ): String {
        if (pos != ridArr.lastIndex) {
            val tmp = ridArr[pos + 1]
            ridArr[pos + 1] = ridArr[pos]
            ridArr[pos] = tmp
        } else {
            val tmp = ridArr.first()
            ridArr[0] = ridArr[pos]
            ridArr[ridArr.lastIndex] = tmp
        }
        var ridFav = ""
        ridArr.indices.forEach {
            ridFav = ridFav + ":" + MyApplication.semicolon.split(ridArr[it])[0]
        }
        Utility.writePref(context, prefToken, ridFav)
        return ridFav
    }

    fun statusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun navigationBarHeight(context: Context): Int {
        var result = 0
        val resourceId =
            context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun spToPx(sp: Int, context: Context): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), context.resources.displayMetrics)
    }
}

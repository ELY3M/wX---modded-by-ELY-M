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

import android.app.ActionBar.LayoutParams
import android.app.Activity
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup

import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.radar.VideoRecordActivity

object UtilityToolbar {

    fun transparentToolbars(toolbar: Toolbar, toolbarBottom: Toolbar) {
        if (UIPreferences.radarToolbarTransparent) {
            toolbar.background.mutate().alpha = 0
            toolbarBottom.background.mutate().alpha = 0
        }
    }

    /*fun transparentToolbars(context: Context, toolbar: Toolbar) {
        if (UIPreferences.radarToolbarTransparent) {
            toolbar.background.mutate().alpha = 0
            toolbar.background.alpha = 0
            toolbar.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        }
    }*/

    fun fullScreenMode(toolbar: Toolbar, toolbarBottom: Toolbar) {
        if (android.os.Build.VERSION.SDK_INT > 20) {
            toolbar.elevation = MyApplication.elevationPref
            toolbarBottom.elevation = MyApplication.elevationPref

        }
        if (MyApplication.fullscreenMode) {
            toolbar.visibility = View.GONE
            toolbarBottom.visibility = View.GONE
        }
    }

    fun fullScreenMode(activity: VideoRecordActivity) {
        fullScreenMode(activity.toolbar, activity.toolbarBottom)
    }

    fun fullScreenMode(toolbar: Toolbar) {
        if (android.os.Build.VERSION.SDK_INT > 20) toolbar.elevation = MyApplication.elevationPref
        if (MyApplication.fullscreenMode) toolbar.visibility = View.GONE
    }

    // overload to simply set elevation
    fun fullScreenMode(toolbar: Toolbar, blank: Boolean) {
        if (android.os.Build.VERSION.SDK_INT > 20) toolbar.elevation = MyApplication.elevationPref
    }

    fun showHide(toolbar: Toolbar, toolbarBottom: Toolbar) {
        if (!MyApplication.lockToolbars) {
            if (toolbar.isShown) {
                toolbar.visibility = View.GONE
                toolbarBottom.visibility = View.GONE
            } else {
                toolbar.visibility = View.VISIBLE
                toolbarBottom.visibility = View.VISIBLE
            }
        }
    }

    fun showHide(toolbar: Toolbar) {
        if (!MyApplication.lockToolbars) {
            if (toolbar.isShown)
                toolbar.visibility = View.GONE
            else
                toolbar.visibility = View.VISIBLE
        }
    }

    // thanks inner_class7 http://stackoverflow.com/questions/26489079/evenly-spaced-menu-items-on-toolbar
    // modified slightly

    /**
     * This method will take however many items you have in your
     * menu/menu_main.xml and distribute them across your devices screen
     * evenly using a Toolbar. Enjoy
     */
    fun setupEvenlyDistributedToolbar(activity: Activity, toolbarBottom: Toolbar, menuRes: Int) {
        // Use Display metrics to get Screen Dimensions
        val display = activity.windowManager.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        // Toolbar
        // Inflate your menu
        toolbarBottom.inflateMenu(menuRes)
        // Add 10 spacing on either side of the toolbar
        toolbarBottom.setContentInsetsAbsolute(10, 10)
        // Get the ChildCount of your Toolbar, this should only be 1
        val childCount = toolbarBottom.childCount
        // Get the Screen Width in pixels
        val screenWidth = metrics.widthPixels
        // Create the Toolbar Params based on the screenWidth
        val toolbarParams = Toolbar.LayoutParams(screenWidth, LayoutParams.WRAP_CONTENT)
        // Loop through the child Items
        for (i in 0 until childCount) {
            // Get the item at the current index
            val childView = toolbarBottom.getChildAt(i)
            // If its a ViewGroup
            if (childView is ViewGroup) {
                // Set its layout params
                childView.setLayoutParams(toolbarParams)
                // Get the child count of this view group, and compute the item widths based on this count & screen size
                val innerChildCount = childView.childCount
                val itemWidth = screenWidth / innerChildCount
                // Create layout params for the ActionMenuView
                val params = ActionMenuView.LayoutParams(itemWidth, LayoutParams.WRAP_CONTENT)
                // Loop through the children
                (0 until innerChildCount)
                    .map { childView.getChildAt(it) }
                    .filterIsInstance<ActionMenuItemView>()
                    .forEach { it.layoutParams = params }
            }
        }
    }
}

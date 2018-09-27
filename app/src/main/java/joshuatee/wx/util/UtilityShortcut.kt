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

package joshuatee.wx.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import androidx.appcompat.widget.Toolbar
import android.view.Menu

import joshuatee.wx.R
import joshuatee.wx.activitiesmisc.AFDActivity
import joshuatee.wx.activitiesmisc.SevereDashboardActivity
import joshuatee.wx.objects.ObjectIntentShortcut
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.radar.USNWSMosaicActivity
import joshuatee.wx.spc.SPCSWOSummaryActivity
import joshuatee.wx.vis.GOES16Activity

// add to menu
// R.id.action_pin -> UtilityShortcut.createShortcut(this, ShortcutType.SevereDashboard)
// use Enum addition and then add to when below

/*

<item android:id="@+id/action_pin"
        android:title="@string/label_pin"
        app:showAsAction="always"
        />

if (android.os.Build.VERSION.SDK_INT < 26) {
    //val menu = toolbar.menu
    val pin = menu.findItem(R.id.action_pin)
    pin.isVisible=false
}

*/

object UtilityShortcut {

    fun createShortcut(context: Context, type: ShortcutType) {
        // https://developer.android.com/guide/topics/ui/shortcuts
        // Pinned shortcuts in API 26 8.0
        if (android.os.Build.VERSION.SDK_INT > 25) {
            //val mShortcutManager = getSystemService(ShortcutManager::class.java)

            /* if (mShortcutManager.isRequestPinShortcutSupported) {
                 // Assumes there's already a shortcut with the ID "my-shortcut".
                 // The shortcut must be enabled.

                 listOf(shortcutId).forEach {
                     val pinShortcutInfo = ShortcutInfo.Builder(this, it).build()
                     // Create the PendingIntent object only if your app needs to be notified
                     // that the user allowed the shortcut to be pinned. Note that, if the
                     // pinning operation fails, your app isn't notified. We assume here that the
                     // app has implemented a method called createShortcutResultIntent() that
                     // returns a broadcast intent.
                     val pinnedShortcutCallbackIntent = mShortcutManager.createShortcutResultIntent(pinShortcutInfo)
                     // Configure the intent so that your app's broadcast receiver gets
                     // the callback successfully.
                     val successCallback = PendingIntent.getBroadcast(this, 0, pinnedShortcutCallbackIntent, 0)
                     mShortcutManager.requestPinShortcut(pinShortcutInfo, successCallback.intentSender)
                 }
             }*/

            //var shortcutId = ""
            //var clz: Class<*> = SevereDashboardActivity::class.java

            val intent: Intent?
            var imageId = 0

            when (type) {
                ShortcutType.SevereDashboard -> {
                    //shortcutId = "SD"
                    intent = ObjectIntentShortcut(context, SevereDashboardActivity::class.java).intent
                    imageId = R.drawable.ntor
                }
                ShortcutType.AFD -> {
                    //shortcutId = "AFD"
                    intent = ObjectIntentShortcut(context, AFDActivity::class.java).intent
                    imageId = R.drawable.widget_afd
                }
                ShortcutType.GOES16 -> {
                    //shortcutId = "GOES16"
                    intent = ObjectIntentShortcut(context, GOES16Activity::class.java, GOES16Activity.RID, arrayOf("")).intent
                    imageId = R.drawable.goes
                }
                ShortcutType.RADAR_MOSAIC -> {
                    //shortcutId = "GOES16"
                    intent = ObjectIntentShortcut(context, USNWSMosaicActivity::class.java).intent
                    imageId = R.drawable.widget_radar_mosaic
                }
                ShortcutType.SPC_SWO_SUMMARY -> {
                    intent = ObjectIntentShortcut(context, SPCSWOSummaryActivity::class.java).intent
                    imageId = R.drawable.spc_sum
                }
            }
            val shortcutId = type.toString()

            //val intent = ObjectIntentShortcut(context, clz).intent

            //val intent = Intent(Intent.ACTION_VIEW, Uri.EMPTY, context, clz)
            // ObjectIntent(contextg, SPCSWOActivity::class.java, SPCSWOActivity.NO, arrayOf(day, ""))

            val mShortcutManager = context.getSystemService(ShortcutManager::class.java)
            val shortcut = ShortcutInfo.Builder(context, shortcutId)
                    .setShortLabel(shortcutId)
                    .setLongLabel(shortcutId)
                    .setIcon(Icon.createWithResource(context, imageId))
                    .setIntents(arrayOf(
                            Intent(Intent.ACTION_MAIN, Uri.EMPTY, context, joshuatee.wx.WX::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
                            intent))
                    .build()
            //mShortcutManager.setDynamicShortcuts(Arrays.asList(shortcut))
            val pinnedShortcutCallbackIntent = mShortcutManager.createShortcutResultIntent(shortcut)
            val successCallback = PendingIntent.getBroadcast(context, 0, pinnedShortcutCallbackIntent, 0)
            mShortcutManager.requestPinShortcut(shortcut, successCallback.intentSender)

        }
    }

    fun hidePinIfNeeded(toolbar: Toolbar) {
        if (android.os.Build.VERSION.SDK_INT < 26) {
            val menu = toolbar.menu
            val pin = menu.findItem(R.id.action_pin)
            pin.isVisible = false
        }
    }

    fun hidePinIfNeeded(menu: Menu) {
        if (android.os.Build.VERSION.SDK_INT < 26) {
            //val menu = toolbar.menu
            val pin = menu.findItem(R.id.action_pin)
            pin.isVisible = false
        }
    }
}





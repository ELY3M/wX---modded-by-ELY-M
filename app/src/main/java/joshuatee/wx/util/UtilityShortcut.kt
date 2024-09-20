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
import joshuatee.wx.misc.WfoTextActivity
import joshuatee.wx.misc.SevereDashboardActivity
import joshuatee.wx.objects.ObjectIntentShortcut
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.radar.RadarMosaicNwsActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.spc.SpcSwoSummaryActivity
import joshuatee.wx.vis.GoesActivity

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

    fun create(context: Context, type: ShortcutType) {
        // https://developer.android.com/guide/topics/ui/shortcuts
        // Pinned shortcuts in API 26 8.0
        val intent: Intent?
        var imageId = 0
        when (type) {
            ShortcutType.SevereDashboard -> {
                intent = ObjectIntentShortcut(context, SevereDashboardActivity::class.java).intent
                imageId = R.drawable.ntor
            }

            ShortcutType.AFD -> {
                intent = ObjectIntentShortcut(
                    context,
                    WfoTextActivity::class.java,
                    WfoTextActivity.URL,
                    arrayOf(Location.wfo, "")
                ).intent
                imageId = R.drawable.widget_afd
            }

            ShortcutType.GOES16 -> {
                intent = ObjectIntentShortcut(
                    context,
                    GoesActivity::class.java,
                    GoesActivity.RID,
                    arrayOf("")
                ).intent
                imageId = R.drawable.goes
            }

            ShortcutType.RADAR_MOSAIC -> {
                intent = ObjectIntentShortcut(context, RadarMosaicNwsActivity::class.java).intent
                imageId = R.drawable.widget_radar_mosaic
            }

            ShortcutType.SPC_SWO_SUMMARY -> {
                intent = ObjectIntentShortcut(context, SpcSwoSummaryActivity::class.java).intent
                imageId = R.drawable.spc_sum
            }
        }
        val shortcutId = type.toString()
        val shortcutManager = context.getSystemService(ShortcutManager::class.java)
        val shortcut = ShortcutInfo.Builder(context, shortcutId)
            .setShortLabel(shortcutId)
            .setLongLabel(shortcutId)
            .setIcon(Icon.createWithResource(context, imageId))
            .setIntents(
                arrayOf(
                    Intent(
                        Intent.ACTION_MAIN,
                        Uri.EMPTY,
                        context,
                        joshuatee.wx.WX::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK), intent
                )
            )
            .build()
        val pinnedShortcutCallbackIntent = shortcutManager!!.createShortcutResultIntent(shortcut)
        val successCallback = PendingIntent.getBroadcast(
            context,
            0,
            pinnedShortcutCallbackIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        shortcutManager.requestPinShortcut(shortcut, successCallback.intentSender)
    }
}

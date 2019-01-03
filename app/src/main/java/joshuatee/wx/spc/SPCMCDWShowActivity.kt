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
//modded by ELY M. 

package joshuatee.wx.spc

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.ContextMenu.ContextMenuInfo
import android.widget.LinearLayout

import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.audio.UtilityTTS
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityShare
import kotlinx.coroutines.*

class SPCMCDWShowActivity : AudioPlayActivity(), OnMenuItemClickListener {

    // show a specific MCD or WAT, long press on image to save location
    //
    // Arugments
    //
    // 1: number of MCD or WAT such as 0403
    //

    companion object {
        const val NO: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var number = ""
    private lateinit var activityArguments: Array<String>
    private lateinit var c0: ObjectCardImage
    private lateinit var c1: ObjectCardText
    private lateinit var objWatch: ObjectWatchProduct
    private lateinit var linearLayout: LinearLayout
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_linear_layout_bottom_toolbar,
            R.menu.spcmcdshowdetail
        )
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        linearLayout = findViewById(R.id.ll)
        c0 = ObjectCardImage(this, linearLayout)
        c1 = ObjectCardText(this, linearLayout, toolbar, toolbarBottom)
        activityArguments = intent.getStringArrayExtra(NO)
        number = activityArguments[0]
        when (activityArguments[2]) {
            "MCD" -> objWatch = ObjectWatchProduct(PolygonType.MCD, number)
            "WATCH_TOR" -> objWatch = ObjectWatchProduct(PolygonType.WATCH_TOR, number)
            "WATCH_SVR" -> objWatch = ObjectWatchProduct(PolygonType.WATCH_SVR, number)
            "MPD" -> objWatch = ObjectWatchProduct(PolygonType.MPD, number)
            else -> {
            }
        }
        title = objWatch.title
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        withContext(Dispatchers.IO) { objWatch.getData(contextg) }
        c1.setText(Utility.fromHtml(objWatch.text))
        if (activityArguments[2] == "MCD" || activityArguments[2] == "MPD") {
            toolbar.subtitle = objWatch.textForSubtitle
        }
        c0.setImage(objWatch.bitmap)
        registerForContextMenu(c0.img)
        if (activityArguments[1] == "sound") {
            UtilityTTS.synthesizeTextAndPlay(applicationContext, objWatch.text, objWatch.prod)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        (0 until objWatch.wfoArr.size - 1).forEach {
            menu.add(
                0,
                v.id,
                0,
                "Add location: " + objWatch.wfoArr[it] + " - " + Utility.readPref(
                    this,
                    "NWS_LOCATION_" + objWatch.wfoArr[it],
                    ""
                )
            )
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val itemStr = item.title.toString()
        (0 until objWatch.wfoArr.size - 1)
            .filter { itemStr.contains(objWatch.wfoArr[it]) }
            .forEach {
                UtilityLocation.saveLocationForMcd(
                    objWatch.wfoArr[it],
                    contextg,
                    linearLayout,
                    uiDispatcher
                )
            }
        return true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, objWatch.text, number, objWatch.prod)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share_all -> UtilityShare.shareText(
                this,
                objWatch.title,
                Utility.fromHtml(objWatch.text),
                objWatch.bitmap
            )
            R.id.action_share_text -> UtilityShare.shareText(
                this,
                objWatch.title,
                Utility.fromHtml(objWatch.text)
            )
            R.id.action_share_url -> UtilityShare.shareText(this, objWatch.title, objWatch.textUrl)
            R.id.action_share_image -> UtilityShare.shareBitmap(
                this,
                objWatch.title,
                objWatch.bitmap
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

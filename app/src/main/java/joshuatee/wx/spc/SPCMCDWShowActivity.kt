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

package joshuatee.wx.spc

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
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
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityShare

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

    private var no = ""
    private lateinit var turl: Array<String>
    private lateinit var c0: ObjectCardImage
    private lateinit var c1: ObjectCardText
    private lateinit var objWatch: ObjectWatchProduct
    private lateinit var linearLayout: LinearLayout
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_bottom_toolbar, R.menu.spcmcdshowdetail)
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        c0 = ObjectCardImage(this)
        c1 = ObjectCardText(this)
        linearLayout = findViewById(R.id.ll)
        linearLayout.addView(c0.card)
        linearLayout.addView(c1.card)
        c1.setOnClickListener(View.OnClickListener { UtilityToolbar.showHide(toolbar, toolbarBottom) })
        turl = intent.getStringArrayExtra(NO)
        no = turl[0]
        when (turl[2]) {
            "MCD" -> objWatch = ObjectWatchProduct(PolygonType.MCD, no)
            "WATCH" -> objWatch = ObjectWatchProduct(PolygonType.WATCH, no)
            "MPD" -> objWatch = ObjectWatchProduct(PolygonType.MPD, no)
            else -> {
            }
        }
        title = objWatch.title
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {
            objWatch.getData(contextg)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            c1.setText(Utility.fromHtml(objWatch.text))
            if (turl[2] == "MCD" || turl[2] == "MPD") {
                toolbar.subtitle = objWatch.textForSubtitle
            }
            c0.setImage(objWatch.bitmap)
            registerForContextMenu(c0.img)
            if (turl[1] == "sound") {
                UtilityTTS.synthesizeTextAndPlay(applicationContext, objWatch.text, objWatch.prod)
            }
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        (0 until objWatch.wfoArr.size - 1).forEach {
            menu.add(0, v.id, 0, "Add location: " + objWatch.wfoArr[it] + " - " + Utility.readPref(this, "NWS_LOCATION_" + objWatch.wfoArr[it], ""))
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val itemStr = item.title.toString()
        (0 until objWatch.wfoArr.size - 1)
                .filter { itemStr.contains(objWatch.wfoArr[it]) }
                .forEach { saveLocation(objWatch.wfoArr[it]) }
        return true
    }

    private fun saveLocation(nwsOffice: String) {
        SaveLoc().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, nwsOffice)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class SaveLoc : AsyncTask<String, String, String>() {
        internal var toastStr = ""
        override fun doInBackground(vararg params: String): String {
            var locNumIntCurrent = Location.numLocations
            locNumIntCurrent += 1
            val locNumToSaveStr = locNumIntCurrent.toString()
            val loc = Utility.readPref(contextg, "NWS_LOCATION_" + params[0], "")
            val addrSend = loc.replace(" ", "+")
            val xyStr = UtilityLocation.getXYFromAddressOSM(addrSend)
            toastStr = Location.locationSave(contextg, locNumToSaveStr, xyStr[0], xyStr[1], loc)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            UtilityUI.makeSnackBar(linearLayout, toastStr)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, objWatch.text, no, objWatch.prod)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share_all -> UtilityShare.shareText(this, objWatch.title, Utility.fromHtml(objWatch.text), objWatch.bitmap)
            R.id.action_share_text -> UtilityShare.shareText(this, objWatch.title, Utility.fromHtml(objWatch.text))
            R.id.action_share_url -> UtilityShare.shareText(this, objWatch.title, objWatch.textUrl)
            R.id.action_share_image -> UtilityShare.shareBitmap(this, objWatch.title, objWatch.bitmap)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

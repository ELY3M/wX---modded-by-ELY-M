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

package joshuatee.wx.spc

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.view.ContextMenu.ContextMenuInfo

import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityShare
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_linear_layout_bottom_toolbar.*

class SpcMcdWatchShowActivity : AudioPlayActivity(), OnMenuItemClickListener {

    // show a specific MCD, Watch, or MPD - long press on image to save location
    //
    // Arguments
    //
    // 1: number of MCD, WAT, or MPD such as 0403
    //

    companion object {
        const val NO: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var number = ""
    private lateinit var activityArguments: Array<String>
    private lateinit var objectCardImage: ObjectCardImage
    private lateinit var objectCardText: ObjectCardText
    private lateinit var objectWatchProduct: ObjectWatchProduct

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
                savedInstanceState,
                R.layout.activity_linear_layout_bottom_toolbar,
                R.menu.spcmcdshowdetail
        )
        toolbarBottom.setOnMenuItemClickListener(this)
        objectCardImage = ObjectCardImage(this, ll)
        objectCardText = ObjectCardText(this, ll, toolbar, toolbarBottom)
        activityArguments = intent.getStringArrayExtra(NO)
        number = activityArguments[0]
        when (activityArguments[2]) {
            "MCD" -> objectWatchProduct = ObjectWatchProduct(PolygonType.MCD, number)
            "WATCH" -> objectWatchProduct = ObjectWatchProduct(PolygonType.WATCH, number)
            "MPD" -> objectWatchProduct = ObjectWatchProduct(PolygonType.MPD, number)
            else -> {
            }
        }
        title = objectWatchProduct.title
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        withContext(Dispatchers.IO) { objectWatchProduct.getData(this@SpcMcdWatchShowActivity) }
        objectCardText.setText(Utility.fromHtml(objectWatchProduct.text))
        toolbar.subtitle = objectWatchProduct.textForSubtitle
        objectCardImage.setImage(objectWatchProduct.bitmap)
        registerForContextMenu(objectCardImage.img)
        UtilityTts.conditionalPlay(
                activityArguments,
                1,
                applicationContext,
                objectWatchProduct.text,
                objectWatchProduct.prod
        )
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        objectWatchProduct.wfos.forEach {
            menu.add(0, v.id, 0, "Add location: $it - " + Utility.readPref(
                    this,
                    "NWS_LOCATION_$it",
                    ""
            )
            )
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        objectWatchProduct.wfos.filter { item.title.toString().contains(it) }.forEach {
            UtilityLocation.saveLocationForMcd(
                    it,
                    this@SpcMcdWatchShowActivity,
                    ll,
                    uiDispatcher
            )
        }
        return true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, objectWatchProduct.text, number, objectWatchProduct.prod)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share_all -> UtilityShare.shareBitmap(
                    this,
                    this,
                    objectWatchProduct.title,
                    objectWatchProduct.bitmap,
                    Utility.fromHtml(objectWatchProduct.text)
            )
            R.id.action_share_text -> UtilityShare.shareText(
                    this,
                    objectWatchProduct.title,
                    Utility.fromHtml(objectWatchProduct.text)
            )
            R.id.action_share_url -> UtilityShare.shareText(this, objectWatchProduct.title, objectWatchProduct.textUrl)
            R.id.action_share_image -> UtilityShare.shareBitmap(
                    this,
                    this,
                    objectWatchProduct.title,
                    objectWatchProduct.bitmap
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

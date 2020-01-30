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

package joshuatee.wx.spc

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.settings.Location
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare

import joshuatee.wx.GlobalArrays
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.*
import joshuatee.wx.util.Utility
import kotlinx.coroutines.*

class SpcSwoStateGraphicsActivity : VideoRecordActivity(), OnItemSelectedListener,
        OnMenuItemClickListener {

    // Show state level SPC SWO graphics for D1-3
    //
    // Arguments
    // 1: day

    companion object {
        const val NO: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var day = ""
    private var imgUrl = ""
    private lateinit var img: ObjectTouchImageView
    private var state = ""
    private var bitmap = UtilityImg.getBlankBitmap()
    private var firstTime = true
    private val imgPrefToken = "SWO_STATE"

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
                savedInstanceState,
                R.layout.activity_spcswostate,
                R.menu.spcswostate,
                iconsEvenlySpaced = true,
                bottomToolbar = true
        )
        toolbarBottom.setOnMenuItemClickListener(this)
        day = intent.getStringArrayExtra(NO)!![0]
        state = Utility.readPref(this, "NWS_LOCATION_${Location.wfo}", "").split(",")[0]
        img = ObjectTouchImageView(this, this, toolbar, toolbarBottom, R.id.iv)
        ObjectSpinner(this, this, this, R.id.spinner1, GlobalArrays.states, state)
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        title = "SWO D$day"
        imgUrl = UtilitySpcSwo.getSwoStateUrl(state, day)
        bitmap = withContext(Dispatchers.IO) { imgUrl.getImage() }
        img.img.visibility = View.VISIBLE
        img.setBitmap(bitmap)
        img.firstRunSetZoomPosn(imgPrefToken)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareBitmap(
                    this,
                    this,
                    "$state SWO D$day",
                    bitmap
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (firstTime) {
            UtilityToolbar.fullScreenMode(this)
            firstTime = false
        }
        img.setZoom(1.0f)
        state = GlobalArrays.states[pos].split(":")[0]
        getContent()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    override fun onStop() {
        img.imgSavePosnZoom(this, imgPrefToken)
        super.onStop()
    }
}





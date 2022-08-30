/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.settings.Location
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.objects.FutureBytes
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.*
import joshuatee.wx.util.Utility

class SpcSwoStateGraphicsActivity : VideoRecordActivity() {

    //
    // Show state level SPC SWO graphics for D1-3
    //
    // Arguments
    // 1: day
    //

    companion object { const val NO = "" }

    private var day = ""
    private lateinit var image: TouchImage
    private var state = ""
    private var firstTime = true
    private val imgPrefToken = "SWO_STATE"

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.spcswostate_top, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_sector).title = state
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_spcswostate, R.menu.spcswostate_top, iconsEvenlySpaced = true, bottomToolbar = false)
        day = intent.getStringArrayExtra(NO)!![0]
        state = Utility.getWfoSiteName(Location.wfo).split(",")[0]
        image = TouchImage(this, toolbar, R.id.iv)
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        title = "SWO D$day"
        invalidateOptionsMenu()
        val imgUrl = UtilitySpcSwo.getSwoStateUrl(state, day)
        FutureBytes(this, imgUrl, ::showImage)
    }

    private fun showImage(bitmap: Bitmap) {
        image.set(bitmap)
        image.firstRun(imgPrefToken)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sector -> ObjectDialogue.generic(this,
                    GlobalArrays.states.filter { !it.startsWith("AK") && !it.startsWith("HI") },
                    ::getContent) {
                if (firstTime) {
                    UtilityToolbar.fullScreenMode(this)
                    firstTime = false
                }
                image.setZoom(1.0f)
                state = GlobalArrays.states.filter { state ->
                    !state.startsWith("AK") && !state.startsWith("HI") }[it].split(":")[0]
            }
            R.id.action_share -> UtilityShare.bitmap(this, "$state SWO D$day", image)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onStop() {
        image.imgSavePosnZoom(imgPrefToken)
        super.onStop()
    }
}

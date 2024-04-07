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

package joshuatee.wx.spc

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.getImage
import joshuatee.wx.R
import joshuatee.wx.settings.Location
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.ImageSummary
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.UtilityImg

class SpcSwoStateGraphicsActivity : VideoRecordActivity() {

    //
    // Show state level SPC SWO graphics for D1-3
    //
    // Arguments
    // 1: day
    //

    companion object {
        const val NO = ""
    }

    private var day = ""
    private var state = ""
    private var bitmaps = mutableListOf<Bitmap>()
    private var urls = listOf<String>()
    private lateinit var box: VBox
    private lateinit var imageSummary: ImageSummary

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.spcswostate_top, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_sector).title = state
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_spcswostate, R.menu.spcswostate_top, bottomToolbar = false)
        day = intent.getStringArrayExtra(NO)!![0]
        state = UtilityLocation.getWfoSiteName(Location.wfo).split(",")[0]
        box = VBox.fromResource(this)
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        title = "SWO D$day"
        urls = UtilitySpcSwo.getSwoStateUrl(state, day)
        invalidateOptionsMenu()
        bitmaps = MutableList(urls.size) { UtilityImg.getBlankBitmap() }
        box.removeChildrenAndLayout()
        imageSummary = ImageSummary(this, box, bitmaps)
        urls.forEachIndexed { index, url ->
            FutureVoid({ bitmaps[index] = url.getImage() }, { update(index) })
        }
    }

    private fun update(index: Int) {
        imageSummary.set(index, bitmaps[index])
        imageSummary.connect(index) { Route.image(this, urls[index], "") }
    }

    private val statesLower48
        get() = GlobalArrays.states.filter { !it.startsWith("AK") && !it.startsWith("HI") }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sector -> ObjectDialogue.generic(this, statesLower48, ::getContent) {
                state = statesLower48[it].split(":")[0]
            }

            R.id.action_share -> UtilityShare.text(this, "$state SWO D$day", "", bitmaps) // UtilityShare.bitmap(this, "$state SWO D$day", touchImage)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

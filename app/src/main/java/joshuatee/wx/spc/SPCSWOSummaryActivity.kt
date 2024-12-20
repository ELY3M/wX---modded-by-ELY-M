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
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.getImage
import joshuatee.wx.R
import joshuatee.wx.objects.DownloadTimer
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ImageSummary
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.UtilityShortcut

class SpcSwoSummaryActivity : BaseActivity() {

    private val bitmaps = MutableList(8) { UtilityImg.getBlankBitmap() }
    private lateinit var box: VBox
    private lateinit var imageSummary: ImageSummary
    private var downloadTimer = DownloadTimer("ACTIVITY_SPC_SWO_SUMMARY")

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.spc_swo_summary, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_linear_layout,
            R.menu.spc_swo_summary,
            false
        )
        setTitle("Convective Outlooks", "SPC")
        box = VBox.fromResource(this)
        imageSummary = ImageSummary(this, box, bitmaps)
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        if (downloadTimer.isRefreshNeeded()) {
            (0..2).forEach {
                FutureVoid({
                    bitmaps[it] = UtilitySpcSwo.getUrls((it + 1).toString())[0].getImage()
                }) { update(it) }
            }
            (3..7).forEach {
                FutureVoid({
                    bitmaps[it] = UtilitySpcSwo.getImageUrlsDays48((it + 1).toString()).getImage()
                }) { update(it) }
            }
        }
    }

    private fun update(index: Int) {
        val day = if (index < 3) {
            (index + 1).toString()
        } else {
            "4-8"
        }
        imageSummary.set(index, bitmaps[index])
        imageSummary.connect(index) { Route.spcSwo(this, day) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_pin -> UtilityShortcut.create(this, ShortcutType.SPC_SWO_SUMMARY)
            R.id.action_share -> UtilityShare.text(this, "Convective Outlook Summary", "", bitmaps)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

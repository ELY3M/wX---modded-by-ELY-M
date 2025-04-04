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
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ImageSummary
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare

class SpcFireOutlookSummaryActivity : BaseActivity() {

    //
    // SPC Fire Weather Outlooks
    //

    private val bitmaps =
        MutableList(UtilitySpcFireOutlook.urls.size) { UtilityImg.getBlankBitmap() }
    private lateinit var box: VBox
    private lateinit var imageSummary: ImageSummary
    private var downloadTimer = DownloadTimer("ACTIVITY_SPC_FIRE_SUMMARY")

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.shared_multigraphics, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_linear_layout,
            R.menu.shared_multigraphics,
            false
        )
        setTitle("Fire Weather Outlooks", "SPC")
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
            UtilitySpcFireOutlook.urls.forEachIndexed { index, url ->
                FutureVoid({ bitmaps[index] = url.getImage() }) { updateImage(index) }
            }
        }
    }

    private fun updateImage(index: Int) {
        imageSummary.set(index, bitmaps[index])
        imageSummary.connect(index) { Route.spcFireOutlookByDay(this, index) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.text(this, "SPC Fire Weather Outlooks", "", bitmaps)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

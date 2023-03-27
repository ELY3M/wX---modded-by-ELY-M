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

package joshuatee.wx.wpc

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.R
import joshuatee.wx.objects.DownloadTimer
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ImageSummary
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare

class WpcRainfallForecastSummaryActivity : BaseActivity() {

    //
    // WPC Excessive Rainfall Outlooks
    //

    private val bitmaps = MutableList(UtilityWpcRainfallForecast.urls.size) { UtilityImg.getBlankBitmap() }
    private lateinit var box: VBox
    private lateinit var imageSummary: ImageSummary
    private var downloadTimer = DownloadTimer("ACTIVITY_WPC_RAINFALL_SUMMARY")

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.shared_multigraphics, menu)
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, R.menu.shared_multigraphics, false)
        setTitle("Excessive Rainfall Outlooks", "WPC")
        box = VBox.fromResource(this)
        imageSummary = ImageSummary(this, box, bitmaps)
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        if (downloadTimer.isRefreshNeeded(this)) {
            UtilityWpcRainfallForecast.urls.forEachIndexed { index, url ->
                FutureVoid(this, { bitmaps[index] = url.getImage() }) { update(index) }
            }
        }
    }

    private fun update(index: Int) {
        imageSummary.set(index, bitmaps[index])
        imageSummary.connect(index) { Route.wpcRainfallByDay(this, index.toString()) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.text(this, "WPC Excessive Rainfall Forecast", "", bitmaps)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

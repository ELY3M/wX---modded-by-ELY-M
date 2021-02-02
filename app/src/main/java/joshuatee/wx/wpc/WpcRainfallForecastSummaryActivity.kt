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

package joshuatee.wx.wpc

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.*
import joshuatee.wx.util.UtilityShare
import kotlinx.coroutines.*

class WpcRainfallForecastSummaryActivity : BaseActivity() {

    private val uiDispatcher = Dispatchers.Main
    private var bitmaps = listOf<Bitmap>()
    private var imagesPerRow = 2
    private lateinit var linearLayout: LinearLayout

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.shared_multigraphics, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, R.menu.shared_multigraphics, false)
        linearLayout = findViewById(R.id.linearLayout)
        if (UtilityUI.isLandScape(this)) {
            imagesPerRow = 3
        }
        title = "Excessive Rainfall Outlooks"
        toolbar.subtitle = "WPC"
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        bitmaps = withContext(Dispatchers.IO) {
            UtilityWpcRainfallForecast.urls.map { it.getImage() }
        }
        linearLayout.removeAllViews()
        val objectImageSummary = ObjectImageSummary(this@WpcRainfallForecastSummaryActivity, linearLayout, bitmaps)
        objectImageSummary.objectCardImages.forEachIndexed { index, objectCardImage ->
            objectCardImage.setOnClickListener {
                val textProduct = UtilityWpcRainfallForecast.productCode[index]
                val imageUrl = UtilityWpcRainfallForecast.urls[index]
                val day = (index + 1).toString()
                ObjectIntent(this@WpcRainfallForecastSummaryActivity, WpcRainfallForecastActivity::class.java, WpcRainfallForecastActivity.NUMBER, arrayOf(textProduct, imageUrl, day))
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.text(this, this, getString(UtilityWpcRainfallForecast.activityTitle), "", bitmaps)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

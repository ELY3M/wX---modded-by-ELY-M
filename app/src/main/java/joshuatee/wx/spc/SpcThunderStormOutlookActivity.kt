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
import android.graphics.Bitmap
import android.view.Menu
import android.view.MenuItem
import android.view.View
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.*
import joshuatee.wx.util.UtilityShare
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_linear_layout_bottom_toolbar.*

class SpcThunderStormOutlookActivity : BaseActivity() {

    //
    // SPC Thunderstorm Outlooks
    //

    private val uiDispatcher = Dispatchers.Main
    private var bitmaps = listOf<Bitmap>()
    private var urls = listOf<String>()
    private var imagesPerRow = 2

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.shared_multigraphics, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, R.menu.shared_multigraphics, false)
        if (UtilityUI.isLandScape(this)) imagesPerRow = 3
        toolbar.subtitle = "SPC"
        title = "Thunderstorm Outlooks"
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        urls = withContext(Dispatchers.IO) { UtilitySpc.thunderStormOutlookUrls }
        bitmaps = withContext(Dispatchers.IO) { urls.map { it.getImage() } }
        linearLayout.removeAllViews()
        val objectImageSummary = ObjectImageSummary(this@SpcThunderStormOutlookActivity, linearLayout, bitmaps)
        objectImageSummary.objectCardImages.forEachIndexed { index, objectCardImage ->
            objectCardImage.setOnClickListener(View.OnClickListener {
                ObjectIntent.showImage(this@SpcThunderStormOutlookActivity, arrayOf(urls[index], ""))
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.text(this, this, "SPC Thunderstorm Outlook", "", bitmaps)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

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

import android.annotation.SuppressLint
import android.os.Bundle
import android.graphics.Bitmap
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.R
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.*
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare

class SpcThunderStormOutlookActivity : BaseActivity() {

    //
    // SPC Thunderstorm Outlooks
    //
    private var bitmaps = mutableListOf<Bitmap>()
    private var urls = listOf<String>()
    private var imagesPerRow = 2
    private lateinit var linearLayout: LinearLayout
    private lateinit var objectImageSummary: ObjectImageSummary

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
        toolbar.subtitle = "SPC"
        title = "Thunderstorm Outlooks"
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        FutureVoid(this, { urls = UtilitySpc.thunderStormOutlookUrls }, ::getImages)
    }

    private fun getImages() {
        bitmaps = MutableList(urls.size){ UtilityImg.getBlankBitmap() }
        linearLayout.removeAllViews()
        objectImageSummary = ObjectImageSummary(this, linearLayout, bitmaps)
        urls.indices.forEach {
            FutureVoid(this, { bitmaps[it] = urls[it].getImage() }, { updateImage(it) })
        }
    }

    private fun updateImage(index: Int) {
        objectImageSummary.setImage(index, bitmaps[index])
        objectImageSummary.setOnClickListener(index) {
            ObjectIntent.showImage(this, arrayOf(urls[index], ""))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.text(this, "SPC Thunderstorm Outlook", "", bitmaps)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

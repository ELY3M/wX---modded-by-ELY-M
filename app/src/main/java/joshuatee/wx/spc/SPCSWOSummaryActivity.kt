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
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.R
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.ui.*
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.UtilityShortcut

class SpcSwoSummaryActivity : BaseActivity() {

    private val bitmaps = MutableList(8){ UtilityImg.getBlankBitmap() }
    private var imagesPerRow = 2
    private lateinit var linearLayout: LinearLayout
    private lateinit var objectImageSummary: ObjectImageSummary

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.spc_swo_summary, menu)
        UtilityShortcut.hidePinIfNeeded(menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, R.menu.spc_swo_summary, false)
        linearLayout = findViewById(R.id.linearLayout)
        if (UtilityUI.isLandScape(this)) {
            imagesPerRow = 3
        }
        toolbar.subtitle = "SPC"
        title = "Convective Outlooks"
        objectImageSummary = ObjectImageSummary(this@SpcSwoSummaryActivity, linearLayout, bitmaps)
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        (0..2).forEach {
            FutureVoid(this, { bitmaps[it] = UtilitySpcSwo.getUrls((it + 1).toString())[0].getImage() }) { updateImage(it) }
        }
        (3..7).forEach {
            FutureVoid(this, { bitmaps[it] = UtilitySpcSwo.getImageUrlsDays48((it + 1).toString()).getImage() }) { updateImage(it) }
        }
    }

    private fun updateImage(index: Int) {
        val day = if (index < 3) {
            (index + 1).toString()
        } else {
            "4-8"
        }
        objectImageSummary.setImage(index, bitmaps[index])
        objectImageSummary.setOnClickListener(index) { ObjectIntent.showSpcSwo(this@SpcSwoSummaryActivity, arrayOf(day, "")) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_pin -> UtilityShortcut.create(this, ShortcutType.SPC_SWO_SUMMARY)
            R.id.action_share -> UtilityShare.text(this, this, "Convective Outlook Summary", "", bitmaps)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

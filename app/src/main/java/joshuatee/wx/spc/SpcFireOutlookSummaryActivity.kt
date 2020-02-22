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
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.ui.ObjectLinearLayout
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.UtilityShare
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_linear_layout_bottom_toolbar.*

class SpcFireOutlookSummaryActivity : BaseActivity(), Toolbar.OnMenuItemClickListener {

    //
    // SPC Fire Weather Outlooks
    //

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var bitmaps = mutableListOf<Bitmap>()
    private var imagesPerRow = 2

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
                savedInstanceState,
                R.layout.activity_linear_layout_bottom_toolbar,
                R.menu.shared_multigraphics,
                true
        )
        if (UtilityUI.isLandScape(this)) {
            imagesPerRow = 3
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        toolbar.subtitle = "SPC"
        title = "Fire Weather Outlooks"
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        bitmaps = mutableListOf()
        withContext(Dispatchers.IO) {
            UtilitySpcFireOutlook.imageUrls.mapTo(bitmaps) {
                it.getImage()
            }
        }
        ll.removeAllViews()
        var numberOfImages = 0
        val horizontalLinearLayouts: MutableList<ObjectLinearLayout> = mutableListOf()
        bitmaps.forEachIndexed { index, bitmap ->
            val objectCardImage: ObjectCardImage
            if (numberOfImages % imagesPerRow == 0) {
                val objectLinearLayout = ObjectLinearLayout(this@SpcFireOutlookSummaryActivity, ll)
                objectLinearLayout.linearLayout.orientation = LinearLayout.HORIZONTAL
                horizontalLinearLayouts.add(objectLinearLayout)
                objectCardImage = ObjectCardImage(
                        this@SpcFireOutlookSummaryActivity,
                        objectLinearLayout.linearLayout,
                        bitmap,
                        imagesPerRow
                )
            } else {
                objectCardImage = ObjectCardImage(
                        this@SpcFireOutlookSummaryActivity,
                        horizontalLinearLayouts.last().linearLayout,
                        bitmap,
                        imagesPerRow
                )
            }
            objectCardImage.setOnClickListener(View.OnClickListener {
                ObjectIntent(
                        this@SpcFireOutlookSummaryActivity,
                        SpcFireOutlookActivity::class.java,
                        SpcFireOutlookActivity.NUMBER,
                        arrayOf(UtilitySpcFireOutlook.textProducts[index], UtilitySpcFireOutlook.imageUrls[index])
                )
            })
            numberOfImages += 1
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareText(
                    this,
                    this,
                    getString(UtilitySpcFireOutlook.activityTitle),
                    "",
                    bitmaps
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

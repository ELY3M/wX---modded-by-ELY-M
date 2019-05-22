/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar

import joshuatee.wx.R
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.UtilityShortcut
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_linear_layout_bottom_toolbar.*

class SPCSWOSummaryActivity : BaseActivity(), Toolbar.OnMenuItemClickListener {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private val bitmaps = mutableListOf<Bitmap>()
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
                savedInstanceState,
                R.layout.activity_linear_layout_bottom_toolbar,
                R.menu.spc_swo_summary,
                true
        )
        toolbarBottom.setOnMenuItemClickListener(this)
        val menu = toolbarBottom.menu
        UtilityShortcut.hidePinIfNeeded(menu)
        contextg = this
        title = "SPC"
        toolbar.subtitle = "Convective Outlook Summary"
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        withContext(Dispatchers.IO) {
            arrayOf("1", "2", "3", "4-8").forEach {
                bitmaps.addAll(
                        UtilitySpcSwo.getImageUrls(it, false)
                )
            }
        }
        bitmaps.forEach { bitmap ->
            val card = ObjectCardImage(contextg, ll, bitmap)
            val day = if (bitmaps.indexOf(bitmap) < 3) {
                (bitmaps.indexOf(bitmap) + 1).toString()
            } else {
                "4-8"
            }
            card.setOnClickListener(View.OnClickListener {
                ObjectIntent(
                        contextg,
                        SPCSWOActivity::class.java,
                        SPCSWOActivity.NO,
                        arrayOf(day, "")
                )
            })
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_pin -> UtilityShortcut.createShortcut(this, ShortcutType.SPC_SWO_SUMMARY)
            R.id.action_share -> UtilityShare.shareText(
                    this,
                    "Convective Outlook Summary",
                    "",
                    bitmaps
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

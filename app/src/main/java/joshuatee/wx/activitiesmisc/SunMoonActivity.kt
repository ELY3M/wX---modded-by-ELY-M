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

package joshuatee.wx.activitiesmisc

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener

import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.MyApplication
import joshuatee.wx.settings.Location
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_linear_layout_bottom_toolbar.*

class SunMoonActivity : AudioPlayActivity(), OnMenuItemClickListener {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var content = ""
    private var contentFull = ""
    private lateinit var objectCardText: ObjectCardText
    private var data = listOf("", "")

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_linear_layout_bottom_toolbar,
            R.menu.shared_tts
        )
        toolbarBottom.setOnMenuItemClickListener(this)
        val menu = toolbarBottom.menu
        menu.findItem(R.id.action_playlist).isVisible = false
        objectCardText = ObjectCardText(this, ll, toolbar, toolbarBottom)
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        withContext(Dispatchers.IO) {
            content = UtilitySunMoon.getExtendedData(Location.locationIndex)
            contentFull = UtilitySunMoon.getFullDates()
        }
        data = UtilitySunMoon.parseData(content)
        title = data[0]
        toolbar.subtitle = Location.name
        objectCardText.setText(data[1] + MyApplication.newline + MyApplication.newline + contentFull)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, data[1], "sunmoon", "sunmoon")) return true
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareText(this, data[0], data[1])
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

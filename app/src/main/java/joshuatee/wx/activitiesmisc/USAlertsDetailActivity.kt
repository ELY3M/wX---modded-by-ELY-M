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

package joshuatee.wx.activitiesmisc

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener

import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.ui.ObjectAlertDetail
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityShare
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_usalertsdetail.*

class USAlertsDetailActivity : AudioPlayActivity(), OnMenuItemClickListener {

    companion object {
        const val URL: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var activityArguments: Array<String>
    private var capAlert = CapAlert()
    private lateinit var objectAlertDetail: ObjectAlertDetail

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_usalertsdetail, R.menu.shared_tts)
        title = ""
        ObjectCard(this, R.id.cardView)
        val menu = toolbarBottom.menu
        val tts = menu.findItem(R.id.action_playlist)
        tts.isVisible = false
        toolbarBottom.setOnMenuItemClickListener(this)
        objectAlertDetail = ObjectAlertDetail(this, linearLayout)
        activityArguments = intent.getStringArrayExtra(URL)!!
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        capAlert = withContext(Dispatchers.IO) {
            CapAlert.createFromUrl(activityArguments[0])
        }
        objectAlertDetail.updateContent(capAlert, activityArguments[0])
        toolbar.subtitle = objectAlertDetail.wfoTitle
        title = objectAlertDetail.title
        UtilityTts.conditionalPlay(
                activityArguments,
                1,
                applicationContext,
                Utility.fromHtml(capAlert.text),
                "alert"
        )
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, capAlert.text, "alert", "alert")) {
            return true
        }
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareText(
                    this,
                    capAlert.title + " " + capAlert.area,
                    Utility.fromHtml(capAlert.text)
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
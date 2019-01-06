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
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener

import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.audio.UtilityTTS
import joshuatee.wx.ui.ObjectAlertDetail
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityShare
import kotlinx.coroutines.*

class USAlertsDetailActivity : AudioPlayActivity(), OnMenuItemClickListener {

    companion object {
        const val URL: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var activityArguments: Array<String>
    private var ca = CAPAlert()
    private lateinit var objAlerts: ObjectAlertDetail

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_usalertsdetail, R.menu.shared_tts)
        title = ""
        ObjectCard(this, R.id.cv1)
        val m = toolbarBottom.menu
        val tts = m.findItem(R.id.action_playlist)
        tts.isVisible = false
        toolbarBottom.setOnMenuItemClickListener(this)
        val linearLayout: LinearLayout = findViewById(R.id.ll)
        objAlerts = ObjectAlertDetail(this, linearLayout)
        activityArguments = intent.getStringArrayExtra(URL)
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        ca = withContext(Dispatchers.IO) { CAPAlert.createFromURL(activityArguments[0]) }
        objAlerts.updateContent(ca, activityArguments[0])
        toolbar.subtitle = ca.area
        title = objAlerts.title
        UtilityTTS.conditionalPlay(
            activityArguments,
            1,
            applicationContext,
            Utility.fromHtml(ca.text),
            "alert"
        )
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, ca.text, "alert", "alert")) {
            return true
        }
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareText(
                this,
                ca.title + " " + ca.area,
                Utility.fromHtml(ca.text)
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
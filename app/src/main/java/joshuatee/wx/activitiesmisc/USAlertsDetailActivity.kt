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

package joshuatee.wx.activitiesmisc

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.ui.ObjectAlertDetail
import joshuatee.wx.ui.Card
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityShare

class USAlertsDetailActivity : AudioPlayActivity(), OnMenuItemClickListener {

    //
    // Displayed detailed information on severe weather alert
    //
    // Arguments:
    // 1: url
    //

    companion object { const val URL = "" }

    private var alertUrl = ""
    private var capAlert = CapAlert()
    private lateinit var objectAlertDetail: ObjectAlertDetail
    private lateinit var box: VBox
    private lateinit var arguments: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_usalertsdetail, R.menu.usalerts_detail)
        arguments = intent.getStringArrayExtra(URL)!!
        alertUrl = arguments[0]
        box = VBox.fromResource(this)
        Card(this, R.id.cardView)
        toolbarBottom.menu.findItem(R.id.action_playlist).isVisible = false
        toolbarBottom.setOnMenuItemClickListener(this)
        objectAlertDetail = ObjectAlertDetail(this, box)
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        FutureVoid(this, { capAlert = CapAlert.createFromUrl(alertUrl) }, ::update)
    }

    private fun update() {
        if (capAlert.getClosestRadar() == "") {
            toolbarBottom.menu.findItem(R.id.action_radar).isVisible = false
        }
        objectAlertDetail.updateContent(capAlert, alertUrl)
        setTitle(objectAlertDetail.title, objectAlertDetail.wfoTitle)
        UtilityTts.conditionalPlay(arguments, 1, applicationContext, Utility.fromHtml(capAlert.text), "alert")
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, capAlert.text, "alert", "alert")) {
            return true
        }
        when (item.itemId) {
            R.id.action_share -> UtilityShare.text(this, capAlert.title + " " + capAlert.area, capAlert.text)
            R.id.action_radar -> Route.radarBySite(this, capAlert.getClosestRadar())
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

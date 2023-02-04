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

package joshuatee.wx.canada

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.widget.ScrollView
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.R
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.CanadaWarnings
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.Utility

class CanadaAlertsActivity : BaseActivity(), Toolbar.OnMenuItemClickListener {

    private var firstTime = true
    private lateinit var canadaWarnings: CanadaWarnings
    private lateinit var scrollView: ScrollView
    private lateinit var box: VBox

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_bottom_toolbar, R.menu.caalerts, true)
        scrollView = findViewById(R.id.scrollView)
        box = VBox.fromResource(this)
        toolbarBottom.setOnMenuItemClickListener(this)
        canadaWarnings = CanadaWarnings(this, box, toolbar)
        canadaWarnings.province = Utility.readPref(this, "CA_ALERTS_PROV", canadaWarnings.province)
        title = "Canada Alerts"
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        scrollView.smoothScrollTo(0, 0)
        FutureVoid(this, canadaWarnings::getData, ::showText)
    }

    private fun showText() {
        canadaWarnings.showData()
        if (firstTime) {
            UtilityToolbar.fullScreenMode(toolbar)
            firstTime = false
        }
        Utility.writePref(this@CanadaAlertsActivity, "CA_ALERTS_PROV", canadaWarnings.province)
        toolbar.subtitle = canadaWarnings.title
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_ca -> canadaWarnings.province = "ca"
            R.id.action_ab -> canadaWarnings.province = "ab"
            R.id.action_bc -> canadaWarnings.province = "bc"
            R.id.action_mb -> canadaWarnings.province = "mb"
            R.id.action_nb -> canadaWarnings.province = "nb"
            R.id.action_nl -> canadaWarnings.province = "nl"
            R.id.action_ns -> canadaWarnings.province = "ns"
            R.id.action_nt -> canadaWarnings.province = "nt"
            R.id.action_nu -> canadaWarnings.province = "nt"
            R.id.action_son -> canadaWarnings.province = "son"
            R.id.action_non -> canadaWarnings.province = "non"
            R.id.action_pei -> canadaWarnings.province = "pei"
            R.id.action_sqc -> canadaWarnings.province = "sqc"
            R.id.action_nqc -> canadaWarnings.province = "nqc"
            R.id.action_sk -> canadaWarnings.province = "sk"
            R.id.action_yt -> canadaWarnings.province = "yt"
            else -> return super.onOptionsItemSelected(item)
        }
        getContent()
        return true
    }
}

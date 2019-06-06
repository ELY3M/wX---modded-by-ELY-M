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


package joshuatee.wx.canada

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar

import joshuatee.wx.R
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCAWarn
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.Utility
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_linear_layout_bottom_toolbar.*

class CanadaAlertsActivity : BaseActivity(), Toolbar.OnMenuItemClickListener {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var firstTime = true
    private lateinit var objectCAWarn: ObjectCAWarn

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_linear_layout_bottom_toolbar,
            R.menu.caalerts,
            true
        )
        toolbarBottom.setOnMenuItemClickListener(this)
        objectCAWarn = ObjectCAWarn(this, this, ll, toolbar)
        objectCAWarn.prov = Utility.readPref(this, "CA_ALERTS_PROV", objectCAWarn.prov)
        title = "Canada Alerts"
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        sv.smoothScrollTo(0, 0)
        withContext(Dispatchers.IO) { objectCAWarn.getData() }
        objectCAWarn.showData()
        if (firstTime) {
            UtilityToolbar.fullScreenMode(toolbar)
            firstTime = false
        }
        Utility.writePref(this@CanadaAlertsActivity, "CA_ALERTS_PROV", objectCAWarn.prov)
        toolbar.subtitle = objectCAWarn.title
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_ca -> objectCAWarn.prov = "ca"
            R.id.action_ab -> objectCAWarn.prov = "ab"
            R.id.action_bc -> objectCAWarn.prov = "bc"
            R.id.action_mb -> objectCAWarn.prov = "mb"
            R.id.action_nb -> objectCAWarn.prov = "nb"
            R.id.action_nl -> objectCAWarn.prov = "nl"
            R.id.action_ns -> objectCAWarn.prov = "ns"
            R.id.action_nt -> objectCAWarn.prov = "nt"
            R.id.action_nu -> objectCAWarn.prov = "nt"
            R.id.action_son -> objectCAWarn.prov = "son"
            R.id.action_non -> objectCAWarn.prov = "non"
            R.id.action_pei -> objectCAWarn.prov = "pei"
            R.id.action_sqc -> objectCAWarn.prov = "sqc"
            R.id.action_nqc -> objectCAWarn.prov = "nqc"
            R.id.action_sk -> objectCAWarn.prov = "sk"
            R.id.action_yt -> objectCAWarn.prov = "yt"
            else -> return super.onOptionsItemSelected(item)
        }
        getContent()
        return true
    }
}

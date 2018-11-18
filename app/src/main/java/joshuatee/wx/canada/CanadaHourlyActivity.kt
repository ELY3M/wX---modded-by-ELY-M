/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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
import android.view.View
import android.widget.LinearLayout

import joshuatee.wx.R
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCALegal
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.UtilityToolbar
import kotlinx.coroutines.*

class CanadaHourlyActivity : BaseActivity() {

    companion object {
        const val LOC_NUM: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var locNumInt = 0
    private lateinit var c0: ObjectCardText

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        val locNum = intent.getStringExtra(LOC_NUM)
        locNumInt = (locNum.toIntOrNull() ?: 0) - 1
        val linearLayout: LinearLayout = findViewById(R.id.ll)
        c0 = ObjectCardText(this)
        c0.setOnClickListener(View.OnClickListener { UtilityToolbar.showHide(toolbar) })
        linearLayout.addView(c0.card)
        linearLayout.addView(ObjectCALegal(this, UtilityCanadaHourly.getHourlyURL(Location.locationIndex)).card)
        title = Location.getName(locNumInt) + " hourly forecast"
        getContent()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        val html = withContext(Dispatchers.IO) { UtilityCanadaHourly.getHourlyString(locNumInt) }
        c0.setText(html)
    }
}

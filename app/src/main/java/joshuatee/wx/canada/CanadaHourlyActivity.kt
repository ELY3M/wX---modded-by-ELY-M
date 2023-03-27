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
import joshuatee.wx.R
import joshuatee.wx.objects.FutureText2
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.CanadaLegal
import joshuatee.wx.ui.CardText
import joshuatee.wx.ui.VBox

class CanadaHourlyActivity : BaseActivity() {

    companion object { const val LOC_NUM = "" }

    private var locationNumber = 0
    private lateinit var text: CardText
    private lateinit var box: VBox

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        box = VBox.fromResource(this)
        locationNumber = (intent.getStringExtra(LOC_NUM)!!.toIntOrNull() ?: 0) - 1
        text = CardText(this, toolbar)
        box.addWidget(text)
        CanadaLegal(this, box, UtilityCanadaHourly.getUrl(Location.locationIndex))
        title = Location.getName(locationNumber) + " hourly forecast"
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        text.typefaceMono()
        FutureText2(this, { UtilityCanadaHourly.getString(locationNumber) }, text::setText1)
    }
}

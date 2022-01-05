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

package joshuatee.wx.canada

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.LinearLayout
import joshuatee.wx.R
import joshuatee.wx.objects.FutureText2
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCALegal
import joshuatee.wx.ui.ObjectCardText

class CanadaHourlyActivity : BaseActivity() {

    companion object { const val LOC_NUM = "" }

    private var locationNumber = 0
    private lateinit var objectCardText: ObjectCardText
    private lateinit var linearLayout: LinearLayout

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        linearLayout = findViewById(R.id.linearLayout)
        locationNumber = (intent.getStringExtra(LOC_NUM)!!.toIntOrNull() ?: 0) - 1
        objectCardText = ObjectCardText(this, linearLayout, toolbar)
        ObjectCALegal(this, linearLayout, UtilityCanadaHourly.getUrl(Location.locationIndex))
        title = Location.getName(locationNumber) + " hourly forecast"
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        objectCardText.typefaceMono()
        FutureText2(this, { UtilityCanadaHourly.getString(locationNumber) }, objectCardText::setText1)
    }
}

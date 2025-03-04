/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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

package joshuatee.wx.ui

import android.content.Context
import android.widget.ScrollView
import joshuatee.wx.objects.LatLon
import joshuatee.wx.safeGet
import joshuatee.wx.util.SevenDay

class SevenDayCollection(
    val context: Context,
    private val boxForecast: VBox,
    val scrollView: ScrollView
) {

    fun update(sevenDay: SevenDay, latLon: LatLon, showSunriseCard: Boolean) {
        boxForecast.removeChildren()
        sevenDay.icons.forEachIndexed { index, iconUrl ->
            val cardSevenDay =
                CardSevenDay(context, iconUrl, true, sevenDay.forecastList.safeGet(index))
            cardSevenDay.connect { scrollView.smoothScrollTo(0, 0) }
            boxForecast.addWidget(cardSevenDay)
        }
        if (showSunriseCard) {
            boxForecast.addWidget(SunRiseCard(context, latLon, scrollView))
        }
    }
}

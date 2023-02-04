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

package joshuatee.wx.settings

import android.os.Bundle
import joshuatee.wx.R
import joshuatee.wx.objects.Route
import joshuatee.wx.radar.NexradUtil
import joshuatee.wx.radarcolorpalettes.ColorPalette
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.CardText
import joshuatee.wx.ui.VBox

class SettingsColorPaletteListingActivity : BaseActivity() {

    private var cardColorPalettes = mutableListOf<CardText>()
    private lateinit var box: VBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        box = VBox.fromResource(this)
        setupUI()
    }

    private fun setupUI() {
        NexradUtil.colorPaletteProducts.filter { it != 165 }.forEach { product ->
            val card = CardText(this,
                    NexradUtil.productCodeStringToName[product] + ": " + ColorPalette.radarColorPalette[product],
                    UIPreferences.textSizeNormal,
                    { Route(this, SettingsColorPaletteActivity::class.java, SettingsColorPaletteActivity.TYPE, arrayOf(product.toString())) },
                    UIPreferences.paddingSettings
            )
            box.addWidget(card)
            cardColorPalettes.add(card)
        }
    }

    override fun onRestart() {
        cardColorPalettes.indices.forEach {
            val product = NexradUtil.productCodeStringToName[NexradUtil.colorPaletteProducts[it]] ?: "Reflectivity"
            val label = product + ": " + ColorPalette.radarColorPalette[NexradUtil.colorPaletteProducts[it]]
            cardColorPalettes[it].text = label
        }
        super.onRestart()
    }
}

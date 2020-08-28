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

package joshuatee.wx.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View

import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.WXGLNexrad
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCardText

import kotlinx.android.synthetic.main.activity_linear_layout.*

class SettingsColorPaletteListingActivity : BaseActivity() {

    private var cardColorPalettes = mutableListOf<ObjectCardText>()

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        WXGLNexrad.colorPaletteProducts.forEach { product ->
            val card = ObjectCardText(
                    this,
                    linearLayout,
                    WXGLNexrad.productCodeStringToName[product] + ": " + MyApplication.radarColorPalette[product],
                    MyApplication.textSizeNormal,
                    MyApplication.paddingSettings
            )
            card.setOnClickListener(View.OnClickListener {
                ObjectIntent(this, SettingsColorPaletteActivity::class.java, SettingsColorPaletteActivity.TYPE, arrayOf(product.toString()))
            })
            cardColorPalettes.add(card)
        }
    }

    override fun onRestart() {
        cardColorPalettes.indices.forEach {
            val product = WXGLNexrad.productCodeStringToName[WXGLNexrad.colorPaletteProducts[it]] ?: "Reflectivity"
            val label = product + ": " + MyApplication.radarColorPalette[WXGLNexrad.colorPaletteProducts[it]]
            cardColorPalettes[it].text = label
        }
        super.onRestart()
    }
}

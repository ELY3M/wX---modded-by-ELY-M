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
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.Extensions.getHtmlSep
import joshuatee.wx.Extensions.parse
import joshuatee.wx.Extensions.safeGet
import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.ui.CanadaLegal
import joshuatee.wx.ui.CardText
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.Utility
import joshuatee.wx.util.DownloadText
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.UtilityString
import joshuatee.wx.wpc.UtilityWpcText

class CanadaTextActivity : AudioPlayActivity(), OnMenuItemClickListener {

    private var product = "focn45"
    private var html = ""
    private lateinit var cardText: CardText
    private lateinit var scrollView: ScrollView
    private lateinit var box: VBox

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout_bottom_toolbar, R.menu.canada_text)
        scrollView = findViewById(R.id.scrollView)
        box = VBox.fromResource(this)
        toolbarBottom.setOnMenuItemClickListener(this)
        cardText = CardText(this, toolbar, toolbarBottom)
        box.addWidget(cardText)
        CanadaLegal(this, box, GlobalVariables.canadaEcSitePrefix)
        product = Utility.readPref(this, "CA_TEXT_LASTUSED", product)
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        title = product
        toolbar.subtitle = getDescription()
        scrollView.smoothScrollTo(0, 0)
        FutureVoid(this, ::download, ::update)
    }

    private fun download() {
        html = if (product != "https://weather.gc.ca/forecast/public_bulletins_e.html?Bulletin=fpcn48.cwao") {
            DownloadText.byProduct(this, product)
        } else {
//            UtilityString.getHtmlAndParseSep(product, "<pre>(.*?)</pre>")
            product.getHtmlSep().parse("<pre>(.*?)</pre>")
        }
    }

    private fun update() {
        cardText.setTextAndTranslate(html)
        Utility.writePref(this, "CA_TEXT_LASTUSED", product)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, html, product, product)) {
            return true
        }
        when (item.itemId) {
            R.id.action_share -> {
                UtilityShare.text(this, getDescription(), Utility.fromHtml(html))
                return true
            }
            R.id.action_focn45 -> setProdAndDescription("focn45")
            R.id.action_fxcn01_d13_west -> setProdAndDescription("fxcn01_d1-3_west")
            R.id.action_fxcn01_d47_west -> setProdAndDescription("fxcn01_d4-7_west")
            R.id.action_fxcn01_d13_east -> setProdAndDescription("fxcn01_d1-3_east")
            R.id.action_fxcn01_d47_east -> setProdAndDescription("fxcn01_d4-7_east")
            R.id.action_uv -> setProdAndDescription("fpcn48")
            R.id.action_s_mb -> setProdAndDescription("awcn11")
            R.id.action_n_mb -> setProdAndDescription("awcn12")
            R.id.action_s_sk -> setProdAndDescription("awcn13")
            R.id.action_n_sk -> setProdAndDescription("awcn14")
            R.id.action_s_ab -> setProdAndDescription("awcn15")
            R.id.action_n_ab -> setProdAndDescription("awcn16")
            else -> return super.onOptionsItemSelected(item)
        }
        getContent()
        return true
    }

    private fun setProdAndDescription(product: String) {
        this.product = product
    }

    private fun getDescription(): String {
        val fullString = UtilityWpcText.labels.filter { it.startsWith("$product:") }.safeGet(0)
        return fullString.split(":").safeGet(1)
    }
}

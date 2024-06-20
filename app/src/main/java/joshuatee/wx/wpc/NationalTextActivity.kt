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

package joshuatee.wx.wpc

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ScrollView
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.safeGet
import java.util.Locale
import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.notifications.NotificationTextProduct
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.FavoriteType
import joshuatee.wx.objects.FutureText
import joshuatee.wx.objects.Route
import joshuatee.wx.ui.CardText
import joshuatee.wx.ui.NavDrawerCombo
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityFavorites
import joshuatee.wx.util.UtilityShare

class NationalTextActivity : AudioPlayActivity(), OnMenuItemClickListener {

    //
    // Display WPC ( and other ) text products
    // last used product is first shown on re-entry
    //
    // Arguments
    // 1: text product (lower case)
    // 2: title (not used?)
    // 3: if "sound" will play TTS on first load
    //

    companion object {
        const val URL = ""
    }

    private lateinit var arguments: Array<String>
    private var product = ""
    private var initialProduct = ""
    private var products = listOf<String>()
    private lateinit var star: MenuItem
    private lateinit var notificationToggle: MenuItem
    private var ridFavOld = ""
    private lateinit var cardText: CardText
    private lateinit var navDrawerCombo: NavDrawerCombo
    private lateinit var scrollView: ScrollView
    private lateinit var box: VBox

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.wpctext_products_top, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_product).title = products.safeGet(0)
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_wpctextproducts, R.menu.wpctext_products)
        arguments = intent.getStringArrayExtra(URL)!!
        if (arguments[0] == "pmdspd") {
            product = UIPreferences.wpcTextFav
        } else {
            product = arguments[0]
            initialProduct = product
        }
        setupUI()
        getContent()
    }

    private fun setupUI() {
        scrollView = findViewById(R.id.scrollView)
        box = VBox.fromResource(this)
        objectToolbarBottom.connect(this)
        star = objectToolbarBottom.getFavIcon()
        notificationToggle = objectToolbarBottom.find(R.id.action_notif_text_prod)
        cardText = CardText(this, toolbar, toolbarBottom)
        box.addWidget(cardText)
        UtilityWpcText.create()
        navDrawerCombo = NavDrawerCombo(this, UtilityWpcText.groups, UtilityWpcText.longCodes, UtilityWpcText.shortCodes, "")
        navDrawerCombo.connect(::changeProduct)
    }

    private fun getContent() {
        products = UtilityFavorites.setupMenu(this, product, FavoriteType.NWS_TEXT)
        invalidateOptionsMenu()
        updateSubmenuNotificationText()
        scrollView.smoothScrollTo(0, 0)
        if (UIPreferences.favorites[FavoriteType.NWS_TEXT]!!.contains(":$product:")) {
            star.setIcon(GlobalVariables.STAR_ICON)
        } else {
            star.setIcon(GlobalVariables.STAR_OUTLINE_ICON)
        }
        ridFavOld = UIPreferences.favorites[FavoriteType.NWS_TEXT]!!
        FutureText(this, product, ::showText)
    }

    private fun showText(html: String) {
        if (html.contains("<BR><BR>")) {
            cardText.setTextAndTranslate(Utility.fromHtml(html))
        } else {
            cardText.setTextAndTranslate(html)
        }
        if (UtilityWpcText.needsFixedWidthFont(product.uppercase(Locale.US))) {
            cardText.typefaceMono()
        } else {
            cardText.typefaceDefault()
        }
        UtilityTts.conditionalPlay(arguments, 2, applicationContext, html, "wpctext")
        if (initialProduct != product) {
            Utility.writePref(this, "WPC_TEXT_FAV", product)
            UIPreferences.wpcTextFav = product
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val textToShare = UtilityShare.prepTextForShare(cardText.text)
        if (audioPlayMenu(item.itemId, cardText.text, product, product)) {
            return true
        }
        when (item.itemId) {
            R.id.action_fav -> toggleFavorite()
            R.id.action_notif_text_prod -> {
                NotificationTextProduct.toggle(this, box.get(), product.uppercase(Locale.US))
                updateSubmenuNotificationText()
            }

            R.id.action_share -> UtilityShare.text(this, product, textToShare)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (navDrawerCombo.onOptionsItemSelected(item)) {
            return true
        }
        products = UtilityFavorites.setupMenu(this, product, FavoriteType.NWS_TEXT)
        when (item.itemId) {
            R.id.action_product -> {
                ObjectDialogue.generic(this, products, ::getContent) {
                    when (it) {
                        1 -> Route.favoriteAdd(this, FavoriteType.NWS_TEXT)
                        2 -> Route.favoriteRemove(this, FavoriteType.NWS_TEXT)
                        else -> {
                            product = products[it].split(" ").getOrNull(0) ?: ""
                            getContent()
                        }
                    }
                }
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun toggleFavorite() {
        UtilityFavorites.toggle(this, product, star, FavoriteType.NWS_TEXT)
    }

    private fun changeProduct() {
        product = navDrawerCombo.getUrl()
        getContent()
    }

    private fun updateSubmenuNotificationText() {
        if (NotificationTextProduct.check(product.uppercase(Locale.US))) {
            notificationToggle.title = resources.getString(R.string.notif_remove)
        } else {
            notificationToggle.title = resources.getString(R.string.notif_add)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        navDrawerCombo.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        navDrawerCombo.onConfigurationChanged(newConfig)
    }
}

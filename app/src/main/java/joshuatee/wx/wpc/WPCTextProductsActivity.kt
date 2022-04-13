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

package joshuatee.wx.wpc

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.Extensions.safeGet
import java.util.Locale
import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.notifications.UtilityNotificationTextProduct
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.*
import joshuatee.wx.util.*

class WpcTextProductsActivity : AudioPlayActivity(), OnMenuItemClickListener {

    //
    // Display WPC ( and other ) text products
    // last used product is first shown on re-entry
    //
    // Arguments
    // 1: text product (lower case)
    //

    companion object { const val URL = "" }

    private lateinit var activityArguments: Array<String>
    private var product = ""
    private var html = ""
    private var initialProduct = ""
    private var products = listOf<String>()
    private lateinit var star: MenuItem
    private lateinit var notificationToggle: MenuItem
    private var ridFavOld = ""
    private lateinit var textCard: ObjectCardText
    private lateinit var drw: ObjectNavDrawerCombo
    private lateinit var scrollView: ScrollView
    private lateinit var linearLayout: LinearLayout
    private val prefToken = "NWS_TEXT_FAV"

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
        scrollView = findViewById(R.id.scrollView)
        linearLayout = findViewById(R.id.linearLayout)
        toolbarBottom.setOnMenuItemClickListener(this)
        star = toolbarBottom.menu.findItem(R.id.action_fav)
        notificationToggle = toolbarBottom.menu.findItem(R.id.action_notif_text_prod)
        activityArguments = intent.getStringArrayExtra(URL)!!
        if (activityArguments[0] == "pmdspd") {
            product = MyApplication.wpcTextFav
        } else {
            product = activityArguments[0]
            initialProduct = product
        }
        textCard = ObjectCardText(this, linearLayout, toolbar, toolbarBottom)
        UtilityWpcText.create()
        drw = ObjectNavDrawerCombo(this, UtilityWpcText.groups, UtilityWpcText.longCodes, UtilityWpcText.shortCodes, this, "")
        drw.setListener(::changeProduct)
        getContent()
    }

    private fun getContent() {
        products = UtilityFavorites.setupMenu(this@WpcTextProductsActivity, MyApplication.nwsTextFav, product, prefToken)
        invalidateOptionsMenu()
        updateSubmenuNotificationText()
        scrollView.smoothScrollTo(0, 0)
        if (MyApplication.nwsTextFav.contains(":$product:")) {
            star.setIcon(MyApplication.STAR_ICON)
        } else {
            star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        }
        ridFavOld = MyApplication.nwsTextFav
        FutureVoid(this, { html = UtilityDownload.getTextProduct(this@WpcTextProductsActivity, product) }, ::showText)
    }

    private fun showText() {
        textCard.setTextAndTranslate(html)
        if (UtilityWpcText.needsFixedWidthFont(product.uppercase(Locale.US))) {
            textCard.typefaceMono()
        } else {
            textCard.typefaceDefault()
        }
        UtilityTts.conditionalPlay(activityArguments, 2, applicationContext, html, "wpctext")
        if (initialProduct != product) {
            Utility.writePref(this@WpcTextProductsActivity, "WPC_TEXT_FAV", product)
            MyApplication.wpcTextFav = product
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val textToShare = UtilityShare.prepTextForShare(html)
        if (audioPlayMenu(item.itemId, html, product, product)) {
            return true
        }
        when (item.itemId) {
            R.id.action_fav -> toggleFavorite()
            R.id.action_notif_text_prod -> {
                UtilityNotificationTextProduct.toggle(this, linearLayout, product.uppercase(Locale.US))
                updateSubmenuNotificationText()
            }
            R.id.action_share -> UtilityShare.text(this, product, textToShare)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        products = UtilityFavorites.setupMenu(this, MyApplication.nwsTextFav, product, prefToken)
        when (item.itemId) {
            R.id.action_product -> {
                genericDialog(products) {
                    when (it) {
                        1 -> ObjectIntent.favoriteAdd(this, arrayOf("NWSTEXT"))
                        2 -> ObjectIntent.favoriteRemove(this, arrayOf("NWSTEXT"))
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

    private fun genericDialog(list: List<String>, fn: (Int) -> Unit) {
        val objectDialogue = ObjectDialogue(this, list)
        objectDialogue.setNegativeButton { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(this)
        }
        objectDialogue.setSingleChoiceItems { dialog, which ->
            fn(which)
            getContent()
            dialog.dismiss()
        }
        objectDialogue.show()
    }

    private fun toggleFavorite() {
        UtilityFavorites.toggle(this, product, star, prefToken)
    }

    private fun changeProduct() {
        product = drw.getUrl()
        getContent()
    }

    private fun updateSubmenuNotificationText() {
        if (UtilityNotificationTextProduct.check(product.uppercase(Locale.US))) {
            notificationToggle.title = resources.getString(R.string.notif_remove)
        } else {
            notificationToggle.title = resources.getString(R.string.notif_add)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drw.actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drw.actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }
}

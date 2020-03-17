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
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener

import java.util.Locale

import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.notifications.UtilityNotificationTextProduct
import joshuatee.wx.settings.FavAddActivity
import joshuatee.wx.settings.FavRemoveActivity
import joshuatee.wx.MyApplication
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.ObjectSpinner

import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.ObjectNavDrawerCombo
import joshuatee.wx.util.*
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_wpctextproducts.*

class WpcTextProductsActivity : AudioPlayActivity(), OnMenuItemClickListener,
        AdapterView.OnItemSelectedListener {

    // Display WPC ( and other ) text products
    // last used product is first shown on re-entry
    //
    // Arguments
    // 1: text product (lower case)
    //

    companion object {
        const val URL: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var activityArguments: Array<String>
    private var product = ""
    private var html = ""
    private var initialProduct = ""
    private var products = listOf<String>()
    private lateinit var star: MenuItem
    private lateinit var notificationToggle: MenuItem
    private var ridFavOld = ""
    private lateinit var textCard: ObjectCardText
    private lateinit var sp: ObjectSpinner
    private lateinit var drw: ObjectNavDrawerCombo

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
                savedInstanceState,
                R.layout.activity_wpctextproducts,
                R.menu.wpctext_products
        )
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
        products = UtilityFavorites.setupMenuNwsText(MyApplication.nwsTextFav, product)
        sp = ObjectSpinner(this, this, this, R.id.spinner1, products)
        UtilityWpcText.createData()
        drw = ObjectNavDrawerCombo(
                this,
                UtilityWpcText.groups,
                UtilityWpcText.longCodes,
                UtilityWpcText.shortCodes,
                this,
                ""
        )
        drw.setListener(::changeProduct)
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        updateSubmenuNotificationText()
        scrollView.smoothScrollTo(0, 0)
        if (MyApplication.nwsTextFav.contains(":$product:")) {
            star.setIcon(MyApplication.STAR_ICON)
        } else {
            star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        }
        ridFavOld = MyApplication.nwsTextFav
        html = withContext(Dispatchers.IO) {
            UtilityDownload.getTextProduct(this@WpcTextProductsActivity, product)
        }
        textCard.setTextAndTranslate(html)
        UtilityTts.conditionalPlay(activityArguments, 2, applicationContext, html, "wpctext")
        if (initialProduct != product) {
            Utility.writePref(this@WpcTextProductsActivity, "WPC_TEXT_FAV", product)
            MyApplication.wpcTextFav = product
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            drw.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, html, product, product)) {
            return true
        }
        when (item.itemId) {
            R.id.action_fav -> toggleFavorite()
            R.id.action_notif_text_prod -> {
                UtilityNotificationTextProduct.toggle(
                        this,
                        linearLayout,
                        product.toUpperCase(Locale.US)
                )
                updateSubmenuNotificationText()
            }
            R.id.action_share -> UtilityShare.shareText(this, product, Utility.fromHtml(html))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
        when (position) {
            1 -> ObjectIntent(
                    this,
                    FavAddActivity::class.java,
                    FavAddActivity.TYPE,
                    arrayOf("NWSTEXT")
            )
            2 -> ObjectIntent(
                    this,
                    FavRemoveActivity::class.java,
                    FavRemoveActivity.TYPE,
                    arrayOf("NWSTEXT")
            )
            else -> {
                product = products[position].split(":").getOrNull(0) ?: ""
                getContent()
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private fun findPosition(key: String) =
            (UtilityWpcText.labels.indices).firstOrNull {
                UtilityWpcText.labels[it].contains(
                        key
                )
            }
                    ?: 0

    override fun onRestart() {
        if (ridFavOld != MyApplication.nwsTextFav) {
            products = UtilityFavorites.setupMenuNwsText(
                    MyApplication.nwsTextFav,
                    UtilityWpcText.labels[findPosition(product)]
            )
            sp.refreshData(this, products)
        }
        super.onRestart()
    }

    private fun toggleFavorite() {
        UtilityFavorites.toggle(this, product, star, "NWS_TEXT_FAV")
        products = UtilityFavorites.setupMenuNwsText(MyApplication.nwsTextFav, product)
        sp.refreshData(this, products)
    }

    private fun changeProduct() {
        product = drw.getUrl()
        products = UtilityFavorites.setupMenuNwsText(
                MyApplication.nwsTextFav,
                UtilityWpcText.labels[findPosition(product)]
        )
        sp.refreshData(this, products)
    }

    private fun updateSubmenuNotificationText() {
        if (UtilityNotificationTextProduct.check(product.toUpperCase(Locale.US))) {
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


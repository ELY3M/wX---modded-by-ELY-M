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

package joshuatee.wx.wpc

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener

import java.util.Locale

import joshuatee.wx.R
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.audio.UtilityTTS
import joshuatee.wx.notifications.UtilityNotificationTextProduct
import joshuatee.wx.settings.FavAddActivity
import joshuatee.wx.settings.FavRemoveActivity
import joshuatee.wx.activitiesmisc.ImageShowActivity
import joshuatee.wx.MyApplication
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityFavorites
import joshuatee.wx.util.UtilityShare

import joshuatee.wx.NWS_TXT_ARR
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.ObjectNavDrawerCombo
import kotlinx.coroutines.*

class WPCTextProductsActivity : AudioPlayActivity(), OnMenuItemClickListener, AdapterView.OnItemSelectedListener {

    // Display WPC ( and other ) text products
    // last used product is first shown on re-entry
    //
    // Arguments
    // 1: text produt ( lower case )
    //

    companion object {
        const val URL: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private lateinit var turl: Array<String>
    private var prod = ""
    private var html = ""
    private lateinit var sv: ScrollView
    private var initProd = ""
    private var prodListArr = listOf<String>()
    private lateinit var star: MenuItem
    private lateinit var notifToggle: MenuItem
    private var ridFavOld = ""
    private lateinit var c0: ObjectCardText
    private lateinit var sp: ObjectSpinner
    private lateinit var drw: ObjectNavDrawerCombo
    private lateinit var ll: LinearLayout
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_wpctextproducts, R.menu.wpctext_products)
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        star = toolbarBottom.menu.findItem(R.id.action_fav)
        notifToggle = toolbarBottom.menu.findItem(R.id.action_notif_text_prod)
        turl = intent.getStringArrayExtra(URL)
        if (turl[0] == "pmdspd") {
            prod = MyApplication.wpcTextFav
        } else {
            prod = turl[0]
            initProd = prod
        }
        sv = findViewById(R.id.sv)
        ll = findViewById(R.id.ll)
        c0 = ObjectCardText(this)
        ll.addView(c0.card)
        c0.setOnClickListener(View.OnClickListener { UtilityToolbar.showHide(toolbar, toolbarBottom) })
        prodListArr = UtilityFavorites.setupFavMenuNWSTEXT(MyApplication.nwsTextFav, prod)
        sp = ObjectSpinner(this, this, R.id.spinner1, prodListArr)
        sp.setOnItemSelectedListener(this)
        UtilityWPCText.createData()
        drw = ObjectNavDrawerCombo(this, UtilityWPCText.GROUPS, UtilityWPCText.LONG_CODES, UtilityWPCText.SHORT_CODES)
        drw.listView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            drw.drawerLayout.closeDrawer(drw.listView)
            prod = drw.getToken(groupPosition, childPosition)
            changeProduct()
            true
        }

    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {

        updateSubmenuNotifText()
        sv.smoothScrollTo(0, 0)
        if (MyApplication.nwsTextFav.contains(":$prod:")) {
            star.setIcon(MyApplication.STAR_ICON)
        } else {
            star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        }
        ridFavOld = MyApplication.nwsTextFav

        html = withContext(Dispatchers.IO) { UtilityDownload.getTextProduct(contextg, prod) }

        c0.setTextAndTranslate(Utility.fromHtml(html))
        if (turl.size > 2) {
            if (turl[2] == "sound") {
                UtilityTTS.synthesizeTextAndPlay(applicationContext, html, "wpctext")
            }
        }
        if (initProd != prod) {
            Utility.writePref(contextg, "WPC_TEXT_FAV", prod)
            MyApplication.wpcTextFav = prod
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = drw.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, html, prod, prod)) {
            return true
        }
        when (item.itemId) {
            R.id.action_fav -> toggleFavorite()
            R.id.action_notif_text_prod -> {
                UtilityNotificationTextProduct.toggle(this, ll, prod.toUpperCase(Locale.US))
                updateSubmenuNotifText()
            }
            R.id.action_mpd -> ObjectIntent(this, WPCMPDShowSummaryActivity::class.java)
            R.id.action_qpferf -> ObjectIntent(this, WPCRainfallForecastActivity::class.java)
            R.id.action_low1 -> ObjectIntent(this, ImageShowActivity::class.java, ImageShowActivity.URL, arrayOf("${MyApplication.nwsWPCwebsitePrefix}/wwd/lowtrack_circles.gif", "Significant Surface Low Tracks"))
            R.id.action_low2 -> {
                ObjectIntent(this, ImageShowActivity::class.java, ImageShowActivity.URL, arrayOf("${MyApplication.nwsWPCwebsitePrefix}/lowtracks/lowtrack_ensembles.gif", "Low Tracks and Clusters"))
            }
            R.id.action_share -> UtilityShare.shareText(this, prod, Utility.fromHtml(html))
            else -> return super.onOptionsItemSelected(item)
        }
        changeProduct()
        return true
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        when (pos) {
            1 -> ObjectIntent(this, FavAddActivity::class.java, FavAddActivity.TYPE, arrayOf("NWSTEXT"))
            2 -> ObjectIntent(this, FavRemoveActivity::class.java, FavRemoveActivity.TYPE, arrayOf("NWSTEXT"))
            else -> {
                prod = prodListArr[pos].split(":").getOrNull(0) ?: ""
                getContent()
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private fun findPosition(key: String) = (0 until NWS_TXT_ARR.size).firstOrNull { NWS_TXT_ARR[it].contains(key) }
            ?: 0

    override fun onRestart() {
        if (ridFavOld != MyApplication.nwsTextFav) {
            prodListArr = UtilityFavorites.setupFavMenuNWSTEXT(MyApplication.nwsTextFav, NWS_TXT_ARR[findPosition(prod)])
            sp.refreshData(this, prodListArr)
        }
        super.onRestart()
    }

    private fun toggleFavorite() {
        UtilityFavorites.toggleFavorite(this, prod, star, "NWS_TEXT_FAV")
        prodListArr = UtilityFavorites.setupFavMenuNWSTEXT(MyApplication.nwsTextFav, prod)
        sp.refreshData(this, prodListArr)
    }

    private fun changeProduct() {
        prodListArr = UtilityFavorites.setupFavMenuNWSTEXT(MyApplication.nwsTextFav, NWS_TXT_ARR[findPosition(prod)])
        sp.refreshData(this, prodListArr)
    }

    private fun updateSubmenuNotifText() {
        if (UtilityNotificationTextProduct.check(prod.toUpperCase(Locale.US))) {
            notifToggle.title = resources.getString(R.string.notif_remove)
        } else {
            notifToggle.title = resources.getString(R.string.notif_add)
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


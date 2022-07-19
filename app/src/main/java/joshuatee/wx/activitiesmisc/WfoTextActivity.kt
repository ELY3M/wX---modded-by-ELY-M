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

package joshuatee.wx.activitiesmisc

import android.annotation.SuppressLint
import android.content.res.Configuration
import java.util.Locale
import androidx.cardview.widget.CardView
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import androidx.core.view.GravityCompat
import joshuatee.wx.Extensions.safeGet
import joshuatee.wx.R
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.notifications.UtilityNotificationTextProduct
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.common.RegExp
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.ui.ObjectImageMap
import joshuatee.wx.ui.ObjectCardText
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityShortcut
import joshuatee.wx.util.UtilityFavorites
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImageMap

class WfoTextActivity : AudioPlayActivity(), OnMenuItemClickListener {

    //
    // The primary purpose of this activity is to view AFD from location's NWS office
    // However, other NWS office text products are also available from the AB menu
    // A map icon is also provided to select an office different from your current location
    //
    // Arguments
    // 1: NWS office
    // 2: product
    //

    companion object { const val URL = "" }

    private var firstTime = true
    private lateinit var activityArguments: Array<String>
    private var product = ""
    private var wfo = ""
    private lateinit var imageMap: ObjectImageMap
    private var html = ""
    private var mapShown = false
    private lateinit var notificationToggle: MenuItem
    private lateinit var star: MenuItem
    private lateinit var locationList: List<String>
    private val prefToken = "WFO_FAV"
    private var ridFavOld = ""
    private var version = 1
    private var oldProduct = ""
    private var oldWfo = ""
    private val wfoListPerState = mutableListOf<String>()
    private val cardList = mutableListOf<CardView>()
    private lateinit var objectCardText: ObjectCardText
    private lateinit var drw: ObjectNavDrawer
    private lateinit var scrollView: ScrollView
    private lateinit var linearLayout: LinearLayout
    private var originalWfo = ""
    private val fixedWidthProducts = listOf("RTP", "RWR", "CLI", "RVA")
    private var wfoProd = mutableListOf<String>()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.afd_top, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_sector).title = locationList.safeGet(0)
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_afd, R.menu.afd)
        scrollView = findViewById(R.id.scrollView)
        linearLayout = findViewById(R.id.linearLayout)
        toolbarBottom.setOnMenuItemClickListener(this)
        drw = ObjectNavDrawer(this, UtilityWfoText.labels, UtilityWfoText.codes, ::getContentFixThis)
        UtilityShortcut.hidePinIfNeeded(toolbarBottom)
        objectCardText = ObjectCardText(this, linearLayout, toolbar, toolbarBottom)
        star = toolbarBottom.menu.findItem(R.id.action_fav)
        notificationToggle = toolbarBottom.menu.findItem(R.id.action_notif_text_prod)
        activityArguments = intent.getStringArrayExtra(URL)!!
        wfo = activityArguments[0]
        if (Utility.readPref(this, "WFO_REMEMBER_LOCATION", "") == "true") {
            wfo = Utility.readPref(this, "WFO_LAST_USED", Location.wfo)
        }
        if (wfo == "") {
            wfo = "OUN"
        }
        product = if (activityArguments[1] == "") {
            UIPreferences.wfoTextFav
        } else {
            activityArguments[1]
        }
        if (product.startsWith("RTP") && product.length == 5) {
            val state = Utility.getWfoSiteName(wfo).split(",")[0]
            product = "RTP$state"
        }
        title = product
        imageMap = ObjectImageMap(this, this, R.id.map, toolbar, toolbarBottom, listOf<View>(objectCardText.get(), scrollView))
        imageMap.addClickHandler(::mapSwitch, UtilityImageMap::mapToWfo)
        getContent()
    }

    private fun getContentFixThis() {
        invalidateOptionsMenu()
        when (drw.token) {
            "RTPZZ" -> {
                val state = Utility.getWfoSiteName(wfo).split(",")[0]
                getProduct(drw.token.replace("ZZ", state))
            }
            else -> getProduct(drw.token)
        }
    }

    override fun onRestart() {
        if (ridFavOld != UIPreferences.wfoFav) {
            locationList = UtilityFavorites.setupMenu(this, UIPreferences.wfoFav, wfo, prefToken)
        }
        super.onRestart()
    }

    private fun getContent() {
        locationList = UtilityFavorites.setupMenu(this@WfoTextActivity, UIPreferences.wfoFav, wfo, prefToken)
        updateSubmenuNotificationText()
        invalidateOptionsMenu()
        if (UIPreferences.wfoFav.contains(":$wfo:")) {
            star.setIcon(GlobalVariables.STAR_ICON)
        } else {
            star.setIcon(GlobalVariables.STAR_OUTLINE_ICON)
        }
        scrollView.smoothScrollTo(0, 0)
        ridFavOld = UIPreferences.wfoFav
        if (product != oldProduct) {
            version = 1
        }
        if (wfo != oldWfo) {
            version = 1
        }
        FutureVoid(this@WfoTextActivity, ::download, ::update)
    }

    private fun download() {
        html = when {
            product == "CLI" -> UtilityDownload.getTextProduct(this@WfoTextActivity, product + wfo + originalWfo)
            product.startsWith("RTP") && product.length == 5 -> UtilityDownload.getTextProduct(this@WfoTextActivity, product)
            else -> {
                if (version == 1) {
                    UtilityDownload.getTextProduct(this@WfoTextActivity, product + wfo)
                } else {
                    UtilityDownload.getTextProduct(product + wfo, version)
                }
            }
        }
    }

    private fun update() {
        title = when {
            product.startsWith("RTP") && product.length == 5 -> product
            else -> product + wfo
        }
        toolbar.subtitle = UtilityWfoText.codeToName[product]
        cardList.forEach { linearLayout.removeView(it) }
        objectCardText.visibility = View.VISIBLE
        scrollView.visibility = View.VISIBLE
        if (html == "") {
            html = "None issued by this office recently."
        }
        objectCardText.setTextAndTranslate(html)
        if (fixedWidthProducts.contains(product) || product.startsWith("RTP")) {
            objectCardText.typefaceMono()
        } else {
            objectCardText.typefaceDefault()
        }
        UtilityTts.conditionalPlay(activityArguments, 2, applicationContext, html, product)
        if (activityArguments[1] == "") {
            if (product.startsWith("RTP") && product.length == 5) {
                Utility.writePref(this@WfoTextActivity, "WFO_TEXT_FAV", "RTPZZ")
            } else {
                Utility.writePref(this@WfoTextActivity, "WFO_TEXT_FAV", product)
            }
            UIPreferences.wfoTextFav = product
        }
        oldProduct = product
        oldWfo = wfo
        Utility.writePref(this@WfoTextActivity, "WFO_LAST_USED", wfo)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val textToShare = UtilityShare.prepTextForShare(html)
        if (audioPlayMenu(item.itemId, html, product, product + wfo)) {
            return true
        }
        when (item.itemId) {
            R.id.action_back -> {
                version += 2
                getContent()
            }
            R.id.action_forward -> {
                if (version > 1) version -= 2
                getContent()
            }
            R.id.action_fav -> toggleFavorite()
            R.id.action_notif_text_prod -> {
                UtilityNotificationTextProduct.toggle(this, linearLayout, product + wfo)
                updateSubmenuNotificationText()
            }
            R.id.action_prod_by_state -> {
                wfoByState()
                getContentByState()
            }
            R.id.action_map -> imageMap.toggleMap()
            R.id.action_pin -> UtilityShortcut.create(this, ShortcutType.AFD)
            R.id.action_website -> ObjectIntent.showWebView(this, arrayOf("https://www.weather.gov/" + wfo.lowercase(Locale.US), wfo, "extended"))
            R.id.action_hazards -> ObjectIntent.showImage(this, arrayOf("https://www.weather.gov/wwamap/png/" + wfo.lowercase(Locale.US) + ".png", "$wfo WWA Map"))
            R.id.action_share -> UtilityShare.text(this, product + wfo, textToShare)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun getProduct(product: String) {
        this.product = product
        getContent()
    }

    private fun mapSwitch(loc: String) {
        wfo = loc.uppercase(Locale.US)
        originalWfo = wfo
        mapShown = false
        getContent()
    }

    private fun toggleFavorite() {
        UtilityFavorites.toggle(this, wfo, star, prefToken)
    }

    private fun updateSubmenuNotificationText() {
        if (UtilityNotificationTextProduct.check(product + wfo)) {
            notificationToggle.title = resources.getString(R.string.notif_remove)
        } else {
            notificationToggle.title = resources.getString(R.string.notif_add)
        }
    }

    private fun wfoByState() {
        val state = locationList[0].split(" ")[1]
        wfoListPerState.clear()
        GlobalArrays.wfos.filter { it.contains(state) }.forEach {
                    wfoListPerState.add(RegExp.space.split(it)[0].replace(":", ""))
                }
        wfoListPerState.sort()
    }

    private fun getContentByState() {
        scrollView.smoothScrollTo(0, 0)
        ridFavOld = UIPreferences.wfoFav
        if (product != oldProduct) {
            version = 1
        }
        if (wfo != oldWfo) {
            version = 1
        }
        title = product
        wfoProd.clear()
        FutureVoid(this@WfoTextActivity, ::downloadState, ::updateState)
    }

    private fun downloadState() {
        html = ""
        wfoListPerState.forEach {
            html = if (version == 1) {
                UtilityDownload.getTextProduct(this@WfoTextActivity, product + it)
            } else {
                UtilityDownload.getTextProduct(product + it, version)
            }
            wfoProd.add(html)
        }
    }

    private fun updateState() {
        objectCardText.visibility = View.GONE
        cardList.clear()
        wfoProd.forEach {
            val textCard = ObjectCardText(this@WfoTextActivity, linearLayout)
            textCard.setTextAndTranslate(it)
            cardList.add(textCard.get())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        locationList = UtilityFavorites.setupMenu(this@WfoTextActivity, UIPreferences.wfoFav, wfo, prefToken)
        when (item.itemId) {
            R.id.action_sector -> genericDialog(locationList) {
                if (locationList.isNotEmpty()) {
                    when (it) {
                        1 -> ObjectIntent.favoriteAdd(this, arrayOf("WFO"))
                        2 -> ObjectIntent.favoriteRemove(this, arrayOf("WFO"))
                        else -> {
                            wfo = locationList[it].split(" ").getOrNull(0) ?: ""
                            originalWfo = wfo
                            if (product.startsWith("RTP") && product.length == 5) {
                                val state = Utility.getWfoSiteName(wfo).split(",")[0]
                                product = "RTP$state"
                            }
                            getContent()
                        }
                    }
                    if (firstTime) {
                        UtilityToolbar.fullScreenMode(toolbar, toolbarBottom)
                        firstTime = false
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

    // For navigation drawer
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drw.actionBarDrawerToggle.syncState()
    }

    // For navigation drawer
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drw.actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_M -> {
                if (event.isCtrlPressed)
                    toolbarBottom.showOverflowMenu()
                return true
            }
            KeyEvent.KEYCODE_D -> {
                if (event.isCtrlPressed)
                    drw.drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            KeyEvent.KEYCODE_F -> {
                if (event.isCtrlPressed)
                    toggleFavorite()
                return true
            }
            KeyEvent.KEYCODE_P -> {
                if (event.isCtrlPressed)
                    audioPlayMenu(R.id.action_read_aloud, html, product, product + wfo)
                return true
            }
            KeyEvent.KEYCODE_S -> {
                if (event.isCtrlPressed)
                    audioPlayMenu(R.id.action_stop, html, product, product + wfo)
                return true
            }
            KeyEvent.KEYCODE_L -> {
                if (event.isCtrlPressed)
                    imageMap.toggleMap()
                return true
            }
            KeyEvent.KEYCODE_SLASH -> {
                if (event.isAltPressed)
                    ObjectDialogue(this, Utility.showWfoTextShortCuts())
                return true
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }
}

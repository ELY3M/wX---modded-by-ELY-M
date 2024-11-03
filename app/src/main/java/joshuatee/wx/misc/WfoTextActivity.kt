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

package joshuatee.wx.misc

import android.annotation.SuppressLint
import android.content.res.Configuration
import java.util.Locale
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import androidx.core.view.GravityCompat
import joshuatee.wx.safeGet
import joshuatee.wx.R
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.notifications.NotificationTextProduct
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.common.RegExp
import joshuatee.wx.objects.FavoriteType
import joshuatee.wx.objects.FutureText2
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.ui.CardText
import joshuatee.wx.ui.NavDrawer
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.ObjectImageMap
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.DownloadText
import joshuatee.wx.util.UtilityShortcut
import joshuatee.wx.util.UtilityFavorites
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImageMap
import joshuatee.wx.util.WfoSites

class WfoTextActivity : AudioPlayActivity(), OnMenuItemClickListener {

    //
    // The primary purpose of this activity is to view AFD from location's NWS office
    // However, other NWS office text products are also available from the AB menu
    // A map icon is also provided to select an office different from your current location
    //
    // Arguments
    // 1: NWS office
    // 2: product
    // 3: "sound" (optional)

    companion object {
        const val URL = ""
    }

    private lateinit var arguments: Array<String>
    private var product = ""
    private var wfo = ""
    private lateinit var imageMap: ObjectImageMap
    private lateinit var notificationToggle: MenuItem
    private lateinit var star: MenuItem
    private lateinit var locationList: List<String>
    private var ridFavOld = ""
    private var version = 1
    private var oldProduct = ""
    private var oldWfo = ""
    private val wfoListPerState = mutableListOf<String>()
    private val cardList = mutableListOf<View>()
    private lateinit var cardText: CardText
    private lateinit var navDrawer: NavDrawer
    private lateinit var scrollView: ScrollView
    private lateinit var box: VBox
    private var originalWfo = ""
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
        arguments = intent.getStringArrayExtra(URL)!!
        wfo = arguments[0]
        setupUI()
        setupProductSite()
        title = product
        getContent()
    }

    private fun setupUI() {
        scrollView = findViewById(R.id.scrollView)
        box = VBox.fromResource(this)
        objectToolbarBottom.connect(this)
        navDrawer = NavDrawer(this, UtilityWfoText.labels, UtilityWfoText.codes, ::changeProduct)
        cardText = CardText(this, toolbar, toolbarBottom)
        box.addWidget(cardText)
        star = objectToolbarBottom.getFavIcon()
        notificationToggle = toolbarBottom.menu.findItem(R.id.action_notif_text_prod)
        imageMap = ObjectImageMap(
            this,
            R.id.map,
            objectToolbar,
            objectToolbarBottom,
            listOf<View>(cardText.getView(), scrollView)
        )
        imageMap.connect(::mapSwitch, UtilityImageMap::mapToWfo)
    }

    private fun setupProductSite() {
        if (Utility.readPref(this, "WFO_REMEMBER_LOCATION", "") == "true") {
            wfo = Utility.readPref(this, "WFO_LAST_USED", Location.wfo)
        }
        if (wfo == "") {
            wfo = "OUN"
        }
        product = if (arguments[1] == "") {
            UIPreferences.wfoTextFav
        } else {
            arguments[1]
        }
        if (product.startsWith("RTP") && product.length == 5) {
//            val state = UtilityLocation.getWfoSiteName(wfo).split(",")[0]
            val state = WfoSites.getState(wfo)
            product = "RTP$state"
        }
    }

    private fun changeProduct() {
        invalidateOptionsMenu()
        when (navDrawer.token) {
            "RTPZZ" -> {
                val state = WfoSites.getState(wfo)
                getProduct(navDrawer.token.replace("ZZ", state))
            }

            else -> getProduct(navDrawer.token)
        }
    }

    override fun onRestart() {
        if (ridFavOld != UIPreferences.favorites[FavoriteType.WFO]) {
            locationList = UtilityFavorites.setupMenu(this, wfo, FavoriteType.WFO)
        }
        super.onRestart()
    }

    private fun getContent() {
        locationList = UtilityFavorites.setupMenu(this, wfo, FavoriteType.WFO)
        updateSubmenuNotificationText()
        invalidateOptionsMenu()
        if (UIPreferences.favorites[FavoriteType.WFO]!!.contains(":$wfo:")) {
            star.setIcon(GlobalVariables.STAR_ICON)
        } else {
            star.setIcon(GlobalVariables.STAR_OUTLINE_ICON)
        }
        scrollView.smoothScrollTo(0, 0)
        ridFavOld = UIPreferences.favorites[FavoriteType.WFO]!!
        if (product != oldProduct) {
            version = 1
        }
        if (wfo != oldWfo) {
            version = 1
        }
        FutureText2(::download, ::update)
    }

    private fun download(): String = when {
        product == "CLI" -> DownloadText.byProduct(this, product + wfo + originalWfo)
        product.startsWith("RTP") && product.length == 5 -> DownloadText.byProduct(this, product)
        else -> {
            if (version == 1) {
                DownloadText.byProduct(this, product + wfo)
            } else {
                DownloadText.byProduct(product + wfo, version)
            }
        }
    }

    private fun update(html1: String) {
        var html = html1
        title = when {
            product.startsWith("RTP") && product.length == 5 -> product
            else -> product + wfo
        }
        toolbar.subtitle = UtilityWfoText.codeToName[product]
        cardList.forEach {
            box.removeView(it)
        }
        cardText.visibility = View.VISIBLE
        scrollView.visibility = View.VISIBLE
        if (html == "") {
            html = "None issued by this office recently."
        }
        cardText.setTextAndTranslate(html)
        if (listOf("RTP", "RWR", "CLI", "RVA").contains(product) || product.startsWith("RTP")) {
            cardText.typefaceMono()
        } else {
            cardText.typefaceDefault()
        }
        UtilityTts.conditionalPlay(arguments, 2, applicationContext, html, product)
        if (arguments[1] == "") {
            if (product.startsWith("RTP") && product.length == 5) {
                Utility.writePref(this, "WFO_TEXT_FAV", "RTPZZ")
            } else {
                Utility.writePref(this, "WFO_TEXT_FAV", product)
            }
            UIPreferences.wfoTextFav = product
        }
        oldProduct = product
        oldWfo = wfo
        Utility.writePref(this, "WFO_LAST_USED", wfo)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val textToShare = UtilityShare.prepTextForShare(cardText.text)
        if (audioPlayMenu(item.itemId, cardText.text, product, product + wfo)) {
            return true
        }
        when (item.itemId) {
            R.id.action_back -> {
                version += 2
                getContent()
            }

            R.id.action_forward -> {
                if (version > 1) {
                    version -= 2
                }
                getContent()
            }

            R.id.action_fav -> toggleFavorite()
            R.id.action_notif_text_prod -> {
                NotificationTextProduct.toggle(this, box.get(), product + wfo)
                updateSubmenuNotificationText()
            }

            R.id.action_prod_by_state -> {
                wfoByState()
                getContentByState()
            }

            R.id.action_map -> imageMap.toggleMap()
            R.id.action_pin -> UtilityShortcut.create(this, ShortcutType.AFD)
            R.id.action_website -> Route.webView(
                this,
                "https://www.weather.gov/" + wfo.lowercase(Locale.US),
                wfo,
                "extended"
            )

            R.id.action_hazards -> Route.image(
                this,
                "https://www.weather.gov/wwamap/png/" + wfo.lowercase(Locale.US) + ".png",
                "$wfo WWA Map"
            )

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
        getContent()
    }

    private fun toggleFavorite() {
        UtilityFavorites.toggle(this, wfo, star, FavoriteType.WFO)
    }

    private fun updateSubmenuNotificationText() {
        if (NotificationTextProduct.check(product + wfo)) {
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
        ridFavOld = UIPreferences.favorites[FavoriteType.WFO]!!
        if (product != oldProduct) {
            version = 1
        }
        if (wfo != oldWfo) {
            version = 1
        }
        title = product
        wfoProd.clear()
        FutureVoid(::downloadState, ::updateState)
    }

    private fun downloadState() {
        wfoListPerState.forEach {
            val html = if (version == 1) {
                DownloadText.byProduct(this, product + it)
            } else {
                DownloadText.byProduct(product + it, version)
            }
            wfoProd.add(html)
        }
    }

    private fun updateState() {
        cardText.visibility = View.GONE
        cardList.clear()
        wfoProd.forEach {
            val textCard = CardText(this)
            box.addWidget(textCard)
            textCard.setTextAndTranslate(it)
            cardList.add(textCard.getView())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (navDrawer.onOptionsItemSelected(item)) {
            return true
        }
        locationList = UtilityFavorites.setupMenu(this, wfo, FavoriteType.WFO)
        when (item.itemId) {
            R.id.action_sector -> ObjectDialogue.generic(this, locationList, ::getContent) {
                if (locationList.isNotEmpty()) {
                    when (it) {
                        1 -> Route.favoriteAdd(this, FavoriteType.WFO)
                        2 -> Route.favoriteRemove(this, FavoriteType.WFO)
                        else -> {
                            wfo = locationList[it].split(" ").getOrNull(0) ?: ""
                            originalWfo = wfo
                            if (product.startsWith("RTP") && product.length == 5) {
                                val state = WfoSites.getState(wfo)
                                product = "RTP$state"
                            }
                        }
                    }
                }
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        navDrawer.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        navDrawer.onConfigurationChanged(newConfig)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_M -> if (event.isCtrlPressed) toolbarBottom.showOverflowMenu()
            KeyEvent.KEYCODE_D -> if (event.isCtrlPressed) navDrawer.openGravity(GravityCompat.START)
            KeyEvent.KEYCODE_F -> if (event.isCtrlPressed) toggleFavorite()
            KeyEvent.KEYCODE_P -> if (event.isCtrlPressed) audioPlayMenu(
                R.id.action_read_aloud,
                cardText.text,
                product,
                product + wfo
            )

            KeyEvent.KEYCODE_S -> if (event.isCtrlPressed) audioPlayMenu(
                R.id.action_stop,
                cardText.text,
                product,
                product + wfo
            )

            KeyEvent.KEYCODE_L -> if (event.isCtrlPressed) imageMap.toggleMap()
            KeyEvent.KEYCODE_SLASH -> if (event.isAltPressed) ObjectDialogue(
                this,
                Utility.showWfoTextShortCuts()
            )

            else -> super.onKeyUp(keyCode, event)
        }
        return true
    }
}

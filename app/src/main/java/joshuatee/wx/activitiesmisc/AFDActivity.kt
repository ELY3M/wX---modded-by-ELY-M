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

package joshuatee.wx.activitiesmisc

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Typeface
import java.util.Locale

import androidx.cardview.widget.CardView
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import androidx.core.view.GravityCompat
import joshuatee.wx.Extensions.parseColumn

import joshuatee.wx.R
import joshuatee.wx.audio.UtilityTts
import joshuatee.wx.notifications.UtilityNotificationTextProduct
import joshuatee.wx.MyApplication
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.settings.FavAddActivity
import joshuatee.wx.settings.FavRemoveActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.GlobalArrays
import joshuatee.wx.ui.*

import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.util.*
import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.activity_afd.*

class AfdActivity : AudioPlayActivity(), OnItemSelectedListener, OnMenuItemClickListener {

    // The primary purpose of this activity is to view AFD from location's NWS office
    // However, other NWS office text products are also available from the AB menu
    // A map icon is also provided to select an office different from your current location
    //
    // Arguments
    // 1: NWS office
    // 2: product
    //

    companion object {
        const val URL: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
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
    private var version = 0
    private var oldProduct = ""
    private var oldWfo = ""
    private var wfoListPerState = mutableListOf<String>()
    private val cardList = mutableListOf<CardView>()
    private lateinit var textCard: ObjectCardText
    private lateinit var spinner: ObjectSpinner
    private lateinit var drw: ObjectNavDrawer
    private var originalWfo = ""
    private val fixedWidthProducts = listOf("RTP", "RWR", "CLI", "RVA")

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_afd, R.menu.afd)
        toolbarBottom.setOnMenuItemClickListener(this)
        drw = ObjectNavDrawer(this, UtilityWfoText.labels, UtilityWfoText.codes)
        drw.setListener(::getContentFixThis)
        UtilityShortcut.hidePinIfNeeded(toolbarBottom)
        textCard = ObjectCardText(this, linearLayout, toolbar, toolbarBottom)
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
            MyApplication.wfoTextFav
        } else {
            activityArguments[1]
        }
        if (product.startsWith("RTP") && product.length == 5) {
            val state = Utility.getWfoSiteName(wfo).split(",")[0]
            product = "RTP$state"
        }
        title = product
        version = 1
        oldProduct = ""
        oldWfo = ""
        locationList = UtilityFavorites.setupMenu(this, MyApplication.wfoFav, wfo, prefToken)
        spinner = ObjectSpinner(this, this, this, R.id.spinner1, locationList)
        imageMap = ObjectImageMap(
                this,
                this,
                R.id.map,
                toolbar,
                toolbarBottom,
                listOf<View>(textCard.card, scrollView)
        )
        imageMap.addClickHandler(::mapSwitch, UtilityImageMap::mapToWfo)
    }

    private fun getContentFixThis() {
        when (drw.token) {
            "CLI" -> {
                product = drw.token
                checkForCliSite()
            }
            "RTPZZ" -> {
                val state = Utility.getWfoSiteName(wfo).split(",")[0]
                getProduct(drw.token.replace("ZZ", state))
            }
            else -> {
                getProduct(drw.token)
            }
        }
    }

    override fun onRestart() {
        if (ridFavOld != MyApplication.wfoFav) {
            locationList = UtilityFavorites.setupMenu(
                    this,
                    MyApplication.wfoFav,
                    wfo,
                    prefToken
            )
            spinner.refreshData(this, locationList)
        }
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        updateSubmenuNotificationText()
        if (MyApplication.wfoFav.contains(":$wfo:")) {
            star.setIcon(MyApplication.STAR_ICON)
        } else {
            star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        }
        scrollView.smoothScrollTo(0, 0)
        ridFavOld = MyApplication.wfoFav
        if (product != oldProduct) {
            version = 1
        }
        if (wfo != oldWfo) {
            version = 1
        }
        html = withContext(Dispatchers.IO) {
            when {
                product == "CLI" -> {
                    UtilityDownload.getTextProduct(this@AfdActivity, product + wfo + originalWfo)
                }
                product.startsWith("RTP") && product.length == 5 -> {
                    UtilityDownload.getTextProduct(this@AfdActivity, product)
                }
                else -> {
                    if (version == 1) {
                        UtilityDownload.getTextProduct(this@AfdActivity, product + wfo)
                    } else {
                        UtilityDownload.getTextProduct(product + wfo, version)
                    }
                }
            }
        }
        title = when {
            product.startsWith("RTP") && product.length == 5 -> product
            else -> product + wfo
        }
        // restore the WFO as CLI modifies to a sub-region
        if (product == "CLI") {
            wfo = originalWfo
        }
        toolbar.subtitle = UtilityWfoText.codeToName[product]
        cardList.forEach {
            linearLayout.removeView(it)
        }
        textCard.visibility = View.VISIBLE
        scrollView.visibility = View.VISIBLE
        if (html == "") {
            html = "None issued by this office recently."
        }
        if (fixedWidthProducts.contains(product) || product.startsWith("RTP")) {
            textCard.setTextAndTranslate(html)
        } else {
            //textCard.setTextAndTranslate(Utility.fromHtml(html))
            textCard.setTextAndTranslate(html)
        }
        if (fixedWidthProducts.contains(product) || product.startsWith("RTP")) {
            textCard.tv.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        } else {
            textCard.tv.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        UtilityTts.conditionalPlay(activityArguments, 2, applicationContext, html, product)
        if (activityArguments[1] == "") {
            if (product.startsWith("RTP") && product.length == 5) {
                Utility.writePref(this@AfdActivity, "WFO_TEXT_FAV", "RTPZZ")
            } else {
                Utility.writePref(this@AfdActivity, "WFO_TEXT_FAV", product)
            }
            MyApplication.wfoTextFav = product
        }
        oldProduct = product
        oldWfo = wfo
        Utility.writePref(this@AfdActivity, "WFO_LAST_USED", wfo)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, html, product, product + wfo)) {
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
                UtilityNotificationTextProduct.toggle(this, linearLayout, product + wfo)
                updateSubmenuNotificationText()
            }
            R.id.action_prod_by_state -> {
                wfoByState()
                getContentByState()
            }
            R.id.action_map -> imageMap.toggleMap()
            R.id.action_pin -> UtilityShortcut.create(this, ShortcutType.AFD)
            R.id.action_website -> ObjectIntent(
                    this,
                    WebView::class.java,
                    WebView.URL,
                    arrayOf("https://www.weather.gov/" + wfo.toLowerCase(Locale.US), wfo, "extended")
            )
            R.id.action_hazards -> ObjectIntent(
                    this,
                    ImageShowActivity::class.java,
                    ImageShowActivity.URL,
                    arrayOf(
                            "https://www.weather.gov/wwamap/png/" + wfo.toLowerCase(Locale.US) + ".png",
                            "$wfo WWA Map"
                    )
            )
            R.id.action_share -> UtilityShare.shareText(
                    this,
                    product + wfo,
                    Utility.fromHtml(html)
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun getProduct(product: String) {
        this.product = product
        getContent()
    }

    private fun mapSwitch(loc: String) {
        wfo = loc.toUpperCase(Locale.US)
        originalWfo = wfo
        mapShown = false
        locationList = UtilityFavorites.setupMenu(this, MyApplication.wfoFav, wfo, prefToken)
        spinner.refreshData(this, locationList)
    }

    private fun toggleFavorite() {
        val ridFav = UtilityFavorites.toggleString(this, wfo, star, prefToken)
        locationList = UtilityFavorites.setupMenu(this, ridFav, wfo, prefToken)
        spinner.refreshData(this, locationList)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (locationList.isNotEmpty()) {
            when (pos) {
                1 -> ObjectIntent(
                        this,
                        FavAddActivity::class.java,
                        FavAddActivity.TYPE,
                        arrayOf("WFO")
                )
                2 -> ObjectIntent(
                        this,
                        FavRemoveActivity::class.java,
                        FavRemoveActivity.TYPE,
                        arrayOf("WFO")
                )
                else -> {
                    wfo = locationList[pos].split(" ").getOrNull(0) ?: ""
                    originalWfo = wfo
                    if (product.startsWith("RTP") && product.length == 5) {
                        val state = Utility.getWfoSiteName(wfo).split(",")[0]
                        product = "RTP$state"
                    }
                    if (product == "CLI") {
                        checkForCliSite()
                    } else {
                        getContent()
                    }
                }
            }
            if (firstTime) {
                UtilityToolbar.fullScreenMode(toolbar, toolbarBottom)
                firstTime = false
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private fun updateSubmenuNotificationText() {
        if (UtilityNotificationTextProduct.check(product + wfo)) {
            notificationToggle.title = resources.getString(R.string.notif_remove)
        } else {
            notificationToggle.title = resources.getString(R.string.notif_add)
        }
    }

    private fun wfoByState() {
        val state = locationList[0].split(" ")[1]
        wfoListPerState = mutableListOf()
        wfoListPerState.clear()
        GlobalArrays.wfos
                .filter { it.contains(state) }
                .forEach {
                    wfoListPerState.add(MyApplication.space.split(it)[0].replace(":", ""))
                }
        wfoListPerState.sort()
    }

    private fun getContentByState() = GlobalScope.launch(uiDispatcher) {
        val wfoProd = mutableListOf<String>()
        scrollView.smoothScrollTo(0, 0)
        ridFavOld = MyApplication.wfoFav
        if (product != oldProduct) {
            version = 1
        }
        if (wfo != oldWfo) {
            version = 1
        }
        title = product
        withContext(Dispatchers.IO) {
            html = ""
            wfoListPerState.forEach {
                html = if (version == 1) {
                    UtilityDownload.getTextProduct(this@AfdActivity, product + it)
                } else {
                    UtilityDownload.getTextProduct(product + it, version)
                }
                wfoProd.add(html)
            }
        }
        textCard.visibility = View.GONE
        cardList.clear()
        wfoProd.forEach {
            val textCard = ObjectCardText(this@AfdActivity, linearLayout)
            textCard.setTextAndTranslate(Utility.fromHtml(it))
            cardList.add(textCard.card)
        }
    }

    private fun checkForCliSite() = GlobalScope.launch(uiDispatcher) {
        if (product == "CLI") {
            val cliHtml = withContext(Dispatchers.IO) {
                UtilityDownload.getStringFromUrl("https://w2.weather.gov/climate/index.php?wfo=" + wfo.toLowerCase(Locale.US))
            }
            val cliSites = cliHtml.parseColumn("cf6PointArray\\[.\\] = new Array\\('.*?','(.*?)'\\)")
            val cliNames = cliHtml.parseColumn("cf6PointArray\\[.\\] = new Array\\('(.*?)','.*?'\\)")
            val dialogueMain = ObjectDialogue(this@AfdActivity, "Select site from $wfo:", cliNames)
            dialogueMain.setSingleChoiceItems(DialogInterface.OnClickListener { dialog, index ->
                wfo = Utility.safeGet(cliSites, index)
                dialog.dismiss()
                getContent()
            })
            originalWfo = wfo
            dialogueMain.show()
        }
    }

    // For navigation drawer
    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            drw.actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

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
                if (event.isCtrlPressed) {
                    toolbarBottom.showOverflowMenu()
                }
                return true
            }
            KeyEvent.KEYCODE_D -> {
                if (event.isCtrlPressed) {
                    drw.drawerLayout.openDrawer(GravityCompat.START)
                }
                true
            }
            KeyEvent.KEYCODE_F -> {
                if (event.isCtrlPressed) {
                    toggleFavorite()
                }
                return true
            }
            KeyEvent.KEYCODE_P -> {
                if (event.isCtrlPressed) {
                    audioPlayMenu(R.id.action_read_aloud, html, product, product + wfo)
                }
                return true
            }
            KeyEvent.KEYCODE_S -> {
                if (event.isCtrlPressed) {
                    audioPlayMenu(R.id.action_stop, html, product, product + wfo)
                }
                return true
            }
            KeyEvent.KEYCODE_L -> {
                if (event.isCtrlPressed) {
                    imageMap.toggleMap()
                }
                return true
            }
            KeyEvent.KEYCODE_SLASH -> {
                if (event.isAltPressed) {
                    ObjectDialogue(this, Utility.showWfoTextShortCuts())
                }
                return true
            }
            else -> super.onKeyUp(keyCode, event)
        }
    }
}


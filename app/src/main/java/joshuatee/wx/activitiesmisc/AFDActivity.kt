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

package joshuatee.wx.activitiesmisc

import android.annotation.SuppressLint
import android.content.Context
import java.util.Locale

import androidx.cardview.widget.CardView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener

import joshuatee.wx.R
import joshuatee.wx.audio.UtilityTTS
import joshuatee.wx.notifications.UtilityNotificationTextProduct
import joshuatee.wx.MyApplication
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.settings.FavAddActivity
import joshuatee.wx.settings.FavRemoveActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.*

import joshuatee.wx.WFO_ARR
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.util.*
import kotlinx.coroutines.*

class AFDActivity : AudioPlayActivity(), OnItemSelectedListener, OnMenuItemClickListener {

    // The primary purpose of this activity is to view AFD from location's NWS office
    // However, other NWS office text products are also available from the AB menu
    // A map icon is also provided to select an office different from your current location
    //
    // Arugments
    // 1: NWS office
    // 2: product
    //

    companion object {
        const val URL: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var firstTime = true
    private lateinit var turl: Array<String>
    private var prod = ""
    private var nwsOffice = ""
    private var sector = ""
    private lateinit var imageMap: ObjectImageMap
    private lateinit var sv: ScrollView
    private var sigHtmlTmp = ""
    private var mapShown = false
    private lateinit var notifToggle: MenuItem
    private lateinit var star: MenuItem
    private lateinit var ridArrLoc: List<String>
    private val prefTokenLocation = "NWS_LOCATION_"
    private val prefToken = "WFO_FAV"
    private var ridFavOld = ""
    private var version = 0
    private var oldProd = ""
    private var oldNwsOffice = ""
    private var wfoListPerState = mutableListOf<String>()
    private lateinit var ll: LinearLayout
    private val cardList = mutableListOf<CardView>()
    private lateinit var c0: ObjectCardText
    private lateinit var spinner1: ObjectSpinner
    private lateinit var contextg: Context

    /**
     *
     */
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_afd, R.menu.afd)
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        UtilityShortcut.hidePinIfNeeded(toolbarBottom)
        ll = findViewById(R.id.ll)
        c0 = ObjectCardText(this)
        ll.addView(c0.card)
        c0.setOnClickListener(View.OnClickListener { UtilityToolbar.showHide(toolbar, toolbarBottom) })
        star = toolbarBottom.menu.findItem(R.id.action_fav)
        notifToggle = toolbarBottom.menu.findItem(R.id.action_notif_text_prod)
        turl = intent.getStringArrayExtra(URL)
        nwsOffice = turl[0]
        if (Utility.readPref(this, "WFO_REMEMBER_LOCATION", "") == "true") {
            nwsOffice = Utility.readPref(this, "WFO_LAST_USED", Location.wfo)
        }
        if (nwsOffice == "") {
            nwsOffice = "OUN"
        }
        prod = if (turl[1] == "") {
            MyApplication.wfoTextFav
        } else {
            turl[1]
        }
        title = prod
        version = 1
        oldProd = ""
        oldNwsOffice = ""
        ridArrLoc = UtilityFavorites.setupFavMenu(this, MyApplication.wfoFav, nwsOffice, prefTokenLocation, prefToken)
        spinner1 = ObjectSpinner(this, this, R.id.spinner1, ridArrLoc)
        spinner1.setOnItemSelectedListener(this)
        sv = findViewById(R.id.sv)
        imageMap = ObjectImageMap(this, this, R.id.map, toolbar, toolbarBottom, listOf<View>(c0.card, sv))
        imageMap.addOnImageMapClickedHandler(object : ImageMap.OnImageMapClickedHandler {
            override fun onImageMapClicked(id: Int, im2: ImageMap) {
                sector = UtilityImageMap.maptoWFO(id)
                im2.visibility = View.GONE
                mapSwitch(sector.toUpperCase(Locale.US))
            }

            override fun onBubbleClicked(id: Int) {}
        })
    }

    override fun onRestart() {
        if (ridFavOld != MyApplication.wfoFav) {
            ridArrLoc = UtilityFavorites.setupFavMenu(this, MyApplication.wfoFav, nwsOffice, prefTokenLocation, prefToken)
            spinner1.refreshData(this, ridArrLoc)
        }
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        updateSubmenuNotifText()
        if (MyApplication.wfoFav.contains(":$nwsOffice:")) {
            star.setIcon(MyApplication.STAR_ICON)
        } else {
            star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        }
        sv.smoothScrollTo(0, 0)
        ridFavOld = MyApplication.wfoFav
        if (prod != oldProd) {
            version = 1
        }
        if (nwsOffice != oldNwsOffice) {
            version = 1
        }

        sigHtmlTmp = withContext(Dispatchers.IO) {
            if (version == 1) {
                UtilityDownload.getTextProduct(contextg, prod + nwsOffice)
            } else {
                UtilityDownload.getTextProduct(prod + nwsOffice, version)
            }
        }

        title = prod
        cardList.forEach { ll.removeView(it) }
        c0.setVisibility(View.VISIBLE)
        sv.visibility = View.VISIBLE
        if (sigHtmlTmp == "") {
            sigHtmlTmp = "None issued by this office recently."
        }
        c0.setTextAndTranslate(Utility.fromHtml(sigHtmlTmp))
        if (turl.size > 2) {
            if (turl[2] == "sound") {
                UtilityTTS.synthesizeTextAndPlay(applicationContext, sigHtmlTmp, prod)
            }
        }
        if (turl[1] == "") {
            Utility.writePref(contextg, "WFO_TEXT_FAV", prod)
            MyApplication.wfoTextFav = prod
        }
        oldProd = prod
        oldNwsOffice = nwsOffice
        Utility.writePref(contextg, "WFO_LAST_USED", nwsOffice)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, sigHtmlTmp, prod, prod + nwsOffice)) {
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
                UtilityNotificationTextProduct.toggle(this, ll, prod + nwsOffice)
                updateSubmenuNotifText()
            }
            R.id.action_prod_by_state -> {
                wfoByState()
                getContentByState()
            }
            R.id.action_map -> imageMap.toggleMap()
            R.id.action_pin -> UtilityShortcut.createShortcut(this, ShortcutType.AFD)
            R.id.action_afd -> getProduct("AFD")
            R.id.action_vfd -> getProduct("VFD")
            R.id.action_hwo -> getProduct("HWO")
            R.id.action_sps -> getProduct("SPS")
            R.id.action_rva -> getProduct("RVA")
            R.id.action_esf -> getProduct("ESF")
            R.id.action_rtp -> getProduct("RTP")
            R.id.action_fwf -> getProduct("FWF")
            R.id.action_pns -> getProduct("PNS")
            R.id.action_lsr -> getProduct("LSR")
            R.id.action_rer -> getProduct("RER")
            R.id.action_nsh -> getProduct("NSH")
            R.id.action_website -> ObjectIntent(this, WebscreenABModels::class.java, WebscreenABModels.URL, arrayOf("http://www.weather.gov/" + nwsOffice.toLowerCase(Locale.US), nwsOffice))
            R.id.action_hazards -> ObjectIntent(this, ImageShowActivity::class.java, ImageShowActivity.URL, arrayOf("http://www.weather.gov/wwamap/png/" + nwsOffice.toLowerCase(Locale.US) + ".png", "$nwsOffice WWA Map"))
            R.id.action_forecast -> ObjectIntent(this, WebscreenABModels::class.java, WebscreenABModels.URL, arrayOf(UtilityDownloadNWS.get7DayURL(Location.x, Location.y), "Local forecast"))
            R.id.action_share -> UtilityShare.shareText(this, prod + nwsOffice, Utility.fromHtml(sigHtmlTmp))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun getProduct(prod: String) {
        this.prod = prod
        getContent()
    }

    private fun mapSwitch(loc: String) {
        nwsOffice = loc
        mapShown = false
        ridArrLoc = UtilityFavorites.setupFavMenu(this, MyApplication.wfoFav, nwsOffice, prefTokenLocation, prefToken)
        spinner1.refreshData(this, ridArrLoc)
    }

    private fun toggleFavorite() {
        val ridFav = UtilityFavorites.toggleFavoriteString(this, nwsOffice, star, prefToken)
        ridArrLoc = UtilityFavorites.setupFavMenu(this, ridFav, nwsOffice, prefTokenLocation, prefToken)
        spinner1.refreshData(this, ridArrLoc)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (ridArrLoc.isNotEmpty()) {
            when (pos) {
                1 -> ObjectIntent(this, FavAddActivity::class.java, FavAddActivity.TYPE, arrayOf("WFO"))
                2 -> ObjectIntent(this, FavRemoveActivity::class.java, FavRemoveActivity.TYPE, arrayOf("WFO"))
                else -> {
                    nwsOffice = ridArrLoc[pos].split(" ").getOrNull(0) ?: ""
                    getContent()
                }
            }
            if (firstTime) {
                UtilityToolbar.fullScreenMode(toolbar, toolbarBottom)
                firstTime = false
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private fun updateSubmenuNotifText() {
        if (UtilityNotificationTextProduct.check(prod + nwsOffice)) {
            notifToggle.title = resources.getString(R.string.notif_remove)
        } else {
            notifToggle.title = resources.getString(R.string.notif_add)
        }
    }

    private fun wfoByState() {
        val state = ridArrLoc[0].split(" ")[1]
        wfoListPerState = mutableListOf()
        wfoListPerState.clear()
        WFO_ARR
                .filter { it.contains(state) }
                .forEach { wfoListPerState.add(MyApplication.space.split(it)[0].replace(":", "")) }
        wfoListPerState.sort()
    }

    private fun getContentByState() = GlobalScope.launch(uiDispatcher) {
        val wfoProd = mutableListOf<String>()
        sv.smoothScrollTo(0, 0)
        ridFavOld = MyApplication.wfoFav
        if (prod != oldProd) {
            version = 1
        }
        if (nwsOffice != oldNwsOffice) {
            version = 1
        }
        title = prod
        withContext(Dispatchers.IO) {
            sigHtmlTmp = ""
            wfoListPerState.forEach {
                sigHtmlTmp = if (version == 1) {
                    UtilityDownload.getTextProduct(contextg, prod + it)
                } else {
                    UtilityDownload.getTextProduct(prod + it, version)
                }
                wfoProd.add(sigHtmlTmp)
            }
        }
        c0.setVisibility(View.GONE)
        cardList.clear()
        wfoProd.forEach {
            val cTmp = ObjectCardText(contextg)
            cTmp.setTextAndTranslate(Utility.fromHtml(it))
            cardList.add(cTmp.card)
            ll.addView(cTmp.card)
        }
    }
}


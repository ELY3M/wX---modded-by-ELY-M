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

package joshuatee.wx.spc

import android.annotation.SuppressLint
import java.util.Locale
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.safeGet
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.FavoriteType
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.ui.CardText
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.ObjectImageMap
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.DownloadText
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityFavorites
import joshuatee.wx.util.UtilityImageMap
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.UtilityString

class LsrByWfoActivity : AudioPlayActivity(), OnMenuItemClickListener {

    //
    // The primary purpose of this activity is to view all recent LSR by WFO
    // Arguments
    // 1: NWS office
    // 2: product ( always LSR )
    //

    companion object {
        const val URL = ""
    }

    private var firstTime = true
    private var wfo = ""
    private lateinit var imageMap: ObjectImageMap
    private lateinit var star: MenuItem
    private lateinit var scrollView: ScrollView
    private lateinit var box: VBox
    private var locations = listOf<String>()
    private var ridFavOld = ""
    private var lsrList = mutableListOf<String>()
    private var textList = mutableListOf<CardText>()
    private var numberLSR = ""

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.afd_top, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_sector).title = locations.safeGet(0)
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_afd, R.menu.lsrbywfo)
        val arguments = intent.getStringArrayExtra(URL)!!
        wfo = arguments[0]
        title = "LSR"
        setupUI()
        getContent()
    }

    private fun setupUI() {
        scrollView = findViewById(R.id.scrollView)
        box = VBox.fromResource(this)
        objectToolbarBottom.connect(this)
        star = objectToolbarBottom.getFavIcon()
        locations = UtilityFavorites.setupMenu(this, wfo, FavoriteType.WFO)
        imageMap = ObjectImageMap(
            this,
            R.id.map,
            objectToolbar,
            objectToolbarBottom,
            listOf<View>(scrollView)
        )
        imageMap.connect(::mapSwitch, UtilityImageMap::mapToWfo)
    }

    override fun onRestart() {
        if (ridFavOld != UIPreferences.favorites[FavoriteType.WFO]) {
            locations = UtilityFavorites.setupMenu(this, wfo, FavoriteType.WFO)
        }
        super.onRestart()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, lsrList.toString(), "LSR", wfo)) {
            return true
        }
        when (item.itemId) {
            R.id.action_fav -> toggleFavorite()
            R.id.action_map -> imageMap.toggleMap()
            R.id.action_share -> UtilityShare.text(this, "LSR$wfo", lsrList.toString())
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun mapSwitch(loc: String) {
        scrollView.visibility = View.VISIBLE
        wfo = loc.uppercase(Locale.US)
        locations = UtilityFavorites.setupMenu(this, wfo, FavoriteType.WFO)
        getContent()
    }

    private fun toggleFavorite() {
        UtilityFavorites.toggle(this, wfo, star, FavoriteType.WFO)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        locations = UtilityFavorites.setupMenu(this, wfo, FavoriteType.WFO)
        when (item.itemId) {
            R.id.action_sector -> ObjectDialogue.generic(this, locations, ::getContent) {
                if (locations.isNotEmpty()) {
                    when (it) {
                        1 -> Route.favoriteAdd(this, FavoriteType.WFO)
                        2 -> Route.favoriteRemove(this, FavoriteType.WFO)
                        else -> wfo = locations[it].split(" ").getOrNull(0) ?: ""
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

    private fun getContent() {
        locations = UtilityFavorites.setupMenu(this, wfo, FavoriteType.WFO)
        invalidateOptionsMenu()
        if (UIPreferences.favorites[FavoriteType.WFO]!!.contains(":$wfo:")) {
            star.setIcon(GlobalVariables.STAR_ICON)
        } else {
            star.setIcon(GlobalVariables.STAR_OUTLINE_ICON)
        }
        scrollView.smoothScrollTo(0, 0)
        ridFavOld = UIPreferences.favorites[FavoriteType.WFO]!!
        box.removeChildren()
        FutureVoid(::downloadFirst, ::getLsrFromWfo)
    }

    private fun downloadFirst() {
        numberLSR = UtilityString.getHtmlAndParseLastMatch(
            "https://forecast.weather.gov/product.php?site=$wfo&issuedby=$wfo&product=LSR&format=txt&version=1&glossary=0",
            "product=LSR&format=TXT&version=(.*?)&glossary"
        )
    }

    private fun download(i: Int, version: Int) {
        lsrList[i] = DownloadText.byProduct("LSR$wfo", version)
    }

    private fun update(i: Int) {
        textList[i].setText1(lsrList[i])
        textList[i].typefaceMono()
    }

    private fun getLsrFromWfo() {
        lsrList.clear()
        textList.clear()
        if (numberLSR == "") {
            lsrList = mutableListOf("None issued by this office recently.")
            toolbar.subtitle = "Showing: None"
        } else {
            var maxVersions = To.int(numberLSR)
            toolbar.subtitle = "Showing: " + To.string(maxVersions)
            if (maxVersions > 30) {
                maxVersions = 30
            }
            var i = 0
            (1..maxVersions step 2).forEach { version ->
                lsrList.add("")
                textList.add(CardText(this))
                box.addWidget(textList.last())
                val iFinal = i
                FutureVoid({ download(iFinal, version) }, { update(iFinal) })
                i += 1
            }
        }
    }
}

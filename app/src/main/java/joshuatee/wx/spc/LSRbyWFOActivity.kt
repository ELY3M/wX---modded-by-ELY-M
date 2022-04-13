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

package joshuatee.wx.spc

import android.annotation.SuppressLint
import java.util.Locale
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.Extensions.safeGet
import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.*
import joshuatee.wx.util.*

class LsrByWfoActivity : AudioPlayActivity(), OnMenuItemClickListener {

    //
    // The primary purpose of this activity is to view all recent LSR by WFO
    // Arguments
    // 1: NWS office
    // 2: product ( always LSR )
    //

    companion object { const val URL = "" }

    private var firstTime = true
    private var wfo = ""
    private lateinit var imageMap: ObjectImageMap
    private var mapShown = false
    private lateinit var star: MenuItem
    private lateinit var scrollView: ScrollView
    private lateinit var linearLayout: LinearLayout
    private var locations = listOf<String>()
    private val prefToken = "WFO_FAV"
    private var ridFavOld = ""
    private var lsrList = mutableListOf<String>()
    private var textList = mutableListOf<ObjectCardText>()
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
        scrollView = findViewById(R.id.scrollView)
        linearLayout = findViewById(R.id.linearLayout)
        toolbarBottom.setOnMenuItemClickListener(this)
        star = toolbarBottom.menu.findItem(R.id.action_fav)
        val activityArguments = intent.getStringArrayExtra(URL)
        wfo = activityArguments!![0]
        if (wfo == "") {
            wfo = "OUN"
        }
        title = "LSR"
        locations = UtilityFavorites.setupMenu(this, MyApplication.wfoFav, wfo, prefToken)
        imageMap = ObjectImageMap(this, this, R.id.map, toolbar, toolbarBottom, listOf<View>(scrollView))
        imageMap.addClickHandler(::mapSwitch, UtilityImageMap::mapToWfo)
        getContent()
    }

    override fun onRestart() {
        if (ridFavOld != MyApplication.wfoFav) {
            locations = UtilityFavorites.setupMenu(this, MyApplication.wfoFav, wfo, prefToken)
        }
        super.onRestart()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, lsrList.toString(), "LSR",  wfo)) {
            return true
        }
        when (item.itemId) {
            R.id.action_fav -> toggleFavorite()
            R.id.action_map -> imageMap.toggleMap()
            R.id.action_share -> UtilityShare.text(this, "LSR$wfo", Utility.fromHtml(lsrList.toString()))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun mapSwitch(loc: String) {
        scrollView.visibility = View.VISIBLE
        wfo = loc.uppercase(Locale.US)
        mapShown = false
        locations = UtilityFavorites.setupMenu(this, MyApplication.wfoFav, wfo, prefToken)
        getContent()
    }

    private fun toggleFavorite() {
        UtilityFavorites.toggle(this, wfo, star, prefToken)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        locations = UtilityFavorites.setupMenu(this, MyApplication.wfoFav, wfo, prefToken)
        when (item.itemId) {
            R.id.action_sector -> genericDialog(locations) {
                if (locations.isNotEmpty()) {
                    when (it) {
                        1 -> ObjectIntent.favoriteAdd(this, arrayOf("WFO"))
                        2 -> ObjectIntent.favoriteRemove(this, arrayOf("WFO"))
                        else -> {
                            wfo = locations[it].split(" ").getOrNull(0) ?: ""
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

    private fun getContent() {
        locations = UtilityFavorites.setupMenu(this@LsrByWfoActivity, MyApplication.wfoFav, wfo, prefToken)
        invalidateOptionsMenu()
        if (MyApplication.wfoFav.contains(":$wfo:")) {
            star.setIcon(MyApplication.STAR_ICON)
        } else {
            star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        }
        scrollView.smoothScrollTo(0, 0)
        ridFavOld = MyApplication.wfoFav
        linearLayout.removeAllViewsInLayout()
        FutureVoid(this, ::downloadFirst, ::getLsrFromWfo)
    }

    private fun downloadFirst() {
        numberLSR = UtilityString.getHtmlAndParseLastMatch(
                "https://forecast.weather.gov/product.php?site=$wfo&issuedby=$wfo&product=LSR&format=txt&version=1&glossary=0",
                "product=LSR&format=TXT&version=(.*?)&glossary"
        )
    }

    private fun download(i: Int , version: Int) {
        lsrList[i] = UtilityDownload.getTextProduct("LSR$wfo", version)
    }

    private fun update(i: Int) {
        textList[i].setText1(Utility.fromHtml(lsrList[i]))
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
            toolbar.subtitle = "Showing: " + (maxVersions - 1).toString()
            if (maxVersions > 30) {
                maxVersions = 30
            }
            var i = 0
            (1..maxVersions + 1 step 2).forEach { version ->
                lsrList.add("")
                textList.add(ObjectCardText(this@LsrByWfoActivity, linearLayout))
                val iFinal = i
                FutureVoid(this, { download(iFinal, version) }, { update(iFinal) })
                i += 1
            }
        }
    }
}

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

package joshuatee.wx.spc

import android.annotation.SuppressLint
import android.content.Context
import java.util.Locale

import android.widget.LinearLayout
import android.widget.ScrollView
import android.os.AsyncTask
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener

import joshuatee.wx.R
import joshuatee.wx.util.ImageMap
import joshuatee.wx.MyApplication
import joshuatee.wx.audio.AudioPlayActivity
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.settings.FavAddActivity
import joshuatee.wx.settings.FavRemoveActivity
import joshuatee.wx.ui.*
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.util.UtilityFavorites
import joshuatee.wx.util.UtilityImageMap
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.UtilityString

class LSRbyWFOActivity : AudioPlayActivity(), OnItemSelectedListener, OnMenuItemClickListener {

    // The primary purpose of this activity is to view all recent LSR by WFO
    // Arugments
    // 1: NWS office
    // 2: product ( always LSR )
    //

    companion object {
        const val URL: String = ""
    }

    private var firstTime = true
    private var prod = ""
    private var nwsOffice = ""
    private var sector = ""
    private lateinit var imageMap: ObjectImageMap
    private lateinit var sv: ScrollView
    private var mapShown = false
    private lateinit var star: MenuItem
    private var ridArrLoc = listOf<String>()
    private val prefTokenLocation = "NWS_LOCATION_"
    private val prefToken = "WFO_FAV"
    private var ridFavOld = ""
    private lateinit var linearLayout: LinearLayout
    private var wfoProd = listOf<String>()
    private lateinit var sp: ObjectSpinner
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_afd, R.menu.lsrbywfo)
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        linearLayout = findViewById(R.id.ll)
        star = toolbarBottom.menu.findItem(R.id.action_fav)
        val turl = intent.getStringArrayExtra(URL)
        nwsOffice = turl[0]
        if (nwsOffice == "") nwsOffice = "OUN"
        prod = if (turl[1] == "")
            MyApplication.wfoTextFav
        else
            turl[1]
        toolbar.title = prod
        ridArrLoc = UtilityFavorites.setupFavMenu(this, MyApplication.wfoFav, nwsOffice, prefTokenLocation, prefToken)
        sp = ObjectSpinner(this, this, R.id.spinner1, ridArrLoc)
        sp.setOnItemSelectedListener(this)
        sv = findViewById(R.id.sv)
        imageMap = ObjectImageMap(this, this, R.id.map, toolbar, toolbarBottom, listOf<View>(sv))
        imageMap.addOnImageMapClickedHandler(object : ImageMap.OnImageMapClickedHandler {
            override fun onImageMapClicked(id: Int, im2: ImageMap) {
                im2.visibility = View.GONE
                sector = UtilityImageMap.maptoWFO(id)
                mapSwitch(sector.toUpperCase(Locale.US))
            }

            override fun onBubbleClicked(id: Int) {}
        })
    }

    override fun onRestart() {
        if (ridFavOld != MyApplication.wfoFav) {
            ridArrLoc = UtilityFavorites.setupFavMenu(this, MyApplication.wfoFav, nwsOffice, prefTokenLocation, prefToken)
            sp.refreshData(contextg, ridArrLoc)
        }
        super.onRestart()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (audioPlayMenu(item.itemId, wfoProd.toString(), prod, prod + nwsOffice))
            return true
        when (item.itemId) {
            R.id.action_fav -> toggleFavorite()
            R.id.action_map -> imageMap.toggleMap()
            R.id.action_share -> UtilityShare.shareText(this, prod + nwsOffice, Utility.fromHtml(wfoProd.toString()))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun mapSwitch(loc: String) {
        sv.visibility = View.VISIBLE
        nwsOffice = loc
        mapShown = false
        ridArrLoc = UtilityFavorites.setupFavMenu(this, MyApplication.wfoFav, nwsOffice, prefTokenLocation, prefToken)
        sp.refreshData(contextg, ridArrLoc)
    }

    private fun toggleFavorite() {
        val ridFav = UtilityFavorites.toggleFavoriteString(this, nwsOffice, star, prefToken)
        ridArrLoc = UtilityFavorites.setupFavMenu(this, ridFav, nwsOffice, prefTokenLocation, prefToken)
        sp.refreshData(contextg, ridArrLoc)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (ridArrLoc.isNotEmpty()) {
            when (pos) {
                1 -> ObjectIntent(this, FavAddActivity::class.java, FavAddActivity.TYPE, arrayOf("WFO"))
                2 -> ObjectIntent(this, FavRemoveActivity::class.java, FavRemoveActivity.TYPE, arrayOf("WFO"))
                else -> {
                    nwsOffice = ridArrLoc[pos].split(" ").getOrNull(0) ?: ""
                    GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                }
            }
            if (firstTime) {
                UtilityToolbar.fullScreenMode(toolbar, toolbarBottom)
                firstTime = false
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            sv.smoothScrollTo(0, 0)
            ridFavOld = MyApplication.wfoFav
        }

        override fun doInBackground(vararg params: String): String {
            wfoProd = lsrFromWFO
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            linearLayout.removeAllViewsInLayout()
            wfoProd.forEach { linearLayout.addView(ObjectCardText(contextg, Utility.fromHtml(it)).card) }
        }
    }

    private val lsrFromWFO: List<String>
        get() {
            val lsrArr = mutableListOf<String>()
            val numberLSR = UtilityString.getHTMLandParseLastMatch("http://forecast.weather.gov/product.php?site=$nwsOffice&issuedby=$nwsOffice&product=LSR&format=txt&version=1&glossary=0",
                    "product=LSR&format=TXT&version=(.*?)&glossary")
            if (numberLSR == "") {
                lsrArr.add("None issued by this office recently.")
            } else {
                var maxVers = numberLSR.toIntOrNull() ?: 0
                if (maxVers > 30) {
                    maxVers = 30
                }
                (1..maxVers + 1 step 2).mapTo(lsrArr) { UtilityDownload.getTextProduct("LSR$nwsOffice", it) }
            }
            return lsrArr
        }
}

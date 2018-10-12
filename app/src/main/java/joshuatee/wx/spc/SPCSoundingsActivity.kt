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
import android.os.AsyncTask
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener

import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.activitiesmisc.WebscreenAB
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.settings.*
import joshuatee.wx.ui.*
import joshuatee.wx.util.*

class SPCSoundingsActivity : BaseActivity(), OnClickListener, OnItemSelectedListener, OnMenuItemClickListener {

    companion object {
        const val URL: String = ""
    }

    private var imgUrl = ""
    private lateinit var img: TouchImageView2
    private lateinit var imageMap: ObjectImageMap
    private var sector = ""
    private var nwsOffice = ""
    private var mapShown = false
    private var firstTime = true
    private lateinit var star: MenuItem
    private var ridArrLoc = listOf<String>()
    private val prefTokenLocation = "NWS_LOCATION_"
    private val prefToken = "SND_FAV"
    private var upperAir = ""
    private var imageLoaded = false
    private var bitmap = UtilityImg.getBlankBitmap()
    private lateinit var sp: ObjectSpinner
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_spcsoundings, R.menu.spcsoundings, true)
        toolbarBottom.setOnMenuItemClickListener(this)
        star = toolbarBottom.menu.findItem(R.id.action_fav)
        img = findViewById(R.id.iv)
        img.setOnClickListener(this)
        contextg = this
        nwsOffice = UtilityLocation.getNearestSnd(this, Location.latLon)
        ridArrLoc = UtilityFavorites.setupFavMenu(this, MyApplication.sndFav, nwsOffice, prefTokenLocation, prefToken)
        sp = ObjectSpinner(this, this, R.id.spinner1, ridArrLoc)
        sp.setOnItemSelectedListener(this)
        imageMap = ObjectImageMap(this, this, R.id.map, toolbar, toolbarBottom, listOf<View>(img))
        imageMap.addOnImageMapClickedHandler(object : ImageMap.OnImageMapClickedHandler {
            override fun onImageMapClicked(id: Int, im2: ImageMap) {
                im2.visibility = View.GONE
                sector = UtilityImageMap.maptoSND(id)
                mapSwitch(sector)
            }

            override fun onBubbleClicked(id: Int) {}
        })
    }

    override fun onRestart() {
        ridArrLoc = UtilityFavorites.setupFavMenu(this, MyApplication.sndFav, nwsOffice, prefTokenLocation, prefToken)
        sp.refreshData(this, ridArrLoc)
        super.onRestart()
    }

    // FIXME not working after change on map
    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {
            bitmap = UtilitySPCSoundings.getImage(contextg, nwsOffice)
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            img.visibility = View.VISIBLE
            img.setImageBitmap(bitmap)
            img.setMaxZoom(4f)
            //val soundingSector = Utility.readPref(contextg, "SOUNDING_SECTOR", "")
            //if (!firstRun && nwsOffice == soundingSector) {
            img.setZoom("SOUNDING")
            //    firstRun = true
            //}
            imageLoaded = true
        }

        override fun onPreExecute() {
            if (MyApplication.sndFav.contains(":$nwsOffice:"))
                star.setIcon(MyApplication.STAR_ICON)
            else
                star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContentSPCPlot : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {
            val date = UtilityString.getHTMLandParse("${MyApplication.nwsSPCwebsitePrefix}/obswx/maps/", "/obswx/maps/" + upperAir + "_([0-9]{6}_[0-9]{2}).gif")
            bitmap = UtilityImg.getBitmapAddWhiteBG(contextg, imgUrl + "_" + date + ".gif")
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            img.visibility = View.VISIBLE
            img.setImageBitmap(bitmap)
            img.setMaxZoom(4f)
        }

        override fun onPreExecute() {
            imgUrl = "${MyApplication.nwsSPCwebsitePrefix}/obswx/maps/$upperAir"
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareBitmap(this, "$nwsOffice sounding", bitmap)
            R.id.action_250mb -> setPlotAndGet("250")
            R.id.action_300mb -> setPlotAndGet("300")
            R.id.action_500mb -> setPlotAndGet("500")
            R.id.action_700mb -> setPlotAndGet("700")
            R.id.action_850mb -> setPlotAndGet("850")
            R.id.action_925mb -> setPlotAndGet("925")
            R.id.action_sfc -> setPlotAndGet("sfc")
            R.id.action_map -> imageMap.toggleMap()
            R.id.action_fav -> toggleFavorite()
            R.id.action_spc_help -> ObjectIntent(this, WebscreenAB::class.java, WebscreenAB.URL, arrayOf("${MyApplication.nwsSPCwebsitePrefix}/exper/mesoanalysis/help/begin.html", nwsOffice))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun setPlotAndGet(upperAir: String) {
        this.upperAir = upperAir
        GetContentSPCPlot().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private fun mapSwitch(loc: String) {
        nwsOffice = loc
        mapShown = false
        ridArrLoc = UtilityFavorites.setupFavMenu(this, MyApplication.sndFav, nwsOffice, prefTokenLocation, prefToken)
        sp.refreshData(this, ridArrLoc)
        img.resetZoom()
    }

    private fun toggleFavorite() {
        val ridFav = UtilityFavorites.toggleFavoriteString(this, nwsOffice, star, prefToken)
        ridArrLoc = UtilityFavorites.setupFavMenu(this, ridFav, nwsOffice, prefTokenLocation, prefToken)
        sp.refreshData(this, ridArrLoc)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (ridArrLoc.isNotEmpty()) {
            if (firstTime) {
                UtilityToolbar.fullScreenMode(toolbar, toolbarBottom)
                firstTime = false
            }
            when (pos) {
                1 -> ObjectIntent(this, FavAddActivity::class.java, FavAddActivity.TYPE, arrayOf("SND"))
                2 -> ObjectIntent(this, FavRemoveActivity::class.java, FavRemoveActivity.TYPE, arrayOf("SND"))
                else -> {
                    nwsOffice = ridArrLoc[pos].split(" ").getOrNull(0) ?: ""
                    GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    override fun onClick(v: View) {
        when (v.id) {R.id.iv -> UtilityToolbar.showHide(toolbar, toolbarBottom)
        }
    }

    override fun onStop() {
        if (imageLoaded) {
            UtilityImg.imgSavePosnZoom(this, img, "SOUNDING")
            Utility.writePref(this, "SOUNDING_SECTOR", nwsOffice)
        }
        super.onStop()
    }
}



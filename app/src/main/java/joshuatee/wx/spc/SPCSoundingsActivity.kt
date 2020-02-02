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
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener

import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.activitiesmisc.WebView
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.settings.*
import joshuatee.wx.ui.*
import joshuatee.wx.util.*
import kotlinx.coroutines.*

class SpcSoundingsActivity : BaseActivity(), OnItemSelectedListener,
        OnMenuItemClickListener {

    companion object {
        const val URL: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var imgUrl = ""
    private lateinit var img: ObjectTouchImageView
    private lateinit var imageMap: ObjectImageMap
    private var nwsOffice = ""
    private var mapShown = false
    private var firstTime = true
    private lateinit var star: MenuItem
    private var locations = listOf<String>()
    private val prefToken = "SND_FAV"
    private var upperAir = ""
    private var bitmap = UtilityImg.getBlankBitmap()
    private lateinit var objectSpinner: ObjectSpinner

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
                savedInstanceState,
                R.layout.activity_spcsoundings,
                R.menu.spcsoundings,
                true
        )
        toolbarBottom.setOnMenuItemClickListener(this)
        star = toolbarBottom.menu.findItem(R.id.action_fav)
        img = ObjectTouchImageView(this, this, toolbar, toolbarBottom, R.id.iv)
        nwsOffice = UtilityLocation.getNearestSnd(Location.latLon)
        locations = UtilityFavorites.setupFavMenu(this, MyApplication.sndFav, nwsOffice, prefToken)
        objectSpinner = ObjectSpinner(this, this, this, R.id.spinner1, locations)
        imageMap = ObjectImageMap(this, this, R.id.map, toolbar, toolbarBottom, listOf<View>(img.img))
        imageMap.addClickHandler(::mapSwitch, UtilityImageMap::mapToSnd)
    }

    override fun onRestart() {
        locations = UtilityFavorites.setupFavMenu(this, MyApplication.sndFav, nwsOffice, prefToken)
        objectSpinner.refreshData(this, locations)
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        if (MyApplication.sndFav.contains(":$nwsOffice:"))
            star.setIcon(MyApplication.STAR_ICON)
        else
            star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        withContext(Dispatchers.IO) {
            bitmap = UtilitySpcSoundings.getImage(this@SpcSoundingsActivity, nwsOffice)
        }
        img.img.visibility = View.VISIBLE
        img.setBitmap(bitmap)
        img.setMaxZoom(4f)
        img.firstRunSetZoomPosn("SOUNDING")
        Utility.writePref(this@SpcSoundingsActivity, "SOUNDING_SECTOR", nwsOffice)
    }

    private fun getContentSPCPlot() = GlobalScope.launch(uiDispatcher) {
        imgUrl = "${MyApplication.nwsSPCwebsitePrefix}/obswx/maps/$upperAir"
        withContext(Dispatchers.IO) {
            val date = UtilityString.getHtmlAndParse(
                    "${MyApplication.nwsSPCwebsitePrefix}/obswx/maps/",
                    "/obswx/maps/" + upperAir + "_([0-9]{6}_[0-9]{2}).gif"
            )
            bitmap = UtilityImg.getBitmapAddWhiteBG(this@SpcSoundingsActivity, imgUrl + "_" + date + ".gif")
        }
        img.img.visibility = View.VISIBLE
        img.setBitmap(bitmap)
        img.setMaxZoom(4f)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareBitmap(this, this, "$nwsOffice sounding", bitmap)
            R.id.action_250mb -> setPlotAndGet("250")
            R.id.action_300mb -> setPlotAndGet("300")
            R.id.action_500mb -> setPlotAndGet("500")
            R.id.action_700mb -> setPlotAndGet("700")
            R.id.action_850mb -> setPlotAndGet("850")
            R.id.action_925mb -> setPlotAndGet("925")
            R.id.action_sfc -> setPlotAndGet("sfc")
            R.id.action_map -> imageMap.toggleMap()
            R.id.action_fav -> toggleFavorite()
            R.id.action_spc_help -> ObjectIntent(
                    this,
                    WebView::class.java,
                    WebView.URL,
                    arrayOf(
                            "${MyApplication.nwsSPCwebsitePrefix}/exper/mesoanalysis/help/begin.html",
                            nwsOffice
                    )
            )
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun setPlotAndGet(upperAir: String) {
        this.upperAir = upperAir
        getContentSPCPlot()
    }

    private fun mapSwitch(loc: String) {
        nwsOffice = loc
        mapShown = false
        locations = UtilityFavorites.setupFavMenu(this, MyApplication.sndFav, nwsOffice, prefToken)
        objectSpinner.refreshData(this, locations)
        img.resetZoom()
    }

    private fun toggleFavorite() {
        val ridFav = UtilityFavorites.toggleFavoriteString(this, nwsOffice, star, prefToken)
        locations = UtilityFavorites.setupFavMenu(this, ridFav, nwsOffice, prefToken)
        objectSpinner.refreshData(this, locations)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (locations.isNotEmpty()) {
            if (firstTime) {
                UtilityToolbar.fullScreenMode(toolbar, toolbarBottom)
                firstTime = false
            }
            when (pos) {
                1 -> ObjectIntent(
                        this,
                        FavAddActivity::class.java,
                        FavAddActivity.TYPE,
                        arrayOf("SND")
                )
                2 -> ObjectIntent(
                        this,
                        FavRemoveActivity::class.java,
                        FavRemoveActivity.TYPE,
                        arrayOf("SND")
                )
                else -> {
                    nwsOffice = locations[pos].split(" ").getOrNull(0) ?: ""
                    getContent()
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    override fun onStop() {
        img.imgSavePosnZoom(this, "SOUNDING")
        super.onStop()
    }
}



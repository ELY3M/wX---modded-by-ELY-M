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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.Extensions.safeGet

import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.settings.*
import joshuatee.wx.ui.*
import joshuatee.wx.util.*
import kotlinx.coroutines.*

class SpcSoundingsActivity : BaseActivity(), OnMenuItemClickListener {

    companion object { const val URL = "" }

    private val uiDispatcher = Dispatchers.Main
    private var imgUrl = ""
    private lateinit var img: ObjectTouchImageView
    private lateinit var imageMap: ObjectImageMap
    private var office = ""
    private var mapShown = false
    private var firstTime = true
    private lateinit var star: MenuItem
    private var locations = listOf<String>()
    private val prefToken = "SND_FAV"
    private var upperAir = ""
    private var bitmap = UtilityImg.getBlankBitmap()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.spcsoundings_top, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_sector).title = locations.safeGet(0)
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_spcsoundings, R.menu.spcsoundings, true)
        toolbarBottom.setOnMenuItemClickListener(this)
        star = toolbarBottom.menu.findItem(R.id.action_fav)
        img = ObjectTouchImageView(this, this, toolbar, toolbarBottom, R.id.iv)
        office = UtilityLocation.getNearestSoundingSite(Location.latLon)
        imageMap = ObjectImageMap(this, this, R.id.map, toolbar, toolbarBottom, listOf<View>(img.img))
        imageMap.addClickHandler(::mapSwitch, UtilityImageMap::mapToSnd)
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        locations = UtilityFavorites.setupMenu(this@SpcSoundingsActivity, MyApplication.sndFav, office, prefToken)
        invalidateOptionsMenu()
        if (MyApplication.sndFav.contains(":$office:")) star.setIcon(MyApplication.STAR_ICON) else star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        bitmap = withContext(Dispatchers.IO) { UtilitySpcSoundings.getImage(this@SpcSoundingsActivity, office) }
        img.img.visibility = View.VISIBLE
        img.setBitmap(bitmap)
        img.setMaxZoom(4f)
        img.firstRunSetZoomPosn("SOUNDING")
        Utility.writePref(this@SpcSoundingsActivity, "SOUNDING_SECTOR", office)
    }

    private fun getContentSPCPlot() = GlobalScope.launch(uiDispatcher) {
        imgUrl = "${MyApplication.nwsSPCwebsitePrefix}/obswx/maps/$upperAir"
        withContext(Dispatchers.IO) {
            val date = UtilityString.getHtmlAndParse("${MyApplication.nwsSPCwebsitePrefix}/obswx/maps/", "/obswx/maps/" + upperAir + "_([0-9]{6}_[0-9]{2}).gif")
            bitmap = UtilityImg.getBitmapAddWhiteBackground(this@SpcSoundingsActivity, imgUrl + "_" + date + ".gif")
        }
        img.img.visibility = View.VISIBLE
        img.setBitmap(bitmap)
        img.setMaxZoom(4f)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.bitmap(this, this, "$office sounding", bitmap)
            R.id.action_250mb -> setPlotAndGet("250")
            R.id.action_300mb -> setPlotAndGet("300")
            R.id.action_500mb -> setPlotAndGet("500")
            R.id.action_700mb -> setPlotAndGet("700")
            R.id.action_850mb -> setPlotAndGet("850")
            R.id.action_925mb -> setPlotAndGet("925")
            R.id.action_sfc -> setPlotAndGet("sfc")
            R.id.action_map -> imageMap.toggleMap()
            R.id.action_fav -> toggleFavorite()
            R.id.action_spc_help -> ObjectIntent.showWebView(this, arrayOf("${MyApplication.nwsSPCwebsitePrefix}/exper/mesoanalysis/help/begin.html", office))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun setPlotAndGet(upperAir: String) {
        this.upperAir = upperAir
        getContentSPCPlot()
    }

    private fun mapSwitch(location: String) {
        office = location
        mapShown = false
        getContent()
        img.resetZoom()
    }

    private fun toggleFavorite() {
        UtilityFavorites.toggle(this, office, star, prefToken)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        locations = UtilityFavorites.setupMenu(this, MyApplication.sndFav, office, prefToken)
        when (item.itemId) {
            R.id.action_sector -> genericDialog(locations) {
                if (locations.isNotEmpty()) {
                    if (firstTime) {
                        UtilityToolbar.fullScreenMode(toolbar, toolbarBottom)
                        firstTime = false
                    }
                    when (it) {
                        1 -> ObjectIntent.favoriteAdd(this, arrayOf("SND"))
                        2 -> ObjectIntent.favoriteRemove(this, arrayOf("SND"))
                        else -> {
                            office = locations[it].split(" ").getOrNull(0) ?: ""
                            getContent()
                        }
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

    override fun onStop() {
        img.imgSavePosnZoom(this, "SOUNDING")
        super.onStop()
    }
}



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

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.getHtml
import joshuatee.wx.parse
import joshuatee.wx.safeGet
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.FavoriteType
import joshuatee.wx.objects.FutureBytes2
import joshuatee.wx.objects.Route
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.ObjectImageMap
import joshuatee.wx.ui.TouchImage
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityFavorites
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImageMap
import joshuatee.wx.util.UtilityShare

class SpcSoundingsActivity : BaseActivity(), OnMenuItemClickListener {

    companion object {
        const val URL = ""
    }

    private var imgUrl = ""
    private lateinit var touchImage: TouchImage
    private lateinit var imageMap: ObjectImageMap
    private var office = ""
    private var firstTime = true
    private lateinit var star: MenuItem
    private var locations = listOf<String>()
    private var upperAir = ""

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.spcsoundings_top, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_sector).title = locations.safeGet(0)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_spcsoundings, R.menu.spcsoundings, true)
        setupUI()
        getContent()
    }

    private fun setupUI() {
        objectToolbarBottom.connect(this)
        star = objectToolbarBottom.getFavIcon()
        touchImage = TouchImage(this, toolbar, toolbarBottom, R.id.iv)
        office = UtilityLocation.getNearestSoundingSite(Location.latLon)
        imageMap = ObjectImageMap(this, R.id.map, objectToolbar, objectToolbarBottom, listOf<View>(touchImage.get()))
        imageMap.connect(::mapSwitch, UtilityImageMap::mapToSnd)
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        locations = UtilityFavorites.setupMenu(this, office, FavoriteType.SND)
        invalidateOptionsMenu()
        if (UIPreferences.favorites[FavoriteType.SND]!!.contains(":$office:")) {
            star.setIcon(GlobalVariables.STAR_ICON)
        } else {
            star.setIcon(GlobalVariables.STAR_OUTLINE_ICON)
        }
        FutureBytes2({ UtilitySpcSoundings.getImage(this, office) }, ::showImage)
    }

    private fun showImage(bitmap: Bitmap) {
        touchImage.visibility = View.VISIBLE
        touchImage.set(bitmap)
        touchImage.setMaxZoom(4f)
        touchImage.firstRun("SOUNDING")
        Utility.writePref(this, "SOUNDING_SECTOR", office)
    }

    private fun downloadSpcPlot(): Bitmap {
        imgUrl = "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/obswx/maps/$upperAir"
        val html = "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/obswx/maps/".getHtml()
        val date = html.parse("/obswx/maps/" + upperAir + "_([0-9]{6}_[0-9]{2}).gif")
        return UtilityImg.getBitmapAddWhiteBackground(this, imgUrl + "_" + date + ".gif")
    }

    private fun showSpcPlot(bitmap: Bitmap) {
        touchImage.visibility = View.VISIBLE
        touchImage.set(bitmap)
        touchImage.setMaxZoom(4f)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.bitmap(this, "$office sounding", touchImage)
            R.id.action_250mb -> setPlotAndGet("250")
            R.id.action_300mb -> setPlotAndGet("300")
            R.id.action_500mb -> setPlotAndGet("500")
            R.id.action_700mb -> setPlotAndGet("700")
            R.id.action_850mb -> setPlotAndGet("850")
            R.id.action_925mb -> setPlotAndGet("925")
            R.id.action_sfc -> setPlotAndGet("sfc")
            R.id.action_map -> imageMap.toggleMap()
            R.id.action_fav -> toggleFavorite()
            R.id.action_spc_help -> Route.webView(this, "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/exper/mesoanalysis/help/begin.html", office)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun setPlotAndGet(upperAir: String) {
        this.upperAir = upperAir
        FutureBytes2(::downloadSpcPlot, ::showSpcPlot)
    }

    private fun mapSwitch(location: String) {
        office = location
        getContent()
        touchImage.resetZoom()
    }

    private fun toggleFavorite() {
        UtilityFavorites.toggle(this, office, star, FavoriteType.SND)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        locations = UtilityFavorites.setupMenu(this, office, FavoriteType.SND)
        when (item.itemId) {
            R.id.action_sector -> ObjectDialogue.generic(this, locations, ::getContent) {
                if (locations.isNotEmpty()) {
                    if (firstTime) {
                        UtilityToolbar.fullScreenMode(toolbar, toolbarBottom)
                        firstTime = false
                    }
                    when (it) {
                        1 -> Route.favoriteAdd(this, FavoriteType.SND)
                        2 -> Route.favoriteRemove(this, FavoriteType.SND)
                        else -> office = locations[it].split(" ").getOrNull(0) ?: ""
                    }
                }
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onStop() {
        touchImage.imgSavePosnZoom("SOUNDING")
        super.onStop()
    }
}

/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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

import android.os.Bundle
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import joshuatee.wx.getHtml
import joshuatee.wx.removeHtml
import joshuatee.wx.safeGet
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.models.ObjectModel
import joshuatee.wx.models.UtilityModels
import joshuatee.wx.objects.DisplayData
import joshuatee.wx.objects.FavoriteType
import joshuatee.wx.objects.FutureText2
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.ui.NavDrawerCombo
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.OnSwipeTouchListener
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.ui.VBox
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityFavorites
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare

class SpcMesoActivity : VideoRecordActivity(), OnMenuItemClickListener {

    //
    // native interface to the mobile SPC meso website
    //
    // arg1 - usually "", but used by spc meso images on homescreen and possibly widget intent
    // arg2 - number of panes, 1 or 2
    // arg3 - pref model token and hash lookup
    //

    companion object {
        var INFO = ""
    }

    private var sector = "19"
    private var curImg = 0
    private var numPanes = 0
    private var favListParm = listOf<String>()
    private lateinit var star: MenuItem
    private var prefSector = ""
    private val prefModel = "SPCMESO"
    private lateinit var prefParam: String
    private lateinit var prefParamLabel: String
    private lateinit var navDrawerCombo: NavDrawerCombo
    private lateinit var displayData: DisplayData
    private var layers = mapOf<SpcMesoLayerType, SpcMesoLayer>()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.spcmeso_top, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_product).title = favListParm.safeGet(0).split(" ").safeGet(0)
        displayData.objectAnimates[0].setButton(menu)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        var arguments = intent.getStringArrayExtra(INFO)
        // Keep for static pinned shortcuts
        if (arguments == null) {
            arguments = arrayOf("", "1", prefModel)
        }
        numPanes = To.int(arguments[1])
        if (numPanes == 1) {
            super.onCreate(savedInstanceState, R.layout.activity_spcmeso, R.menu.spcmesomultipane, iconsEvenlySpaced = false, bottomToolbar = true)
        } else {
            super.onCreate(savedInstanceState, R.layout.activity_spcmeso_multipane, R.menu.spcmesomultipane, iconsEvenlySpaced = false, bottomToolbar = true)
            val box = VBox.fromResource(this)
            if (UtilityUI.isLandScape(this)) {
                box.makeHorizontal()
            }
        }
        objectToolbarBottom.connect(this)
        setupProductAndSector(arguments)
        setupMenu()
        setupSwipeActions()
        UtilitySpcMeso.create()
        setupNavigationDrawer()
        getContent()
    }

    private fun setupProductAndSector(arguments: Array<String>?) {
        val numPanesAsString = numPanes.toString()
        prefSector = prefModel + numPanesAsString + "_SECTOR_LAST_USED"
        prefParam = prefModel + numPanesAsString + "_PARAM_LAST_USED"
        prefParamLabel = prefModel + numPanesAsString + "_PARAM_LAST_USED_LABEL"
        displayData = DisplayData(this, numPanes, ObjectModel(this, "", ""))
        displayData.param[0] = "pmsl"
        displayData.paramLabel[0] = "MSL Pressure/Wind"
        if (numPanes > 1) {
            displayData.param[1] = "500mb"
            displayData.paramLabel[1] = "500mb Analysis"
        }
        if (arguments!![0] != "" && numPanes == 1) {
            val tmpArrFav = UtilitySpcMeso.setParamFromFav(arguments[0])
            displayData.param[0] = tmpArrFav
            displayData.paramLabel[0] = UtilitySpcMeso.getLabelFromParam(tmpArrFav)
        } else {
            (0 until numPanes).forEach {
                displayData.param[it] = Utility.readPref(this, prefParam + it.toString(), displayData.param[it])
                displayData.paramLabel[it] = Utility.readPref(this, prefParamLabel + it.toString(), displayData.paramLabel[it])
            }
        }
        sector = Utility.readPref(this, prefSector, sector)
    }

    private fun setupMenu() {
        layers = UtilitySpcMesoInputOutput.getLayers(this, objectToolbarBottom, ::getContent)
        if (numPanes < 2) {
            objectToolbarBottom.hide(R.id.action_img1)
            objectToolbarBottom.hide(R.id.action_img2)
        } else {
            objectToolbarBottom.hide(R.id.action_multipane)
        }
        star = objectToolbarBottom.getFavIcon()
        star.setIcon(GlobalVariables.STAR_OUTLINE_ICON)
    }

    private fun setupSwipeActions() {
        if (numPanes == 1) {
            displayData.image[0].connect(object : OnSwipeTouchListener(this) {
                override fun onSwipeLeft() {
                    if (displayData.image[curImg].currentZoom < 1.01f) {
                        val index = UtilitySpcMeso.moveForward(getFavList())
                        showProductInFavList(index)
                    }
                }

                override fun onSwipeRight() {
                    if (displayData.image[curImg].currentZoom < 1.01f) {
                        val index = UtilitySpcMeso.moveBack(getFavList())
                        showProductInFavList(index)
                    }
                }
            })
        }
    }

    private fun setupNavigationDrawer() {
        navDrawerCombo = NavDrawerCombo(this, UtilitySpcMeso.groups, UtilitySpcMeso.longCodes, UtilitySpcMeso.shortCodes, "")
        navDrawerCombo.connect { changeProduct(navDrawerCombo.getUrl()) }
    }

    fun getFavList() = favListParm

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        displayData.objectAnimates.forEach {
            it.stop()
        }
        favListParm = UtilityFavorites.setupMenu(this, displayData.param[curImg], FavoriteType.SPCMESO)
        invalidateOptionsMenu()
        if (UIPreferences.favorites[FavoriteType.SPCMESO]!!.contains(":" + displayData.param[curImg] + ":")) {
            star.setIcon(GlobalVariables.STAR_ICON)
        } else {
            star.setIcon(GlobalVariables.STAR_OUTLINE_ICON)
        }
        FutureVoid(
                {
                    (0 until numPanes).forEach {
                        displayData.bitmaps[it] = UtilitySpcMesoInputOutput.getImage(this, displayData.param[it], sector, layers)
                    }
                }
        ) {
            (0 until numPanes).forEach {
                if (numPanes > 1) {
                    UtilityImg.resizeViewAndSetImage(this, displayData.bitmaps[it], displayData.image[it].get() as ImageView)
                    displayData.image[it].imageLoaded = true
                } else {
                    displayData.image[it].set(displayData.bitmaps[it])
                }
                displayData.image[it].setMaxZoom(4.0f)
            }
            displayData.image.forEachIndexed { index, touchImage ->
                touchImage.firstRun(prefModel + numPanes.toString() + index.toString())
            }
            Utility.writePref(this, prefParam + curImg, displayData.param[curImg])
            Utility.writePref(this, prefParamLabel + curImg, displayData.paramLabel[curImg])
            setTitle()
        }
    }

    private fun getAnimate(frames: Int) {
        displayData.objectAnimates.forEachIndexed { index, objectAnimate ->
            objectAnimate.animateClicked({}) {
                UtilitySpcMesoInputOutput.getAnimation(
                        displayData.param[index],
                        sector,
                        frames)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (navDrawerCombo.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_toggleRadar -> layers[SpcMesoLayerType.Radar]!!.toggle()
            R.id.action_toggleTopography -> layers[SpcMesoLayerType.Topography]!!.toggle()
            R.id.action_toggleCounty -> layers[SpcMesoLayerType.County]!!.toggle()
            R.id.action_toggleObs -> layers[SpcMesoLayerType.Observations]!!.toggle()
            R.id.action_togglePopulation -> layers[SpcMesoLayerType.Population]!!.toggle()
            R.id.action_toggleSPCOutlook -> layers[SpcMesoLayerType.Outlook]!!.toggle()
            R.id.action_toggleWatWarn -> layers[SpcMesoLayerType.Watwarn]!!.toggle()
            R.id.action_mslp -> changeProduct("pmsl")
            R.id.action_ttd -> changeProduct("ttd")
            R.id.action_thea -> changeProduct("thea")
            R.id.action_bigsfc -> changeProduct("bigsfc")
            R.id.action_rgnlrad -> changeProduct("rgnlrad")
            R.id.action_1kmv -> changeProduct("1kmv")
            R.id.action_300mb -> changeProduct("300mb")
            R.id.action_500mb -> changeProduct("500mb")
            R.id.action_700mb -> changeProduct("700mb")
            R.id.action_850mb2 -> changeProduct("850mb2")
            R.id.action_850mb -> changeProduct("850mb")
            R.id.action_925mb -> changeProduct("925mb")
            R.id.action_muli -> changeProduct("muli")
            R.id.action_pwtr -> changeProduct("pwtr")
            R.id.action_scp -> changeProduct("scp")
            R.id.action_sigh -> changeProduct("sigh")
            R.id.action_stpc -> changeProduct("stpc")
            R.id.action_eshr -> changeProduct("eshr")
            R.id.action_shr6 -> changeProduct("shr6")
            R.id.action_srh1 -> changeProduct("srh1")
            R.id.action_srh3 -> changeProduct("srh3")
            R.id.action_mucp -> changeProduct("mucp")
            R.id.action_sbcp -> changeProduct("sbcp")
            R.id.action_mlcp -> changeProduct("mlcp")
            R.id.action_laps -> changeProduct("laps")
            R.id.action_lllr -> changeProduct("lllr")
            R.id.action_lclh -> changeProduct("lclh")
            R.id.action_US -> changeSector("19")
            R.id.action_MW -> changeSector("20")
            R.id.action_NC -> changeSector("13")
            R.id.action_C -> changeSector("14")
            R.id.action_SC -> changeSector("15")
            R.id.action_NE -> changeSector("16")
            R.id.action_CE -> changeSector("17")
            R.id.action_SE -> changeSector("18")
            R.id.action_SW -> changeSector("12")
            R.id.action_NW -> changeSector("11")
            R.id.action_GL -> changeSector("21")
            R.id.action_GB -> changeSector("22")
            R.id.action_help -> getHelp()
            R.id.action_multipane -> Route.spcMesoDualPane(this)
            R.id.action_fav -> toggleFavorite()
            R.id.action_img1 -> setImage(0)
            R.id.action_img2 -> setImage(1)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun setImage(frameNumber: Int) {
        curImg = frameNumber
        setTitle()
    }

    private fun setTitle() {
        if (numPanes > 1) {
            UtilityModels.setSubtitleRestoreZoom(displayData.image, toolbar, "(" +
                    (curImg + 1) + ")" + displayData.paramLabel[0] + "/" + displayData.paramLabel[1])
        } else {
            toolbar.subtitle = displayData.paramLabel[0]
        }
    }

    private fun showProductInFavList(index: Int) {
        if (favListParm.count() > index) {
            displayData.param[curImg] = favListParm[index].split(" ").safeGet(0)
            displayData.paramLabel[curImg] = favListParm[index]
            getContent()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (navDrawerCombo.onOptionsItemSelected(item)) {
            return true
        }
        favListParm = UtilityFavorites.setupMenu(this, displayData.param[curImg], FavoriteType.SPCMESO)
        when (item.itemId) {
            R.id.action_product -> ObjectDialogue.generic(this, favListParm, {}) {
                when (it) {
                    1 -> Route.favoriteAdd(this, FavoriteType.SPCMESO)
                    2 -> Route.favoriteRemove(this, FavoriteType.SPCMESO)
                    else -> showProductInFavList(it)
                }
            }

            R.id.action_animate -> {
                if (displayData.objectAnimates[0].isRunning()) {
                    displayData.objectAnimates.forEach {
                        it.stop()
                    }
                } else {
                    getAnimate(To.int(RadarPreferences.uiAnimIconFrames))
                }
            }

            R.id.action_a6 -> getAnimate(6)
            R.id.action_a12 -> getAnimate(12)
            R.id.action_a18 -> getAnimate(18)
            R.id.action_pause -> displayData.objectAnimates[0].pause()
            R.id.action_share -> if (UIPreferences.recordScreenShare && Build.VERSION.SDK_INT < 33) {
                checkOverlayPerms()
            } else {
                if (numPanes == 1) {
                    val title = UtilitySpcMeso.sectorMap[sector] + " - " + displayData.paramLabel[0]
                    UtilityShare.bitmap(this, title, displayData.bitmaps[0])
                } else {
                    val title = UtilitySpcMeso.sectorMap[sector] + " - " + displayData.paramLabel[curImg]
                    UtilityShare.bitmap(this, title, displayData.bitmaps[curImg])
                }
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        navDrawerCombo.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        navDrawerCombo.onConfigurationChanged(newConfig)
    }

    private fun getHelp() {
        FutureText2(
                { ("${GlobalVariables.nwsSPCwebsitePrefix}/exper/mesoanalysis/help/help_" + displayData.param[curImg] + ".html").getHtml() },
                { s ->
                    var helpText = s
                    if (helpText.contains("Page Not Found")) {
                        helpText = "Help is not available for this parameter."
                    }
                    ObjectDialogue(this, helpText.removeHtml())
                }
        )
    }

    private fun changeProduct(param: String) {
        displayData.param[curImg] = param
        val index = UtilitySpcMeso.params.indexOf(param)
        displayData.paramLabel[curImg] = UtilitySpcMeso.labels[index]
        saveImageState()
        getContent()
    }

    private fun changeSector(sector: String) {
        displayData.image.forEach {
            it.resetZoom()
        }
        this.sector = sector
        Utility.writePref(this, prefSector, sector)
        getContent()
    }

    private fun saveImageState() {
        displayData.image.forEachIndexed { index, touchImage ->
            touchImage.imgSavePosnZoom(prefModel + numPanes.toString() + index.toString())
        }
    }

    override fun onStop() {
        saveImageState()
        super.onStop()
    }

    private fun toggleFavorite() {
        UtilityFavorites.toggle(this, displayData.param[curImg], star, FavoriteType.SPCMESO)
    }
}

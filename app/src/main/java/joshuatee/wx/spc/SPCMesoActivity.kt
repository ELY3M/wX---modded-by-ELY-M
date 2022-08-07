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
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import joshuatee.wx.Extensions.getHtml
import joshuatee.wx.Extensions.safeGet
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.models.ObjectModel
import joshuatee.wx.models.UtilityModels
import joshuatee.wx.objects.DisplayData
import joshuatee.wx.objects.FutureText2
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.ui.*
import joshuatee.wx.util.*

class SpcMesoActivity : VideoRecordActivity(), OnMenuItemClickListener {

    //
    // native interface to the mobile SPC meso website
    //
    // arg1 - number of panes, 1 or 2
    // arg2 - pref model token and hash lookup
    //

    companion object { var INFO = "" }

    private var showRadar = true
    private var showOutlook = true
    private var showWatwarn = true
    private var showTopography = true
    private var showCounty = false
    private var sector = "19"
    private lateinit var menuRadar: MenuItem
    private lateinit var menuOutlook: MenuItem
    private lateinit var menuWatwarn: MenuItem
    private lateinit var menuTopography: MenuItem
    private lateinit var menuCounty: MenuItem
    private val menuRadarStr = "Radar"
    private val menuOutlookStr = "SPC Day 1 Outlook"
    private val menuWatwarnStr = "Watches/Warnings"
    private val menuTopographyStr = "Topography"
    private val menuCountyStr = "Counties"
    private val on = "(on) "
    private var curImg = 0
    private var imageLoaded = false
    private var numPanes = 0
    private var favListParm = listOf<String>()
    private lateinit var star: MenuItem
    private var prefSector = ""
    private var prefModel = ""
    private lateinit var prefParam: String
    private lateinit var prefParamLabel: String
    private lateinit var objectNavDrawerCombo: ObjectNavDrawerCombo
    private lateinit var displayData: DisplayData
    private val prefToken = "SPCMESO_FAV"

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
        title = "SPC Meso"
        var arguments = intent.getStringArrayExtra(INFO)
        // Keep for static pinned shortcuts
        if (arguments == null) {
            arguments = arrayOf("", "1", "SPCMESO")
        }
        val numPanesAsString = arguments[1]
        numPanes = numPanesAsString.toIntOrNull() ?: 0
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
        prefModel = arguments[2]
        prefSector = prefModel + numPanesAsString + "_SECTOR_LAST_USED"
        prefParam = prefModel + numPanesAsString + "_PARAM_LAST_USED"
        prefParamLabel = prefModel + numPanesAsString + "_PARAM_LAST_USED_LABEL"
        displayData = DisplayData(this, this, numPanes, ObjectModel(this, "", ""))
        displayData.param[0] = "pmsl"
        displayData.paramLabel[0] = "MSL Pressure/Wind"
        if (numPanes > 1) {
            displayData.param[1] = "500mb"
            displayData.paramLabel[1] = "500mb Analysis"
        }
        if (arguments[0] != "" && numPanes == 1) {
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
        showRadar = Utility.readPref(this, prefModel + "_SHOW_RADAR", "false").startsWith("t")
        showOutlook = Utility.readPref(this, prefModel + "_SHOW_OUTLOOK", "false").startsWith("t")
        showWatwarn = Utility.readPref(this, prefModel + "_SHOW_WATWARN", "false").startsWith("t")
        showTopography = Utility.readPref(this, prefModel + "_SHOW_TOPO", "false").startsWith("t")
        showCounty= Utility.readPref(this, prefModel + "_SHOW_COUNTY", "false").startsWith("t")
        val menu = toolbarBottom.menu
        menuRadar = menu.findItem(R.id.action_toggleRadar)
        menuOutlook = menu.findItem(R.id.action_toggleSPCOutlook)
        menuWatwarn = menu.findItem(R.id.action_toggleWatWarn)
        menuTopography = menu.findItem(R.id.action_toggleTopography)
        menuCounty = menu.findItem(R.id.action_toggleCounty)
        if (numPanes < 2) {
            objectToolbarBottom.hide(R.id.action_img1)
            objectToolbarBottom.hide(R.id.action_img2)
        } else {
            objectToolbarBottom.hide(R.id.action_multipane)
        }
        star = objectToolbarBottom.getFavIcon()
        star.setIcon(GlobalVariables.STAR_OUTLINE_ICON)
        if (showRadar) {
            menuRadar.title = on + menuRadarStr
        }
        if (showOutlook) {
            menuOutlook.title = on + menuOutlookStr
        }
        if (showWatwarn) {
            menuWatwarn.title = on + menuWatwarnStr
        }
        if (showTopography) {
            menuTopography.title = on + menuTopographyStr
        }
        if (showCounty) {
            menuCounty.title = on + menuCountyStr
        }
        if (numPanes == 1) {
            displayData.image[0].connect(object : OnSwipeTouchListener(this) {
                override fun onSwipeLeft() { if (displayData.image[curImg].currentZoom < 1.01f) {
                    val index = UtilitySpcMeso.moveForward(getFavList())
                    showProductInFavList(index)
                } }

                override fun onSwipeRight() { if (displayData.image[curImg].currentZoom < 1.01f) {
                    val index = UtilitySpcMeso.moveBack(getFavList())
                    showProductInFavList(index)
                } }
            })
        }
        UtilitySpcMeso.create()
        objectNavDrawerCombo = ObjectNavDrawerCombo(this, UtilitySpcMeso.groups, UtilitySpcMeso.longCodes, UtilitySpcMeso.shortCodes, "")
        objectNavDrawerCombo.connect2 { _, _, groupPosition, childPosition, _ ->
            objectNavDrawerCombo.close()
            setAndLaunchParam(objectNavDrawerCombo.getToken(groupPosition, childPosition), groupPosition, childPosition)
            getContent()
            true
        }
        getContent()
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
        favListParm = UtilityFavorites.setupMenu(this, UIPreferences.spcMesoFav, displayData.param[curImg], prefToken)
        invalidateOptionsMenu()
        if (UIPreferences.spcMesoFav.contains(":" + displayData.param[curImg] + ":"))
            star.setIcon(GlobalVariables.STAR_ICON)
        else
            star.setIcon(GlobalVariables.STAR_OUTLINE_ICON)
        FutureVoid(this,
                {
                    (0 until numPanes).forEach {
                        displayData.bitmaps[it] = UtilitySpcMesoInputOutput.getImage(this, displayData.param[it], sector)
                    }
                }
        )  {
            (0 until numPanes).forEach {
                    if (numPanes > 1) {
                        UtilityImg.resizeViewAndSetImage(this, displayData.bitmaps[it], displayData.image[it].get() as ImageView)
                    } else {
                        displayData.image[it].set(displayData.bitmaps[it])
                    }
                    displayData.image[it].setMaxZoom(4.0f)
            }
            displayData.image.forEachIndexed { index, touchImage ->
                touchImage.firstRun(prefModel + numPanes.toString() + index.toString())
            }
            imageLoaded = true
            Utility.writePref(this, prefParam + curImg, displayData.param[curImg])
            Utility.writePref(this, prefParamLabel + curImg, displayData.paramLabel[curImg])
            setTitle()
        }
    }

    private fun getAnimate(frames: Int) {
        displayData.objectAnimates.forEachIndexed { index, objectAnimate ->
            objectAnimate.animateClicked({}) { UtilitySpcMesoInputOutput.getAnimation(
                    displayData.param[index],
                    sector,
                    frames)}
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (objectNavDrawerCombo.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_toggleRadar -> {
                if (showRadar) {
                    Utility.writePref(this, prefModel + "_SHOW_RADAR", "false")
                    menuRadar.title = menuRadarStr
                    showRadar = false
                } else {
                    showRadar = true
                    menuRadar.title = on + menuRadarStr
                    Utility.writePref(this, prefModel + "_SHOW_RADAR", "true")
                }
                getContent()
            }
            R.id.action_toggleTopography -> {
                if (showTopography) {
                    Utility.writePref(this, prefModel + "_SHOW_TOPO", "false")
                    menuTopography.title = menuTopographyStr
                    showTopography = false
                } else {
                    showTopography = true
                    menuTopography.title = on + menuTopographyStr
                    Utility.writePref(this, prefModel + "_SHOW_TOPO", "true")
                }
                getContent()
            }
            R.id.action_toggleCounty -> {
                if (showCounty) {
                    Utility.writePref(this, prefModel + "_SHOW_COUNTY", "false")
                    menuCounty.title = menuCountyStr
                    showCounty = false
                } else {
                    showCounty = true
                    menuCounty.title = on + menuCountyStr
                    Utility.writePref(this, prefModel + "_SHOW_COUNTY", "true")
                }
                getContent()
            }
            R.id.action_toggleSPCOutlook -> {
                if (showOutlook) {
                    Utility.writePref(this, prefModel + "_SHOW_OUTLOOK", "false")
                    menuOutlook.title = menuOutlookStr
                    showOutlook = false
                } else {
                    showOutlook = true
                    menuOutlook.title = on + menuOutlookStr
                    Utility.writePref(this, prefModel + "_SHOW_OUTLOOK", "true")
                }
                getContent()
            }
            R.id.action_toggleWatWarn -> {
                if (showWatwarn) {
                    Utility.writePref(this, prefModel + "_SHOW_WATWARN", "false")
                    menuWatwarn.title = menuWatwarnStr
                    showWatwarn = false
                } else {
                    showWatwarn = true
                    menuWatwarn.title = on + menuWatwarnStr
                    Utility.writePref(this, prefModel + "_SHOW_WATWARN", "true")
                }
                getContent()
            }
            R.id.action_mslp -> setAndLaunchParam("pmsl", 1, 0)
            R.id.action_ttd -> setAndLaunchParam("ttd", 1, 1)
            R.id.action_thea -> setAndLaunchParam("thea", 1, 3)
            R.id.action_bigsfc -> setAndLaunchParam("bigsfc", 0, 0)
            R.id.action_rgnlrad -> setAndLaunchParam("rgnlrad", 0, 2)
            R.id.action_1kmv -> setAndLaunchParam("1kmv", 0, 1)
            R.id.action_300mb -> setAndLaunchParam("300mb", 2, 5)
            R.id.action_500mb -> setAndLaunchParam("500mb", 2, 4)
            R.id.action_700mb -> setAndLaunchParam("700mb", 2, 3)
            R.id.action_850mb2 -> setAndLaunchParam("850mb", 2, 2)
            R.id.action_850mb -> setAndLaunchParam("850mb", 2, 1)
            R.id.action_925mb -> setAndLaunchParam("925mb", 2, 0)
            R.id.action_muli -> setAndLaunchParam("muli", 3, 5)
            R.id.action_pwtr -> setAndLaunchParam("pwtr", 8, 0)
            R.id.action_scp -> setAndLaunchParam("scp", 5, 0)
            R.id.action_sigh -> setAndLaunchParam("sigh", 5, 7)
            R.id.action_stpc -> setAndLaunchParam("stpc", 5, 3)
            R.id.action_eshr -> setAndLaunchParam("eshr", 4, 0)
            R.id.action_shr6 -> setAndLaunchParam("shr6", 4, 1)
            R.id.action_srh1 -> setAndLaunchParam("srh1", 4, 7)
            R.id.action_srh3 -> setAndLaunchParam("srh3", 4, 6)
            R.id.action_mucp -> setAndLaunchParam("mucp", 3, 2)
            R.id.action_sbcp -> setAndLaunchParam("sbcp", 3, 0)
            R.id.action_mlcp -> setAndLaunchParam("mlcp", 3, 1)
            R.id.action_laps -> setAndLaunchParam("laps", 3, 6)
            R.id.action_lllr -> setAndLaunchParam("lllr", 3, 7)
            R.id.action_lclh -> setAndLaunchParam("lclh", 3, 9)
            R.id.action_US -> setAndLaunchSector("19")
            R.id.action_MW -> setAndLaunchSector("20")
            R.id.action_NC -> setAndLaunchSector("13")
            R.id.action_C -> setAndLaunchSector("14")
            R.id.action_SC -> setAndLaunchSector("15")
            R.id.action_NE -> setAndLaunchSector("16")
            R.id.action_CE -> setAndLaunchSector("17")
            R.id.action_SE -> setAndLaunchSector("18")
            R.id.action_SW -> setAndLaunchSector("12")
            R.id.action_NW -> setAndLaunchSector("11")
            R.id.action_help -> getHelp()
            R.id.action_multipane -> Route(this, SpcMesoActivity::class.java, INFO, arrayOf("", "2", prefModel))
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
        if (objectNavDrawerCombo.onOptionsItemSelected(item)) {
            return true
        }
        favListParm = UtilityFavorites.setupMenu(this, UIPreferences.spcMesoFav, displayData.param[curImg], prefToken)
        when (item.itemId) {
            R.id.action_product -> ObjectDialogue.generic(this, favListParm, {}) {
                when (it) {
                    1 -> Route.favoriteAdd(this, arrayOf("SPCMESO"))
                    2 -> Route.favoriteRemove(this, arrayOf("SPCMESO"))
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
            R.id.action_share -> {
                if (UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else {
                    var title = UtilitySpcMeso.sectorMap[sector] + " - " + displayData.paramLabel[0]
                    if (numPanes == 1) {
                        UtilityShare.bitmap(this, title, displayData.bitmaps[0])
                    } else {
                        title = UtilitySpcMeso.sectorMap[sector] + " - " + displayData.paramLabel[curImg]
                        UtilityShare.bitmap(this, title, displayData.bitmaps[curImg])
                    }
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        objectNavDrawerCombo.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        objectNavDrawerCombo.onConfigurationChanged(newConfig)
    }

    private fun getHelp() {
        FutureText2(
                this,
                { ("${GlobalVariables.nwsSPCwebsitePrefix}/exper/mesoanalysis/help/help_" + displayData.param[curImg] + ".html").getHtml() },
                { s ->
                    var helpText = s
                    if (helpText.contains("Page Not Found")) {
                        helpText = "Help is not available for this parameter."
                    }
                    ObjectDialogue(this, Utility.fromHtml(helpText))
                }
        )
    }

    private fun setAndLaunchParam(param: String, a: Int, b: Int) {
        displayData.param[curImg] = param
        displayData.paramLabel[curImg] = UtilitySpcMeso.longCodes[a][b]
        getContent()
    }

    private fun setAndLaunchSector(sectorNo: String) {
        displayData.image[0].resetZoom()
        if (numPanes > 1) {
            displayData.image[1].resetZoom()
        }
        sector = sectorNo
        Utility.writePref(this, prefSector, sector)
        getContent()
    }

    override fun onStop() {
        displayData.image.forEachIndexed { index, touchImage ->
            touchImage.imgSavePosnZoom(prefModel + numPanes.toString() + index.toString())
        }
        super.onStop()
    }

    private fun toggleFavorite() {
        UtilityFavorites.toggle(this, displayData.param[curImg], star, prefToken)
    }
}

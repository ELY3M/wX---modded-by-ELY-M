/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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

import android.os.Bundle
import android.content.res.Configuration
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import joshuatee.wx.Extensions.getHtml

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.models.DisplayData
import joshuatee.wx.models.UtilityModels
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.ObjectNavDrawerCombo
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.ui.OnSwipeTouchListener
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.settings.FavAddActivity
import joshuatee.wx.settings.FavRemoveActivity
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityAlertDialog
import joshuatee.wx.util.UtilityFavorites
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare
import kotlinx.coroutines.*

class SPCMesoActivity : VideoRecordActivity(), OnMenuItemClickListener,
    AdapterView.OnItemSelectedListener {

    // native interface to the mobile SPC meso website
    //
    // arg1 - number of panes, 1 or 2
    // arg2 - pref model token and hash lookup

    companion object {
        var INFO: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var animRan = false
    private var showRadar = true
    private var showOutlook = true
    private var showWatwarn = true
    private var showTopography = true
    private var sector = "19"
    // FIXME move to util
    private val sectorMap = mapOf(
        "19" to "US",
        "20" to "MW",
        "13" to "NC",
        "14" to "C",
        "15" to "SC",
        "16" to "NE",
        "17" to "CE",
        "18" to "SE",
        "12" to "SW",
        "11" to "NW"
    )
    private lateinit var menuRadar: MenuItem
    private lateinit var menuOutlook: MenuItem
    private lateinit var menuWatwarn: MenuItem
    private lateinit var menuTopography: MenuItem
    private val menuRadarStr = "Radar"
    private val menuOutlookStr = "SPC Day 1 Outlook"
    private val menuWatwarnStr = "Watches/Warnings"
    private val menuTopographyStr = "Topopgraphy"
    private val on = "(on) "
    private var curImg = 0
    private var imageLoaded = false
    private var firstRun = false
    private var numPanes = 0
    private var favListLabel = listOf<String>()
    private var favListParm = listOf<String>()
    private lateinit var star: MenuItem
    private var prefSector = ""
    private var prefModel = ""
    private lateinit var prefParam: String
    private lateinit var prefParamLabel: String
    private lateinit var sp: ObjectSpinner
    private lateinit var drw: ObjectNavDrawerCombo
    private lateinit var displayData: DisplayData
    private lateinit var contextg: Context

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.spcmeso_top, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        var args = intent.getStringArrayExtra(INFO)
        contextg = this
        if (args == null) {
            args = arrayOf("", "1", "SPCMESO")
        }
        val numPanesStr = args[1]
        numPanes = numPanesStr.toIntOrNull() ?: 0
        if (numPanes == 1) {
            super.onCreate(
                savedInstanceState,
                R.layout.activity_spcmeso,
                R.menu.spcmesomultipane,
                iconsEvenlySpaced = false,
                bottomToolbar = true
            )
        } else {
            super.onCreate(
                savedInstanceState,
                R.layout.activity_spcmeso_multipane,
                R.menu.spcmesomultipane,
                iconsEvenlySpaced = false,
                bottomToolbar = true
            )
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        prefModel = args[2]
        prefSector = prefModel + numPanesStr + "_SECTOR_LAST_USED"
        prefParam = prefModel + numPanesStr + "_PARAM_LAST_USED"
        prefParamLabel = prefModel + numPanesStr + "_PARAM_LAST_USED_LABEL"
        displayData = DisplayData(this, this, numPanes, ObjectSpinner(this as Context))
        displayData.param[0] = "pmsl"
        displayData.paramLabel[0] = "MSL Pressure/Wind"
        if (numPanes > 1) {
            displayData.param[1] = "500mb"
            displayData.paramLabel[1] = "500mb Analysis"
        }
        if (args[0] != "" && numPanes == 1) {
            val tmpArrFav = UtilitySPCMESO.setParamFromFav(args[0])
            displayData.param[0] = tmpArrFav[0]
            displayData.paramLabel[0] = tmpArrFav[1]
        } else {
            (0 until numPanes).forEach {
                displayData.param[it] =
                    Utility.readPref(this, prefParam + it.toString(), displayData.param[it])
                displayData.paramLabel[it] = Utility.readPref(
                    this,
                    prefParamLabel + it.toString(),
                    displayData.paramLabel[it]
                )
            }
        }
        sector = Utility.readPref(this, prefSector, sector)
        showRadar = Utility.readPref(this, prefModel + "_SHOW_RADAR", "false").startsWith("t")
        showOutlook = Utility.readPref(this, prefModel + "_SHOW_OUTLOOK", "false").startsWith("t")
        showWatwarn = Utility.readPref(this, prefModel + "_SHOW_WATWARN", "false").startsWith("t")
        showTopography = Utility.readPref(this, prefModel + "_SHOW_TOPO", "false").startsWith("t")
        val menu = toolbarBottom.menu
        menuRadar = menu.findItem(R.id.action_toggleRadar)
        menuOutlook = menu.findItem(R.id.action_toggleSPCOutlook)
        menuWatwarn = menu.findItem(R.id.action_toggleWatWarn)
        menuTopography = menu.findItem(R.id.action_toggleTopography)
        if (numPanes < 2) {
            menu.findItem(R.id.action_img1).isVisible = false
            menu.findItem(R.id.action_img2).isVisible = false
        } else
            menu.findItem(R.id.action_multipane).isVisible = false
        star = menu.findItem(R.id.action_fav)
        star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        if (showRadar)
            menuRadar.title = on + menuRadarStr
        if (showOutlook)
            menuOutlook.title = on + menuOutlookStr
        if (showWatwarn)
            menuWatwarn.title = on + menuWatwarnStr
        if (showTopography)
            menuTopography.title = on + menuTopographyStr
        if (numPanes == 1) {
            displayData.img[0].setOnTouchListener(object : OnSwipeTouchListener(this) {
                override fun onSwipeLeft() {
                    if (displayData.img[curImg].currentZoom < 1.01f) {
                        UtilitySPCMESO.moveForward(sp)
                    }
                }

                override fun onSwipeRight() {
                    if (displayData.img[curImg].currentZoom < 1.01f) {
                        UtilitySPCMESO.moveBack(sp)
                    }
                }
            })
        }
        favListLabel = UtilityFavorites.setupFavMenuSPCMESO(
            MyApplication.spcmesoLabelFav,
            displayData.paramLabel[curImg]
        )
        favListParm = UtilityFavorites.setupFavMenuSPCMESO(
            MyApplication.spcmesoFav,
            displayData.param[curImg]
        )
        sp = ObjectSpinner(this, this, this, R.id.spinner1, favListLabel)
        UtilitySPCMESO.createData()
        drw = ObjectNavDrawerCombo(
            this,
            UtilitySPCMESO.groups,
            UtilitySPCMESO.longCodes,
            UtilitySPCMESO.shortCodes,
            this,
            ""
        )
        drw.listView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            drw.drawerLayout.closeDrawer(drw.listView)
            displayData.param[curImg] = drw.getToken(groupPosition, childPosition)
            displayData.paramLabel[curImg] = drw.getLabel(groupPosition, childPosition)
            Utility.writePref(this, prefParam + curImg, displayData.param[curImg])
            Utility.writePref(this, prefParamLabel + curImg, displayData.paramLabel[curImg])
            refreshSpinner()
            true
        }
    }

    override fun onRestart() {
        favListLabel = UtilityFavorites.setupFavMenuSPCMESO(
            MyApplication.spcmesoLabelFav,
            displayData.paramLabel[curImg]
        )
        favListParm = UtilityFavorites.setupFavMenuSPCMESO(
            MyApplication.spcmesoFav,
            displayData.param[curImg]
        )
        sp.refreshData(contextg, favListLabel)
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        if (MyApplication.spcmesoFav.contains(":" + displayData.param[curImg] + ":"))
            star.setIcon(MyApplication.STAR_ICON)
        else
            star.setIcon(MyApplication.STAR_OUTLINE_ICON)

        withContext(Dispatchers.IO) {
            (0 until numPanes).forEach {
                displayData.bitmap[it] =
                    UtilitySPCMESOInputOutput.getImage(contextg, displayData.param[it], sector)
            }
        }
        (0 until numPanes).forEach {
            if (numPanes > 1) {
                UtilityImg.resizeViewSetImgByHeight(displayData.bitmap[it], displayData.img[it])
            } else {
                displayData.img[it].setImageBitmap(displayData.bitmap[it])
            }
            displayData.img[it].setMaxZoom(4f)
            animRan = false
        }
        if (!firstRun) {
            (0 until numPanes).forEach {
                displayData.img[it].setZoom(
                    Utility.readPref(
                        contextg,
                        prefModel + numPanes + it.toString() + "_ZOOM",
                        1.0f
                    ),
                    Utility.readPref(contextg, prefModel + numPanes + it.toString() + "_X", 0.5f),
                    Utility.readPref(contextg, prefModel + numPanes + it.toString() + "_Y", 0.5f)
                )
            }
            firstRun = true
        }
        imageLoaded = true
        if (numPanes > 1) {
            UtilityModels.setSubtitleRestoreIMGXYZOOM(
                displayData.img, toolbar, "(" + (curImg + 1) + ")"
                        + displayData.paramLabel[0] + "/" + displayData.paramLabel[1]
            )
        }
    }

    private fun getAnimate(frames: Int) = GlobalScope.launch(uiDispatcher) {
        withContext(Dispatchers.IO) {
            (0 until numPanes).forEach {
                displayData.animDrawable[it] = UtilitySPCMESOInputOutput.getAnimation(
                    contextg,
                    sector,
                    displayData.param[it],
                    frames
                )
            }
        }
        (0 until numPanes).forEach {
            displayData.img[it].setImageDrawable(displayData.animDrawable[it])
            displayData.animDrawable[it].isOneShot = false
            displayData.animDrawable[it].start()
        }
        animRan = true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            // FIXME consolidate code below
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
            R.id.action_300mb -> setAndLaunchParam("300mb", 2, 4)
            R.id.action_500mb -> setAndLaunchParam("500mb", 2, 3)
            R.id.action_700mb -> setAndLaunchParam("700mb", 2, 2)
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
            R.id.action_multipane -> ObjectIntent(
                this,
                SPCMesoActivity::class.java,
                SPCMesoActivity.INFO,
                arrayOf("", "2", prefModel)
            )
            R.id.action_fav -> toggleFavorite()
            // FIXME consolidate code below
            R.id.action_img1 -> {
                curImg = 0
                UtilityModels.setSubtitleRestoreIMGXYZOOM(
                    displayData.img,
                    toolbar,
                    "(" + (curImg + 1) + ")" + displayData.paramLabel[0] + "/" + displayData.paramLabel[1]
                )
            }
            R.id.action_img2 -> {
                curImg = 1
                UtilityModels.setSubtitleRestoreIMGXYZOOM(
                    displayData.img,
                    toolbar,
                    "(" + (curImg + 1) + ")" + displayData.paramLabel[0] + "/" + displayData.paramLabel[1]
                )
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drw.actionBarDrawerToggle.onOptionsItemSelected(item)) return true
        when (item.itemId) {
            R.id.action_a6 -> getAnimate(6)
            R.id.action_a12 -> getAnimate(12)
            R.id.action_a18 -> getAnimate(18)
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20) {
                    checkOverlayPerms()
                } else {
                    var title = sectorMap[sector] + " - " + displayData.paramLabel[0]
                    if (animRan) {
                        UtilityShare.shareAnimGif(this, title, displayData.animDrawable[0])
                    } else {
                        if (numPanes == 1) {
                            UtilityShare.shareBitmap(this, title, displayData.bitmap[0])
                        } else {
                            title = sectorMap[sector] + " - " + displayData.paramLabel[curImg]
                            UtilityShare.shareText(this, title, "", displayData.bitmap[curImg])
                        }
                    }
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drw.actionBarDrawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drw.actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    private fun getHelp() = GlobalScope.launch(uiDispatcher) {
        var helpText = withContext(Dispatchers.IO) {
            ("${MyApplication.nwsSPCwebsitePrefix}/exper/mesoanalysis/help/help_" + displayData.param[curImg] + ".html").getHtml()
        }
        if (helpText.contains("Page Not Found")) {
            helpText = "Help is not available for this parameter."
        }
        showHelpTextDialog(Utility.fromHtml(helpText))
    }

    private fun showHelpTextDialog(help_str: String) {
        UtilityAlertDialog.showHelpText(help_str, this)
    }

    private fun setAndLaunchParam(paramStr: String, a: Int, b: Int) {
        displayData.param[curImg] = paramStr
        displayData.paramLabel[curImg] = UtilitySPCMESO.longCodes[a][b]
        Utility.writePref(this, prefParam + curImg, displayData.param[curImg])
        Utility.writePref(this, prefParamLabel + curImg, displayData.paramLabel[curImg])
        refreshSpinner()
        getContent()
    }

    private fun setAndLaunchSector(sectorNo: String) {
        displayData.img[0].resetZoom()
        if (numPanes > 1)
            displayData.img[1].resetZoom()
        sector = sectorNo
        Utility.writePref(this, prefSector, sector)
        getContent()
    }

    override fun onStop() {
        if (imageLoaded) {
            (0 until numPanes).forEach {
                UtilityImg.imgSavePosnZoom(
                    this,
                    displayData.img[it],
                    prefModel + numPanes.toString() + it.toString()
                )
            }
        }
        super.onStop()
    }

    private fun toggleFavorite() {
        UtilityFavorites.toggleFavoriteSPCMESO(
            this,
            displayData.param[curImg],
            displayData.paramLabel[curImg],
            star
        )
        favListLabel = UtilityFavorites.setupFavMenuSPCMESO(
            MyApplication.spcmesoLabelFav,
            displayData.paramLabel[curImg]
        )
        favListParm = UtilityFavorites.setupFavMenuSPCMESO(
            MyApplication.spcmesoFav,
            displayData.param[curImg]
        )
        sp.refreshData(contextg, favListLabel)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        when (parent.id) {
            R.id.spinner1 -> {
                when (pos) {
                    1 -> ObjectIntent(
                        this,
                        FavAddActivity::class.java,
                        FavAddActivity.TYPE,
                        arrayOf("SPCMESO")
                    )
                    2 -> ObjectIntent(
                        this,
                        FavRemoveActivity::class.java,
                        FavRemoveActivity.TYPE,
                        arrayOf("SPCMESO")
                    )
                    else -> {
                        if (favListParm.count() > pos && favListLabel.count() > pos) {
                            displayData.param[curImg] = favListParm[pos]
                            displayData.paramLabel[curImg] = favListLabel[pos]
                            Utility.writePref(this, prefParam + curImg, displayData.param[curImg])
                            Utility.writePref(
                                this,
                                prefParamLabel + curImg,
                                displayData.paramLabel[curImg]
                            )
                            getContent()
                        }
                    }
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    private fun refreshSpinner() {
        favListLabel = UtilityFavorites.setupFavMenuSPCMESO(
            MyApplication.spcmesoLabelFav,
            displayData.paramLabel[curImg]
        )
        favListParm = UtilityFavorites.setupFavMenuSPCMESO(
            MyApplication.spcmesoFav,
            displayData.param[curImg]
        )
        sp.refreshData(contextg, favListLabel)
    }
}

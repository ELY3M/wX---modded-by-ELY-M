/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

    This file is part of wX.

    wX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    wX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with wX.  If not, see <http://www.gnu.org/licenses/>.

 */
//modded by ELY M.   

package joshuatee.wx.radar

import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import joshuatee.wx.R
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility

class WXGLRadarActivityMultiPane : VideoRecordActivity(), OnMenuItemClickListener {

    //
    // This activity is a general purpose viewer of nexrad
    // nexrad data is downloaded from NWS TGFTP, decoded and drawn using OpenGL ES
    // Unlike the traditional viewer this one shows multiple nexrad radars at the same time
    // nexrad sites, products, zoom and x/y are saved on stop and restored on start
    //
    // Arguments
    // 1: RID
    // 2: State NO LONGER NEEDED
    // 3: number of panes
    // 4: coming from single pane
    //

    companion object {
        const val RID = ""
    }

    private var landScape = false
    private var nexradArguments = NexradArgumentsMultipane()
    private lateinit var nexradState: NexradStatePane
    private lateinit var nexradSubmenu: NexradSubmenu
    private lateinit var nexradLongPressMenu: NexradLongPressMenu
    private lateinit var nexradUI: NexradUI
    private lateinit var nexradAnimation: NexradAnimation

    override fun onCreate(savedInstanceState: Bundle?) {
        nexradArguments.process(intent.getStringArrayExtra(RID))
        landScape = UtilityUI.isLandScape(this)
        var heightDivider = 2
        //
        // determine correct layout to use
        //
        heightDivider = setupLayout(savedInstanceState, nexradArguments.numberOfPanes, heightDivider)
        val widthDivider = if (nexradArguments.numberOfPanes == 4) {
            2
        } else if (nexradArguments.numberOfPanes == 2 && landScape) {
            2
        } else {
            1
        }
        nexradState = NexradStatePane(this,
                nexradArguments.numberOfPanes,
                listOf(R.id.rl1, R.id.rl2, R.id.rl3, R.id.rl4),
                widthDivider,
                heightDivider)
        nexradState.setupMultiPaneObjects(nexradArguments)
        nexradSubmenu = NexradSubmenu(objectToolbarBottom, nexradState)
        nexradUI = NexradUI(this, nexradState, nexradSubmenu, nexradArguments, ::getContentParallel)
        nexradLongPressMenu = NexradLongPressMenu(this, nexradState, nexradArguments, nexradUI::longPressRadarSiteSwitch)
        nexradAnimation = NexradAnimation(this, nexradState, nexradUI)
        nexradState.initGlView(nexradLongPressMenu.changeListener)
        nexradState.readPreferencesMultipane(this, nexradArguments)
        getContentParallel()
    }

    private fun setupLayout(savedInstanceState: Bundle?, numberOfPanes: Int, heightDividerF: Int): Int {
        var heightDivider = heightDividerF
        val layoutType: Int
        if (numberOfPanes == 2) {
            if (landScape) {
                layoutType = if (UtilityUI.isThemeAllWhite())
                    R.layout.activity_uswxoglmultipane_immersive_landscape_white
                else
                    R.layout.activity_uswxoglmultipane_immersive_landscape
                heightDivider = 1
            } else {
                layoutType = if (UtilityUI.isThemeAllWhite())
                    R.layout.activity_uswxoglmultipane_white
                else {
                    R.layout.activity_uswxoglmultipane
                }
            }
            super.onCreate(savedInstanceState, layoutType, R.menu.uswxoglradarmultipane, bottomToolbar = true)
        } else {
            layoutType = if (UtilityUI.isThemeAllWhite())
                R.layout.activity_uswxoglmultipane_quad_immersive_white
            else
                R.layout.activity_uswxoglmultipane_quad_immersive
            super.onCreate(savedInstanceState, layoutType, R.menu.uswxoglradarmultipane, bottomToolbar = true)
        }
        objectToolbarBottom.connect(this)
        return heightDivider
    }

    override fun onRestart() {
        nexradUI.onRestart()
        nexradSubmenu.setAnimateToPlay()
        nexradState.wxglTextObjects.forEach {
            it.initializeLabels(this)
            it.addLabels()
        }
        getContentParallel()
        super.onRestart()
    }

    private fun getContent(z: Int) {
        nexradUI.getContentPrepMultiPane(z)
        initGeom(z)
        FutureVoid({
            NexradDraw.plotRadar(nexradState.wxglRenders[z], nexradUI::getGPSFromDouble, nexradState::getLatLon, false)
        }) {
            nexradState.showViews()
            nexradState.draw(z)
            nexradUI.setSubTitleMultiPane()
            NexradRenderUI.updateLastRadarTime(this)
            if (RadarPreferences.wxoglCenterOnLocation) {
                nexradState.wxglSurfaceViews[z].resetView()
            }
        }
        NexradLayerDownload.download(
                this,
                nexradState.wxglRenders[z],
                nexradState.wxglSurfaceViews[z],
                nexradState.wxglTextObjects,
                nexradUI::setTitleWithWarningCountsMultiPane)
    }

    private fun initGeom(z: Int) {
        NexradDraw.initGeom(z,
                nexradState.oldRadarSites,
                nexradState.wxglRenders,
                nexradState.wxglTextObjects,
                nexradUI.objectImageMap,
                nexradState.wxglSurfaceViews,
                nexradUI::getGPSFromDouble,
                nexradState::getLatLon,
                false,
                nexradUI.settingsVisited
        )
        nexradUI.settingsVisited = false
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        UtilityUI.immersiveMode(this)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        UtilityUI.immersiveMode(this)
        if (nexradUI.inOglAnim && (item.itemId != R.id.action_fav) && (item.itemId != R.id.action_share) && (item.itemId != R.id.action_tools)) {
            nexradUI.stopAnimationAndGetContent()
            if (item.itemId == R.id.action_a) {
                return true
            }
        }
        when (item.itemId) {
            R.id.action_help -> NexradRenderUI.showHelp(this)
            R.id.action_share -> startScreenRecord()
            R.id.action_settings -> {
                nexradUI.settingsVisited = true; Route.settingsRadar(this)
            }

            R.id.action_radar_markers -> Route.image(this, "raw:radar_legend", "Radar Markers")
            R.id.action_radar_site_status_l3 -> Route.webView(this, "http://radar3pub.ncep.noaa.gov", resources.getString(R.string.action_radar_site_status_l3), "extended")
            R.id.action_radar_site_status_l2 -> Route.webView(this, "http://radar2pub.ncep.noaa.gov", resources.getString(R.string.action_radar_site_status_l2), "extended")
            R.id.action_radar1 -> switchRadar(0)
            R.id.action_radar2 -> switchRadar(1)
            R.id.action_radar3 -> switchRadar(2)
            R.id.action_radar4 -> switchRadar(3)
            R.id.action_n0q, R.id.action_n0q_menu -> nexradState.getReflectivity(::getContentIntelligent)
            R.id.action_n0u, R.id.action_n0u_menu -> nexradState.getVelocity(::getContentIntelligent)
            R.id.action_n0b -> changeProduct("N" + nexradState.tilt + "B")
            R.id.action_n0g -> changeProduct("N" + nexradState.tilt + "G")
            R.id.action_n0s -> changeProduct("N" + nexradState.tilt + "S")
            R.id.action_net -> changeProduct("EET")
            R.id.action_N0X -> changeProduct("N" + nexradState.tilt + "X")
            R.id.action_N0C -> changeProduct("N" + nexradState.tilt + "C")
            R.id.action_N0K -> changeProduct("N" + nexradState.tilt + "K")
            R.id.action_H0C -> changeProduct("H" + nexradState.tilt + "C")
            R.id.action_about -> nexradUI.showRadarScanInfo()
            R.id.action_vil -> changeProduct("DVL")
            R.id.action_dsp -> changeProduct("DSA")
            R.id.action_daa -> changeProduct("DAA")
            R.id.action_nsw -> changeProduct("NSW")
            R.id.action_ncr -> changeProduct("NCR")
            R.id.action_ncz -> changeProduct("NCZ")
            //I need those!  ELY M. 
	    R.id.action_et -> changeProduct("ET")
            R.id.action_vil -> changeProduct("VIL")
            R.id.action_l2vel -> changeProduct("L2VEL")
            R.id.action_l2ref -> changeProduct("L2REF")
            R.id.action_tilt1 -> changeTilt("0")
            R.id.action_tilt2 -> changeTilt("1")
            R.id.action_tilt3 -> changeTilt("2")
            R.id.action_tilt4 -> changeTilt("3")
            R.id.action_a12 -> animateRadar(12)
            R.id.action_a18 -> animateRadar(18)
            R.id.action_a6_sm -> animateRadar(6)
            R.id.action_a -> animateRadar(To.int(RadarPreferences.uiAnimIconFrames))
            R.id.action_a36 -> animateRadar(36)
            R.id.action_a72 -> animateRadar(72)
            R.id.action_a144 -> animateRadar(144)
            R.id.action_a3 -> animateRadar(3)
            R.id.action_fav -> nexradUI.pauseButtonTapped()
            R.id.action_radar_4 -> startFourPaneNexrad()
            R.id.action_TDWR -> nexradUI.showTdwrDialog()
            //elys mod
            //R.id.action_ridmap -> nexradUI.showMap()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun startFourPaneNexrad() {
        nexradState.writePreferencesMultipane(this, nexradArguments.doNotSavePref)
        Route.radarMultiPane(this, arrayOf(joshuatee.wx.settings.Location.rid, "", "4", "true"))
    }

    private fun startScreenRecord() {
        if (UIPreferences.recordScreenShare) {
            // TODO FIXME
//            showDistanceTool = "true"
            checkOverlayPerms()
        } else {
            NexradRenderUI.showImageForShare(this, "1", nexradState.render.state.rid, nexradState.render.state.product)
        }
    }

    private fun animateRadar(frameCount: Int) {
        nexradSubmenu.setAnimateToStop()
        nexradSubmenu.setAnimateToPause()
        nexradAnimation.run(frameCount)
    }

    private fun changeTilt(tiltStr: String) {
        nexradState.changeTilt(tiltStr)
        getContent(nexradState.curRadar)
    }

    private fun changeProduct(product: String) {
        nexradState.product = product
        getContentIntelligent()
    }

    // user can select which radar # they want to control from the submenu
    private fun switchRadar(radarNumber: Int) {
        nexradState.curRadar = radarNumber
        nexradSubmenu.adjustTiltAndProductMenus()
        nexradUI.setToolbarTitle()
    }

    override fun onStop() {
        super.onStop()
        nexradState.writePreferencesMultipaneOnStop(this, nexradArguments.doNotSavePref)
        nexradUI.onStop()
    }

    override fun onPause() {
        nexradUI.stopRepeatingTask()
        nexradState.onPause()
        super.onPause()
    }

    override fun onResume() {
        nexradUI.checkForAutoRefresh()
        nexradState.onResume()
        super.onResume()
    }

    @Synchronized
    private fun getContentParallel() {
        nexradState.wxglRenders.indices.forEach {
            getContent(it)
        }
    }

    private fun getContentIntelligent() {
        if (RadarPreferences.dualpaneshareposn) {
            getContentParallel()
        } else {
            getContent(nexradState.curRadar)
        }
    }

    private fun toggleAnimate() {
        if (nexradUI.inOglAnim) {
            nexradUI.stopAnimationAndGetContent()
        } else {
            animateRadar(To.int(RadarPreferences.uiAnimIconFrames))
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_L -> if (event.isCtrlPressed) nexradUI.showMap()
            KeyEvent.KEYCODE_M -> if (event.isCtrlPressed) toolbarBottom.showOverflowMenu()
            KeyEvent.KEYCODE_A -> if (event.isCtrlPressed) toggleAnimate()
            KeyEvent.KEYCODE_R -> if (event.isCtrlPressed) nexradState.getReflectivity(::getContentIntelligent)
            KeyEvent.KEYCODE_V -> if (event.isCtrlPressed) nexradState.getVelocity(::getContentIntelligent)
            KeyEvent.KEYCODE_SLASH -> if (event.isAltPressed) ObjectDialogue(this, Utility.showRadarShortCuts())
            KeyEvent.KEYCODE_REFRESH -> getContentIntelligent()
            KeyEvent.KEYCODE_DPAD_UP -> if (event.isCtrlPressed) {
                //wxglSurfaceViews.forEach{ it.zoomOutByKey(numberOfPanes.toFloat()) }
            } else {
                nexradState.wxglSurfaceViews.forEach {
                    it.onScrollByKeyboard(0.0f, -20.0f)
                }
            }

            KeyEvent.KEYCODE_DPAD_DOWN -> if (event.isCtrlPressed) {
                //wxglSurfaceViews.forEach{ it.zoomInByKey(numberOfPanes.toFloat()) }
            } else {
                nexradState.wxglSurfaceViews.forEach {
                    it.onScrollByKeyboard(0.0f, 20.0f)
                }
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> nexradState.wxglSurfaceViews.forEach { it.onScrollByKeyboard(-20.0f, 0.0f) }
            KeyEvent.KEYCODE_DPAD_RIGHT -> nexradState.wxglSurfaceViews.forEach { it.onScrollByKeyboard(20.0f, 0.0f) }
            else -> return super.onKeyUp(keyCode, event)
        }
        return true
    }
}

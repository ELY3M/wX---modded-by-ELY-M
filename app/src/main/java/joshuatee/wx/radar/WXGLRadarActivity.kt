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
import androidx.core.app.NavUtils
import androidx.core.app.TaskStackBuilder
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.FavoriteType
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.FutureText2
import joshuatee.wx.objects.Route
import joshuatee.wx.safeGet
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.To
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityFavorites
//elys mod
import joshuatee.wx.misc.WebView
import joshuatee.wx.util.UtilityLog

class WXGLRadarActivity : VideoRecordActivity(), OnMenuItemClickListener {

    //
    // This activity is a general purpose viewer of nexrad and mosaic content
    // nexrad data is downloaded from NWS FTP, decoded and drawn using OpenGL ES
    //
    // Arguments
    // 1: RID
    // 2: State - NO LONGER NEEDED
    // 3: Product ( optional )
    // 4: Fixed site ( simply having a 4th arg will prevent remember location from working )
    // 4: URL String ( optional, archive from SPC Storm reports long-press (deprecated))
    // 5: X ( optional, archive )
    // 6: Y ( optional, archive )
    //

    companion object {
        var RID = ""
        var dspLegendMax = 0.0f
        var velMax: Short = 120
        var velMin: Short = -120
        var spotterId = ""
        var spotterShowSelected = false
    }
    //elys mod
    private var radarHidden = false

    private var nexradArguments = NexradArgumentsSinglePane()
    private lateinit var nexradState: NexradStatePane
    private lateinit var nexradSubmenu: NexradSubmenu
    private lateinit var nexradLongPressMenu: NexradLongPressMenu
    private lateinit var nexradUI: NexradUI
    private lateinit var nexradColorLegend: NexradColorLegend
    private lateinit var nexradAnimation: NexradAnimation

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.uswxoglradar_top, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_sector).title = nexradState.radarSitesForFavorites.safeGet(0).split(" ")[0]
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        nexradArguments.process(intent.getStringArrayExtra(RID))
        setupLayout(savedInstanceState)
        nexradState = NexradStatePane(this, 1, listOf(R.id.rl), 1, 1)
        nexradSubmenu = NexradSubmenu(objectToolbarBottom, nexradState)
        nexradUI = NexradUI(this, nexradState, nexradSubmenu, nexradArguments, ::getContent)
        nexradLongPressMenu = NexradLongPressMenu(this, nexradState, nexradArguments, nexradUI::longPressRadarSiteSwitch)
        nexradState.initGlView(nexradLongPressMenu.changeListener)
        nexradState.readPreferences(nexradArguments)
        nexradColorLegend = NexradColorLegend(this, nexradState)
        nexradAnimation = NexradAnimation(this, nexradState, nexradUI) // removed nexradArguments as last arg
        getContent()
    }

    private fun setupLayout(savedInstanceState: Bundle?) {
        if (UtilityUI.isThemeAllWhite()) {
            super.onCreate(savedInstanceState, R.layout.activity_uswxogl_white, R.menu.uswxoglradar, bottomToolbar = true)
        } else {
            super.onCreate(savedInstanceState, R.layout.activity_uswxogl, R.menu.uswxoglradar, bottomToolbar = true)
        }
        objectToolbarBottom.connect(this)
    }

    override fun onRestart() {
        nexradUI.onRestart()
        nexradSubmenu.setStarButton()
        nexradSubmenu.setAnimateToPlay()
        nexradState.wxglTextObjects.forEach {
            it.initializeLabels(this)
            it.addLabels()
        }
        getContent()
        super.onRestart()
    }

    @Synchronized
    private fun getContent() {
        nexradUI.getContentPrep()
        initGeom()
        FutureVoid({
            NexradDraw.plotRadar(
                    nexradState.render,
                    nexradUI::getGPSFromDouble,
                    nexradState::getLatLon,
                    nexradArguments.archiveMode,
                    nexradArguments.urlStr)
        }, {
            nexradState.draw()
            nexradUI.setSubTitle()
            NexradRenderUI.updateLastRadarTime(this)
            nexradColorLegend.updateAfterDownload()
            nexradState.oldProd = nexradState.product
        })
        NexradLayerDownload.download(
                this,
                nexradState.render,
                nexradState.surface,
                nexradState.wxglTextObjects,
                nexradUI::setTitleWithWarningCounts)
    }

    private fun initGeom() {
        NexradDraw.initGeom(
                0,
                nexradState.oldRadarSites,
                nexradState.wxglRenders,
                nexradState.wxglTextObjects,
                nexradUI.objectImageMap,
                nexradState.wxglSurfaceViews,
                nexradUI::getGPSFromDouble,
                nexradState::getLatLon,
                nexradArguments.archiveMode,
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
            if (item.itemId == R.id.action_n0q || item.itemId == R.id.action_n0u) {
                nexradUI.stopAnimation()
            } else {
                nexradUI.stopAnimationAndGetContent()
            }
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
            R.id.action_radar_2 -> showMultipaneRadar(2)
            R.id.action_radar_4 -> showMultipaneRadar(4)
            R.id.action_radar_site_status_l3 -> Route.webView(this, "http://radar3pub.ncep.noaa.gov", resources.getString(R.string.action_radar_site_status_l3), "extended")
            R.id.action_radar_site_status_l2 -> Route.webView(this, "http://radar2pub.ncep.noaa.gov", resources.getString(R.string.action_radar_site_status_l2), "extended")
            R.id.action_n0q, R.id.action_n0q_menu -> nexradState.getReflectivity(::getContent)
            R.id.action_n0u, R.id.action_n0u_menu -> nexradState.getVelocity(::getContent)
            R.id.action_n0b -> changeProduct("N" + nexradState.tilt + "B")
            R.id.action_n0g -> changeProduct("N" + nexradState.tilt + "G")
            R.id.action_tz0 -> changeProduct("TZ${nexradState.tilt}")
            R.id.action_tv0 -> changeProduct("TV${nexradState.tilt}")
            R.id.action_tzl -> changeProduct("TZL")
            R.id.action_n0s -> changeProduct("N" + nexradState.tilt + "S")
            R.id.action_net -> changeProduct("EET")
            R.id.action_N0X -> changeProduct("N" + nexradState.tilt + "X")
            R.id.action_N0C -> changeProduct("N" + nexradState.tilt + "C")
            R.id.action_N0K -> changeProduct("N" + nexradState.tilt + "K")
            R.id.action_H0C -> changeProduct("H" + nexradState.tilt + "C")
            R.id.action_radar_hide -> hideRadar()
            R.id.action_legend -> nexradColorLegend.show()
            R.id.action_about -> nexradUI.showRadarScanInfo()
            R.id.action_dvl -> changeProduct("DVL")
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
            R.id.action_NVW -> getContentVwp()
            R.id.action_fav -> nexradUI.actionToggleFavorite()
            R.id.action_TDWR -> nexradUI.showTdwrDialog()
            //elys mod
            //R.id.action_ridmap -> nexradUI.showMap()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun startScreenRecord() {
        if (UIPreferences.recordScreenShare) {
            showDistanceTool = "true"
            checkOverlayPerms()
        } else {
            NexradRenderUI.showImageForShare(this, "0", nexradState.render.state.rid, nexradState.render.state.product)
        }
    }

    private fun animateRadar(frameCount: Int) {
        nexradSubmenu.setAnimateToStop()
        nexradSubmenu.setAnimateToPause()
        nexradAnimation.run(frameCount)
    }

    private fun changeTilt(tiltStr: String) {
        nexradState.changeTilt(tiltStr)
        getContent()
    }

    private fun changeProduct(product: String) {
        nexradState.product = product
        getContent()
    }

    override fun onStop() {
        super.onStop()
        nexradState.writePreferences(this, nexradArguments)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        nexradState.radarSitesForFavorites = UtilityFavorites.setupMenu(this, nexradState.radarSite, FavoriteType.RID)
        when (item.itemId) {
            R.id.action_sector -> {
                ObjectDialogue.generic(this, nexradState.radarSitesForFavorites, ::getContent) {
                    if (nexradState.radarSitesForFavorites.size > 2) {
                        nexradUI.stopAnimation()
                        when (it) {
                            1 -> Route.favoriteAdd(this, FavoriteType.RID)
                            2 -> Route.favoriteRemove(this, FavoriteType.RID)
                            else -> {
                                if (nexradState.radarSitesForFavorites[it] == " ") {
                                    nexradState.radarSite = joshuatee.wx.settings.Location.rid
                                } else {
                                    nexradState.radarSite = nexradState.radarSitesForFavorites[it].split(" ").getOrNull(0)
                                            ?: ""
                                }
                                nexradUI.mapSwitch(nexradState.radarSite)
                            }
                        }
                    }
                    UtilityUI.immersiveMode(this)
                }
            }

            android.R.id.home -> {
                if (Utility.readPref(this, "LAUNCH_TO_RADAR", "false") == "false") {
                    NavUtils.navigateUpFromSameTask(this)
                } else {
                    navigateUp()
                }
            }

            else -> super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateUp() {
        val upIntent = NavUtils.getParentActivityIntent(this)
        if (NavUtils.shouldUpRecreateTask(this, upIntent!!) || isTaskRoot) {
            TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities()
        } else {
            NavUtils.navigateUpTo(this, upIntent)
        }
    }

/* orinigal 
    private fun getContentVwp() {
        FutureText2(
                { NexradLevel3TextProduct.getVwp(nexradState.radarSite) })
        { data -> Route.text(this, data, nexradState.radarSite + " VAD Wind Profile") }
    }
*/    
    //elys mod
    private fun getContentVwp() {
        var vmpurl = "https://weather.cod.edu/satrad/nexrad/index.php?type="+nexradState.radarSite+"-NVW"
        Route(this@WXGLRadarActivity, WebView::class.java, WebView.URL, arrayOf(vmpurl, nexradState.radarSite + " VAD Wind Profile"))
    }

    private fun showMultipaneRadar(numberOfPanes: Int) {
        if (!nexradArguments.fixedSite) {
            nexradState.writePreferences(this, nexradArguments)
            Route.radarMultiPane(this, arrayOf(joshuatee.wx.settings.Location.rid, "", numberOfPanes.toString(), "true"))
        } else {
            Route.radarMultiPane(this, arrayOf(nexradArguments.originalRadarSite, "", numberOfPanes.toString(), "false"))
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
            KeyEvent.KEYCODE_2 -> if (event.isCtrlPressed) showMultipaneRadar(2)
            KeyEvent.KEYCODE_4 -> if (event.isCtrlPressed) showMultipaneRadar(4)
            KeyEvent.KEYCODE_L -> if (event.isCtrlPressed) nexradUI.showMap()
            KeyEvent.KEYCODE_M -> if (event.isCtrlPressed) toolbarBottom.showOverflowMenu()
            KeyEvent.KEYCODE_A -> if (event.isCtrlPressed) toggleAnimate()
            KeyEvent.KEYCODE_F -> if (event.isCtrlPressed) nexradUI.actionToggleFavorite()
            KeyEvent.KEYCODE_R -> if (event.isCtrlPressed) nexradState.getReflectivity(::getContent)
            KeyEvent.KEYCODE_V -> if (event.isCtrlPressed) nexradState.getVelocity(::getContent)
            KeyEvent.KEYCODE_SLASH -> if (event.isAltPressed) ObjectDialogue(this, Utility.showRadarShortCuts())
            KeyEvent.KEYCODE_REFRESH -> getContent()
            KeyEvent.KEYCODE_DPAD_UP -> if (event.isCtrlPressed) {
                nexradState.surface.zoomOutByKey()
            } else {
                nexradState.surface.onScrollByKeyboard(0.0f, -20.0f)
            }

            KeyEvent.KEYCODE_DPAD_DOWN -> if (event.isCtrlPressed) {
                nexradState.surface.zoomInByKey()
            } else {
                nexradState.surface.onScrollByKeyboard(0.0f, 20.0f)
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> nexradState.surface.onScrollByKeyboard(-20.0f, 0.0f)
            KeyEvent.KEYCODE_DPAD_RIGHT -> nexradState.surface.onScrollByKeyboard(20.0f, 0.0f)
            else -> return super.onKeyUp(keyCode, event)
        }
        return true
    }
    //elys mod
    private fun hideRadar() {
        UtilityLog.d("radarHidden", "showRadar() radarShown: "+radarHidden)
        if (radarHidden) {
            UtilityLog.d("radarHidden", "showRadar() setting to false")
            radarHidden = false
            RadarPreferences.hideRadar = false
            Utility.writePref(this, "RADAR_HIDE_RADAR", "false")
            UtilityLog.d("radarHidden", "showRadar() radarShowRadar: "+RadarPreferences.hideRadar)
        } else {
            UtilityLog.d("radarHidden", "showRadar() setting to true")
            radarHidden = true
            RadarPreferences.hideRadar = true
            Utility.writePref(this, "RADAR_HIDE_RADAR", "true")
            UtilityLog.d("radarshow", "showRadar() radarShowRadar: "+RadarPreferences.hideRadar)
        }
    }


}

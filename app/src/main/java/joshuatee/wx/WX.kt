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
//modded by ELY M.

package joshuatee.wx

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.fragments.LocationFragment
import joshuatee.wx.fragments.ViewPagerAdapter
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.spc.UtilitySpc
import joshuatee.wx.ui.CommonActionBarFragment
import joshuatee.wx.ui.Drawer
import joshuatee.wx.ui.Fab
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.ObjectToolbar
import joshuatee.wx.ui.UtilityTheme
import joshuatee.wx.util.Utility

class WX : CommonActionBarFragment() {

    private lateinit var vpa: ViewPagerAdapter
    private lateinit var voiceRecognitionIcon: MenuItem
    private var tabIndex = 0
    private lateinit var slidingTabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var toolbarBottom: Toolbar
    private lateinit var objectToolbarBottom: ObjectToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(UIPreferences.themeInt)
        super.onCreate(savedInstanceState)
        setupUI()
        setupToolbars()
        setupVoiceRecognition()
        setupFab()
        setupTabs()
        setupNavDrawer()
	    checkinternet() //elys mod
        refreshDynamicContent()
    }

    private fun setupUI() {
        val layoutId = if (UIPreferences.navDrawerMainScreen) {
            if (UIPreferences.navDrawerMainScreenOnRight) {
                R.layout.activity_main_drawer_right
            } else {
                R.layout.activity_main_drawer
            }
        } else {
            R.layout.activity_main
        }
        setContentView(layoutId)
        slidingTabLayout = findViewById(R.id.slidingTabLayout)
        viewPager = findViewById(R.id.viewPager)
        UtilityTheme.setPrimaryColor(this)
    }

    private fun setupToolbars() {
        //
        // Toolbar (bottom) setup
        //
        toolbarBottom = findViewById(R.id.toolbar_bottom)
        objectToolbarBottom = ObjectToolbar(toolbarBottom)
        // used in CommonActionBarFragment
        view = findViewById(android.R.id.content)
        toolbarBottom.elevation = UIPreferences.elevationPref
        toolbarBottom.inflateMenu(R.menu.cab)
        objectToolbarBottom.connect(this)
        objectToolbarBottom.connectClick { toolbarBottom.showOverflowMenu() }

//        ViewCompat.setOnApplyWindowInsetsListener(toolbarBottom) { v, windowInsets ->
//            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
//            // Apply the insets as a margin to the view. This solution sets
//            // only the bottom, left, and right dimensions, but you can apply whichever
//            // insets are appropriate to your layout. You can also update the view padding
//            // if that's more appropriate.
//            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//                leftMargin = insets.left
//                bottomMargin = insets.bottom
//                rightMargin = insets.right
//            }
//            // Return CONSUMED if you don't want want the window insets to keep passing
//            // down to descendant views.
//            WindowInsetsCompat.CONSUMED
//        }
    }

    private fun setupVoiceRecognition() {
        //
        // optional voice recognition icon
        //
        voiceRecognitionIcon = objectToolbarBottom.find(R.id.action_vr)
        voiceRecognitionIcon.isVisible = UIPreferences.vrButton
    }

    private fun setupFab() {
        //
        // radar floating action button unless disabled
        //
        Fab(this, R.id.fab, GlobalVariables.ICON_RADAR_WHITE) { openNexradRadar(this) }
        objectToolbarBottom.hideRadar()
    }

    private fun setupTabs() {
        //
        // Tab setup
        //
        viewPager.offscreenPageLimit = 4
        vpa = ViewPagerAdapter(this)
        viewPager.adapter = vpa
        with(slidingTabLayout) {
            tabGravity = TabLayout.GRAVITY_FILL
            TabLayoutMediator(this, viewPager) { tab, position ->
                tab.text = "OBJECT ${(position + 1)}"
            }.attach()
            elevation = UIPreferences.elevationPref
            if (UIPreferences.simpleMode || UIPreferences.hideTopToolbar || UIPreferences.navDrawerMainScreen) {
                visibility = View.GONE
            }
            //
            // Tab indicator color
            //
            setSelectedTabIndicatorColor(UtilityTheme.getPrimaryColorFromSelectedTheme(this@WX, 0))
        }
    }

    private fun setupNavDrawer() {
        //
        // Navigation Drawer (if enabled) setup
        //
        if (UIPreferences.navDrawerMainScreen) {
            toolbarBottom.visibility = View.GONE
            val drawer = Drawer(this)
            Fab(this, R.id.fab2, GlobalVariables.ICON_ADD2) {
                val statusList = UtilitySpc.checkSpc()
                val headerSize = drawer.setStatusText(statusList[0], statusList[1])
                drawer.setHeaderHeight(headerSize)
                drawer.openDrawer()
            }
        }
    }

    private fun refreshDynamicContent() {
        if (!UIPreferences.simpleMode) {
            val tabStr = UtilitySpc.checkSpc()
            vpa.setTabTitles(1, tabStr[0])
            vpa.setTabTitles(2, tabStr[1])
            with(slidingTabLayout) {
                if (tabCount > 2) {
                    getTabAt(0)!!.text = UIPreferences.tabHeaders[0]
                    getTabAt(1)!!.text = vpa.tabTitles[1]
                    getTabAt(2)!!.text = vpa.tabTitles[2]
                }
            }
        }
    }

    override fun onResume() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(onBroadcast, IntentFilter("notifran"))
        super.onResume()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onBroadcast)
        super.onPause()
    }

    private val onBroadcast = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, i: Intent) {
            refreshDynamicContent()
        }
    }

    override fun onRestart() {
        super.onRestart()
        voiceRecognitionIcon.isVisible = UIPreferences.vrButton
        refreshDynamicContent()
    }
    
    //elys mod
    fun checkinternet() {
        if (UIPreferences.checkinternet) {
            Utility.checkInternet(this)
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_R -> if (event.isCtrlPressed) openNexradRadar(this)
            KeyEvent.KEYCODE_A -> if (event.isCtrlPressed) openAfd()
            KeyEvent.KEYCODE_P -> if (event.isCtrlPressed) openSettings()
            KeyEvent.KEYCODE_C -> if (event.isCtrlPressed) openVis()
            KeyEvent.KEYCODE_D -> if (event.isCtrlPressed) openDashboard()
            KeyEvent.KEYCODE_G -> if (event.isCtrlPressed) openRainfallOutlookSummary()
            KeyEvent.KEYCODE_2 -> if (event.isCtrlPressed) openActivity(this, "RADAR_DUAL_PANE")
            KeyEvent.KEYCODE_4 -> if (event.isCtrlPressed) openActivity(this, "RADAR_QUAD_PANE")
            KeyEvent.KEYCODE_E, KeyEvent.KEYCODE_Z -> if (event.isCtrlPressed) openActivity(
                this,
                "SPCMESO1"
            )

            KeyEvent.KEYCODE_N -> if (event.isCtrlPressed) openActivity(this, "MODEL_NCEP")
            KeyEvent.KEYCODE_M -> if (event.isCtrlPressed) findViewById<Toolbar>(R.id.toolbar_bottom).showOverflowMenu()
            KeyEvent.KEYCODE_H -> if (event.isCtrlPressed) openHourly()
            KeyEvent.KEYCODE_O -> if (event.isCtrlPressed) openActivity(this, "NHC")
            KeyEvent.KEYCODE_L -> if (event.isCtrlPressed) {
                val currentFragment = supportFragmentManager.fragments.first() as LocationFragment
                currentFragment.showLocations()
            }

            KeyEvent.KEYCODE_I -> if (event.isCtrlPressed) openNationalImages()
            KeyEvent.KEYCODE_S -> if (event.isCtrlPressed) openSpcSwoSummary()
            KeyEvent.KEYCODE_T -> if (event.isCtrlPressed) openNationalText()
            KeyEvent.KEYCODE_SLASH -> if (event.isAltPressed) ObjectDialogue(
                this,
                Utility.showMainScreenShortCuts()
            )

            KeyEvent.KEYCODE_J -> if (event.isCtrlPressed) {
                tabIndex += -1
                if (tabIndex < 0) {
                    tabIndex = 2
                }
                viewPager.currentItem = tabIndex
            }

            KeyEvent.KEYCODE_K -> if (event.isCtrlPressed) {
                tabIndex += 1
                if (tabIndex > 2) {
                    tabIndex = 0
                }
                viewPager.currentItem = tabIndex
            }

            KeyEvent.KEYCODE_REFRESH -> {
                val currentFragment = supportFragmentManager.fragments.first() as LocationFragment
                currentFragment.getContent()
            }

            else -> return super.onKeyUp(keyCode, event)
        }
        return true
    }
}

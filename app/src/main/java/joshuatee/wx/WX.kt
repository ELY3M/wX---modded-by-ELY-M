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
import android.view.View.OnClickListener
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.tabs.TabLayout
import joshuatee.wx.fragments.LocationFragment
import joshuatee.wx.fragments.ViewPagerAdapter
import joshuatee.wx.spc.UtilitySpc
import joshuatee.wx.ui.*
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import kotlinx.android.synthetic.main.activity_main.*

class WX : CommonActionBarFragment() {

    private var backButtonCounter = 0
    private lateinit var vpa: ViewPagerAdapter
    private lateinit var miVr: MenuItem
    private var tabIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(UIPreferences.themeInt)
        UtilityLog.d("wx", "TABLET: " + UtilityUI.isTablet().toString())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        UtilityTheme.setPrimaryColor(this)
        val toolbarBottom: Toolbar = findViewById(R.id.toolbar_bottom)
        view = findViewById(android.R.id.content)
        if (android.os.Build.VERSION.SDK_INT > 20)
            toolbarBottom.elevation = MyApplication.elevationPref
        if (MyApplication.iconsEvenSpaced) {
            UtilityToolbar.setupEvenlyDistributedToolbar(this, toolbarBottom, R.menu.cab)
        } else {
            toolbarBottom.inflateMenu(R.menu.cab)
        }
        if (MyApplication.checkinternet) {
            Utility.checkInternet(this)
        }
        toolbarBottom.setOnMenuItemClickListener(this)
        toolbarBottom.setOnClickListener { toolbarBottom.showOverflowMenu() }
        val menu = toolbarBottom.menu
        helpMi = menu.findItem(R.id.action_help)
        miVr = menu.findItem(R.id.action_vr)
        miVr.isVisible = MyApplication.vrButton
        if (MyApplication.helpMode) helpMi.title = helpStr
        val fab = ObjectFab(
                this,
                this,
                R.id.fab,
                MyApplication.ICON_RADAR,
                OnClickListener { openNexradRadar(this, 0) })
        if (UIPreferences.mainScreenRadarFab) {
            val radarMi = menu.findItem(R.id.action_radar)
            radarMi.isVisible = false
        } else {
            fab.visibility = View.GONE
        }
        viewPager.offscreenPageLimit = 4
        vpa = ViewPagerAdapter(supportFragmentManager)
        viewPager.adapter = vpa
        slidingTabLayout.tabGravity = TabLayout.GRAVITY_FILL
        slidingTabLayout.setupWithViewPager(viewPager)
        if (android.os.Build.VERSION.SDK_INT > 20) {
            slidingTabLayout.elevation = MyApplication.elevationPref
        }
        if (MyApplication.simpleMode || UIPreferences.hideTopToolbar) {
            slidingTabLayout.visibility = View.GONE
        }
        slidingTabLayout.setSelectedTabIndicatorColor(
                UtilityTheme.getPrimaryColorFromSelectedTheme(
                        this,
                        0
                )
        )
        refreshDynamicContent()
        if (android.os.Build.VERSION.SDK_INT < 21) {
            toolbarBottom.bringToFront()
        }
    }

    override fun onBackPressed() {
        if (UIPreferences.prefPreventAccidentalExit) {
            if (backButtonCounter < 1) {
                UtilityUI.makeSnackBar(
                        slidingTabLayout,
                        "Please tap the back button one more time to close wX."
                )
                backButtonCounter += 1
            } else {
                finish()
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun refreshDynamicContent() {
        if (!MyApplication.simpleMode) {
            val tabStr = UtilitySpc.checkSpc()
            vpa.setTabTitles(1, tabStr[0])
            vpa.setTabTitles(2, tabStr[1])
            if (slidingTabLayout.tabCount > 2) {
                slidingTabLayout.getTabAt(0)!!.text = MyApplication.tabHeaders[0]
                slidingTabLayout.getTabAt(1)!!.text = vpa.tabTitles[1]
                slidingTabLayout.getTabAt(2)!!.text = vpa.tabTitles[2]
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
        miVr.isVisible = MyApplication.vrButton
        if (MyApplication.helpMode)
            helpMi.title = helpStr
        else
            helpMi.title = "Help"
        backButtonCounter = 0
        refreshDynamicContent()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_R -> {
                if (event.isCtrlPressed) {
                    openNexradRadar(this, 0)
                }
                return true
            }
            KeyEvent.KEYCODE_A -> {
                if (event.isCtrlPressed) {
                    openAfd(0)
                }
                return true
            }
            KeyEvent.KEYCODE_S -> {
                if (event.isCtrlPressed) {
                    openSettings(0)
                }
                return true
            }
            KeyEvent.KEYCODE_C -> {
                if (event.isCtrlPressed) {
                    openVis(0)
                }
                return true
            }
            KeyEvent.KEYCODE_D -> {
                if (event.isCtrlPressed) {
                    openDashboard(0)
                }
                return true
            }
            KeyEvent.KEYCODE_2 -> {
                if (event.isCtrlPressed) {
                    openActivity(this, "RADAR_DUAL_PANE")
                }
                return true
            }
            KeyEvent.KEYCODE_4 -> {
                if (event.isCtrlPressed) {
                    openActivity(this, "RADAR_QUAD_PANE")
                }
                return true
            }
            //KeyEvent.KEYCODE_W -> {
            //    if (event.isCtrlPressed) {
            //        openActivity(this, "USWARN")
            //    }
            //    return true
            //}
            KeyEvent.KEYCODE_E -> {
                if (event.isCtrlPressed) {
                    openActivity(this, "SPCMESO1")
                }
                return true
            }
            KeyEvent.KEYCODE_N -> {
                if (event.isCtrlPressed) {
                    openActivity(this, "MODEL_NCEP")
                }
                return true
            }
            KeyEvent.KEYCODE_M -> {
                if (event.isCtrlPressed) {
                    val toolbarBottom: Toolbar = findViewById(R.id.toolbar_bottom)
                    toolbarBottom.showOverflowMenu()
                }
                return true
            }
            KeyEvent.KEYCODE_H -> {
                if (event.isCtrlPressed) {
                    openHourly(0)
                }
                return true
            }
            KeyEvent.KEYCODE_O -> {
                if (event.isCtrlPressed) {
                    openActivity(this, "NHC")
                }
                return true
            }
            KeyEvent.KEYCODE_L -> {
                if (event.isCtrlPressed) {
                    openActivity(this, "LTG")
                }
                return true
            }
            KeyEvent.KEYCODE_I -> {
                if (event.isCtrlPressed) {
                    openActivity(this, "WPCIMG")
                }
                return true
            }
            KeyEvent.KEYCODE_Z -> {
                if (event.isCtrlPressed) {
                    openActivity(this, "WPCTEXT")
                }
                return true
            }
            KeyEvent.KEYCODE_SLASH -> {
                if (event.isAltPressed) {
                    ObjectDialogue(this, Utility.showMainScreenShortCuts())
                }
                return true
            }
            KeyEvent.KEYCODE_J -> {
                if (event.isCtrlPressed) {
                    tabIndex += -1
                    if (tabIndex < 0) {
                        tabIndex = 2
                    }
                    viewPager.currentItem = tabIndex
                }
                return true
            }
            KeyEvent.KEYCODE_K -> {
                if (event.isCtrlPressed) {
                    tabIndex += 1
                    if (tabIndex > 2) {
                        tabIndex = 0
                    }
                    viewPager.currentItem = tabIndex
                }
                return true
            }
            KeyEvent.KEYCODE_REFRESH -> {
                val currentFragment = supportFragmentManager.fragments.first() as LocationFragment
                currentFragment.getContent()
                return true
            }
            else -> return super.onKeyUp(keyCode, event)
        }
    }
}




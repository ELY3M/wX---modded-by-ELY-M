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
//modded by ELY M.

package joshuatee.wx

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.widget.Toolbar
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener

import joshuatee.wx.fragments.ViewPagerAdapter
import joshuatee.wx.spc.UtilitySPC
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.ui.UtilityTheme
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility

class WX : CommonActionBarFragment() {

    private var backButtonCounter = 0
    private lateinit var vpa: ViewPagerAdapter
    private lateinit var mSlidingTabLayout: TabLayout
    private lateinit var miVr: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(UIPreferences.themeInt)
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
            fab.setVisibility(View.GONE)
        }
        val viewPager: ViewPager = findViewById(R.id.pager)
        viewPager.offscreenPageLimit = 4
        vpa = ViewPagerAdapter(supportFragmentManager)
        viewPager.adapter = vpa
        mSlidingTabLayout = findViewById(R.id.sliding_tabs)
        mSlidingTabLayout.tabGravity = TabLayout.GRAVITY_FILL
        mSlidingTabLayout.setupWithViewPager(viewPager)
        if (android.os.Build.VERSION.SDK_INT > 20) {
            mSlidingTabLayout.elevation = MyApplication.elevationPref
        }
        if (MyApplication.simpleMode || UIPreferences.hideTopToolbar) {
            mSlidingTabLayout.visibility = View.GONE
        }
        mSlidingTabLayout.setSelectedTabIndicatorColor(
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
                    mSlidingTabLayout,
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
            val tabStr = UtilitySPC.checkSpc(this)
            vpa.setTabTitles(1, tabStr[0])
            vpa.setTabTitles(2, tabStr[1])
            if (mSlidingTabLayout.tabCount > 3) {
                mSlidingTabLayout.getTabAt(0)!!.text = MyApplication.tabHeaders[0]
                mSlidingTabLayout.getTabAt(1)!!.text = vpa.tabTitles[1]
                mSlidingTabLayout.getTabAt(2)!!.text = vpa.tabTitles[2]
                mSlidingTabLayout.getTabAt(3)!!.text = MyApplication.tabHeaders[3]
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
            KeyEvent.KEYCODE_I -> {
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
            KeyEvent.KEYCODE_G -> {
                if (event.isCtrlPressed) {
                    openVis(0)
                }
                return true
            }
            KeyEvent.KEYCODE_E -> {
                if (event.isCtrlPressed) {
                    openDashboard(0)
                }
                return true
            }
            else -> return super.onKeyUp(keyCode, event)
        }
    }
}




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
//modded by ELY M.

package joshuatee.wx

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import joshuatee.wx.activitiesmisc.*
import joshuatee.wx.canada.CanadaAlertsActivity
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.fragments.LocationFragment
import joshuatee.wx.fragments.ViewPagerAdapter
import joshuatee.wx.models.ModelsSpcHrefActivity
import joshuatee.wx.models.ModelsSpcHrrrActivity
import joshuatee.wx.models.ModelsSpcSrefActivity
import joshuatee.wx.nhc.NhcActivity
import joshuatee.wx.objects.Route
import joshuatee.wx.settings.Location
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.settings.UtilityNavDrawer
import joshuatee.wx.spc.*
import joshuatee.wx.ui.*
import joshuatee.wx.util.Utility
import joshuatee.wx.vis.GoesActivity
import joshuatee.wx.wpc.WpcImagesActivity
import joshuatee.wx.wpc.WpcRainfallForecastSummaryActivity
import joshuatee.wx.wpc.WpcTextProductsActivity

class WX : CommonActionBarFragment() {

    private var backButtonCounter = 0
    private lateinit var vpa: ViewPagerAdapter
    private lateinit var voiceRecognitionIcon: MenuItem
    private var tabIndex = 0
    private lateinit var navigationView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var slidingTabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(UIPreferences.themeInt)
        super.onCreate(savedInstanceState)
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
        val toolbarBottom: Toolbar = findViewById(R.id.toolbar_bottom)
        view = findViewById(android.R.id.content)
        toolbarBottom.elevation = UIPreferences.elevationPref
        if (UIPreferences.iconsEvenSpaced) {
            UtilityToolbar.setupEvenlyDistributedToolbar(this, toolbarBottom, R.menu.cab)
        } else {
            toolbarBottom.inflateMenu(R.menu.cab)
        }

        //elys mod
        if (UIPreferences.checkinternet) {
            Utility.checkInternet(this)
        }

        toolbarBottom.setOnMenuItemClickListener(this)
        toolbarBottom.setOnClickListener { toolbarBottom.showOverflowMenu() }
        val menu = toolbarBottom.menu
        voiceRecognitionIcon = menu.findItem(R.id.action_vr)
        voiceRecognitionIcon.isVisible = UIPreferences.vrButton
        val fab = ObjectFab(this, R.id.fab, GlobalVariables.ICON_RADAR_WHITE) { openNexradRadar(this) }
        if (UIPreferences.mainScreenRadarFab) {
            val radarMi = menu.findItem(R.id.action_radar)
            radarMi.isVisible = false
        } else {
            fab.visibility = View.GONE
        }
        viewPager.offscreenPageLimit = 4
        vpa = ViewPagerAdapter(this)
        viewPager.adapter = vpa
        slidingTabLayout.tabGravity = TabLayout.GRAVITY_FILL
//        slidingTabLayout.setupWithViewPager(viewPager)
        TabLayoutMediator(slidingTabLayout, viewPager) { tab, position ->
            tab.text = "OBJECT ${(position + 1)}"
        }.attach()

        slidingTabLayout.elevation = UIPreferences.elevationPref
        if (UIPreferences.simpleMode || UIPreferences.hideTopToolbar || UIPreferences.navDrawerMainScreen) {
            slidingTabLayout.visibility = View.GONE
        }
        slidingTabLayout.setSelectedTabIndicatorColor(UtilityTheme.getPrimaryColorFromSelectedTheme(this, 0))
        if (UIPreferences.navDrawerMainScreen) {
            toolbarBottom.visibility = View.GONE
            slidingTabLayout.visibility = View.GONE
            navigationView = findViewById(R.id.nav_view)
            drawerLayout = findViewById(R.id.drawer_layout)
            navigationView.itemIconTintList = null
            navigationView.setItemIconSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40.0f, MyApplication.dm).toInt())
            if (!UIPreferences.themeIsWhite) {
                navigationView.itemTextColor = ColorStateList.valueOf(Color.WHITE)
            }
            val color = UtilityTheme.getPrimaryColorFromSelectedTheme(this, 0)
            var tint = ColorStateList.valueOf(color)
            val headerLayout = navigationView.getHeaderView(0)
            headerLayout.setBackgroundColor(color)
            if (UIPreferences.themeInt == R.style.MyCustomTheme_whitest_NOAB) {
                val colorForWhite = ContextCompat.getColor(this, R.color.primary_blue)
                headerLayout.setBackgroundColor(colorForWhite)
                tint = ColorStateList.valueOf(colorForWhite)
            }
            // TODO chunk below needs a lot of refactor , create static Route and pass drawer to close as optional
            val statusText = headerLayout.findViewById<TextView>(R.id.statusText)
            statusText.visibility = View.GONE
            val severeDashboardButton = headerLayout.findViewById<ImageButton>(R.id.severeDashboardButton)
            val severeDashboardText = headerLayout.findViewById<TextView>(R.id.severeDashboardText)
            val visButton = headerLayout.findViewById<ImageButton>(R.id.visibleSatelliteButton)
            val visText = headerLayout.findViewById<TextView>(R.id.visibleSatelliteText)
            val wfoButton = headerLayout.findViewById<ImageButton>(R.id.wfoButton)
            val wfoText = headerLayout.findViewById<TextView>(R.id.wfoText)
            val hourlyButton = headerLayout.findViewById<ImageButton>(R.id.hourlyButton)
            val hourlyText = headerLayout.findViewById<TextView>(R.id.hourlyText)
            val settingsButton = headerLayout.findViewById<ImageButton>(R.id.settingsButton)
            val settingsText = headerLayout.findViewById<TextView>(R.id.settingsText)
            listOf(severeDashboardButton, visButton, wfoButton, hourlyButton, settingsButton).forEach {
                it.backgroundTintList = tint
            }
            val gravityForDrawer = if (UIPreferences.navDrawerMainScreenOnRight) {
                GravityCompat.END
            } else {
                GravityCompat.START
            }
            severeDashboardButton.setOnClickListener {
                Route(this, SevereDashboardActivity::class.java)
                drawerLayout.closeDrawer(gravityForDrawer)
            }
            severeDashboardText.setOnClickListener {
                Route(this, SevereDashboardActivity::class.java)
                drawerLayout.closeDrawer(gravityForDrawer)
            }
            visButton.setOnClickListener {
                Route.vis(this)
                drawerLayout.closeDrawer(gravityForDrawer)
            }
            visText.setOnClickListener {
                Route.vis(this)
                drawerLayout.closeDrawer(gravityForDrawer)
            }
            wfoButton.setOnClickListener {
                Route.wfoText(this)
                drawerLayout.closeDrawer(gravityForDrawer)
            }
            wfoText.setOnClickListener {
                Route.wfoText(this)
                drawerLayout.closeDrawer(gravityForDrawer)
            }
            hourlyButton.setOnClickListener {
                Route.hourly(this)
                drawerLayout.closeDrawer(gravityForDrawer)
            }
            hourlyText.setOnClickListener {
                Route.hourly(this)
                drawerLayout.closeDrawer(gravityForDrawer)
            }
            settingsButton.setOnClickListener{
                Route.settings(this)
                drawerLayout.closeDrawer(gravityForDrawer)
            }
            settingsText.setOnClickListener{
                Route.settings(this)
                drawerLayout.closeDrawer(gravityForDrawer)
            }
            UtilityNavDrawer.hideItems(this, navigationView)
            navigationView.setNavigationItemSelectedListener{ item ->
                when (item.itemId) {
                    R.id.esrl -> Route.model(this, arrayOf("1", "ESRL", "ESRL"))
                    R.id.rainfall_outlook -> Route(this, WpcRainfallForecastSummaryActivity::class.java)
                    R.id.glcfs -> Route.model(this, arrayOf("1", "GLCFS", "GLCFS"))
                    R.id.goes_conus_wv -> Route(this, GoesActivity::class.java, GoesActivity.RID, arrayOf("CONUS", "09"))
                    R.id.goes_global -> Route(this, ImageCollectionActivity::class.java, ImageCollectionActivity.TYPE, arrayOf("GOESFD"))
                    R.id.lightning -> {
                        if (UIPreferences.lightningUseGoes) {
                            Route(this, GoesActivity::class.java, GoesActivity.RID, arrayOf("CONUS", "23"))
                        } else {
                            Route(this, LightningActivity::class.java)
                        }
                    }
                    R.id.national_images -> Route(this, WpcImagesActivity::class.java, "", arrayOf())
                    R.id.national_text -> Route(this, WpcTextProductsActivity::class.java, WpcTextProductsActivity.URL, arrayOf("pmdspd", "Short Range Forecast Discussion"))
                    R.id.ncep_models -> Route.model(this, arrayOf("1", "NCEP", "NCEP"))
                    R.id.nhc -> Route(this, NhcActivity::class.java)
                    R.id.nssl_wrf -> Route.model(this, arrayOf("1", "NSSL", "NSSL"))
                    R.id.observations -> Route.observations(this)
                    R.id.observation_sites -> Route(this, NwsObsSitesActivity::class.java)
                    R.id.opc -> Route(this, ImageCollectionActivity::class.java, ImageCollectionActivity.TYPE, arrayOf("OPC"))
                    R.id.radar_mosaic -> Route.radarMosaic(this)
                    R.id.radar_dual_pane -> Route.radarMultiPane(this, arrayOf(Location.rid, "", "2"))
                    R.id.radar_quad_pane -> Route.radarMultiPane(this, arrayOf(Location.rid, "", "4"))
                    R.id.spc_comp_map -> Route(this, SpcCompmapActivity::class.java)
                    R.id.spc_convective_outlooks -> Route(this, SpcSwoSummaryActivity::class.java )
                    R.id.spc_day_1 -> Route.spcSwo(this, arrayOf("1", ""))
                    R.id.spc_day_2 -> Route.spcSwo(this, arrayOf("2", ""))
                    R.id.spc_day_3 -> Route.spcSwo(this, arrayOf("3", ""))
                    R.id.spc_day_4_8 -> Route.spcSwo(this, arrayOf("4-8", ""))
                    R.id.spc_fire_outlooks -> Route(this, SpcFireOutlookSummaryActivity::class.java)
                    R.id.spc_href -> Route(this, ModelsSpcHrefActivity::class.java, "", arrayOf("1", "SPCHREF", "SPC HREF"))
                    R.id.spc_hrrr -> Route(this, ModelsSpcHrrrActivity::class.java, "", arrayOf("1", "SPCHRRR", "SPC HRRR"))
                    R.id.spc_mesoanalysis -> Route(this,SpcMesoActivity::class.java, SpcMesoActivity.INFO, arrayOf("", "1", "SPCMESO"))
                    R.id.spc_soundings -> Route.sounding(this)
                    R.id.spc_sref -> Route(this, ModelsSpcSrefActivity::class.java, ModelsSpcSrefActivity.INFO, arrayOf("1", "SPCSREF", "SPCSREF"))
                    R.id.spc_storm_reports -> Route.spcStormReports(this)
                    R.id.spc_thunderstorm_outlooks -> Route(this, SpcThunderStormOutlookActivity::class.java)
                    R.id.spotters -> Route(this, SpottersActivity::class.java)
                    R.id.twitter_states -> Route(this, WebViewTwitter::class.java)
                    R.id.twitter_tornado -> Route.webView(this, arrayOf("https://mobile.twitter.com/hashtag/tornado", "#tornado"))
                    R.id.us_alerts -> {
                        if (Location.isUS) {
                            Route.usAlerts(this)
                        } else {
                            Route(this, CanadaAlertsActivity::class.java)
                        }
                    }
                    R.id.wpc_gefs -> Route.model(this, arrayOf("1", "WPCGEFS", "WPC"))
		    //elys mod
                    R.id.aurora -> Route(this, ImageCollectionActivity::class.java, ImageCollectionActivity.TYPE, arrayOf("AURORA"))
                }
                if (UIPreferences.navDrawerMainScreenOnRight) {
                    drawerLayout.closeDrawer(GravityCompat.END)
                } else {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                true
            }
            ObjectFab(this, R.id.fab2, GlobalVariables.ICON_ADD2) {
                val headerSize: Float
                val tabStr = UtilitySpc.checkSpc()
                if (UIPreferences.checkspc || UIPreferences.checktor || UIPreferences.checkwpc && (tabStr[0] != "SPC" || tabStr[1] != "MISC")) {
                    statusText.visibility = View.VISIBLE
                    statusText.text = tabStr[0] + " " + tabStr[1]
                    headerSize = 280f
                } else {
                    statusText.visibility = View.GONE
                    headerSize = 250f
                }
                val layoutParams = headerLayout.layoutParams
                layoutParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, headerSize, resources.displayMetrics).toInt()
                headerLayout.layoutParams = layoutParams
                if (UIPreferences.navDrawerMainScreenOnRight) {
                    drawerLayout.openDrawer(GravityCompat.END)
                } else {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
            }
        }
        refreshDynamicContent()
    }

    override fun onBackPressed() {
        if (UIPreferences.prefPreventAccidentalExit) {
            if (backButtonCounter < 1) {
                ObjectPopupMessage(slidingTabLayout, "Please tap the back button one more time to close wX.")
                backButtonCounter += 1
            } else {
                finish()
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun refreshDynamicContent() {
        if (!UIPreferences.simpleMode) {
            val tabStr = UtilitySpc.checkSpc()
            vpa.setTabTitles(1, tabStr[0])
            vpa.setTabTitles(2, tabStr[1])
            if (slidingTabLayout.tabCount > 2) {
                slidingTabLayout.getTabAt(0)!!.text = UIPreferences.tabHeaders[0]
                slidingTabLayout.getTabAt(1)!!.text = vpa.tabTitles[1]
                slidingTabLayout.getTabAt(2)!!.text = vpa.tabTitles[2]
            }
        }
    }

    override fun onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(onBroadcast, IntentFilter("notifran"))
        super.onResume()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onBroadcast)
        super.onPause()
    }

    private val onBroadcast = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, i: Intent) { refreshDynamicContent() }
    }

    override fun onRestart() {
        super.onRestart()
        voiceRecognitionIcon.isVisible = UIPreferences.vrButton
        backButtonCounter = 0
        refreshDynamicContent()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_R -> if (event.isCtrlPressed) openNexradRadar(this)
            KeyEvent.KEYCODE_A -> if (event.isCtrlPressed) openAfd()
            KeyEvent.KEYCODE_S -> if (event.isCtrlPressed) openSettings()
            KeyEvent.KEYCODE_C -> if (event.isCtrlPressed) openVis()
            KeyEvent.KEYCODE_D -> if (event.isCtrlPressed) openDashboard()
            KeyEvent.KEYCODE_2 -> if (event.isCtrlPressed) openActivity(this, "RADAR_DUAL_PANE")
            KeyEvent.KEYCODE_4 -> if (event.isCtrlPressed) openActivity(this, "RADAR_QUAD_PANE")
            KeyEvent.KEYCODE_E -> if (event.isCtrlPressed) openActivity(this, "SPCMESO1")
            KeyEvent.KEYCODE_N -> if (event.isCtrlPressed) openActivity(this, "MODEL_NCEP")
            KeyEvent.KEYCODE_M -> if (event.isCtrlPressed) findViewById<Toolbar>(R.id.toolbar_bottom).showOverflowMenu()
            KeyEvent.KEYCODE_H -> if (event.isCtrlPressed) openHourly()
            KeyEvent.KEYCODE_O -> if (event.isCtrlPressed) openActivity(this, "NHC")
            KeyEvent.KEYCODE_L -> {
                if (event.isCtrlPressed) {
                    val currentFragment = supportFragmentManager.fragments.first() as LocationFragment
                    currentFragment.showLocations()
                }
            }
            KeyEvent.KEYCODE_I -> if (event.isCtrlPressed) openActivity(this, "WPCIMG")
            KeyEvent.KEYCODE_Z -> if (event.isCtrlPressed) openActivity(this, "WPCTEXT")
            KeyEvent.KEYCODE_SLASH -> if (event.isAltPressed) ObjectDialogue(this, Utility.showMainScreenShortCuts())
            KeyEvent.KEYCODE_J -> {
                if (event.isCtrlPressed) {
                    tabIndex += -1
                    if (tabIndex < 0) {
                        tabIndex = 2
                    }
                    viewPager.currentItem = tabIndex
                }
            }
            KeyEvent.KEYCODE_K -> {
                if (event.isCtrlPressed) {
                    tabIndex += 1
                    if (tabIndex > 2) {
                        tabIndex = 0
                    }
                    viewPager.currentItem = tabIndex
                }
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

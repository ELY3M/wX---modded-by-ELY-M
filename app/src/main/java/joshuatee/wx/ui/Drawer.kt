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

package joshuatee.wx.ui

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.objects.Route
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.settings.UtilityNavDrawer

class Drawer(
        val activity: Activity,
) {

    val headerItems = mutableListOf<DrawerHeaderItem>()
    private val gravityForDrawer = if (UIPreferences.navDrawerMainScreenOnRight) {
        GravityCompat.END
    } else {
        GravityCompat.START
    }
    private var tint: ColorStateList
    val statusText: TextView
    val drawerLayout: DrawerLayout = activity.findViewById(R.id.drawer_layout)
    private val navigationView: NavigationView = activity.findViewById(R.id.nav_view)
    val headerLayout: View

    init {
        navigationView.itemIconTintList = null
        navigationView.setItemIconSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40.0f, MyApplication.dm).toInt())
        if (!UIPreferences.themeIsWhite) {
            navigationView.itemTextColor = ColorStateList.valueOf(Color.WHITE)
        }
        headerLayout = navigationView.getHeaderView(0)

        val color = UtilityTheme.getPrimaryColorFromSelectedTheme(activity, 0)
        tint = ColorStateList.valueOf(color)
        headerLayout.setBackgroundColor(color)

        if (UIPreferences.themeInt == R.style.MyCustomTheme_whitest_NOAB || UIPreferences.themeInt == R.style.MyCustomTheme_NOAB) {
            val colorForWhite = ContextCompat.getColor(activity, R.color.primary_blue)
            headerLayout.setBackgroundColor(colorForWhite)
            tint = ColorStateList.valueOf(colorForWhite)
        }
        //
        // Status text for Warning/watch count if configured
        //
        statusText = headerLayout.findViewById(R.id.statusText)
        statusText.visibility = View.GONE
        UtilityNavDrawer.hideItems(activity, navigationView)
        addHeaderItems()
        addSecondaryItems()
    }

    fun addItem(
            buttonId: Int,
            textId: Int,
            fn: () -> Unit
    ) {
        headerItems.add(DrawerHeaderItem(drawerLayout, headerLayout, buttonId, textId, tint, gravityForDrawer, fn))
    }

    fun setHeaderHeight(headerSize: Float) {
        val layoutParams = headerLayout.layoutParams
        layoutParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, headerSize, activity.resources.displayMetrics).toInt()
        headerLayout.layoutParams = layoutParams
    }

    fun openDrawer() {
        if (UIPreferences.navDrawerMainScreenOnRight) {
            drawerLayout.openDrawer(GravityCompat.END)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    fun closeDrawer() {
        if (UIPreferences.navDrawerMainScreenOnRight) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    // TODO FIXME this does not work well if user modified tab headers
    fun setStatusText(spcText: String, miscText: String): Float {
        val headerSize: Float
        if (UIPreferences.checkspc || UIPreferences.checktor || UIPreferences.checkwpc && (spcText != "SPC" || miscText != "MISC")) {
            statusText.visibility = View.VISIBLE
            statusText.text = "$spcText $miscText"
            headerSize = 280.0f
        } else {
            statusText.visibility = View.GONE
            headerSize = 250.0f
        }
        return headerSize
    }

    private fun addHeaderItems() {
        addItem(R.id.severeDashboardButton, R.id.severeDashboardText) { Route.severeDash(activity) }
        addItem(R.id.visibleSatelliteButton, R.id.visibleSatelliteText) { Route.vis(activity) }
        addItem(R.id.wfoButton, R.id.wfoText) { Route.wfoText(activity) }
        addItem(R.id.hourlyButton, R.id.hourlyText) { Route.hourly(activity) }
        addItem(R.id.settingsButton, R.id.settingsText) { Route.settings(activity) }
    }

    private fun addSecondaryItems() {
        //
        // Navigation drawer routing for secondary items
        //
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.esrl -> Route.modelEsrl(activity)
                R.id.rainfall_outlook -> Route.wpcRainfallSummary(activity)
                R.id.goes_conus_wv -> Route.visWv(activity)
                R.id.goes_global -> Route.goesFd(activity)
                R.id.lightning -> Route.lightning(activity)
                R.id.national_images -> Route.wpcImages(activity)
                R.id.national_text -> Route.wpcText(activity)
                R.id.ncep_models -> Route.modelNcep(activity)
                R.id.nhc -> Route.nhc(activity)
                R.id.nssl_wrf -> Route.modelNsslWrf(activity)
                R.id.observations -> Route.observations(activity)
                R.id.observation_sites -> Route.obsSites(activity)
                R.id.opc -> Route.opc(activity)
                R.id.radar_mosaic -> Route.radarMosaic(activity)
                R.id.radar_dual_pane -> Route.radarMultiPane2(activity)
                R.id.radar_quad_pane -> Route.radarMultiPane4(activity)
                R.id.spc_comp_map -> Route.spcCompmap(activity)
                R.id.spc_convective_outlooks -> Route.spcSwoSummary(activity)
                R.id.spc_day_1 -> Route.spcSwoDay1(activity)
                R.id.spc_day_2 -> Route.spcSwoDay2(activity)
                R.id.spc_day_3 -> Route.spcSwoDay3(activity)
                R.id.spc_day_4_8 -> Route.spcSwoDay48(activity)
                R.id.spc_fire_outlooks -> Route.spcFireOutlookSummary(activity)
                R.id.spc_href -> Route.spcHref(activity)
                R.id.spc_hrrr -> Route.spcHrrr(activity)
                R.id.spc_mesoanalysis -> Route.spcMeso(activity)
                R.id.spc_soundings -> Route.sounding(activity)
                R.id.spc_sref -> Route.spcSref(activity)
                R.id.spc_storm_reports -> Route.spcStormReports(activity)
                R.id.spc_thunderstorm_outlooks -> Route.spcTstorm(activity)
                R.id.spotters -> Route.spotters(activity)
                R.id.twitter_states -> Route.webViewTwitterStates(activity)
                R.id.twitter_tornado -> Route.webViewTwitterTornado(activity)
                R.id.us_alerts -> Route.alerts(activity)
                R.id.wpc_gefs -> Route.wpcGefs(activity)
            }
            closeDrawer()
            true
        }
    }
}

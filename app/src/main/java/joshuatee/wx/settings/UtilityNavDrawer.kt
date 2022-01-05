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

package joshuatee.wx.settings

import android.content.Context
import com.google.android.material.navigation.NavigationView
import joshuatee.wx.R
import joshuatee.wx.util.Utility

internal object UtilityNavDrawer {

    val labels = listOf(
        "ESRL HRRR/RAP",
        "Excessive Rainfall Outlook",
        "GLCFS",
        "Global Satellite",
        "GOES Conus Water Vapor",
        "Lightning",
        "National Images",
        "National Text",
        "NCEP Models",
        "NHC",
        "NSSL WRF",
        "Observations",
        "Observation Sites",
        "OPC",
        "Radar Mosaic",
        "Radar dual pane",
        "Radar quad pane",
        "SPC Composite Map",
        "SPC Convective Outlooks",
        "SPC Convective Day 1",
        "SPC Convective Day 2",
        "SPC Convective Day 3",
        "SPC Convective Day 4-8",
        "SPC Fire Weather Outlooks",
        "SPC HREF",
        "SPC HRRR",
        "SPC Mesoanalysis",
        "SPC Soundings",
        "SPC SREF",
        "SPC Storm Reports",
        "SPC Thunderstorm Outlooks",
        "Spotters",
        "Twitter states",
        "Twitter tornado",
        "US Alerts",
        "WPC GEFS",
        "Aurora Forecast"
    )

    val labelToTokenMap = mapOf(
        "ESRL HRRR/RAP" to "model_hrrr",
        "Excessive Rainfall Outlook" to "wpc_rainfall",
        "GLCFS" to "model_glcfs",
        "Global Satellite" to "goesfulldisk",
        "GOES Conus Water Vapor" to "goes",
        "Lightning" to "lightning",
        "National Images" to "wpcimages",
        "National Text" to "wpctext",
        "NCEP Models" to "model_ncep",
        "NHC" to "nhc",
        "NSSL WRF" to "model_nssl_wrf",
        "Observations" to "obs",
        "Observation Sites" to "nwsobs",
        "OPC" to "opc",
        "Radar Mosaic" to "nwsmosaic",
        "Radar dual pane" to "wxogldual",
        "Radar quad pane" to "wxoglquad",
        "SPC Composite Map" to "spccompmap",
        "SPC Convective Outlooks" to "spcsummary",
        "SPC Convective Day 1" to "spcswod1",
        "SPC Convective Day 2" to "spcswod2",
        "SPC Convective Day 3" to "spcswod3",
        "SPC Convective Day 4-8" to "spcswod48",
        "SPC Fire Weather Outlooks" to "spcfire",
        "SPC HREF" to "spchref",
        "SPC HRRR" to "spchrrr",
        "SPC Mesoanalysis" to "spcmeso",
        "SPC Soundings" to "spcsoundings",
        "SPC SREF" to "spcsref",
        "SPC Storm Reports" to "spcstormrpt1",
        "SPC Thunderstorm Outlooks" to "spctstorm",
        "Spotters" to "spotters",
        "Twitter states" to "twitter_state",
        "Twitter tornado" to "twitter_tornado",
        "US Alerts" to "uswarn",
        "WPC GEFS" to "wpcgefs",
        "Aurora Forecast" to "aurora"
    )

    private val navDrawerIdToToken = mapOf(
        R.id.esrl to "model_hrrr",
        R.id.rainfall_outlook to "wpc_rainfall",
        R.id.glcfs to "model_glcfs",
        R.id.goes_global to "goesfulldisk",
        R.id.goes_conus_wv to "goes",
        R.id.lightning to "lightning",
        R.id.national_images to "wpcimages",
        R.id.national_text to "wpctext",
        R.id.ncep_models to "model_ncep",
        R.id.nhc to "nhc",
        R.id.nssl_wrf to "model_nssl_wrf",
        R.id.observations to "obs",
        R.id.observation_sites to "nwsobs",
        R.id.opc to "opc",
        R.id.radar_mosaic to "nwsmosaic",
        R.id.radar_dual_pane to "wxogldual",
        R.id.radar_quad_pane to "wxoglquad",
        R.id.spc_comp_map to "spccompmap",
        R.id.spc_convective_outlooks to "spcsummary",
        R.id.spc_day_1 to "spcswod1",
        R.id.spc_day_2 to "spcswod2",
        R.id.spc_day_3 to "spcswod3",
        R.id.spc_day_4_8 to "spcswod4",
        R.id.spc_fire_outlooks to "spcfire",
        R.id.spc_href to "spchref",
        R.id.spc_hrrr to "spchrrr",
        R.id.spc_mesoanalysis to "spcmeso",
        R.id.spc_soundings to "spcsoundings",
        R.id.spc_sref to "spcsref",
        R.id.spc_storm_reports to "spcstormrpt1",
        R.id.spc_thunderstorm_outlooks to "spctstorm",
        R.id.spotters to "spotters",
        R.id.twitter_states to "twitter_state",
        R.id.twitter_tornado to "twitter_tornado",
        R.id.us_alerts to "uswarn",
        R.id.wpc_gefs to "wpcgefs",
        R.id.aurora to "aurora"
    )

    // The Pref ver below is important, it must stay as is and no other prefs must start with XZ_
    // This is referenced in ObjectSettingsCheckBox so that they are all true by default
    fun getPrefVar(token: String) = "XZ_NAV_DRAWER_$token"

    private const val navDrawerTokenPref = "NAV_DRAWER_TOKEN_LIST"

    fun getNavDrawerTokenList(context: Context) = Utility.readPref(context, navDrawerTokenPref, "")

    private fun setNavDrawerTokenList(context: Context, value: String) {
        Utility.writePref(context, navDrawerTokenPref, value)
        Utility.commitPref(context)
    }

    fun hideItems(context: Context, navigationView: NavigationView) {
        // if the token list is "" then it's set to the default which is show everything
        // it will be appropriately set the first time existing the settings -> UI -> nav drawer screen
        val tokenString = getNavDrawerTokenList(context)
        if (tokenString != "") {
            navDrawerIdToToken.forEach { (key, value) ->
                //val visible = Utility.readPref(context, getPrefVar(value), "")  == "true"
                val visible = tokenString.contains(":$value")
                if (!visible) {
                    val item = navigationView.menu.findItem(key)
                    item.isVisible = false
                }
            }
        }
    }

    // Go through all tokens and see if any are true
    // If one is true append it to the list which is colon separated
    // Write the string to storage
    fun generateNewTokenList(context: Context) {
        var tokenList = ""
        labelToTokenMap.forEach { (_, value) ->
            // check for not false since by default these aren't set at all
            if (Utility.readPref(context, getPrefVar(value), "") != "false") {
                tokenList += ":$value"
            }
        }
        // also write pref
        setNavDrawerTokenList(context, tokenList)
    }
}

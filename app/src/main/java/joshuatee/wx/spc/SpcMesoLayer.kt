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

package joshuatee.wx.spc

import android.content.Context
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.ui.ObjectToolbar
import joshuatee.wx.util.Utility

class SpcMesoLayer(
    val context: Context,
    val type: SpcMesoLayerType,
    objectToolbar: ObjectToolbar?,
    val getFunction: () -> Unit = {}
) {

    var isEnabled: Boolean
    private var menuItem: MenuItem? = null
    private val on = "(on) "

    init {
        isEnabled = Utility.readPref(context, prefTokens[type]!!, "false").startsWith("t")
        if (objectToolbar != null) {
            menuItem = objectToolbar.find(resourceIds[type]!!)
            if (isEnabled) {
                menuItem?.title = on + labels[type]
            }
        }
    }

    fun toggle() {
        if (isEnabled) {
            Utility.writePref(context, prefTokens[type]!!, "false")
            menuItem?.title = labels[type]
            isEnabled = false
        } else {
            isEnabled = true
            menuItem?.title = on + labels[type]
            Utility.writePref(context, prefTokens[type]!!, "true")
        }
        getFunction()
    }

    fun getUrl(sector: String) =
        "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/exper/mesoanalysis/s$sector/${urlToken[type]}/${urlToken[type]}.gif"

    companion object {
        val prefTokens = mapOf(
            SpcMesoLayerType.County to "SPCMESO_SHOW_COUNTY",
            SpcMesoLayerType.Outlook to "SPCMESO_SHOW_OUTLOOK",
            SpcMesoLayerType.Watwarn to "SPCMESO_SHOW_WATWARN",
            SpcMesoLayerType.Topography to "SPCMESO_SHOW_TOPO",
            SpcMesoLayerType.Radar to "SPCMESO_SHOW_RADAR",
            SpcMesoLayerType.Observations to "SPCMESO_SHOW_OBS",
            SpcMesoLayerType.Population to "SPCMESO_SHOW_POPULATION"

        )
        val resourceIds = mapOf(
            SpcMesoLayerType.County to R.id.action_toggleCounty,
            SpcMesoLayerType.Outlook to R.id.action_toggleSPCOutlook,
            SpcMesoLayerType.Watwarn to R.id.action_toggleWatWarn,
            SpcMesoLayerType.Topography to R.id.action_toggleTopography,
            SpcMesoLayerType.Radar to R.id.action_toggleRadar,
            SpcMesoLayerType.Observations to R.id.action_toggleObs,
            SpcMesoLayerType.Population to R.id.action_togglePopulation
        )
        val labels = mapOf(
            SpcMesoLayerType.County to "Counties",
            SpcMesoLayerType.Outlook to "SPC Day 1 Outlook",
            SpcMesoLayerType.Watwarn to "Watches/Warnings",
            SpcMesoLayerType.Topography to "Topography",
            SpcMesoLayerType.Radar to "Radar",
            SpcMesoLayerType.Observations to "Observations",
            SpcMesoLayerType.Population to "Population"
        )

        val urlToken = mapOf(
            SpcMesoLayerType.County to "cnty",
            SpcMesoLayerType.Outlook to "otlk",
            SpcMesoLayerType.Watwarn to "warns",
            SpcMesoLayerType.Topography to "topo",
            SpcMesoLayerType.Radar to "rgnlrad",
            SpcMesoLayerType.Observations to "bigsfc",
            SpcMesoLayerType.Population to "population"
        )
    }
}

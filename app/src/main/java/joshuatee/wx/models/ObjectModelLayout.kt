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

package joshuatee.wx.models

import android.app.Activity
import joshuatee.wx.R
import joshuatee.wx.util.To

class ObjectModelLayout(val activity: Activity, prefModel: String, numPanesStr: String) {

    val numPanes: Int
    var layoutSinglePane = R.layout.activity_models_generic_nospinner
    var layoutMultiPane = R.layout.activity_models_generic_multipane_nospinner
    var menuResId = R.menu.models_generic
    var topMenuResId = R.menu.models_generic_top

    init {
        numPanes = To.int(numPanesStr)
        when (prefModel) {
            "WPCGEFS" -> {

            }
            "NSSL" -> {

            }
            "ESRL" -> {

            }
            "NCEP" -> {

            }
            "SPCSREF" -> {
                layoutSinglePane = R.layout.activity_models_spcsref
                layoutMultiPane = R.layout.activity_models_spcsrefmultipane
                menuResId = R.menu.models_spcsref
                topMenuResId = R.menu.models_spcsref_top
            }
            "SPCHREF" -> {
                layoutSinglePane = R.layout.activity_models_spchref
                layoutMultiPane = R.layout.activity_models_spchrefmultipane
                menuResId = R.menu.models_spchref
                topMenuResId = R.menu.models_spchref_top
            }
            "SPCHRRR" -> {
                topMenuResId = R.menu.models_spchrrr_top
                menuResId = R.menu.models_spchrrr
            }
        }

    }
}

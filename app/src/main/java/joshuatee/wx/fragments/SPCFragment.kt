/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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

package joshuatee.wx.fragments

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import joshuatee.wx.R
import joshuatee.wx.models.ModelsSPCHRRRActivity
import joshuatee.wx.models.ModelsSPCSREFActivity
import joshuatee.wx.models.ModelsSPCHREFActivity
import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.spc.SPCCompmapActivity
import joshuatee.wx.spc.SPCFireOutlookActivity
import joshuatee.wx.spc.SPCMCDWShowSummaryActivity
import joshuatee.wx.spc.SPCMesoActivity
import joshuatee.wx.spc.SPCSWOActivity
import joshuatee.wx.spc.SPCSWOSummaryActivity
import joshuatee.wx.spc.SPCStormReportsActivity
import joshuatee.wx.spc.SPCTstormOutlookActivity
import joshuatee.wx.util.Utility

class SPCFragment : Fragment() {

    private val hm = mutableMapOf<String, TileObject>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recyclerview, container, false)
        val rowListItem = allItemList
        val lLayout = GridLayoutManager(activity, UIPreferences.tilesPerRow)
        val rView: RecyclerView = view.findViewById(R.id.recycler_view)
        rView.setHasFixedSize(true)
        rView.layoutManager = lLayout
        val rcAdapter = TileAdapter(context!!, rowListItem, UIPreferences.tilesPerRow, "FRAGMENT_SPC_ORDER")
        rView.adapter = rcAdapter
        val callback = SimpleItemTouchHelperCallback(rcAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(rView)
        return view
    }

    private val allItemList: MutableList<TileObject>
        get() {
            hm["spcsref"] = TileObject(R.drawable.spcsref, ModelsSPCSREFActivity::class.java, ModelsSPCSREFActivity.INFO,
                    arrayOf("1", "SPCSREF", "SPCSREF"), resources.getString(R.string.help_spc_sref), "spcsref")
            hm["spcsummary"] = TileObject(R.drawable.spc_sum, SPCSWOSummaryActivity::class.java, "",
                    arrayOf(), resources.getString(R.string.help_spc_swo_summary), "spcsummary")
            hm["spcswod1"] = TileObject(R.drawable.day1, SPCSWOActivity::class.java, SPCSWOActivity.NO,
                    arrayOf("1", ""), resources.getString(R.string.help_spc_swo_detail), "spcswod1")
            hm["spcswod2"] = TileObject(R.drawable.day2, SPCSWOActivity::class.java, SPCSWOActivity.NO,
                    arrayOf("2", ""), resources.getString(R.string.help_spc_swo_detail), "spcswod2")
            hm["spcswod3"] = TileObject(R.drawable.day3, SPCSWOActivity::class.java, SPCSWOActivity.NO,
                    arrayOf("3", ""), resources.getString(R.string.help_spc_swo_detail), "spcswod3")
            hm["spcswod48"] = TileObject(R.drawable.day48, SPCSWOActivity::class.java, SPCSWOActivity.NO,
                    arrayOf("4-8", ""), resources.getString(R.string.help_spc_swo_detail), "spcswod48")
            hm["spcstormrpt1"] = TileObject(R.drawable.report_today, SPCStormReportsActivity::class.java,
                    SPCStormReportsActivity.NO, arrayOf("today"), resources.getString(R.string.help_spc_storm_reports), "spcstormrpt1")
            hm["spcstormrpt2"] = TileObject(R.drawable.report_yesterday, SPCStormReportsActivity::class.java,
                    SPCStormReportsActivity.NO, arrayOf("yesterday"), resources.getString(R.string.help_spc_storm_reports), "spcstormrpt2")
            hm["spcmcd"] = TileObject(R.drawable.mcd_tile, SPCMCDWShowSummaryActivity::class.java, SPCMCDWShowSummaryActivity.NO,
                    arrayOf("mcd"), resources.getString(R.string.help_spc_mcd), "spcmcd")
            hm["spcwat"] = TileObject(R.drawable.wat, SPCMCDWShowSummaryActivity::class.java, SPCMCDWShowSummaryActivity.NO,
                    arrayOf("wat"), resources.getString(R.string.help_spc_watches), "spcwat")
            hm["spcmeso"] = TileObject(R.drawable.meso, SPCMesoActivity::class.java, SPCMesoActivity.INFO,
                    arrayOf("", "1", "SPCMESO"), resources.getString(R.string.help_spc_mesoanalysis), "spcmeso")
            hm["spcfire"] = TileObject(R.drawable.fire_outlook, SPCFireOutlookActivity::class.java, "",
                    arrayOf(""), resources.getString(R.string.help_spc_fire_weather), "spcfire")
            hm["spctstorm"] = TileObject(R.drawable.tstorm, SPCTstormOutlookActivity::class.java, "",
                    arrayOf(), resources.getString(R.string.help_spc_tstorm), "spctstorm")
            hm["spccompmap"] = TileObject(R.drawable.spccompmap, SPCCompmapActivity::class.java, "",
                    arrayOf(), resources.getString(R.string.help_spc_compmap), "spccompmap")
            hm["spchrrr"] = TileObject(R.drawable.spchrrr, ModelsSPCHRRRActivity::class.java, "",
                    arrayOf("1", "SPCHRRR", "SPC HRRR"), resources.getString(R.string.help_spchrrr_models), "spchrrr")
            hm["spchref"] = TileObject(R.drawable.spchref, ModelsSPCHREFActivity::class.java, "",
                    arrayOf("1", "SPCHREF", "SPC HREF"), resources.getString(R.string.help_spchref_models), "spchref")
            val tileOrder = "spcsref:spcsummary:spcswod1:spcswod2:spcswod3:spcswod48:spcstormrpt1:spcstormrpt2:spcmcd:spcwat:spcmeso:spcfire:spctstorm:spccompmap:"
            var spcPref: String = Utility.readPref("FRAGMENT_SPC_ORDER", tileOrder)
            if (!spcPref.contains("spchrrr")) {
                spcPref += "spchrrr:"
                Utility.writePref("FRAGMENT_SPC_ORDER", spcPref)
            }
            if (!spcPref.contains("spcsseo")) {
                spcPref += "spcsseo:"
                Utility.writePref("FRAGMENT_SPC_ORDER", spcPref)
            }
            if (!spcPref.contains("spchref")) {
                spcPref += "spchref:"
                Utility.writePref("FRAGMENT_SPC_ORDER", spcPref)
            }
            val tileOrderArr = MyApplication.colon.split(spcPref)
            return tileOrderArr
                    .filterNot { it.contains("modeltt") }.filterNot { it.contains("spcsseo") }
                    .mapTo(mutableListOf()) { hm[it]!! }
        }
}


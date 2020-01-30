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
import joshuatee.wx.models.ModelsSpcHrrrActivity
import joshuatee.wx.models.ModelsSpcSrefActivity
import joshuatee.wx.models.ModelsSpcHrefActivity
import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.spc.SpcCompmapActivity
import joshuatee.wx.spc.SpcFireOutlookActivity
import joshuatee.wx.spc.SpcMcdWatchShowSummaryActivity
import joshuatee.wx.spc.SpcMesoActivity
import joshuatee.wx.spc.SpcSwoActivity
import joshuatee.wx.spc.SpcSwoSummaryActivity
import joshuatee.wx.spc.SpcStormReportsActivity
import joshuatee.wx.spc.SpcThunderStormOutlookActivity
import joshuatee.wx.util.Utility

class SpcFragment : Fragment() {

    private val hm = mutableMapOf<String, TileObject>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
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
            hm["spcsref"] = TileObject(
                    R.drawable.spcsref,
                    ModelsSpcSrefActivity::class.java,
                    ModelsSpcSrefActivity.INFO,
                    arrayOf("1", "SPCSREF", "SPCSREF"),
                    resources.getString(R.string.help_spc_sref),
                    "spcsref", "SREF"
            )
            hm["spcsummary"] = TileObject(
                    R.drawable.spc_sum, SpcSwoSummaryActivity::class.java, "",
                    arrayOf(), resources.getString(R.string.help_spc_swo_summary), "spcsummary", "Convective Summary by Image"
            )
            hm["spcswod1"] = TileObject(
                    R.drawable.day1, SpcSwoActivity::class.java, SpcSwoActivity.NO,
                    arrayOf("1", ""), resources.getString(R.string.help_spc_swo_detail), "spcswod1", "Day 1"
            )
            hm["spcswod2"] = TileObject(
                    R.drawable.day2, SpcSwoActivity::class.java, SpcSwoActivity.NO,
                    arrayOf("2", ""), resources.getString(R.string.help_spc_swo_detail), "spcswod2", "Day 2"
            )
            hm["spcswod3"] = TileObject(
                    R.drawable.day3, SpcSwoActivity::class.java, SpcSwoActivity.NO,
                    arrayOf("3", ""), resources.getString(R.string.help_spc_swo_detail), "spcswod3", "Day 3"
            )
            hm["spcswod48"] = TileObject(
                    R.drawable.day48, SpcSwoActivity::class.java, SpcSwoActivity.NO,
                    arrayOf("4-8", ""), resources.getString(R.string.help_spc_swo_detail), "spcswod48", "Day 4 through 8"
            )
            hm["spcstormrpt1"] = TileObject(
                    R.drawable.report_today,
                    SpcStormReportsActivity::class.java,
                    SpcStormReportsActivity.NO,
                    arrayOf("today"),
                    resources.getString(R.string.help_spc_storm_reports),
                    "spcstormrpt1", "Storm reports today"
            )
            hm["spcstormrpt2"] = TileObject(
                    R.drawable.report_yesterday,
                    SpcStormReportsActivity::class.java,
                    SpcStormReportsActivity.NO,
                    arrayOf("yesterday"),
                    resources.getString(R.string.help_spc_storm_reports),
                    "spcstormrpt2", "Storm reports yesterday"
            )
            hm["spcmcd"] = TileObject(
                    R.drawable.mcd_tile,
                    SpcMcdWatchShowSummaryActivity::class.java,
                    SpcMcdWatchShowSummaryActivity.NO,
                    arrayOf("mcd"),
                    resources.getString(R.string.help_spc_mcd),
                    "spcmcd", "MCD"
            )
            hm["spcwat"] = TileObject(
                    R.drawable.wat,
                    SpcMcdWatchShowSummaryActivity::class.java,
                    SpcMcdWatchShowSummaryActivity.NO,
                    arrayOf("wat"),
                    resources.getString(R.string.help_spc_watches),
                    "spcwat", "Watches"
            )
            hm["spcmeso"] = TileObject(
                    R.drawable.meso,
                    SpcMesoActivity::class.java,
                    SpcMesoActivity.INFO,
                    arrayOf("", "1", "SPCMESO"),
                    resources.getString(R.string.help_spc_mesoanalysis),
                    "spcmeso", "Mesoanalysis"
            )
            hm["spcfire"] = TileObject(
                    R.drawable.fire_outlook, SpcFireOutlookActivity::class.java, "",
                    arrayOf(""), resources.getString(R.string.help_spc_fire_weather), "spcfire", "Fire outlooks"
            )
            hm["spctstorm"] = TileObject(
                    R.drawable.tstorm, SpcThunderStormOutlookActivity::class.java, "",
                    arrayOf(), resources.getString(R.string.help_spc_tstorm), "spctstorm", "Thunderstorm outlooks"
            )
            hm["spccompmap"] = TileObject(
                    R.drawable.spccompmap, SpcCompmapActivity::class.java, "",
                    arrayOf(), resources.getString(R.string.help_spc_compmap), "spccompmap", "Compmap"
            )
            hm["spchrrr"] = TileObject(
                    R.drawable.spchrrr,
                    ModelsSpcHrrrActivity::class.java,
                    "",
                    arrayOf("1", "SPCHRRR", "SPC HRRR"),
                    resources.getString(R.string.help_spchrrr_models),
                    "spchrrr", "HRRR"
            )
            hm["spchref"] = TileObject(
                    R.drawable.spchref,
                    ModelsSpcHrefActivity::class.java,
                    "",
                    arrayOf("1", "SPCHREF", "SPC HREF"),
                    resources.getString(R.string.help_spchref_models),
                    "spchref", "HREF"
            )
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


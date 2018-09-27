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

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.util.Utility
import joshuatee.wx.vis.USNWSGOESActivity

class ImagesFragmentGOES : Fragment() {

    private val hm = mutableMapOf<String, TileObject>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recyclerview, container, false)
        val rowListItem = allItemList
        val lLayout = GridLayoutManager(activity, 4)
        val rView: RecyclerView = view.findViewById(R.id.recycler_view)
        rView.setHasFixedSize(true)
        rView.layoutManager = lLayout
        val rcAdapter = TileAdapter(context!!, rowListItem, 4, "FRAGMENT_IMGGOES_ORDER")
        rView.adapter = rcAdapter
        val callback = SimpleItemTouchHelperCallback(rcAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(rView)
        return view
    }

    private val allItemList: MutableList<TileObject>
        get() {
            val nw = TileObject(R.drawable.nw_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "nw", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "nw")
            hm["nw"] = nw
            val np = TileObject(R.drawable.np_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "np", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "np")
            hm["np"] = np
            val gl = TileObject(R.drawable.gl_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "gl", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "gl")
            hm["gl"] = gl
            val ne = TileObject(R.drawable.ne_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "ne", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "ne")
            hm["ne"] = ne
            val wc = TileObject(R.drawable.wc_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "wc", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "wc")
            hm["wc"] = wc
            val cp = TileObject(R.drawable.cp_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "cp", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "cp")
            hm["cp"] = cp
            val mw = TileObject(R.drawable.mw_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "mw", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "mw")
            hm["mw"] = mw
            val ma = TileObject(R.drawable.ma_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "ma", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "ma")
            hm["ma"] = ma
            val sw = TileObject(R.drawable.sw_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "sw", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "sw")
            hm["sw"] = sw
            val sc = TileObject(R.drawable.sc_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "sc", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "sc")
            hm["sc"] = sc
            val se = TileObject(R.drawable.se_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "se", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "se")
            hm["se"] = se
            val pr = TileObject(R.drawable.pr_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "pr", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "pr")
            hm["pr"] = pr
            val hi = TileObject(R.drawable.hi_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "hi", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "hi")
            hm["hi"] = hi
            val ak = TileObject(R.drawable.ak_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "ak", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "ak")
            hm["ak"] = ak
            val epac = TileObject(R.drawable.epac_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "epac", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "epac")
            hm["epac"] = epac
            val watl = TileObject(R.drawable.watl_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "watl", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "watl")
            hm["watl"] = watl
            val weus = TileObject(R.drawable.weus_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "weus", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "weus")
            hm["weus"] = weus
            val ceus = TileObject(R.drawable.ceus_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "ceus", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "ceus")
            hm["ceus"] = ceus
            val eaus = TileObject(R.drawable.eaus_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "eaus", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "eaus")
            hm["eaus"] = eaus
            val nhem = TileObject(R.drawable.nhem_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "nhem", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "nhem")
            hm["nhem"] = nhem
            val nepac = TileObject(R.drawable.nepac_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "nepac", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "nepac")
            hm["nepac"] = nepac
            val e = TileObject(R.drawable.e_epac_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "e", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "e")
            hm["e"] = e
            val carb = TileObject(R.drawable.carb_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "carb", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "carb")
            hm["carb"] = carb
            val nwatl = TileObject(R.drawable.nwatl_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "nwatl", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "nwatl")
            hm["nwatl"] = nwatl
            val watl2 = TileObject(R.drawable.watl_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "watl", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "watl2")
            hm["watl2"] = watl2
            val gmex = TileObject(R.drawable.gmex_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "gmex", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "gmex")
            hm["gmex"] = gmex
            val catl = TileObject(R.drawable.catl_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "catl", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "catl")
            hm["catl"] = catl
            val natl = TileObject(R.drawable.natl_sm, USNWSGOESActivity::class.java, USNWSGOESActivity.RID,
                    arrayOf("nws", "natl", "tab"),
                    resources.getString(R.string.help_goes_image_browser), "natl")
            hm["natl"] = natl
            val tileOrder = "nw:np:gl:ne:wc:cp:mw:ma:sw:sc:se:pr:hi:ak:epac:watl:weus:ceus:eaus:nhem:nepac:e:carb:nwatl:watl2:gmex:catl:natl:"
            val tileOrderArr = MyApplication.colon.split(Utility.readPref("FRAGMENT_IMGGOES_ORDER", tileOrder))
            return tileOrderArr
                    .filterNot {
                        it.contains("nw") || it.contains("np") || it.contains("gl")
                                || it.contains("gl")
                                || it.contains("ne")
                                || it.contains("wc")
                                || it.contains("cp")
                                || it.contains("mw")
                                || it.contains("ma")
                                || it.contains("sw")
                                || it.contains("sc")
                                || it.contains("se")
                                || it.contains("pr")
                                || it.contains("watl")
                                || it.contains("ceus")
                                || it.contains("eaus")
                                || it.contains("nhem")
                                || it.contains("carb")
                                || it.contains("gmex")
                                || it.contains("natl")
                                || it.contains("catl")
                                || it == ("e")
                    }
                    .mapTo(mutableListOf()) { hm[it]!! }
        }
}



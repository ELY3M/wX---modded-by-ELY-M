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
//modded by ELY M.
//aurora add-in

package joshuatee.wx.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import joshuatee.wx.R
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.misc.ImageCollectionActivity
import joshuatee.wx.misc.NwsObsSitesActivity
import joshuatee.wx.misc.USAlertsActivity
import joshuatee.wx.models.ModelsGenericActivity
import joshuatee.wx.nhc.NhcActivity
import joshuatee.wx.radar.RadarMosaicActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.vis.GoesActivity
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.radar.WXGLRadarActivityMultiPane
import joshuatee.wx.util.Utility
import joshuatee.wx.wpc.NationalImagesActivity
import joshuatee.wx.wpc.RainfallOutlookSummaryActivity
import joshuatee.wx.wpc.NationalTextActivity

class MiscFragment : Fragment() {

    private val hm = mutableMapOf<String, TileObject>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recyclerview, container, false)
        TabScreen(requireActivity(), view, "FRAGMENT_MISC_ORDER", tileObjects)
        return view
    }

    private val tileObjects: MutableList<TileObject>
        get() {
            hm["model_ncep"] = TileObject(
                R.drawable.ncep,
                ModelsGenericActivity::class.java,
                ModelsGenericActivity.INFO,
                arrayOf("1", "NCEP", "NCEP"),
                "model_ncep", "NCEP"
            )
            hm["model_hrrr"] = TileObject(
                R.drawable.hrrrviewer,
                ModelsGenericActivity::class.java,
                ModelsGenericActivity.INFO,
                arrayOf("1", "ESRL", "ESRL"),
                "model_hrrr", "HRRR"
            )

            hm["uswarn"] = TileObject(
                R.drawable.uswarn,
                USAlertsActivity::class.java,
                USAlertsActivity.URL,
                arrayOf(
                    ".*?Tornado Warning.*?|.*?Severe Thunderstorm Warning.*?|.*?Flash Flood Warning.*?",
                    "us"
                ),
                "uswarn", "US Warnings"
            )
            hm["wpctext"] = TileObject(
                R.drawable.srfd,
                NationalTextActivity::class.java,
                NationalTextActivity.URL,
                arrayOf("pmdspd", "Short Range Forecast Discussion"),
                "wpctext", "National text products"
            )
            hm["nhc"] = TileObject(
                R.drawable.nhc,
                NhcActivity::class.java,
                "",
                arrayOf(),
                "nhc", "NHC"
            )
            hm["nwsmosaic"] = TileObject(
                R.drawable.nws_sector,
                RadarMosaicActivity::class.java,
                RadarMosaicActivity.URL,
                arrayOf("sector"),
                "nwsmosaic", "NWS Radar Mosaics"
            )
            hm["goes"] = TileObject(
                R.drawable.goes,
                GoesActivity::class.java,
                GoesActivity.RID,
                arrayOf("CONUS", "9"),
                "goes", "GOES"
            )
            hm["lightning"] = TileObject(
                R.drawable.lightning,
                GoesActivity::class.java,
                GoesActivity.RID,
                arrayOf("CONUS", "23"),
                "lightning", "lightning"
            )
            hm["wpcimages"] = TileObject(
                R.drawable.fmap,
                NationalImagesActivity::class.java,
                "",
                arrayOf(),
                "wpcimages", "National Images"
            )
            hm["opc"] = TileObject(
                R.drawable.opc,
                ImageCollectionActivity::class.java,
                ImageCollectionActivity.TYPE,
                arrayOf("OPC"),
                "opc", "OPC"
            )
            hm["goesfulldisk"] = TileObject(
                R.drawable.goesfulldisk,
                ImageCollectionActivity::class.java,
                ImageCollectionActivity.TYPE,
                arrayOf("GOESFD"),
                "goesfulldisk", "GOES Full Disk"
            )
            hm["nwsobs"] = TileObject(
                R.drawable.nwsobssites,
                NwsObsSitesActivity::class.java,
                "",
                arrayOf(),
                "nwsobs", "Observation sites"
            )
            if (!UIPreferences.dualpaneRadarIcon) {
                hm["wxogl"] = TileObject(
                    R.drawable.wxogldualpane,
                    WXGLRadarActivityMultiPane::class.java,
                    WXGLRadarActivityMultiPane.RID,
                    arrayOf(Location.rid, "", "2"),
                    "wxogl", "Dual pane nexrad radar"
                )
            } else {
                hm["wxogl"] = TileObject(
                    R.drawable.wxoglsinglepane,
                    WXGLRadarActivity::class.java,
                    WXGLRadarActivity.RID,
                    arrayOf(Location.rid, ""),
                    "wxogl", "Single pane nexrad radar"
                )
            }
            Location.checkCurrentLocationValidity()
            hm["wxoglquad"] = TileObject(
                R.drawable.wxoglquadpane,
                WXGLRadarActivityMultiPane::class.java,
                WXGLRadarActivityMultiPane.RID,
                arrayOf(Location.rid, "", "4"),
                "wxoglquad", "Dual pane nexrad radar"
            )
            hm["model_nssl_wrf"] = TileObject(
                R.drawable.nsslwrf,
                ModelsGenericActivity::class.java,
                ModelsGenericActivity.INFO,
                arrayOf("1", "NSSL", "NSSL"),
                "model_nssl_wrf", "WRF"
            )
            hm["goes16"] = TileObject(
                R.drawable.goes16,
                GoesActivity::class.java,
                GoesActivity.RID,
                arrayOf(""),
                "goes16", "GOES"
            )
            hm["wpcgefs"] = TileObject(
                R.drawable.wpcgefs,
                ModelsGenericActivity::class.java,
                ModelsGenericActivity.INFO,
                arrayOf("1", "WPCGEFS", "WPC"),
                "wpcgefs", "WPC GEFS"
            )
            hm["wpc_rainfall"] = TileObject(
                R.drawable.wpc_rainfall,
                RainfallOutlookSummaryActivity::class.java,
                "",
                arrayOf(),
                "wpc_rainfall", "WPC RAINFALL"
            )
	    //elys mod
            hm["aurora"] = TileObject(
                    R.drawable.auroralforecast,
                    ImageCollectionActivity::class.java,
                    ImageCollectionActivity.TYPE,
                    arrayOf("AURORA"),
                    "aurora", "Aurora Forecast"
            )
	    //end
            val tileOrder = "model_ncep:model_hrrr:model_ncar_ensemble:uswarn:wpctext:nhc:nwsmosaic:goes:lightning:wpcimages:opc:goesfulldisk:nwsobs:wxogl:wxoglquad:wpc_rainfall:"
            var miscPref = Utility.readPref("FRAGMENT_MISC_ORDER", tileOrder)
            if (!miscPref.contains("wxoglquad")) {
                miscPref += "wxoglquad:"
                Utility.writePref("FRAGMENT_MISC_ORDER", miscPref)
            }
            miscPref = Utility.readPref("FRAGMENT_MISC_ORDER", tileOrder)
            if (!miscPref.contains("model_ncar_ensemble")) {
                miscPref += "model_ncar_ensemble:"
                Utility.writePref("FRAGMENT_MISC_ORDER", miscPref)
            }
            miscPref = Utility.readPref("FRAGMENT_MISC_ORDER", tileOrder)
            if (!miscPref.contains("model_nssl_wrf")) {
                miscPref += "model_nssl_wrf:"
                Utility.writePref("FRAGMENT_MISC_ORDER", miscPref)
            }
            miscPref = Utility.readPref("FRAGMENT_MISC_ORDER", tileOrder)
            if (!miscPref.contains("goes16")) {
                miscPref += "goes16:"
                Utility.writePref("FRAGMENT_MISC_ORDER", miscPref)
            }
            miscPref = Utility.readPref("FRAGMENT_MISC_ORDER", tileOrder)
            if (!miscPref.contains("wpcgefs")) {
                miscPref += "wpcgefs:"
                Utility.writePref("FRAGMENT_MISC_ORDER", miscPref)
            }
            miscPref = Utility.readPref("FRAGMENT_MISC_ORDER", tileOrder)
            if (!miscPref.contains("wpc_rainfall")) {
                miscPref += "wpc_rainfall:"
                Utility.writePref("FRAGMENT_MISC_ORDER", miscPref)
            }
	    //elys mod
            miscPref = Utility.readPref("FRAGMENT_MISC_ORDER", tileOrder)
            if (!miscPref.contains("aurora")) {
                miscPref += "aurora:"
                Utility.writePref("FRAGMENT_MISC_ORDER", miscPref)
            }
	    //end
            val tiles = miscPref.split(":").dropLastWhile { it.isEmpty() }
            return tiles
                .filterNot {
                    it.contains("model_cod") || it.contains("model_wrf") || it.contains("model_ncar_ensemble") || it.contains(
                        "twitter"
                    )
                }
                .map { hm[it]!! }.toMutableList()
        }
}

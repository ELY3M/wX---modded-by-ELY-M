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
import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.activitiesmisc.*
import joshuatee.wx.models.ModelsGenericActivity
import joshuatee.wx.nhc.NHCActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.vis.GOES16Activity
import joshuatee.wx.radar.USNWSMosaicActivity
import joshuatee.wx.radar.WXGLRadarActivity
import joshuatee.wx.radar.WXGLRadarActivityMultiPane
import joshuatee.wx.util.Utility
import joshuatee.wx.wpc.WPCImagesActivity
import joshuatee.wx.wpc.WPCTextProductsActivity

class MiscFragment : Fragment() {

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
        val rcAdapter =
            TileAdapter(context!!, rowListItem, UIPreferences.tilesPerRow, "FRAGMENT_MISC_ORDER")
        rView.adapter = rcAdapter
        val callback = SimpleItemTouchHelperCallback(rcAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(rView)
        return view
    }

    private val allItemList: MutableList<TileObject>
        get() {
            hm["model_ncep"] = TileObject(
                R.drawable.ncep,
                ModelsGenericActivity::class.java,
                ModelsGenericActivity.INFO,
                arrayOf("1", "NCEP", "NCEP"),
                resources.getString(R.string.help_ncep_models),
                "model_ncep"
            )
            //hm["model_hrrr"] = TileObject(R.drawable.hrrrviewer, ModelsESRLActivity::class.java, ModelsESRLActivity.INFO, arrayOf("1", "ESRL"), resources.getString(R.string.help_hrrr_viewer), "model_hrrr")
            hm["model_hrrr"] = TileObject(
                R.drawable.hrrrviewer,
                ModelsGenericActivity::class.java,
                ModelsGenericActivity.INFO,
                arrayOf("1", "ESRL", "ESRL"),
                resources.getString(R.string.help_hrrr_viewer),
                "model_hrrr"
            )

            hm["uswarn"] = TileObject(
                R.drawable.warn,
                USWarningsWithRadarActivity::class.java,
                USWarningsWithRadarActivity.URL,
                arrayOf(
                    ".*?Tornado Warning.*?|.*?Severe Thunderstorm Warning.*?|.*?Flash Flood Warning.*?",
                    "us"
                ),
                resources.getString(R.string.help_uswarn),
                "uswarn"
            )
            hm["wpctext"] = TileObject(
                R.drawable.srfd,
                WPCTextProductsActivity::class.java,
                WPCTextProductsActivity.URL,
                arrayOf("pmdspd", "Short Range Forecast Discussion"),
                resources.getString(R.string.help_wpc_text_products),
                "wpctext"
            )
            hm["nhc"] = TileObject(
                R.drawable.nhc,
                NHCActivity::class.java,
                "",
                arrayOf(),
                resources.getString(R.string.help_nhc),
                "nhc"
            )
            hm["nwsmosaic"] = TileObject(
                R.drawable.nws_sector,
                USNWSMosaicActivity::class.java,
                USNWSMosaicActivity.URL,
                arrayOf("", ""),
                resources.getString(R.string.help_nws_radar_mosaics),
                "nwsmosaic"
            )
            hm["goes"] = TileObject(
                R.drawable.goes,
                GOES16Activity::class.java,
                GOES16Activity.RID,
                arrayOf("CONUS", "09"),
                resources.getString(R.string.help_goes_viewer),
                "goes"
            )
            hm["lightning"] = TileObject(
                R.drawable.lightning,
                LightningActivity::class.java,
                "",
                arrayOf(),
                resources.getString(R.string.help_lightning),
                "lightning"
            )
            hm["wpcimages"] = TileObject(
                R.drawable.fmap,
                WPCImagesActivity::class.java,
                "",
                arrayOf(),
                resources.getString(R.string.help_wpc_images),
                "wpcimages"
            )
            hm["twitter_state"] = TileObject(
                R.drawable.twstate,
                WebscreenABState::class.java,
                "",
                arrayOf(),
                resources.getString(R.string.help_twitter),
                "twitter_state"
            )
            hm["twitter_tornado"] = TileObject(
                R.drawable.twtornado, WebscreenAB::class.java, WebscreenAB.URL,
                arrayOf(
                    "<html><meta name=\"viewport\" content=\"width=device-width, user-scalable=no\" /> <body width=\"100%\"><div><script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+\"://platform.twitter.com/widgets.js\";fjs.parentNode.insertBefore(js,fjs);}}(document,\"script\",\"twitter-wjs\");</script><html><a class=\"twitter-timeline\" data-dnt=\"true\" href=\"https://twitter.com/search?q=%23tornado\" data-widget-id=\"406096257220763648\" data-chrome=\"noscrollbar noheader nofooter noborders \" data-tweet-limit=20>Tweets about \"#tornado\"</a></div></body></html>",
                    "#tornado"
                ),
                resources.getString(R.string.help_twitter), "twitter_tornado"
            )
            hm["opc"] = TileObject(
                R.drawable.opc,
                ImageCollectionActivity::class.java,
                ImageCollectionActivity.TYPE,
                arrayOf("OPC"),
                resources.getString(R.string.help_opc),
                "opc"
            )
            hm["goesfulldisk"] = TileObject(
                R.drawable.goesfulldisk,
                ImageCollectionActivity::class.java,
                ImageCollectionActivity.TYPE,
                arrayOf("GOESFD"),
                resources.getString(R.string.help_goesfulldisk),
                "goesfulldisk"
            )
            hm["nwsobs"] = TileObject(
                R.drawable.nwsobs,
                NWSObsSitesActivity::class.java,
                "",
                arrayOf(),
                resources.getString(R.string.help_nws_obs_sites),
                "nwsobs"
            )
            if (!UIPreferences.dualpaneRadarIcon) {
                hm["wxogl"] = TileObject(
                    R.drawable.wxogldualpane,
                    WXGLRadarActivityMultiPane::class.java,
                    WXGLRadarActivityMultiPane.RID,
                    arrayOf(Location.rid, "", "2"),
                    "",
                    "wxogl"
                )
            } else {
                hm["wxogl"] = TileObject(
                    R.drawable.wxoglsinglepane,
                    WXGLRadarActivity::class.java,
                    WXGLRadarActivity.RID,
                    arrayOf(Location.rid, ""),
                    "",
                    "wxogl"
                )
            }
            Location.checkCurrentLocationValidity()
            //val locInt = Location.currentLocation
            hm["wxoglquad"] = TileObject(
                R.drawable.wxoglquadpane,
                WXGLRadarActivityMultiPane::class.java,
                WXGLRadarActivityMultiPane.RID,
                arrayOf(Location.rid, "", "4"),
                "",
                "wxoglquad"
            )
            hm["model_nssl_wrf"] = TileObject(
                R.drawable.nsslwrf,
                ModelsGenericActivity::class.java,
                ModelsGenericActivity.INFO,
                arrayOf("1", "NSSL", "NSSL"),
                resources.getString(R.string.help_models_nssl_wrf),
                "model_nssl_wrf"
            )
            hm["goes16"] = TileObject(
                R.drawable.goes16,
                GOES16Activity::class.java,
                GOES16Activity.RID,
                arrayOf(""),
                resources.getString(R.string.help_goes16),
                "goes16"
            )
            hm["wpcgefs"] = TileObject(
                R.drawable.wpcgefs,
                ModelsGenericActivity::class.java,
                ModelsGenericActivity.INFO,
                arrayOf("1", "WPCGEFS", "WPC"),
                resources.getString(R.string.help_wpcgefs),
                "wpcgefs"
            )
            val tileOrder =
                "model_ncep:model_hrrr:model_ncar_ensemble:uswarn:wpctext:nhc:nwsmosaic:goes:lightning:wpcimages:twitter_state:twitter_tornado:opc:goesfulldisk:nwsobs:wxogl:wxoglquad:"

            var miscPref: String = Utility.readPref("FRAGMENT_MISC_ORDER", tileOrder)
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
            val tileOrderArr = MyApplication.colon.split(miscPref)
            return tileOrderArr
                .filterNot { it.contains("model_cod") || it.contains("model_wrf") || it.contains("model_ncar_ensemble") }
                .mapTo(mutableListOf()) { hm[it]!! }
        }
}


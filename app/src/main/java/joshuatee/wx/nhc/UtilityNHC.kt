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

package joshuatee.wx.nhc

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication

object UtilityNhc {

    const val widgetImageUrlTop = "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_atl_0d0.png"
    const val widgetImageUrlBottom = "${MyApplication.nwsNhcWebsitePrefix}/xgtwo/two_pac_0d0.png"

    fun getHurricaneInfo(): List<ObjectNhcStormDetails> {
        val stormDataList = mutableListOf<ObjectNhcStormDetails>()
        val url = MyApplication.nwsNhcWebsitePrefix + "/CurrentStorms.json"
        //val url = "https://www.nhc.noaa.gov/productexamples/NHC_JSON_Sample.json"
        val html = url.getHtml()
        val ids = html.parseColumn("\"id\": \"(.*?)\"")
        val binNumbers = html.parseColumn("\"binNumber\": \"(.*?)\"")
        val names = html.parseColumn("\"name\": \"(.*?)\"")
        val classifications = html.parseColumn("\"classification\": \"(.*?)\"")
        val intensities = html.parseColumn("\"intensity\": \"(.*?)\"")
        val pressures = html.parseColumn("\"pressure\": \"(.*?)\"")
        // sample data not quoted for these two
        //intensities = html.parseColumn("\"intensity\": (.*?),");
        //pressures = html.parseColumn("\"pressure\": (.*?),");
        //
        val latitudes = html.parseColumn("\"latitude\": \"(.*?)\"")
        val longitudes = html.parseColumn("\"longitude\": \"(.*?)\"")
        val movementDirs = html.parseColumn("\"movementDir\": (.*?),")
        val movementSpeeds = html.parseColumn("\"movementSpeed\": (.*?),")
        val lastUpdates = html.parseColumn("\"lastUpdate\": \"(.*?)\"")
        //binNumbers.forEach {
        //    val text = UtilityDownload.getTextProduct(context, "MIATCP" + it)
        //    val status = text.parseFirst("(\\.\\.\\..*?\\.\\.\\.)")
        //    statusList.add(status)
        //}

        if (ids.isNotEmpty()) {
            ids.indices.forEach { index ->
                val objectNhcStormDetails = ObjectNhcStormDetails(
                        names[index],
                        movementDirs[index],
                        movementSpeeds[index],
                        pressures[index],
                        binNumbers[index],
                        ids[index],
                        lastUpdates[index],
                        classifications[index],
                        latitudes[index],
                        longitudes[index],
                        intensities[index],
                        ""
                )
                stormDataList.add(objectNhcStormDetails)
                //val card = ObjectCardNhcStormReportItem(context, linearLayout, objectNhcStormDetails)
                //card.setListener(View.OnClickListener { ObjectIntent.showNhcStorm(context, objectNhcStormDetails) })
            }
        }
        return stormDataList
    }

    fun getImage(rid: String, prod: String) = ("https://www.ssd.noaa.gov/PS/TROP/floaters/" + rid + "/imagery/" + prod + "0.gif").getImage()
}

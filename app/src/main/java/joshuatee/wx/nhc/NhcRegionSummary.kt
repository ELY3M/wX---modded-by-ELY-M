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

package joshuatee.wx.nhc

import android.graphics.Bitmap
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.common.GlobalVariables

class NhcRegionSummary(region: NhcOceanEnum) {

    var urls: List<String>
    var titles: List<String>
    var bitmaps = listOf<Bitmap>()
    private var replaceString: String
    private var baseUrl: String

    init {
        when (region) {
            NhcOceanEnum.ATL -> {
                titles = listOf(
                        "Atlantic Tropical Cyclones and Disturbances ",
                        "ATL: Two-Day Graphical Tropical Weather Outlook",
                        "ATL: Five-Day Graphical Tropical Weather Outlook"
                )
                urls = listOf(
                        "${GlobalVariables.nwsNhcWebsitePrefix}/xgtwo/two_atl_0d0.png",
                        "${GlobalVariables.nwsNhcWebsitePrefix}/xgtwo/two_atl_2d0.png",
                        "${GlobalVariables.nwsNhcWebsitePrefix}/xgtwo/two_atl_5d0.png"
                )
                replaceString = "NHC Atlantic Wallet"
                baseUrl = "${GlobalVariables.nwsNhcWebsitePrefix}/nhc_at"
            }
            NhcOceanEnum.EPAC -> {
                titles = listOf(
                        "EPAC Tropical Cyclones and Disturbances ",
                        "EPAC: Two-Day Graphical Tropical Weather Outlook",
                        "EPAC: Five-Day Graphical Tropical Weather Outlook"
                )
                urls = listOf(
                        "${GlobalVariables.nwsNhcWebsitePrefix}/xgtwo/two_pac_0d0.png",
                        "${GlobalVariables.nwsNhcWebsitePrefix}/xgtwo/two_pac_2d0.png",
                        "${GlobalVariables.nwsNhcWebsitePrefix}/xgtwo/two_pac_5d0.png"
                )
                replaceString = "NHC Eastern Pacific Wallet"
                baseUrl = "${GlobalVariables.nwsNhcWebsitePrefix}/nhc_ep"
            }
            NhcOceanEnum.CPAC -> {
                titles = listOf(
                        "CPAC Tropical Cyclones and Disturbances ",
                        "CPAC: Two-Day Graphical Tropical Weather Outlook",
                        "CPAC: Five-Day Graphical Tropical Weather Outlook"
                )
                urls = listOf(
                        "${GlobalVariables.nwsNhcWebsitePrefix}/xgtwo/two_cpac_0d0.png",
                        "${GlobalVariables.nwsNhcWebsitePrefix}/xgtwo/two_cpac_2d0.png",
                        "${GlobalVariables.nwsNhcWebsitePrefix}/xgtwo/two_cpac_5d0.png"
                )
                replaceString = ""
                baseUrl = ""
            }
        }
    }

    fun getImages() {
        bitmaps = urls.map { it.getImage() }
    }

    fun getTitle(index: Int) = arrayOf(urls[index], titles[index])
}

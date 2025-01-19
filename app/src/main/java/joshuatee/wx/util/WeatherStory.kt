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

package joshuatee.wx.util

import joshuatee.wx.settings.Location

object WeatherStory {

    fun getUrl(): String {
        var productUrl = ""
        val site = Location.wfo.lowercase()
        var url = "https://www.weather.gov/$site/weatherstory"
        val html = UtilityIO.getHtml(url)
        var scrapeUrl = UtilityString.parse(
            html,
            "src=.(https://www.weather.gov/?/images/.../wxstory/Tab.FileL.png). "
        )
        if (scrapeUrl.isNotEmpty()) {
            productUrl = scrapeUrl
        } else {
            scrapeUrl =
                UtilityString.parse(html, "a href=.(/images/\\w{3}/features/weatherstory.png).")
            if (scrapeUrl.isNotEmpty()) {
                productUrl = "https://www.weather.gov/$scrapeUrl"
            } else {
                scrapeUrl =
                    UtilityString.parse(
                        html,
                        "[=o].(/images/\\w{3}/[wW]eather[Ss]tory.(gif|jpg|png))."
                    )
                if (scrapeUrl.isNotEmpty()) {
                    productUrl = "https://www.weather.gov/$scrapeUrl"
                } else {
                    scrapeUrl =
                        UtilityString.parse(
                            html,
                            "img src=.(https://www.weather.gov/?/images/\\w{3}/WxStory/WeatherStory[1-9].png)."
                        )
                    if (scrapeUrl.isNotEmpty()) {
                        productUrl = scrapeUrl
                    } else {
                        scrapeUrl =
                            UtilityString.parse(
                                html,
                                "[= o].(/images/\\w{3}/[Ww]eather[Ss]tory/weatherstory.(png|gif))."
                            )
                        if (scrapeUrl.isNotEmpty()) {
                            productUrl = "https://www.weather.gov/$scrapeUrl"
                        } else {
                            url = "https://weather.gov/$site"
                            val html = UtilityIO.getHtml(url)
                            //                             <div class="image"><img       src="http://www.weather.gov/images//mob/graphicast/image8.png" style="max-width: 100%;"></div>
                            //                             <div class="image"><img       src="http://www.weather.gov/images/abq/graphicast/image1.gif" style="max-width: 100%;"></div>
                            val scrapeUrls1 = UtilityString.parseColumn(
                                html,
                                "src=.(https?://www.weather.gov/images/?/.../graphicast/\\S*?[0-9]{0,1}.png). "
                            )
                            val scrapeUrls2 = UtilityString.parseColumn(
                                html,
                                "src=.(https?://www.weather.gov/images/?/.../graphicast/\\S*?[0-9]{0,1}.jpg). "
                            )
                            val scrapeUrls3 = UtilityString.parseColumn(
                                html,
                                "src=.(https?://www.weather.gov/images/?/.../graphicast/\\S*?[0-9]{0,1}.gif). "
                            )
                            val scrapeUrlsAll = scrapeUrls1 + scrapeUrls2 + scrapeUrls3
                            if (scrapeUrlsAll.isNotEmpty()) {
                                productUrl = scrapeUrlsAll[0]
                            } else {
                                val scrapeUrls = UtilityString.parseColumn(
                                    html,
                                    "src=.(https?://www.weather.gov/?/images/?/.../WxStory/\\S*?[0-9].png). "
                                )
                                if (scrapeUrls.isNotEmpty()) {
                                    productUrl = scrapeUrls[0]
                                }
                            }
                        }
                    }
                }
            }
        }
        return productUrl
    }
}

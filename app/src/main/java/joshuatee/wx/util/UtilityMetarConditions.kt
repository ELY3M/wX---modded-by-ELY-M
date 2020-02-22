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

package joshuatee.wx.util

object UtilityMetarConditions {

    // https://www.weather.gov/forecast-icons
    val iconFromCondition: Map<String, String> = mapOf(
            //"" to "skc",
            "Mostly Clear" to "few",

            "Fair" to "skc",
            "Clear" to "skc",
            "Fair with Haze" to "skc",
            "Clear with Haze" to "skc",
            "Fair and Breezy" to "skc",
            "Clear and Breezy" to "skc",

            "A Few Clouds" to "few",
            "A Few Clouds with Haze" to "few",
            "A Few Clouds and Breezy" to "few",

            "Partly Cloudy" to "sct",
            "Partly Cloudy with Haze" to "sct",
            "Partly Cloudy and Breezy" to "sct",

            "Mostly Cloudy" to "bkn",
            "Mostly Cloudy with Haze" to "bkn",
            "Mostly Cloudy and Breezy" to "bkn",
            "Cumulonimbus Clouds Observed" to "bkn",

            "Overcast" to "ovc",
            "Overcast with Haze" to "ovc",
            "Overcast and Breezy" to "ovc",

            "Snow" to "snow",
            "Light Snow" to "snow",
            "Light Snow With Thunder" to "snow", // new add 2019_02_05
            "Light Snow, Mist" to "snow",
            "Heavy Snow" to "snow",
            "Snow Showers" to "snow",
            "Light Snow Showers" to "snow",
            "Heavy Snow Showers" to "snow",
            "Showers Snow" to "snow",
            "Light Showers Snow" to "snow",
            "Heavy Showers Snow" to "snow",
            "Snow Fog/Mist" to "snow",
            "Light Drizzle, Snow And Mist" to "snow",
            "Light Snow Fog/Mist" to "snow",
            "Heavy Snow Fog/Mist" to "snow",
            "Snow Showers Fog/Mist" to "snow",
            "Light Snow Showers Fog/Mist" to "snow",
            "Heavy Snow Showers Fog/Mist" to "snow",
            "Showers Snow Fog/Mist" to "snow",
            "Light Showers Snow Fog/Mist" to "snow",
            "Heavy Showers Snow Fog/Mist" to "snow",
            "Snow Fog" to "snow",
            "Light Snow Fog" to "snow",
            "Heavy Snow Fog" to "snow",
            "Snow Showers Fog" to "snow",
            "Light Snow Showers Fog" to "snow",
            "Heavy Snow Showers Fog" to "snow",
            "Showers in Vicinity Snow" to "snow",
            "Snow Showers in Vicinity" to "snow",
            "Snow Showers in Vicinity Fog/Mist" to "snow",
            "Snow Showers in Vicinity Fog" to "snow",
            "Low Drifting Snow" to "snow",
            "Blowing Snow" to "snow",
            "Snow Low Drifting Snow" to "snow",
            "Snow Blowing Snow" to "snow",
            "Light Snow Low Drifting Snow" to "snow",
            "Light Snow Blowing Snow" to "snow",
            "Light Snow Blowing Snow Fog/Mist" to "snow",
            "Heavy Snow Low Drifting Snow" to "snow",
            "Heavy Snow Blowing Snow" to "snow",
            "Thunderstorm Snow" to "snow",
            "Light Thunderstorm Snow" to "snow",
            "Heavy Thunderstorm Snow" to "snow",
            "Snow Grains" to "snow",
            "Light Snow Grains" to "snow",
            "Heavy Snow Grains" to "snow",
            "Heavy Blowing Snow" to "snow",
            "Blowing Snow in Vicinity" to "snow",

            "Rain Snow" to "ra_sn",
            "Light Rain Snow" to "ra_sn",
            "Heavy Rain Snow" to "ra_sn",
            "Snow Rain" to "ra_sn",
            "Light Snow Rain" to "ra_sn",
            "Heavy Snow Rain" to "ra_sn",
            "Drizzle Snow" to "ra_sn",
            "Light Drizzle Snow" to "ra_sn",
            "Heavy Drizzle Snow" to "ra_sn",
            "Snow Drizzle" to "ra_sn",
            "Light Snow Drizzle" to "ra_sn",

            "Rain Ice Pellets" to "ra_ip",
            "Light Rain Ice Pellets" to "ra_ip",
            "Heavy Rain Ice Pellets" to "ra_ip",
            "Drizzle Ice Pellets" to "ra_ip",
            "Light Drizzle Ice Pellets" to "ra_ip",
            "Heavy Drizzle Ice Pellets" to "ra_ip",
            "Ice Pellets Rain" to "ra_ip",
            "Light Ice Pellets Rain" to "ra_ip",
            "Heavy Ice Pellets Rain" to "ra_ip",
            "Ice Pellets Drizzle" to "ra_ip",
            "Light Ice Pellets Drizzle" to "ra_ip",
            "Heavy Ice Pellets Drizzle" to "ra_ip",

            "Freezing Rain" to "fzra",
            "Freezing Drizzle" to "fzra",
            "Light Freezing Rain" to "fzra",
            "Light Freezing Drizzle" to "fzra",
            "Heavy Freezing Rain" to "fzra",
            "Heavy Freezing Drizzle" to "fzra",
            "Freezing Rain in Vicinity" to "fzra",
            "Freezing Drizzle in Vicinity" to "fzra",
            "Light Freezing Drizzle, Snow" to "fzra",  // NEW ADD 2019_01_25
            "Freezing Rain, Ice Pellets" to "fzra",  // NEW ADD 2019_02_05

            "Freezing Rain Rain" to "ra_fzra",
            "Light Freezing Rain Rain" to "ra_fzra",
            "Heavy Freezing Rain Rain" to "ra_fzra",
            "Rain Freezing Rain" to "ra_fzra",
            "Light Rain Freezing Rain" to "ra_fzra",
            "Heavy Rain Freezing Rain" to "ra_fzra",
            "Freezing Drizzle Rain" to "ra_fzra",
            "Light Freezing Drizzle Rain" to "ra_fzra",
            "Heavy Freezing Drizzle Rain" to "ra_fzra",
            "Rain Freezing Drizzle" to "ra_fzra",
            "Light Rain Freezing Drizzle" to "ra_fzra",
            "Heavy Rain Freezing Drizzle" to "ra_fzra",

            "Freezing Rain Snow" to "fzra_sn",
            "Light Freezing Rain Snow" to "fzra_sn",
            "Heavy Freezing Rain Snow" to "fzra_sn",
            "Freezing Drizzle Snow" to "fzra_sn",
            "Light Freezing Drizzle Snow" to "fzra_sn",
            "Heavy Freezing Drizzle Snow" to "fzra_sn",
            "Snow Freezing Rain" to "fzra_sn",
            "Light Snow Freezing Rain" to "fzra_sn",
            "Heavy Snow Freezing Rain" to "fzra_sn",
            "Snow Freezing Drizzle" to "fzra_sn",
            "Light Snow Freezing Drizzle" to "fzra_sn",
            "Heavy Snow Freezing Drizzle" to "fzra_sn",

            "Light Snow Pellets" to "ip", // new add 2019_02_05
            "Light Snow, Ice Pellets" to "ip", // new add 2019_02_05
            "Ice Pellets" to "ip",
            "Light Ice Pellets" to "ip",
            "Heavy Ice Pellets" to "ip",
            "Ice Pellets in Vicinity" to "ip",
            "Showers Ice Pellets" to "ip",
            "Thunderstorm Ice Pellets" to "ip",
            "Ice Crystals" to "ip",
            "Hail" to "ip",
            "Small Hail/Snow Pellets" to "ip",
            "Light Small Hail/Snow Pellets" to "ip",
            "Heavy small Hail/Snow Pellets" to "ip",
            "Showers Hail" to "ip",
            "Hail Showers" to "ip",

            "Snow Ice Pellets" to "snip",

            "Light Rain" to "minus_ra",
            "Drizzle" to "minus_ra",
            "Light Drizzle" to "minus_ra",
            "Heavy Drizzle" to "minus_ra",
            "Light Rain Fog/Mist" to "minus_ra",
            "Light Rain Mist" to "minus_ra",
            "Drizzle Fog/Mist" to "minus_ra",
            "Light Drizzle Fog/Mist" to "minus_ra",
            "Heavy Drizzle Fog/Mist" to "minus_ra",
            "Light Rain Fog" to "minus_ra",
            "Drizzle Fog" to "minus_ra",
            "Light Drizzle Fog" to "minus_ra",
            "Heavy Drizzle Fog" to "minus_ra",

            "Rain" to "ra",
            "Heavy Rain" to "ra",
            "Rain Fog/Mist" to "ra",
            "Heavy Rain Fog/Mist" to "ra",
            "Rain Fog" to "ra",
            "Heavy Rain Fog" to "ra",
            "Precipitation" to "ra",

            "Rain Showers" to "shra",
            "Light Rain Showers" to "shra",
            "Light Rain and Breezy" to "shra",
            "Heavy Rain Showers" to "shra",
            "Rain Showers in Vicinity" to "shra",
            "Light Showers Rain" to "shra",
            "Heavy Showers Rain" to "shra",
            "Showers Rain" to "shra",
            "Showers Rain in Vicinity" to "shra",
            "Rain Showers Fog/Mist" to "shra",
            "Light Rain Showers Fog/Mist" to "shra",
            "Heavy Rain Showers Fog/Mist" to "shra",
            "Rain Showers in Vicinity Fog/Mist" to "shra",
            "Light Showers Rain Fog/Mist" to "shra",
            "Heavy Showers Rain Fog/Mist" to "shra",
            "Showers Rain Fog/Mist" to "shra",
            "Showers Rain in Vicinity Fog/Mist" to "shra",

            "Showers in Vicinity" to "hi_shwrs",
            "Showers in Vicinity Fog/Mist" to "hi_shwrs",
            "Showers in Vicinity Fog" to "hi_shwrs",
            "Showers in Vicinity Haze" to "hi_shwrs",

            "Thunderstorm" to "tsra",
            "Thunderstorm Rain" to "tsra",
            "Light Thunderstorm Rain" to "tsra",
            "Heavy Thunderstorm Rain" to "tsra",
            "Thunderstorm Rain Fog/Mist" to "tsra",
            "Light Thunderstorm Rain Fog/Mist" to "tsra",
            "Heavy Thunderstorm Rain Fog and Windy" to "tsra",
            "Heavy Thunderstorm Rain Fog/Mist" to "tsra",
            "Thunderstorm Showers in Vicinity" to "tsra",
            "Light Thunderstorm Rain Haze" to "tsra",
            "Heavy Thunderstorm Rain Haze" to "tsra",
            "Thunderstorm Fog" to "tsra",
            "Light Thunderstorm Rain Fog" to "tsra",
            "Heavy Thunderstorm Rain Fog" to "tsra",
            "Thunderstorm Light Rain" to "tsra",
            "Thunderstorm Heavy Rain" to "tsra",
            "Thunderstorm Light Rain Fog/Mist" to "tsra",
            "Thunderstorm Heavy Rain Fog/Mist" to "tsra",
            "Thunderstorm in Vicinity Fog/Mist" to "tsra",
            "Thunderstorm in Vicinity Haze" to "tsra",
            "Thunderstorm Haze in Vicinity" to "tsra",
            "Thunderstorm Light Rain Haze" to "tsra",
            "Thunderstorm Heavy Rain Haze" to "tsra",
            "Thunderstorm Light Rain Fog" to "tsra",
            "Thunderstorm Heavy Rain Fog" to "tsra",
            "Thunderstorm Hail" to "tsra",
            "Light Thunderstorm Rain Hail" to "tsra",
            "Heavy Thunderstorm Rain Hail" to "tsra",
            "Thunderstorm Rain Hail Fog/Mist" to "tsra",
            "Light Thunderstorm Rain Hail Fog/Mist" to "tsra",
            "Heavy Thunderstorm Rain Hail Fog/Hail" to "tsra",
            "Thunderstorm Showers in Vicinity Hail" to "tsra",
            "Light Thunderstorm Rain Hail Haze" to "tsra",
            "Heavy Thunderstorm Rain Hail Haze" to "tsra",
            "Thunderstorm Hail Fog" to "tsra",
            "Light Thunderstorm Rain Hail Fog" to "tsra",
            "Heavy Thunderstorm Rain Hail Fog" to "tsra",
            "Thunderstorm Light Rain Hail" to "tsra",
            "Thunderstorm Heavy Rain Hail" to "tsra",
            "Thunderstorm Light Rain Hail Fog/Mist" to "tsra",
            "Thunderstorm Heavy Rain Hail Fog/Mist" to "tsra",
            "Thunderstorm in Vicinity Hail" to "tsra",
            "Thunderstorm in Vicinity Hail Haze" to "tsra",
            "Thunderstorm Haze in Vicinity Hail" to "tsra",
            "Thunderstorm Light Rain Hail Haze" to "tsra",
            "Thunderstorm Heavy Rain Hail Haze" to "tsra",
            "Thunderstorm Light Rain Hail Fog" to "tsra",
            "Thunderstorm Heavy Rain Hail Fog" to "tsra",
            "Thunderstorm Small Hail/Snow Pellets" to "tsra",
            "Thunderstorm Rain Small Hail/Snow Pellets" to "tsra",
            "Light Thunderstorm Rain Small Hail/Snow Pellets" to "tsra",
            "Heavy Thunderstorm Rain Small Hail/Snow Pellets" to "tsra",
            "Thunder" to "tsra",
            "Rain With Thunder" to "tsra",
            "Thunder In The Vicinity And Mist" to "tsra",

            "Thunder In The Vicinity And Heavy Rain and Mist" to "tsra",


            "Freezing With Thunder Rain And Mist" to "tsra",
            "Heavy Rain With Thunder" to "tsra",
            "Heavy Rain With Thunder In The Vicinity" to "tsra",
            "Light Rain With Thunder" to "tsra",


            //"Thunderstorm in Vicinity" to "scttsra",

            "Thunderstorm in Vicinity" to "hi_tsra",
            "Thunder In The Vicinity" to "hi_tsra",
            "Thunderstorm in Vicinity Fog" to "hi_tsra",
            "Lightning Observed" to "hi_tsra",
            "Cumulonimbus Clouds, Lightning Observed" to "hi_tsra",
            "Cumulonimbus Clouds, Towering Cumulus Clouds Observed" to "hi_tsra",
            // FIXME
            //"Thunderstorm in Vicinity Haze" to "hi_tsra",
            "Cumulonimbus Clouds Observed" to "tsra_hi",
            "Towering Cumulus Clouds Observed" to "tsra_hi",

            "Funnel Cloud" to "fc",
            "Funnel Cloud in Vicinity" to "fc",
            "Tornado/Water Spout" to "fc",

            "Tornado" to "tor",

            "Hurricane Warning" to "hur_warn",

            "Hurricane Watch" to "hur_watch",

            "Tropical Storm Warning" to "ts_warn",

            "Tropical Storm Watch" to "ts_watch",

            "Tropical Storm Conditions presently exist w/Hurricane Warning in effect" to "ts_nowarn",

            "Windy" to "wind_skc",
            "Breezy" to "wind_skc",
            "Fair and Windy" to "wind_skc",

            "A Few Clouds and Windy" to "wind_few",

            "Partly Cloudy and Windy" to "wind_sct",

            "Mostly Cloudy and Windy" to "wind_bkn",

            "Overcast and Windy" to "wind_ovc",

            "Dust" to "du",
            "Low Drifting Dust" to "du",
            "Blowing Dust" to "du",
            "Blowing Widespread Dust" to "du",
            "Sand" to "du",
            "Blowing Sand" to "du",
            "Low Drifting Sand" to "du",
            "Dust/Sand Whirls" to "du",
            "Dust/Sand Whirls in Vicinity" to "du",
            "Dust Storm" to "du",
            "Heavy Dust Storm" to "du",
            "Dust Storm in Vicinity" to "du",
            "Sand Storm" to "du",
            "Heavy Sand Storm" to "du",
            "Sand Storm in Vicinity" to "du",

            "Smoke" to "fu",

            // no night
            "Haze" to "hz",
            "Hot" to "hot",

            "Cold" to "cold",

            "Blizzard" to "blizzard",

            "Fog" to "fg",
            "Fog/Mist" to "fg",
            "Freezing Fog" to "fg",
            "Shallow Fog" to "fg",
            "Partial Fog" to "fg",
            "Patches of Fog" to "fg",
            "Fog in Vicinity" to "fg",
            "Freezing Fog in Vicinity" to "fg",
            "Shallow Fog in Vicinity" to "fg",
            "Partial Fog in Vicinity" to "fg",
            "Patches of Fog in Vicinity" to "fg",
            //"Showers in Vicinity Fog" to "fg",
            "Light Freezing Fog" to "fg",
            "Heavy Freezing Fog" to "fg",
            "Precipitation and Mist" to "fg"
    )
}




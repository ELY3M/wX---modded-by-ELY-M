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

import android.content.Context
import android.graphics.Bitmap
import joshuatee.wx.ui.ForecastIcon

object UtilityForecastIcon {

    // Given the raw icon URL from NWS, determine if bitmap is on disk or must be created
    // input examples
    //  https://api.weather.gov/icons/land/day/rain_showers,60/rain_showers,30?size=medium
    //  https://api.weather.gov/icons/land/night/bkn?size=medium
    //  https://api.weather.gov/icons/land/day/tsra_hi,40?size=medium
    fun getIcon(context: Context, url: String): Bitmap = if (url == "NULL" || url == "") {
        ForecastIcon.blankBitmap()
    } else {
        val fileName = getFilename(url)
        val fileId = UtilityNwsIcon.iconMap["$fileName.png"]
        if (fileId == null || fileName.contains(",")) {
            parseBitmapString(context, fileName)
        } else {
            UtilityImg.loadBitmap(context, fileId, false)
        }
    }

    // Given one string that does not have a match on disk, decode and return a bitmap with textual labels
    // it could be composed of 2 bitmaps with one or more textual labels (if string has a "/" ) or just one bitmap with label
    // input examples
    //  rain_showers,70/tsra,80
    //  ntsra,80
    //  legacy: i=nsn;j=nsn;ip=60;jp=30
    //  legacy add - 2nd condition
    private fun parseBitmapString(context: Context, url: String): Bitmap = if (url.contains("/") || url.contains(";j=") || (url.contains("i=") && url.contains("j="))) {
        val conditions = url.split("/").dropLastWhile { it.isEmpty() } //  snow,20/ovc,20
        if (conditions.size > 1) {
            getDualBitmapWithNumbers(context, conditions[0], conditions[1])
        } else {
            // legacy add
            val urlTmp = url.replace("i=", "")
                    .replace("j=", "")
                    .replace("ip=", "")
                    .replace("jp=", "")
            val items = urlTmp.split(";")
            if (items.size > 3) {
                getDualBitmapWithNumbers(context, items[0] + items[2], items[1] + items[3])
            } else if (items.size > 2) {
                if (url.contains(";jp=")) {
                    getDualBitmapWithNumbers(context, items[0], items[1] + items[2])
                } else {
                    getDualBitmapWithNumbers(context, items[0] + items[2], items[1])
                }
            } else {
                getDualBitmapWithNumbers(context, items[0], items[1])
            }
            // legacy add end
        }
    } else {
        getBitmapWithOneNumber(context, url)
    }

    // Given two strings return a custom bitmap made of two bitmaps with optional numeric label
    // input examples
    //  rain_showers,60 rain_showers,30
    //  nrain_showers,80 nrain_showers,70
    //  ntsra_hi,40 ntsra_hi
    //  bkn rain
    private fun getDualBitmapWithNumbers(context: Context, iconLeftString: String, iconRightString: String): Bitmap {
        val leftTokens = iconLeftString.split(",").dropLastWhile { it.isEmpty() }
        val rightTokens = iconRightString.split(",").dropLastWhile { it.isEmpty() }
        var leftNumber = if (leftTokens.size > 1) {
            leftTokens[1]
        } else {
            ""
        }
        var rightNumber = if (rightTokens.size > 1) {
            rightTokens[1]
        } else {
            ""
        }
        var leftWeatherCondition: String
        var rightWeatherCondition: String
        if (leftTokens.isNotEmpty() && rightTokens.isNotEmpty()) {
            leftWeatherCondition = leftTokens[0]
            rightWeatherCondition = rightTokens[0]
        } else {
            leftWeatherCondition = ""
            rightWeatherCondition = ""
        }
        // legacy add
        if (!iconLeftString.contains(",") && !iconRightString.contains(",")) {
            leftNumber = UtilityString.parse(iconLeftString, ".*?([0-9]+)")
            leftWeatherCondition = UtilityString.parse(iconLeftString, "([a-z_]+)")
            rightNumber = UtilityString.parse(iconRightString, ".*?([0-9]+)")
            rightWeatherCondition = UtilityString.parse(iconRightString, "([a-z_]+)")
        }
        // legacy add end
        val forecastIcon = ForecastIcon(context, leftWeatherCondition, rightWeatherCondition)
        forecastIcon.drawLeftText(leftNumber)
        forecastIcon.drawRightText(rightNumber)
        return forecastIcon.get()
    }

    // Given one string return a custom bitmap with numeric label
    // input examples
    // nrain_showers,80
    // tsra_hi,40
    private fun getBitmapWithOneNumber(context: Context, iconString: String): Bitmap {
        val items = iconString.split(",").dropLastWhile { it.isEmpty() }
        var number = if (items.size > 1) {
            items[1]
        } else {
            ""
        }
        var weatherCondition = if (items.isNotEmpty()) {
            items[0]
        } else {
            ""
        }
        // legacy add
        if (!iconString.contains(",")) {
            number = UtilityString.parse(iconString, ".*?([0-9]+)")
            weatherCondition = UtilityString.parse(iconString, "([a-z_]+)")
        }
        // legacy add end
        val forecastIcon = ForecastIcon(context, weatherCondition)
        forecastIcon.drawSingleText(number)
        return forecastIcon.get()
    }

    private fun getFilename(url: String): String {
        var fileName = url.replace("?size=medium", "")
                .replace("?size=small", "")
                .replace("https://api.weather.gov/icons/land/", "")
                .replace("http://api.weather.gov/icons/land/", "")
                .replace("http://nids-wapiapp.bldr.ncep.noaa.gov:9000/icons/land/", "")
                .replace("day/", "")
                // legacy add
                .replace("http://forecast.weather.gov/newimages/medium/", "")
                .replace("https://forecast.weather.gov/newimages/medium/", "")
                .replace(".png", "")
                .replace("http://forecast.weather.gov/DualImage.php?", "")
                .replace("https://forecast.weather.gov/DualImage.php?", "")
                .replace("&amp", "")
        // legacy add end
        if (fileName.contains("night")) {
            fileName = fileName.replace("night/", "n").replace("/", "/n")
        }
        return fileName
    }
}

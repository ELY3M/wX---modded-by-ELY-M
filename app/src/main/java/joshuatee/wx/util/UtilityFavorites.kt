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

import android.content.Context
import android.view.MenuItem

import joshuatee.wx.MyApplication

import joshuatee.wx.GlobalArrays
import joshuatee.wx.wpc.UtilityWpcText

object UtilityFavorites {

    private const val DELIM_TOKEN = " "
    private const val ADD_STR = "Add..."
    private const val MODIFY_STR = "Modify..."
    const val initialValue = " : : :"

    // TODO refactor method/var names

    private fun checkAndCorrect(context: Context, value: String, prefToken: String) {
        if (value.contains("::")) {
            val newFav = value.replace(":{2,}".toRegex(), ":")
            savePref(context, newFav, prefToken)
        }
        if (!value.contains(MyApplication.prefSeparator)) {
            val newFav = MyApplication.prefSeparator + value.trimStart()
            savePref(context, newFav, prefToken)
        }
    }

    private fun savePref(context: Context, value: String, prefToken: String) {
        Utility.writePref(context, prefToken, value)
        when (prefToken) {
            "WFO_FAV" -> MyApplication.wfoFav = value
            "RID_FAV" -> MyApplication.ridFav = value
            "SND_FAV" -> MyApplication.sndFav = value
        }
    }

    fun setupMenu(
        context: Context,
        favoriteString: String,
        value: String,
        prefToken: String
    ): List<String> {
        checkAndCorrect(context, favoriteString, prefToken)
        var favorites = favoriteString.split(":").dropLastWhile { it.isEmpty() }.toMutableList()
        if (favorites.size < 3) {
            favorites = MutableList(3) { "" }
        }
        favorites[0] = value
        favorites[1] = ADD_STR
        favorites[2] = MODIFY_STR
        val returnList = MutableList(favorites.size) { "" }
        var name: String
        favorites.indices.forEach { k ->
            name = when (prefToken) {
                "RID_FAV" -> Utility.getRadarSiteName(favorites[k])
                "WFO_FAV" -> Utility.getWfoSiteName(favorites[k])
                "SND_FAV" -> Utility.getSoundingSiteName(favorites[k])
                else -> "FIXME"
            }
            if (k == 1 || k == 2) {
                returnList[k] = favorites[k]
            } else {
                returnList[k] = favorites[k] + DELIM_TOKEN + name
            }
        }
        return returnList.toList()
    }

    fun setupMenuCanada(favoriteString: String, value: String): List<String> {
        val favorites = favoriteString.split(":").dropLastWhile { it.isEmpty() }.toMutableList()
        favorites[0] = value
        favorites[1] = ADD_STR
        favorites[2] = MODIFY_STR
        val returnList = MutableList(favorites.size) { "" }
        favorites.indices.forEach { k ->
            GlobalArrays.canadaRadars.indices.filter { GlobalArrays.canadaRadars[it].contains(favorites[k]) }
                .forEach { returnList[k] = GlobalArrays.canadaRadars[it].replace(":", "") }
            if (k == 1 || k == 2) {
                returnList[k] = favorites[k]
            }
        }
        return returnList.toList()
    }

    fun toggle(context: Context, value: String, star: MenuItem, prefToken: String) {
        var favoriteString = Utility.readPref(context, prefToken, initialValue)
        if (favoriteString.contains(value)) {
            favoriteString = favoriteString.replace("$value:", "")
            star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        } else {
            favoriteString = "$favoriteString$value:"
            star.setIcon(MyApplication.STAR_ICON)
        }
        Utility.writePref(context, prefToken, favoriteString)
        when (prefToken) {
            "RID_FAV" -> MyApplication.ridFav = favoriteString
            "WFO_FAV" -> MyApplication.wfoFav = favoriteString
            "SND_FAV" -> MyApplication.sndFav = favoriteString
            "SREF_FAV" -> MyApplication.srefFav = favoriteString
            "NWS_TEXT_FAV" -> MyApplication.nwsTextFav = favoriteString
        }
    }

    // mirror of method above save it returns the string
    fun toggleString(
        context: Context,
        value: String,
        star: MenuItem,
        prefToken: String
    ): String {
        var favoriteString = Utility.readPref(context, prefToken, initialValue)
        if (favoriteString.contains(value)) {
            favoriteString = favoriteString.replace("$value:", "")
            star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        } else {
            favoriteString = "$favoriteString$value:"
            star.setIcon(MyApplication.STAR_ICON)
        }
        Utility.writePref(context, prefToken, favoriteString)
        when (prefToken) {
            "RID_FAV" -> MyApplication.ridFav = favoriteString
            "WFO_FAV" -> MyApplication.wfoFav = favoriteString
            "SND_FAV" -> MyApplication.sndFav = favoriteString
            "SREF_FAV" -> MyApplication.srefFav = favoriteString
            "NWS_TEXT_FAV" -> MyApplication.nwsTextFav = favoriteString
        }
        return favoriteString
    }

    fun toggleSpcMeso(context: Context, value: String, label: String, star: MenuItem) {
        var favoriteString = Utility.readPref(context, "SPCMESO_FAV", initialValue)
        var favoriteLabelString = Utility.readPref(context, "SPCMESO_LABEL_FAV", initialValue)
        if (favoriteString.contains(value)) {
            favoriteString = favoriteString.replace("$value:", "")
            favoriteLabelString = favoriteLabelString.replace("$label:", "")
            star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        } else {
            favoriteString = "$favoriteString$value:"
            favoriteLabelString = "$favoriteLabelString$label:"
            star.setIcon(MyApplication.STAR_ICON)
        }
        Utility.writePref(context, "SPCMESO_FAV", favoriteString)
        Utility.writePref(context, "SPCMESO_LABEL_FAV", favoriteLabelString)
        MyApplication.spcMesoFav = favoriteString
        MyApplication.spcmesoLabelFav = favoriteLabelString
    }

    // Takes a value and a colon separated string
    // returns a List with the value at the start followed by two constant values (add/modify)
    // followed by each token in the string as list items
    // If somehow the input colon separated string is to small correct it in this method
    fun setupMenuSpc(favoriteString: String, value: String): List<String> {
        var favorites = favoriteString.split(":").dropLastWhile { it.isEmpty() }.toMutableList()
        if (favorites.size < 3) {
            favorites = MutableList(3) { "" }
        }
        favorites[0] = value
        favorites[1] = ADD_STR
        favorites[2] = MODIFY_STR
        return favorites.toList()
    }

    fun setupMenuNwsText(favoriteString: String, value: String): List<String> {
        val favorites = favoriteString.split(":").dropLastWhile { it.isEmpty() }.toMutableList()
        favorites[0] = value
        favorites[1] = ADD_STR
        favorites[2] = MODIFY_STR
        val returnList = MutableList(favorites.size) { "" }
        favorites.indices.forEach {
            if (it == 1 || it == 2) {
                returnList[it] = favorites[it]
            } else {
                val index = findPositionNwsText(favorites[it])
                if (index == -1) {
                    returnList[it] = value
                } else {
                    returnList[it] = UtilityWpcText.labels[findPositionNwsText(favorites[it])]
                }
            }
        }
        return returnList.toList()
    }

    fun findPositionNwsText(key: String): Int {
        val index = UtilityWpcText.labels.indices.firstOrNull {
            UtilityWpcText.labels[it].startsWith(key)
        } ?: -1
        return index
    }
}

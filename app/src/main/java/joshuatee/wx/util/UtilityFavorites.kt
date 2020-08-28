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

import joshuatee.wx.canada.UtilityCanada
import joshuatee.wx.spc.UtilitySpcMeso
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
            "SREF_FAV" -> MyApplication.srefFav = value
            "NWS_TEXT_FAV" -> MyApplication.nwsTextFav = value
            "SPCMESO_FAV" ->  MyApplication.spcMesoFav = value
            "RID_CA_FAV" -> MyApplication.caRidFav = value
        }
    }

    fun setupMenu(context: Context, favoriteString: String, value: String, prefToken: String): List<String> {
        checkAndCorrect(context, favoriteString, prefToken)
        var favorites = favoriteString.split(":").dropLastWhile { it.isEmpty() }.toMutableList()
        if (favorites.size < 3) favorites = MutableList(3) { "" }
        favorites[0] = value
        favorites[1] = ADD_STR
        favorites[2] = MODIFY_STR
        val returnList = MutableList(favorites.size) { "" }
        favorites.indices.forEach { k ->
            UtilityLog.d("wx", "DEBUG: " + favorites[k])
            val name = when (prefToken) {
                "RID_FAV" -> Utility.getRadarSiteName(favorites[k])
                "WFO_FAV" -> Utility.getWfoSiteName(favorites[k])
                "SND_FAV" -> Utility.getSoundingSiteName(favorites[k])
                "NWS_TEXT_FAV" -> UtilityWpcText.getLabel(favorites[k])
                "SPCMESO_FAV" -> UtilitySpcMeso.getLabelFromParam(favorites[k])
                "RID_CA_FAV" -> UtilityCanada.getRadarLabel(favorites[k])
                "SPCSREF_FAV" -> ""
                else -> "FIXME"
            }
            if (k == 1 || k == 2) returnList[k] = favorites[k] else returnList[k] = favorites[k] + DELIM_TOKEN + name
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
            "SPCMESO_FAV" ->  MyApplication.spcMesoFav = favoriteString
            "RID_CA_FAV" -> MyApplication.caRidFav = favoriteString
        }
    }
}

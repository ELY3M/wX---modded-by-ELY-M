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
import android.view.MenuItem
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.FavoriteType
import joshuatee.wx.radar.RadarSites
import joshuatee.wx.spc.UtilitySpcMeso
import joshuatee.wx.wpc.UtilityWpcText

object UtilityFavorites {

    const val INITIAL_VALUE = " : : :"

    private fun checkAndCorrect(context: Context, type: FavoriteType) {
        if (UIPreferences.favorites[type]!!.contains("::")) {
            val newFav = UIPreferences.favorites[type]!!.replace(":{2,}".toRegex(), ":")
            savePref(context, newFav, type)
        }
        if (!UIPreferences.favorites[type]!!.contains(GlobalVariables.PREFERENCE_SEPARATOR)) {
            val newFav =
                GlobalVariables.PREFERENCE_SEPARATOR + UIPreferences.favorites[type]!!.trimStart()
            savePref(context, newFav, type)
        }
    }

    private fun savePref(context: Context, value: String, type: FavoriteType) {
        Utility.writePref(context, getPrefToken(type), value)
        UIPreferences.favorites[type] = value
    }

    fun setupMenu(context: Context, value: String, type: FavoriteType): List<String> {
        checkAndCorrect(context, type)
        var favorites = UIPreferences.favorites[type]!!.split(":").dropLastWhile { it.isEmpty() }
            .toMutableList()
        if (favorites.size < 3) {
            favorites = MutableList(3) { "" }
        }
        favorites[0] = value
        favorites[1] = "Add..."
        favorites[2] = "Modify..."
        val returnList = MutableList(favorites.size) { "" }
        favorites.forEachIndexed { k, fav ->
            UtilityLog.d("wxFAV", fav)
            val name = when (type) {
                FavoriteType.RID -> RadarSites.getName(fav)
                FavoriteType.WFO -> WfoSites.getFullName(fav)
                FavoriteType.SND -> SoundingSites.sites.byCode[fav]?.fullName
                FavoriteType.NWS_TEXT -> UtilityWpcText.getLabel(fav)
                FavoriteType.SPCMESO -> UtilitySpcMeso.getLabelFromParam(fav)
                FavoriteType.SREF -> ""
            }
            if (k == 1 || k == 2) {
                returnList[k] = fav
            } else {
                returnList[k] = "$fav $name"
            }
        }
        return returnList
    }

    fun toggle(context: Context, value: String, star: MenuItem, type: FavoriteType) {
        var favoriteString = Utility.readPref(context, getPrefToken(type), INITIAL_VALUE)
        if (favoriteString.contains(value)) {
            favoriteString = favoriteString.replace("$value:", "")
            star.setIcon(GlobalVariables.STAR_OUTLINE_ICON)
        } else {
            favoriteString = "$favoriteString$value:"
            star.setIcon(GlobalVariables.STAR_ICON)
        }
        savePref(context, favoriteString, type)
    }

    fun getPrefToken(type: FavoriteType): String = type.name + "_FAV"
}

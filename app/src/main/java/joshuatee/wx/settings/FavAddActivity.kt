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

package joshuatee.wx.settings

import android.os.Bundle
import joshuatee.wx.R
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.models.UtilityModelSpcSrefInterface
import joshuatee.wx.objects.FavoriteType
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.spc.UtilitySpcMeso
import joshuatee.wx.ui.ObjectRecyclerView
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityFavorites
import joshuatee.wx.wpc.UtilityWpcText

class FavAddActivity : BaseActivity() {

    //
    // called from various activities that need favorite management,
    // allows one to add from list of favorite sites
    //
    // arg1: type such as "SND", "WFO", "SREF", "SPCMESO", "NWSTEXT", and "RID"
    //

    companion object { const val TYPE = "" }

    private var data = listOf<String>()
    private var dataTokens = listOf<String>()
    private lateinit var type: FavoriteType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_toolbar, null, false)
        type = FavoriteType.stringToType(intent.getStringArrayExtra(TYPE)!![0])
        var verboseTitle = ""
        when (type) {
            FavoriteType.SND -> {
                data = GlobalArrays.soundingSites.map { "$it " + Utility.getSoundingSiteName(it) }
                verboseTitle = "sounding site"
            }
            FavoriteType.WFO -> {
                data = GlobalArrays.wfos
                verboseTitle = "NWS office"
            }
            FavoriteType.RID -> {
                data = GlobalArrays.radars + GlobalArrays.tdwrRadars
                verboseTitle = "radar site"
            }
            FavoriteType.NWS_TEXT -> {
                data = UtilityWpcText.labels
                verboseTitle = "text product"
            }
            FavoriteType.SREF -> {
                data = UtilityModelSpcSrefInterface.params
                verboseTitle = "parameter"
            }
            FavoriteType.SPCMESO -> {
                data = UtilitySpcMeso.labels
                dataTokens = UtilitySpcMeso.params
                verboseTitle = "parameter"
            }
        }
        setTitle("Add Favorite", verboseTitle)
        ObjectRecyclerView(this, R.id.card_list, data.toMutableList(), ::itemClicked)
    }

    private fun itemClicked(position: Int) {
        val item = data[position]
        var favoriteString = Utility.readPref(this, UtilityFavorites.getPrefToken(type), UtilityFavorites.initialValue)
        val tmpArr = when (type) {
            FavoriteType.SPCMESO -> {
                if (dataTokens[position].contains(":")) {
                    dataTokens[position].split(":").dropLastWhile { it.isEmpty() }
                } else {
                    dataTokens[position].split(" ").dropLastWhile { it.isEmpty() }
                }
            }
            FavoriteType.SND -> {
                if (GlobalArrays.soundingSites[position].contains(":")) {
                    GlobalArrays.soundingSites[position].split(":").dropLastWhile { it.isEmpty() }
                } else {
                    GlobalArrays.soundingSites[position].split(" ").dropLastWhile { it.isEmpty() }
                }
            }
            else -> {
                if (data[position].contains(":")) {
                    data[position].split(":").dropLastWhile { it.isEmpty() }
                } else {
                    data[position].split(" ").dropLastWhile { it.isEmpty() }
                }
            }
        }
        if (!favoriteString.contains(tmpArr[0])) {
            favoriteString += when (type) {
                FavoriteType.SPCMESO -> UtilitySpcMeso.params[position] + ":"
                else -> tmpArr[0] + ":"
            }
            Utility.writePref(this, UtilityFavorites.getPrefToken(type), favoriteString)
            UIPreferences.favorites[type] = favoriteString
            toolbar.subtitle = "Last added: $item"
        } else {
            toolbar.subtitle = "Already added: $item"
        }
    }
}

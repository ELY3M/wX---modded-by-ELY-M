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

package joshuatee.wx.settings

import android.annotation.SuppressLint
import android.os.Bundle

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.GlobalArrays

import joshuatee.wx.models.UtilityModelSpcSrefInterface
import joshuatee.wx.ui.BaseActivity

import joshuatee.wx.spc.UtilitySpcMeso
import joshuatee.wx.ui.ObjectRecyclerView
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityFavorites
import joshuatee.wx.wpc.UtilityWpcText

class FavAddActivity : BaseActivity() {

    companion object { const val TYPE = "" }

    private var prefToken = ""
    private var data = listOf<String>()
    private var dataTokens = listOf<String>()
    private var type = ""

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_toolbar, null, false)
        type = intent.getStringArrayExtra(TYPE)!![0]
        var verboseTitle = ""
        when (type) {
            "SND" -> {
                prefToken = "SND_FAV"
                val list = mutableListOf<String>()
                GlobalArrays.soundingSites.forEach { list.add("$it " + Utility.getSoundingSiteName(it)) }
                data = list.toList()
                verboseTitle = "sounding site"
            }
            "RIDCA" -> {
                prefToken = "RID_CA_FAV"
                data = GlobalArrays.canadaRadars
                verboseTitle = "radar site"
            }
            "WFO" -> {
                prefToken = "WFO_FAV"
                data = GlobalArrays.wfos
                verboseTitle = "NWS office"
            }
            "RID" -> {
                prefToken = "RID_FAV"
                data = GlobalArrays.radars + GlobalArrays.tdwrRadars
                verboseTitle = "radar site"
            }
            "NWSTEXT" -> {
                prefToken = "NWS_TEXT_FAV"
                data = UtilityWpcText.labels
                verboseTitle = "text product"
            }
            "SREF" -> {
                prefToken = "SREF_FAV"
                data = UtilityModelSpcSrefInterface.params
                verboseTitle = "parameter"
            }
            "SPCMESO" -> {
                prefToken = "SPCMESO_FAV"
                data = UtilitySpcMeso.labels
                dataTokens = UtilitySpcMeso.params
                verboseTitle = "parameter"
            }
        }
        title = "Add $verboseTitle"
        ObjectRecyclerView(this, this, R.id.card_list, data.toMutableList(), ::itemClicked)
    }

    private fun itemClicked(position: Int) {
        val item = data[position]
        var favoriteString = Utility.readPref(this, prefToken, UtilityFavorites.initialValue)
        val tmpArr = when (type) {
            "SPCMESO" -> {
                if (dataTokens[position].contains(":")) {
                    dataTokens[position].split(":").dropLastWhile { it.isEmpty() }
                } else {
                    dataTokens[position].split(" ").dropLastWhile { it.isEmpty() }
                }
            }
            "SND" -> {
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
                "SPCMESO" -> UtilitySpcMeso.params[position] + ":"
                else -> tmpArr[0] + ":"
            }
            Utility.writePref(this, prefToken, favoriteString)
            saveMyApp(favoriteString)
            toolbar.subtitle = "Last added: $item"
        } else {
            toolbar.subtitle = "Already added: $item"
        }
    }

    private fun saveMyApp(favorite: String) {
        when (type) {
            "SND" -> MyApplication.sndFav = favorite
            "WFO" -> MyApplication.wfoFav = favorite
            "RID" -> MyApplication.ridFav = favorite
            "NWSTEXT" -> MyApplication.nwsTextFav = favorite
            "SREF" -> MyApplication.srefFav = favorite
            "SPCMESO" -> MyApplication.spcMesoFav = favorite
            "RIDCA" -> MyApplication.caRidFav = favorite
        }
    }
}

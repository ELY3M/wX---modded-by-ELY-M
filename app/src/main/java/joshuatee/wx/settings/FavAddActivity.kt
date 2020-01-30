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
import joshuatee.wx.wpc.UtilityWpcText

class FavAddActivity : BaseActivity() {

    companion object {
        const val TYPE: String = ""
    }

    private var prefToken = ""
    private var prefTokenLabel = ""
    private var data = listOf<String>()
    private var dataTokens = listOf<String>()
    private var type = ""

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_toolbar, null, false)
        type = intent.getStringArrayExtra(TYPE)!![0]
        title = "Add $type"
        when (type) {
            "SND" -> {
                prefToken = "SND_FAV"
                val tmpArr = mutableListOf<String>()
                GlobalArrays.soundingSites.indices.forEach {
                    var tmpLoc = Utility.readPref(this, "NWS_LOCATION_" + GlobalArrays.soundingSites[it], "")
                    if (tmpLoc == "") {
                        tmpLoc = Utility.readPref(
                                this,
                                "NWS_SOUNDINGLOCATION_" + GlobalArrays.soundingSites[it],
                                ""
                        )
                    }
                    tmpArr.add(GlobalArrays.soundingSites[it] + " " + tmpLoc)
                }
                data = tmpArr.toList()
            }
            "RIDCA" -> {
                prefToken = "RID_CA_FAV"
                data = GlobalArrays.canadaRadars
            }
            "WFO" -> {
                prefToken = "WFO_FAV"
                data = GlobalArrays.wfos
            }
            "RID" -> {
                prefToken = "RID_FAV"
                data = GlobalArrays.radars + GlobalArrays.tdwrRadars
            }
            "NWSTEXT" -> {
                prefToken = "NWS_TEXT_FAV"
                data = UtilityWpcText.labels
            }
            "SREF" -> {
                prefToken = "SREF_FAV"
                data = UtilityModelSpcSrefInterface.params
            }
            "SPCMESO" -> {
                prefToken = "SPCMESO_FAV"
                prefTokenLabel = "SPCMESO_LABEL_FAV"
                data = UtilitySpcMeso.labels
                dataTokens = UtilitySpcMeso.params
            }
        }
        ObjectRecyclerView(this, this, R.id.card_list, data.toMutableList(), ::itemClicked)
    }

    private fun itemClicked(position: Int) {
        val item = data[position]
        var ridFav = Utility.readPref(this, prefToken, " : : :")
        var ridFavLabel = ""
        val tmpArr: Array<String>
        when (type) {
            "SPCMESO" -> {
                ridFavLabel = Utility.readPref(this, prefTokenLabel, " : : :")
                tmpArr = if (dataTokens[position].contains(":")) {
                    MyApplication.colon.split(dataTokens[position])
                } else {
                    MyApplication.space.split(dataTokens[position])
                }
            }
            "SND" -> {
                tmpArr = if (GlobalArrays.soundingSites[position].contains(":")) {
                    MyApplication.colon.split(GlobalArrays.soundingSites[position])
                } else {
                    MyApplication.space.split(GlobalArrays.soundingSites[position])
                }
            }
            else -> {
                tmpArr = if (data[position].contains(":")) {
                    MyApplication.colon.split(data[position])
                } else {
                    MyApplication.space.split(data[position])
                }
            }
        }
        if (!ridFav.contains(tmpArr[0])) {
            when (type) {
                "SPCMESO" -> {
                    ridFav += UtilitySpcMeso.params[position] + ":"
                    ridFavLabel += UtilitySpcMeso.labels[position] + ":"
                    Utility.writePref(this, prefTokenLabel, ridFavLabel)
                }
                else -> ridFav += tmpArr[0] + ":"
            }
            Utility.writePref(this, prefToken, ridFav)
            saveMyApp(ridFav, ridFavLabel)
            toolbar.subtitle = "Last added: $item"
        } else {
            toolbar.subtitle = "Already added: $item"
        }
    }

    private fun saveMyApp(fav: String, favLabel: String) {
        when (type) {
            "SND" -> MyApplication.sndFav = fav
            "WFO" -> MyApplication.wfoFav = fav
            "RID" -> MyApplication.ridFav = fav
            "NWSTEXT" -> MyApplication.nwsTextFav = fav
            "SREF" -> MyApplication.srefFav = fav
            "SPCMESO" -> {
                MyApplication.spcMesoFav = fav
                MyApplication.spcmesoLabelFav = favLabel
            }
        }
    }
}


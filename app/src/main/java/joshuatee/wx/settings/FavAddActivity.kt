/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import joshuatee.wx.*

import joshuatee.wx.models.UtilityModelsSPCSREFInterface
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.SingleTextAdapter

import joshuatee.wx.spc.UtilitySPCMESO
import joshuatee.wx.util.Utility

class FavAddActivity : BaseActivity() {

    companion object {
        const val TYPE = ""
    }

    private var prefToken = ""
    private var prefTokenLabel = ""
    private var data = listOf<String>()
    private var dataTokens = listOf<String>()
    private var type = ""

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_toolbar, null, false)
        type = intent.getStringArrayExtra(TYPE)[0]
        title = "Add $type"
        when (type) {
            "SND" -> {
                prefToken = "SND_FAV"
                val tmpArr = mutableListOf<String>()
                SND_ARR.indices.forEach {
                    var tmpLoc = Utility.readPref(this, "NWS_LOCATION_" + SND_ARR[it], "")
                    if (tmpLoc == "") {
                        tmpLoc = Utility.readPref(this, "NWS_SOUNDINGLOCATION_" + SND_ARR[it], "")
                    }
                    tmpArr.add(SND_ARR[it] + " " + tmpLoc)
                }
                data = tmpArr.toList()
            }
            "RIDCA" -> {
                prefToken = "RID_CA_FAV"
                data = CA_RID_ARR
            }
            "WFO" -> {
                prefToken = "WFO_FAV"
                data = WFO_ARR
            }
            "RID" -> {
                prefToken = "RID_FAV"
                data = RID_ARR + TDWR_RIDS
            }
            "NWSTEXT" -> {
                prefToken = "NWS_TEXT_FAV"
                data = NWS_TXT_ARR
            }
            "SREF" -> {
                prefToken = "SREF_FAV"
                data = UtilityModelsSPCSREFInterface.PARAMS
            }
            "SPCMESO" -> {
                prefToken = "SPCMESO_FAV"
                prefTokenLabel = "SPCMESO_LABEL_FAV"
                data = UtilitySPCMESO.LABELS
                dataTokens = UtilitySPCMESO.PARAMS
            }
        }
        val recyclerView: RecyclerView = findViewById(R.id.card_list)
        recyclerView.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = RecyclerView.VERTICAL
        recyclerView.layoutManager = llm
        val ca = SingleTextAdapter(data)
        recyclerView.adapter = ca
        ca.setOnItemClickListener(object : SingleTextAdapter.MyClickListener {
            override fun onItemClick(position: Int) {
                itemClicked(position)
            }
        })
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
                tmpArr = if (SND_ARR[position].contains(":")) {
                    MyApplication.colon.split(SND_ARR[position])
                } else {
                    MyApplication.space.split(SND_ARR[position])
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
                    ridFav += UtilitySPCMESO.PARAMS[position] + ":"
                    ridFavLabel += UtilitySPCMESO.LABELS[position] + ":"
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
                MyApplication.spcmesoFav = fav
                MyApplication.spcmesoLabelFav = favLabel
            }
        }
    }
}


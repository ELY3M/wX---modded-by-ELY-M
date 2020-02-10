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
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.util.UtilityFavorites

import joshuatee.wx.GlobalArrays
import joshuatee.wx.spc.UtilitySpcMeso
import joshuatee.wx.ui.ObjectRecyclerView
import joshuatee.wx.util.Utility
import joshuatee.wx.wpc.UtilityWpcText

class FavRemoveActivity : BaseActivity() {

    //
    // called from various activities that need favorite management,
    // allows one to remove from list of favorite sites and reorder
    //
    // arg1: type such as SND WFO RID
    //

    companion object {
        const val TYPE: String = ""
    }

    private val ridArr = mutableListOf<String>()
    private var tempList = listOf<String>()
    private var ridFav = ""
    private var ridFavLabel = ""
    private var prefToken = ""
    private var prefTokenLabel = ""
    private var prefTokenLocation = ""
    private var ridArrLabel = mutableListOf<String>()
    private lateinit var recyclerView: ObjectRecyclerView
    private var type = ""
    private val initialValue = " : : "

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
                savedInstanceState,
                R.layout.activity_recyclerview_toolbar,
                null,
                false
        )
        val activityArguments = intent.getStringArrayExtra(TYPE)
        type = activityArguments!![0]
        var verboseTitle = ""
        when (type) {
            "SND" -> {
                prefToken = "SND_FAV"
                prefTokenLocation = "NWS_LOCATION_"
                verboseTitle = "sounding sites"
            }
            "WFO" -> {
                prefToken = "WFO_FAV"
                prefTokenLocation = "NWS_LOCATION_"
                verboseTitle = "NWS offices"
            }
            "RID" -> {
                prefToken = "RID_FAV"
                prefTokenLocation = "RID_LOC_"
                verboseTitle = "radar sites"
            }
            "NWSTEXT" -> {
                prefToken = "NWS_TEXT_FAV"
                verboseTitle = "text products"
            }
            "SREF" -> {
                prefToken = "SREF_FAV"
                verboseTitle = "parameters"
            }
            "RIDCA" -> {
                prefToken = "RID_CA_FAV"
                verboseTitle = "radar sites"
            }
            "SPCMESO" -> {
                prefToken = "SPCMESO_FAV"
                prefTokenLabel = "SPCMESO_LABEL_FAV"
                verboseTitle = "parameters"
            }
        }
        ridFav = Utility.readPref(this, prefToken, UtilityFavorites.initialValue)
        title = "Modify $verboseTitle"
        toolbar.subtitle = "Tap item to delete or move."
        updateList()
        recyclerView = ObjectRecyclerView(this, this, R.id.card_list, ridArrLabel, ::itemClicked)
    }

    private fun updateList() {
        tempList = ridFav.split(":").dropLastWhile { it.isEmpty() }
        ridArr.clear()
        (3 until tempList.size).mapTo(ridArr) { tempList[it] }
        ridArrLabel = mutableListOf()
        ridArr.forEach {
            when (type) {
                "NWSTEXT" -> ridArrLabel.add(getFullString(it))
                "SREF" -> ridArrLabel.add(it)
                "RIDCA" -> ridArrLabel.add(findCanadaRadarSiteLabel(it))
                "SPCMESO" -> ridArrLabel.add(findSpcMesoLabel(it))
                else -> ridArrLabel.add(getFullString(it))
            }
        }
    }

    private fun moveUp(pos: Int) {
        ridFav = Utility.readPref(this, prefToken, "")
        tempList = ridFav.split(":").dropLastWhile { it.isEmpty() }
        ridArr.clear()
        (3 until tempList.size).mapTo(ridArr) { tempList[it] }
        if (pos != 0) {
            val tmp = ridArr[pos - 1]
            val tmp2 = ridArr[pos]
            ridArr[pos - 1] = tmp2
            recyclerView.setItem(pos - 1, recyclerView.getItem(pos))
            ridArr[pos] = tmp
            recyclerView.setItem(pos, getFullString(tmp))
        } else {
            val tmp = ridArr.last()
            val tmp2 = ridArr[pos]
            ridArr[ridArr.lastIndex] = tmp2
            recyclerView.setItem(ridArr.lastIndex, recyclerView.getItem(pos))
            ridArr[0] = tmp
            recyclerView.setItem(0, getFullString(tmp))
        }
        when (type) {
            "SPCMESO" -> {
                ridFav = initialValue
                ridArr.forEach {
                    ridFav += ":$it"
                }
                Utility.writePref(this, prefToken, "$ridFav:")
                ridFavLabel = initialValue
                ridFavLabel += recyclerView.toString()
                Utility.writePref(this, prefTokenLabel, ridFavLabel)
            }
            else -> {
                ridFav = initialValue
                ridArr.forEach {
                    ridFav += ":$it"
                }
                Utility.writePref(this, prefToken, "$ridFav:")
            }
        }
    }

    private fun moveDown(pos: Int) {
        ridFav = Utility.readPref(this, prefToken, "")
        tempList = ridFav.split(":").dropLastWhile { it.isEmpty() }
        ridArr.clear()
        (3 until tempList.size).mapTo(ridArr) { tempList[it] }
        if (pos != ridArr.lastIndex) {
            val tmp = ridArr[pos + 1]
            val tmp2 = ridArr[pos]
            ridArr[pos + 1] = tmp2
            recyclerView.setItem(pos + 1, recyclerView.getItem(pos))
            ridArr[pos] = tmp
            recyclerView.setItem(pos, getFullString(tmp))
        } else {
            val tmp = ridArr[0]
            ridArr[0] = ridArr[pos]
            recyclerView.setItem(0, recyclerView.getItem(pos))
            ridArr[ridArr.lastIndex] = tmp
            recyclerView.setItem(ridArr.lastIndex, getFullString(tmp))
        }
        ridFav = initialValue
        ridArr.forEach {
            ridFav += ":$it"
        }
        Utility.writePref(this, prefToken, "$ridFav:")
        when (type) {
            "SPCMESO" -> {
                ridFavLabel = initialValue
                ridFavLabel += recyclerView.toString()
                Utility.writePref(this, prefTokenLabel, ridFavLabel)
            }
        }
    }

    private fun getFullString(shortCode: String): String {
        var fullName = ""
        when (type) {
            "SND" -> fullName = Utility.getSoundingSiteName(shortCode)
            "WFO" -> fullName = shortCode + ": " + Utility.getWfoSiteName(shortCode)
            "RID" -> fullName = shortCode + ": " + Utility.getRadarSiteName(shortCode)
            "NWSTEXT" -> fullName = UtilityWpcText.labels[UtilityFavorites.findPositionNwsText(shortCode)]
            "SREF" -> fullName = shortCode
            "RIDCA" -> fullName = findCanadaRadarSiteLabel(shortCode)
            "SPCMESO" -> fullName = findSpcMesoLabel(shortCode)
        }
        return fullName
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

    private fun findCanadaRadarSiteLabel(rid: String) =
            (GlobalArrays.canadaRadars.indices).firstOrNull {
                GlobalArrays.canadaRadars[it].contains(
                        rid
                )
            }
                    ?.let { GlobalArrays.canadaRadars[it].replace(":", "") }
                    ?: rid

    private fun findSpcMesoLabel(rid: String): String {
        val index = UtilitySpcMeso.params.indexOf(rid)
        if (index == -1)
            return UtilitySpcMeso.labels[0]
        return UtilitySpcMeso.labels[index]
    }

    private fun itemClicked(position: Int) {
        val bottomSheetFragment = BottomSheetFragment(this, position, recyclerView.getItem(position), false)
        bottomSheetFragment.functions = listOf(::deleteItem, ::moveUpItem, ::moveDownItem)
        bottomSheetFragment.labelList = listOf("Delete Item", "Move Up", "Move Down")
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    private fun deleteItem(position: Int) {
        when (type) {
            "SPCMESO" -> {
                ridFav = Utility.readPref(this, prefToken, " : :")
                ridFav = ridFav.replace(ridArr[position] + ":", "")
                recyclerView.deleteItem(position)
                ridFavLabel = initialValue
                ridFavLabel += recyclerView.toString()
                Utility.writePref(this, prefToken, ridFav)
                Utility.writePref(this, prefTokenLabel, ridFavLabel)
                saveMyApp(ridFav, ridFavLabel)
            }
            else -> {
                ridFav = Utility.readPref(this, prefToken, " : :")
                ridFav = ridFav.replace(ridArr[position] + ":", "")
                recyclerView.deleteItem(position)
                Utility.writePref(this, prefToken, ridFav)
                saveMyApp(ridFav, ridFavLabel)
            }
        }
    }

    private fun moveUpItem(position: Int) {
        moveUp(position)
        saveMyApp(ridFav, ridFavLabel)
    }

    private fun moveDownItem(position: Int) {
        moveDown(position)
        saveMyApp(ridFav, ridFavLabel)
    }
}


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

    // FIXME this is nasty, works but has not been touched in years

    companion object { const val TYPE = "" }

    private var favorites = mutableListOf<String>()
    private var favoriteString = ""
    private var prefToken = ""
    private var prefTokenLocation = ""
    private var labels = mutableListOf<String>()
    private lateinit var objectRecyclerView: ObjectRecyclerView
    private var type = ""
    private val initialValue = " : : "
    private val startIndex = 3

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_toolbar, null, false)
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
                verboseTitle = "parameters"
            }
        }
        favoriteString = Utility.readPref(this, prefToken, UtilityFavorites.initialValue)
        title = "Modify $verboseTitle"
        toolbar.subtitle = "Tap item to delete or move."
        updateList()
        objectRecyclerView = ObjectRecyclerView(this, this, R.id.card_list, labels, ::itemClicked)
    }

    private fun updateList() {
        val tempList = favoriteString.split(":").dropLastWhile { it.isEmpty() }
        favorites.clear()
        favorites = (startIndex until tempList.size).map{ tempList[it] }.toMutableList()
        labels = mutableListOf()
        favorites.forEach {
            when (type) {
                "NWSTEXT" -> labels.add(getFullString(it))
                "SREF" -> labels.add(it)
                "RIDCA" -> labels.add(findCanadaRadarSiteLabel(it))
                "SPCMESO" -> labels.add(findSpcMesoLabel(it))
                else -> labels.add(getFullString(it))
            }
        }
    }

    // FIXME this needs to be redone with consolidation of homescreen, settings-location activity as well
    private fun moveUp(position: Int) {
        favoriteString = Utility.readPref(this, prefToken, "")
        val tempList = favoriteString.split(":").dropLastWhile { it.isEmpty() }
        favorites.clear()
        favorites = (startIndex until tempList.size).map { tempList[it] }.toMutableList()
        if (position != 0) {
            val tmp = favorites[position - 1]
            val tmp2 = favorites[position]
            favorites[position - 1] = tmp2
            objectRecyclerView.setItem(position - 1, objectRecyclerView.getItem(position))
            favorites[position] = tmp
            objectRecyclerView.setItem(position, getFullString(tmp))
        } else {
            val tmp = favorites.last()
            val tmp2 = favorites[position]
            favorites[favorites.lastIndex] = tmp2
            objectRecyclerView.setItem(favorites.lastIndex, objectRecyclerView.getItem(position))
            favorites[0] = tmp
            objectRecyclerView.setItem(0, getFullString(tmp))
        }
        favoriteString = initialValue
        favorites.forEach { favoriteString += ":$it" }
        Utility.writePref(this, prefToken, "$favoriteString:")
    }

    private fun moveDown(pos: Int) {
        favoriteString = Utility.readPref(this, prefToken, "")
        val tempList = favoriteString.split(":").dropLastWhile { it.isEmpty() }
        favorites.clear()
        favorites = (startIndex until tempList.size).map { tempList[it] }.toMutableList()
        if (pos != favorites.lastIndex) {
            val tmp = favorites[pos + 1]
            val tmp2 = favorites[pos]
            favorites[pos + 1] = tmp2
            objectRecyclerView.setItem(pos + 1, objectRecyclerView.getItem(pos))
            favorites[pos] = tmp
            objectRecyclerView.setItem(pos, getFullString(tmp))
        } else {
            val tmp = favorites[0]
            favorites[0] = favorites[pos]
            objectRecyclerView.setItem(0, objectRecyclerView.getItem(pos))
            favorites[favorites.lastIndex] = tmp
            objectRecyclerView.setItem(favorites.lastIndex, getFullString(tmp))
        }
        favoriteString = initialValue
        favorites.forEach { favoriteString += ":$it" }
        Utility.writePref(this, prefToken, "$favoriteString:")
    }

    private fun getFullString(shortCode: String): String {
        return when (type) {
            "SND" -> Utility.getSoundingSiteName(shortCode)
            "WFO" -> shortCode + ": " + Utility.getWfoSiteName(shortCode)
            "RID" -> shortCode + ": " + Utility.getRadarSiteName(shortCode)
            "NWSTEXT" -> shortCode + ": " + UtilityWpcText.getLabel(shortCode)
            "SREF" -> shortCode
            "RIDCA" -> findCanadaRadarSiteLabel(shortCode)
            "SPCMESO" -> findSpcMesoLabel(shortCode)
            else -> ""
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

    private fun findCanadaRadarSiteLabel(rid: String) =
            (GlobalArrays.canadaRadars.indices).firstOrNull {
                        GlobalArrays.canadaRadars[it].contains(rid)
                    }?.let { GlobalArrays.canadaRadars[it].replace(":", "") } ?: rid

    private fun findSpcMesoLabel(rid: String): String {
        val index = UtilitySpcMeso.params.indexOf(rid)
        return if (index == -1) UtilitySpcMeso.labels[0] else UtilitySpcMeso.labels[index]
    }

    private fun itemClicked(position: Int) {
        val bottomSheetFragment = BottomSheetFragment(this, position, objectRecyclerView.getItem(position), false)
        bottomSheetFragment.functions = listOf(::deleteItem, ::moveUpItem, ::moveDownItem)
        bottomSheetFragment.labelList = listOf("Delete Item", "Move Up", "Move Down")
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    private fun deleteItem(position: Int) {
        when (type) {
            "SPCMESO" -> {
                favoriteString = Utility.readPref(this, prefToken, " : :")
                favoriteString = favoriteString.replace(favorites[position] + ":", "")
                objectRecyclerView.deleteItem(position)
                Utility.writePref(this, prefToken, favoriteString)
                saveMyApp(favoriteString)
            }
            else -> {
                favoriteString = Utility.readPref(this, prefToken, " : :")
                favoriteString = favoriteString.replace(favorites[position] + ":", "")
                objectRecyclerView.deleteItem(position)
                Utility.writePref(this, prefToken, favoriteString)
                saveMyApp(favoriteString)
            }
        }
    }

    private fun moveUpItem(position: Int) {
        moveUp(position)
        saveMyApp(favoriteString)
    }

    private fun moveDownItem(position: Int) {
        moveDown(position)
        saveMyApp(favoriteString)
    }
}


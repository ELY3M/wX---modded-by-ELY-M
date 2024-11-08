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

package joshuatee.wx.settings

import android.annotation.SuppressLint
import android.os.Bundle
import joshuatee.wx.swap
import joshuatee.wx.R
import joshuatee.wx.objects.FavoriteType
import joshuatee.wx.radar.RadarSites
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.util.UtilityFavorites
import joshuatee.wx.spc.UtilitySpcMeso
import joshuatee.wx.ui.ObjectRecyclerView
import joshuatee.wx.util.SoundingSites
import joshuatee.wx.util.Utility
import joshuatee.wx.util.WfoSites
import joshuatee.wx.wpc.UtilityWpcText

class FavRemoveActivity : BaseActivity() {

    //
    // called from various activities that need favorite management,
    // allows one to remove from list of favorite sites and reorder
    //
    // arg1: type such as "SND", "WFO", "SREF", "SPCMESO", "NWSTEXT", and "RID"
    //

    companion object {
        const val TYPE = ""
    }

    private var favorites = mutableListOf<String>()
    private var favoriteString = ""
    private var prefTokenLocation = ""
    private var labels = mutableListOf<String>()
    private lateinit var objectRecyclerView: ObjectRecyclerView
    private lateinit var type: FavoriteType
    private val initialValue = " : : "
    private var verboseTitle = ""

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_toolbar, null, false)
        type = FavoriteType.stringToType(intent.getStringArrayExtra(TYPE)!![0])
        setupVars()
        setTitle("Modify $verboseTitle", "Tap item to delete or move.")
        objectRecyclerView = ObjectRecyclerView(this, R.id.card_list, labels, ::itemClicked)
        updateList()
    }

    private fun setupVars() {
        when (type) {
            FavoriteType.SND -> {
                prefTokenLocation = "NWS_LOCATION_"
                verboseTitle = "sounding sites"
            }

            FavoriteType.WFO -> {
                prefTokenLocation = "NWS_LOCATION_"
                verboseTitle = "NWS offices"
            }

            FavoriteType.RID -> {
                prefTokenLocation = "RID_LOC_"
                verboseTitle = "radar sites"
            }

            FavoriteType.NWS_TEXT -> verboseTitle = "text products"
            FavoriteType.SREF -> verboseTitle = "parameters"
            FavoriteType.SPCMESO -> verboseTitle = "parameters"
        }
        favoriteString = Utility.readPref(
            this,
            UtilityFavorites.getPrefToken(type),
            UtilityFavorites.INITIAL_VALUE
        )
    }

    private fun updateList() {
        // strip leading " " due to add/modify labels and trailing empty
        favorites = favoriteString.split(":").dropWhile { it == " " }.dropLastWhile { it.isEmpty() }
            .toMutableList()
        labels.clear()
        favorites.forEach {
            when (type) {
                FavoriteType.NWS_TEXT -> labels.add(getFullString(it))
                FavoriteType.SREF -> labels.add(it)
                FavoriteType.SPCMESO -> labels.add(findSpcMesoLabel(it))
                else -> labels.add(getFullString(it))
            }
        }
        objectRecyclerView.notifyDataSetChanged()
    }

    private fun moveUp(pos: Int) {
        initData()
        if (pos != 0) {
            favorites.swap(pos - 1, pos)
        } else {
            favorites.swap(0, favorites.lastIndex)
        }
        writeData()
    }

    private fun moveDown(pos: Int) {
        initData()
        if (pos != favorites.lastIndex) {
            favorites.swap(pos + 1, pos)
        } else {
            favorites.swap(0, favorites.lastIndex)
        }
        writeData()
    }

    private fun deleteItem(position: Int) {
        favoriteString = Utility.readPref(this, UtilityFavorites.getPrefToken(type), " : :")
        favoriteString = favoriteString.replace(favorites[position] + ":", "")
        objectRecyclerView.deleteItem(position)
        Utility.writePref(this, UtilityFavorites.getPrefToken(type), favoriteString)
        saveMyApp(favoriteString)
    }

    private fun initData() {
        favoriteString = Utility.readPref(this, UtilityFavorites.getPrefToken(type), "")
        favorites = favoriteString.split(":").dropWhile { it == " " }.dropLastWhile { it.isEmpty() }
            .toMutableList()
    }

    private fun writeData() {
        favoriteString = initialValue
        favorites.forEach {
            favoriteString += ":$it"
        }
        Utility.writePref(this, UtilityFavorites.getPrefToken(type), "$favoriteString:")
        updateList()
        saveMyApp(favoriteString)
    }

    private fun getFullString(shortCode: String) = when (type) {
//        FavoriteType.SND -> UtilityLocation.getSoundingSiteName(shortCode)
        FavoriteType.SND -> SoundingSites.sites.byCode[shortCode]!!.fullName
        FavoriteType.WFO -> shortCode + ": " + WfoSites.getFullName(shortCode)
        FavoriteType.RID -> shortCode + ": " + RadarSites.getName(shortCode)
        FavoriteType.NWS_TEXT -> shortCode + ": " + UtilityWpcText.getLabel(shortCode)
        FavoriteType.SREF -> shortCode
        FavoriteType.SPCMESO -> findSpcMesoLabel(shortCode)
    }

    private fun saveMyApp(s: String) {
        UIPreferences.favorites[type] = s
    }

    private fun findSpcMesoLabel(rid: String): String {
        val index = UtilitySpcMeso.params.indexOf(rid)
        return if (index == -1) {
            UtilitySpcMeso.labels[0]
        } else {
            UtilitySpcMeso.labels[index]
        }
    }

    private fun itemClicked(position: Int) {
        val bottomSheetFragment =
            BottomSheetFragment(this, position, objectRecyclerView.getItem(position), false)
        bottomSheetFragment.functions = listOf(::deleteItem, ::moveUp, ::moveDown)
        bottomSheetFragment.labelList = listOf("Delete Item", "Move Up", "Move Down")
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }
}

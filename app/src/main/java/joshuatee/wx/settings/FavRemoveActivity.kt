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
import android.view.View

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.objects.ActionMode
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.util.UtilityFavorites

import joshuatee.wx.CA_RID_ARR
import joshuatee.wx.NWS_TXT_ARR
import joshuatee.wx.spc.UtilitySPCMESO
import joshuatee.wx.ui.ObjectRecyclerView
import joshuatee.wx.util.Utility

class FavRemoveActivity : BaseActivity() {

    // called from various activities that need favorite manament,
    // allows one to remove from list of favorite sites and reorder
    //
    // arg1: type such as SND WFO RID

    companion object {
        const val TYPE: String = ""
    }

    private val ridArr = mutableListOf<String>()
    private var ridArrtmp = listOf<String>()
    private var ridFav = ""
    private var ridFavLabel = ""
    private var prefToken = ""
    private var prefTokenLabel = ""
    private var prefTokenLocation = ""
    private var ridArrLabel = mutableListOf<String>()
    private lateinit var recyclerView: ObjectRecyclerView
    private var type = ""
    private var actionMode = ActionMode.DELETE

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_recyclerview_toolbar_with_threefab,
            null,
            false
        )
        val turl = intent.getStringArrayExtra(TYPE)
        type = turl[0]
        when (type) {
            "SND" -> {
                prefToken = "SND_FAV"
                prefTokenLocation = "NWS_LOCATION_"
            }
            "WFO" -> {
                prefToken = "WFO_FAV"
                prefTokenLocation = "NWS_LOCATION_"
            }
            "RID" -> {
                prefToken = "RID_FAV"
                prefTokenLocation = "RID_LOC_"
            }
            "NWSTEXT" -> prefToken = "NWS_TEXT_FAV"
            "SREF" -> prefToken = "SREF_FAV"
            "RIDCA" -> prefToken = "RID_CA_FAV"
            "RIDAU" -> prefToken = "RID_AU_FAV"
            "SPCMESO" -> {
                prefToken = "SPCMESO_FAV"
                prefTokenLabel = "SPCMESO_LABEL_FAV"
            }
        }
        ridFav = Utility.readPref(this, prefToken, " : : :")
        title = "Modify $type"
        toolbar.subtitle = actionMode.getDescription()
        ObjectFab(
            this,
            this,
            R.id.fab,
            MyApplication.ICON_DELETE,
            View.OnClickListener { toggleMode(ActionMode.DELETE) })
        ObjectFab(
            this,
            this,
            R.id.fab1,
            MyApplication.ICON_ARROW_UP,
            View.OnClickListener { toggleMode(ActionMode.UP) })
        ObjectFab(
            this,
            this,
            R.id.fab2,
            MyApplication.ICON_ARROW_DOWN,
            View.OnClickListener { toggleMode(ActionMode.DOWN) })
        updateList()
        recyclerView = ObjectRecyclerView(this, this, R.id.card_list, ridArrLabel, ::itemClicked)
    }

    private fun updateList() {
        ridArrtmp = ridFav.split(":").dropLastWhile { it.isEmpty() }
        ridArr.clear()
        (3 until ridArrtmp.size).mapTo(ridArr) { ridArrtmp[it] }
        ridArrLabel = mutableListOf()
        ridArr.indices.forEach {
            when (type) {
                "NWSTEXT" -> ridArrLabel.add(getFullString(ridArr[it]))
                "SREF" -> ridArrLabel.add(ridArr[it])
                "RIDCA" -> ridArrLabel.add(findCARIDLabel(ridArr[it]))
                "SPCMESO" -> ridArrLabel.add(findSPCMesoLabel(ridArr[it]))
                else -> ridArrLabel.add(getFullString(ridArr[it]))
            }
        }
    }

    private fun moveUp(pos: Int) {
        ridFav = Utility.readPref(this, prefToken, "")
        ridArrtmp = ridFav.split(":").dropLastWhile { it.isEmpty() }
        ridArr.clear()
        (3 until ridArrtmp.size).mapTo(ridArr) { ridArrtmp[it] }
        if (pos != 0) {
            val tmp = ridArr[pos - 1]
            val tmp2 = ridArr[pos]
            ridArr[pos - 1] = tmp2
            recyclerView.setItem(pos - 1, recyclerView.getItem(pos))
            ridArr[pos] = tmp
            recyclerView.setItem(pos, getFullString(tmp))
        } else {
            val tmp = ridArr[ridArr.size - 1]
            val tmp2 = ridArr[pos]
            ridArr[ridArr.size - 1] = tmp2
            recyclerView.setItem(ridArr.size - 1, recyclerView.getItem(pos))
            ridArr[0] = tmp
            recyclerView.setItem(0, getFullString(tmp))
        }
        when (type) {
            "SPCMESO" -> {
                ridFav = " : : "
                ridArr.indices.forEach { ridFav += ":" + ridArr[it] }
                // FIXME why does this need a trailing semi-colon
                Utility.writePref(this, prefToken, "$ridFav:")
                ridFavLabel = " : : "
                ridFavLabel += recyclerView.toString()
                Utility.writePref(this, prefTokenLabel, ridFavLabel)
            }
            else -> {
                ridFav = " : : "
                ridArr.indices.forEach { ridFav += ":" + ridArr[it] }
                Utility.writePref(this, prefToken, ridFav)
            }
        }
    }

    private fun moveDown(pos: Int) {
        ridFav = Utility.readPref(this, prefToken, "")
        ridArrtmp = ridFav.split(":").dropLastWhile { it.isEmpty() }
        ridArr.clear()
        (3 until ridArrtmp.size).mapTo(ridArr) { ridArrtmp[it] }
        if (pos != ridArr.size - 1) {
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
            ridArr[ridArr.size - 1] = tmp
            recyclerView.setItem(ridArr.size - 1, getFullString(tmp))
        }
        ridFav = " : : "
        ridArr.indices.forEach { ridFav = ridFav + ":" + ridArr[it] }
        Utility.writePref(this, prefToken, ridFav)
        when (type) {
            "SPCMESO" -> {
                ridFavLabel = " : : "
                ridFavLabel += recyclerView.toString()
                Utility.writePref(this, prefTokenLabel, ridFavLabel)
            }
        }
    }

    private fun getFullString(shortCode: String): String {
        var tmpLoc = ""
        when (type) {
            "SND" -> {
                tmpLoc = Utility.readPref(this, "NWS_LOCATION_$shortCode", "")
                tmpLoc = if (tmpLoc == "") {
                    shortCode + ": " + Utility.readPref(this, "NWS_SOUNDINGLOCATION_$shortCode", "")
                } else {
                    "$shortCode: $tmpLoc"
                }
            }
            "WFO" -> tmpLoc = shortCode + ": " +
                    Utility.readPref(this, prefTokenLocation + shortCode, "")
            "RID" -> tmpLoc = shortCode + ": " +
                    Utility.readPref(this, prefTokenLocation + shortCode, "")
            "NWSTEXT" -> tmpLoc = NWS_TXT_ARR[UtilityFavorites.findPositionNWSTEXT(shortCode)]
            "SREF" -> tmpLoc = shortCode
            "RIDCA" -> tmpLoc = findCARIDLabel(shortCode)
            "SPCMESO" -> tmpLoc = findSPCMesoLabel(shortCode)
        }
        return tmpLoc
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

    private fun findCARIDLabel(rid: String) =
        (0 until CA_RID_ARR.size).firstOrNull { CA_RID_ARR[it].contains(rid) }
            ?.let { CA_RID_ARR[it].replace(":", "") }
            ?: rid

    private fun findSPCMesoLabel(rid: String): String {
        val index = UtilitySPCMESO.params.indexOf(rid)
        if (index == -1) return UtilitySPCMESO.labels[0]
        return UtilitySPCMESO.labels[index]
    }

    private fun itemClicked(position: Int) {
        when (actionMode) {
            ActionMode.DELETE -> {
                when (type) {
                    "SPCMESO" -> {
                        ridFav = Utility.readPref(this, prefToken, " : :")
                        ridFav = ridFav.replace(ridArr[position] + ":", "")
                        recyclerView.deleteItem(position)
                        ridFavLabel = " : : "
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
            ActionMode.UP -> {
                moveUp(position)
                saveMyApp(ridFav, ridFavLabel)
            }
            ActionMode.DOWN -> {
                moveDown(position)
                saveMyApp(ridFav, ridFavLabel)
            }
            else -> {
            }
        }
    }

    private fun toggleMode(am: ActionMode) {
        actionMode = am
        toolbar.subtitle = actionMode.getDescription()
    }
}


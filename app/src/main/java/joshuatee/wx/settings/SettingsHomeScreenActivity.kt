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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.R
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.ui.*
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityAlertDialog
import joshuatee.wx.wpc.UtilityWpcText
import java.util.*

class SettingsHomeScreenActivity : BaseActivity(), Toolbar.OnMenuItemClickListener {

    private var favoriteList = mutableListOf<String>()
    private var favoriteString = ""
    private val prefToken = "HOMESCREEN_FAV"
    private var labels = mutableListOf<String>()
    private var homeScreenFavOrig = ""
    private lateinit var recyclerView: ObjectRecyclerView
    private lateinit var dialogueMain: ObjectDialogue
    private lateinit var dialogueImages: ObjectDialogue
    private lateinit var dialogueAfd: ObjectDialogue
    private lateinit var dialogueRadar: ObjectDialogue
    private lateinit var dialogueWeb: ObjectDialogue
    private val homeScreenCanadaDefault = "TXT-CC2:TXT-HAZ:IMG-CARAIN:TXT-7DAY2"

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_homescreen, R.menu.settings_homescreen, true)
        toolbarBottom.setOnMenuItemClickListener(this)
        favoriteString = UIPreferences.homescreenFav
        homeScreenFavOrig = favoriteString
        toolbar.subtitle = "Tap item to delete or move."
        UtilityToolbar.fullScreenMode(toolbar, false)
        ObjectFab(this, this, R.id.fab, GlobalVariables.ICON_ADD) { dialogueMain.show() }
        updateList(true)
        recyclerView = ObjectRecyclerView(this, this, R.id.card_list, labels, ::prodClicked)
        dialogueMain = ObjectDialogue(this, "Select text products:", UtilityHomeScreen.localChoicesText + UtilityWpcText.labels)
        dialogueMain.setSingleChoiceItems { dialog, which ->
            alertDialogClicked(dialogueMain, "TXT-", which)
            dialog.dismiss()
        }
        dialogueImages = ObjectDialogue(this, "Select image products:", UtilityHomeScreen.localChoicesImg + GlobalArrays.nwsImageProducts)
        dialogueImages.setSingleChoiceItems { dialog, which ->
            alertDialogClicked(dialogueImages, "", which)
            dialog.dismiss()
        }
        dialogueAfd = ObjectDialogue(this, "Select fixed location AFD products:", GlobalArrays.wfos)
        dialogueAfd.setSingleChoiceItems { dialog, which ->
            alertDialogClicked(dialogueAfd, "TXT-" + "AFD", which)
            dialog.dismiss()
        }
        dialogueRadar = ObjectDialogue(this, "Select fixed location Nexrad products:", GlobalArrays.radars + GlobalArrays.tdwrRadarsForHomeScreen)
        dialogueRadar.setSingleChoiceItems { dialog, which ->
            alertDialogClicked(dialogueRadar, "NXRD-", which)
            dialog.dismiss()
        }
        dialogueWeb = ObjectDialogue(this, "Select Web page:", UtilityHomeScreen.localChoicesWeb)
        dialogueWeb.setSingleChoiceItems { dialog, which ->
            alertDialogClicked(dialogueWeb, "WEB-", which)
            dialog.dismiss()
        }
    }

    private fun updateList(firstTime: Boolean = false) {
        favoriteString = favoriteString.replace("^:".toRegex(), "")
        saveHomescreenList()
        val tempList = favoriteString.split(":").dropLastWhile { it.isEmpty() }
        if (favoriteString != "") {
            favoriteList.clear()
            tempList.forEach {
                favoriteList.add(it)
            }
            if (firstTime) {
                labels = mutableListOf()
            }
            favoriteList.indices.forEach { k ->
                if (!firstTime) {
                    labels[k] = findPositionAFD(favoriteList[k])
                } else {
                    labels.add(findPositionAFD(favoriteList[k]))
                }
                if (labels[k] == "") labels[k] = findPositionTEXT(favoriteList[k])
                if (labels[k] == "") labels[k] = findPositionIMG(favoriteList[k])
                if (labels[k] == "") labels[k] = findPositionTEXTLOCAL(favoriteList[k])
                if (labels[k] == "") labels[k] = findPositionIMG2(favoriteList[k])
                if (labels[k] == "") labels[k] = findPositionRadarNexrad(favoriteList[k])
                if (labels[k] == "") labels[k] = findPositionRadarTdwr(favoriteList[k])
                if (labels[k] == "") labels[k] = favoriteList[k]
            }
        } else {
            if (!firstTime) {
                labels.clear()
                favoriteList.clear()
            } else {
                labels = mutableListOf()
                favoriteList = mutableListOf()
            }
        }
        if (!firstTime) {
            recyclerView.notifyDataSetChanged()
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_img -> dialogueImages.show()
            R.id.action_afd -> dialogueAfd.show()
            R.id.action_radar -> dialogueRadar.show()
            R.id.action_web -> dialogueWeb.show()
            R.id.action_help -> ObjectDialogue(this, resources.getString(R.string.homescreen_help_label))
            R.id.action_reset -> {
                UIPreferences.homescreenFav = UIPreferences.HOMESCREEN_FAV_DEFAULT
                favoriteString = UIPreferences.homescreenFav
                updateList(true)
                recyclerView.refreshList(labels)
            }
            R.id.action_reset_ca -> {
                UIPreferences.homescreenFav = homeScreenCanadaDefault
                favoriteString = UIPreferences.homescreenFav
                updateList(true)
                recyclerView.refreshList(labels)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun moveUp(position: Int) {
        favoriteString = UIPreferences.homescreenFav
        favoriteList = favoriteString.split(":").dropLastWhile { it.isEmpty() }.toMutableList()
        if (position != 0) {
            val tmp = favoriteList[position - 1]
            favoriteList[position - 1] = favoriteList[position]
            favoriteList[position] = tmp
        } else {
            val tmp = favoriteList.last()
            favoriteList[favoriteList.lastIndex] = favoriteList[position]
            favoriteList[0] = tmp
        }
        favoriteString = ""
        favoriteList.forEach {
            favoriteString += ":$it"
        }
        updateList()
    }

    private fun moveDown(position: Int) {
        favoriteString = UIPreferences.homescreenFav
        favoriteList = favoriteString.split(":").dropLastWhile { it.isEmpty() }.toMutableList()
        if (position != favoriteList.lastIndex) {
            val tmp = favoriteList[position + 1]
            favoriteList[position + 1] = favoriteList[position]
            favoriteList[position] = tmp
        } else {
            val tmp = favoriteList[0]
            favoriteList[0] = favoriteList[position]
            favoriteList[favoriteList.lastIndex] = tmp
        }
        favoriteString = ""
        favoriteList.forEach {
            favoriteString += ":$it"
        }
        updateList()
    }

    private fun saveHomescreenList() {
        Utility.writePref(this, prefToken, favoriteString)
        UIPreferences.homescreenFav = favoriteString
    }

    private fun findPositionTEXT(key: String) = (UtilityWpcText.labels.indices)
            .firstOrNull {
                UtilityWpcText.labels[it].startsWith(key.lowercase(Locale.US).replace("txt-", ""))
            } ?.let { UtilityWpcText.labels[it] } ?: ""

    private fun findPositionIMG(key: String) = (GlobalArrays.nwsImageProducts.indices)
            .firstOrNull { GlobalArrays.nwsImageProducts[it].startsWith(key.replace("IMG-", "")) }
            ?.let { GlobalArrays.nwsImageProducts[it] } ?: ""

    private fun findPositionIMG2(key: String): String {
        for (l in UtilityHomeScreen.localChoicesImg) {
            if (l.startsWith(key.replace("OGL-", ""))) {
                return l
            }
            if (l.startsWith(key.replace("IMG-", ""))) {
                return l
            }
        }
        return ""
    }

    private fun findPositionTEXTLOCAL(key: String) = UtilityHomeScreen.localChoicesText.firstOrNull { it.startsWith(key.replace("TXT-", "")) } ?: ""

    private fun findPositionAFD(key: String): String {
        (GlobalArrays.wfos.indices).forEach {
            if (GlobalArrays.wfos[it].startsWith(key.replace("TXT-AFD", ""))) {
                return "AFD " + GlobalArrays.wfos[it]
            }
            if (GlobalArrays.wfos[it].startsWith(key.replace("IMG-", ""))) {
                return "VIS " + GlobalArrays.wfos[it]
            }
        }
        return ""
    }

    private fun findPositionRadarNexrad(key: String): String {
        val allRadars = GlobalArrays.radars
        return allRadars.indices
                .firstOrNull { allRadars[it].startsWith(key.replace("NXRD-", "")) }
                ?.let { allRadars[it] + " (NEXRAD)" } ?: ""
    }

    private fun findPositionRadarTdwr(key: String): String {
        val allRadars = GlobalArrays.tdwrRadarsForHomeScreen
        return allRadars.indices
                .firstOrNull { allRadars[it].startsWith(key.replace("NXRD-", "")) }
                ?.let { allRadars[it] + " (TDWR)" } ?: ""
    }

    override fun onBackPressed() {
        if (favoriteString != homeScreenFavOrig) {
            UtilityAlertDialog.restart()
        } else {
            super.onBackPressed()
        }
    }

    private fun prodClicked(position: Int) {
        val bottomSheetFragment = BottomSheetFragment(this, position, recyclerView.getItem(position), false)
        bottomSheetFragment.functions = listOf(::deleteItem, ::moveUp, ::moveDown)
        bottomSheetFragment.labelList = listOf("Delete Item", "Move Up", "Move Down")
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    private fun deleteItem(position: Int) {
        if (position < favoriteList.size) {
            favoriteString = UIPreferences.homescreenFav
            favoriteString += ":"
            favoriteString = favoriteString.replace(favoriteList[position] + ":", "")
            favoriteString = favoriteString.replace(":$".toRegex(), "")
            recyclerView.deleteItem(position)
            recyclerView.notifyDataSetChanged()
            updateList()
        }
    }

    private fun alertDialogClicked(dialogue: ObjectDialogue, token: String, which: Int) {
        val strName = dialogue.getItem(which)
        var textProduct = token + strName.split(":").dropLastWhile { it.isEmpty() }[0].uppercase(Locale.US)
        if (token == "") {
            textProduct = if (textProduct != "RADAR") {
                "IMG-" + textProduct.uppercase(Locale.US)
            } else {
                "OGL-" + textProduct.uppercase(Locale.US)
            }
        }
        favoriteString = UIPreferences.homescreenFav
        val homeScreenList = favoriteString.split(":")
        if (!homeScreenList.contains(textProduct)) {
            favoriteString = "$favoriteString:$textProduct"
            Utility.writePref(this, prefToken, favoriteString)
            UIPreferences.homescreenFav = favoriteString
            labels.add(textProduct)
            updateList()
            recyclerView.notifyDataSetChanged()
        } else {
            ObjectPopupMessage(recyclerView.get(), "$textProduct is already in home screen.")
        }
    }
}

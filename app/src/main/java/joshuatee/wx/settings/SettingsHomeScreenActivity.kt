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
import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.swap
import joshuatee.wx.R
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.radar.RadarSites
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.FabExtended
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.ObjectRecyclerView
import joshuatee.wx.ui.PopupMessage
import joshuatee.wx.util.Utility
import joshuatee.wx.util.WfoSites
import joshuatee.wx.wpc.UtilityWpcText
import java.util.Locale

class SettingsHomeScreenActivity : BaseActivity(), Toolbar.OnMenuItemClickListener {

    private var favoriteList = mutableListOf<String>()
    private val prefToken = "HOMESCREEN_FAV"
    private var labels = mutableListOf<String>()
    private var homeScreenFavOrig = ""
    private lateinit var recyclerView: ObjectRecyclerView
    private lateinit var dialogueMain: ObjectDialogue
    private lateinit var dialogueImages: ObjectDialogue
    private lateinit var dialogueAfd: ObjectDialogue
    private lateinit var dialogueRadar: ObjectDialogue

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_recyclerview_homescreen,
            R.menu.settings_homescreen,
            true
        )
        setTitle("Manage Home Screen", "Tap item to delete or move.")
        FabExtended(
            this,
            R.id.fab_image,
            GlobalVariables.ICON_MAP,
            "Add Image"
        ) { dialogueImages.show() }
        FabExtended(
            this,
            R.id.fab_text,
            GlobalVariables.ICON_ADD,
            "Add Text Products"
        ) { dialogueMain.show() }
        objectToolbarBottom.connect(this)
        homeScreenFavOrig = UIPreferences.homescreenFav
        recyclerView = ObjectRecyclerView(this, R.id.card_list, labels, ::prodClicked)
        updateList()
        setupUI()
    }

    private fun setupUI() {
        dialogueMain = ObjectDialogue(
            this,
            "Select text products:",
            UtilityHomeScreen.localChoicesText + UtilityWpcText.labelsWithCodes
        )
        dialogueMain.connect { dialog, index -> dialogClicked(dialogueMain, "TXT-", index, dialog) }

        dialogueImages = ObjectDialogue(
            this,
            "Select image products:",
            UtilityHomeScreen.localChoicesImg + GlobalArrays.nwsImageProducts
        )
        dialogueImages.connect { dialog, index -> dialogClicked(dialogueImages, "", index, dialog) }

        dialogueAfd =
            ObjectDialogue(this, "Select fixed location AFD products:", WfoSites.sites.nameList)
        dialogueAfd.connect { dialog, index ->
            dialogClicked(
                dialogueAfd,
                "TXT-" + "AFD",
                index,
                dialog
            )
        }

        dialogueRadar = ObjectDialogue(
            this,
            "Select fixed location Nexrad products:",
            RadarSites.nexradRadars() + RadarSites.tdwrRadars()
        )
        dialogueRadar.connect { dialog, index ->
            dialogClicked(
                dialogueRadar,
                "NXRD-",
                index,
                dialog
            )
        }
    }

    private fun dialogClicked(
        dialogue: ObjectDialogue,
        token: String,
        index: Int,
        dialog: DialogInterface
    ) {
        val s = dialogue.getItem(index)
        var textProduct = token + s.split(":")[0].uppercase(Locale.US)
        if (token == "") {
            textProduct = if (textProduct != "RADAR") {
                "IMG-" + textProduct.uppercase(Locale.US)
            } else {
                "OGL-" + textProduct.uppercase(Locale.US)
            }
        }
        val homeScreenList = UIPreferences.homescreenFav.split(":")
        if (!homeScreenList.contains(textProduct)) {
            UIPreferences.homescreenFav = "${UIPreferences.homescreenFav}:$textProduct"
            saveHomescreenList()
            updateList()
        } else {
            PopupMessage(recyclerView.getView(), "$textProduct is already in home screen.")
        }
        dialog.dismiss()
    }

    private fun updateList() {
        UIPreferences.homescreenFav = UIPreferences.homescreenFav.replace("^:".toRegex(), "")
        saveHomescreenList()
        if (UIPreferences.homescreenFav != "") {
            favoriteList = UIPreferences.homescreenFav.split(":").dropLastWhile { it.isEmpty() }
                .toMutableList()
            labels = MutableList(favoriteList.size) { "" }
            recyclerView.refreshList(labels)
            favoriteList.forEachIndexed { k, fav ->
                labels[k] = findPositionAfd(fav)
                if (labels[k] == "") labels[k] = findPositionText(fav)
                if (labels[k] == "") labels[k] = findPositionImage(fav)
                if (labels[k] == "") labels[k] = findPositionTextLocal(fav)
                if (labels[k] == "") labels[k] = findPositionImage2(fav)
                if (labels[k] == "") labels[k] = findPositionRadarNexrad(fav)
                if (labels[k] == "") labels[k] = findPositionRadarTdwr(fav)
                if (labels[k] == "") labels[k] = fav
            }
        } else {
            labels.clear()
            favoriteList.clear()
        }
        recyclerView.notifyDataSetChanged()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_afd -> dialogueAfd.show()
            R.id.action_radar -> dialogueRadar.show()
            R.id.action_help -> ObjectDialogue(
                this,
                resources.getString(R.string.homescreen_help_label)
            )

            R.id.action_reset -> {
                UIPreferences.homescreenFav = UIPreferences.HOMESCREEN_FAVORITE_DEFAULT
                updateList()
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun moveUp(pos: Int) {
        initData()
        if (pos != 0) {
            favoriteList.swap(pos, pos - 1)
        } else {
            favoriteList.swap(0, favoriteList.lastIndex)
        }
        writeData()
    }

    private fun moveDown(pos: Int) {
        initData()
        if (pos != favoriteList.lastIndex) {
            favoriteList.swap(pos, pos + 1)
        } else {
            favoriteList.swap(0, favoriteList.lastIndex)
        }
        writeData()
    }

    private fun initData() {
        favoriteList =
            UIPreferences.homescreenFav.split(":").dropLastWhile { it.isEmpty() }.toMutableList()
    }

    private fun writeData() {
        UIPreferences.homescreenFav = ":" + favoriteList.joinToString(":")
        updateList()
    }

    private fun deleteItem(pos: Int) {
        if (pos < favoriteList.size) {
            recyclerView.deleteItem(pos)
            favoriteList.removeAt(pos)
            writeData()
            updateList()
        }
    }

    private fun saveHomescreenList() {
        Utility.writePref(this, prefToken, UIPreferences.homescreenFav)
    }

    private fun findPositionText(key: String) = UtilityWpcText.labelsWithCodes
        .firstOrNull { it.startsWith(key.lowercase(Locale.US).replace("txt-", "")) } ?: ""

    private fun findPositionImage(key: String) = GlobalArrays.nwsImageProducts
        .firstOrNull { it.startsWith(key.replace("IMG-", "")) } ?: ""

    private fun findPositionImage2(key: String): String {
        UtilityHomeScreen.localChoicesImg.forEach {
            if (it.startsWith(key.replace("OGL-", ""))) {
                return it
            }
            if (it.startsWith(key.replace("IMG-", ""))) {
                return it
            }
        }
        return ""
    }

    private fun findPositionTextLocal(key: String) = UtilityHomeScreen.localChoicesText
        .firstOrNull { it.startsWith(key.replace("TXT-", "")) } ?: ""

    private fun findPositionAfd(key: String): String {
        WfoSites.sites.nameList.forEach {
            if (it.startsWith(key.replace("TXT-AFD", ""))) {
                return "AFD $it"
            }
            if (it.startsWith(key.replace("IMG-", ""))) {
                return "VIS $it"
            }
        }
        return ""
    }

    private fun findPositionRadarNexrad(key: String) = RadarSites.nexradRadars()
        .firstOrNull { it.startsWith(key.replace("NXRD-", "")) }
        ?.let { "$it (NEXRAD)" } ?: ""

    private fun findPositionRadarTdwr(key: String) = RadarSites.tdwrRadars()
        .firstOrNull { it.startsWith(key.replace("NXRD-", "")) }
        ?.let { "$it (TDWR)" } ?: ""

    override fun onStop() {
        if (UIPreferences.homescreenFav != homeScreenFavOrig) {
            Utility.restart()
        } else {
            super.onStop()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (UIPreferences.homescreenFav != homeScreenFavOrig) {
                    Utility.restart()
                } else {
                    super.onStop()
                }
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun prodClicked(position: Int) {
        val bottomSheetFragment =
            BottomSheetFragment(this, position, recyclerView.getItem(position), false)
        with(bottomSheetFragment) {
            functions = listOf(::deleteItem, ::moveUp, ::moveDown)
            labelList = listOf("Delete Item", "Move Up", "Move Down")
            show(supportFragmentManager, bottomSheetFragment.tag)
        }
    }
}

/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View

import java.util.Locale

import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.util.UtilityAlertDialog

import joshuatee.wx.GlobalArrays

import joshuatee.wx.ui.*
import joshuatee.wx.util.Utility

class SettingsHomeScreenActivity : BaseActivity(), Toolbar.OnMenuItemClickListener {

    private var ridArr = mutableListOf<String>()
    private var ridFav = ""
    private val prefToken = "HOMESCREEN_FAV"
    private var ridArrLabel = mutableListOf<String>()
    private val localChoicesText = listOf(
        "CC: Current Conditions",
        "CC2: Current Conditions with image",
        "HAZ: Hazards", "7DAY: 7 Day Forecast",
        "7DAY2: 7 Day Forecast with images",
        "AFDLOC: Area Forecast Discussion",
        "HWOLOC: Hazardous Weather Outlook",
        "VFDLOC: Aviation only Area Forecast Discussion",
        "SUNMOON: Sun/Moon Data",
        "HOURLY: Hourly Forecast",
        "CTOF: Celsius to Fahrenheit table"
    )
    private val localChoicesImg = listOf(
        "RADAR: Local NEXRAD Radar",
        "CARAIN: Local CA Radar",
        "WEATHERSTORY: Local NWS Weather Story"
    )
    private var hmFavOrig = ""
    private lateinit var recyclerView: ObjectRecyclerView
    private lateinit var diaMain: ObjectDialogue
    private lateinit var diaImg: ObjectDialogue
    private lateinit var diaAfd: ObjectDialogue
    private lateinit var diaRadar: ObjectDialogue

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_recyclerview_homescreen,
            R.menu.settings_homescreen,
            true
        )
        toolbarBottom.setOnMenuItemClickListener(this)
        ridFav = MyApplication.homescreenFav
        hmFavOrig = ridFav
        toolbar.subtitle = "Tap item to delete or move."
        UtilityToolbar.fullScreenMode(toolbar, false)
        ObjectFab(
            this,
            this,
            R.id.fab,
            MyApplication.ICON_ADD,
            View.OnClickListener { diaMain.show() })
        updateList(true)
        recyclerView = ObjectRecyclerView(this, this, R.id.card_list, ridArrLabel, ::prodClicked)
        diaMain = ObjectDialogue(
            this,
            "Select text products:",
            localChoicesText + GlobalArrays.nwsTextProducts
        )
        diaMain.setSingleChoiceItems(DialogInterface.OnClickListener { _, which ->
            alertDialogClicked(
                diaMain,
                "TXT-",
                which
            )
        })
        diaImg = ObjectDialogue(
            this,
            "Select image products:",
            localChoicesImg + GlobalArrays.nwsImageProducts
        )
        diaImg.setSingleChoiceItems(DialogInterface.OnClickListener { _, which ->
            alertDialogClicked(
                diaImg,
                "",
                which
            )
        })
        diaAfd = ObjectDialogue(this, "Select fixed location AFD products:", GlobalArrays.wfos)
        diaAfd.setSingleChoiceItems(DialogInterface.OnClickListener { _, which ->
            alertDialogClicked(
                diaAfd,
                "TXT-" + "AFD",
                which
            )
        })
        diaRadar =
            ObjectDialogue(this, "Select fixed location Nexrad products:", GlobalArrays.radars)
        diaRadar.setSingleChoiceItems(DialogInterface.OnClickListener { _, which ->
            alertDialogClicked(
                diaRadar,
                "NXRD-",
                which
            )
        })
    }

    private fun updateList(firstTime: Boolean = false) {
        ridFav = ridFav.replace("^:".toRegex(), "")
        MyApplication.homescreenFav = ridFav
        Utility.writePref(this, prefToken, ridFav)
        val ridArrtmp = ridFav.split(":").dropLastWhile { it.isEmpty() }
        if (ridFav != "") {
            ridArr.clear()
            ridArrtmp.indices.forEach { ridArr.add(ridArrtmp[it]) }
            if (firstTime) {
                ridArrLabel = mutableListOf()
            }
            ridArr.indices.forEach { k ->
                if (!firstTime) {
                    ridArrLabel[k] = findPositionAFD(ridArr[k])
                } else {
                    ridArrLabel.add(findPositionAFD(ridArr[k]))
                }
                if (ridArrLabel[k] == "") {
                    ridArrLabel[k] = findPositionTEXT(ridArr[k])
                }
                if (ridArrLabel[k] == "") {
                    ridArrLabel[k] = findPositionIMG(ridArr[k])
                }
                if (ridArrLabel[k] == "") {
                    ridArrLabel[k] = findPositionTEXTLOCAL(ridArr[k])
                }
                if (ridArrLabel[k] == "") {
                    ridArrLabel[k] = findPositionIMG2(ridArr[k])
                }
                if (ridArrLabel[k] == "") {
                    ridArrLabel[k] = findPositionRadar(ridArr[k])
                }
                if (ridArrLabel[k] == "") {
                    ridArrLabel[k] = ridArr[k]
                }
            }
        } else {
            if (!firstTime) {
                ridArrLabel.clear()
                ridArr.clear()
            } else {
                ridArrLabel = mutableListOf()
                ridArr = mutableListOf()
            }
        }
        if (!firstTime) {
            recyclerView.notifyDataSetChanged()
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_img -> diaImg.show()
            R.id.action_afd -> diaAfd.show()
            R.id.action_radar -> diaRadar.show()
            R.id.action_help -> showHelpText(resources.getString(R.string.homescreen_help_label))
            R.id.action_reset -> {
                MyApplication.homescreenFav = MyApplication.HOMESCREEN_FAV_DEFAULT
                Utility.writePref(this, prefToken, MyApplication.homescreenFav)
                ridFav = MyApplication.homescreenFav
                updateList(true)
                recyclerView.refreshList(ridArrLabel)
            }
            R.id.action_reset_ca -> {
                MyApplication.homescreenFav = MyApplication.HOMESCREEN_FAV_DEFAULT_CA
                Utility.writePref(this, prefToken, MyApplication.homescreenFav)
                ridFav = MyApplication.homescreenFav
                updateList(true)
                recyclerView.refreshList(ridArrLabel)
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun showHelpText(helpStr: String) {
        UtilityAlertDialog.showHelpText(helpStr, this)
    }

    private fun moveUp(pos: Int) {
        ridFav = MyApplication.homescreenFav
        val ridArrtmp = MyApplication.colon.split(ridFav)
        ridArr.clear()
        ridArrtmp.indices.forEach { ridArr.add(ridArrtmp[it]) }
        if (pos != 0) {
            val tmp = ridArr[pos - 1]
            ridArr[pos - 1] = ridArr[pos]
            ridArr[pos] = tmp
        } else {
            val tmp = ridArr[ridArr.size - 1]
            ridArr[ridArr.size - 1] = ridArr[pos]
            ridArr[0] = tmp
        }
        ridFav = ""
        ridArr.indices.forEach { ridFav = ridFav + ":" + ridArr[it] }
        ridFav = ridFav.replace("^:".toRegex(), "")
        Utility.writePref(this, prefToken, ridFav)
    }

    private fun moveDown(pos: Int) {
        ridFav = MyApplication.homescreenFav
        val ridArrtmp = MyApplication.colon.split(ridFav)
        ridArr.clear()
        ridArrtmp.indices.forEach { ridArr.add(ridArrtmp[it]) }
        if (pos != ridArr.size - 1) {
            val tmp = ridArr[pos + 1]
            ridArr[pos + 1] = ridArr[pos]
            ridArr[pos] = tmp
        } else {
            val tmp = ridArr[0]
            ridArr[0] = ridArr[pos]
            ridArr[ridArr.size - 1] = tmp
        }
        ridFav = ""
        ridArr.indices.forEach { ridFav = ridFav + ":" + ridArr[it] }
        ridFav = ridFav.replace("^:".toRegex(), "")
        Utility.writePref(this, prefToken, ridFav)
    }

    private fun findPositionTEXT(key: String) = (0 until GlobalArrays.nwsTextProducts.size)
        .firstOrNull {
            GlobalArrays.nwsTextProducts[it].startsWith(
                key.toLowerCase().replace(
                    "txt-",
                    ""
                )
            )
        }
        ?.let { GlobalArrays.nwsTextProducts[it] }
        ?: ""

    private fun findPositionIMG(key: String) = (0 until GlobalArrays.nwsImageProducts.size)
        .firstOrNull { GlobalArrays.nwsImageProducts[it].startsWith(key.replace("IMG-", "")) }
        ?.let { GlobalArrays.nwsImageProducts[it] }
        ?: ""

    private fun findPositionIMG2(key: String): String {
        for (l in localChoicesImg) {
            if (l.startsWith(key.replace("OGL-", ""))) return l
            if (l.startsWith(key.replace("IMG-", ""))) return l
        }
        return ""
    }

    private fun findPositionTEXTLOCAL(key: String) =
        localChoicesText.firstOrNull { it.startsWith(key.replace("TXT-", "")) }
            ?: ""

    private fun findPositionAFD(key: String): String {
        (0 until GlobalArrays.wfos.size).forEach {
            if (GlobalArrays.wfos[it].startsWith(key.replace("TXT-AFD", ""))) {
                return "AFD " + GlobalArrays.wfos[it]
            }
            if (GlobalArrays.wfos[it].startsWith(key.replace("IMG-", ""))) {
                return "VIS " + GlobalArrays.wfos[it]
            }
        }
        return ""
    }

    private fun findPositionRadar(key: String) = (0 until GlobalArrays.radars.size)
        .firstOrNull { GlobalArrays.radars[it].startsWith(key.replace("NXRD-", "")) }
        ?.let { GlobalArrays.radars[it] + " (NEXRAD)" } ?: ""

    override fun onBackPressed() {
        if (ridFav != hmFavOrig) {
            UtilityAlertDialog.restart()
        } else {
            super.onBackPressed()
        }
    }

    private fun prodClicked(position: Int) {
        val bottomSheetFragment = BottomSheetFragment()
        bottomSheetFragment.position = position
        bottomSheetFragment.usedForLocation = true
        bottomSheetFragment.fnList = listOf(::deleteItem, ::moveUpItem, ::moveDownItem)
        bottomSheetFragment.labelList = listOf("Delete Item", "Move Up", "Move Down")
        bottomSheetFragment.actContext = this
        bottomSheetFragment.topLabel = recyclerView.getItem(position)
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    private fun moveUpItem(position: Int) {
        moveUp(position)
        MyApplication.homescreenFav = ridFav
        updateList()
    }

    private fun moveDownItem(position: Int) {
        moveDown(position)
        MyApplication.homescreenFav = ridFav
        updateList()
    }

    private fun deleteItem(position: Int) {
        if (position < ridArr.size) {
            ridFav = MyApplication.homescreenFav
            ridFav += ":"
            ridFav = ridFav.replace(ridArr[position] + ":", "")
            ridFav = ridFav.replace(":$".toRegex(), "")
            Utility.writePref(this, prefToken, ridFav)
            MyApplication.homescreenFav = ridFav
            recyclerView.deleteItem(position)
            recyclerView.notifyDataSetChanged()
            updateList()
        }
    }

    private fun alertDialogClicked(dialogue: ObjectDialogue, token: String, which: Int) {
        val strName = dialogue.getItem(which)
        var txtprod =
            token + strName.split(":").dropLastWhile { it.isEmpty() }[0].toUpperCase(Locale.US)
        if (token == "") {
            txtprod = if (txtprod != "RADAR") {
                "IMG-" + txtprod.toUpperCase()
            } else {
                "OGL-" + txtprod.toUpperCase()
            }
        }
        ridFav = MyApplication.homescreenFav
        if (!ridFav.contains(":$txtprod")) {
            ridFav = "$ridFav:$txtprod"
            Utility.writePref(this, prefToken, ridFav)
            MyApplication.homescreenFav = ridFav
            ridArrLabel.add(txtprod)
            updateList()
            recyclerView.notifyDataSetChanged()
        } else {
            UtilityUI.makeSnackBar(recyclerView.recyclerView, "$txtprod is already in homescreen.")
        }
    }
} 


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
import android.content.DialogInterface
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View

import java.util.Locale

import joshuatee.wx.R
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.ActionMode
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.ui.SingleTextAdapterList
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.UtilityAlertDialog

import joshuatee.wx.WFO_ARR
import joshuatee.wx.RID_ARR
import joshuatee.wx.NWS_IMG_ARR

import joshuatee.wx.NWS_TXT_ARR
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
    private lateinit var recyclerView: RecyclerView
    private lateinit var ca: SingleTextAdapterList
    private lateinit var diaMain: ObjectDialogue
    private lateinit var diaImg: ObjectDialogue
    private lateinit var diaAfd: ObjectDialogue
    private lateinit var diaVis: ObjectDialogue
    private lateinit var diaRadar: ObjectDialogue
    private var actionMode = ActionMode.DELETE

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
        toolbar.subtitle = actionMode.getDescription()
        UtilityToolbar.fullScreenMode(toolbar, false)
        ObjectFab(
            this,
            this,
            R.id.fab,
            MyApplication.ICON_ADD,
            View.OnClickListener { diaMain.show() })
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
        updateList(true)
        recyclerView = findViewById(R.id.card_list)
        recyclerView.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = RecyclerView.VERTICAL
        recyclerView.layoutManager = llm
        ca = SingleTextAdapterList(ridArrLabel)
        recyclerView.adapter = ca
        ca.setOnItemClickListener(object : SingleTextAdapterList.MyClickListener {
            override fun onItemClick(position: Int) {
                prodClicked(position)
            }
        })
        diaMain = ObjectDialogue(this, "Select text products:", localChoicesText + NWS_TXT_ARR)
        diaMain.setSingleChoiceItems(DialogInterface.OnClickListener { _, which ->
            alertDialogClicked(
                diaMain,
                "TXT-",
                which
            )
        })
        diaImg = ObjectDialogue(this, "Select image products:", localChoicesImg + NWS_IMG_ARR)
        diaImg.setSingleChoiceItems(DialogInterface.OnClickListener { _, which ->
            alertDialogClicked(
                diaImg,
                "",
                which
            )
        })
        diaAfd = ObjectDialogue(this, "Select fixed location AFD products:", WFO_ARR)
        diaAfd.setSingleChoiceItems(DialogInterface.OnClickListener { _, which ->
            alertDialogClicked(
                diaAfd,
                "TXT-" + "AFD",
                which
            )
        })
        diaVis = ObjectDialogue(this, "Select fixed location 1KM Vis products:", WFO_ARR)
        diaVis.setSingleChoiceItems(DialogInterface.OnClickListener { _, which ->
            alertDialogClicked(
                diaVis,
                "IMG-",
                which
            )
        })
        diaRadar = ObjectDialogue(this, "Select fixed location Nexrad products:", RID_ARR)
        diaRadar.setSingleChoiceItems(DialogInterface.OnClickListener { _, which ->
            alertDialogClicked(
                diaRadar,
                "NXRD-",
                which
            )
        })
    }

    private fun toggleMode(am: ActionMode) {
        actionMode = am
        toolbar.subtitle = actionMode.getDescription()
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
            ca.notifyDataSetChanged()
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> toggleMode(ActionMode.DELETE)
            R.id.action_img -> diaImg.show()
            R.id.action_afd -> diaAfd.show()
            R.id.action_cloud -> diaVis.show()
            R.id.action_radar -> diaRadar.show()
            R.id.action_help -> showHelpText(resources.getString(R.string.homescreen_help_label))
            R.id.action_reset -> {
                MyApplication.homescreenFav = MyApplication.HOMESCREEN_FAV_DEFAULT
                Utility.writePref(this, prefToken, MyApplication.homescreenFav)
                ridFav = MyApplication.homescreenFav
                updateList(true)
                ca = SingleTextAdapterList(ridArrLabel)
                recyclerView.adapter = ca
            }
            R.id.action_reset_ca -> {
                MyApplication.homescreenFav = MyApplication.HOMESCREEN_FAV_DEFAULT_CA
                Utility.writePref(this, prefToken, MyApplication.homescreenFav)
                ridFav = MyApplication.homescreenFav
                updateList(true)
                ca = SingleTextAdapterList(ridArrLabel)
                recyclerView.adapter = ca
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

    private fun findPositionTEXT(key: String) = (0 until NWS_TXT_ARR.size)
        .firstOrNull { NWS_TXT_ARR[it].startsWith(key.toLowerCase().replace("txt-", "")) }
        ?.let { NWS_TXT_ARR[it] }
        ?: ""

    private fun findPositionIMG(key: String) = (0 until NWS_IMG_ARR.size)
        .firstOrNull { NWS_IMG_ARR[it].startsWith(key.replace("IMG-", "")) }
        ?.let { NWS_IMG_ARR[it] }
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
        (0 until WFO_ARR.size).forEach {
            if (WFO_ARR[it].startsWith(key.replace("TXT-AFD", ""))) {
                return "AFD " + WFO_ARR[it]
            }
            if (WFO_ARR[it].startsWith(key.replace("IMG-", ""))) {
                return "VIS " + WFO_ARR[it]
            }
        }
        return ""
    }

    private fun findPositionRadar(key: String) = (0 until RID_ARR.size)
        .firstOrNull { RID_ARR[it].startsWith(key.replace("NXRD-", "")) }
        ?.let { RID_ARR[it] + " (NEXRAD)" } ?: ""

    override fun onBackPressed() {
        if (ridFav != hmFavOrig) {
            UtilityAlertDialog.restart()
        } else {
            super.onBackPressed()
        }
    }

    private fun prodClicked(position: Int) {
        when (actionMode) {
            ActionMode.DELETE -> {
                if (position < ridArr.size) {
                    ridFav = MyApplication.homescreenFav
                    ridFav += ":"
                    ridFav = ridFav.replace(ridArr[position] + ":", "")
                    ridFav = ridFav.replace(":$".toRegex(), "")
                    Utility.writePref(this, prefToken, ridFav)
                    MyApplication.homescreenFav = ridFav
                    ca.deleteItem(position)
                    ca.notifyDataSetChanged()
                }
            }
            ActionMode.UP -> {
                moveUp(position)
                MyApplication.homescreenFav = ridFav
            }
            ActionMode.DOWN -> {
                moveDown(position)
                MyApplication.homescreenFav = ridFav
            }
            else -> {
            }
        }
        updateList()
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
            ca.notifyDataSetChanged()
        } else {
            UtilityUI.makeSnackBar(recyclerView, "$txtprod is already in homescreen.")
        }
    }
} 


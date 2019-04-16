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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.view.View

import joshuatee.wx.R
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.ui.ObjectRecyclerViewGeneric
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility

class SettingsLocationRecyclerViewActivity : BaseActivity() {

    // Activity to manage ( add, delete, edit ) all locations
    //

    private val locArr = mutableListOf<String>()
    private lateinit var recyclerView: ObjectRecyclerViewGeneric
    private lateinit var ca: SettingsLocationAdapterList

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState,
            R.layout.activity_settings_location_recyclerview,
            null,
            false
        )
        ObjectFab(this, this, R.id.fab_add, View.OnClickListener { addItemFAB() })
        toolbar.subtitle = "Tap location to edit, delete, or move."
        updateList()
        recyclerView = ObjectRecyclerViewGeneric(this, this, R.id.card_list)
        ca = SettingsLocationAdapterList(locArr)
        recyclerView.recyclerView.adapter = ca
        updateTitle()
        ca.setListener(::itemSelected)
    }

    private fun updateList() {
        val locNumIntCurrent = Location.numLocations
        locArr.clear()
        (0 until locNumIntCurrent).forEach {
            val locNumStr = (it + 1).toString()
            val locX = Utility.readPref(this, "LOC" + locNumStr + "_X", "")
            val locY = Utility.readPref(this, "LOC" + locNumStr + "_Y", "")
            val locLabel = Utility.readPref(this, "LOC" + locNumStr + "_LABEL", "")
            val zone = Utility.readPref(this, "ZONE$locNumStr", "")
            val btnStr =
                (it + 1).toString() + ": \"" + locLabel + "\" " + "(" + UtilityStringExternal.truncate(
                    locX,
                    6
                ) + "," + UtilityStringExternal.truncate(locY, 6) + ") " + zone
            locArr.add(btnStr)
        }
    }

    override fun onRestart() {
        updateList()
        ca = SettingsLocationAdapterList(locArr)
        recyclerView.recyclerView.adapter = ca
        updateTitle()
        Location.refreshLocationData(this)
        super.onRestart()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onBroadcast)
        super.onPause()
    }

    private val onBroadcast = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, intent: Intent) {
            updateList()
        }
    }

    override fun onResume() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(onBroadcast, IntentFilter("locationadded"))
        super.onResume()
    }

    private fun updateTitle() {
        title = "Locations (" + ca.itemCount + ")"
    }

    private fun itemSelected(position: Int) {
        val bottomSheetFragment = BottomSheetFragment()
        bottomSheetFragment.position = position
        bottomSheetFragment.usedForLocation = true
        bottomSheetFragment.fnList = listOf(::edit, ::delete, ::moveUp, ::moveDown)
        bottomSheetFragment.labelList = listOf("Edit Location", "Delete Location", "Move Up", "Move Down")
        bottomSheetFragment.actContext = this
        bottomSheetFragment.topLabel = Location.getName(position)
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    private fun edit(position: Int) {
        val locStrPass = (position + 1).toString()
        val intent = Intent(this, SettingsLocationGenericActivity::class.java)
        intent.putExtra(SettingsLocationGenericActivity.LOC_NUM, arrayOf(locStrPass, ""))
        startActivity(intent)
    }

    private fun delete(position: Int) {
        if (ca.itemCount > 1) {
            Location.deleteLocation(this, (position + 1).toString())
            ca.deleteItem(position)
            ca.notifyDataSetChanged()
            updateList()
            updateTitle()
            UtilityWXJobService.startService(this)
        } else {
            UtilityUI.makeSnackBar(
                    recyclerView.recyclerView,
                    "Must have at least one location."
            )
        }
    }

    private fun moveUp(position: Int) {
        if (position > 0) {
            val locA = Location(this, position - 1)
            val locB = Location(this, position)
            locA.saveLocationToNewSlot(position)
            locB.saveLocationToNewSlot(position - 1)
        } else {
            val locA = Location(this, Location.numLocations - 1)
            val locB = Location(this, 0)
            locA.saveLocationToNewSlot(0)
            locB.saveLocationToNewSlot(Location.numLocations - 1)
        }
        ca.notifyDataSetChanged()
    }

    private fun moveDown(position: Int) {
        if (position < Location.numLocations - 1) {
            val locA = Location(this, position)
            val locB = Location(this, position + 1)
            locA.saveLocationToNewSlot(position + 1)
            locB.saveLocationToNewSlot(position)
        } else {
            val locA = Location(this, position)
            val locB = Location(this, 0)
            locA.saveLocationToNewSlot(0)
            locB.saveLocationToNewSlot(position)
        }
        ca.notifyDataSetChanged()
    }

    private fun addItemFAB() {
        val locStrPass = (locArr.size + 1).toString()
        val intent = Intent(this, SettingsLocationGenericActivity::class.java)
        intent.putExtra(SettingsLocationGenericActivity.LOC_NUM, arrayOf(locStrPass, ""))
        startActivity(intent)
    }
} 

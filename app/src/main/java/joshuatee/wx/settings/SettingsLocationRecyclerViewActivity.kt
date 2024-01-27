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
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.notifications.UtilityWXJobService
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.FabExtended
import joshuatee.wx.ui.PopupMessage
import joshuatee.wx.ui.RecyclerViewGeneric
import joshuatee.wx.util.CurrentConditions
import joshuatee.wx.util.To

class SettingsLocationRecyclerViewActivity : BaseActivity() {

    //
    // Activity to manage ( add, delete, edit ) all locations
    //

    private var locations = mutableListOf<String>()
    private lateinit var recyclerView: RecyclerViewGeneric
    private lateinit var settingsLocationAdapterList: SettingsLocationAdapterList
    private var currentConditionsList = mutableListOf<CurrentConditions>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_settings_location_recyclerview, null, false)
        FabExtended(this, R.id.fab_add, GlobalVariables.ICON_ADD, "Add Location") { addLocation() }
        updateList()
        setupUI()
        updateTitle()
        getContent()
    }

    private fun setupUI() {
        settingsLocationAdapterList = SettingsLocationAdapterList(locations)
        settingsLocationAdapterList.setListener(::itemSelected)
        recyclerView = RecyclerViewGeneric(this, R.id.card_list)
        recyclerView.adapter = settingsLocationAdapterList
    }

    private fun getContent() {
        currentConditionsList.clear()
        FutureVoid(::download, ::update)
    }

    private fun download() {
        Location.locations.indices.forEach { index ->
            currentConditionsList.add(CurrentConditions(this, index))
            currentConditionsList.last().format()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun update() {
        locations.clear()
        Location.locations.forEachIndexed { index, location ->
            location.updateObservation(currentConditionsList[index].topLine)
            locations.add(currentConditionsList[index].topLine)
        }
        settingsLocationAdapterList.notifyDataSetChanged()
    }

    private fun updateList() {
        locations = MutableList(Location.numLocations) { "" }
        Location.locations.forEach {
            it.updateObservation("")
        }
    }

    override fun onRestart() {
        updateList()
        settingsLocationAdapterList = SettingsLocationAdapterList(locations)
        recyclerView.adapter = settingsLocationAdapterList
        updateTitle()
        Location.refreshLocationData(this)
        getContent()
        super.onRestart()
    }

    private fun updateTitle() {
        setTitle("Locations (" + To.string(Location.numLocations) + ")", "Tap location to edit, delete, or move.")
    }

    private fun itemSelected(position: Int) {
        val bottomSheetFragment = BottomSheetFragment(this, position, Location.getName(position), true)
        bottomSheetFragment.functions = listOf(::edit, ::delete, ::moveUp, ::moveDown)
        bottomSheetFragment.labelList = listOf("Edit Location", "Delete Location", "Move Up", "Move Down")
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    private fun edit(position: Int) {
        Route.locationEdit(this, (position + 1).toString())
    }

    private fun delete(position: Int) {
        if (settingsLocationAdapterList.itemCount > 1) {
            Location.delete(this, (position + 1).toString())
            settingsLocationAdapterList.deleteItem(position)
            settingsLocationAdapterList.notifyItemRemoved(position)
            updateTitle()
            UtilityWXJobService.startService(this)
        } else {
            PopupMessage(recyclerView.get(), "Must have at least one location.")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun moveUp(position: Int) {
        if (position > 0) {
            val locA = ObjectLocation(this, position - 1)
            val locB = ObjectLocation(this, position)
            locA.saveToNewSlot(position)
            locB.saveToNewSlot(position - 1)
        } else {
            val locA = ObjectLocation(this, Location.numLocations - 1)
            val locB = ObjectLocation(this, 0)
            locA.saveToNewSlot(0)
            locB.saveToNewSlot(Location.numLocations - 1)
        }
        settingsLocationAdapterList.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun moveDown(position: Int) {
        if (position < Location.numLocations - 1) {
            val locA = ObjectLocation(this, position)
            val locB = ObjectLocation(this, position + 1)
            locA.saveToNewSlot(position + 1)
            locB.saveToNewSlot(position)
        } else {
            val locA = ObjectLocation(this, position)
            val locB = ObjectLocation(this, 0)
            locA.saveToNewSlot(0)
            locB.saveToNewSlot(position)
        }
        settingsLocationAdapterList.notifyDataSetChanged()
    }

    private fun addLocation() {
        Route.locationEdit(this, (locations.size + 1).toString())
    }
}

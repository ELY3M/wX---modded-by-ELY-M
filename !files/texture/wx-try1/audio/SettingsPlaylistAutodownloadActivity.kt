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

package joshuatee.wx.audio

import android.annotation.SuppressLint
import java.util.Calendar
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.ui.SingleTextAdapter
import joshuatee.wx.util.Utility

class SettingsPlaylistAutodownloadActivity : BaseActivity() {

    private var currHr: Int = 0
    private var currMin: Int = 0
    private lateinit var globalc: Context
    private var ridArr = listOf<String>()
    private var ridFav = ""
    private val tokenSep = "T"
    private val prefToken = "PLAYLIST_AUTODOWNLOAD_TIMES"
    private var deleteMode = false
    private var hour = 0
    private var minute = 0
    private var gposition = 0
    private val modifyModeString = "Modify mode"
    private lateinit var recyclerView: RecyclerView
    private lateinit var ca: SingleTextAdapter
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_toolbar_with_twofab, null, false)
        contextg = this
        toolbar.subtitle = modifyModeString
        globalc = this
        val fab1 = ObjectFab(this, this, R.id.fab1, R.drawable.ic_alarm_add_24dp)
        fab1.setOnClickListener(View.OnClickListener { pickTimeFAB() })
        val fab2 = ObjectFab(this, this, R.id.fab2, MyApplication.ICON_DELETE)
        fab2.setOnClickListener(View.OnClickListener { deleteFAB() })
        ridFav = Utility.readPref(this, prefToken, "")
        val c = Calendar.getInstance()
        hour = c.get(Calendar.HOUR_OF_DAY)
        minute = c.get(Calendar.MINUTE)
        updateList()
        recyclerView = findViewById(R.id.card_list)
        recyclerView.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = RecyclerView.VERTICAL
        recyclerView.layoutManager = llm
        ca = SingleTextAdapter(ridArr)
        recyclerView.adapter = ca
        ca.setOnItemClickListener(object : SingleTextAdapter.MyClickListener {
            override fun onItemClick(position: Int) {
                pickItem(position)
            }
        })
    }

    private fun updateList() {
        ridArr = ridFav.split(tokenSep.toRegex()).dropLastWhile { it.isEmpty() }
    }

    private fun deleteFAB() {
        if (deleteMode) {
            deleteMode = false
            toolbar.subtitle = modifyModeString
        } else {
            deleteMode = true
            toolbar.subtitle = "Delete mode"
        }
    }

    private fun pickTimeFAB() {
        val mTimePicker: TimePickerDialog
        mTimePicker = TimePickerDialog(globalc, TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
            if (!ridFav.contains(selectedHour.toString() + ":" + String.format("%2s", selectedMinute.toString()).replace(' ', '0'))) {
                ridFav = ridFav + selectedHour.toString() + ":" + String.format("%2s", selectedMinute.toString()).replace(' ', '0') + tokenSep
                Utility.writePref(this, prefToken, ridFav)
                updateList()
                ca = SingleTextAdapter(ridArr)
                recyclerView.adapter = ca
                UtilityPlayListAutoDownload.setAlarm(contextg, ridArr.size - 1, selectedHour, selectedMinute)
            }
        }, hour, minute, false)//no not 24 hour time
        mTimePicker.updateTime(hour, minute)
        mTimePicker.setCanceledOnTouchOutside(true)
        mTimePicker.setCancelable(true)
        mTimePicker.show()
    }

    private fun pickItem(position: Int) {
        gposition = position
        if (deleteMode) {
            ridArr.indices.forEach { UtilityPlayListAutoDownload.cancelAlarm(contextg, it) }
            ridFav = ridFav.replace(ridArr[position] + tokenSep, "")
            Utility.writePref(this, prefToken, ridFav)
            updateList()
            ca = SingleTextAdapter(ridArr)
            recyclerView.adapter = ca
            UtilityPlayListAutoDownload.setAllAlarms(contextg)
        } else {
            val timeArr = ridArr[position].split(":").dropLastWhile { it.isEmpty() }
            currHr = timeArr[0].toIntOrNull() ?: 0
            currMin = timeArr[1].toIntOrNull() ?: 0
            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(globalc, TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                if (!ridFav.contains(selectedHour.toString() + ":" + String.format("%2s", selectedMinute.toString()).replace(' ', '0'))) {
                    ridFav = ridFav.replace(currHr.toString() + ":" + String.format("%2s", currMin).replace(' ', '0'),
                            selectedHour.toString() + ":" + String.format("%2s", selectedMinute.toString()).replace(' ', '0'))
                    UtilityPlayListAutoDownload.setAlarm(contextg, gposition, selectedHour, selectedMinute)
                    Utility.writePref(this, prefToken, ridFav)
                    updateList()
                    ca = SingleTextAdapter(ridArr)
                    recyclerView.adapter = ca
                }
            }, hour, minute, false)
            mTimePicker.setTitle("Select Time")
            mTimePicker.updateTime(currHr, currMin)
            mTimePicker.setCanceledOnTouchOutside(true)
            mTimePicker.setCancelable(true)
            mTimePicker.show()
        }
        updateList()
        ca = SingleTextAdapter(ridArr)
        recyclerView.adapter = ca
    }
} 




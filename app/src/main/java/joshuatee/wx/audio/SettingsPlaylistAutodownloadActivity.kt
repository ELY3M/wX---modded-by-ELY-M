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

package joshuatee.wx.audio

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import java.util.Calendar
import android.os.Bundle
import android.view.View

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectFab
import joshuatee.wx.ui.ObjectRecyclerView
import joshuatee.wx.util.Utility

class SettingsPlaylistAutodownloadActivity : BaseActivity() {

    private var currHr = 0
    private var currMin = 0
    private var ridArr = mutableListOf<String>()
    private var ridFav = ""
    private val tokenSep = "T"
    private val prefToken = "PLAYLIST_AUTODOWNLOAD_TIMES"
    private var deleteMode = false
    private var hour = 0
    private var minute = 0
    private val modifyModeString = "Modify mode"
    private lateinit var recyclerView: ObjectRecyclerView

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_recyclerview_toolbar_with_twofab, null, false)
        toolbar.subtitle = modifyModeString
        ObjectFab(this, this, R.id.fab1, R.drawable.ic_alarm_add_24dp, View.OnClickListener { pickTimeFAB() })
        ObjectFab(this, this, R.id.fab2, MyApplication.ICON_DELETE, View.OnClickListener { deleteFAB() })
        ridFav = Utility.readPref(this, prefToken, "")
        val calendar = Calendar.getInstance()
        hour = calendar.get(Calendar.HOUR_OF_DAY)
        minute = calendar.get(Calendar.MINUTE)
        updateList()
        recyclerView = ObjectRecyclerView(this, this, R.id.cardList, ridArr, ::pickItem)
    }

    private fun updateList() { ridArr = ridFav.split(tokenSep.toRegex()).dropLastWhile { it.isEmpty() }.toMutableList() }

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
        mTimePicker = TimePickerDialog(
                this@SettingsPlaylistAutodownloadActivity,
                TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                    if (!ridFav.contains("$selectedHour:" + String.format("%2s", selectedMinute.toString()).replace(' ', '0'))
                    ) {
                        ridFav = ridFav + selectedHour.toString() + ":" +
                                String.format("%2s", selectedMinute.toString()).replace(' ', '0') +
                                tokenSep
                        Utility.writePref(this, prefToken, ridFav)
                        updateList()
                        recyclerView.refreshList(ridArr)
                        UtilityPlayListAutoDownload.setAlarm(this@SettingsPlaylistAutodownloadActivity, ridArr.lastIndex, selectedHour, selectedMinute)
                    }
                },
                hour,
                minute,
                false
        )//no not 24 hour time
        mTimePicker.updateTime(hour, minute)
        mTimePicker.setCanceledOnTouchOutside(true)
        mTimePicker.setCancelable(true)
        mTimePicker.show()
    }

    private fun pickItem(position: Int) {
        if (deleteMode) {
            ridArr.indices.forEach { UtilityPlayListAutoDownload.cancelAlarm(this@SettingsPlaylistAutodownloadActivity, it) }
            ridFav = ridFav.replace(ridArr[position] + tokenSep, "")
            Utility.writePref(this, prefToken, ridFav)
            updateList()
            recyclerView.refreshList(ridArr)
            UtilityPlayListAutoDownload.setAllAlarms(this@SettingsPlaylistAutodownloadActivity)
        } else {
            val timeArr = ridArr[position].split(":").dropLastWhile { it.isEmpty() }
            currHr = timeArr[0].toIntOrNull() ?: 0
            currMin = timeArr[1].toIntOrNull() ?: 0
            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(
                    this@SettingsPlaylistAutodownloadActivity,
                    TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                        if (!ridFav.contains(
                                        "$selectedHour:" + String.format(
                                                "%2s",
                                                selectedMinute.toString()
                                        ).replace(' ', '0')
                                )
                        ) {
                            ridFav = ridFav.replace(
                                    "$currHr:" + String.format("%2s", currMin).replace(
                                            ' ',
                                            '0'
                                    ),
                                    "$selectedHour:" + String.format(
                                            "%2s",
                                            selectedMinute.toString()
                                    ).replace(' ', '0')
                            )
                            UtilityPlayListAutoDownload.setAlarm(
                                    this@SettingsPlaylistAutodownloadActivity,
                                    position,
                                    selectedHour,
                                    selectedMinute
                            )
                            Utility.writePref(this, prefToken, ridFav)
                            updateList()
                            recyclerView.refreshList(ridArr)
                        }
                    },
                    hour,
                    minute,
                    false
            )
            mTimePicker.setTitle("Select Time")
            mTimePicker.updateTime(currHr, currMin)
            mTimePicker.setCanceledOnTouchOutside(true)
            mTimePicker.setCancelable(true)
            mTimePicker.show()
        }
        updateList()
        recyclerView.refreshList(ridArr)
    }
} 





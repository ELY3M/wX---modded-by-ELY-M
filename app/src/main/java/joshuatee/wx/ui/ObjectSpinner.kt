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

package joshuatee.wx.ui

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences

class ObjectSpinner {

    var arrayAdapter: ArrayAdapter<String>
        private set
    private val spinner1: Spinner
    val list: MutableList<String>

    constructor(activity: Activity, context: Context, spinnerRedId: Int, dataArray: List<String>, initialValue: String) {
        list = dataArray.toMutableList()
        spinner1 = activity.findViewById(spinnerRedId)
        setupSpinner(spinner1, true)
        arrayAdapter = ArrayAdapter(context, R.layout.simple_spinner_item, dataArray)
        arrayAdapter.setDropDownViewResource(MyApplication.spinnerLayout)
        spinner1.adapter = arrayAdapter
        spinner1.setSelection(findPosition(initialValue))
        spinner1.setBackgroundColor(Color.TRANSPARENT)
    }

    constructor(activity: Activity, context: Context, spinnerRedId: Int, dataArray: List<String>) {
        list = dataArray.toMutableList()
        spinner1 = activity.findViewById(spinnerRedId)
        setupSpinner(spinner1, true)
        arrayAdapter = ArrayAdapter(context, R.layout.simple_spinner_item, dataArray)
        arrayAdapter.setDropDownViewResource(MyApplication.spinnerLayout)
        spinner1.adapter = arrayAdapter
        spinner1.setBackgroundColor(Color.TRANSPARENT)
    }

    constructor(activity: Activity, context: Context, spinnerRedId: Int) {
        list = mutableListOf()
        spinner1 = activity.findViewById(spinnerRedId)
        setupSpinner(spinner1, true)
        arrayAdapter = ArrayAdapter(context, R.layout.simple_spinner_item, list)
        arrayAdapter.setDropDownViewResource(MyApplication.spinnerLayout)
        spinner1.adapter = arrayAdapter
        spinner1.setBackgroundColor(Color.TRANSPARENT)
    }

    // dummy used in SPC Meso
    constructor(context: Context) {
        list = mutableListOf()
        spinner1 = Spinner(context)
        spinner1.setBackgroundColor(Color.TRANSPARENT)
        arrayAdapter = ArrayAdapter(context, R.layout.simple_spinner_item)
    }

    fun refreshData(context: Context, dataArray: List<String>) {
        arrayAdapter = ArrayAdapter(context, R.layout.simple_spinner_item, dataArray)
        arrayAdapter.setDropDownViewResource(MyApplication.spinnerLayout)
        spinner1.adapter = arrayAdapter
    }

    private fun findPosition(key: String) = (0 until arrayAdapter.count).firstOrNull { arrayAdapter.getItem(it) != null && arrayAdapter.getItem(it).contains(key) }
            ?: 0

    fun setSelection(idx: Int) {
        spinner1.setSelection(idx)
    }

    fun setSelection(key: String) {
        var index = arrayAdapter.getPosition(key)
        if (index == -1) {
            index = 0
        }
        spinner1.setSelection(index)
    }

    fun setOnItemSelectedListener(l: AdapterView.OnItemSelectedListener) {
        spinner1.onItemSelectedListener = l
    }

    val selectedItemPosition: Int get() = spinner1.selectedItemPosition

    val selectedItem: Any
        get() = spinner1.selectedItem ?: 0

    /*val selectedString: String
        get() {
            return if (spinner1.selectedItem != null) {
                spinner1.selectedItem.toString()
            } else {
                ""
            }
        }

    val selectedItemId: Long get() = spinner1.selectedItemId*/

    fun addAll(tmpList: List<String>) {
        list.clear()
        list.addAll(tmpList)
        arrayAdapter.clear()
        arrayAdapter.addAll(tmpList)
    }

    fun add(value: String) {
        list.add(value)
    }

    fun notifyDataSetChanged() {
        arrayAdapter.notifyDataSetChanged()
    }

    fun clear() {
        arrayAdapter.clear()
    }

    fun size(): Int = arrayAdapter.count

    fun getItemAtPosition(k: Int): Any {
        return spinner1.getItemAtPosition(k) ?: return ""
    }

    operator fun get(idx: Int): String = list[idx]

    operator fun set(idx: Int, value: String) {
        list[idx] = value
    }

    companion object {
        fun setupSpinner(spinner1: Spinner, light: Boolean) {
            var tint = ColorStateList.valueOf(UIPreferences.colorBlack)
            if (light) {
                tint = ColorStateList.valueOf(UIPreferences.colorOffwhiteToolbar)
            }
            if (android.os.Build.VERSION.SDK_INT > 20) {
                spinner1.backgroundTintList = tint
            }
        }
    }
}



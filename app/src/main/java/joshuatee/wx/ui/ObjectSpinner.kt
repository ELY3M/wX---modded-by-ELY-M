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
    private val spinner: Spinner
    val list: MutableList<String>

    constructor(
        activity: Activity,
        context: Context,
        fn: AdapterView.OnItemSelectedListener,
        spinnerRedId: Int,
        dataArray: List<String>,
        initialValue: String
    ) {
        list = dataArray.toMutableList()
        spinner = activity.findViewById(spinnerRedId)
        setupSpinner()
        arrayAdapter = ArrayAdapter(context, R.layout.simple_spinner_item, dataArray)
        arrayAdapter.setDropDownViewResource(MyApplication.spinnerLayout)
        spinner.adapter = arrayAdapter
        spinner.setSelection(findPosition(initialValue))
        spinner.setBackgroundColor(Color.TRANSPARENT)
        setOnItemSelectedListener(fn)
    }

    constructor(
        activity: Activity,
        context: Context,
        fn: AdapterView.OnItemSelectedListener,
        spinnerRedId: Int,
        dataArray: List<String>,
        initialPosition: Int
    ) {
        list = dataArray.toMutableList()
        spinner = activity.findViewById(spinnerRedId)
        setupSpinner()
        arrayAdapter = ArrayAdapter(context, R.layout.simple_spinner_item, dataArray)
        arrayAdapter.setDropDownViewResource(MyApplication.spinnerLayout)
        spinner.adapter = arrayAdapter
        spinner.setSelection(initialPosition)
        spinner.setBackgroundColor(Color.TRANSPARENT)
        setOnItemSelectedListener(fn)
    }

    constructor(
        activity: Activity,
        context: Context,
        fn: AdapterView.OnItemSelectedListener,
        spinnerRedId: Int,
        dataArray: List<String>
    ) {
        list = dataArray.toMutableList()
        spinner = activity.findViewById(spinnerRedId)
        setupSpinner()
        arrayAdapter = ArrayAdapter(context, R.layout.simple_spinner_item, dataArray)
        arrayAdapter.setDropDownViewResource(MyApplication.spinnerLayout)
        spinner.adapter = arrayAdapter
        spinner.setBackgroundColor(Color.TRANSPARENT)
        setOnItemSelectedListener(fn)
    }

    constructor(
        activity: Activity,
        context: Context,
        fn: AdapterView.OnItemSelectedListener,
        spinnerRedId: Int
    ) {
        list = mutableListOf()
        spinner = activity.findViewById(spinnerRedId)
        setupSpinner()
        arrayAdapter = ArrayAdapter(context, R.layout.simple_spinner_item, list)
        arrayAdapter.setDropDownViewResource(MyApplication.spinnerLayout)
        spinner.adapter = arrayAdapter
        spinner.setBackgroundColor(Color.TRANSPARENT)
        setOnItemSelectedListener(fn)
    }

    // dummy used in SPC Meso
    constructor(context: Context) {
        list = mutableListOf()
        spinner = Spinner(context)
        spinner.setBackgroundColor(Color.TRANSPARENT)
        arrayAdapter = ArrayAdapter(context, R.layout.simple_spinner_item)
    }

    fun refreshData(context: Context, dataArray: List<String>) {
        arrayAdapter = ArrayAdapter(context, R.layout.simple_spinner_item, dataArray)
        arrayAdapter.setDropDownViewResource(MyApplication.spinnerLayout)
        spinner.adapter = arrayAdapter
    }

    private fun findPosition(key: String) = (0 until arrayAdapter.count).firstOrNull {
        arrayAdapter.getItem(it) != null && arrayAdapter.getItem(it)!!.contains(key)
    }
        ?: 0

    fun setSelection(idx: Int) {
        spinner.setSelection(idx)
    }

    fun setSelection(key: String) {
        var index = arrayAdapter.getPosition(key)
        if (index == -1) {
            index = 0
        }
        spinner.setSelection(index)
    }

    private fun setOnItemSelectedListener(l: AdapterView.OnItemSelectedListener) {
        spinner.onItemSelectedListener = l
    }

    val selectedItemPosition: Int get() = spinner.selectedItemPosition

    val selectedItem: Any get() = spinner.selectedItem ?: 0

    val lastIndex: Int get() = list.lastIndex

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

    operator fun get(index: Int): String = list[index]

    operator fun set(index: Int, value: String) {
        list[index] = value
    }

    private fun setupSpinner() {
        val tint = ColorStateList.valueOf(UIPreferences.colorOffwhiteToolbar)
        if (android.os.Build.VERSION.SDK_INT > 20) {
            spinner.backgroundTintList = tint
        }
    }
}



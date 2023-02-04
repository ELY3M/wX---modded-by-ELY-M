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

package joshuatee.wx.ui

import android.app.Activity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ObjectRecyclerView(activity: Activity, resourceId: Int, list: MutableList<String>, fn: (Int) -> Unit) : Widget {

    private val recyclerView: RecyclerView = activity.findViewById(resourceId)
    private var adapter = SingleTextAdapterList(list)

    init {
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        adapter.setOnItemClickListener(object : SingleTextAdapterList.MyClickListener {
            override fun onItemClick(position: Int) { fn(position) }
        })
    }

    fun refreshList(list: MutableList<String>) {
        adapter = SingleTextAdapterList(list)
        recyclerView.adapter = adapter
    }

    fun notifyDataSetChanged() {
        adapter.notifyDataSetChanged()
    }

    fun setItem(index: Int, str: String) {
        adapter.setItem(index, str)
    }

    fun getItem(index: Int): String = adapter.getItem(index)

    fun deleteItem(index: Int) {
        adapter.deleteItem(index)
    }

    override fun getView() = recyclerView

    override fun toString(): String = adapter.toString()
}

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

package joshuatee.wx.ui

import android.app.Activity
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ObjectRecyclerView(
    context: Context,
    activity: Activity,
    resid: Int,
    list: MutableList<String>,
    fn: (Int) -> Unit
) {

    var recyclerView: RecyclerView = activity.findViewById(resid)
    var ca: SingleTextAdapterList

    init {
        recyclerView.setHasFixedSize(true)
        val llm = LinearLayoutManager(context)
        llm.orientation = RecyclerView.VERTICAL
        recyclerView.layoutManager = llm
        ca = SingleTextAdapterList(list)
        recyclerView.adapter = ca
        ca.setOnItemClickListener(object : SingleTextAdapterList.MyClickListener {
            override fun onItemClick(position: Int) {
                fn(position)
            }
        })
    }

    fun refreshList(list: MutableList<String>) {
        ca = SingleTextAdapterList(list)
        recyclerView.adapter = ca
    }

    fun notifyDataSetChanged() {
        ca.notifyDataSetChanged()
    }

    fun setItem(index: Int, str: String) {
        ca.setItem(index, str)
    }

    fun getItem(index: Int): String {
        return ca.getItem(index)
    }

    fun deleteItem(index: Int) {
        ca.deleteItem(index)
    }

    override fun toString(): String {
        return ca.toString()
    }
}


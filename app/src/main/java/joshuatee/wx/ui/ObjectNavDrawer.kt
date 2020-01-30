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
import android.widget.AdapterView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import android.widget.ArrayAdapter
import android.widget.ListView

import joshuatee.wx.R

class ObjectNavDrawer(activity: Activity, private var labels: List<String>) {

    val drawerLayout: DrawerLayout = activity.findViewById(R.id.drawer_layout)
    val listView: ListView = activity.findViewById(R.id.left_drawer)
    val actionBarDrawerToggle: ActionBarDrawerToggle
    var tokens = listOf<String>()
    var index: Int = 0

    init {
        listView.adapter = ArrayAdapter(activity, R.layout.drawer_list_item, labels)
        actionBarDrawerToggle = ActionBarDrawerToggle(
            activity,
            drawerLayout,
            R.string.drawer_open,
            R.string.drawer_close
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
    }

    fun updateLists(activity: Activity, items: List<String>, tokens: List<String>) {
        listView.adapter = ArrayAdapter(activity, R.layout.drawer_list_item, items)
        labels = items
        this.tokens = tokens
    }

    fun updateLists(activity: Activity, items: List<String>) {
        listView.adapter = ArrayAdapter(activity, R.layout.drawer_list_item, items)
        labels = items
    }

    constructor(activity: Activity, items: List<String>, tokens: List<String>) : this(
        activity,
        items
    ) {
        this.tokens = tokens
    }

    fun getLabel(position: Int): String = labels[position]

    //fun getToken(position: Int): String = tokens[position]

    //fun getUrl(): String = tokens[index]

    var url: String = ""
        get() = tokens[index]
        //set(newValue) {
        //    card.visibility = newValue
        //}

    fun getUrlCount(): Int = tokens.size

    // TODO convert to getter
    fun getLabel(): String {
        if (index >= labels.size) {
            index = labels.size - 1
        }
        return labels[index]
    }

    /*fun getToken(): String {
        if (index >= tokens.size) {
            index = tokens.size - 1
        }
        return tokens[index]
    }*/

    var token: String = ""
        get() {
            if (index >= tokens.size) {
                index = tokens.size - 1
            }
            return tokens[index]
        }

    fun setListener(fn: () -> Unit) {
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            listView.setItemChecked(position, false)
            drawerLayout.closeDrawer(listView)
            index = position
            fn()
        }
    }
}



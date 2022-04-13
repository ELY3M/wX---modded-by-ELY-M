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
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import joshuatee.wx.R
import joshuatee.wx.util.Utility


class ObjectNavDrawer(activity: Activity, private var labels: List<String>) {

    val drawerLayout: DrawerLayout = activity.findViewById(R.id.drawer_layout)
    val listView: ListView = activity.findViewById(R.id.left_drawer)
    val actionBarDrawerToggle: ActionBarDrawerToggle
    var tokens = listOf<String>()
    var index = 0

    init {
        if (Utility.isThemeAllWhite()) {
            listView.setBackgroundColor(Color.WHITE)
            listView.adapter = object : ArrayAdapter<String>(activity, R.layout.drawer_list_item, labels) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view: View = super.getView(position, convertView, parent)
                    view.findViewById<TextView>(android.R.id.text1).setTextColor(Color.BLACK)
                    return view
                }
            }
        } else {
            listView.adapter = ArrayAdapter(activity, R.layout.drawer_list_item, labels)
        }
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
        if (Utility.isThemeAllWhite()) {
            listView.setBackgroundColor(Color.WHITE)
            listView.adapter = object : ArrayAdapter<String>(activity, R.layout.drawer_list_item, items) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view: View = super.getView(position, convertView, parent)
                    view.findViewById<TextView>(android.R.id.text1).setTextColor(Color.BLACK)
                    return view
                }
            }
        } else {
            listView.adapter = ArrayAdapter(activity, R.layout.drawer_list_item, items)
        }
        labels = items
        this.tokens = tokens
    }

    fun updateLists(activity: Activity, items: List<String>) {
        listView.adapter = ArrayAdapter(activity, R.layout.drawer_list_item, items)
        if (Utility.isThemeAllWhite()) {
            listView.setBackgroundColor(Color.WHITE)
            listView.adapter = object : ArrayAdapter<String>(activity, R.layout.drawer_list_item, items) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view: View = super.getView(position, convertView, parent)
                    view.findViewById<TextView>(android.R.id.text1).setTextColor(Color.BLACK)
                    return view
                }
            }
        } else {
            listView.adapter = ArrayAdapter(activity, R.layout.drawer_list_item, items)
        }
        labels = items
    }

    constructor(activity: Activity, items: List<String>, tokens: List<String>) : this(activity, items) {
        this.tokens = tokens
    }

    constructor(activity: Activity, items: List<String>, tokens: List<String>, fn: () -> Unit) : this(activity, items, tokens) {
        setListener(fn)
    }

    val url: String
        get() = tokens[index]

    fun getUrlCount() = tokens.size

    fun getLabel(position: Int) = labels[position]

    fun getLabel(): String {
        if (index >= labels.size) {
            index = labels.size - 1
        }
        return labels[index]
    }

    val token: String
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

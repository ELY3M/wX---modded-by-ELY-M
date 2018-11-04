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
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import android.util.SparseArray
import android.widget.ExpandableListView
import joshuatee.wx.util.MyExpandableListAdapter

import joshuatee.wx.R
import joshuatee.wx.util.Group

class ObjectNavDrawerCombo(activity: Activity, items: SparseArray<Group>, private val labels: Array<Array<String>>, private val tokens: Array<Array<String>>) {

    val drawerLayout: DrawerLayout = activity.findViewById(R.id.drawer_layout)
    val listView: ExpandableListView = activity.findViewById(R.id.left_drawer)
    val actionBarDrawerToggle: ActionBarDrawerToggle

    init {
        listView.setAdapter(MyExpandableListAdapter(activity, items))
        actionBarDrawerToggle = ActionBarDrawerToggle(activity, drawerLayout, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
    }

    fun getLabel(grp: Int, ch: Int): String = labels[grp][ch]

    fun getToken(grp: Int, ch: Int): String = tokens[grp][ch]
}



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
import android.graphics.Color
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import android.util.SparseArray
import android.widget.ExpandableListView
import joshuatee.wx.util.MyExpandableListAdapter

import joshuatee.wx.R
import joshuatee.wx.util.Group
import joshuatee.wx.util.Utility

class ObjectNavDrawerCombo(
    activity: Activity,
    items: SparseArray<Group>,
    private val labels: Array<Array<String>>,
    private val tokens: Array<Array<String>>,
    context: Context,
    prefPrefix: String
) {

    val drawerLayout: DrawerLayout = activity.findViewById(R.id.drawer_layout)
    val listView: ExpandableListView = activity.findViewById(R.id.left_drawer)
    val actionBarDrawerToggle: ActionBarDrawerToggle
    var imgIdx: Int
    var imgGroupIdx: Int

    init {
        if (Utility.isThemeAllWhite()) {
            listView.setBackgroundColor(Color.WHITE)
        } else if (Utility.isThemeAllBlack()) {
            listView.setBackgroundColor(Color.BLACK)
        } /*else {
            listView.setBackgroundColor(R.attr.colorPrimary)
        }*/
        listView.setAdapter(MyExpandableListAdapter(activity, items))
        actionBarDrawerToggle = ActionBarDrawerToggle(
            activity,
            drawerLayout,
            R.string.drawer_open,
            R.string.drawer_close
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        if (prefPrefix != "") {
            imgIdx = Utility.readPref(context, "${prefPrefix}_IDX", 0)
            imgGroupIdx = Utility.readPref(context, "${prefPrefix}_GROUPIDX", 0)
        } else {
            imgIdx = 0
            imgGroupIdx = 0
        }
    }

    fun getLabel(grp: Int, ch: Int) = labels[grp][ch]

    fun getToken(grp: Int, ch: Int) = tokens[grp][ch]

    fun getUrl() = getToken(imgGroupIdx, imgIdx)

    fun getLabel() = getLabel(imgGroupIdx, imgIdx)

    fun setListener(fn: () -> Unit) {
        listView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            drawerLayout.closeDrawer(listView)
            imgIdx = childPosition
            imgGroupIdx = groupPosition
            fn()
            true
        }
    }
}



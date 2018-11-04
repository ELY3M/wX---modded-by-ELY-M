package joshuatee.wx.util

import android.app.Activity
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.CheckedTextView
import android.widget.TextView

import joshuatee.wx.R

class MyExpandableListAdapter(act: Activity, private val groups: SparseArray<Group>) : BaseExpandableListAdapter() {

    private val inflater = act.layoutInflater

    override fun getChild(groupPosition: Int, childPosition: Int): Any = groups.get(groupPosition).children[childPosition]

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = 0

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertViewF: View?, parent: ViewGroup): View {
        var convertView = convertViewF
        val children = getChild(groupPosition, childPosition) as String
        val text: TextView
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listrow_details, null)
        }
        text = convertView!!.findViewById(R.id.textView1)
        text.text = children
        return convertView
    }

    override fun getChildrenCount(groupPosition: Int): Int = groups.get(groupPosition).children.size

    override fun getGroup(groupPosition: Int): Any = groups.get(groupPosition)

    override fun getGroupCount(): Int = groups.size()

    override fun getGroupId(groupPosition: Int): Long = 0

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertViewF: View?, parent: ViewGroup): View {
        var convertView = convertViewF
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listrow_group, null)
        }
        val group = getGroup(groupPosition) as Group
        (convertView as CheckedTextView).text = group.string
        convertView.isChecked = isExpanded
        return convertView
    }

    override fun hasStableIds(): Boolean = true

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
} 
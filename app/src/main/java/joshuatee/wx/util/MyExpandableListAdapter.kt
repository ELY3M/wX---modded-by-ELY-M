package joshuatee.wx.util

import android.app.Activity
import android.graphics.Color
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.CheckedTextView
import android.widget.LinearLayout
import android.widget.TextView
import joshuatee.wx.R

class MyExpandableListAdapter(act: Activity, private val groups: SparseArray<Group>) : BaseExpandableListAdapter() {

    private val inflater = act.layoutInflater

    override fun getChild(groupPosition: Int, childPosition: Int): Any = groups.get(groupPosition).children[childPosition]

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = 0

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, view: View?, parent: ViewGroup): View {
        var convertView = view
        val children = getChild(groupPosition, childPosition) as String
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listrow_details, null)
        }
        val text: TextView = convertView!!.findViewById(R.id.textView1)
        val linearLayout: LinearLayout = convertView.findViewById(R.id.linearLayout)
//        val spacing: View = convertView.findViewById(R.id.spacing)
        text.text = children
        if (Utility.isThemeAllWhite()) {
//            spacing.setBackgroundColor(Color.WHITE)
            linearLayout.setBackgroundColor(Color.WHITE)
            text.setBackgroundColor(Color.WHITE)
            text.setTextColor(Color.BLACK)
        } else if (Utility.isThemeAllBlack()) {
//            spacing.setBackgroundColor(Color.BLACK)
            linearLayout.setBackgroundColor(Color.BLACK)
            text.setBackgroundColor(Color.BLACK)
            text.setTextColor(Color.WHITE)
        }
        return convertView
    }

    override fun getChildrenCount(groupPosition: Int) = groups.get(groupPosition).children.size

    override fun getGroup(groupPosition: Int): Any = groups.get(groupPosition)

    override fun getGroupCount() = groups.size()

    override fun getGroupId(groupPosition: Int): Long = 0

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, view: View?, parent: ViewGroup): View {
        var convertView = view
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listrow_group, null)
        }
        val group = getGroup(groupPosition) as Group
        (convertView as CheckedTextView).text = group.string
        convertView.isChecked = isExpanded
        if (Utility.isThemeAllWhite()) {
            convertView.setBackgroundColor(Color.WHITE)
            convertView.setTextColor(Color.BLACK)
        } else if (Utility.isThemeAllBlack()) {
            convertView.setBackgroundColor(Color.BLACK)
            convertView.setTextColor(Color.WHITE)
        }
        return convertView
    }

    override fun hasStableIds() = true

    override fun isChildSelectable(groupPosition: Int, childPosition: Int) = true
}

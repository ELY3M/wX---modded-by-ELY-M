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

package joshuatee.wx.settings

import android.content.Context
import joshuatee.wx.R
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import joshuatee.wx.ui.ObjectTextView

class BottomSheetFragment : BottomSheetDialogFragment() {

    lateinit  var linearLayout: LinearLayout
    lateinit  var label: TextView
    lateinit  var edit: TextView
    lateinit  var delete: TextView
    var position = -1
    lateinit var actContext: Context
    lateinit var fnList: List<(Int) -> Unit>
    lateinit var labelList: List<String>
    lateinit var topLabel: String
    private var textViewList = mutableListOf<ObjectTextView>()
    var usedForLocation = false

    private var fragmentView: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = inflater.inflate(R.layout.bottom_sheet_layout, container, false)
        label = fragmentView!!.findViewById(R.id.label)
        linearLayout = fragmentView!!.findViewById(R.id.linearLayout)
        labelList.forEachIndexed { index, it ->
            val item = ObjectTextView(actContext, it)
            textViewList.add(item)
            item.setPadding(60, 30, 0, 30)
            item.gravity = Gravity.CENTER_HORIZONTAL
            item.tv.setOnClickListener { fnList[index](position); dismiss() }
            linearLayout.addView(item.tv)
        }
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (usedForLocation && Location.numLocations == 1) {
            textViewList[1].tv.visibility = View.INVISIBLE
            textViewList[2].tv.visibility = View.INVISIBLE
            textViewList[3].tv.visibility = View.INVISIBLE
        }
        initView()
    }

    private fun initView() {
        label.text = topLabel
    }
}
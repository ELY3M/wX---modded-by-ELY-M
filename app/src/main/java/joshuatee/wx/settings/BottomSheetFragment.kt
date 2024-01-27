/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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
import android.graphics.Color
import joshuatee.wx.R
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import joshuatee.wx.ui.Text
import joshuatee.wx.util.Utility

class BottomSheetFragment(
        private val actContext: Context,
        val position: Int,
        private val topLabel: String,
        private val usedForLocation: Boolean
) : BottomSheetDialogFragment() {

    private lateinit var box: LinearLayout
    private lateinit var label: TextView
    lateinit var functions: List<(Int) -> Unit>
    lateinit var labelList: List<String>
    private var textViewList = mutableListOf<Text>()
    private var fragmentView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = inflater.inflate(R.layout.bottom_sheet_layout, container, false)
        label = fragmentView!!.findViewById(R.id.label)
        when {
            Utility.isThemeAllWhite() -> {
                label.setTextColor(Color.BLACK)
                label.setBackgroundColor(Color.LTGRAY)
            }

            Utility.isThemeAllBlack() -> {
                label.setTextColor(Color.WHITE)
                label.setBackgroundColor(Color.BLACK)
            }

            else -> {
                label.setTextColor(Color.WHITE)
                label.setBackgroundColor(Color.BLACK)
            }
        }
        box = fragmentView!!.findViewById(R.id.linearLayout)
        labelList.forEachIndexed { index, it ->
            val item = Text(actContext, it)
            textViewList.add(item)
            item.setPadding(60, 30, 0, 30)
            item.gravity = Gravity.CENTER_HORIZONTAL
            if (Utility.isThemeAllBlack()) {
                item.color = Color.WHITE
                item.setBackgroundColor(Color.BLACK)
            } else {
                item.color = Color.BLACK
            }
            item.connect {
                functions[index](position)
                dismiss()
            }
            box.addView(item.getView())
        }
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (usedForLocation && Location.numLocations == 1) {
            listOf(1, 2, 3).forEach {
                textViewList[it].visibility = View.INVISIBLE
            }
        }
        initView()
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun initView() {
        label.text = topLabel
    }
}

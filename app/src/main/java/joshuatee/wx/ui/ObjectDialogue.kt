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

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import android.widget.ArrayAdapter

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import android.util.TypedValue
import android.widget.TextView
import android.view.ViewGroup
import android.view.View

class ObjectDialogue {

    companion object {
        private const val checkedItem = -1
    }

    private val alertDialog: AlertDialog.Builder
    private var arrayAdapter: ArrayAdapter<String>

    constructor(context: Context, title: String, list: List<String>) {
        alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle(title)
        //arrayAdapter = ArrayAdapter(context, R.layout.simple_spinner_item, list)
        arrayAdapter = object : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val tv = view.findViewById(android.R.id.text1) as TextView
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
                tv.setTextColor(Color.WHITE)
                return view
            }
        }
        arrayAdapter.setDropDownViewResource(MyApplication.spinnerLayout)
        alertDialog.setNegativeButton(
                "Done"
        ) { dialog, _ -> dialog.dismiss() }
    }

    constructor(context: Context, list: List<String>) {
        alertDialog = AlertDialog.Builder(context)
        //arrayAdapter = ArrayAdapter(context, R.layout.simple_spinner_item, list)
        arrayAdapter = object : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val tv = view.findViewById(android.R.id.text1) as TextView
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
                tv.setTextColor(Color.WHITE)
                return view
            }
        }
        arrayAdapter.setDropDownViewResource(MyApplication.spinnerLayout)
        alertDialog.setNegativeButton(
                "Done"
        ) { dialog, _ -> dialog.dismiss() }
    }

    constructor(context: Context, text: String) {
        arrayAdapter = ArrayAdapter(context, R.layout.simple_spinner_item)
        alertDialog = AlertDialog.Builder(context)
        alertDialog.setMessage(text)
        alertDialog.setCancelable(false)
        alertDialog.setNegativeButton(
                "Done"
        ) { dialog, _ -> dialog.dismiss() }
        val ad = alertDialog.create()
        ad.setCanceledOnTouchOutside(true)
        ad.show()
    }

    fun show() {
        alertDialog.show()
    }

    fun setSingleChoiceItems(l: DialogInterface.OnClickListener) {
        alertDialog.setSingleChoiceItems(arrayAdapter, checkedItem, l)
    }

    fun setNegativeButton(l: DialogInterface.OnClickListener) {
        alertDialog.setNegativeButton("Cancel", l)
    }

    fun setTitle(title: String) {
        alertDialog.setTitle(title)
    }

    fun getItem(idx: Int): String = arrayAdapter.getItem(idx) ?: ""
}



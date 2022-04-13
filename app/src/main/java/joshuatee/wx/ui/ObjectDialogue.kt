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
import joshuatee.wx.util.Utility

class ObjectDialogue {

    companion object {
        private const val checkedItem = -1
    }

    private val alertDialog: AlertDialog.Builder
    private val arrayAdapter: ArrayAdapter<String>

    constructor(context: Context, title: String, list: List<String>) {
        alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle(title)
        arrayAdapter = object : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById(android.R.id.text1) as TextView
                setupTextView(textView)
                return view
            }
        }
        arrayAdapter.setDropDownViewResource(MyApplication.spinnerLayout)
        alertDialog.setNegativeButton("Done") { dialog, _ -> dialog.dismiss() }
    }

    constructor(context: Context, list: List<String>) {
        alertDialog = AlertDialog.Builder(context)
        arrayAdapter = object : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById(android.R.id.text1) as TextView
                setupTextView(textView)
                return view
            }
        }
        arrayAdapter.setDropDownViewResource(MyApplication.spinnerLayout)
        alertDialog.setNegativeButton("Done") { dialog, _ -> dialog.dismiss() }
    }

    fun setupTextView(textView: TextView) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
        if (Utility.isThemeAllWhite()) {
            textView.setTextColor(Color.BLACK)
        } else {
            textView.setTextColor(Color.WHITE)
        }
        textView.setPadding(20,10,20,10)
    }

    constructor(context: Context, text: String) {
        arrayAdapter = ArrayAdapter(context, R.layout.simple_spinner_item)
        alertDialog = AlertDialog.Builder(context)
        alertDialog.setMessage(text)
        alertDialog.setCancelable(false)
        alertDialog.setNegativeButton("Done") { dialog, _ -> dialog.dismiss() }
        val ad = alertDialog.create()
        ad.setCanceledOnTouchOutside(true)
        ad.show()
    }

    fun show() { alertDialog.show() }

    fun setSingleChoiceItems(listener: DialogInterface.OnClickListener) { alertDialog.setSingleChoiceItems(arrayAdapter, checkedItem, listener) }

    fun setNegativeButton(listener: DialogInterface.OnClickListener) { alertDialog.setNegativeButton("Cancel", listener) }

    fun setTitle(title: String) { alertDialog.setTitle(title) }

    fun getItem(index: Int) = arrayAdapter.getItem(index) ?: ""
}

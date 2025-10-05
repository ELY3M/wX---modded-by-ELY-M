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

package joshuatee.wx.ui

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import android.widget.ArrayAdapter
import joshuatee.wx.R
import android.view.ViewGroup
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import joshuatee.wx.settings.UIPreferences

class ObjectDialogue {

    companion object {
        private const val CHECKED_ITEM = -1

        fun generic(
            context: Activity,
            list: List<String>,
            getContent: () -> Unit,
            fn: (Int) -> Unit
        ) {
            ObjectDialogue(context, list) { dialog, which ->
                fn(which)
                getContent()
                dialog.dismiss()
            }.show()
        }
    }

    private val alertDialog: AlertDialog.Builder
    private val arrayAdapter: ArrayAdapter<String>

    constructor(
        context: Context,
        list: List<String>,
        title: String = "",
        listener: DialogInterface.OnClickListener
    ) {
        alertDialog = if (UtilityUI.isThemeMaterial3()) {
            MaterialAlertDialogBuilder(context)
        } else {
            AlertDialog.Builder(context)
        }
        if (title != "") {
            alertDialog.setTitle(title)
        }
        arrayAdapter =
            object : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, list) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val text = Text(view, android.R.id.text1)
                    setupTextView(text)
                    return view
                }
            }
        arrayAdapter.setDropDownViewResource(UIPreferences.spinnerLayout)
        alertDialog.setNegativeButton("Done") { dialog, _ -> dialog.dismiss() }
        connect(listener)
    }

    constructor(context: Context, text: String) {
        arrayAdapter = ArrayAdapter(context, R.layout.simple_spinner_item)
        alertDialog = if (UtilityUI.isThemeMaterial3()) {
            MaterialAlertDialogBuilder(context)
        } else {
            AlertDialog.Builder(context)
        }
        alertDialog.setMessage(text)
        alertDialog.setCancelable(false)
        alertDialog.setNegativeButton("Done") { dialog, _ -> dialog.dismiss() }
        val ad = alertDialog.create()
        ad.setCanceledOnTouchOutside(true)
        ad.show()
        val text = Text(ad, android.R.id.message)
        text.setTextSize(UIPreferences.textSizeNormal)
    }

    private fun setupTextView(textView: Text) {
        textView.setTextSize(UIPreferences.textSizeNormal)
        if (UtilityUI.isThemeAllWhite()) {
            textView.setTextColor(Color.BLACK)
        } else {
            textView.setTextColor(Color.WHITE)
        }
        textView.setPadding(20, 10, 20, 10)
    }

    fun show() {
        alertDialog.show()
    }

    private fun connect(listener: DialogInterface.OnClickListener) {
        alertDialog.setSingleChoiceItems(arrayAdapter, CHECKED_ITEM, listener)
    }

    fun setTitle(title: String) {
        alertDialog.setTitle(title)
    }

    fun getItem(index: Int): String = arrayAdapter.getItem(index) ?: ""
}

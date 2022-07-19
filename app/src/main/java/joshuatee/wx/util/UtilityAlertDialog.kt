/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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
//modded by ELY M. 

package joshuatee.wx.util

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.webkit.WebView
import android.webkit.WebViewClient

import joshuatee.wx.ui.ObjectDialogue
import android.webkit.WebResourceRequest
import android.os.Build
import android.annotation.TargetApi
import kotlin.system.exitProcess

object UtilityAlertDialog {

    fun showHelpTextWeb(help: String, activity: Activity) {
        val alert = AlertDialog.Builder(activity)
        val wv = WebView(activity)
        wv.loadUrl(help)
        wv.webViewClient = object : WebViewClient() {
            @SuppressWarnings("deprecation")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                view.loadUrl(request.url.toString())
                return true
            }
        }
        alert.setView(wv)
        alert.setNegativeButton("Close") { dialog, _ -> dialog.dismiss() }
        alert.show()
    }

    fun showDialogueWithContext(str: String, context: Context) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setMessage(str).setCancelable(false)
            .setPositiveButton("OK") { dialog, _ -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    fun showDialogBox(title: String, icon: Int, message :String, context: Context) {
        val alert = AlertDialog.Builder(context)
        alert.setTitle(title)
        alert.setIcon(icon)
        alert.setMessage(message).setCancelable(true).setPositiveButton("OK") { dialog, _ -> dialog.cancel() }
        alert.create().show()
    }


    fun restart() {
        exitProcess(0)
    }
}

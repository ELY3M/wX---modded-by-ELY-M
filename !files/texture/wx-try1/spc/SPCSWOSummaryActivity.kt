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

package joshuatee.wx.spc

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout

import joshuatee.wx.R
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.objects.ShortcutType
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.util.UtilityShortcut

class SPCSWOSummaryActivity : BaseActivity() {

    private val bmAl = mutableListOf<Bitmap>()
    private lateinit var linearLayout: LinearLayout
    private lateinit var contextg: Context

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.spc_swo_summary, menu)
        UtilityShortcut.hidePinIfNeeded(menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        contextg = this
        linearLayout = findViewById(R.id.ll)
        title = "SPC"
        toolbar.subtitle = "Convective Outlook Summary"
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {
            arrayOf("1", "2", "3", "4-8").forEach { bmAl.addAll(UtilitySPCSWO.getImageURLs(it, false)) }
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            var card: ObjectCardImage
            bmAl.forEach { bitmap ->
                card = ObjectCardImage(contextg, bitmap)
                val day = if (bmAl.indexOf(bitmap) < 3) {
                    (bmAl.indexOf(bitmap) + 1).toString()
                } else {
                    "4-8"
                }
                card.setOnClickListener(View.OnClickListener { ObjectIntent(contextg, SPCSWOActivity::class.java, SPCSWOActivity.NO, arrayOf(day, "")) })
                linearLayout.addView(card.card)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_pin -> UtilityShortcut.createShortcut(this, ShortcutType.SPC_SWO_SUMMARY)
            R.id.action_share -> UtilityShare.shareText(this, "Convective Outlook Summary", "", bmAl)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

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
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.wpc.WPCTextProductsActivity

class SPCFireOutlookActivity : BaseActivity() {

    private val bitmaps = mutableListOf<Bitmap>()
    private lateinit var linearLayout: LinearLayout
    private lateinit var contextg: Context

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.shared_multigraphics, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        contextg = this
        title = getString(UtilitySPCFireOutlook.ACTIVITY_TITLE_INT)
        linearLayout = findViewById(R.id.ll)
        title = "SPC"
        toolbar.subtitle = "Fire Weather Outlook"
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {
            UtilitySPCFireOutlook.PROD_IMG_URL.mapTo(bitmaps) { it.getImage() }
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            var card: ObjectCardImage
            bitmaps.forEach { bitmap ->
                card = ObjectCardImage(contextg, bitmap)
                val prod = UtilitySPCFireOutlook.PROD_TEXT_URL[bitmaps.indexOf(bitmap)]
                card.setOnClickListener(View.OnClickListener { ObjectIntent(contextg, WPCTextProductsActivity::class.java, WPCTextProductsActivity.URL, arrayOf(prod)) })
                linearLayout.addView(card.card)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareText(this, getString(UtilitySPCFireOutlook.ACTIVITY_TITLE_INT), "", bitmaps)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

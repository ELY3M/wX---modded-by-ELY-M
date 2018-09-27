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

package joshuatee.wx.wpc

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
import joshuatee.wx.activitiesmisc.TextScreenActivity
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.ObjectCardImage
import joshuatee.wx.util.UtilityShare

class WPCRainfallForecastActivity : BaseActivity() {

    private val bmAl = mutableListOf<Bitmap>()
    private lateinit var ll: LinearLayout
    private lateinit var contextg: Context

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.shared_multigraphics, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_linear_layout, null, false)
        contextg = this
        title = getString(UtilityWPCRainfallForecast.ACTIVITY_TITLE_INT)
        ll = findViewById(R.id.ll)
        GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {
            UtilityWPCRainfallForecast.PROD_IMG_URL.forEach { bmAl.add(it.getImage()) }
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            var card: ObjectCardImage
            bmAl.forEach {
                card = ObjectCardImage(contextg, it)
                val prodTextUrlLocal = UtilityWPCRainfallForecast.PROD_TEXT_URL[bmAl.indexOf(it)]
                val prodTitleLocal = UtilityWPCRainfallForecast.PROD_TITLE[bmAl.indexOf(it)] + " - " + getString(UtilityWPCRainfallForecast.ACTIVITY_TITLE_INT)
                card.setOnClickListener(View.OnClickListener { ObjectIntent(contextg, TextScreenActivity::class.java, TextScreenActivity.URL, arrayOf(prodTextUrlLocal, prodTitleLocal)) })
                ll.addView(card.card)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareText(this, getString(UtilityWPCRainfallForecast.ACTIVITY_TITLE_INT), "", bmAl)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

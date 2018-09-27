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

package joshuatee.wx.activitiesmisc

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare

class ImageShowActivity : BaseActivity(), OnClickListener {

    // This is a general purpose activity used to view one image.
    // URL and title are passed in via extras
    //
    // Arguments
    // 1: URL
    // 2: Title
    // 3: (optional) string "true" means a whitebg is needed

    companion object {
        const val URL = ""
    }

    private var url = ""
    private var urlArr = listOf<String>()
    private var bitmap = UtilityImg.getBlankBitmap()
    private var shareTitle = ""
    private var needsWhitebg = false
    private lateinit var img: TouchImageView2

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.image_show_activity, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show, null, false)
        img = findViewById(R.id.iv)
        img.setOnClickListener(this)
        val turl = intent.getStringArrayExtra(URL)
        url = turl[0]
        title = turl[1]
        shareTitle = turl[1]
        if (turl.size > 2) {
            needsWhitebg = turl[2] == "true"
        }
        when {
            url.contains("file:") -> {
                urlArr = url.split(":")
                getContentFromStorage()
            }
            url.contains("raw:") -> {
                urlArr = url.split(":")
                loadRawBitmap()
            }
            else -> GetContent().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }

    private fun loadRawBitmap() {
        bitmap = UtilityImg.loadBM(this, R.drawable.radar_legend, false)
        img.setImageBitmap(bitmap)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetContent : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {
            bitmap = url.getImage()
            return "Executed"
        }

        override fun onPostExecute(result: String) {
            if (needsWhitebg) {
                val layers = mutableListOf<Drawable>()
                layers.add(ColorDrawable(Color.WHITE))
                layers.add(BitmapDrawable(resources, bitmap))
                bitmap = UtilityImg.layerDrawableToBitmap(layers)
            }
            img.setImageBitmap(bitmap)
        }
    }

    private fun getContentFromStorage() {
        try {
            val inputStream = openFileInput(urlArr[1])
            val bm = BitmapFactory.decodeStream(inputStream)
            img.setImageBitmap(bm)
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.shareBitmap(this, shareTitle, bitmap)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onClick(v: View) {
        when (v.id) {R.id.iv -> UtilityToolbar.showHide(toolbar)
        }
    }
}

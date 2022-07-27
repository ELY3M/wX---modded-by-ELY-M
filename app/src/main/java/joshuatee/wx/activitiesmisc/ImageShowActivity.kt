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

package joshuatee.wx.activitiesmisc

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.R
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.ui.BaseActivity
import joshuatee.wx.ui.TouchImage
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityShare

class ImageShowActivity : BaseActivity() {

    //
    // This is a general purpose activity used to view one image. URL and title are passed in via extras
    //
    // Arguments
    // 1: URL
    // 2: Title
    // 3: (optional) string "true" means a white background is needed
    //

    companion object { const val URL = "" }

    private var url = ""
    private var urls = listOf<String>()
    private var bitmap = UtilityImg.getBlankBitmap()
    private var shareTitle = ""
    private var needsWhiteBackground = false
    private lateinit var image: TouchImage

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.image_show_activity, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show, R.menu.image_show_activity, false)
        image = TouchImage(this, toolbar, R.id.iv)
        val arguments = intent.getStringArrayExtra(URL)!!
        url = arguments[0]
        setTitle("Image Viewer", arguments[1])
        shareTitle = arguments[1]
        if (arguments.size > 2) {
            needsWhiteBackground = arguments[2] == "true"
        }
        when {
            url.contains("file:") -> {
                urls = url.split(":")
                getContentFromStorage()
            }
            url.contains("raw:") -> {
                urls = url.split(":")
                loadRawBitmap()
            }
            else -> getContent()
        }
    }

    private fun loadRawBitmap() {
        bitmap = UtilityImg.loadBitmap(this, R.drawable.radar_legend, false)
        image.setBitmap(bitmap)
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        FutureVoid(this, { bitmap = url.getImage() }, ::showImage)
    }

    private fun showImage() {
        if (needsWhiteBackground) {
            bitmap = UtilityImg.addColorBackground(this, bitmap, Color.WHITE)
        }
        image.setBitmap(bitmap)
    }

    private fun getContentFromStorage() {
        image.setBitmap(UtilityIO.bitmapFromInternalStorage(this, urls[1]))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.bitmap(this, shareTitle, bitmap)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

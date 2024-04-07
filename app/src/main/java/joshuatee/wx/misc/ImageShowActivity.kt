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

package joshuatee.wx.misc

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.objects.FutureBytes
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
    // 1: URL (can be a local file with something like this "raw:radar_legend" / "app/src/main/res/drawable-nodpi/radar_legend.png" )
    // 2: Title
    // 3: (optional) string "true" means a white background is needed
    //

    companion object {
        const val URL = ""
    }

    private var url = ""
    private var urls = listOf<String>()
    private var shareTitle = ""
    private var needsWhiteBackground = false
    private lateinit var touchImage: TouchImage

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.image_show_activity, menu)
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show, R.menu.image_show_activity, false)
        val arguments = intent.getStringArrayExtra(URL)!!
        url = arguments[0]
        shareTitle = arguments[1]
        if (arguments.size > 2) {
            needsWhiteBackground = arguments[2] == "true"
        }
        setTitle("Image Viewer", shareTitle)
        touchImage = TouchImage(this, toolbar, R.id.iv)
        when {
            // TODO FIXME is "file:" used anymore, think not
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
        val bitmap = UtilityImg.loadBitmap(this, R.drawable.radar_legend, false)
        touchImage.set(bitmap)
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        FutureBytes(url, ::showImage)
    }

    private fun showImage(bitmap2: Bitmap) {
        var bitmap = bitmap2
        if (needsWhiteBackground) {
            bitmap = UtilityImg.addColorBackground(this, bitmap, Color.WHITE)
        }
        touchImage.set(bitmap)
    }

    private fun getContentFromStorage() {
        touchImage.set(UtilityIO.bitmapFromInternalStorage(this, urls[1]))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> UtilityShare.bitmap(this, shareTitle, touchImage)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}

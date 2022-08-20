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

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.objects.FutureBytes
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.ObjectImagesCollection
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.TouchImage
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.util.UtilityShare
import joshuatee.wx.vis.UtilityGoesFullDisk

class ImageCollectionActivity : VideoRecordActivity() {

    //
    // used for OPC, GOES Full Disk, Observations
    //

    companion object { const val TYPE = "" }

    private lateinit var image: TouchImage
    private lateinit var objectNavDrawer: ObjectNavDrawer
    private lateinit var imageCollection: ObjectImagesCollection
    // TODO FIXME use ObjectAnimate if possible
    private var animDrawable = AnimationDrawable()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.imagecollection, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val actionAnimate = menu.findItem(R.id.action_animate)
        actionAnimate.isVisible = false
        if (objectNavDrawer.url.contains("jma") && imageCollection.title == "GOESFD") {
            actionAnimate.isVisible = true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show_navdrawer, R.menu.imagecollection, iconsEvenlySpaced = true, bottomToolbar = false)
        val arguments = intent.getStringArrayExtra(TYPE)!!
        imageCollection = ObjectImagesCollection.map[arguments[0]]!!
        objectNavDrawer = ObjectNavDrawer(this, imageCollection.labels, imageCollection.urls, ::getContent)
        image = TouchImage(this, toolbar, R.id.iv, objectNavDrawer, imageCollection.prefTokenIdx)
        image.connect(objectNavDrawer, ::getContent)
        objectNavDrawer.index = Utility.readPrefInt(this, imageCollection.prefTokenIdx, 0)
        toolbar.setOnClickListener { objectNavDrawer.open() }
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        setTitle(imageCollection.title, objectNavDrawer.getLabel())
        FutureBytes(this, objectNavDrawer.url, ::showImage)
    }

    private fun showImage(bitmap: Bitmap) {
        if (objectNavDrawer.url.contains("large_latestsfc.gif")) {
            image.setMaxZoom(16.0f)
        } else {
            image.setMaxZoom(4.0f)
        }
        image.set(bitmap)
        image.firstRun(imageCollection.prefImagePosition)
        invalidateOptionsMenu()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        objectNavDrawer.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        objectNavDrawer.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (objectNavDrawer.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_animate -> getAnimate()
            R.id.action_share -> if (UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else {
                    UtilityShare.bitmap(this, imageCollection.title, image.bitmap)
                }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onStop() {
        image.imgSavePosnZoom(imageCollection.prefImagePosition)
        super.onStop()
    }

    private fun getAnimate() {
        FutureVoid(this,
            { animDrawable = UtilityGoesFullDisk.getAnimation(this, objectNavDrawer.url) })
            { UtilityImgAnim.startAnimation(animDrawable, image) }
    }
}

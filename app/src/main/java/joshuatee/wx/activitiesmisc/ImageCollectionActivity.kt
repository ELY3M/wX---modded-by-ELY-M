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
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.R
import joshuatee.wx.objects.FutureBytes
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.Route
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.ImagesCollection
import joshuatee.wx.ui.NavDrawer
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

    private lateinit var touchImage: TouchImage
    private lateinit var navDrawer: NavDrawer
    private lateinit var imageCollection: ImagesCollection
    // TODO FIXME use ObjectAnimate if possible
    private var animDrawable = AnimationDrawable()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.imagecollection, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val actionAnimate = menu.findItem(R.id.action_animate)
        actionAnimate.isVisible = false
        if (navDrawer.url.contains("jma") && imageCollection.title == "GOESFD") {
            actionAnimate.isVisible = true
        }
        if (imageCollection.title != "Observations") {
            menu.findItem(R.id.action_rtma).isVisible = false
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_image_show_navdrawer, R.menu.imagecollection, iconsEvenlySpaced = true, bottomToolbar = false)
        val arguments = intent.getStringArrayExtra(TYPE)!!
        val typeOfCollection = arguments[0]
        setupUI(typeOfCollection)
        getContent()
    }

    private fun setupUI(typeOfCollection: String) {
        imageCollection = ImagesCollection.map[typeOfCollection]!!
        navDrawer = NavDrawer(this, imageCollection.labels, imageCollection.urls, ::getContent)
        touchImage = TouchImage(this, toolbar, R.id.iv, navDrawer, imageCollection.prefTokenIdx)
        touchImage.connect(navDrawer, ::getContent)
        navDrawer.index = Utility.readPrefInt(this, imageCollection.prefTokenIdx, 0)
        objectToolbar.connectClick { navDrawer.open() }
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() {
        setTitle(imageCollection.title, navDrawer.getLabel())
        FutureBytes(this, navDrawer.url, ::showImage)
    }

    private fun showImage(bitmap: Bitmap) {
        with (touchImage) {
            if (navDrawer.url.contains("large_latestsfc.gif")) {
                setMaxZoom(16.0f)
            } else {
                setMaxZoom(4.0f)
            }
            set(bitmap)
            firstRun(imageCollection.prefImagePosition)
        }
        invalidateOptionsMenu()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        navDrawer.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        navDrawer.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (navDrawer.onOptionsItemSelected(item)) {
            return true
        }
        when (item.itemId) {
            R.id.action_animate -> getAnimate()
            R.id.action_rtma -> Route.rtma(this)
            R.id.action_share -> if (UIPreferences.recordScreenShare && Build.VERSION.SDK_INT < 33) {
                    checkOverlayPerms()
                } else {
                    UtilityShare.bitmap(this, imageCollection.title, touchImage)
                }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onStop() {
        touchImage.imgSavePosnZoom(imageCollection.prefImagePosition)
        super.onStop()
    }

    private fun getAnimate() {
        FutureVoid(this,
            { animDrawable = UtilityGoesFullDisk.getAnimation(this, navDrawer.url) })
            { UtilityImgAnim.startAnimation(animDrawable, touchImage) }
    }
}

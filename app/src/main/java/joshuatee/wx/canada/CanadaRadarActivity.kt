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

package joshuatee.wx.canada

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.R
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.settings.FavAddActivity
import joshuatee.wx.settings.FavRemoveActivity
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.ui.UtilityToolbar
import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.ObjectImageMap
import joshuatee.wx.util.*
import kotlinx.coroutines.*

class CanadaRadarActivity : VideoRecordActivity(), OnClickListener, OnItemSelectedListener, OnMenuItemClickListener {

    // Canada Radar
    //
    // arg1: radar site
    // arg2: image type

    companion object {
        const val RID: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var animDrawable: AnimationDrawable = AnimationDrawable()
    private var firstTime = true
    private var animRan = false
    private lateinit var img: TouchImageView2
    private var rad = ""
    private var rid1 = ""
    private var mosaicShown = false
    private var mosaicShownId = ""
    private var imageType = "rad"
    private var ridFav = ""
    private var firstRun = false
    private lateinit var imageMap: ObjectImageMap
    private var ridArrLoc = listOf<String>()
    private lateinit var star: MenuItem
    private var url = "https://weather.gc.ca/data/satellite/goes_wcan_visible_100.jpg"
    private var bitmap = UtilityImg.getBlankBitmap()
    private lateinit var sp: ObjectSpinner
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_canada_radar, R.menu.canada_radar, true, true)
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        star = toolbarBottom.menu.findItem(R.id.action_fav)
        img = findViewById(R.id.iv)
        img.setOnClickListener(this)
        val args = intent.getStringArrayExtra(RID)
        rid1 = args[0]
        imageType = args[1]
        if (rid1 == "NAT") {
            rid1 = "CAN"
        }
        url = Utility.readPref(this, "CA_LAST_RID_URL", url)
        if (MyApplication.wxoglRememberLocation) {
            if (MyApplication.wxoglRid != "") {
                rid1 = Utility.readPref(this, "CA_LAST_RID", rid1)
            }
        }
        title = "Canada"
        imageMap = ObjectImageMap(this, this, R.id.map, toolbar, toolbarBottom, listOf<View>(img))
        imageMap.addOnImageMapClickedHandler(object : ImageMap.OnImageMapClickedHandler {
            override fun onImageMapClicked(id: Int, im2: ImageMap) {
                im2.visibility = View.GONE
                ridMapSwitch(UtilityImageMap.maptoCARid(id))
            }

            override fun onBubbleClicked(id: Int) {}
        })
        ridFav = Utility.readPref(this, "RID_CA_FAV", " : : :")
        ridArrLoc = UtilityFavorites.setupFavMenuCA(ridFav, rid1)
        sp = ObjectSpinner(this, this, R.id.spinner1, ridArrLoc)
        sp.setOnItemSelectedListener(this)
    }

    override fun onRestart() {
        ridFav = Utility.readPref(this, "RID_CA_FAV", " : : :")
        ridArrLoc = UtilityFavorites.setupFavMenuCA(ridFav, rid1)
        sp.refreshData(this, ridArrLoc)
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        if (ridFav.contains(":$rid1:")) {
            star.setIcon(MyApplication.STAR_ICON)
        } else {
            star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        }
        withContext(Dispatchers.IO) {
            bitmap = if (imageType == "rad") {
                UtilityCanadaImg.getRadarBitmapOptionsApplied(contextg, rad, "")
            } else {
                url.getImage()
            }
        }
        img.visibility = View.VISIBLE
        img.setImageBitmap(bitmap)
        firstRun = UtilityImg.firstRunSetZoomPosn(firstRun, img, "CA_LAST_RID")
        animRan = false
    }

    private fun getMosaic(sector: String) = GlobalScope.launch(uiDispatcher) {
        withContext(Dispatchers.IO) {
            mosaicShownId = sector
            bitmap = UtilityCanadaImg.getRadarMosaicBitmapOptionsApplied(contextg, sector)
        }
        img.setImageBitmap(bitmap)
        animRan = false
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_animate -> getAnimate("short")
            R.id.action_animate_long -> getAnimate("long")
            R.id.action_animate_stop -> {
                animDrawable.stop()
                getContent()
            }
            R.id.action_local -> {
                mosaicShown = false
                imageType = "rad"
                getContent()
            }
            R.id.action_ir -> {
                mosaicShown = false
                imageType = "ir"
                getContent()
            }
            R.id.action_wv -> {
                mosaicShown = false
                imageType = "wv"
                getContent()
            }
            R.id.action_vis_west -> {
                mosaicShown = false
                imageType = "vis"
                url = "https://weather.gc.ca/data/satellite/goes_wcan_visible_100.jpg"
                getContent()
            }
            R.id.action_vis_east -> {
                mosaicShown = false
                imageType = "vis"
                url = "https://weather.gc.ca/data/satellite/goes_ecan_visible_100.jpg"
                getContent()
            }
            R.id.action_ir_west -> {
                mosaicShown = false
                imageType = "ir"
                url = "https://weather.gc.ca/data/satellite/goes_wcan_1070_100.jpg"
                getContent()
            }
            R.id.action_ir_east -> {
                mosaicShown = false
                imageType = "ir"
                url = "https://weather.gc.ca/data/satellite/goes_ecan_1070_100.jpg"
                getContent()
            }
            R.id.action_can -> {
                mosaicShown = true
                imageType = "rad"
                getMosaic("CAN")
            }
            R.id.action_pac -> {
                mosaicShown = true
                imageType = "rad"
                getMosaic("PAC")
            }
            R.id.action_wrn -> {
                mosaicShown = true
                imageType = "rad"
                getMosaic("WRN")
            }
            R.id.action_ont -> {
                mosaicShown = true
                imageType = "rad"
                getMosaic("ONT")
            }
            R.id.action_que -> {
                mosaicShown = true
                imageType = "rad"
                getMosaic("QUE")
            }
            R.id.action_ern -> {
                mosaicShown = true
                imageType = "rad"
                getMosaic("ERN")
            }
            R.id.action_fav -> toggleFavorite()
            R.id.action_share -> {
                if (android.os.Build.VERSION.SDK_INT > 20 && UIPreferences.recordScreenShare) {
                    if (isStoragePermissionGranted) {
                        if (android.os.Build.VERSION.SDK_INT > 22)
                            checkDrawOverlayPermission()
                        else
                            fireScreenCaptureIntent()
                    }
                } else {
                    if (animRan)
                        UtilityShare.shareAnimGif(this, "", animDrawable)
                    else
                        UtilityShare.shareBitmap(this, "", bitmap)
                }
            }
            R.id.action_ridmap -> imageMap.toggleMap()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun ridMapSwitch(r: String) {
        imageType = "rad"
        rid1 = r
        img.resetZoom()
        ridArrLoc = UtilityFavorites.setupFavMenuCA(ridFav, r)
        sp.refreshData(this, ridArrLoc)
    }

    private fun getAnimate(frameCountStr: String) = GlobalScope.launch(uiDispatcher) {
        withContext(Dispatchers.IO) {
            animDrawable = if (imageType == "vis" || imageType == "wv" || imageType == "ir") {
                UtilityCanadaImg.getGOESAnim(contextg, url)
            } else {
                if (!mosaicShown)
                    UtilityCanadaImg.getRadarAnimOptionsApplied(contextg, rad, frameCountStr)
                else
                    UtilityCanadaImg.getRadarMosaicAnimation(contextg, mosaicShownId, frameCountStr)
            }
        }
        animRan = UtilityImgAnim.startAnimation(animDrawable, img)
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        if (firstTime) {
            UtilityToolbar.fullScreenMode(toolbar, toolbarBottom)
            firstTime = false
        }
        when (pos) {
            1 -> ObjectIntent(contextg, FavAddActivity::class.java, FavAddActivity.TYPE, arrayOf("RIDCA"))
            2 -> ObjectIntent(contextg, FavRemoveActivity::class.java, FavRemoveActivity.TYPE, arrayOf("RIDCA"))
            else -> {
                if (ridArrLoc[pos].length > 3) {
                    rad = UtilityStringExternal.truncate(ridArrLoc[pos], 3)
                    mosaicShown = false
                    img.resetZoom()
                    getContent()
                } else {
                    mosaicShown = true
                    img.resetZoom()
                    if (imageType == "rad") {
                        getMosaic(ridArrLoc[pos])
                    } else {
                        getContent()
                    }
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv -> UtilityToolbar.showHide(toolbar, toolbarBottom)
        }
    }

    private fun toggleFavorite() {
        ridFav = UtilityFavorites.toggleFavoriteString(this, rid1, star, "RID_CA_FAV")
        ridArrLoc = UtilityFavorites.setupFavMenuCA(ridFav, rid1)
        sp.refreshData(this, ridArrLoc)
    }

    override fun onStop() {
        UtilityPreferences.writePrefs(this, listOf("CA_LAST_RID", rid1, "CA_LAST_RID_URL", url))
        UtilityImg.imgSavePosnZoom(this, img, "CA_LAST_RID")
        super.onStop()
    }
}

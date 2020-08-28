/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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
import android.content.DialogInterface
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.Extensions.safeGet
import joshuatee.wx.Extensions.startAnimation

import joshuatee.wx.R
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.ObjectIntent
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.ui.*
import joshuatee.wx.util.*
import kotlinx.coroutines.*

class CanadaRadarActivity : VideoRecordActivity(), OnClickListener, OnMenuItemClickListener {

    // Canada Radar
    //
    // arg1: radar site
    // arg2: image type
    //

    companion object { const val RID = "" }

    private val uiDispatcher = Dispatchers.Main
    private var animDrawable = AnimationDrawable()
    private var firstTime = true
    private var animRan = false
    private lateinit var img: ObjectTouchImageView
    private var radarSite = ""
    private var mosaicShown = false
    private var mosaicShownId = ""
    private var imageType = "rad"
    private lateinit var imageMap: ObjectImageMap
    private var favorites = listOf<String>()
    private lateinit var star: MenuItem
    private var url = "https://weather.gc.ca/data/satellite/goes_wcan_visible_100.jpg"
    private var bitmap = UtilityImg.getBlankBitmap()
    private val prefToken = "RID_CA_FAV"

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.canada_radar_top, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_sector).title = favorites.safeGet(0)
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState, R.layout.activity_canada_radar, R.menu.canada_radar, iconsEvenlySpaced = true, bottomToolbar = true)
        toolbarBottom.setOnMenuItemClickListener(this)
        star = toolbarBottom.menu.findItem(R.id.action_fav)
        img = ObjectTouchImageView(this, this, R.id.iv)
        img.setOnClickListener(this)
        val activityArguments = intent.getStringArrayExtra(RID)
        radarSite = activityArguments!![0]
        imageType = activityArguments[1]
        if (radarSite == "NAT") radarSite = "CAN"
        url = Utility.readPref(this, "CA_LAST_RID_URL", url)
        if (MyApplication.wxoglRememberLocation && MyApplication.wxoglRid != "") radarSite = Utility.readPref(this, "CA_LAST_RID", radarSite)
        title = "Canada"
        imageMap = ObjectImageMap(this, this, R.id.map, toolbar, toolbarBottom, listOf<View>(img.img))
        imageMap.addClickHandler(::ridMapSwitch, UtilityImageMap::mapToCanadaRadarSite)
        getContent()
    }

    override fun onRestart() {
        getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {
        favorites = UtilityFavorites.setupMenu(this@CanadaRadarActivity, MyApplication.caRidFav, radarSite, prefToken)
        invalidateOptionsMenu()
        if (MyApplication.caRidFav.contains(":$radarSite:")) star.setIcon(MyApplication.STAR_ICON) else star.setIcon(MyApplication.STAR_OUTLINE_ICON)
        withContext(Dispatchers.IO) {
            bitmap = if (imageType == "rad") {
                UtilityCanadaImg.getRadarBitmapOptionsApplied(this@CanadaRadarActivity, radarSite, "")
            } else {
                url.getImage()
            }
        }
        img.img.visibility = View.VISIBLE
        img.setBitmap(bitmap)
        img.firstRunSetZoomPosn("CA_LAST_RID")
        animRan = false
    }

    private fun getMosaic(sector: String) = GlobalScope.launch(uiDispatcher) {
        mosaicShownId = sector
        bitmap = withContext(Dispatchers.IO) { UtilityCanadaImg.getRadarMosaicBitmapOptionsApplied(this@CanadaRadarActivity, sector) }
        img.setBitmap(bitmap)
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
            R.id.action_vis_west -> getVisOrIr("vis", "https://weather.gc.ca/data/satellite/goes_wcan_visible_100.jpg")
            R.id.action_vis_east -> getVisOrIr("vis", "https://weather.gc.ca/data/satellite/goes_ecan_visible_100.jpg")
            R.id.action_ir_west -> getVisOrIr("ir", "https://weather.gc.ca/data/satellite/goes_wcan_1070_100.jpg")
            R.id.action_ir_east -> getVisOrIr("ir", "https://weather.gc.ca/data/satellite/goes_ecan_1070_100.jpg")
            R.id.action_can -> getRadarMosaic("CAN")
            R.id.action_pac -> getRadarMosaic("PAC")
            R.id.action_wrn -> getRadarMosaic("WRN")
            R.id.action_ont -> getRadarMosaic("ONT")
            R.id.action_que -> getRadarMosaic("QUE")
            R.id.action_ern -> getRadarMosaic("ERN")
            R.id.action_fav -> toggleFavorite()
            R.id.action_share -> {
                if (UIPreferences.recordScreenShare) {
                    checkOverlayPerms()
                } else {
                    if (animRan)
                        UtilityShare.animGif(this, "", animDrawable)
                    else
                        UtilityShare.bitmap(this, this, "", bitmap)
                }
            }
            R.id.action_ridmap -> imageMap.toggleMap()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun getRadarMosaic(sector: String) {
        this.mosaicShown = true
        this.imageType = "rad"
        img.resetZoom()
        getMosaic(sector)
    }

    private fun getVisOrIr(imageType: String, url: String) {
        this.imageType = imageType
        this.url = url
        getContent()
    }

    private fun ridMapSwitch(radarSite: String) {
        imageType = "rad"
        this.radarSite = radarSite
        img.resetZoom()
        getContent()
    }

    private fun getAnimate(frameCountStr: String) = GlobalScope.launch(uiDispatcher) {
        withContext(Dispatchers.IO) {
            animDrawable = if (imageType == "vis" || imageType == "wv" || imageType == "ir") {
                UtilityCanadaImg.getGoesAnimation(this@CanadaRadarActivity, url)
            } else {
                if (!mosaicShown)
                    UtilityCanadaImg.getRadarAnimOptionsApplied(this@CanadaRadarActivity, radarSite, frameCountStr)
                else
                    UtilityCanadaImg.getRadarMosaicAnimation(this@CanadaRadarActivity, mosaicShownId, frameCountStr)
            }
        }
        animRan = animDrawable.startAnimation(img)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        favorites = UtilityFavorites.setupMenu(this, MyApplication.caRidFav, radarSite, prefToken)
        when (item.itemId) {
            R.id.action_sector -> genericDialog(favorites) {
                if (firstTime) {
                    UtilityToolbar.fullScreenMode(toolbar, toolbarBottom)
                    firstTime = false
                }
                when (it) {
                    1 -> ObjectIntent.favoriteAdd(this@CanadaRadarActivity, arrayOf("RIDCA"))
                    2 -> ObjectIntent.favoriteRemove(this@CanadaRadarActivity, arrayOf("RIDCA"))
                    else -> {
                        if (favorites[it].length > 3) {
                            radarSite = UtilityStringExternal.truncate(favorites[it], 3)
                            mosaicShown = false
                            img.resetZoom()
                            getContent()
                        } else {
                            mosaicShown = true
                            img.resetZoom()
                            if (imageType == "rad") getMosaic(favorites[it]) else getContent()
                        }
                    }
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun genericDialog(list: List<String>, fn: (Int) -> Unit) {
        val objectDialogue = ObjectDialogue(this, list)
        objectDialogue.setNegativeButton(DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(this)
        })
        objectDialogue.setSingleChoiceItems(DialogInterface.OnClickListener { dialog, which ->
            fn(which)
            getContent()
            dialog.dismiss()
        })
        objectDialogue.show()
    }

    override fun onClick(v: View) {
        when (v.id) { R.id.iv -> UtilityToolbar.showHide(toolbar, toolbarBottom) }
    }

    private fun toggleFavorite() {
        UtilityFavorites.toggle(this, radarSite, star, prefToken)
    }

    override fun onStop() {
        UtilityPreferences.write(this, listOf("CA_LAST_RID", radarSite, "CA_LAST_RID_URL", url))
        img.imgSavePosnZoom(this, "CA_LAST_RID")
        super.onStop()
    }
}

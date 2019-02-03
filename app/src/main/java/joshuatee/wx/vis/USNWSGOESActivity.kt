/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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

package joshuatee.wx.vis

import android.annotation.SuppressLint
import android.content.Context
import java.util.Collections
import java.util.Locale

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.radar.VideoRecordActivity
import joshuatee.wx.settings.Location
import joshuatee.wx.ui.*
import joshuatee.wx.util.*
import kotlinx.coroutines.*

class USNWSGOESActivity : VideoRecordActivity(), OnClickListener, OnItemSelectedListener,
    OnMenuItemClickListener {

    // NWS GOES Image viewer
    //
    // Arguments
    // 1: "nws"
    // 2: sector ( specific WFO in lower case or as example "CEUS" )
    // 3: product ( optional, "wv" as example )
    //

    companion object {
        const val RID: String = ""
    }

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private var firstTime = true
    private var animRan = false
    private var animDrawable = AnimationDrawable()
    private var wfoChoosen = false
    private var rid = ""
    private var oldSector = ""
    private var oldSatSector = ""
    private val mesoImg = mutableListOf<String>()
    private val overlayImg = mutableListOf<String>()
    private lateinit var imageMap: ObjectImageMap
    private var bitmap = UtilityImg.getBlankBitmap()
    private var imageTypeNhc = "vis"
    private lateinit var img: TouchImageView2
    private var firstRun2 = false
    private var radsNws = listOf<String>()
    private var sector = ""
    private var satSector = ""
    private var nwsOffice = ""
    private var firstRun = true
    private var actType = ""
    private lateinit var turl: Array<String>
    private lateinit var miMeso: MenuItem
    private lateinit var miRadar: MenuItem
    private lateinit var miWv: MenuItem
    private lateinit var miZoomout: MenuItem
    private var imageLoaded = false
    private lateinit var sp: ObjectSpinner
    private lateinit var contextg: Context

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(
            savedInstanceState, R.layout.activity_usgoes, R.menu.usnwsgoes,
            iconsEvenlySpaced = true,
            bottomToolbar = true
        )
        contextg = this
        toolbarBottom.setOnMenuItemClickListener(this)
        Collections.addAll(
            mesoImg,
            *TextUtils.split(Utility.readPref(this, "GOESVISREG_MESO", ""), ":")
        )
        Collections.addAll(
            overlayImg,
            *TextUtils.split(Utility.readPref(this, "GOESVISREG_OVERLAY", ""), ":")
        )
        val menu = toolbarBottom.menu
        miMeso = menu.findItem(R.id.action_meso)
        miRadar = menu.findItem(R.id.action_radar_overlay)
        miWv = menu.findItem(R.id.action_wv)
        miZoomout = menu.findItem(R.id.action_zoomout)
        radsNws = UtilityGOES.RADS_TOP
        img = findViewById(R.id.iv)
        img.setOnClickListener(this)
        imageMap = ObjectImageMap(this, this, R.id.map, toolbar, toolbarBottom, listOf<View>(img))
        imageMap.addOnImageMapClickedHandler(object : ImageMap.OnImageMapClickedHandler {
            override fun onImageMapClicked(id: Int, im2: ImageMap) {
                im2.visibility = View.GONE
                sector = UtilityImageMap.maptoGOESWFO(id)
                satSector = UtilityGOES.getGOESSatSectorFromNWSOffice(contextg, sector)
                mapSwitch()
            }

            override fun onBubbleClicked(id: Int) {}
        })
        turl = intent.getStringArrayExtra(RID)
        rid = turl[0]
        nwsOffice = turl[1]
        if (nwsOffice == "") {
            nwsOffice = Location.wfo.toLowerCase(Locale.US)
        }
        oldSector = nwsOffice
        oldSatSector = UtilityGOES.getGOESSatSectorFromNWSOffice(contextg, oldSector)
        if (turl.size > 2) {
            actType = turl[2]
        }
        sp = if (rid == "nhc") {
            ObjectSpinner(this, this, this, R.id.spinner1, UtilityGOES.RADS)
        } else {
            ObjectSpinner(this, this, this, R.id.spinner1, radsNws)
        }
        sp.setSelection(findPosition(nwsOffice.toUpperCase(Locale.US)))
    }

    private fun findPosition(key: String) =
        (0 until sp.size()).firstOrNull { sp[it].startsWith("$key:") }
            ?: 0

    override fun onRestart() {
        if (satSector == "jma" || satSector == "eumet")
            getContentNONGOES()
        else
            getContent()
        super.onRestart()
    }

    private fun getContent() = GlobalScope.launch(uiDispatcher) {

        if (wfoChoosen && !satSector.contains("wfo")) satSector += "/wfo"
        // EEPAC:EAST needs to be east/epac not to be confused with west/epac
        if (sector == "eepac") sector = "epac"
        miRadar.isVisible =
            !(sector == "ceus" || sector == "weus" || sector == "eaus" || sector == "hi" || sector == "ak")
        miWv.isVisible = !wfoChoosen
        miZoomout.isVisible =
            !(sector == "ceus" || sector == "eaus" || sector.contains("pac") || sector.contains(
                "atl"
            ))
        toolbar.title = imageTypeNhc
        sector = sector.replace(":", "")

        withContext(Dispatchers.IO) {
            bitmap = UtilityUSImgNWSGOES.getGOESMosaic(
                contextg,
                satSector,
                sector,
                imageTypeNhc,
                mesoImg,
                overlayImg,
                wfoChoosen,
                true
            )
        }

        img.visibility = View.VISIBLE
        img.setImageBitmap(bitmap)
        animRan = false
        if (!firstRun2) {
            img.setZoom(MyApplication.goesVisZoom, MyApplication.goesVisX, MyApplication.goesVisY)
            firstRun2 = true
        }
        miMeso.isVisible = wfoChoosen || sector.length == 2
        imageLoaded = true
    }

    private fun getContentNONGOES() = GlobalScope.launch(uiDispatcher) {

        var imgFormat = ".gif"
        if (satSector == "eumet") imgFormat = ".jpg"
        toolbar.title = imageTypeNhc
        sector = sector.replace(":", "")

        withContext(Dispatchers.IO) {
            bitmap = UtilityUSImgNWSGOES.getGOESMosaicNONGOES(
                contextg,
                satSector,
                sector,
                imageTypeNhc,
                imgFormat,
                mesoImg,
                overlayImg,
                wfoChoosen,
                true
            )
        }

        img.visibility = View.VISIBLE
        img.setImageBitmap(bitmap)
        animRan = false
        if (!firstRun2 && sector == MyApplication.goesVisSector) {
            img.setZoom(MyApplication.goesVisZoom, MyApplication.goesVisX, MyApplication.goesVisY)
            firstRun2 = true
        }
        miMeso.isVisible = wfoChoosen || sector.length == 2
        imageLoaded = true
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
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
                        UtilityShare.shareAnimGif(this, "$sector $imageTypeNhc", animDrawable)
                    else
                        UtilityShare.shareBitmap(this, "$sector $imageTypeNhc", bitmap)
                }
            }
            R.id.action_map -> imageMap.toggleMap()
            R.id.action_a12 -> {
                if (satSector == "jma" || satSector == "eumet")
                    animateRadarNONGOES(12)
                else
                    animateRadar(12)
            }
            R.id.action_a18 -> {
                if (satSector == "jma" || satSector == "eumet")
                    animateRadarNONGOES(18)
                else
                    animateRadar(18)
            }
            R.id.action_a36 -> {
                if (satSector == "jma" || satSector == "eumet")
                    animateRadarNONGOES(36)
                else
                    animateRadar(36)
            }
            R.id.action_a6 -> {
                if (satSector == "jma" || satSector == "eumet")
                    animateRadarNONGOES(6)
                else
                    animateRadar(6)
            }
            R.id.action_a -> {
                if (satSector == "jma" || satSector == "eumet")
                    animateRadarNONGOES(MyApplication.uiAnimIconFrames.toInt())
                else
                    animateRadar(MyApplication.uiAnimIconFrames.toInt())
            }
            R.id.action_vis -> {
                imageTypeNhc = "vis"
                if (satSector == "jma" || satSector == "eumet")
                    getContentNONGOES()
                else
                    getContent()
            }
            R.id.action_avn -> {
                imageTypeNhc = "avn"
                if (satSector == "jma" || satSector == "eumet")
                    getContentNONGOES()
                else
                    getContent()
            }
            R.id.action_ir2 -> {
                imageTypeNhc = "ir2"
                if (satSector == "jma" || satSector == "eumet")
                    getContentNONGOES()
                else
                    getContent()
            }
            R.id.action_ir2f -> {
                imageTypeNhc = "ir2f"
                if (satSector == "jma" || satSector == "eumet")
                    getContentNONGOES()
                else
                    getContent()
            }
            R.id.action_bd -> {
                imageTypeNhc = "bd"
                if (satSector == "jma" || satSector == "eumet")
                    getContentNONGOES()
                else
                    getContent()
            }
            R.id.action_ir4 -> {
                imageTypeNhc = "ir4"
                if (satSector == "jma" || satSector == "eumet")
                    getContentNONGOES()
                else
                    getContent()
            }
            R.id.action_jsl -> {
                imageTypeNhc = "jsl"
                if (satSector == "jma" || satSector == "eumet")
                    getContentNONGOES()
                else
                    getContent()
            }
            R.id.action_rgb -> {
                imageTypeNhc = "rgb"
                if (satSector == "jma" || satSector == "eumet")
                    getContentNONGOES()
                else
                    getContent()
            }
            R.id.action_ft -> {
                imageTypeNhc = "ft"
                if (satSector == "jma" || satSector == "eumet")
                    getContentNONGOES()
                else
                    getContent()
            }
            R.id.action_rb -> {
                imageTypeNhc = "rb"
                if (satSector == "jma" || satSector == "eumet")
                    getContentNONGOES()
                else
                    getContent()
            }
            R.id.action_wv -> {
                imageTypeNhc = "wv"
                if (satSector == "jma" || satSector == "eumet")
                    getContentNONGOES()
                else
                    getContent()
            }
            R.id.action_zoomout -> {
                if (wfoChoosen) {
                    oldSector = sector
                    oldSatSector = satSector
                    sector = UtilityGOES.getGOESSectorFromNWSOffice(contextg, sector)
                    wfoChoosen = false
                    satSector =
                        if (sector == "nw" || sector == "sw" || sector == "wc" || sector == "ak" || sector == "hi")
                            "west"
                        else
                            "east"
                } else if (sector == "eaus" || sector == "weus") {
                    sector = oldSector
                    satSector = oldSatSector
                    wfoChoosen = true
                    if (imageTypeNhc == "wv") imageTypeNhc = "vis"
                } else {
                    sector = if (satSector == "east")
                        "eaus"
                    else
                        "weus"
                }
                img.resetZoom()
                getContent()
            }
            R.id.action_mlsp -> {
                mesoSelected("pmsl2")
                getContent()
            }
            R.id.action_temp -> {
                mesoSelected("temp")
                getContent()
            }
            R.id.action_dew -> {
                mesoSelected("d-temp")
                getContent()
            }
            R.id.action_rh -> {
                mesoSelected("rh")
                getContent()
            }
            R.id.action_spw -> {
                mesoSelected("spw")
                getContent()
            }
            R.id.action_sli -> {
                mesoSelected("sli")
                getContent()
            }
            R.id.action_wind -> {
                mesoSelected("wind")
                getContent()
            }
            R.id.action_plot -> {
                mesoSelected("plot")
                getContent()
            }
            R.id.action_radar_overlay -> {
                mesoSelected("radar")
                getContent()
            }
            R.id.action_clear -> {
                mesoImg.clear()
                getContent()
            }
            R.id.action_fronts -> {
                overlaySelected("FRNT")
                getContent()
            }
            R.id.action_counties -> {
                overlaySelected("CNTY")
                getContent()
            }
            R.id.action_latlon -> {
                overlaySelected("LALO")
                getContent()
            }
            R.id.action_roads -> {
                overlaySelected("ROAD")
                getContent()
            }
            R.id.action_cwa -> {
                overlaySelected("CWA")
                getContent()
            }
            R.id.action_warn -> {
                mesoSelected("warn")
                getContent()
            }
            R.id.action_clear_overlays -> {
                overlayImg.clear()
                getContent()
            }
            R.id.action_east -> {
                sector = "eaus"
                satSector = "east"
                wfoChoosen = false
                sp.setSelection(findPosition("EAUS"))
            }
            R.id.action_west -> {
                sector = "weus"
                satSector = "west"
                wfoChoosen = false
                sp.setSelection(findPosition("WEUS"))
            }
            R.id.action_us -> {
                sector = "ceus"
                satSector = "comp"
                wfoChoosen = false
                sp.setSelection(findPosition("CEUS"))
            }
            R.id.action_na -> {
                sector = "nhem"
                satSector = "comp"
                wfoChoosen = false
                sp.setSelection(findPosition("NHEM"))
            }
            R.id.action_hi -> {
                sector = "hi"
                satSector = "west"
                wfoChoosen = false
                sp.setSelection(findPosition("HI"))
            }
            R.id.action_ak -> {
                sector = "ak"
                satSector = "west"
                wfoChoosen = false
                sp.setSelection(findPosition("AK"))
            }
            R.id.action_nw -> {
                sector = "nw"
                satSector = "west"
                wfoChoosen = false
                sp.setSelection(findPosition("NW"))
            }
            R.id.action_wc -> {
                sector = "wc"
                satSector = "west"
                wfoChoosen = false
                sp.setSelection(findPosition("WC"))
            }
            R.id.action_sw -> {
                sector = "sw"
                satSector = "west"
                wfoChoosen = false
                sp.setSelection(findPosition("SW"))
            }
            R.id.action_np -> {
                sector = "np"
                satSector = "east"
                wfoChoosen = false
                sp.setSelection(findPosition("NP"))
            }
            R.id.action_cp -> {
                sector = "wc"
                satSector = "east"
                wfoChoosen = false
                sp.setSelection(findPosition("CP"))
            }
            R.id.action_sc -> {
                sector = "sc"
                satSector = "east"
                wfoChoosen = false
                sp.setSelection(findPosition("SC"))
            }
            R.id.action_mw -> {
                sector = "mw"
                satSector = "east"
                wfoChoosen = false
                sp.setSelection(findPosition("MW"))
            }
            R.id.action_gl -> {
                sector = "gl"
                satSector = "east"
                wfoChoosen = false
                sp.setSelection(findPosition("GL"))
            }
            R.id.action_ne -> {
                sector = "ne"
                satSector = "east"
                wfoChoosen = false
                sp.setSelection(findPosition("NE"))
            }
            R.id.action_ma -> {
                sector = "ma"
                satSector = "east"
                wfoChoosen = false
                sp.setSelection(findPosition("MA"))
            }
            R.id.action_se -> {
                sector = "se"
                satSector = "east"
                wfoChoosen = false
                sp.setSelection(findPosition("SE"))
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun animateRadar(frameCount: Int) = GlobalScope.launch(uiDispatcher) {
        animDrawable = withContext(Dispatchers.IO) {
            UtilityUSImgNWSGOES.getNWSGOESAnim(
                contextg,
                satSector,
                sector,
                imageTypeNhc,
                frameCount
            )
        }
        animRan = UtilityImgAnim.startAnimation(animDrawable, img)
    }

    private fun animateRadarNONGOES(frameCount: Int) = GlobalScope.launch(uiDispatcher) {
        animDrawable = withContext(Dispatchers.IO) {
            UtilityUSImgNWSGOES.getNWSGOESAnimNONGOES(
                contextg,
                satSector,
                sector,
                imageTypeNhc,
                frameCount
            )
        }
        animRan = UtilityImgAnim.startAnimation(animDrawable, img)
    }

    private fun mesoSelected(meso_s: String) {
        if (mesoImg.contains(meso_s))
            mesoImg.remove(meso_s)
        else
            mesoImg.add(meso_s)
    }

    private fun overlaySelected(meso_s: String) {
        if (overlayImg.contains(meso_s))
            overlayImg.remove(meso_s)
        else
            overlayImg.add(meso_s)
    }

    private fun mapSwitch() {
        if (sector == "hi" || sector == "ak")
            wfoChoosen = false
        else {
            wfoChoosen = true
            imageTypeNhc = "vis"
        }
        img.resetZoom()
        sp.setSelection(findPosition(sector.toUpperCase(Locale.US)))
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {

        val tmpArr: List<String>
        val nwsLocationArr: List<String>

        // FIXME this entire logic needs to be reworked for readability and efficiency

        if (radsNws[pos].contains("JMA") || radsNws[pos].contains("EUMET")) {
            val tmpArr2 = radsNws[pos].split(":")
            sector = tmpArr2[1].toLowerCase(Locale.US)
            satSector = tmpArr2[0].toLowerCase(Locale.US)
            wfoChoosen = false
            getContentNONGOES()
        } else {
            if (firstRun && turl[1] != "") {
                firstRun = false
                when (actType) {
                    "regional_vis" -> {
                        sector = UtilityGOES.getGOESSectorFromNWSOffice(contextg, nwsOffice)
                        wfoChoosen = false
                        if (sector == "hfo") {
                            sector = "hi"
                        }
                        if (sector == "afc" || sector == "afg" || sector == "ajk") {
                            sector = "ak"
                        }
                        satSector =
                            if (sector == "nw" || sector == "sw" || sector == "wc" || sector == "hi" || sector == "ak") {
                                "west"
                            } else {
                                "east"
                            }
                        getContent()
                    }
                    "mosaic" -> {
                        sector = UtilityGOES.getGOESSectorFromNWSOffice(contextg, nwsOffice)
                        wfoChoosen = false
                        if (turl.size > 3) imageTypeNhc = turl[3]
                        if (sector == "hfo") {
                            sector = "hi"
                        }
                        if (sector == "afc" || sector == "afg" || sector == "ajk") {
                            sector = "ak"
                        }
                        satSector =
                            if (sector == "nw" || sector == "sw" || sector == "wc" || sector == "hi" || sector == "ak") {
                                "west"
                            } else {
                                "east"
                            }
                        getContent()
                    }
                    "nws_warn" -> {
                        sector = nwsOffice
                        satSector = UtilityGOES.getGOESSatSectorFromNWSOffice(contextg, sector)
                        wfoChoosen = true
                        mesoSelected("radar")
                        getContent()
                    }
                    "wv" -> {
                        sector = "ceus"
                        satSector = "comp"
                        imageTypeNhc = "wv"
                        getContent()
                    }
                    "tab" -> {
                        imageTypeNhc = "vis"
                        sector = nwsOffice
                        satSector = UtilityGOES.getGOESSatSectorFromSector(sector)
                        wfoChoosen = false
                        getContent()
                    }
                    else -> {
                        sector = nwsOffice
                        nwsLocationArr = Utility.readPref(
                            contextg,
                            "NWS_LOCATION_" + sector.toUpperCase(Locale.US),
                            ""
                        ).split(",")
                        val state = nwsLocationArr[0]
                        if (sector == "hfo") {
                            sector = "hi"
                        }
                        if (sector == "afc" || sector == "afg" || sector == "ajk") {
                            sector = "ak"
                        }
                        satSector =
                            if (state == "AZ" || state == "MT" || state == "ID" || state == "NV" || state == "CA" || state == "OR" || state == "UT" || state == "WA") {
                                "west"
                            } else {
                                "east"
                            }
                        if (sector == "hi" || sector == "ak") {
                            wfoChoosen = false
                            satSector = "west"
                        } else {
                            wfoChoosen = true
                        }
                        getContent()
                    }
                }
            } else {
                wfoChoosen = false
                if (rid == "nhc") {
                    tmpArr = UtilityGOES.RADS[pos].split(":")
                    sector = tmpArr[0].toLowerCase(Locale.US)
                    satSector = tmpArr[1].toLowerCase(Locale.US)
                    img.resetZoom()
                    getContent()
                } else {
                    tmpArr = radsNws[pos].split(":")
                    sector = tmpArr[0].toLowerCase(Locale.US)
                    if (radsNws[pos].contains(",")) {
                        satSector = UtilityGOES.getGOESSatSectorFromNWSOffice(contextg, sector)
                        if (sector == "hi" || sector == "ak") {
                            wfoChoosen = false
                            satSector = "west"
                        } else {
                            wfoChoosen = true
                            imageTypeNhc = "vis"
                        }
                    } else {
                        satSector = tmpArr[1].toLowerCase(Locale.US)
                    }
                    img.resetZoom()
                    getContent()
                }
            }
        }

        if (firstTime) {
            UtilityToolbar.fullScreenMode(toolbar, toolbarBottom)
            firstTime = false
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv -> UtilityToolbar.showHide(toolbar, toolbarBottom)
        }
    }

    override fun onStop() {
        if (imageLoaded) {
            Utility.writePref(this, "GOESVISREG_MESO", TextUtils.join(":", mesoImg))
            Utility.writePref(this, "GOESVISREG_OVERLAY", TextUtils.join(":", overlayImg))
            UtilityImg.imgSavePosnZoom(this, img, "GOESVIS")
            MyApplication.goesVisSector = sector
            Utility.writePref(this, "GOESVIS_SECTOR", sector)
        }
        super.onStop()
    }
}


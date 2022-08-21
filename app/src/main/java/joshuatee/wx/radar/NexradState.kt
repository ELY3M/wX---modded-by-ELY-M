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

package joshuatee.wx.radar

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import joshuatee.wx.MyApplication
import joshuatee.wx.objects.PolygonWarningType
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.Utility

class NexradState(activity: Activity, val numberOfPanes: Int, relativeLayoutResIdList: List<Int>, widthDivider: Int, heightDivider: Int) {

    val wxglRenders = mutableListOf<WXGLRender>()
    val wxglSurfaceViews = mutableListOf<WXGLSurfaceView>()
    var wxglTextObjects = mutableListOf<WXGLTextObject>()
    val relativeLayouts = mutableListOf<RelativeLayout>()
    var oldRadarSites = Array(numberOfPanes) { "" }
    var tilt = "0"
    var latD = 0.0
    var lonD = 0.0
    var curRadar = 0
    private val landScape = UtilityUI.isLandScape(activity)
    // single pane only
    var radarSitesForFavorites = listOf<String>()
    var oldProd = ""

    init {
        repeat(numberOfPanes) {
            wxglSurfaceViews.add(WXGLSurfaceView(activity, widthDivider, numberOfPanes, heightDivider))
            wxglRenders.add(WXGLRender(activity, it))
        }
        wxglRenders.indices.forEach {
            relativeLayouts.add(activity.findViewById(relativeLayoutResIdList[it]))
            relativeLayouts.last().addView(wxglSurfaceViews[it])
            //
            // setup special sizing for multipane
            //
            if (numberOfPanes == 4) {
                val params = relativeLayouts[it].layoutParams
                if (UIPreferences.radarImmersiveMode || UIPreferences.radarToolbarTransparent)
                    params.height = MyApplication.dm.heightPixels / 2 + UtilityUI.statusBarHeight(activity)
                else
                    params.height = MyApplication.dm.heightPixels /
                            2 - UIPreferences.actionBarHeight /
                            2 - UtilityUI.statusBarHeight(activity) / 2 -
                            (UtilityUI.navigationBarHeight(activity) / 2.0).toInt()
                if (UIPreferences.radarToolbarTransparent && !UIPreferences.radarImmersiveMode && numberOfPanes == 4) {
                    params.height = MyApplication.dm.heightPixels / 2 - UtilityUI.statusBarHeight(activity) / 2
                }
                params.width = MyApplication.dm.widthPixels / 2
            } else if (numberOfPanes == 2) {
                val params = relativeLayouts[it].layoutParams
                if (!landScape) {
                    params.height = MyApplication.dm.heightPixels / 2 -
                            (UIPreferences.actionBarHeight / 2) - UtilityUI.statusBarHeight(activity) / 2 -
                            (UtilityUI.navigationBarHeight(activity) / 2.0).toInt()
                    params.width = MyApplication.dm.widthPixels
                } else {
                    params.width = MyApplication.dm.widthPixels
                    params.height = MyApplication.dm.heightPixels
                }
            }
        }
        wxglSurfaceViews.indices.forEach {
            wxglTextObjects.add(WXGLTextObject(activity, relativeLayouts[it], wxglSurfaceViews[it], wxglRenders[it], numberOfPanes, it))
            wxglSurfaceViews[it].wxglTextObjects = wxglTextObjects
            wxglTextObjects.last().initializeLabels(activity)
        }
    }

    fun setupMultiPaneObjects() {
        repeat(numberOfPanes) {
            wxglSurfaceViews[it].idxInt = it
            wxglRenders[it].radarStatusStr = (it + 1).toString()
            wxglRenders[it].indexString = (it + 1).toString()
        }
    }

    fun readPreferences(arguments: Array<String>?, nexradArguments: NexradArguments) {
        product = "N0Q"
        radarSite = if (arguments == null) {
            joshuatee.wx.settings.Location.rid
        } else {
            arguments[0]
        }
        // hack, in rare cases a user will save a location that doesn't pick up RID
        if (radarSite == "") {
            radarSite = "TLX"
        }
        if (arguments != null && arguments.size > 2) {
            product = arguments[2]
            if (product == "N0R") {
                product = "N0Q"
            }
        }
        if (RadarPreferences.wxoglRememberLocation && !nexradArguments.archiveMode && !nexradArguments.fixedSite) {
            surface.scaleFactor = RadarPreferences.wxoglZoom
            if (RadarPreferences.wxoglRid != "") {
                radarSite = RadarPreferences.wxoglRid
            }
            product = RadarPreferences.wxoglProd
            render.setViewInitial(RadarPreferences.wxoglZoom, RadarPreferences.wxoglX, RadarPreferences.wxoglY)
        } else {
            surface.scaleFactor = RadarPreferences.wxoglSize / 10.0f
            render.setViewInitial(RadarPreferences.wxoglSize / 10.0f, 0.0f, 0.0f)
        }
    }

    fun readPreferencesMultipane(context: Context, arguments: Array<String>?, useSinglePanePref: Boolean, prefPrefix: String) {
        //
        // radar site
        //
        wxglRenders.forEachIndexed { index, wxglRender ->
            var initialRadarSite = arguments!![0]
            if (arguments[0] == "") {
                initialRadarSite = joshuatee.wx.settings.Location.rid
            }
            if (!useSinglePanePref) {
                wxglRender.rid = Utility.readPref(context, prefPrefix + "_RID" + (index + 1).toString(), initialRadarSite)
            } else {
                wxglRender.rid = Utility.readPref(context, prefPrefix + "_RID", initialRadarSite)
            }
        }
        if (RadarPreferences.dualpaneshareposn) {
            (1 until numberOfPanes).forEach {
                wxglRenders[it].rid = wxglRenders[0].rid
            }
        }
        //
        // product
        //
        val defaultProducts = listOf("N0Q", "N0U", "N0C", "DVL")
        wxglRenders.indices.forEach {
            oldRadarSites[it] = ""
            wxglRenders[it].product = Utility.readPref(context, prefPrefix + "_PROD" + (it + 1).toString(), defaultProducts[it])
        }
        //
        // restore session (x, y, zoom)
        //
        var zoomPref = "_ZOOM1"
        var xPref = "_X1"
        var yPref = "_Y1"
        if (useSinglePanePref) {
            zoomPref = "_ZOOM"
            xPref = "_X"
            yPref = "_Y"
        }
        wxglSurfaceViews[0].scaleFactor = Utility.readPrefFloat(context, prefPrefix + zoomPref, RadarPreferences.wxoglSize.toFloat() / 10.0f)
        wxglRenders[0].setViewInitial(
                Utility.readPrefFloat(context, prefPrefix + zoomPref, RadarPreferences.wxoglSize.toFloat() / 10.0f),
                Utility.readPrefFloat(context, prefPrefix + xPref, 0.0f),
                Utility.readPrefFloat(context, prefPrefix + yPref, 0.0f)
        )
        if (RadarPreferences.dualpaneshareposn) {
            (1 until numberOfPanes).forEach {
                wxglSurfaceViews[it].scaleFactor = wxglSurfaceViews[0].scaleFactor
                wxglRenders[it].setViewInitial(
                        Utility.readPrefFloat(context, prefPrefix + zoomPref, RadarPreferences.wxoglSize.toFloat() / 10.0f),
                        wxglRenders[0].x, wxglRenders[0].y
                )
            }
        } else {
            (1 until numberOfPanes).forEach {
                wxglSurfaceViews[it].scaleFactor = Utility.readPrefFloat(context, prefPrefix + "_ZOOM" + (it + 1).toString(), RadarPreferences.wxoglSize.toFloat() / 10.0f)
                wxglRenders[it].setViewInitial(
                        Utility.readPrefFloat(context, prefPrefix + "_ZOOM" + (it + 1).toString(), RadarPreferences.wxoglSize.toFloat() / 10.0f),
                        Utility.readPrefFloat(context, prefPrefix + "_X" + (it + 1).toString(), 0.0f),
                        Utility.readPrefFloat(context, prefPrefix + "_Y" + (it + 1).toString(), 0.0f)
                )
            }
        }
    }

    fun writePreferences(context: Context, nexradArguments: NexradArguments) {
        if (!nexradArguments.archiveMode && !nexradArguments.fixedSite) {
            savePrefs(context, render)
        }
    }

    fun writePreferencesMultipane(context: Context, doNotSavePref: Boolean, prefPrefix: String) {
        if (!doNotSavePref) {
            wxglRenders.forEachIndexed { index, wxglRender ->
                savePrefs(context, prefPrefix, index + 1, wxglRender)
            }
        } else {
            wxglRenders.forEachIndexed { index, wxglRender ->
                saveProductPrefs(context, prefPrefix, index + 1, wxglRender)
            }
        }
    }

    fun writePreferencesMultipaneOnStop(context: Context, doNotSavePref: Boolean, prefPrefix: String) {
        if (!doNotSavePref) {
            wxglRenders.forEachIndexed { index, wxglRender ->
                savePrefs(context, prefPrefix, index + 1, wxglRender)
            }
        } else {
            // TODO FIXME why is this here?
            wxglRenders.forEachIndexed { index, wxglRender ->
                saveProductPrefs(context, prefPrefix, index + 1, wxglRender)
            }
        }
    }

    private fun savePrefs(context: Context, wxglRender: WXGLRender) {
        val prefPrefix = "WXOGL"
        Utility.writePref(context, prefPrefix + "_RID", wxglRender.rid)
        Utility.writePref(context, prefPrefix + "_PROD", wxglRender.product)
        Utility.writePrefFloat(context, prefPrefix + "_ZOOM", wxglRender.zoom)
        Utility.writePrefFloat(context, prefPrefix + "_X", wxglRender.x)
        Utility.writePrefFloat(context, prefPrefix + "_Y", wxglRender.y)
        RadarPreferences.wxoglRid = wxglRender.rid
        RadarPreferences.wxoglProd = wxglRender.product
        RadarPreferences.wxoglZoom = wxglRender.zoom
        RadarPreferences.wxoglX = wxglRender.x
        RadarPreferences.wxoglY = wxglRender.y
    }

    private fun savePrefs(context: Context, prefPrefix: String, idx: Int, wxglRender: WXGLRender) {
        Utility.writePref(context, prefPrefix + "_RID" + idx.toString(), wxglRender.rid)
        Utility.writePref(context, prefPrefix + "_PROD" + idx.toString(), wxglRender.product)
        Utility.writePrefFloat(context, prefPrefix + "_ZOOM" + idx.toString(), wxglRender.zoom)
        Utility.writePrefFloat(context, prefPrefix + "_X" + idx.toString(), wxglRender.x)
        Utility.writePrefFloat(context, prefPrefix + "_Y" + idx.toString(), wxglRender.y)
    }

    private fun saveProductPrefs(context: Context, prefPrefix: String, idx: Int, wxglRender: WXGLRender) {
        Utility.writePref(context, prefPrefix + "_PROD" + idx.toString(), wxglRender.product)
    }

    fun setToolbarTitleMultipane(updateWithWarnings: Boolean): String {
        // TODO FIXME refactor this mess
        var title = ""
        if (numberOfPanes == 4) {
            title = if (RadarPreferences.dualpaneshareposn) {
                (curRadar + 1).toString() + ":" + wxglRenders[0].rid + "(" + wxglRenders[0].product + "," + wxglRenders[1].product + "," + wxglRenders[2].product + "," + wxglRenders[3].product + ")"
            } else {
                (curRadar + 1).toString() + ": " + wxglRenders[0].rid + "(" + wxglRenders[0].product + ") " + wxglRenders[1].rid + "(" + wxglRenders[1].product + ") " + wxglRenders[2].rid + "(" + wxglRenders[2].product + ") " + wxglRenders[3].rid + "(" + wxglRenders[3].product + ")"
            }
        } else if (numberOfPanes == 2) {
            title = if (RadarPreferences.dualpaneshareposn) {
                (curRadar + 1).toString() + ":" + wxglRenders[0].rid + "(" + wxglRenders[0].product + "," + wxglRenders[1].product + ")"
            } else {
                (curRadar + 1).toString() + ": " + wxglRenders[0].rid + "(" + wxglRenders[0].product + ") " + wxglRenders[1].rid + "(" + wxglRenders[1].product + ") "
            }
        }
        if (updateWithWarnings) {
            title += " (" +
                    WXGLPolygonWarnings.getCount(PolygonWarningType.ThunderstormWarning).toString() + "," +
                    WXGLPolygonWarnings.getCount(PolygonWarningType.TornadoWarning).toString() + "," +
                    WXGLPolygonWarnings.getCount(PolygonWarningType.FlashFloodWarning).toString() + ")"
        }
        return title
    }

    fun setAllPanesTo(newRadarSite: String) {
        wxglRenders.forEach {
            it.rid = newRadarSite
        }
    }

    // single pane
    fun adjustPaneTo(newRadarSite: String) {
        radarSite = newRadarSite
        surface.scaleFactor = RadarPreferences.wxoglSize / 10.0f
        render.setViewInitial(RadarPreferences.wxoglSize / 10.0f, 0.0f, 0.0f)
    }

    // multi pane
    fun adjustAllPanesTo(newRadarSite: String) {
        wxglRenders.indices.forEach {
            wxglRenders[it].rid = newRadarSite
            wxglSurfaceViews[it].scaleFactor = RadarPreferences.wxoglSize / 10.0f
            wxglRenders[it].setViewInitial(RadarPreferences.wxoglSize / 10.0f, 0.0f, 0.0f)
        }
    }

    fun adjustOnePaneTo(index: Int, newRadarSite: String) {
        wxglRenders[index].rid = newRadarSite
        wxglSurfaceViews[index].scaleFactor = RadarPreferences.wxoglSize / 10.0f
        wxglRenders[index].setViewInitial(RadarPreferences.wxoglSize / 10.0f, 0.0f, 0.0f)
    }

    // TODO FIXME constructLocationDot should not take String
    fun updateLocationDotsMultipane() {
        wxglRenders.indices.forEach {
            wxglRenders[it].constructLocationDot(latD, lonD, false)
            draw(it)
            if (RadarPreferences.wxoglCenterOnLocation) {
                wxglSurfaceViews[it].resetView()
                UtilityWXGLTextObject.hideLabels(it, wxglTextObjects)
                UtilityWXGLTextObject.showLabels(it, wxglTextObjects)
            }
        }
    }

    fun updateLocationDots(nexradArguments: NexradArguments) {
        render.constructLocationDot(latD, lonD, nexradArguments.archiveMode)
        draw()
        if (RadarPreferences.wxoglCenterOnLocation) {
            UtilityWXGLTextObject.hideLabels(1, wxglTextObjects)
            UtilityWXGLTextObject.showLabels(1, wxglTextObjects)
        }
    }

    fun changeTilt(tiltStr: String) {
        tilt = tiltStr
        product = product.replace("N[0-3]".toRegex(), "N$tilt")
        if (product.startsWith("TR")) {
            product = product.replace("TR[0-3]".toRegex(), "TR$tilt")
        }
        if (product.startsWith("TZ")) {
            product = product.replace("TZ[0-3]".toRegex(), "TZ$tilt")
        }
        if (product.startsWith("TV")) {
            product = product.replace("TV[0-3]".toRegex(), "TV$tilt")
        }
    }

    fun draw() {
        surface.requestRender()
    }

    fun draw(index: Int) {
        wxglSurfaceViews[index].requestRender()
    }

    fun drawAll() {
        wxglSurfaceViews.forEach {
            it.requestRender()
        }
    }

    fun onPause() {
        wxglSurfaceViews.forEach {
            it.onPause()
        }
    }

    fun onResume() {
        wxglSurfaceViews.forEach {
            it.onResume()
        }
    }

    fun showViews() {
        wxglSurfaceViews.indices.forEach {
            wxglSurfaceViews[it].visibility = View.VISIBLE
            relativeLayouts[it].visibility = View.VISIBLE
        }
    }

    fun adjustForTdwrSinglePane() {
        val ridIsTdwr = WXGLNexrad.isRidTdwr(radarSite)
        if ((product.matches(Regex("N[0-3]Q")) || product == "L2REF") && ridIsTdwr) {
            if (tilt == "3") {
                tilt = "2"
            }
            product = "TZL"
        }
        if ((product == "TZL" || product.startsWith("TZ")) && !ridIsTdwr) {
            product = "N" + tilt + "Q"
        }
        if ((product.matches(Regex("N[0-3]U")) || product == "L2VEL") && ridIsTdwr) {
            if (tilt == "3") {
                tilt = "2"
            }
            product = "TV${tilt}"
        }
        if (product.startsWith("TV") && !ridIsTdwr) {
            product = "N" + tilt + "U"
        }
    }

    fun adjustForTdwrMultiPane(z: Int) {
        if ((wxglRenders[z].product.matches(Regex("N[0-3]Q")) || wxglRenders[z].product == "L2REF") && WXGLNexrad.isRidTdwr(wxglRenders[z].rid)) {
            wxglRenders[z].product = "TZL"
        }
        if (wxglRenders[z].product == "TZL" && !WXGLNexrad.isRidTdwr(wxglRenders[z].rid)) {
            wxglRenders[z].product = "N0Q"
        }
        if ((wxglRenders[z].product.matches(Regex("N[0-3]U")) || wxglRenders[z].product == "L2VEL") && WXGLNexrad.isRidTdwr(wxglRenders[z].rid)) {
            wxglRenders[z].product = "TV0"
        }
        if (wxglRenders[z].product == "TV0" && !WXGLNexrad.isRidTdwr(wxglRenders[z].rid)) {
            wxglRenders[z].product = "N0U"
        }
    }

    var product: String
        get() = wxglRenders[curRadar].product
        set(prod) {
            wxglRenders[curRadar].product = prod
        }

    var radarSite: String
        get() = wxglRenders[curRadar].rid
        set(rid) {
            wxglRenders[curRadar].rid = rid
        }

    val surface: WXGLSurfaceView
        get() = wxglSurfaceViews[curRadar]

    val render: WXGLRender
        get() = wxglRenders[curRadar]
}

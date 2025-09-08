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

package joshuatee.wx.radar

import android.content.Context
import android.view.View
import joshuatee.wx.objects.LatLon
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.util.Utility

class NexradStatePane(
    val activity: VideoRecordActivity,
    numberOfPanes: Int,
    relativeLayoutResIdList: List<Int>,
    widthDivider: Int,
    heightDivider: Int
) : NexradState(numberOfPanes) {

    var oldRadarSites = Array(numberOfPanes) { "" }
    var tilt = "0"
    var latD = 0.0
    var lonD = 0.0

    // single pane only
    var radarSitesForFavorites = listOf<String>()
    var oldProd = ""

    // multipane only, single pane is "WXOGL" hard coded in methods
    private var prefPrefix = "WXOGL_DUALPANE"

    init {
        repeat(numberOfPanes) {
            wxglSurfaceViews.add(NexradRenderSurfaceView(activity, widthDivider, heightDivider))
            wxglRenders.add(NexradRender(activity, it))
        }
        wxglRenders.indices.forEach {
            relativeLayouts.add(activity.findViewById(relativeLayoutResIdList[it]))
            relativeLayouts.last().addView(wxglSurfaceViews[it])
        }
        wxglSurfaceViews.indices.forEach {
            wxglTextObjects.add(
                NexradRenderTextObject(
                    activity,
                    relativeLayouts[it],
                    wxglSurfaceViews[it],
                    wxglRenders[it].state,
                    numberOfPanes,
                    it
                )
            )
            wxglSurfaceViews[it].textObjects = wxglTextObjects
            wxglTextObjects.last().initializeLabels(activity)
        }
        val latLonListAsDoubles = UtilityLocation.getGps(activity)
        latD = latLonListAsDoubles[0]
        lonD = latLonListAsDoubles[1]
    }

    fun initGlView(changeListener: NexradRenderSurfaceView.OnProgressChangeListener) {
        wxglRenders.indices.forEach {
            NexradDraw.initGlView(it, this, activity, changeListener)
        }
    }

    fun setupMultiPaneObjects(nexradArguments: NexradArgumentsMultipane) {
        if (nexradArguments.numberOfPanes == 4) {
            prefPrefix = "WXOGL_QUADPANE"
        }
        if (nexradArguments.useSinglePanePref) {
            prefPrefix = "WXOGL"
        }
        repeat(numberOfPanes) {
            wxglSurfaceViews[it].idxInt = it
            wxglRenders[it].state.timeStampId = (it + 1).toString()
            wxglRenders[it].state.indexString = (it + 1).toString()
        }
    }

    fun readPreferences(nexradArguments: NexradArgumentsSinglePane) {
        product = "N0Q"
        radarSite = if (nexradArguments.arguments == null) {
            joshuatee.wx.settings.Location.radarSite
        } else {
            nexradArguments.arguments!![0]
        }
        // hack, in rare cases a user will save a location that doesn't pick up RID
        if (radarSite == "") {
            radarSite = "TLX"
        }
        if (nexradArguments.arguments != null && nexradArguments.arguments!!.size > 2) {
            product = nexradArguments.arguments!![2]
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
            render.setViewInitial(
                RadarPreferences.wxoglZoom,
                RadarPreferences.wxoglX,
                RadarPreferences.wxoglY
            )
        } else {
            surface.scaleFactor = RadarPreferences.wxoglSize / 10.0f
            render.setViewInitial(RadarPreferences.wxoglSize / 10.0f, 0.0f, 0.0f)
        }
    }

    fun readPreferencesMultipane(context: Context, nexradArguments: NexradArgumentsMultipane) {
        //
        // radar site
        //
        wxglRenders.forEachIndexed { index, wxglRender ->
            var initialRadarSite = nexradArguments.arguments!![0]
            if (nexradArguments.arguments!![0] == "") {
                initialRadarSite = joshuatee.wx.settings.Location.radarSite
            }
            if (!nexradArguments.useSinglePanePref) {
                wxglRender.state.rid = Utility.readPref(
                    context,
                    prefPrefix + "_RID" + (index + 1).toString(),
                    initialRadarSite
                )
            } else {
                wxglRender.state.rid =
                    Utility.readPref(context, prefPrefix + "_RID", initialRadarSite)
            }
            // jump from single pane which is from an alert
            if (nexradArguments.doNotSavePref && !nexradArguments.useSinglePanePref) {
                wxglRender.state.rid = initialRadarSite
            }
        }
        if (RadarPreferences.dualpaneshareposn) {
            (1 until numberOfPanes).forEach {
                wxglRenders[it].state.rid = wxglRenders[0].state.rid
            }
        }
        //
        // product
        //
        val defaultProducts = listOf("N0Q", "N0U", "N0C", "DVL")
        wxglRenders.indices.forEach {
            oldRadarSites[it] = ""
            wxglRenders[it].state.product = Utility.readPref(
                context,
                prefPrefix + "_PROD" + (it + 1).toString(),
                defaultProducts[it]
            )
        }
        // jump from single pane which is from an alert
        if (nexradArguments.doNotSavePref && !nexradArguments.useSinglePanePref) {
            wxglRenders.indices.forEach {
                wxglRenders[it].state.product = defaultProducts[it]
            }
        }
        //
        // restore session (x, y, zoom)
        //
        var zoomPref = "_ZOOM1"
        var xPref = "_X1"
        var yPref = "_Y1"
        if (nexradArguments.useSinglePanePref) {
            zoomPref = "_ZOOM"
            xPref = "_X"
            yPref = "_Y"
        }
        wxglSurfaceViews[0].scaleFactor = Utility.readPrefFloat(
            context,
            prefPrefix + zoomPref,
            RadarPreferences.wxoglSize.toFloat() / 10.0f
        )
        wxglRenders[0].setViewInitial(
            Utility.readPrefFloat(
                context,
                prefPrefix + zoomPref,
                RadarPreferences.wxoglSize.toFloat() / 10.0f
            ),
            Utility.readPrefFloat(context, prefPrefix + xPref, 0.0f),
            Utility.readPrefFloat(context, prefPrefix + yPref, 0.0f)
        )
        if (RadarPreferences.dualpaneshareposn) {
            (1 until numberOfPanes).forEach {
                wxglSurfaceViews[it].scaleFactor = wxglSurfaceViews[0].scaleFactor
                wxglRenders[it].setViewInitial(
                    Utility.readPrefFloat(
                        context,
                        prefPrefix + zoomPref,
                        RadarPreferences.wxoglSize.toFloat() / 10.0f
                    ),
                    wxglRenders[0].state.x, wxglRenders[0].state.y
                )
            }
        } else {
            (1 until numberOfPanes).forEach {
                wxglSurfaceViews[it].scaleFactor = Utility.readPrefFloat(
                    context,
                    prefPrefix + "_ZOOM" + (it + 1).toString(),
                    RadarPreferences.wxoglSize.toFloat() / 10.0f
                )
                wxglRenders[it].setViewInitial(
                    Utility.readPrefFloat(
                        context,
                        prefPrefix + "_ZOOM" + (it + 1).toString(),
                        RadarPreferences.wxoglSize.toFloat() / 10.0f
                    ),
                    Utility.readPrefFloat(context, prefPrefix + "_X" + (it + 1).toString(), 0.0f),
                    Utility.readPrefFloat(context, prefPrefix + "_Y" + (it + 1).toString(), 0.0f)
                )
            }
        }
    }

    fun writePreferences(context: Context, nexradArguments: NexradArgumentsSinglePane) {
        if (!nexradArguments.archiveMode && !nexradArguments.fixedSite) {
            savePrefs(context, render.state)
        }
    }

    fun writePreferencesMultipane(context: Context, doNotSavePref: Boolean) {
        if (!doNotSavePref) {
            wxglRenders.forEachIndexed { index, wxglRender ->
                savePrefs(context, prefPrefix, index + 1, wxglRender.state)
            }
        } else {
            wxglRenders.forEachIndexed { index, wxglRender ->
                saveProductPrefs(context, prefPrefix, index + 1, wxglRender.state)
            }
        }
    }

    fun writePreferencesMultipaneOnStop(context: Context, doNotSavePref: Boolean) {
        if (!doNotSavePref) {
            wxglRenders.forEachIndexed { index, wxglRender ->
                savePrefs(context, prefPrefix, index + 1, wxglRender.state)
            }
        }
    }

    // single pane
    private fun savePrefs(context: Context, state: NexradRenderState) {
        val prefToken = "WXOGL"
        Utility.writePref(context, prefToken + "_RID", state.rid)
        Utility.writePref(context, prefToken + "_PROD", state.product)
        Utility.writePrefFloat(context, prefToken + "_ZOOM", state.zoom)
        Utility.writePrefFloat(context, prefToken + "_X", state.x)
        Utility.writePrefFloat(context, prefToken + "_Y", state.y)
        RadarPreferences.wxoglRid = state.rid
        RadarPreferences.wxoglProd = state.product
        RadarPreferences.wxoglZoom = state.zoom
        RadarPreferences.wxoglX = state.x
        RadarPreferences.wxoglY = state.y
    }

    private fun savePrefs(
        context: Context,
        prefPrefix: String,
        idx: Int,
        state: NexradRenderState
    ) {
        Utility.writePref(context, prefPrefix + "_RID" + idx.toString(), state.rid)
        Utility.writePref(context, prefPrefix + "_PROD" + idx.toString(), state.product)
        Utility.writePrefFloat(context, prefPrefix + "_ZOOM" + idx.toString(), state.zoom)
        Utility.writePrefFloat(context, prefPrefix + "_X" + idx.toString(), state.x)
        Utility.writePrefFloat(context, prefPrefix + "_Y" + idx.toString(), state.y)
    }

    private fun saveProductPrefs(
        context: Context,
        prefPrefix: String,
        idx: Int,
        state: NexradRenderState
    ) {
        Utility.writePref(context, prefPrefix + "_PROD" + idx.toString(), state.product)
    }

    fun setToolbarTitleMultipane(updateWithWarnings: Boolean): String {
        var title = if (RadarPreferences.dualpaneshareposn) {
            var s = (curRadar + 1).toString() + ":" + wxglRenders[0].state.rid + "("
            wxglRenders.forEach {
                s += it.state.product + ","
            }
            s = s.trimEnd(',')
            "$s)"
        } else {
            var s = (curRadar + 1).toString() + ": "
            wxglRenders.forEach {
                s += it.state.rid + "(" + it.state.product + ") "
            }
            s.trimEnd()
        }
        if (updateWithWarnings) {
            title += " " + Warnings.getCountString()
        }
        return title
    }

    fun setAllPanesTo(newRadarSite: String) {
        wxglRenders.forEach {
            it.state.rid = newRadarSite
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
            wxglRenders[it].state.rid = newRadarSite
            wxglSurfaceViews[it].scaleFactor = RadarPreferences.wxoglSize / 10.0f
            wxglRenders[it].setViewInitial(RadarPreferences.wxoglSize / 10.0f, 0.0f, 0.0f)
        }
    }

    fun adjustOnePaneTo(index: Int, newRadarSite: String) {
        wxglRenders[index].state.rid = newRadarSite
        wxglSurfaceViews[index].scaleFactor = RadarPreferences.wxoglSize / 10.0f
        wxglRenders[index].setViewInitial(RadarPreferences.wxoglSize / 10.0f, 0.0f, 0.0f)
    }

    fun updateLocationDots(nexradArguments: NexradArguments) {
        wxglRenders.indices.forEach {
            wxglRenders[it].construct.locationDot(
                latD,
                lonD,
                nexradArguments.archiveMode
            )
            draw(it)
            if (RadarPreferences.wxoglCenterOnLocation) {
                wxglSurfaceViews[it].resetView()
                NexradRenderTextObject.hideLabels(wxglTextObjects)
                NexradRenderTextObject.showLabels(wxglTextObjects)
            }
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
        if ((product.matches(Regex("N[0-3]Q")) || product == "L2REF") && isTdwr) {
            if (tilt == "3") {
                tilt = "2"
            }
            product = "TZL"
        }
        if ((product == "TZL" || product.startsWith("TZ")) && !isTdwr) {
            product = "N" + tilt + "Q"
        }
        if ((product.matches(Regex("N[0-3]U")) || product == "L2VEL") && isTdwr) {
            if (tilt == "3") {
                tilt = "2"
            }
            product = "TV${tilt}"
        }
        if (product.startsWith("TV") && !isTdwr) {
            product = "N" + tilt + "U"
        }
    }

    fun adjustForTdwrMultiPane(z: Int) {
        with(wxglRenders[z]) {
            if ((state.product.matches(Regex("N[0-3]Q")) || state.product == "L2REF") && isRidTdwr(
                    state.rid
                )
            ) {
                state.product = "TZL"
            }
            if (state.product == "TZL" && !isRidTdwr(state.rid)) {
                state.product = "N0Q"
            }
            if ((state.product.matches(Regex("N[0-3]U")) || state.product == "L2VEL") && isRidTdwr(
                    state.rid
                )
            ) {
                state.product = "TV0"
            }
            if (state.product == "TV0" && !isRidTdwr(state.rid)) {
                state.product = "N0U"
            }
        }
    }

    fun getReflectivity(getContent: () -> Unit) {
        product = if (RadarPreferences.iconsLevel2 && product.matches("N[0-3]Q".toRegex())) {
            "L2REF"
        } else {
            if (!isTdwr) {
                "N" + tilt + "Q"
            } else {
                "TZL"
            }
        }
        getContent()
    }

    fun getVelocity(getContent: () -> Unit) {
        product = if (RadarPreferences.iconsLevel2 && product.matches("N[0-3]U".toRegex())) {
            "L2VEL"
        } else {
            if (!isTdwr) {
                "N" + tilt + "U"
            } else {
                "TV${tilt}"
            }
        }
        getContent()
    }

    fun getLatLon(): LatLon = LatLon(latD, lonD)
}

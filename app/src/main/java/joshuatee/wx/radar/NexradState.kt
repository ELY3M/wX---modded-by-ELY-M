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
import android.widget.RelativeLayout
import joshuatee.wx.settings.RadarPreferences

// NOT CURRENTLY USED

// FIXME TODO tasks remaining:
// add methods to restore/save prefs
// cut-over for single pane
// cut-over for multi-pane
// cut-over for location fragment

class NexradState(activity: Activity, val numberOfPanes: Int, relativeLayoutResIdList: List<Int>, widthDivider: Int, heightDivider: Int) {

    val wxglRenders = mutableListOf<WXGLRender>()
    val wxglSurfaceViews = mutableListOf<WXGLSurfaceView>()
    var wxglTextObjects = mutableListOf<WXGLTextObject>()
    val relativeLayouts = mutableListOf<RelativeLayout>()
    var oldRadarSites = Array(1) { "" }
    var tilt = "0"
    var latD = 0.0
    var lonD = 0.0
    // single pane only
    var radarSitesForFavorites = listOf<String>()
    var oldProd = ""
    var curRadar = 0

    init {
        repeat(numberOfPanes) {
            wxglSurfaceViews.add(WXGLSurfaceView(activity, widthDivider, numberOfPanes, heightDivider))
            wxglRenders.add(WXGLRender(activity, it))
        }
        wxglRenders.indices.forEach {
            relativeLayouts.add(activity.findViewById(relativeLayoutResIdList[it]))
            relativeLayouts.last().addView(wxglSurfaceViews[it])
        }
        wxglSurfaceViews.indices.forEach {
            wxglTextObjects.add(WXGLTextObject(activity, relativeLayouts[it], wxglSurfaceViews[it], wxglRenders[it], numberOfPanes, it))
            wxglSurfaceViews[it].wxglTextObjects = wxglTextObjects
            wxglTextObjects.last().initializeLabels(activity)
        }
    }

    fun readPreferences(arguments: Array<String>?, nexradArguments: NexradArguments) {
        product = "N0Q"
        if (arguments == null) {
            radarSite = joshuatee.wx.settings.Location.rid
        } else {
            radarSite = arguments[0]
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

    fun writePreferences(context: Context, nexradArguments: NexradArguments) {
        if (!nexradArguments.archiveMode && !nexradArguments.fixedSite) {
            WXGLNexrad.savePrefs(context, "WXOGL", render)
        }
    }

    fun draw() {
        surface.requestRender()
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

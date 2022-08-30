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

import android.content.Context
import android.widget.RelativeLayout
import joshuatee.wx.MyApplication
import joshuatee.wx.settings.RadarPreferences

class NexradStateMainScreen(
        activityReference: Context,
        numberOfPanes: Int,
        homeScreenTokens: List<String>
) : NexradState(numberOfPanes) {

    var oldRadarSites = Array(numberOfPanes) { "" }

    init {
        var index = 0
        isHomeScreen = true
        homeScreenTokens.forEach { token ->
            if (token == "OGL-RADAR" || token.contains("NXRD-")) {
                wxglRenders.add(WXGLRender(MyApplication.appContext, 4))
                wxglSurfaceViews.add(WXGLSurfaceView(MyApplication.appContext, 1, 1, 1))
                wxglSurfaceViews[index].index = index
                if (token.contains("NXRD-")) {
                    wxglRenders[index].rid = token.replace("NXRD-", "")
                } else {
                    wxglRenders[index].rid = ""
                }
                oldRadarSites[index] = ""
                relativeLayouts.add(RelativeLayout(activityReference))
                wxglTextObjects.add(WXGLTextObject(activityReference,
                        relativeLayouts[index],
                        wxglSurfaceViews[index],
                        wxglRenders[index],
                        1, 4))
                wxglSurfaceViews[index].wxglTextObjects = wxglTextObjects
                wxglSurfaceViews[index].locationFragment = true
                wxglTextObjects[index].initializeLabels(activityReference)
                relativeLayouts[index].addView(wxglSurfaceViews[index])
                index += 1
            }
        }
    }

    fun adjustPaneTo(index: Int, newRadarSite: String) {
        wxglRenders[index].rid = newRadarSite
        wxglSurfaceViews[index].scaleFactor = RadarPreferences.wxoglSize / 10.0f
        wxglRenders[index].setViewInitial(RadarPreferences.wxoglSize / 10.0f, 0.0f, 0.0f)
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

    fun adjustForTdwr(idx: Int) {
        if (wxglRenders[idx].product == "N0Q" && isRidTdwr(wxglRenders[idx].rid)) {
            wxglRenders[idx].product = "TZL"
        }
        if (wxglRenders[idx].product == "TZL" && !isRidTdwr(wxglRenders[idx].rid)) {
            wxglRenders[idx].product = "N0Q"
        }
        if (wxglRenders[idx].product == "N0U" && isRidTdwr(wxglRenders[idx].rid)) {
            wxglRenders[idx].product = "TV0"
        }
        if (wxglRenders[idx].product == "TV0" && !isRidTdwr(wxglRenders[idx].rid)) {
            wxglRenders[idx].product = "N0U"
        }
    }

    fun resetAllGlview() {
        wxglSurfaceViews.indices.forEach {
            NexradDraw.resetGlview(wxglSurfaceViews[it], wxglRenders[it])
            wxglTextObjects[it].addLabels()
        }
    }
}

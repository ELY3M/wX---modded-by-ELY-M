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

import android.widget.RelativeLayout
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.Utility

class NexradColorLegend(val activity: VideoRecordActivity, val nexradState: NexradStatePane) {

    private var legendShown = false
    private var legend: ViewColorLegend? = null

    init {
        if (RadarPreferences.showLegend) {
            showLegend()
        }
    }

    fun showLegend() {
        if (!legendShown) {
            if (nexradState.product == "DSA" || nexradState.product == "DAA") {
                WXGLRadarActivity.dspLegendMax = (255.0f / nexradState.render.wxglNexradLevel3.halfword3132) * 0.01f
            }
            WXGLRadarActivity.velMax = nexradState.render.wxglNexradLevel3.halfword48
            WXGLRadarActivity.velMin = nexradState.render.wxglNexradLevel3.halfword47
            legendShown = true
            val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1)
            legend = ViewColorLegend(activity, nexradState.product)
            nexradState.relativeLayouts[0].addView(legend, layoutParams)
            RadarPreferences.showLegend = true
            Utility.writePref(activity,"RADAR_SHOW_LEGEND", "true")
        } else {
            nexradState.relativeLayouts[0].removeView(legend)
            legendShown = false
            RadarPreferences.showLegend = false
            Utility.writePref(activity,"RADAR_SHOW_LEGEND", "false")
        }
    }

    private fun updateLegend() {
        nexradState.relativeLayouts[0].removeView(legend)
        val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1)
        legend = ViewColorLegend(activity, nexradState.product)
        nexradState.relativeLayouts[0].addView(legend, layoutParams)
    }

    fun updateLegendAfterDownload() {
        if (legendShown && nexradState.product != nexradState.oldProd && nexradState.product != "DSA" && nexradState.product != "DAA") {
            updateLegend()
        }
        if (legendShown && (nexradState.product == "DSA" || nexradState.product == "DAA" || nexradState.product == "N0U")) {
            WXGLRadarActivity.dspLegendMax = (255.0f / nexradState.render.wxglNexradLevel3.halfword3132) * 0.01f
            WXGLRadarActivity.velMax = nexradState.render.wxglNexradLevel3.halfword48
            WXGLRadarActivity.velMin = nexradState.render.wxglNexradLevel3.halfword47
            updateLegend()
        }
    }
}

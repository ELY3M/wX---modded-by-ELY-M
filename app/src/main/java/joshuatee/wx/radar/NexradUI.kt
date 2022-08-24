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

import android.graphics.Color
import joshuatee.wx.common.GlobalArrays
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.ui.ObjectDialogue
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.UtilityLog
import java.io.File

class NexradUI(val activity: VideoRecordActivity, val nexradState: NexradStatePane) {

    fun showTdwrDialog(mapSwitch: (String) -> Unit) {
        val objectDialogue = ObjectDialogue(activity, GlobalArrays.tdwrRadars)
        objectDialogue.connectCancel { dialog, _ ->
            dialog.dismiss()
            UtilityUI.immersiveMode(activity)
        }
        objectDialogue.connect { dialog, itemIndex ->
            val s = GlobalArrays.tdwrRadars[itemIndex]
            nexradState.radarSite = s.split(" ")[0]
            nexradState.product = "TZL"
            mapSwitch(nexradState.radarSite)
            dialog.dismiss()
        }
        objectDialogue.show()
    }

    fun showRadarScanInfo() {
        if (nexradState.numberOfPanes == 1) {
            ObjectDialogue(activity, WXGLNexrad.getRadarInfo(activity, ""))
        } else {
            val scanInfoList = nexradState.wxglRenders.indices.map { WXGLNexrad.getRadarInfo(activity, (it + 1).toString()) }
            ObjectDialogue(activity, scanInfoList.joinToString(GlobalVariables.newline + GlobalVariables.newline))
        }
    }

    fun setTitleWithWarningCounts() {
        if (RadarPreferences.warnings) {
            activity.title = nexradState.product + " " + WXGLPolygonWarnings.getCountString()
        }
    }

    fun setSubTitle() {
        val items = WXGLNexrad.getRadarInfo(activity,"").split(" ")
        if (items.size > 3) {
            activity.toolbar.subtitle = items[3]
            if (ObjectDateTime.isRadarTimeOld(items[3]))
                activity.toolbar.setSubtitleTextColor(Color.RED)
            else
                activity.toolbar.setSubtitleTextColor(Color.LTGRAY)
        } else {
            activity.toolbar.subtitle = ""
        }
    }

    //
    // multipane
    //
    fun setToolbarTitle(updateWithWarnings: Boolean = false) {
        activity.title = nexradState.setToolbarTitleMultipane(updateWithWarnings)
    }

    fun setTitleWithWarningCountsMultiPane() {
        setToolbarTitle(RadarPreferences.warnings)
    }
    //
    // multipane end
    //

    // used by animation
    fun progressUpdate(vararg values: String) {
        if ((values[1].toIntOrNull() ?: 0) > 1) {
            val list = WXGLNexrad.getRadarInfo(activity, "").split(" ")
            if (list.size > 3)
                activity.toolbar.subtitle = list[3] + " (" + values[0] + "/" + values[1] + ")"
            else
                activity.toolbar.subtitle = ""
        } else {
            activity.toolbar.subtitle = "Problem downloading"
        }
    }

    // TODO FIXME move out
    fun animateDownloadFiles(frameCount: Int): List<String> {
        val animArray = WXGLDownload.getRadarFilesForAnimation(activity, frameCount, nexradState.radarSite, nexradState.product)
        try {
            animArray.indices.forEach {
                val file = File(activity.filesDir, animArray[it])
                activity.deleteFile("nexrad_anim$it")
                if (!file.renameTo(File(activity.filesDir, "nexrad_anim$it")))
                    UtilityLog.d("wx", "Problem moving to nexrad_anim$it")
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return animArray
    }

    //
    // multipane
    //
    fun animateDownloadFilesMultiPane(frameCount: Int): Array<Array<String>> {
        val animArray = Array(nexradState.numberOfPanes) { Array(frameCount) { "" } }
        nexradState.wxglRenders.forEachIndexed { z, wxglRender ->
            animArray[z] = WXGLDownload.getRadarFilesForAnimation(activity, frameCount, wxglRender.rid, wxglRender.product).toTypedArray()
            try {
                animArray[z].indices.forEach { r ->
                    val file = File(activity.filesDir, animArray[z][r])
                    activity.deleteFile((z + 1).toString() + wxglRender.product + "nexrad_anim" + r.toString())
                    if (!file.renameTo(File(activity.filesDir, (z + 1).toString() + wxglRender.product + "nexrad_anim" + r.toString())))
                        UtilityLog.d("wx", "Problem moving to " + (z + 1).toString() + wxglRender.product + "nexrad_anim" + r.toString())
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        }
        return animArray
    }

    fun progressUpdateMultiPane(vararg values: String) {
        if ((values[1].toIntOrNull() ?: 0) > 1) {
            setSubTitleMultiPane(values[0], values[1])
        } else {
            activity.toolbar.subtitle = "Problem downloading"
        }
    }

    fun setSubTitleMultiPane(a: String = "", b: String = "") {
        // take each radar timestamp and split into a list
        // make sure the list is the correct size (more then 3 elements)
        // create another list consisting of the 4th item of each list (the HH:MM:SS)
        // set the subtitle to a string which is the new list joined by "/"
        val radarInfoList = nexradState.wxglRenders.indices.map { WXGLNexrad.getRadarInfo(activity, (it + 1).toString()) }
        val tmpArray = radarInfoList.map { it.split(" ") }
        if (tmpArray.all { it.size > 3}) {
            val tmpArray2 = tmpArray.map { it[3] }
            var s = tmpArray2.joinToString("/")
            if (a != "" && b != "") {
                s += "($a/$b)"
            }
            activity.toolbar.subtitle = s
        } else {
            activity.toolbar.subtitle = ""
        }
    }
    // infoArr will look like the following for 2 panes
    /*
    [Fri Aug 19 07:22:51 EDT 2022
    Radar Mode: 2
    VCP: 12
    Product Code: 94
    Radar height: 776
    Radar Lat: 32.573
    Radar Lon: -97.303, Fri Aug 19 07:22:51 EDT 2022
    Radar Mode: 2
    VCP: 12
    Product Code: 99
    Radar height: 776
    Radar Lat: 32.573
    Radar Lon: -97.303]
     */
}

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

import android.os.SystemClock
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.util.To
import joshuatee.wx.util.UtilityFileManagement
import joshuatee.wx.util.UtilityLog
import kotlinx.coroutines.*
import java.io.File

class NexradAnimation(
        val activity: VideoRecordActivity,
        private val nexradState: NexradStatePane,
        private val nexradUI: NexradUI
) {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main

    private fun downloadFiles(frameCount: Int): List<List<String>> {
        val listOfUrls = List(nexradState.numberOfPanes) { List(frameCount) { "" } }.toMutableList()
        nexradState.wxglRenders.forEachIndexed { paneIndex, wxglRender ->
            listOfUrls[paneIndex] = NexradDownload.getRadarFilesForAnimation(activity, frameCount, wxglRender.state.rid, wxglRender.state.product)
            try {
                listOfUrls[paneIndex].indices.forEach { frameIndex ->
                    val file = File(activity.filesDir, listOfUrls[paneIndex][frameIndex])
                    val fileName = getFilename(paneIndex, frameIndex, wxglRender.state)
                    activity.deleteFile(fileName)
                    if (!file.renameTo(File(activity.filesDir, fileName))) {
                        UtilityLog.d("wx", "Problem moving to $fileName")
                    }
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        }
        return listOfUrls
    }

    private fun getFilename(paneIndex: Int, frameIndex: Int, state: NexradRenderState): String =
            (paneIndex + 1).toString() + state.product + "nexrad_anim" + frameIndex.toString()

    private fun updateMultiPane(vararg values: String) {
        if (To.int(values[1]) > 1) {
            nexradUI.setSubTitleMultiPane(values[0], values[1])
        } else {
            activity.toolbar.subtitle = "Problem downloading"
        }
    }

    private fun updateSinglePane(vararg values: String) {
        if (To.int(values[1]) > 1) {
            val items = NexradUtil.getRadarInfo(activity, "").split(" ")
            if (items.size > 3)
                activity.toolbar.subtitle = "${items[3]} (${values[0]}/${values[1]})"
            else
                activity.toolbar.subtitle = ""
        } else {
            activity.toolbar.subtitle = "Problem downloading"
        }
    }

    fun run(frameCount: Int) = GlobalScope.launch(uiDispatcher) {
        nexradState.showViews()
        nexradUI.inOglAnim = true
        withContext(Dispatchers.IO) {
            var listOfUrls = downloadFiles(frameCount)
            var loopCnt = 0
            while (nexradUI.inOglAnim) {
                if (nexradUI.animTriggerDownloads) {
                    listOfUrls = downloadFiles(frameCount)
                    nexradUI.animTriggerDownloads = false
                }
                for (frameIndex in listOfUrls[0].indices) {
                    while (nexradUI.inOglAnimPaused) {
                        SystemClock.sleep(nexradUI.delay.toLong())
                    }
                    // formerly priorTime was set at the end but that is goofed up with pause
                    val priorTime = ObjectDateTime.currentTimeMillis()
                    // added because if paused and then another icon life vel/ref it won't load correctly, likely
                    // timing issue
                    if (!nexradUI.inOglAnim) {
                        break
                    }
                    if (loopCnt > 0) {
                        nexradState.wxglRenders.forEachIndexed { paneIndex, wxglRender ->
                            wxglRender.constructPolygons(getFilename(paneIndex, frameIndex, wxglRender.state), false)
                        }
                    } else {
                        nexradState.wxglRenders.forEachIndexed { paneIndex, wxglRender ->
                            wxglRender.constructPolygons(getFilename(paneIndex, frameIndex, wxglRender.state), true)
                        }
                    }
                    launch(uiDispatcher) {
                        if (nexradState.numberOfPanes > 1) {
                            updateMultiPane((frameIndex + 1).toString(), listOfUrls[0].size.toString())
                        } else {
                            updateSinglePane((frameIndex + 1).toString(), listOfUrls[0].size.toString())
                        }
                    }
                    nexradState.drawAll()
                    val timeMilli = ObjectDateTime.currentTimeMillis()
                    if ((timeMilli - priorTime) < nexradUI.delay) {
                        SystemClock.sleep(nexradUI.delay - (timeMilli - priorTime))
                    }
                    if (!nexradUI.inOglAnim) {
                        break
                    }
                    // sleep twice as long after the last frame
                    if (frameIndex == listOfUrls[0].lastIndex) {
                        SystemClock.sleep(nexradUI.delay.toLong() * 2)
                    }
                }
                loopCnt += 1
            }
        }
        UtilityFileManagement.deleteCacheFiles(activity)
    }
}

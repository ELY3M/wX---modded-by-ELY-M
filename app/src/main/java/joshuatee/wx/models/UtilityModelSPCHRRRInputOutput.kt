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

package joshuatee.wx.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.Extensions.*
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.MyApplication
import joshuatee.wx.util.UtilityLog

internal object UtilityModelSpcHrrrInputOutput {

    val runTime: RunTimeData
        get() {
            val runData = RunTimeData()
            val htmlRunStatus = (GlobalVariables.nwsSPCwebsitePrefix + "/exper/hrrr/data/hrrr3/cron.log").getHtml()
            runData.validTime = htmlRunStatus.parse("Latest Run: ([0-9]{10})")
            runData.mostRecentRun = runData.validTime
            runData.listRunAdd(runData.mostRecentRun)
            val runTimes = htmlRunStatus.parseColumn("Run: ([0-9]{8}/[0-9]{4})")
            for (time in runTimes.reversed()) {
                var t = time.replace("/", "")
                if (t != (runData.mostRecentRun + "00")) {
                    t = t.dropLast(2)
                    runData.listRunAdd(t)
                }
            }
            return runData
        }

    fun getImage(context: Context, om: ObjectModelNoSpinner, time: String, overlayImg: List<String>): Bitmap {
        val layerUrl = "${MyApplication.nwsSPCwebsitePrefix}/exper/mesoanalysis/"
        val bitmaps = mutableListOf<Bitmap>()
        val layers = mutableListOf<Drawable>()
        overlayImg.forEach {
            val url = layerUrl + getSectorCode(om.sector).lowercase(Locale.US) + "/" + it + "/" + it + ".gif"
            bitmaps.add(UtilityImg.eraseBackground(url.getImage(), -1))
        }
        val backgroundUrl = "${MyApplication.nwsSPCwebsitePrefix}/exper/hrrr/data/hrrr3/" + getSectorCode(om.sector).lowercase(Locale.US) + "/R" +
                om.run.replace("Z", "") + "_F" + formatTime(time) + "_V" + getValidTime(om.run, time) +
                "_" + getSectorCode(om.sector) + "_" + om.currentParam + ".gif"
        bitmaps.add(UtilityImg.eraseBackground(backgroundUrl.getImage(), -1))
        layers.add(ColorDrawable(Color.WHITE))
        layers += bitmaps.map { BitmapDrawable(context.resources, it) }
        return UtilityImg.layerDrawableToBitmap(layers)
    }

    fun getAnimation(context: Context, om: ObjectModelNoSpinner, overlayImg: List<String>): AnimationDrawable {
        if (om.spinnerTimeValue == -1) return AnimationDrawable()
        val bitmaps = (om.spinnerTimeValue until om.times.size).map { k ->
            getImage(context, om, om.times[k].split(" ").dropLastWhile { it.isEmpty() }.getOrNull(0) ?: "", overlayImg)
        }
        return UtilityImgAnim.getAnimationDrawableFromBitmapList(context, bitmaps)
    }

    private fun getSectorCode(sectorName: String) =
        (UtilityModelSpcHrrrInterface.sectors.indices)
            .firstOrNull { sectorName == UtilityModelSpcHrrrInterface.sectors[it] }
            ?.let { UtilityModelSpcHrrrInterface.sectorCodes[it] }
            ?: "S19"

    private fun getValidTime(run: String, validTimeForecast: String): String {
        val format = SimpleDateFormat("yyyyMMddHH", Locale.US)
        val parsed: Date
        val oneMinuteInMillis: Long = 60000
        try {
            parsed = format.parse(run)!!
            val t = parsed.time
            return format.format(Date(t  + 60 * oneMinuteInMillis * validTimeForecast.toLong()))
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return ""
    }

    private fun formatTime(time: String) = "0$time"
}

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

package joshuatee.wx.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import java.util.Locale
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.getHtml
import joshuatee.wx.getImage
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.parse
import joshuatee.wx.parseColumn
import joshuatee.wx.util.To
import androidx.core.graphics.drawable.toDrawable

internal object UtilityModelSpcHrrrInputOutput {

    val runTime: RunTimeData
        get() {
            val runData = RunTimeData()
            val htmlRunStatus =
                (GlobalVariables.NWS_SPC_WEBSITE_PREFIX + "/exper/hrrr/data/hrrr3/cron.log").getHtml()
            runData.validTime = htmlRunStatus.parse("Latest Run: ([0-9]{10})")
            runData.mostRecentRun = runData.validTime
            val runTimes = htmlRunStatus.parseColumn("Run: ([0-9]{8}/[0-9]{4})")
            runTimes.reversed().filterNot { it.contains("Latest Run:") }.forEach {
                runData.listRunAdd(it.replace("/", "").dropLast(2))
            }
            return runData
        }

    fun getImage(
        context: Context,
        om: ObjectModel,
        time: String,
        overlayImg: List<String>
    ): Bitmap {
        val layerUrl = "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/exper/mesoanalysis/"
        val bitmaps = mutableListOf<Bitmap>()
        val layers = mutableListOf<Drawable>()
        overlayImg.forEach {
            val url =
                layerUrl + getSectorCode(om.sector).lowercase(Locale.US) + "/" + it + "/" + it + ".gif"
            bitmaps.add(UtilityImg.eraseBackground(url.getImage(), -1))
        }
        val backgroundUrl = "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/exper/hrrr/data/hrrr3/" +
                getSectorCode(om.sector).lowercase(Locale.US) + "/R" +
                om.run.replace("Z", "") + "_F" + formatTime(time) + "_V" +
                getValidTime(
                    om.run,
                    time
                ) + "_" + getSectorCode(om.sector) + "_" + om.currentParam + ".gif"
        bitmaps.add(UtilityImg.eraseBackground(backgroundUrl.getImage(), -1))
        layers.add(Color.WHITE.toDrawable())
        layers += bitmaps.map { it.toDrawable(context.resources) }
        return UtilityImg.layerDrawableToBitmap(layers)
    }

    fun getAnimation(
        context: Context,
        om: ObjectModel,
        overlayImg: List<String>
    ): AnimationDrawable = if (om.spinnerTimeValue == -1) {
        AnimationDrawable()
    } else {
        val bitmaps = (om.timeIndex until om.times.size).map { k ->
            getImage(
                context, om, om.times[k].split(" ").dropLastWhile { it.isEmpty() }.getOrNull(0)
                    ?: "", overlayImg
            )
        }
        UtilityImgAnim.getAnimationDrawableFromBitmapList(context, bitmaps)
    }

    private fun getSectorCode(sectorName: String): String =
        UtilityModelSpcHrrrInterface.sectors.indices
            .firstOrNull { sectorName == UtilityModelSpcHrrrInterface.sectors[it] }
            ?.let { UtilityModelSpcHrrrInterface.sectorCodes[it] }
            ?: "S19"

    private fun getValidTime(run: String, validTimeForecast: String): String {
        val timeFormatString = "yyyyMMddHH"
        val time = ObjectDateTime.parse(run, timeFormatString)
        time.addHours(To.int(validTimeForecast).toLong())
        return time.format(timeFormatString)
    }

    private fun formatTime(time: String): String = "0$time"
}

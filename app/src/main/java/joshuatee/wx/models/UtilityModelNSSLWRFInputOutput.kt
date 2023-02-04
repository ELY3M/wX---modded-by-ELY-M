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

package joshuatee.wx.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import java.util.Locale
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.Extensions.*
import joshuatee.wx.objects.ObjectDateTime

internal object UtilityModelNsslWrfInputOutput {

    private const val baseUrl = "https://cams.nssl.noaa.gov"

    val runTime: RunTimeData
        get() {
            val runData = RunTimeData()
            val htmlRunStatus = baseUrl.getHtml()
            val html = htmlRunStatus.parse("\\{model: \"fv3_nssl\",(rd: .[0-9]{8}\",rt: .[0-9]{4}\",)")
            val day = html.parse("rd:.(.*?),.*?").replace("\"", "")
            val time = html.parse("rt:.(.*?)00.,.*?").replace("\"", "")
            val mostRecentRun = day + time
            runData.listRunAddAll(ObjectDateTime.generateModelRuns(mostRecentRun, 24, "yyyyMMddHH","yyyyMMddHH", 4))
            runData.mostRecentRun = mostRecentRun
            return runData
        }

    fun getImage(context: Context, om: ObjectModel, timeOriginal: String): Bitmap {
        val time = timeOriginal.split(" ")[0]
        val sectorIndex = if (om.sector == "") {
            0
        } else {
            UtilityModelNsslWrfInterface.sectorsLong.indexOf(om.sector)
        }
        val sector = UtilityModelNsslWrfInterface.sectors[sectorIndex]
        val baseLayerUrl = "https://cams.nssl.noaa.gov/graphics/blank_maps/spc_$sector.png"
        var modelPostfix = "_nssl"
        var model = om.model.lowercase(Locale.US)
        if (om.model == "HRRRV3") {
            modelPostfix = ""
        } else if (om.model == "WRF_3KM") {
            model = "wrf_nssl_3km"
            modelPostfix = ""
        }
        return if (om.run.length > 8) {
            val year = om.run.substring(0, 4)
            val month = om.run.substring(4, 6)
            val day = om.run.substring(6, 8)
            val hour = om.run.substring(8, 10)
            val url = baseUrl + "/graphics/models/" + model + modelPostfix + "/" + year + "/" + month + "/" + day + "/" + hour + "00/f" +
                    time + "00/" + om.currentParam + ".spc_" + sector.lowercase(Locale.US) + ".f" + time + "00.png"
            val baseLayerImage = baseLayerUrl.getImage()
            val productLayerImage = url.getImage()
            UtilityImg.addColorBackground(context, UtilityImg.mergeImages(context, productLayerImage, baseLayerImage), Color.WHITE)
        } else {
            UtilityImg.getBlankBitmap()
        }
    }
}

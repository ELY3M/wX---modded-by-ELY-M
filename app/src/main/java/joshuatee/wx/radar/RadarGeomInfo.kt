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
import android.graphics.Color
import joshuatee.wx.R
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RadarGeomInfo(val context: Context, private val type: RadarGeometryTypeEnum) {

    //
    // radar geometry files are in: ./app/src/main/res/raw/
    // count values below are file size in bytes divided by 4 as these files contain binary packed floats
    //

    var isEnabled = Utility.readPref(context, prefToken[type]!!, defaultPref[type]!!).startsWith("t")
    private val lineSizeDefault = 2
    var lineSize = Utility.readPrefInt(context, prefTokenLineSize[type]!!, lineSizeDefault).toFloat()
    var colorInt = Utility.readPrefInt(context, prefTokenColorInt[type]!!, prefTokenColorIntDefault[type]!!)
    var lineData: ByteBuffer = ByteBuffer.allocateDirect(0)
    var count = typeToCount[type]!!
    var fileId = typeToFileName[type]!!

    fun update() {
        isEnabled = Utility.readPref(context, prefToken[type]!!, defaultPref[type]!!).startsWith("t")
        if (isEnabled && lineData.capacity() == 0) {
            loadData(context)
        }
        lineSize = Utility.readPrefInt(context, prefTokenLineSize[type]!!, lineSizeDefault).toFloat()
        colorInt = Utility.readPrefInt(context, prefTokenColorInt[type]!!, prefTokenColorIntDefault[type]!!)
    }

    private fun initBuffer() {
        lineData = ByteBuffer.allocateDirect(4 * count)
        lineData.order(ByteOrder.nativeOrder())
        lineData.position(0)
    }

    fun loadData(context: Context) {
        if (isEnabled) {
            initBuffer()
            try {
                val inputStream = context.resources.openRawResource(fileId)
                val dataInputStream = DataInputStream(BufferedInputStream(inputStream))
                for (unused in 0 until count) {
                    lineData.putFloat(dataInputStream.readFloat())
                }
                dataInputStream.close()
                inputStream.close()
            } catch (e: IOException) {
                UtilityLog.handleException(e)
            }
        }
    }

    companion object {
        val typeToCount = mapOf(
                RadarGeometryTypeEnum.CaLines to 161792,
                RadarGeometryTypeEnum.MxLines to 151552,
                RadarGeometryTypeEnum.StateLines to 205748,
                RadarGeometryTypeEnum.CountyLines to 212992,
                RadarGeometryTypeEnum.HwLines to 862208,
                RadarGeometryTypeEnum.HwExtLines to 770048,
                RadarGeometryTypeEnum.LakeLines to 503808,
        )

        val typeToFileName = mapOf(
                RadarGeometryTypeEnum.CaLines to R.raw.ca,
                RadarGeometryTypeEnum.MxLines to R.raw.mx,
                RadarGeometryTypeEnum.StateLines to R.raw.statev2,
                RadarGeometryTypeEnum.CountyLines to R.raw.county,
                RadarGeometryTypeEnum.HwLines to R.raw.hwv4,
                RadarGeometryTypeEnum.HwExtLines to R.raw.hwv4ext,
                RadarGeometryTypeEnum.LakeLines to R.raw.lakesv3,
        )

        val prefToken = mapOf(
                RadarGeometryTypeEnum.CaLines to "RADAR_CAMX_BORDERS",
                RadarGeometryTypeEnum.MxLines to "RADAR_CAMX_BORDERS",
                RadarGeometryTypeEnum.StateLines to "RADAR_SHOW_STATELINES",
                RadarGeometryTypeEnum.CountyLines to "RADAR_SHOW_COUNTY",
                RadarGeometryTypeEnum.HwLines to "COD_HW_DEFAULT",
                RadarGeometryTypeEnum.HwExtLines to "RADAR_HW_ENH_EXT",
                RadarGeometryTypeEnum.LakeLines to "COD_LAKES_DEFAULT",
        )

        val defaultPref = mapOf(
                RadarGeometryTypeEnum.CaLines to "false",
                RadarGeometryTypeEnum.MxLines to "false",
                RadarGeometryTypeEnum.StateLines to "true",
                RadarGeometryTypeEnum.CountyLines to "true",
                RadarGeometryTypeEnum.HwLines to "true",
                RadarGeometryTypeEnum.HwExtLines to "false",
                RadarGeometryTypeEnum.LakeLines to "false",
        )

        // TODO FIXME no separate config for CAMX
        val prefTokenLineSize = mapOf(
                RadarGeometryTypeEnum.CaLines to "RADAR_STATE_LINESIZE",
                RadarGeometryTypeEnum.MxLines to "RADAR_STATE_LINESIZE",
                RadarGeometryTypeEnum.StateLines to "RADAR_STATE_LINESIZE",
                RadarGeometryTypeEnum.CountyLines to "RADAR_COUNTY_LINESIZE",
                RadarGeometryTypeEnum.HwLines to "RADAR_HW_LINESIZE",
                RadarGeometryTypeEnum.HwExtLines to "RADAR_HWEXT_LINESIZE",
                RadarGeometryTypeEnum.LakeLines to "RADAR_LAKE_LINESIZE",
        )

        val prefTokenColorInt = mapOf(
                RadarGeometryTypeEnum.CaLines to "RADAR_COLOR_STATE",
                RadarGeometryTypeEnum.MxLines to "RADAR_COLOR_STATE",
                RadarGeometryTypeEnum.StateLines to "RADAR_COLOR_STATE",
                RadarGeometryTypeEnum.CountyLines to "RADAR_COLOR_COUNTY",
                RadarGeometryTypeEnum.HwLines to "RADAR_COLOR_HW",
                RadarGeometryTypeEnum.HwExtLines to "RADAR_COLOR_HW_EXT",
                RadarGeometryTypeEnum.LakeLines to "RADAR_COLOR_LAKES",
        )

        val prefTokenColorIntDefault = mapOf(
                RadarGeometryTypeEnum.CaLines to Color.rgb(255, 255, 255),
                RadarGeometryTypeEnum.MxLines to Color.rgb(255, 255, 255),
                RadarGeometryTypeEnum.StateLines to Color.rgb(255, 255, 255),
                RadarGeometryTypeEnum.CountyLines to Color.rgb(75, 75, 75),
                RadarGeometryTypeEnum.HwLines to Color.rgb(135, 135, 135),
                RadarGeometryTypeEnum.HwExtLines to Color.rgb(91, 91, 91),
                RadarGeometryTypeEnum.LakeLines to Color.rgb(0, 0, 255),
        )
    }
}

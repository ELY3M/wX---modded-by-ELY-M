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
import joshuatee.wx.R
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.UtilityLog
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

object RadarGeometry {

    const val canadaResId = R.raw.ca
    const val mexicoResId = R.raw.mx
    private const val caCnt = 161792
    private const val mxCnt = 151552
//    var countState = 200000 // v3 205748
    private var countStateUs = 205748
    var countState = 205748
    var countHw = 862208 // on disk size 3448832 yields  862208
    const val countHwExt = 770048 // on disk 3080192 yields 770048
    private const val hwExtFileResId = R.raw.hwv4ext // 2016_04_06
    const val countLakes = 503808 // was 14336 + 489476
    var countCounty = 212992 // file on disk is 851968, should be
    private var hwFileResId = R.raw.hwv4
    private const val lakesFileResId = R.raw.lakesv3
    private var countyFileResId = R.raw.county

    var stateRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
    var hwRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
    var hwExtRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
    var lakesRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
    var countyRelativeBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)

    // TODO FIXME move buffer from MyApp here
    // TODO FIXME rename to initialize
    fun initRadarGeometryByType(context: Context, type: GeographyType) {
//        if (!MyApplication.radarHwEnh) {
//            MyApplication.hwFileResId = R.raw.hw
//            MyApplication.countHw = 112640
//        }
        var stateLinesFileResId = R.raw.statev2
        countState = 205748
        if (RadarPreferences.radarStateHires) {
            stateLinesFileResId = R.raw.statev3
            countState = 1166552
            countStateUs = 1166552
        }
        if (RadarPreferences.radarCamxBorders) {
            countState += caCnt + mxCnt
        }
        if (RadarPreferences.radarCountyHires) {
            countyFileResId = R.raw.countyv2
            countCounty = 820852
        }
        val fileIds = listOf(
                lakesFileResId,
                hwFileResId,
                countyFileResId,
                stateLinesFileResId,
                canadaResId,
                mexicoResId,
                hwExtFileResId
        )
        val countArr = listOf(countLakes, countHw, countCounty, countStateUs, caCnt, mxCnt, countHwExt)
        val prefArr = listOf(true, true, true, true, RadarPreferences.radarCamxBorders, RadarPreferences.radarCamxBorders, RadarPreferences.radarHwEnhExt)
        when (type) {
            GeographyType.STATE_LINES -> {
                stateRelativeBuffer = ByteBuffer.allocateDirect(4 * countState)
                stateRelativeBuffer.order(ByteOrder.nativeOrder())
                stateRelativeBuffer.position(0)
                listOf(3, 4, 5).forEach {
                    loadBuffer(context, fileIds[it], stateRelativeBuffer, countArr[it], prefArr[it])
                }
            }
            GeographyType.HIGHWAYS -> {
                hwRelativeBuffer = ByteBuffer.allocateDirect(4 * countHw)
                hwRelativeBuffer.order(ByteOrder.nativeOrder())
                hwRelativeBuffer.position(0)
                for (s in intArrayOf(1)) {
                    loadBuffer(context, fileIds[s], hwRelativeBuffer, countArr[s], prefArr[s])
                }
            }
            GeographyType.HIGHWAYS_EXTENDED -> {
                if (RadarPreferences.radarHwEnhExt) {
                    hwExtRelativeBuffer = ByteBuffer.allocateDirect(4 * countHwExt)
                    hwExtRelativeBuffer.order(ByteOrder.nativeOrder())
                    hwExtRelativeBuffer.position(0)
                }
                for (s in intArrayOf(6)) {
                    loadBuffer(context, fileIds[s], hwExtRelativeBuffer, countArr[s], prefArr[s])
                }
            }
            GeographyType.LAKES -> {
                lakesRelativeBuffer = ByteBuffer.allocateDirect(4 * countLakes)
                lakesRelativeBuffer.order(ByteOrder.nativeOrder())
                lakesRelativeBuffer.position(0)
                val s = 0
                loadBuffer(context, fileIds[s], lakesRelativeBuffer, countArr[s], prefArr[s])
            }
            GeographyType.COUNTY_LINES -> {
                countyRelativeBuffer = ByteBuffer.allocateDirect(4 * countCounty)
                countyRelativeBuffer.order(ByteOrder.nativeOrder())
                countyRelativeBuffer.position(0)
                val s = 2
                loadBuffer(context, fileIds[s], countyRelativeBuffer, countArr[s], prefArr[s])
            }
            else -> {}
        }
    }

    private fun loadBuffer(context: Context, fileID: Int, byteBuffer: ByteBuffer, count: Int, isEnabled: Boolean) {
        if (isEnabled) {
            try {
                val inputStream = context.resources.openRawResource(fileID)
                val dataInputStream = DataInputStream(BufferedInputStream(inputStream))
                // for (index in 0 until count) {
                repeat(count) {
                    byteBuffer.putFloat(dataInputStream.readFloat())
                }
                dataInputStream.close()
                inputStream.close()
            } catch (e: IOException) {
                UtilityLog.handleException(e)
            }
        }
    }
}

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

import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.content.Context
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.UCARRandomAccessFile
import joshuatee.wx.util.UtilityFileManagement
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityIO

class NexradLevel2 {

    val radialStartAngle: ByteBuffer = ByteBuffer.allocateDirect(720 * 4)
    var binSize = 0.0f
        private set
    val numberOfRangeBins = 916
    val binWord: ByteBuffer = ByteBuffer.allocateDirect(720 * numberOfRangeBins)
    private val days: ByteBuffer = ByteBuffer.allocateDirect(2)
    private val msecs: ByteBuffer = ByteBuffer.allocateDirect(4)
    private var obuff = ByteBuffer.allocate(0)
    private var ibuff = ByteBuffer.allocate(0)

    init {
        if (RadarPreferences.useJni) {
            obuff = ByteBuffer.allocateDirect(829472)
            ibuff = ByteBuffer.allocateDirect(600000)
            // decomp size is a follows ref 827040 vel 460800
        }
    }

    // last argument is true/false on whether or not the DECOMP stage needs to happen
    fun decodeAndPlot(
        context: Context,
        fileName: String,
        prod: String,
        radarStatusStr: String,
        idxStr: String,
        performDecompression: Boolean
    ) {
        val decompFileName = "$fileName.decomp$idxStr"
        val productCode: Short = if (prod == "L2VEL") 154 else 153
        if (performDecompression) {
            try {
                val dis = UCARRandomAccessFile(
                    UtilityIO.getFilePath(context, fileName),
                    "r",
                    1024 * 256 * 10
                )
                dis.bigEndian = true
                dis.close()
                NexradLevel2Util.decompress(context, fileName, decompFileName, productCode.toInt())
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            } catch (e: OutOfMemoryError) {
                UtilityLog.handleException(e)
            }
        }
        radialStartAngle.order(ByteOrder.nativeOrder())
        radialStartAngle.position(0)
        binWord.order(ByteOrder.nativeOrder())
        binWord.position(0)
        days.order(ByteOrder.nativeOrder())
        days.position(0)
        msecs.order(ByteOrder.nativeOrder())
        msecs.position(0)
        try {
            if (performDecompression) {
                Level2.decode(
                    context,
                    decompFileName,
                    binWord,
                    radialStartAngle,
                    productCode.toInt(),
                    days,
                    msecs
                )
                if (!decompFileName.contains("l2")) {
                    NexradLevel2Util.writeDecodedFile(
                        context,
                        decompFileName + "bb",
                        binWord,
                        radialStartAngle,
                        days,
                        msecs
                    )
                }
            } else {
                NexradLevel2Util.readDecodedFile(
                    context,
                    decompFileName + "bb",
                    binWord,
                    radialStartAngle,
                    days,
                    msecs
                )
            }
            msecs.position(0)
            days.position(0)
            val days2 = days.short
            val milliSeconds = msecs.int
            val timeString = ObjectDateTime.radarTimeL2(days2, milliSeconds)
            val radarInfo =
                timeString + GlobalVariables.newline + "Product Code: " + productCode.toString()
            NexradUtil.writeRadarInfo(context, radarStatusStr, radarInfo)
            binSize = NexradUtil.getBinSize(productCode.toInt())
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        if (!RadarPreferences.useJni) {
            UtilityFileManagement.deleteFile(context, decompFileName)
        }
    }
}

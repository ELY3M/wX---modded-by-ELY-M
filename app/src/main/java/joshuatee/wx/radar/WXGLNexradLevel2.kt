/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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

import joshuatee.wx.JNI
import joshuatee.wx.MyApplication
import joshuatee.wx.util.*

class WXGLNexradLevel2 {

    val radialStartAngle: ByteBuffer = ByteBuffer.allocateDirect(720 * 4)
    var binSize: Float = 0f
        private set
    val numberOfRangeBins: Int = 916
    val binWord: ByteBuffer = ByteBuffer.allocateDirect(720 * numberOfRangeBins)
    private val days: ByteBuffer = ByteBuffer.allocateDirect(2)
    private val msecs: ByteBuffer = ByteBuffer.allocateDirect(4)
    private var obuff = ByteBuffer.allocate(0)
    private var ibuff = ByteBuffer.allocate(0)

    init {
        if (MyApplication.radarUseJni) {
            obuff = ByteBuffer.allocateDirect(829472)
            ibuff = ByteBuffer.allocateDirect(600000)
            // decomp size is a follows ref 827040 vel 460800
        }
    }

    // last argument is true/false on whether or not the DECOMP stage needs to happen
    fun decocodeAndPlotNexradL2(
        context: Context,
        fileName: String,
        prod: String,
        radarStatusStr: String,
        idxStr: String,
        performDecomp: Boolean
    ) {
        val decompFileName = "$fileName.decomp$idxStr"
        var productCode: Short = 153
        if (prod == "L2VEL") {
            productCode = 154
        }
        if (MyApplication.radarUseJni) {
            ibuff.position(0)
            obuff.position(0)
            try {
                JNI.level2Decompress(
                    UtilityIO.getFilePath(context, fileName),
                    UtilityIO.getFilePath(context, decompFileName),
                    ibuff,
                    obuff,
                    productCode.toInt()
                )
            } catch (e: Exception) {
                UtilityLog.HandleException(e)
            }
        } else {
            if (performDecomp) {
                try {
                    val dis = UCARRandomAccessFile(
                        UtilityIO.getFilePath(context, fileName),
                        "r",
                        1024 * 256 * 10
                    )
                    dis.bigEndian = true
                    dis.close()
                    UtilityWXOGLPerfL2.level2Decompress(
                        context,
                        fileName,
                        decompFileName,
                        productCode.toInt()
                    )
                } catch (e: Exception) {
                    UtilityLog.HandleException(e)
                } catch (e: OutOfMemoryError) {
                    UtilityLog.HandleException(e)
                }
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
            if (MyApplication.radarUseJni) {
                JNI.level2Decode(
                    UtilityIO.getFilePath(context, decompFileName),
                    binWord,
                    radialStartAngle,
                    productCode.toInt(),
                    days,
                    msecs
                )
            } else {
                if (performDecomp) {
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
                        UtilityWXOGLPerfL2.writeDecodedFile(
                            context,
                            decompFileName + "bb",
                            binWord,
                            radialStartAngle,
                            days,
                            msecs
                        )
                    }
                } else {
                    UtilityWXOGLPerfL2.readDecodedFile(
                        context,
                        decompFileName + "bb",
                        binWord,
                        radialStartAngle,
                        days,
                        msecs
                    )
                }
            }
            msecs.position(0)
            days.position(0)
            val days2 = days.short
            val msecs2 = msecs.int
            val d = UtilityTime.radarTimeL2(days2, msecs2)
            val radarInfo =
                d.toString() + MyApplication.newline + "Product Code: " + productCode.toInt().toString()
            Utility.writePref(context, "WX_RADAR_CURRENT_INFO$radarStatusStr", radarInfo)
            binSize = WXGLNexrad.getBinSize(productCode.toInt())
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        if (!MyApplication.radarUseJni) UtilityFileManagement.deleteFile(context, decompFileName)
    }
}



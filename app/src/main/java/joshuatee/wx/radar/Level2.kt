/*
 * Copyright 1998-2009 University Corporation for Atmospheric Research/Unidata
 *
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation.  Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package joshuatee.wx.radar

// The following has chunks of code from Level2VolumeScan.java so using the license for that file
// This file has now been extensively modified from the original

import java.nio.ByteBuffer

import android.content.Context

import joshuatee.wx.util.UCARRandomAccessFile
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityLog

internal object Level2 {
    /* added for high resolution message type 31 */
    private const val REFLECTIVITY_HIGH = 5
    /**
     * High Resolution Radial Velocity moment identifier
     */
    private const val VELOCITY_HIGH = 6
    /**
     * Size of the file header, aka title
     */
    private const val FILE_HEADER_SIZE = 24
    private var first: Level2Record? = null
    private var vcp = 0

    fun decode(context: Context, fileName: String, binWord: ByteBuffer, radialStartAngle: ByteBuffer, prod: Int, days: ByteBuffer, milliSeconds: ByteBuffer) {
        val velocityProd = prod == 154
        try {
            val ucarRandomAccessFile = UCARRandomAccessFile(UtilityIO.getFilePath(context, fileName), "r", 1024 * 256 * 10)
            ucarRandomAccessFile.bigEndian = true
            ucarRandomAccessFile.let {
                it.setBufferSize(2621440) // 1024*256*10
                it.bigEndian = true
                it.seek(FILE_HEADER_SIZE.toLong())
            }
            val highReflectivity = mutableListOf<Level2Record>()
            val highVelocity = mutableListOf<Level2Record>()
            var messageOffset31: Long = 0
            var recordNumber = 0
            while (true) {
                val r = Level2Record.factory(ucarRandomAccessFile, recordNumber, messageOffset31) ?: break
                recordNumber += 1
                if (r.messageType.toInt() == 31) messageOffset31 += (r.messageSize * 2 + 12 - 2432)
                if (r.messageType.toInt() != 1 && r.messageType.toInt() != 31) continue
                if (vcp == 0) vcp = r.vcp.toInt()
                if (first == null) first = r
                if (r.messageType.toInt() == 31) {
                    if (r.hasHighResREFData) highReflectivity.add(r)
                }
                if (r.hasHighResVELData) highVelocity.add(r)
            }
            val numberOfRadials = 720
            days.position(0)
            days.putShort(highReflectivity[1].dataJulianDate)
            milliSeconds.position(0)
            milliSeconds.putInt(highReflectivity[1].dataMsecs)
            if (!velocityProd) {
                for (r in 0 until numberOfRadials) {
                    if (highReflectivity[r].elevationNum.toInt() == 1) {
                        radialStartAngle.putFloat(450.0f - highReflectivity[r].azimuth)
                        highReflectivity[r].readData(ucarRandomAccessFile, REFLECTIVITY_HIGH, binWord)
                    }
                }
            } else {
                for (r in 0 until numberOfRadials) {
                    if (highVelocity[r].elevationNum.toInt() == 2) {
                        radialStartAngle.putFloat(450.0f - highVelocity[r].azimuth)
                        highVelocity[r].readData(ucarRandomAccessFile, VELOCITY_HIGH, binWord)
                    }
                }
            }
            ucarRandomAccessFile.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        } catch (e: OutOfMemoryError) {
            UtilityLog.handleException(e)
        }
    }
}

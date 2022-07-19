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

import java.io.IOException
import java.nio.ByteBuffer
import joshuatee.wx.util.UCARRandomAccessFile

internal class Level2Record @Throws(IOException::class)
private constructor(ucarRandomAccessFile: UCARRandomAccessFile, record: Int, messageOffset31: Long) {

    private val messageOffset: Long = (record * RADAR_DATA_SIZE).toLong() + FILE_HEADER_SIZE.toLong() + messageOffset31 // offset of start of message
    var hasHighResREFData = false
    var hasHighResVELData = false
    // message header
    var messageSize: Short = 0
    var messageType: Byte = 0
    var dataMsecs = 0
    var dataJulianDate: Short = 0
    var elevationNum: Short = 0
    var vcp: Short = 0
    var azimuth = 0.0f
    private var dbp1 = 0
    private var dbp4 = 0
    private var dbp5 = 0
    private var dbp6 = 0
    private var dbp7 = 0
    private var dbp8 = 0
    private var dbp9 = 0
    private var reflectHROffset: Short = 0
    private var velocityHROffset: Short = 0

    init {
        ucarRandomAccessFile.seek(messageOffset)
        ucarRandomAccessFile.skipBytes(CTM_HEADER_SIZE)
        messageSize = ucarRandomAccessFile.readShort() // size in "halfwords" = 2 bytes
        ucarRandomAccessFile.skipBytes(1)
        messageType = ucarRandomAccessFile.readByte()
        ucarRandomAccessFile.skipBytes(12)
        if (messageType.toInt() == 1) {
            // data header
            dataMsecs = ucarRandomAccessFile.readInt()   // collection time for this radial, msecs since midnight
            dataJulianDate = ucarRandomAccessFile.readShort() // prob "collection time"
            ucarRandomAccessFile.skipBytes(10)
            elevationNum = ucarRandomAccessFile.readShort() // RDA elevation number
            ucarRandomAccessFile.skipBytes(26)
            vcp = ucarRandomAccessFile.readShort() // volume coverage pattern
            ucarRandomAccessFile.skipBytes(20)
        } else if (messageType.toInt() == 31) {
            // data header
            ucarRandomAccessFile.skipBytes(4)
            dataMsecs = ucarRandomAccessFile.readInt()   // collection time for this radial, msecs since midnight
            dataJulianDate = ucarRandomAccessFile.readShort() // prob "collection time"
            ucarRandomAccessFile.skipBytes(2)
            azimuth = ucarRandomAccessFile.readFloat() // LOOK why unsigned ??
            ucarRandomAccessFile.skipBytes(6)
            elevationNum = ucarRandomAccessFile.readByte().toShort() // RDA elevation number
            ucarRandomAccessFile.skipBytes(9)
            dbp1 = ucarRandomAccessFile.readInt()
            ucarRandomAccessFile.skipBytes(8)
            dbp4 = ucarRandomAccessFile.readInt()
            dbp5 = ucarRandomAccessFile.readInt()
            dbp6 = ucarRandomAccessFile.readInt()
            dbp7 = ucarRandomAccessFile.readInt()
            dbp8 = ucarRandomAccessFile.readInt()
            dbp9 = ucarRandomAccessFile.readInt()
            vcp = getDataBlockValue(ucarRandomAccessFile, dbp1.toShort(), 40)
            var dbpp4 = 0
            var dbpp5 = 0
            if (dbp4 > 0) {
                val tname = getDataBlockStringValue(ucarRandomAccessFile, dbp4.toShort(), 1, 3)
                if (tname.startsWith("REF")) {
                    hasHighResREFData = true
                    dbpp4 = dbp4
                } else if (tname.startsWith("VEL")) {
                    hasHighResVELData = true
                    dbpp5 = dbp4
                }
            }
            if (dbp5 > 0) {
                val tname = getDataBlockStringValue(ucarRandomAccessFile, dbp5.toShort(), 1, 3)
                if (tname.startsWith("REF")) {
                    hasHighResREFData = true
                    dbpp4 = dbp5
                } else if (tname.startsWith("VEL")) {
                    hasHighResVELData = true
                    dbpp5 = dbp5
                }
            }
            if (dbp6 > 0) {
                val tname = getDataBlockStringValue(ucarRandomAccessFile, dbp6.toShort(), 1, 3)
                if (tname.startsWith("REF")) {
                    hasHighResREFData = true
                    dbpp4 = dbp6
                } else if (tname.startsWith("VEL")) {
                    hasHighResVELData = true
                    dbpp5 = dbp6
                }
            }
            if (dbp7 > 0) {
                val tname = getDataBlockStringValue(ucarRandomAccessFile, dbp7.toShort(), 1, 3)
                if (tname.startsWith("REF")) {
                    hasHighResREFData = true
                    dbpp4 = dbp7
                } else if (tname.startsWith("VEL")) {
                    hasHighResVELData = true
                    dbpp5 = dbp7
                }
            }
            if (dbp8 > 0) {
                val tname = getDataBlockStringValue(ucarRandomAccessFile, dbp8.toShort(), 1, 3)
                if (tname.startsWith("REF")) {
                    hasHighResREFData = true
                    dbpp4 = dbp8
                } else if (tname.startsWith("VEL")) {
                    hasHighResVELData = true
                    dbpp5 = dbp8
                }
            }
            if (dbp9 > 0) {
                val tname = getDataBlockStringValue(ucarRandomAccessFile, dbp9.toShort(), 1, 3)
                if (tname.startsWith("REF")) {
                    hasHighResREFData = true
                    dbpp4 = dbp9
                } else if (tname.startsWith("VEL")) {
                    hasHighResVELData = true
                    dbpp5 = dbp9
                }
            }
            if (hasHighResREFData) reflectHROffset = (dbpp4 + 28).toShort()
            if (hasHighResVELData) velocityHROffset = (dbpp5 + 28).toShort()
        }
    }

    private fun getDataOffset(dataType: Int) = when (dataType) {
            REFLECTIVITY_HIGH -> reflectHROffset
            VELOCITY_HIGH -> velocityHROffset
            else -> Short.MIN_VALUE
        }

    @Throws(IOException::class)
    private fun getDataBlockValue(ucarRandomAccessFile: UCARRandomAccessFile, offset: Short, skip: Int): Short {
        val off = offset.toLong() + messageOffset + MESSAGE_HEADER_SIZE.toLong()
        ucarRandomAccessFile.seek(off)
        ucarRandomAccessFile.skipBytes(skip)
        return ucarRandomAccessFile.readShort()
    }

    @Throws(IOException::class)
    private fun getDataBlockStringValue(ucarRandomAccessFile: UCARRandomAccessFile, offset: Short, skip: Int, size: Int): String {
        val off = offset.toLong() + messageOffset + MESSAGE_HEADER_SIZE.toLong()
        ucarRandomAccessFile.seek(off)
        ucarRandomAccessFile.skipBytes(skip)
        val byteArray = ByteArray(size)
        for (i in 0 until size) { byteArray[i] = ucarRandomAccessFile.readByte() }
        return String(byteArray)
    }

    @Throws(IOException::class)
    fun readData(ucarRandomAccessFile: UCARRandomAccessFile, dataType: Int, binWord: ByteBuffer) {
        // offset is from "start of digital radar data message header"
        val offset = messageOffset + MESSAGE_HEADER_SIZE.toLong() + getDataOffset(dataType).toLong()
        ucarRandomAccessFile.seek(offset)
        for (i in 0..915) { binWord.put(ucarRandomAccessFile.readUnsignedByte().toByte()) }
    }

    companion object {
        /* added for high resolution message type 31 */
        private const val REFLECTIVITY_HIGH = 5
        /**
         * High Resolution Radial Velocity moment identifier
         */
        private const val VELOCITY_HIGH = 6
        /**
         * Size of the CTM record header
         */
        private const val CTM_HEADER_SIZE = 12
        /**
         * Size of the the message header, to start of the data message
         */
        private const val MESSAGE_HEADER_SIZE = 28
        /**
         * Size of the entire message, if its a radar data message
         */
        private const val RADAR_DATA_SIZE = 2432
        /**
         * Size of the file header, aka title
         */
        private const val FILE_HEADER_SIZE = 24

        @Throws(IOException::class)
        fun factory(din: UCARRandomAccessFile, record: Int, message_offset31: Long): Level2Record? {
            val offset = (record * RADAR_DATA_SIZE).toLong() + FILE_HEADER_SIZE.toLong() + message_offset31
            return if (offset >= din.length()) null else Level2Record(din, record, message_offset31)
        }
    }
}

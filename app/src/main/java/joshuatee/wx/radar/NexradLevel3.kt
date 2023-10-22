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

import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.content.Context
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.UCARRandomAccessFile
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityLog
import java.util.Locale

class NexradLevel3 internal constructor() {

    // https://www.roc.noaa.gov/WSR88D/BuildInfo/Files.aspx
    // https://www.roc.noaa.gov/wsr88d/PublicDocs/ICDs/2620001X.pdf

    var binWord: ByteBuffer = ByteBuffer.allocate(0)
        private set
    var radialStart: ByteBuffer = ByteBuffer.allocate(0)
        private set
    var halfword3132 = 0f
        private set
    var halfword47: Short = -120
        private set
    var halfword48: Short = 120
        private set
    var timestamp: String = ""
        private set
    var productCode: Short = 94
        private set
    var binSize = 0.0f
        private set
    var numberOfRangeBins: Short = 0
        private set
    var seekStart: Long = 0
        private set
    var compressedFileSize = 0
        private set
    var iBuff: ByteBuffer = ByteBuffer.allocate(0)
    var oBuff: ByteBuffer = ByteBuffer.allocate(0)
    var radarHeight = 0
    var degree = 0.0f
    private var operationalMode: Short = 0
    private var volumeCoveragePattern: Short = 0
    private var latitudeOfRadar = 0.0
    private var longitudeOfRadar = 0.0

    init {
        try {
            if (RadarPreferences.useJni) {
                oBuff = ByteBuffer.allocateDirect(1000000)
                oBuff.order(ByteOrder.nativeOrder())
            }
        } catch (e: OutOfMemoryError) {
            UtilityLog.handleException(e)
        }
    }

    private fun init4Bit(code: Short) {
        when (code.toInt()) {
            181 -> {
                binWord = ByteBuffer.allocateDirect(360 * 720)
                binWord.order(ByteOrder.nativeOrder())
                radialStart = ByteBuffer.allocateDirect(4 * 360)
                radialStart.order(ByteOrder.nativeOrder())
            }

            78, 80 -> {
                binWord = ByteBuffer.allocateDirect(360 * 592)
                binWord.order(ByteOrder.nativeOrder())
                radialStart = ByteBuffer.allocateDirect(4 * 360)
                radialStart.order(ByteOrder.nativeOrder())
            }

            37, 38, 41, 57 -> {
                binWord = ByteBuffer.allocateDirect(464 * 464)
                binWord.order(ByteOrder.nativeOrder())
                radialStart = ByteBuffer.allocateDirect(4 * 360)
                radialStart.order(ByteOrder.nativeOrder())
            }

            else -> {
                binWord = ByteBuffer.allocateDirect(360 * 230)
                binWord.order(ByteOrder.nativeOrder())
                radialStart = ByteBuffer.allocateDirect(4 * 360)
                radialStart.order(ByteOrder.nativeOrder())
            }
        }
    }

    // fig 3-6 on 3-32 in INTERFACE CONTROL DOCUMENT FOR THE RPG TO CLASS 1 USER
    // RPG Build 14.0 Build Date 1/3/2014 Document Number 2620001U Code Identification 0WY55 WSR-88D ROC
    // TODO consume additional header data
    // ICD Can be found at: https://www.roc.noaa.gov/WSR88D/BuildInfo/Files.aspx
    // Latest is Build 18.0	released on 18 Jan 2018 stored at https://www.roc.noaa.gov/wsr88d/PublicDocs/ICDs/2620001X.pdf
    fun decodeAndPlot(context: Context, fileName: String, site: String, radarStatus: String) {
        try {
            val dis = UCARRandomAccessFile(UtilityIO.getFilePath(context, fileName))
            dis.bigEndian = true
            // ADVANCE PAST WMO HEADER
            while (true) {
                if (dis.readShort().toInt() == -1) {
                    break
                }
            }
            // the following chunk was added to analyze the header so that status info could be extracted
            // index 4 is radar height
            // index 0,1 is lat as Int
            // index 2,3 is long as Int
            latitudeOfRadar = dis.readInt() / 1000.0
            longitudeOfRadar = dis.readInt() / 1000.0
            radarHeight = dis.readUnsignedShort()
            productCode = dis.readUnsignedShort().toShort()
            // TODO FIXME hack for n0b and n0g using same product code as Level 2
            if (productCode == 153.toShort()) {
                productCode = 2153
            }
            if (productCode == 154.toShort()) {
                productCode = 2154
            }
            operationalMode = dis.readUnsignedShort().toShort()
            volumeCoveragePattern = dis.readUnsignedShort().toShort()
            val sequenceNumber = dis.readUnsignedShort().toShort()
            val volumeScanNumber = dis.readUnsignedShort().toShort()
            val volumeScanDate = dis.readUnsignedShort().toShort()
            val volumeScanTime = dis.readInt()
            writeTime(context, volumeScanDate, volumeScanTime, radarStatus, site)
            // Because the scale for storm total precip ( 172 ) is stored as a float in halfwords 33/34
            // it is necessary to further dissect the header. Previously we skipped 74 bytes
            // hw 24-30
            dis.skipBytes(10)
            val elevationNumber = dis.readUnsignedShort()
            val elevationAngle = dis.readShort()
            degree = elevationAngle.toInt() / 10.0f
            // hw 31-32 as a int
            //final int             halfword_31 = dis.readUnsignedShort();
            halfword3132 = dis.readFloat()
            // hw 33-34 as a int
            dis.skipBytes(28)
            // velocity product 99 has 47 ( max neg vel ) and 48 ( max pos vel )
            halfword47 = dis.readUnsignedShort().toShort()
            halfword48 = dis.readUnsignedShort().toShort()
            dis.skipBytes(24)
            seekStart = dis.filePointer
            if (RadarPreferences.useJni) {
                compressedFileSize = (dis.length() - dis.filePointer).toInt()
                iBuff = ByteBuffer.allocateDirect(compressedFileSize)
                iBuff.order(ByteOrder.nativeOrder())
            }
            dis.close()
            binSize = NexradUtil.getBinSize(productCode.toInt())
            numberOfRangeBins = NexradUtil.getNumberRangeBins(productCode.toInt())
        } catch (e: IOException) {
            UtilityLog.handleException(e)
        } catch (e: OutOfMemoryError) {
            UtilityLog.handleException(e)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    private fun writeTime(context: Context, volumeScanDate: Short, volumeScanTime: Int, radarStatus: String, site: String) {
        val date = ObjectDateTime.radarTime(volumeScanDate, volumeScanTime)
        val radarInfo = formatRadarString(date)
        NexradUtil.writeRadarInfo(context, radarStatus, radarInfo)
        // decodeAndPlotFourBit did not 2nd this 2nd write
        NexradUtil.writeRadarInfo(context, radarStatus + site.uppercase(Locale.US), radarInfo)
        timestamp = radarInfo
    }

    // Used for Legacy 4bit radar - SRM, comp ref
    fun decodeAndPlotFourBit(context: Context, fileName: String, site: String, radarStatus: String) {
        try {
            val fis = context.openFileInput(fileName)
            val dis = DataInputStream(BufferedInputStream(fis))
            dis.skipBytes(30)
            /*final short	message_code      = (short) dis.readUnsignedShort();
        final short    date_of_message  = (short) dis.readUnsignedShort();
        final int   time_of_message  = dis.readInt();
        final int   length_of_message  = dis.readInt();
        final short    source_id         = (short) dis.readUnsignedShort();
        final short   destination_id   = (short) dis.readUnsignedShort();
        final short  number_of_blocks = (short) dis.readUnsignedShort();
        final short  header2 = (short) dis.readUnsignedShort();*/
            dis.skipBytes(20)
            latitudeOfRadar = dis.readInt() / 1000.0
            longitudeOfRadar = dis.readInt() / 1000.0
            radarHeight = dis.readUnsignedShort()
            productCode = dis.readUnsignedShort().toShort()
            init4Bit(productCode)
            operationalMode = dis.readUnsignedShort().toShort()
            volumeCoveragePattern = dis.readUnsignedShort().toShort()
            val sequenceNumber = dis.readUnsignedShort().toShort()
            val volumeScanNumber = dis.readUnsignedShort().toShort()
            val volumeScanDate = dis.readUnsignedShort().toShort()
            val volumeScanTime = dis.readInt()
            writeTime(context, volumeScanDate, volumeScanTime, radarStatus, site)
            val productGenerationDate = dis.readUnsignedShort().toShort()
            val productGenerationTime = dis.readInt()
            //final short        product_generation_date = (short) dis.readUnsignedShort();
            //final int        product_generation_time    = dis.readInt() ;
            //dis.skipBytes(6)

            /*final short  p1                        = (short) dis.readUnsignedShort();
        final short        p2                        = (short) dis.readUnsignedShort();
        final short        elevation_number         = (short) dis.readUnsignedShort();
        final short        p3                        = (short) dis.readUnsignedShort();
        final short        data_threshold1           = (short) dis.readUnsignedShort();
        final short        data_threshold2          = (short) dis.readUnsignedShort();
        final short        data_threshold3          = (short) dis.readUnsignedShort();
        final short        data_threshold4           = (short) dis.readUnsignedShort();
        final short        data_threshold5           = (short) dis.readUnsignedShort();
        final short        data_threshold6          = (short) dis.readUnsignedShort();
        final short        data_threshold7           = (short) dis.readUnsignedShort();
        final short        data_threshold8           = (short) dis.readUnsignedShort();
        final short        data_threshold9           = (short) dis.readUnsignedShort();
        final short        data_threshold10          = (short) dis.readUnsignedShort();
        final short        data_threshold11          = (short) dis.readUnsignedShort();
        final short        data_threshold12           = (short) dis.readUnsignedShort();
        final short        data_threshold13         = (short) dis.readUnsignedShort();
        final short        data_threshold14          = (short) dis.readUnsignedShort();
        final short        data_threshold15          = (short) dis.readUnsignedShort();
        final short        data_threshold16           = (short) dis.readUnsignedShort();
        final short        p4                        = (short) dis.readUnsignedShort();
        final short        p5                        = (short) dis.readUnsignedShort();
        final short        p6                        = (short) dis.readUnsignedShort();
        final short        p7                        = (short) dis.readUnsignedShort();
        final short        p8                        = (short) dis.readUnsignedShort();
        final short        p9                        = (short) dis.readUnsignedShort();
        final short        p10                        = (short) dis.readUnsignedShort();
        final short        number_of_maps             = (short) dis.readUnsignedShort();*/
            dis.skipBytes(56) // 28 ushorts above ( x2 )
            //final int  offset_to_symbology_block =  dis.readInt() ;
            //final int  offset_to_graphic_block = dis.readInt() ;
            //final int  offset_to_tabular_block = dis.readInt() ;
            //   #Product Symbology Block
            //final short  h1   = (short) dis.readUnsignedShort();
            //final short  h1b   = (short) dis.readUnsignedShort();
            //final int  length_of_block   = dis.readInt() ;
            //final short  number_of_layers   = (short) dis.readUnsignedShort();
            //final short  h2   = (short) dis.readUnsignedShort();
            //final int  smbology_length  = dis.readInt() ;
            // RDP
            //final int  header_before_radial  = dis.readUnsignedShort() ;
            //final int  index_of_first_range_bin  = dis.readUnsignedShort() ;
            dis.skipBytes(32)
            dis.close()
            numberOfRangeBins = if (productCode.toInt() == 37 || productCode.toInt() == 38 || productCode.toInt() == 41 || productCode.toInt() == 57) {
                NexradDecodeFourBit.raster(context, fileName, binWord)
            } else {
                NexradDecodeFourBit.radial(context, fileName, radialStart, binWord)
            }
            binSize = NexradUtil.getBinSize(productCode.toInt())
        } catch (e: IOException) {
            UtilityLog.handleException(e)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    private fun formatRadarString(date: String): String =
            date + GlobalVariables.newline +
                    "Radar Mode: " + operationalMode + GlobalVariables.newline +
                    "VCP: " + volumeCoveragePattern + GlobalVariables.newline +
                    "Product Code: " + productCode + GlobalVariables.newline +
                    "Radar height: " + radarHeight + GlobalVariables.newline +
                    "Radar Lat: " + latitudeOfRadar + GlobalVariables.newline +
                    "Radar Lon: " + longitudeOfRadar

}

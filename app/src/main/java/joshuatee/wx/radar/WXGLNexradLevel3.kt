/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
//modded by ELY M.

package joshuatee.wx.radar

import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Date

import android.content.Context

import joshuatee.wx.MyApplication
import joshuatee.wx.util.*

class WXGLNexradLevel3 {

    // https://www.roc.noaa.gov/WSR88D/BuildInfo/Files.aspx
    // https://www.roc.noaa.gov/wsr88d/PublicDocs/ICDs/2620001X.pdf

    var binWord: ByteBuffer = ByteBuffer.allocate(0)
        private set
    var radialStart: ByteBuffer = ByteBuffer.allocate(0)
        private set
    var halfword3132: Float = 0f
        private set
    var halfword47: Short = -120
        private set
    var halfword48: Short = 120
        private set
    var timestamp: String = ""
        private set
    var productCode: Short = 94
        private set
    var binSize: Float = 0f
        private set
    var numberOfRangeBins: Short = 0
        private set
    var seekStart: Long = 0
        private set
    var compressedFileSize: Int = 0
        private set
    var iBuff: ByteBuffer = ByteBuffer.allocate(0)
    var oBuff: ByteBuffer = ByteBuffer.allocate(0)

    val productSpecific = IntArray(10)
    companion object {
        var volumeScanPattern: Int = 0
        var radarHeight: Int = 0
        var radarElevation: Int = 0
        var getElevation: Float = 0f
        var productSpecific2: Int = 0
    }

    init {
        try {
            if (MyApplication.radarUseJni) {
                oBuff = ByteBuffer.allocateDirect(1000000)
                oBuff.order(ByteOrder.nativeOrder())
            }
        } catch (e: OutOfMemoryError) {
            UtilityLog.HandleException(e)
        }
    }

    private fun initData4Bit() {
        binWord = ByteBuffer.allocateDirect(360 * 230)
        binWord.order(ByteOrder.nativeOrder())
        radialStart = ByteBuffer.allocateDirect(4 * 360)
        radialStart.order(ByteOrder.nativeOrder())
    }

    // final argument is whether or not to handle decompression, by default true
    fun decocodeAndPlotNexradDigital(context: Context, fileName: String, radarStatusStr: String) {
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
            val latitudeOfRadar = dis.readInt() / 1000.0
            val longitudeOfRadar = dis.readInt() / 1000.0
            val heightOfRadar = dis.readUnsignedShort().toShort()
            radarHeight = heightOfRadar.toInt()
            productCode = dis.readUnsignedShort().toShort()
            val operationalMode = dis.readUnsignedShort().toShort()
            volumeScanPattern =  dis.readUnsignedShort()
            UtilityLog.d("wx", "VCP: "+ volumeScanPattern)
            val seqnumber = dis.readUnsignedShort()
            val scannumber = dis.readUnsignedShort()
            //val volscandate = dis.readUnsignedShort()
            //val volscantime = dis.readInt()
            //dis.skipBytes(6)
            val volumeScanDate = dis.readUnsignedShort().toShort()
            val volumeScanTime = dis.readInt()
            val gendate = dis.readUnsignedShort()
            val gentime = dis.readInt()
            // Get first 2 product specific codes (halfwords 27 and 28)
            for (i in 0..1) {
                productSpecific[i] = dis.readUnsignedShort()
                UtilityLog.d("wx", "productSpecific["+i+"]: "+productSpecific[i])
            }
            // Get elevation number
            radarElevation = dis.readUnsignedShort()
            UtilityLog.d("wx", "RadarElevation: "+ radarElevation)
            productSpecific[2] = dis.readUnsignedShort()
            UtilityLog.d("wx", "productSpecific[2]: "+productSpecific[2])
            productSpecific2 = productSpecific[2]
            getElevation = (productSpecific[2] / 10.0f)
            val d = UtilityTime.radarTime(volumeScanDate, volumeScanTime)
            val radarInfo = formatRadarString(
                d,
                operationalMode.toInt(),
                volumeScanPattern,
                productCode.toInt(),
                heightOfRadar.toInt(),
                radarElevation,
                productSpecific2,
                getElevation.toInt(),
                latitudeOfRadar,
                longitudeOfRadar
            )
            Utility.writePref(context, "WX_RADAR_CURRENT_INFO$radarStatusStr", radarInfo)
            timestamp = radarInfo
            // Apr 2016
            // Because the scale for storm total precip ( 172 ) is stored as a float in halfwords 33/34
            // it is necessary to further disect the header. Previously we skipped 74 bytes
            // hw 24-30
            dis.skipBytes(14)
            // hw 31-32 as a int
            //final int             halfword_31 = dis.readUnsignedShort();
            halfword3132 = dis.readFloat()
            // hw 33-34 as a int
            //float halfword_33_34 = dis.readFloat();
            dis.skipBytes(28) // was 26
            // velocity product 99 has 47 ( max neg vel ) and 48 ( max pos vel )
            halfword47 = dis.readUnsignedShort().toShort()
            halfword48 = dis.readUnsignedShort().toShort()
            dis.skipBytes(24) // was 28
            seekStart = dis.filePointer
            if (MyApplication.radarUseJni) {
                compressedFileSize = (dis.length() - dis.filePointer).toInt()
                iBuff = ByteBuffer.allocateDirect(compressedFileSize)
                iBuff.order(ByteOrder.nativeOrder())
            }
            dis.close()
            binSize = WXGLNexrad.getBinSize(productCode.toInt())
            numberOfRangeBins = WXGLNexrad.getNumberRangeBins(productCode.toInt())
        } catch (e: IOException) {
            UtilityLog.HandleException(e)
        } catch (e: OutOfMemoryError) {
            UtilityLog.HandleException(e)
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
    }

    // Used for Legacy 4bit radar - only SRM and Spectrum Width
    fun decocodeAndPlotNexradLevel3FourBit(context: Context, fn: String, radarStatusStr: String) {
        initData4Bit()
        try {
            val fis = context.openFileInput(fn)
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
            val latitudeOfRadar = dis.readInt() / 1000.0
            val longitudeOfRadar = dis.readInt() / 1000.0
            val heightOfRadar = dis.readUnsignedShort().toShort()
            radarHeight = heightOfRadar.toInt()
            productCode = dis.readUnsignedShort().toShort()
            val operationalMode = dis.readUnsignedShort().toShort()
            volumeScanPattern =  dis.readUnsignedShort()
            UtilityLog.d("wx", "4-bit VCP: "+ volumeScanPattern)
            val seqnumber = dis.readUnsignedShort()
            val scannumber = dis.readUnsignedShort()
            //val volscandate = dis.readUnsignedShort()
            //val volscantime = dis.readInt()
            //dis.skipBytes(6)
            val volumeScanDate = dis.readUnsignedShort().toShort()
            val volumeScanTime = dis.readInt()
            // Get first 2 product specific codes (halfwords 27 and 28)
            for (i in 0..1) {
                productSpecific[i] = dis.readUnsignedShort()
                UtilityLog.d("wx", "4-bit productSpecific["+i+"]: "+productSpecific[i])
            }
            // Get elevation number
            radarElevation = dis.readUnsignedShort()
            UtilityLog.d("wx", "4-bit RadarElevation: "+ radarElevation)
            productSpecific[2] = dis.readUnsignedShort()
            UtilityLog.d("wx", "productSpecific[2]: "+productSpecific[2])
            productSpecific2 = productSpecific[2]
            getElevation = (productSpecific[2] / 10.0f)

            val d = UtilityTime.radarTime(volumeScanDate, volumeScanTime)
            //final short        product_generation_date = (short) dis.readUnsignedShort();
            //final int        product_generation_time    = dis.readInt() ;
            dis.skipBytes(6)
            val radarInfo = formatRadarString(
                d,
                operationalMode.toInt(),
                volumeScanPattern,
                productCode.toInt(),
                heightOfRadar.toInt(),
                radarElevation,
                productSpecific2,
                getElevation.toInt(),
                latitudeOfRadar,
                longitudeOfRadar
            )
            Utility.writePref(context, "WX_RADAR_CURRENT_INFO$radarStatusStr", radarInfo)
            timestamp = radarInfo
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
            numberOfRangeBins =
                UtilityWXOGLPerfL3FourBit.decode4Bit(context, fn, radialStart, binWord)
            binSize = WXGLNexrad.getBinSize(productCode.toInt())
        } catch (e: IOException) {
            UtilityLog.HandleException(e)
        }
    }

    private fun formatRadarString(
        d: Date,
        operationalMode: Int,
        volumeScanPattern: Int,
        productCode: Int,
        heightOfRadar: Int,
        radarElevation: Int,
        productSpecific2: Int,
        getElevation: Int,
        latitudeOfRadar: Double,
        longitudeOfRadar: Double
    ): String {
        return try {
            d.toString() + MyApplication.newline +
                    "Radar Mode: " + operationalMode + MyApplication.newline +
                    "VCP: " + volumeScanPattern + MyApplication.newline +
                    "Product Code: " + productCode + MyApplication.newline +
                    "Radar height: " + heightOfRadar + MyApplication.newline +
                    "Elevation Num: " + radarElevation + MyApplication.newline +
                    "productSpecific[2]: " + productSpecific2 + MyApplication.newline +
                    "getElevation: " + getElevation + MyApplication.newline +
                    "Radar Lat: " + latitudeOfRadar + MyApplication.newline +
                    "Radar Lon: " + longitudeOfRadar
        } catch (e: AssertionError) {
            ""
        }
    }
}



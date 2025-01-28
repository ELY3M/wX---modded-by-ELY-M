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

import joshuatee.wx.getHtml
import joshuatee.wx.getInputStream
import java.io.EOFException
import java.io.IOException
import joshuatee.wx.util.UtilityLog
import java.io.DataInputStream

object NexradLevel3TextProduct {

    fun download(product: String, radarSite: String): String =
        NexradDownload.getRadarFileUrl(radarSite, product).getHtml()

    fun getVwp(radarSite: String): String {
        // https://tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.48vwp/SI.kccx/
        val product = "VWP"
        val url = NexradDownload.getRadarFileUrl(radarSite, product)
        val inputStream = url.getInputStream()
        if (inputStream != null) {
            val dis = DataInputStream(inputStream)
            var output = "<font face=monospace><small>"
            try {
                // ADVANCE PAST WMO HEADER
                while (true) {
                    if (dis.readShort().toInt() == -1) {
                        break
                    }
                }
                dis.skipBytes(26) // 3 int (12) + 7*2 (14)
                while (true) {
                    if (dis.readShort().toInt() == -1) {
                        break
                    }
                }
                var byte: Byte?
                var vSpotted = false
                try {
                    while (true) {
                        byte = dis.readByte()
                        if (byte.toInt().toChar() == 'V') {
                            vSpotted = true
                        }
                        if (Character.isAlphabetic(byte.toInt()) || Character.isWhitespace(byte.toInt())
                            || Character.isDigit(byte.toInt()) || Character.isISOControl(byte.toInt()) || Character.isDefined(
                                byte.toInt()
                            )
                        ) {
                            if (vSpotted) {
                                output += if (byte == 0.toByte()) {
                                    "<br>"
                                } else {
                                    String(byteArrayOf(byte), charset("ISO-8859-1"))
                                }
                            }
                        }
                    }
                } catch (e: EOFException) {
                    // getting to this point is on purpose
                    // UtilityLog.handleException(e)
                } finally {
                    try {
                        dis.close()
                    } catch (e: IOException) {
                        UtilityLog.handleException(e)
                    }
                }
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
            output += "</small></font>"
            return output
        } else {
            return ""
        }
    }
}

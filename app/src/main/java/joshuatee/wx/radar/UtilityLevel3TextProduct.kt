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
import java.io.EOFException
import java.io.IOException
import joshuatee.wx.util.UCARRandomAccessFile
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityLog

object UtilityLevel3TextProduct {

    fun readFile(context: Context, fileName: String): String {
        val ucarRandomAccessFile = UCARRandomAccessFile(UtilityIO.getFilePath(context, fileName))
        ucarRandomAccessFile.bigEndian = true
        return read(ucarRandomAccessFile)
    }

    fun read(ucarRandomAccessFile: UCARRandomAccessFile?): String {
        val stringBuilder = StringBuilder(1500)
        try {
            ucarRandomAccessFile?.let {
                while (true) {
                    if (it.readShort().toInt() == -1) {
                        break
                    }
                }
                it.skipBytes(26)
                while (true) {
                    if (it.readShort().toInt() == -1) {
                        break
                    }
                }
                try {
                    while (!it.isAtEndOfFile) {
                        stringBuilder.append(String(byteArrayOf(it.readByte()), charset("ISO-8859-1")))
                    }
                } catch (e: EOFException) {
                    UtilityLog.handleException(e)
                } catch (e: OutOfMemoryError) {
                    UtilityLog.handleException(e)
                } finally {
                    try {
                        it.close()
                    } catch (e: IOException) {
                        UtilityLog.handleException(e)
                    }
                }
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return stringBuilder.toString()
    }
}

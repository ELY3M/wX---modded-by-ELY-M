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
//modded by ELY M. (readTextFile() cant be private....)

package joshuatee.wx.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.util.bzip2.Compression
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.BufferedReader
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.io.DataInputStream

object UtilityIO {

    fun uncompress(context: Context, fileName: String): DataInputStream {
        val dis = UCARRandomAccessFile(getFilePath(context, fileName))
        dis.bigEndian = true
        // ADVANCE PAST WMO HEADER
        while (dis.readShort().toInt() != -1) {
            // while (dis.readUnsignedShort() != 16) {
        }
        dis.skipBytes(100)
        val magic = ByteArray(3)
        magic[0] = 'B'.code.toByte()
        magic[1] = 'Z'.code.toByte()
        magic[2] = 'h'.code.toByte()
        val compression = Compression.getCompression(magic)
        val compressedFileSize = dis.length() - dis.filePointer
        val buf = ByteArray(compressedFileSize.toInt())
        dis.read(buf)
        dis.close()
        val decompressedStream = compression.decompress(ByteArrayInputStream(buf))
        return DataInputStream(BufferedInputStream(decompressedStream))
    }

    fun saveInputStream(context: Context, inputSteam: InputStream, filename: String) {
        try {
            val fos = context.openFileOutput(filename, Context.MODE_PRIVATE)
            val buffer = ByteArray(8192)
            var read = inputSteam.read(buffer)
            while (read != -1) {
                fos.write(buffer, 0, read)
                read = inputSteam.read(buffer)
            }
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    fun getFilePath(context: Context, fileName: String): String = context.getFileStreamPath(fileName).absolutePath

    fun readTextFileFromRaw(resources: Resources, fileRaw: Int) = readTextFile(resources.openRawResource(fileRaw))
    //elys mod - cant be private. it is used in other file.  
    fun readTextFile(inputStream: InputStream): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val byteArray = ByteArray(32768)
        try {
            var len = inputStream.read(byteArray)
            while (len != -1) {
                byteArrayOutputStream.write(byteArray, 0, len)
                len = inputStream.read(byteArray)
            }
            byteArrayOutputStream.close()
            inputStream.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return byteArrayOutputStream.toString()
    }

    fun saveRawToInternalStorage(context: Context, rawFileId: Int, intStorageFn: String) {
        try {
            val input = context.resources.openRawResource(rawFileId)
            val output = context.openFileOutput(intStorageFn, Context.MODE_PRIVATE)
            val byteArray = ByteArray(1024)
            var count = input.read(byteArray)
            while (count != -1) {
                output.write(byteArray, 0, count)
                count = input.read(byteArray)
            }
            output.flush()
            output.close()
            input.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    fun bitmapFromInternalStorage(context: Context, path: String): Bitmap {
        var bitmap = UtilityImg.getBlankBitmap()
        try {
            val inputSteam = context.openFileInput(path)
            bitmap = BitmapFactory.decodeStream(inputSteam)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        } catch (e: OutOfMemoryError) {
            UtilityLog.handleException(e)
        }
        return bitmap
    }

    fun bitmapToInternalStorage(context: Context, bitmap: Bitmap, path: String) {
        try {
            val fos = context.openFileOutput(path, Context.MODE_PRIVATE)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    fun readTextFromUri(context: Context, uri: Uri): String {
        var content = ""
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream!!))
            var line = reader.readLine()
            while (line != null) {
                content += line + GlobalVariables.newline
                line = reader.readLine()
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return content
    }

    fun getHtml(url: String) = UtilityNetworkIO.getStringFromUrl(url)
}

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

package joshuatee.wx.util

import java.io.ByteArrayOutputStream
import java.io.InputStream

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import joshuatee.wx.MyApplication
import java.io.BufferedReader
import java.io.InputStreamReader

object UtilityIO {

    fun saveInputStream(context: Context, inputSteam: InputStream, filename: String) {
        try {
            val fos = context.openFileOutput(filename, Context.MODE_PRIVATE)
            val buffer = ByteArray(8192)
            var read: Int
            read = inputSteam.read(buffer)
            while (read != -1) {
                fos.write(buffer, 0, read)
                read = inputSteam.read(buffer)
            }
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
    }

    fun getFilePath(context: Context, fileName: String): String =
        context.getFileStreamPath(fileName).absolutePath

    fun readTextFile(inputStream: InputStream): String {
        val outputStream = ByteArrayOutputStream()
        val buf = ByteArray(32768)
        var len: Int
        try {
            len = inputStream.read(buf)
            while (len != -1) {
                outputStream.write(buf, 0, len)
                len = inputStream.read(buf)
            }
            outputStream.close()
            inputStream.close()
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return outputStream.toString()
    }

    fun saveRawToInternalStorage(context: Context, rawFileId: Int, intStorageFn: String) {
        try {
            val input = context.resources.openRawResource(rawFileId)
            val output = context.openFileOutput(intStorageFn, Context.MODE_PRIVATE)
            val data = ByteArray(1024)
            var count: Int
            count = input.read(data)
            while (count != -1) {
                output.write(data, 0, count)
                count = input.read(data)
            }
            output.flush()
            output.close()
            input.close()
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
    }

    fun bitmapFromInternalStorage(context: Context, path: String): Bitmap {
        var bitmap: Bitmap = UtilityImg.getBlankBitmap()
        try {
            val inputSteam = context.openFileInput(path)
            bitmap = BitmapFactory.decodeStream(inputSteam)
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        } catch (e: OutOfMemoryError) {
            UtilityLog.HandleException(e)
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
            UtilityLog.HandleException(e)
        }
    }

    fun readTextFromUri(context: Context, uri: Uri): String {
        var content = ""
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream!!))
            var line = reader.readLine()
            while (line != null) {
                content += line + MyApplication.newline
                line = reader.readLine()
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return content
    }
}

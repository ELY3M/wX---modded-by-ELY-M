/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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

import android.content.Context

import java.io.File

object UtilityFileManagement {

    fun deleteCacheFiles(context: Context): String {
        var fileList = ""
        val files = context.fileList()
        files.forEach {
            if (!it.contains("colormap") && !it.contains("shared")) {
                fileList += "$it "
                context.deleteFile(it)
            }
        }
        return fileList
    }

    fun deleteFile(context: Context, fileName: String) {
        context.deleteFile(fileName)
    }

    fun internalFileExist(context: Context, path: String) = context.getFileStreamPath(path).exists()

    fun moveFile(context: Context, src: String, target: String) {
        val file = File(context.filesDir, src)
        if (!file.renameTo(File(context.filesDir, target))) UtilityLog.d("wx", "Problem moving file to $target")
    }

    fun moveFile(context: Context, src: String, target: String, moveSize: Int) {
        val file = File(context.filesDir, src)
        if (file.length() > moveSize) {
            if (!file.renameTo(File(context.filesDir, target))) UtilityLog.d("wx", "Problem moving file to $target")
        }
    }
}

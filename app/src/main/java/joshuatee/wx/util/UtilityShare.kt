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

import java.io.File
import java.io.FileOutputStream

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import joshuatee.wx.MyApplication

import joshuatee.wx.ui.UtilityUI

object UtilityShare {

    fun shareTextAsAttachment(context: Context, subject: String, text: String, filename: String) {
        val dir = File(context.filesDir.toString() + "/shared")
        if (!dir.mkdirs())
            UtilityLog.d("wx", "failed to mkdir: " + context.filesDir + "/shared")
        val file = File(dir, filename)
        val imgUri = FileProvider.getUriForFile(
            context,
            "${MyApplication.packageNameAsString}.fileprovider",
            file
        )
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            fos.write(text.toByteArray(), 0, text.toByteArray().size)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        } finally {
            if (fos != null)
                try {
                    fos.close()
                } catch (e: Exception) {
                    UtilityLog.HandleException(e)
                }
        }
        val formattedDate = UtilityTime.getDateAsString("yyyy-MM-dd HH:mm:ss")
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "$subject $formattedDate")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, "wX Settings attached")
        sharingIntent.putExtra(Intent.EXTRA_STREAM, imgUri)//attachment
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    fun shareText(context: Context, subject: String, text: String) {
        val formattedDate = UtilityTime.getDateAsString("yyyy-MM-dd HH:mm:ss")
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "$subject $formattedDate")
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    fun shareText(context: Context, subject: String, text: String, bitmap: Bitmap) {
        val dir = File(context.filesDir.toString() + "/shared")
        if (!dir.mkdirs())
            UtilityLog.d("wx", "failed to mkdir: " + context.filesDir + "/shared")
        val file = File(dir, "img1.png")
        val imgUri = FileProvider.getUriForFile(
            context,
            "${MyApplication.packageNameAsString}.fileprovider",
            file
        )
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        val formattedDate = UtilityTime.getDateAsString("yyyy-MM-dd HH:mm:ss")
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "$subject $formattedDate")
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text)
        sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM, imgUri)
        sharingIntent.type = "image/png"
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    fun shareText(context: Context, subject: String, text: String, bitmapArr: List<Bitmap>) {
        val imgUriAl = ArrayList<Uri>()
        bitmapArr.forEachIndexed { i, bm ->
            val dir = File(context.filesDir.toString() + "/shared")
            if (!dir.mkdirs()) {
                UtilityLog.d("wx", "failed to mkdir: " + context.filesDir + "/shared")
            }
            val file = File(dir, "img" + i.toString() + ".png")
            val imgUri = FileProvider.getUriForFile(
                context,
                "${MyApplication.packageNameAsString}.fileprovider",
                file
            )
            imgUriAl.add(imgUri)
            try {
                val fos = FileOutputStream(file)
                bm.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.close()
            } catch (e: Exception) {
                UtilityLog.HandleException(e)
            }
        }
        val formattedDate = UtilityTime.getDateAsString("yyyy-MM-dd HH:mm:ss")
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND_MULTIPLE)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "$subject $formattedDate")
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text)
        sharingIntent.putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, imgUriAl)
        sharingIntent.type = "image/png"
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    fun shareBitmap(context: Context, subject: String, bitmap: Bitmap) {
        val dir = File(context.filesDir.toString() + "/shared")
        if (!dir.mkdirs())
            UtilityLog.d("wx", "failed to mkdir: " + context.filesDir + "/shared")
        val file = File(dir, "img1.png")
        val imgUri = FileProvider.getUriForFile(
            context,
            "${MyApplication.packageNameAsString}.fileprovider",
            file
        )
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        val formattedDate = UtilityTime.getDateAsString("yyyy-MM-dd HH:mm:ss")
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "$subject $formattedDate")
        sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM, imgUri)
        sharingIntent.type = "image/png"
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    internal var animDrawablePublic: AnimationDrawable? = null
    internal var contextPublic: Context? = null
    internal var subjectPublic: String? = null

    fun shareAnimGif(context: Context, subject: String, animDrawable: AnimationDrawable) {
        UtilityUI.makeToastLegacy(context, "Creating animated gif")
        contextPublic = context
        animDrawablePublic = animDrawable
        subjectPublic = subject
        val intent = Intent(context, CreateAnimatedGifService::class.java)
        context.startService(intent)
    }
}

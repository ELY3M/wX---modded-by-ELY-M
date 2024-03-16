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

package joshuatee.wx.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.ShareCompat.IntentBuilder
import java.io.File
import java.io.FileOutputStream
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.BitmapAttr
import joshuatee.wx.objects.ObjectDateTime
import joshuatee.wx.ui.Image
import joshuatee.wx.ui.TouchImage

object UtilityShare {

    fun prepTextForShare(text: String): String =
            text.replace(GlobalVariables.newline, GlobalVariables.newline + GlobalVariables.newline)

    fun textAsAttachment(context: Context, subject: String, text: String, filename: String) {
        val dir = File(context.filesDir.toString() + "/shared")
        if (!dir.mkdirs()) {
            UtilityLog.d("wx", "failed to mkdir: " + context.filesDir + "/shared")
        }
        val file = File(dir, filename)
        val imgUri = FileProvider.getUriForFile(context, "${GlobalVariables.PACKAGE_NAME}.fileprovider", file)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            fos.write(text.toByteArray(), 0, text.toByteArray().size)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: Exception) {
                    UtilityLog.handleException(e)
                }
            }
        }
        val formattedDate = ObjectDateTime.getDateAsString("yyyy-MM-dd HH:mm:ss")
        val intentBuilder = IntentBuilder(context)
        with(intentBuilder) {
            setSubject("$subject $formattedDate")
            addEmailTo("")
            setText(text)
            setStream(imgUri)
        }
        val sharingIntent = intentBuilder.intent
        sharingIntent.data = imgUri
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    fun text(context: Context, subject: String, text: String) {
        val formattedDate = ObjectDateTime.getDateAsString("yyyy-MM-dd HH:mm:ss")
        val sharingIntent = Intent(Intent.ACTION_SEND)
        with(sharingIntent) {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$subject $formattedDate")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    fun text(activity: Activity, subject: String, text: String, bitmaps: List<Bitmap>) {
        bitmap(activity, subject, UtilityImg.mergeImagesVertically(bitmaps), text)
    }

    fun textWithBitmapAttr(activity: Activity, subject: String, text: String, bitmapsAttr: List<BitmapAttr>) {
        bitmap(activity, subject, UtilityImg.mergeImagesVerticallyBitmapAttr(bitmapsAttr), text)
    }

    fun bitmap(activity: Activity, subject: String, image: Image, text: String = "") {
        bitmap(activity, subject, image.bitmap, text)
    }

    fun bitmap(activity: Activity, subject: String, image: TouchImage, text: String = "") {
        bitmap(activity, subject, image.bitmap, text)
    }

    fun bitmap(context: Context, subject: String, bitmap: Bitmap, text: String = "") {
        val dir = File(context.filesDir.toString() + "/shared")
        if (!dir.mkdirs()) {
            UtilityLog.d("wx", "failed to mkdir: " + context.filesDir + "/shared")
        }
        val file = File(dir, "img1.png")
        val imgUri = FileProvider.getUriForFile(context, "${GlobalVariables.PACKAGE_NAME}.fileprovider", file)
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        val formattedDate = ObjectDateTime.getDateAsString("yyyy-MM-dd HH:mm:ss")
        val intentBuilder = IntentBuilder(context)
        with(intentBuilder) {
            setSubject("$subject $formattedDate")
            addEmailTo("")
            setText(text)
            setStream(imgUri)
            setType("image/png")
        }
        val sharingIntent = intentBuilder.intent
        sharingIntent.data = imgUri
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (sharingIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(sharingIntent, "Share via"))
        }
    }
}

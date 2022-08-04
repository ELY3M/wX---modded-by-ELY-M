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

package joshuatee.wx.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.ShareCompat.IntentBuilder
import java.io.File
import java.io.FileOutputStream
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.objects.ObjectDateTime

object UtilityShare {

    fun prepTextForShare(text: String) = text.replace(GlobalVariables.newline, GlobalVariables.newline + GlobalVariables.newline)

    fun textAsAttachment(activity: Activity, subject: String, text: String, filename: String) {
        val dir = File(activity.filesDir.toString() + "/shared")
        if (!dir.mkdirs()) {
            UtilityLog.d("wx", "failed to mkdir: " + activity.filesDir + "/shared")
        }
        val file = File(dir, filename)
        val imgUri = FileProvider.getUriForFile(activity, "${GlobalVariables.packageNameAsString}.fileprovider", file)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            fos.write(text.toByteArray(), 0, text.toByteArray().size)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        } finally {
            if (fos != null)
                try {
                    fos.close()
                } catch (e: Exception) {
                    UtilityLog.handleException(e)
                }
        }
        val formattedDate = ObjectDateTime.getDateAsString("yyyy-MM-dd HH:mm:ss")
        val intentBuilder = IntentBuilder(activity)
        intentBuilder.setSubject("$subject $formattedDate")
        intentBuilder.addEmailTo("")
        intentBuilder.setText(text)
        intentBuilder.setStream(imgUri)
        val sharingIntent = intentBuilder.intent
        sharingIntent.data = imgUri
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        activity.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    fun text(context: Context, subject: String, text: String) {
        val formattedDate = ObjectDateTime.getDateAsString("yyyy-MM-dd HH:mm:ss")
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "$subject $formattedDate")
        sharingIntent.putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }

    fun text(activity: Activity, subject: String, text: String, bitmaps: List<Bitmap>) {
        val bitmap = UtilityImg.mergeImagesVertically(bitmaps)
        bitmap(activity, subject, bitmap, text)
    }

    fun bitmap(activity: Activity, subject: String, bitmap: Bitmap, text: String = "") {
        val dir = File(activity.filesDir.toString() + "/shared")
        if (!dir.mkdirs()) UtilityLog.d("wx", "failed to mkdir: " + activity.filesDir + "/shared")
        val file = File(dir, "img1.png")
        val imgUri = FileProvider.getUriForFile(activity, "${GlobalVariables.packageNameAsString}.fileprovider", file)
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        val formattedDate = ObjectDateTime.getDateAsString("yyyy-MM-dd HH:mm:ss")
        val intentBuilder = IntentBuilder(activity)
        intentBuilder.setSubject("$subject $formattedDate")
        intentBuilder.addEmailTo("")
        intentBuilder.setText(text)
        intentBuilder.setStream(imgUri)
        intentBuilder.setType("image/png")
        val sharingIntent = intentBuilder.intent
        sharingIntent.data = imgUri
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (sharingIntent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(Intent.createChooser(sharingIntent, "Share via"))
        }
    }
}

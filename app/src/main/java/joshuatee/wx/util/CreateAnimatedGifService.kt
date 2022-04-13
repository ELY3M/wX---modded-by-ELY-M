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

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import android.app.IntentService
import android.content.Intent
import joshuatee.wx.MyApplication

// this service notifies the alarm manager to run AlertReceiver ( notifications ) according to the
// configured interval

class CreateAnimatedGifService : IntentService("CreateAnimatedGifService") {

    override fun onHandleIntent(intent: Intent?) {
        val bos = ByteArrayOutputStream()
        val encoder = AnimatedGifEncoderExternal()
        encoder.setRepeat(0)
        encoder.setDelay(UtilityImg.animInterval(this))
        encoder.start(bos)
        (0 until UtilityShare.animDrawablePublic!!.numberOfFrames).forEach {
            encoder.addFrame(
                UtilityImg.drawableToBitmap(UtilityShare.animDrawablePublic!!.getFrame(it))
            )
        }
        encoder.finish()
        val dir = File("$filesDir/shared")
        if (!dir.mkdirs()) UtilityLog.d("wx", "unable to create: $filesDir/shared")
        val file = File(dir, "${MyApplication.packageNameFileNameAsString}_anim.gif")
        val contentUri = FileProvider.getUriForFile(
            this,
            "${MyApplication.packageNameAsString}.fileprovider",
            file
        )
        val outStream: FileOutputStream
        try {
            outStream = FileOutputStream(file)
            outStream.write(bos.toByteArray())
            outStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val formattedDate = UtilityTime.getDateAsString("yyyy-MM-dd HH:mm:ss")
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            UtilityShare.subjectPublic + " Animation" + " " + formattedDate
        )
        sharingIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        sharingIntent.type = "image/gif"
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }
}

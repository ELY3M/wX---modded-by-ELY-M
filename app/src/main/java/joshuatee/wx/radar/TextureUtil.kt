package joshuatee.wx.radar

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLUtils

import android.opengl.GLES20.GL_CLAMP_TO_EDGE
import android.opengl.GLES20.GL_LINEAR
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_TEXTURE_MAG_FILTER
import android.opengl.GLES20.GL_TEXTURE_MIN_FILTER
import android.opengl.GLES20.GL_TEXTURE_WRAP_S
import android.opengl.GLES20.GL_TEXTURE_WRAP_T
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glGenTextures
import android.opengl.GLES20.glTexParameteri

/**
 * @author wupanjie
 *
 * https://github.com/wuapnjie/DailyAndroid/blob/2ca2abd5e3688743bdc7784f9e97bce4e9a45280/RainDrop/app/src/main/java/com/xiaopo/flying/raindrop/TextureUtil.java
 */

object TextureUtil {

    fun loadTextureFromResource(File: String): Int {
        val options = BitmapFactory.Options()
        options.inScaled = false  // No pre-scaling
        //options.inSampleSize = 4;
        val bitmap = BitmapFactory.decodeFile(File, options)
        return loadTextureFromBitmap(bitmap, true)
    }


    fun loadTextureFromBitmap(bitmap: Bitmap, needRecycle: Boolean): Int {
        val textureHandle = IntArray(1)

        glGenTextures(1, textureHandle, 0)

        if (textureHandle[0] != 0) {
            glBindTexture(GL_TEXTURE_2D, textureHandle[0])
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)
            if (needRecycle) { bitmap.recycle() }
        }

        if (textureHandle[0] == 0) {
            throw RuntimeException("Error loading texture")
        }

        return textureHandle[0]
    }
}



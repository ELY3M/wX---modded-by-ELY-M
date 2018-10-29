package joshuatee.wx.radar

import android.graphics.Bitmap
import android.opengl.GLES10.GL_TEXTURE_ENV
import android.opengl.GLES10.GL_TEXTURE_ENV_MODE
import android.opengl.GLES20.*
import android.opengl.GLUtils
import android.util.Log
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import javax.microedition.khronos.opengles.GL10

class TextureOperations(logID: String) {

    companion object {
        private var TAG = "TextureOperations"
        private val verbose = true

        fun invalidateTexture(gl: GL10?, textures: IntArray): Boolean {
            if (gl != null) {
                val len = textures.size
                gl.glDeleteTextures(len, textures, 0)
                for (i in 0 until len) {
                    if (textures[i] != 0) {
                        if (verbose) {
                            Log.d(TAG, "GLDELTEX [" + textures[i] + "] ")
                        }
                        textures[i] = 0
                    }
                }
            }
            return false
        }

        fun invalidateTexture(gl: GL10?, textures: Array<IntArray>): Boolean {
            if (gl != null) {
                val len = textures[0].size
                for (k in textures.indices) {
                    gl.glDeleteTextures(len, textures[k], 0)
                    for (i in 0 until len) {
                        if (textures[k][i] != 0) {
                            if (verbose) {
                                Log.d(TAG, "GLDELTEX [" + textures[k][i] + "] ")
                            }
                            textures[k][i] = 0
                        }
                    }
                }
            }
            return false
        }



        fun loadTextureFromBitmapFast(gl: GL10, bitmap: Bitmap): Int {
            val tex = IntArray(1)
            gl.glGenTextures(1, tex, 0)
            gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
            gl.glBindTexture(GL_TEXTURE_2D, tex[0])
            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST.toFloat())
            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST.toFloat())
            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE.toFloat())
            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE.toFloat())
            gl.glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE.toFloat())
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)
            if (verbose) {
                Log.d(TAG, "GLGENTEX [" + tex[0] + "] Width=" + bitmap.width + " Height=" + bitmap.height)
            }
            return tex[0]
        }




        fun loadTextureFromBitmapFastSmoothed(gl: GL10, bitmap: Bitmap): Int {
            val tex = IntArray(1)
            gl.glGenTextures(1, tex, 0)
            gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
            gl.glBindTexture(GL_TEXTURE_2D, tex[0])
            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR.toFloat())
            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR.toFloat())
            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE.toFloat())
            gl.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE.toFloat())
            gl.glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE.toFloat())
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)
            if (verbose) {
                Log.d(TAG, "GLGENTEX [" + tex[0] + "] Width=" + bitmap.width + " Height=" + bitmap.height)
            }
            return tex[0]
        }

        fun restoreGifTransparency(bmp: Bitmap): Bitmap {
            System.gc()
            val length = bmp.width * bmp.height
            val pixels = IntArray(length)
            bmp.getPixels(pixels, 0, bmp.width, 0, 0, bmp.width, bmp.height)
            var i = 0
            while (i < length) {
                if (pixels[i] == -1) {
                    pixels[i] = 0
                }
                if (pixels[i] != 0 && colorCheck(pixels[i])) {
                    pixels[i] = 1879048192 + (pixels[i] and ViewCompat.MEASURED_SIZE_MASK)
                }
                i++
            }
            bmp.setPixels(pixels, 0, bmp.width, 0, 0, bmp.width, bmp.height)
            System.gc()
            return bmp
        }

        internal fun colorCheck(value: Int): Boolean {
            return if (withinRange((16711680 and value) / 65536, 232, 10) && withinRange((MotionEventCompat.ACTION_POINTER_INDEX_MASK and value) / 256, 232, 10) && withinRange(value and 255, 232, 10)) {
                true
            } else false
        }

        internal fun withinRange(value: Int, checkvalue: Int, variance: Int): Boolean {
            return if (Math.abs(value - checkvalue) <= variance) {
                true
            } else false
        }
    }
}

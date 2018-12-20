package joshuatee.wx.radar

import android.graphics.Bitmap
import android.opengl.GLUtils
import android.util.Log
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import javax.microedition.khronos.opengles.GL10

class TextureOperations(logID: String) {

    companion object {
        private var TAG = "TextureOperations"
        private val verbose = false

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
            gl.glPixelStorei(3317, 1)
            gl.glBindTexture(3553, tex[0])
            gl.glTexParameterf(3553, 10241, 9728.0f)
            gl.glTexParameterf(3553, 10240, 9728.0f)
            gl.glTexParameterf(3553, 10242, 33071.0f)
            gl.glTexParameterf(3553, 10243, 33071.0f)
            gl.glTexEnvf(8960, 8704, 7681.0f)
            GLUtils.texImage2D(3553, 0, bitmap, 0)
            if (verbose) {
                Log.d(TAG, "GLGENTEX [" + tex[0] + "] Width=" + bitmap.width + " Height=" + bitmap.height)
            }
            return tex[0]
        }

        fun loadTextureFromBitmapFastSmoothed(gl: GL10, bitmap: Bitmap): Int {
            val tex = IntArray(1)
            gl.glGenTextures(1, tex, 0)
            gl.glPixelStorei(3317, 1)
            gl.glBindTexture(3553, tex[0])
            gl.glTexParameterf(3553, 10241, 9729.0f)
            gl.glTexParameterf(3553, 10240, 9729.0f)
            gl.glTexParameterf(3553, 10242, 33071.0f)
            gl.glTexParameterf(3553, 10243, 33071.0f)
            gl.glTexEnvf(8960, 8704, 7681.0f)
            GLUtils.texImage2D(3553, 0, bitmap, 0)
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

package joshuatee.wx.radar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import android.util.DisplayMetrics
import android.util.Log
import joshuatee.wx.MyApplication
import java.io.File
import javax.microedition.khronos.opengles.GL10
import javax.microedition.khronos.opengles.GL11
import javax.microedition.khronos.opengles.GL11Ext

class IconLoad(iconfile: String, display_density: Float) {
    ///private static String iconfile = "star_cyan.png";
    private var BUSY: Boolean = false
    internal val TAG = "IconLoad"

    private val cropRect = IntArray(4)

    internal var context: Context? = null
    //DisplayMetrics dm = context.getResources().getDisplayMetrics();
    //private float display_density = dm.densityDpi;
    private var drawHeight: Int = 0
    private var drawWidth: Int = 0

    private val textures = IntArray(1)
    private var texturesLoaded = false


    init {
        FillCropRect(iconfile, display_density)
    }

    private fun FillCropRect(iconfile: String, display_density: Float) {
        Log.i(TAG, "FillCropRect")
        val options = Options()
        options.inScaled = false
        val file = File(MyApplication.FilesPath + iconfile)
        if (file.exists()) {
            Log.i(TAG, "file exists")
            val decodeFile = BitmapFactory.decodeFile(file.absolutePath, options)
            try {
                cropRect[0] = 0
                cropRect[1] = decodeFile.height
                cropRect[2] = decodeFile.width
                cropRect[3] = -decodeFile.height
                drawWidth = (cropRect[2].toFloat() * display_density * icon_sizing_factor).toInt()
                drawHeight = ((-cropRect[3]).toFloat() * display_density * icon_sizing_factor).toInt()
                decodeFile.recycle()

                val stringBuilder2 = StringBuilder()
                stringBuilder2.append("Croprect set to ")
                stringBuilder2.append(cropRect[0])
                stringBuilder2.append(" ")
                stringBuilder2.append(cropRect[1])
                stringBuilder2.append(" ")
                stringBuilder2.append(cropRect[2])
                stringBuilder2.append(" ")
                stringBuilder2.append(cropRect[3])
                stringBuilder2.append(" ")
                Log.i(TAG, stringBuilder2.toString())
                return
            } catch (e: Exception) {
                Log.e(TAG, "Exception setting the cropRect")
                e.printStackTrace()
                return
            }

        } else {
            Log.e(TAG, "Unable to find texture " + file.absolutePath)
        }

    }

    private fun loadGLTexture(gl: GL10, iconfile: String) {
        Log.i(TAG, "loadGLTexture")
        if (BUSY) {
            Log.e(TAG, "Loading Textures Already Busy")
            return
        }
        BUSY = true
        Log.d(TAG, "Loading Textures")
        //texturesLoaded = TextureOperations.invalidateTexture(gl, textures)
        val options = Options()
        options.inScaled = false
        val file = File(MyApplication.FilesPath + iconfile)
        if (file.exists()) {
            val decodeFile = BitmapFactory.decodeFile(file.absolutePath, options)
            textures[0] = TextureOperations.loadTextureFromBitmapFast(gl, decodeFile)
            decodeFile.recycle()
            texturesLoaded = true
            BUSY = false
            return
        } else {
            Log.e(TAG, "Unable to find texture " + file.absolutePath)
            BUSY = false
        }
    }


    fun drawIcon(gl: GL10, iconfile: String, x: Int, y: Int) {
        Log.i(TAG, "drawIcon")
        if (!texturesLoaded) {
            Log.i(TAG, "Loading Textures in drawIcon")
            loadGLTexture(gl, iconfile)
        }

        draw(gl, x, y)
    }

    fun unloadTextures(gl: GL10) {
        Log.i(TAG, "unloadtextures")
        texturesLoaded = TextureOperations.invalidateTexture(gl, textures)
    }

    fun draw(gl10: GL10, x: Int, y: Int) {
        Log.i(TAG, "draw")
        gl10.glEnable(GL10.GL_TEXTURE_2D)
        gl10.glBindTexture(GL10.GL_TEXTURE_2D, textures[0])
        (gl10 as GL11).glTexParameteriv(GL10.GL_TEXTURE_2D, 35741, cropRect, 0)
        (gl10 as GL11Ext).glDrawTexiOES(x, y, 0, drawWidth, drawHeight)
        //gl10.glDisable(GL10.GL_TEXTURE_2D)
    }

    companion object {
        private val icon_sizing_factor = 0.4f
    }


}

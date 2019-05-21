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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Bitmap.Config
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.core.graphics.drawable.DrawableCompat
import android.widget.ImageView
import androidx.core.content.ContextCompat
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.MyApplication
import joshuatee.wx.ui.ObjectNavDrawer
import joshuatee.wx.ui.TouchImageView2

object UtilityImg {

    fun showNextImg(drw: ObjectNavDrawer, fn: () -> Unit) {
        drw.index += 1
        if (drw.index == drw.getUrlCount()) {
            drw.index = 0
        }
        fn()
    }

    fun showPrevImg(drw: ObjectNavDrawer, fn: () -> Unit) {
        drw.index -= 1
        if (drw.index == -1) {
            drw.index = drw.getUrlCount() - 1
        }
        fn()
    }

    fun mergeImages(context: Context, imageA: Bitmap, imageB: Bitmap): Bitmap {
        val layers = mutableListOf<Drawable>()
        layers.add(BitmapDrawable(context.resources, imageA))
        layers.add(BitmapDrawable(context.resources, imageB))
        return layerDrawableToBitmap(layers)
    }

    fun addColorBG(context: Context, imageA: Bitmap, color: Int): Bitmap {
        val layers = mutableListOf<Drawable>()
        layers.add(ColorDrawable(color))
        layers.add(BitmapDrawable(context.resources, imageA))
        return layerDrawableToBitmap(layers)
    }

    fun getBlankBitmap(): Bitmap = Bitmap.createBitmap(10, 10, Config.ARGB_8888)

    fun getBitmapRemoveBG(imgUrl: String, color: Int): Bitmap =
        eraseBG(imgUrl.getImage(), color)

    fun getBitmapAddWhiteBG(context: Context, imgUrl: String): Bitmap {
        val layers = mutableListOf<Drawable>()
        val bitmap = imgUrl.getImage()
        layers.add(ColorDrawable(Color.WHITE))
        layers.add(BitmapDrawable(context.resources, bitmap))
        return layerDrawableToBitmap(layers)
    }

    fun firstRunSetZoomPosn(firstRunF: Boolean, img: TouchImageView2, pref: String): Boolean {
        var firstRun = firstRunF
        if (!firstRun) {
            img.setZoom(pref)
            firstRun = true
        }
        return firstRun
    }

    fun imgRestorePosnZoom(context: Context, img: TouchImageView2, prefStr: String) {
        img.setZoom(
            Utility.readPref(context, prefStr + "_ZOOM", 1.0f),
            Utility.readPref(context, prefStr + "_X", 0.5f),
            Utility.readPref(context, prefStr + "_Y", 0.5f)
        )
    }

    fun imgSavePosnZoom(context: Context, img: TouchImageView2, prefStr: String) {
        val poi = img.scrollPosition
        var z = img.currentZoom
        if (poi != null) {
            var x = poi.x
            var y = poi.y
            if (x.isNaN()) x = 1.0f
            if (y.isNaN()) y = 1.0f
            if (z.isNaN()) z = 1.0f
            Utility.writePref(context, prefStr + "_X", x)
            Utility.writePref(context, prefStr + "_Y", y)
            Utility.writePref(context, prefStr + "_ZOOM", z)
            when (prefStr) {
                "SPCHRRR" -> {
                    MyApplication.spchrrrZoom = z
                    MyApplication.spchrrrX = x
                    MyApplication.spchrrrY = y
                }
                "SPCSSEO" -> {
                    MyApplication.spcsseoZoom = z
                    MyApplication.spcsseoX = x
                    MyApplication.spcsseoY = y
                }
                "WPCGEFS1" -> {
                    MyApplication.wpcgefsZoom = z
                    MyApplication.wpcgefsX = x
                    MyApplication.wpcgefsY = y
                }
                else -> {
                }
            }
        }
    }

    fun loadBitmap(context: Context, res: Int, resize: Boolean): Bitmap {
        val bitmap: Bitmap
        val inputStream = context.resources.openRawResource(res)
        var options: BitmapFactory.Options? = null
        if (resize) {
            options = BitmapFactory.Options()
            options.inPreferredConfig = Config.RGB_565
            options.inSampleSize = 2
        }
        bitmap = try {
            if (!resize)
                BitmapFactory.decodeStream(inputStream)
            else
                BitmapFactory.decodeStream(inputStream, null, options)
                    ?: getBlankBitmap()
        } catch (e: OutOfMemoryError) {
            UtilityLog.handleException(e)
            return getBlankBitmap()
        } finally {
            try {
                inputStream.close()
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        }
        return bitmap
    }

    fun animInterval(context: Context): Int = 50 * Utility.readPref(context, "ANIM_INTERVAL", 15)

    fun bitmapToLayerDrawable(context: Context, bitmap: Bitmap): LayerDrawable =
        LayerDrawable(arrayOf(BitmapDrawable(context.resources, bitmap)))

    fun layerDrawableToBitmap(layers: MutableList<Drawable>): Bitmap {
        val drawable = LayerDrawable(layers.toTypedArray())
        val bitmap: Bitmap
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        if (width > 0 && height > 0) {
            try {
                bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888)
            } catch (e: OutOfMemoryError) {
                return getBlankBitmap()
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        } else {
            bitmap = getBlankBitmap()
        }
        return bitmap
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        val bitmap: Bitmap
        if (width > 0 && height > 0) {
            bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        } else {
            bitmap = Bitmap.createBitmap(10, 10, Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }
        return bitmap
    }

    fun eraseBG(src: Bitmap, color: Int): Bitmap {
        val width = src.width
        val height = src.height
        return try {
            val b = src.copy(Config.ARGB_8888, true)
            b.setHasAlpha(true)
            val size = width * height
            val pixels = IntArray(size)
            src.getPixels(pixels, 0, width, 0, 0, width, height)
            (0 until size)
                .filter { pixels[it] == color }
                .forEach { pixels[it] = 0 }
            b.setPixels(pixels, 0, width, 0, 0, width, height)
            b
        } catch (e: OutOfMemoryError) {
            getBlankBitmap()
        }
    }

    fun resizeViewSetImgInCard(bitmap: Bitmap, iv: ImageView) {
        val paramsIv = iv.layoutParams
        paramsIv.width = MyApplication.dm.widthPixels - (MyApplication.lLpadding * 2).toInt()
        paramsIv.height = (MyApplication.dm.widthPixels - (MyApplication.lLpadding * 2).toInt()) *
                bitmap.height / bitmap.width
        iv.layoutParams = paramsIv
        iv.setImageBitmap(bitmap)
    }

    fun resizeViewSetImgByHeight(bitmap: Bitmap, iv: ImageView) {
        val paramsIv = iv.layoutParams
        paramsIv.height = MyApplication.dm.heightPixels / 2
        paramsIv.width = paramsIv.height * bitmap.width / bitmap.height
        iv.layoutParams = paramsIv
        iv.setImageBitmap(bitmap)
    }

    fun scaleBitmap(bitmap: Bitmap, wantedWidth: Int, wantedHeight: Int): Bitmap {
        val output = Bitmap.createBitmap(wantedWidth, wantedHeight, Config.ARGB_8888)
        val canvas = Canvas(output)
        val m = Matrix()
        m.setScale(wantedWidth.toFloat() / bitmap.width, wantedHeight.toFloat() / bitmap.height)
        canvas.drawBitmap(bitmap, m, Paint())
        return output
    }

    fun drawTextToBitmap(context: Context, bitmap: Bitmap, mText: String, textColor: Int): Bitmap {
        try {
            val scale = context.resources.displayMetrics.density
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = textColor
            paint.textSize = (12 * scale).toInt().toFloat()
            paint.setShadowLayer(1f, 0f, 1f, Color.DKGRAY)
            val bounds = Rect()
            paint.getTextBounds(mText, 0, mText.length, bounds)
            val x = (bitmap.width - bounds.width()) / 6
            val y = 15
            canvas.drawText(mText, x * scale, y * scale, paint)
            return bitmap
        } catch (e: Exception) {
            UtilityLog.handleException(e)
            return Bitmap.createBitmap(10, 10, Config.ARGB_8888)
        }
    }

    fun drawTextToBitmapForNexrad(context: Context, bitmap: Bitmap): Bitmap {
        val radarStatus = Utility.readPref(context, "WX_RADAR_CURRENT_INFO_WIDGET_TIME", "")
        try {
            val scale = context.resources.displayMetrics.density
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.color = Color.WHITE
            paint.textSize = (12 * scale).toInt().toFloat()
            paint.setShadowLayer(1f, 0f, 1f, Color.DKGRAY)
            val bounds = Rect()
            paint.getTextBounds(radarStatus, 0, radarStatus.length, bounds)
            val x = (bitmap.width - bounds.width()) / 6
            val y = 15
            canvas.drawText(radarStatus, x * scale, y * scale, paint)
            return bitmap
        } catch (e: Exception) {
            UtilityLog.handleException(e)
            return Bitmap.createBitmap(10, 10, Config.ARGB_8888)
        }
    }

    fun vectorDrawableToBitmap(context: Context, resdraw: Int, color: Int): Bitmap {
        val d = ContextCompat.getDrawable(context, resdraw)!!
        DrawableCompat.setTint(d, color)
        val b = Bitmap.createBitmap(d.intrinsicWidth, d.intrinsicHeight, Config.ARGB_8888)
        val c = Canvas(b)
        d.setBounds(0, 0, c.width, c.height)
        d.draw(c)
        return b
    }

    fun mergeImagesVertically(images: List<Bitmap>): Bitmap {
        val combinedImage: Bitmap?
        var width = 0
        var height = 0
        images.forEach {
            height += it.height
            if (it.width > width) {
                width = it.width
            }
        }
        combinedImage = Bitmap.createBitmap(width, height, Config.ARGB_8888)
        val comboImage = Canvas(combinedImage!!)
        var workingHeight = 0f
        images.forEach {
            comboImage.drawBitmap(it, 0f, workingHeight, null)
            workingHeight += it.height
        }
        return combinedImage
    }
}

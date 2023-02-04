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
import joshuatee.wx.radar.CanvasCreate
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.radar.NexradUtil
import joshuatee.wx.ui.UtilityUI

object UtilityImg {

    fun mergeImages(context: Context, imageA: Bitmap, imageB: Bitmap): Bitmap =
            layerDrawableToBitmap(listOf(BitmapDrawable(context.resources, imageA), BitmapDrawable(context.resources, imageB)))

    fun addColorBackground(context: Context, bitmap: Bitmap, color: Int): Bitmap =
            layerDrawableToBitmap(listOf(ColorDrawable(color), BitmapDrawable(context.resources, bitmap)))

    fun getBlankBitmap(): Bitmap = Bitmap.createBitmap(10, 10, Config.ARGB_8888)

    fun getBitmapAddWhiteBackground(context: Context, imgUrl: String): Bitmap =
            layerDrawableToBitmap(listOf(ColorDrawable(Color.WHITE), BitmapDrawable(context.resources, imgUrl.getImage())))

    fun loadBitmap(context: Context, resourceId: Int, resize: Boolean): Bitmap {
        val inputStream = context.resources.openRawResource(resourceId)
        var options: BitmapFactory.Options? = null
        if (resize) {
            options = BitmapFactory.Options()
            options.inPreferredConfig = Config.RGB_565
            options.inSampleSize = 2
        }
        return try {
            if (!resize)
                BitmapFactory.decodeStream(inputStream)
            else
                BitmapFactory.decodeStream(inputStream, null, options) ?: getBlankBitmap()
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
    }

    fun animInterval(context: Context): Int =
            50 * Utility.readPrefInt(context, "ANIM_INTERVAL", UIPreferences.animationIntervalDefault)

    fun bitmapToLayerDrawable(context: Context, bitmap: Bitmap): LayerDrawable =
            LayerDrawable(arrayOf(BitmapDrawable(context.resources, bitmap)))

    fun layerDrawableToBitmap(layers: List<Drawable>): Bitmap {
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

    fun eraseBackground(src: Bitmap, color: Int): Bitmap {
        val width = src.width
        val height = src.height
        return try {
            val b = src.copy(Config.ARGB_8888, true)
            b.setHasAlpha(true)
            val size = width * height
            val pixels = IntArray(size)
            src.getPixels(pixels, 0, width, 0, 0, width, height)
            (0 until size).filter { pixels[it] == color }.forEach {
                pixels[it] = 0
            }
            b.setPixels(pixels, 0, width, 0, 0, width, height)
            b
        } catch (e: OutOfMemoryError) {
            getBlankBitmap()
        }
    }

    fun resizeViewSetImgInCard(bitmap: Bitmap, imageView: ImageView, numberAcross: Int = 1) {
        val layoutParams = imageView.layoutParams
        layoutParams.width = (MyApplication.dm.widthPixels - (UIPreferences.lLpadding * 2).toInt()) / numberAcross
        layoutParams.height = ((MyApplication.dm.widthPixels - (UIPreferences.lLpadding * 2).toInt()) * bitmap.height / bitmap.width ) / numberAcross
        imageView.layoutParams = layoutParams
        imageView.setImageBitmap(bitmap)
    }

    fun resizeViewAndSetImage(context: Context, bitmap: Bitmap, imageView: ImageView) {
        if (UtilityUI.isLandScape(context)) {
            resizeViewSetImgByWidth(bitmap, imageView)
        } else {
            resizeViewSetImgByHeight(bitmap, imageView)
        }
    }

    private fun resizeViewSetImgByHeight(bitmap: Bitmap, imageView: ImageView) {
        val layoutParams = imageView.layoutParams
        layoutParams.height = MyApplication.dm.heightPixels / 2
        layoutParams.width = layoutParams.height * bitmap.width / bitmap.height
        imageView.layoutParams = layoutParams
        imageView.setImageBitmap(bitmap)
    }

    private fun resizeViewSetImgByWidth(bitmap: Bitmap, imageView: ImageView) {
        val layoutParams = imageView.layoutParams
        layoutParams.width = MyApplication.dm.widthPixels / 2
        layoutParams.height = layoutParams.width * bitmap.width / bitmap.height
        imageView.layoutParams = layoutParams
        imageView.setImageBitmap(bitmap)
    }

    fun scaleBitmap(bitmap: Bitmap, wantedWidth: Int, wantedHeight: Int): Bitmap {
        val output = Bitmap.createBitmap(wantedWidth, wantedHeight, Config.ARGB_8888)
        val canvas = Canvas(output)
        val matrix = Matrix()
        matrix.setScale(wantedWidth.toFloat() / bitmap.width, wantedHeight.toFloat() / bitmap.height)
        canvas.drawBitmap(bitmap, matrix, Paint())
        return output
    }

    fun drawTextToBitmap(context: Context, bitmap: Bitmap, text: String, textColor: Int): Bitmap = try {
        val scale = context.resources.displayMetrics.density
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = textColor
        paint.textSize = 12 * scale
        paint.setShadowLayer(1.0f, 0.0f, 1.0f, Color.DKGRAY)
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val x = (bitmap.width - bounds.width()) / 6
        val y = 15
        canvas.drawText(text, x * scale, y * scale, paint)
        bitmap
    } catch (e: Exception) {
        UtilityLog.handleException(e)
        Bitmap.createBitmap(10, 10, Config.ARGB_8888)
    }

    // used in UtilityUSImgWX for nexrad
    fun drawText(context: Context, bitmap: Bitmap): Bitmap = try {
        val radarStatus = NexradUtil.readRadarTimeForWidget(context)
        val scale = context.resources.displayMetrics.density
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.WHITE
        paint.textSize = (12 * scale).toInt().toFloat()
        paint.setShadowLayer(1.0f, 0.0f, 1.0f, Color.DKGRAY)
        val bounds = Rect()
        paint.getTextBounds(radarStatus, 0, radarStatus.length, bounds)
        val x = (bitmap.width - bounds.width()) / 6
        val y = 15
        canvas.drawText(radarStatus, x * scale, y * scale, paint)
        bitmap
    } catch (e: Exception) {
        UtilityLog.handleException(e)
        Bitmap.createBitmap(10, 10, Config.ARGB_8888)
    }

    fun vectorDrawableToBitmap(context: Context, resourceDrawable: Int, color: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, resourceDrawable)!!
        DrawableCompat.setTint(drawable, color)
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
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
        if (width == 0 || height == 0) {
            return getBlankBitmap()
        }
        combinedImage = Bitmap.createBitmap(width, height, Config.ARGB_8888)
        val comboImage = Canvas(combinedImage!!)
        var workingHeight = 0.0f
        images.forEach {
            comboImage.drawBitmap(it, 0.0f, workingHeight, null)
            workingHeight += it.height
        }
        return combinedImage
    }

    // FIXME TODO move elsewhere
    fun getNexradRefBitmap(context: Context, radarSite: String): Bitmap =
            CanvasCreate.layeredImage(context, radarSite, "N0Q")
}

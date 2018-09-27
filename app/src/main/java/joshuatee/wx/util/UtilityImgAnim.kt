/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import joshuatee.wx.Extensions.getImage

import joshuatee.wx.ui.TouchImageView2

import joshuatee.wx.Extensions.*

object UtilityImgAnim {

    fun getURLArray(url: String, pattern: String, count: String): List<String> {
        val retAl = mutableListOf<String>()
        try {
            val radarIndexHtml = url.getHtml()
            val radarAl = radarIndexHtml.parseColumn(pattern)
            val frameCnt = count.toIntOrNull() ?: 0
            if (radarAl.size >= frameCnt) {
                (radarAl.size - frameCnt until radarAl.size).mapTo(retAl) { radarAl[it] }
            } else {
                (0 until radarAl.size).mapTo(retAl) { radarAl[it] }
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return retAl
    }

    fun getAnimationDrawableFromURLList(context: Context, urlAl: List<String>, delayF: Int): AnimationDrawable {
        var delay = delayF
        val animDrawable = AnimationDrawable()
        val bmAl = urlAl.map { it.getImage() }
        bmAl.forEachIndexed { i, it ->
            if (it.width > 10) {
                if (i == bmAl.lastIndex) {
                    delay *= 3
                }
                animDrawable.addFrame(BitmapDrawable(context.resources, it), delay)
            }
        }
        return animDrawable
    }

    fun getAnimationDrawableFromURLListWhiteBG(context: Context, urlAl: List<String>, delayF: Int): AnimationDrawable {
        var delay = delayF
        val animDrawable = AnimationDrawable()
        val bmAl = urlAl.mapTo(mutableListOf()) { UtilityImg.getBitmapAddWhiteBG(context, it) }
        bmAl.forEachIndexed { i, it ->
            if (it.width > 10) {
                if (i == bmAl.lastIndex) {
                    delay *= 3
                }
                animDrawable.addFrame(BitmapDrawable(context.resources, it), delay)
            }
        }
        return animDrawable
    }

    fun getAnimationDrawableFromBMList(context: Context, bmAl: List<Bitmap>, delayF: Int): AnimationDrawable {
        var delay = delayF
        val animDrawable = AnimationDrawable()
        bmAl.forEachIndexed { i, it ->
            if (it.width > 10) {
                if (i == bmAl.lastIndex) {
                    delay *= 3
                }
                animDrawable.addFrame(BitmapDrawable(context.resources, it), delay)
            }
        }
        return animDrawable
    }

    fun getAnimationDrawableFromBMList(context: Context, bmAl: List<Bitmap>): AnimationDrawable {
        val animDrawable = AnimationDrawable()
        var delay = UtilityImg.animInterval(context) * 2
        bmAl.forEachIndexed { i, it ->
            if (it.width > 10) {
                if (i == bmAl.lastIndex) {
                    delay *= 3
                }
                animDrawable.addFrame(BitmapDrawable(context.resources, it), delay)
            }
        }
        return animDrawable
    }

    fun getAnimationDrawableFromBMListWithCanvas(context: Context, bmAl: List<Bitmap>, delayF: Int, cd: ColorDrawable, bitmapCanvas: Bitmap): AnimationDrawable {
        var delay = delayF
        val animDrawable = AnimationDrawable()
        val layers = arrayOfNulls<Drawable>(3)
        bmAl.forEachIndexed { i, it ->
            if (it.width > 10) {
                if (i == bmAl.lastIndex) {
                    delay *= 3
                }
                layers[0] = cd
                layers[1] = BitmapDrawable(context.resources, it)
                layers[2] = BitmapDrawable(context.resources, bitmapCanvas)
                animDrawable.addFrame(LayerDrawable(layers), delay)
            }
        }
        return animDrawable
    }

    fun startAnimation(animDrawable: AnimationDrawable, img: TouchImageView2): Boolean {
        img.setImageDrawable(animDrawable)
        animDrawable.isOneShot = false
        animDrawable.start()
        return true
    }
}

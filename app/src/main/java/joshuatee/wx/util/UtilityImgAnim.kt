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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.ui.ObjectTouchImageView

object UtilityImgAnim {

    fun getAnimationDrawableFromUrlList(context: Context, urls: List<String>, delayOriginal: Int): AnimationDrawable {
        var delay = delayOriginal
        val animationDrawable = AnimationDrawable()
        val bitmaps = urls.map { it.getImage() }
        bitmaps.forEachIndexed { index, bitmap ->
            if (bitmap.width > 10) {
                if (index == bitmaps.lastIndex) delay *= 3
                animationDrawable.addFrame(BitmapDrawable(context.resources, bitmap), delay)
            }
        }
        return animationDrawable
    }

    fun getAnimationDrawableFromUrlListWhiteBackground(context: Context, urls: List<String>, delayOriginal: Int): AnimationDrawable {
        var delay = delayOriginal
        val animationDrawable = AnimationDrawable()
        val bitmaps = urls.map { UtilityImg.getBitmapAddWhiteBackground(context, it) }
        bitmaps.forEachIndexed { index, bitmap ->
            if (bitmap.width > 10) {
                if (index == bitmaps.lastIndex) delay *= 3
                animationDrawable.addFrame(BitmapDrawable(context.resources, bitmap), delay)
            }
        }
        return animationDrawable
    }

    fun getAnimationDrawableFromBitmapList(context: Context, bitmaps: List<Bitmap>, delayOriginal: Int): AnimationDrawable {
        var delay = delayOriginal
        val animationDrawable = AnimationDrawable()
        bitmaps.forEachIndexed { index, bitmap ->
            if (bitmap.width > 10) {
                if (index == bitmaps.lastIndex) delay *= 3
                animationDrawable.addFrame(BitmapDrawable(context.resources, bitmap), delay)
            }
        }
        return animationDrawable
    }

    fun getAnimationDrawableFromBitmapList(context: Context, bitmaps: List<Bitmap>): AnimationDrawable {
        val animationDrawable = AnimationDrawable()
        var delay = UtilityImg.animInterval(context) * 2
        bitmaps.forEachIndexed { index, bitmap ->
            if (bitmap.width > 10) {
                if (index == bitmaps.lastIndex) delay *= 3
                animationDrawable.addFrame(BitmapDrawable(context.resources, bitmap), delay)
            }
        }
        return animationDrawable
    }

    fun startAnimation(animationDrawable: AnimationDrawable, img: TouchImageView2): Boolean {
        img.setImageDrawable(animationDrawable)
        animationDrawable.isOneShot = false
        animationDrawable.start()
        return true
    }

    fun startAnimation(animationDrawable: AnimationDrawable, img: ObjectTouchImageView): Boolean {
        img.setImageDrawable(animationDrawable)
        animationDrawable.isOneShot = false
        animationDrawable.start()
        return true
    }
}

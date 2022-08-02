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

package joshuatee.wx.objects

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.view.Menu
import android.view.MenuItem
import joshuatee.wx.Extensions.getImage
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.ui.TouchImage
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.util.UtilityImgAnim

class ObjectAnimate(val context: Context, val image: TouchImage) {

    var animationDrawable = AnimationDrawable()
    private var animateButton: MenuItem? = null
    private var pauseButton: MenuItem? = null
    var urls = listOf<String>()
    var isPaused = false

    fun start() {
        UtilityImgAnim.startAnimation(animationDrawable, image)
    }

    fun setButton(menu: Menu) {
        animateButton = menu.findItem(R.id.action_animate)
        pauseButton = menu.findItem(R.id.action_pause)
        pauseButton?.isVisible = false
    }

    fun stop() {
        animateButton?.setIcon(GlobalVariables.ICON_PLAY)
        pauseButton?.isVisible = false
        animationDrawable.stop()
    }

    fun pause() {
        isPaused = if (isRunning()) {
            animationDrawable.stop()
            pauseButton?.setIcon(GlobalVariables.ICON_PLAY)
            true
        } else {
            animationDrawable.start()
            pauseButton?.setIcon(GlobalVariables.ICON_PAUSE)
            false
        }
    }

    fun setIconToRun() {
        animateButton?.setIcon(GlobalVariables.ICON_STOP)
        pauseButton?.isVisible = true
        pauseButton?.setIcon(GlobalVariables.ICON_PAUSE)
    }

    fun download() {
        animationDrawable = getAnimationDrawableFromUrlList(context, urls)
    }

    fun isRunning(): Boolean {
        return animationDrawable.isRunning
    }

    fun animateClicked(getContent: () -> Unit, getFn: () -> List<String>) {
        if (isRunning() || isPaused) {
            isPaused = false
            stop()
            getContent()
        } else {
            setIconToRun()
            FutureVoid(context,
                    {
                        urls = getFn()
                        download()
                    })
            { start() }
        }
    }

    companion object {
        fun getAnimationDrawableFromUrlList(context: Context, urls: List<String>): AnimationDrawable {
            val bitmaps = urls.map { it.getImage() }
            val animationDrawable = AnimationDrawable()
            var delay = UtilityImg.animInterval(context) * 2
            bitmaps.forEachIndexed { index, bitmap ->
                if (bitmap.width > 10) {
                    if (index == bitmaps.lastIndex) {
                        delay *= 3
                    }
                    animationDrawable.addFrame(BitmapDrawable(context.resources, bitmap), delay)
                }
            }
            return animationDrawable
        }

//        fun getAnimationDrawableFromUrlListWhiteBackground(context: Context, urls: List<String>, delayOriginal: Int): AnimationDrawable {
//            var delay = delayOriginal
//            val animationDrawable = AnimationDrawable()
//            val bitmaps = urls.map { UtilityImg.getBitmapAddWhiteBackground(context, it) }
//            bitmaps.forEachIndexed { index, bitmap ->
//                if (bitmap.width > 10) {
//                    if (index == bitmaps.lastIndex) {
//                        delay *= 3
//                    }
//                    animationDrawable.addFrame(BitmapDrawable(context.resources, bitmap), delay)
//                }
//            }
//            return animationDrawable
//        }
    }
}

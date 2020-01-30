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

package joshuatee.wx.models

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import joshuatee.wx.R
import joshuatee.wx.ui.ObjectSpinner
import joshuatee.wx.ui.OnSwipeTouchListener
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.util.UtilityImg

class DisplayData(
    context: Context,
    activity: Activity,
    numPanes: Int,
    spTime: ObjectSpinner
) {

    var animDrawable: MutableList<AnimationDrawable> = mutableListOf()
    var param: MutableList<String> = mutableListOf()
    var paramLabel: MutableList<String> = mutableListOf()
    var img: MutableList<TouchImageView2> = mutableListOf()
    var bitmap: MutableList<Bitmap> = mutableListOf()

    init {
        for (it in 0 until numPanes) {
            img.add(TouchImageView2(context))
            bitmap.add(UtilityImg.getBlankBitmap())
            param.add("")
            paramLabel.add("")
            animDrawable.add(AnimationDrawable())
        }
        val resId = listOf(R.id.iv1, R.id.iv2)
        (0 until numPanes).forEach {
            img[it] = activity.findViewById(resId[it])
        }
        if (numPanes > 1) {
            img[0].setOnTouchImageViewListener { img[1].setZoom(img[0]) }
            img[1].setOnTouchImageViewListener { img[0].setZoom(img[1]) }
        }
        (0 until numPanes).forEach {
            img[it].setOnTouchListener(object : OnSwipeTouchListener(context) {
                override fun onSwipeLeft() {
                    if (img[0].currentZoom < 1.01f) UtilityModels.moveForward(spTime)
                }

                override fun onSwipeRight() {
                    if (img[0].currentZoom < 1.01f) UtilityModels.moveBack(spTime)
                }
            })
        }
    }
}



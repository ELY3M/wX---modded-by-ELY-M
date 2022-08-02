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

import android.app.Activity
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import joshuatee.wx.R
import joshuatee.wx.models.ObjectModel
import joshuatee.wx.ui.OnSwipeTouchListener
import joshuatee.wx.ui.TouchImage
import joshuatee.wx.util.UtilityImg

class DisplayData(context: Context, activity: Activity, numPanes: Int, om: ObjectModel) {

    var animDrawable = MutableList(numPanes) { AnimationDrawable() }
    var param = MutableList(numPanes) {""}
    var paramLabel = MutableList(numPanes) {""}
    val image = mutableListOf<TouchImage>()
    val objectAnimates = mutableListOf<ObjectAnimate>()
    var bitmaps = MutableList(numPanes) { UtilityImg.getBlankBitmap() }

    init {
        val resourceId = listOf(R.id.iv1, R.id.iv2)
        (0 until numPanes).forEach { index ->
            image.add(TouchImage(activity, resourceId[index]))
            objectAnimates.add(ObjectAnimate(context, image.last()))
        }
        if (numPanes > 1) {
            image[0].connect2 { image[1].setZoom(image[0]) }
            image[1].connect2 { image[0].setZoom(image[1]) }
        }
        (0 until numPanes).forEach {
            if (om.prefModel != "") { // Don't use in SPC Meso
                image[it].connect(object : OnSwipeTouchListener(context) {
                    override fun onSwipeLeft() {
                        if (image[0].currentZoom < 1.01f) om.rightClick()
                    }

                    override fun onSwipeRight() {
                        if (image[0].currentZoom < 1.01f) om.leftClick()
                    }
                })
            }
        }
    }
}

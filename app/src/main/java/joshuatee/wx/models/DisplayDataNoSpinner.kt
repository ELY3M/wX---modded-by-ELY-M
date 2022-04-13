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
import android.graphics.drawable.AnimationDrawable
import joshuatee.wx.R
import joshuatee.wx.ui.OnSwipeTouchListener
import joshuatee.wx.ui.TouchImageView2
import joshuatee.wx.util.UtilityImg

class DisplayDataNoSpinner(context: Context, activity: Activity, numPanes: Int, om: ObjectModelNoSpinner) {

    var animDrawable = MutableList(numPanes) {AnimationDrawable()}
    var param = MutableList(numPanes) {""}
    var paramLabel = MutableList(numPanes) {""}
    var img = MutableList(numPanes) {TouchImageView2(context)}
    var bitmap = MutableList(numPanes) {UtilityImg.getBlankBitmap()}

    init {
        val resourceId = listOf(R.id.iv1, R.id.iv2)
        (0 until numPanes).forEach {index -> img[index] = activity.findViewById(resourceId[index]) }
        if (numPanes > 1) {
            img[0].setOnTouchImageViewListener { img[1].setZoom(img[0]) }
            img[1].setOnTouchImageViewListener { img[0].setZoom(img[1]) }
        }
        (0 until numPanes).forEach {
            if (om.prefModel != "") { // Don't use in SPC Meso
                img[it].setOnTouchListener(object : OnSwipeTouchListener(context) {
                    override fun onSwipeLeft() {
                        if (img[0].currentZoom < 1.01f) om.rightClick()
                    }

                    override fun onSwipeRight() {
                        if (img[0].currentZoom < 1.01f) om.leftClick()
                    }
                })
            }
        }
    }
}

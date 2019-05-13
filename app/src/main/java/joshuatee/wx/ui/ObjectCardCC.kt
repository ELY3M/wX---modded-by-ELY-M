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

package joshuatee.wx.ui

import android.content.Context
import android.graphics.Bitmap
import android.view.Gravity
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.widget.TextViewCompat

import joshuatee.wx.MyApplication
import joshuatee.wx.objects.TextSize
import joshuatee.wx.settings.Location
import joshuatee.wx.util.ObjectForecastPackageCurrentConditions

class ObjectCardCC(context: Context, version: Int) {

    private val objCard: ObjectCard
    private var imageView = ObjectImageView(context)
    val textViewTop: ObjectTextView
    val textViewBottom: ObjectTextView
    private val tvCc22: ObjectTextView

    init {
        // FIXME better variable names
        val linearLayoutHorizontal = LinearLayout(context)
        val linearLayoutVertical = LinearLayout(context)
        textViewTop = ObjectTextView(context, TextSize.MEDIUM)
        textViewTop.gravity = Gravity.CENTER
        textViewTop.setPadding(MyApplication.padding, 0, MyApplication.padding, 0)
        if (android.os.Build.VERSION.SDK_INT > 20) {
            TextViewCompat.setAutoSizeTextTypeWithDefaults(
                textViewTop.tv,
                TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
            )
            textViewTop.maxLines = 1
        }
        textViewBottom = ObjectTextView(context)
        textViewBottom.gravity = Gravity.CENTER
        textViewBottom.setAsBackgroundText()
        textViewBottom.setPadding(MyApplication.padding, 0, MyApplication.padding, 2)
        tvCc22 = ObjectTextView(context)
        tvCc22.gravity = Gravity.CENTER
        tvCc22.setAsBackgroundText()
        tvCc22.setPadding(MyApplication.padding, 0, MyApplication.padding, 0)
        if (version == 2) {
            linearLayoutHorizontal.orientation = LinearLayout.HORIZONTAL
            textViewTop.gravity = Gravity.START
            tvCc22.gravity = Gravity.START
            textViewBottom.gravity = Gravity.START
            textViewTop.setPadding(
                MyApplication.padding,
                MyApplication.paddingSmall,
                MyApplication.paddingSmall,
                0
            )
            tvCc22.setPadding(MyApplication.padding, 0, MyApplication.paddingSmall, 0)
            textViewBottom.setPadding(
                MyApplication.padding,
                0,
                MyApplication.paddingSmall,
                MyApplication.paddingSmall
            )
            linearLayoutVertical.orientation = LinearLayout.VERTICAL
            linearLayoutVertical.gravity = Gravity.CENTER_VERTICAL
            linearLayoutVertical.addView(textViewTop.tv)
            linearLayoutVertical.addView(tvCc22.tv)
            linearLayoutVertical.addView(textViewBottom.tv)
            linearLayoutHorizontal.addView(imageView.image)
            linearLayoutHorizontal.addView(linearLayoutVertical)
        } else {
            // legeacy code
            linearLayoutHorizontal.orientation = LinearLayout.VERTICAL
            linearLayoutHorizontal.addView(textViewTop.tv)
            linearLayoutHorizontal.addView(textViewBottom.tv)
        }
        objCard = ObjectCard(context)
        objCard.addView(linearLayoutHorizontal)
    }

    val card: CardView get() = objCard.card

    fun setStatus(text: String) {
        textViewBottom.text = text
    }

    fun setTopLine(text: String) {
        textViewTop.text = text
    }

    private fun setMiddleLine(text: String) {
        tvCc22.text = text
    }

    fun setListener(
        alertDialogStatus: ObjectDialogue?,
        alertDialogStatusAl: MutableList<String>,
        radarTimestamps: () -> List<String>,
        helpCurrentGeneric: Int,
        fn: (Int) -> Unit
    ) {
        imageView.image.setOnClickListener {
            if (MyApplication.helpMode) {
                fn(helpCurrentGeneric)
            } else {
                alertDialogStatusAl.clear()
                alertDialogStatusAl.add("Edit Location...")
                alertDialogStatusAl.add("Sun/Moon data...")
                alertDialogStatusAl.add("Force Data Refresh...")
                if (MyApplication.locDisplayImg && Location.isUS) {
                    alertDialogStatusAl.add("Radar type: Reflectivity")
                    alertDialogStatusAl.add("Radar type: Velocity")
                    alertDialogStatusAl.add("Reset zoom and center")
                    alertDialogStatusAl += radarTimestamps()
                }
                alertDialogStatus?.show()
            }
        }
    }

    fun updateContent(
        bitmap: Bitmap,
        objCc: ObjectForecastPackageCurrentConditions,
        isUS: Boolean,
        ccTime: String,
        radarTime: String
    ) {
        imageView.setImage(bitmap)
        val sep = " - "
        val tmpArrCc = objCc.data1.split(sep).dropLastWhile { it.isEmpty() }
        val tempArr: List<String>
        if (tmpArrCc.size > 4 && isUS) {
            tempArr = tmpArrCc[0].split("/").dropLastWhile { it.isEmpty() }
            setTopLine(tmpArrCc[4].replace("^ ".toRegex(), "") + " " + tempArr[0] + tmpArrCc[2])
            setMiddleLine(
                tempArr[1].replace(
                    "^ ".toRegex(),
                    ""
                ) + sep + tmpArrCc[1] + sep + tmpArrCc[3]
            )
            setStatus(ccTime + radarTime)
        } else {
            tempArr = tmpArrCc[0].split("/").dropLastWhile { it.isEmpty() }
            setTopLine(tmpArrCc[4] + "" + tempArr[0] + tmpArrCc[2])
            setMiddleLine(
                tempArr[1].replace(
                    "^ ".toRegex(),
                    ""
                ) + sep + tmpArrCc[1] + sep + tmpArrCc[3]
            )
            setStatus(ccTime.replace("^ ".toRegex(), "") + radarTime)
        }
    }
}



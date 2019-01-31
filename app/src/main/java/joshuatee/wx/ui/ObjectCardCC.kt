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
import androidx.appcompat.widget.AppCompatTextView
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.widget.TextViewCompat

import joshuatee.wx.MyApplication
import joshuatee.wx.UIPreferences
import joshuatee.wx.settings.Location
import joshuatee.wx.util.ObjectForecastPackage

class ObjectCardCC(context: Context, version: Int) {

    private val objCard: ObjectCard
    var imageView: ImageView
        private set
    val textViewTop: AppCompatTextView
    val textViewBottom: AppCompatTextView
    private val tvCc22: AppCompatTextView

    init {
        val llCv2 = LinearLayout(context)
        val llCv2V = LinearLayout(context)
        textViewTop = AppCompatTextView(context)
        textViewTop.gravity = Gravity.CENTER
        textViewTop.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeNormal)
        textViewTop.setPadding(MyApplication.padding, 2, MyApplication.padding, 0)
        TextViewCompat.setAutoSizeTextTypeWithDefaults(
            textViewTop,
            TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
        )
        textViewTop.maxLines = 1
        textViewBottom = AppCompatTextView(context)
        textViewBottom.gravity = Gravity.CENTER
        textViewBottom.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
        textViewBottom.setTextColor(UIPreferences.backgroundColor)
        textViewBottom.setTextAppearance(context, UIPreferences.smallTextTheme)
        textViewBottom.setPadding(MyApplication.padding, 0, MyApplication.padding, 2)
        tvCc22 = AppCompatTextView(context)
        tvCc22.gravity = Gravity.CENTER
        tvCc22.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
        tvCc22.setTextColor(UIPreferences.backgroundColor)
        tvCc22.setTextAppearance(context, UIPreferences.smallTextTheme)
        tvCc22.setPadding(MyApplication.padding, 0, MyApplication.padding, 2)
        if (version == 2) {
            // specific to 2
            llCv2.orientation = LinearLayout.HORIZONTAL
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
            imageView = ImageView(context)
            llCv2V.orientation = LinearLayout.VERTICAL
            llCv2V.gravity = Gravity.CENTER_VERTICAL
            llCv2V.addView(textViewTop)
            llCv2V.addView(tvCc22)
            llCv2V.addView(textViewBottom)
            llCv2.addView(imageView)
            llCv2.addView(llCv2V)
        } else {
            // legeacy code
            llCv2.orientation = LinearLayout.VERTICAL
            llCv2.addView(textViewTop)
            llCv2.addView(textViewBottom)
            imageView = ImageView(context)
        }
        objCard = ObjectCard(context)
        objCard.addView(llCv2)
    }

    private fun setImage(bitmap: Bitmap, size: Int) {
        val paramsIv = imageView.layoutParams
        paramsIv.width = size
        paramsIv.height = size
        imageView.layoutParams = paramsIv
        imageView.setImageBitmap(bitmap)
        imageView.setPadding(
            MyApplication.paddingSmall,
            MyApplication.paddingSmall,
            MyApplication.paddingSmall,
            MyApplication.paddingSmall
        )
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
        imageView.setOnClickListener {
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
        size: Int,
        objFcst: ObjectForecastPackage,
        isUS: Boolean,
        ccTime: String,
        radarTime: String
    ) {
        setImage(bitmap, size)
        val sep = " - "
        val tmpArrCc = objFcst.objCC.data1.split(sep).dropLastWhile { it.isEmpty() }
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



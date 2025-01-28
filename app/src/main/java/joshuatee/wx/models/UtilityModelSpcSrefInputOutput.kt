/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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

import android.content.Context
import android.graphics.Bitmap
import joshuatee.wx.util.UtilityImg
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.getHtml
import joshuatee.wx.parse
import joshuatee.wx.parseColumn

internal object UtilityModelSpcSrefInputOutput {

    private const val PATTERN_1 = "([0-9]{10}z</a>&nbsp in through <b>f[0-9]{3})"
    private const val PATTERN_2 =
        "<tr><td class=.previous.><a href=.sref.php\\?run=[0-9]{10}&id=SREF_H5__.>([0-9]{10}z)</a></td></tr>"

    val runTime: RunTimeData
        get() {
            val runData = RunTimeData()
            val html = "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/exper/sref/".getHtml()
            val tmpTxt = html.parse(PATTERN_1)
            val runTimes = html.parseColumn(PATTERN_2)
            val latestRunAl = tmpTxt.split("</a>").dropLastWhile { it.isEmpty() }
            if (latestRunAl.isNotEmpty()) {
                runData.listRunAdd(latestRunAl[0])
            }
            runData.listRunAddAll(runTimes)
            runData.imageCompleteStr = tmpTxt
            if (runData.listRun.isNotEmpty()) {
                runData.mostRecentRun = runData.listRun.first()
            }
            return runData
        }

    fun getImage(context: Context, om: ObjectModel, time: String): Bitmap {
        val run = om.run.replace("z", "")
        val url =
            "${GlobalVariables.NWS_SPC_WEBSITE_PREFIX}/exper/sref/gifs/$run/${om.currentParam}$time.gif"
        return UtilityImg.getBitmapAddWhiteBackground(context, url)
    }
}

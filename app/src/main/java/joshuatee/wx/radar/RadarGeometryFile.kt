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

// work in progress - currently not used

package joshuatee.wx.radar

/*import android.content.Context
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.IOException
import java.nio.ByteBuffer*/

import joshuatee.wx.R

class RadarGeometryFile(
        //var context: Context,
        //var fileId: Int,
        //var count: Int,
        //var preferenceToken: String,
        //var showItemDefault: Boolean
) {

    /*var byteBuffer: ByteBuffer = ByteBuffer.allocateDirect(0)
    var initialized = false
    var showItem: Boolean

    init {
        showItem = if (showItemDefault) {
            Utility.readPref(context, preferenceToken, "true").startsWith("t")
        } else {
            Utility.readPref(context, preferenceToken, "false").startsWith("t")
        }
        if (showItem) {
            //initialize()
        }
    }

    fun initializeIfNeeded() {
        showItem = if (showItemDefault) {
            Utility.readPref(context, preferenceToken, "true").startsWith("t")
        } else {
            Utility.readPref(context, preferenceToken, "false").startsWith("t")
        }
        if (showItem && !initialized) {
            //initialize()
        }
    }

    fun initialize(addData: Boolean) {
        if (!initialized) {
            try {
                val inputStream = context.resources.openRawResource(fileId)
                val dis = DataInputStream(BufferedInputStream(inputStream))
                (0 until count).forEach { _ -> byteBuffer.putFloat(dis.readFloat()) }
                dis.close()
                inputStream.close()
            } catch (e: IOException) {
                UtilityLog.handleException(e)
            }
            initialized = true
        }
    }


    companion object {

        fun byType(context: Context, type: RadarGeometryFileType): RadarGeometryFile {
            when (type) {
                RadarGeometryFileType.countyLines -> return RadarGeometryFile(
                        context,
                        R.raw.county,
                        212992,
                        "RADAR_SHOW_COUNTY",
                        true
                )
                RadarGeometryFileType.highways -> return RadarGeometryFile(
                        context,
                        R.raw.hwv4,
                        862208,
                        "COD_HW_DEFAULT",
                        true
                )
                RadarGeometryFileType.stateLines -> return RadarGeometryFile(
                        context,
                        R.raw.statev2,
                        205748,
                        "RADAR_SHOW_STATELINES",
                        true
                )
                RadarGeometryFileType.rivers -> return RadarGeometryFile(
                        context,
                        R.raw.lakesv3,
                        503812,
                        "COD_LAKES_DEFAULT",
                        false
                )
                RadarGeometryFileType.highwaysExtended -> return RadarGeometryFile(
                        context,
                        R.raw.hwv4ext,
                        770048,
                        "RADAR_HW_ENH_EXT",
                        false
                )
                RadarGeometryFileType.canadaLines -> return RadarGeometryFile(
                        context,
                        R.raw.ca,
                        161792,
                        "RADAR_CANADA_LINES",
                        false
                )
                RadarGeometryFileType.mexicoLines -> return RadarGeometryFile(
                        context,
                        R.raw.mx,
                        151552,
                        "RADAR_MEXICO_LINES",
                        false
                )
                else -> return RadarGeometryFile(
                        context,
                        R.raw.statev2,
                        205748,
                        "RADAR_SHOW_STATELINES",
                        true
                )
            }
        }

        fun checkForInitialization() {
            RadarGeometryFileType.values().forEach {
                byTypes[it]!!.initializeIfNeeded()
            }
        }

        var byTypes: MutableMap<RadarGeometryFileType, RadarGeometryFile> = mutableMapOf()

        fun instantiateAll(context: Context) {
            RadarGeometryFileType.values().forEach {
                byTypes[it] = byType(context, it)
            }
        }
    }*/
}

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

package joshuatee.wx.radarcolorpalettes

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.intentfilter.androidpermissions.PermissionManager
import joshuatee.wx.MyApplication

import joshuatee.wx.R
import joshuatee.wx.RegExp
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityIO
import java.io.*
import java.nio.charset.Charset
import java.util.*

object UtilityColorPalette {

    val TAG: String = "joshuatee ColorPalette"

    //TODO Where is one for SRM (code 56)
    //TODO make color table for SRM

    fun getColorMapStringFromDisk(context: Context, prod: String, code: String): String {

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED|| ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            Log.i(TAG, "stupid permission!!!!!!!!!!!!!!!")
        }

        //TODO TESTING scan dir for *_94.txt files....
        scanfor94pal()

            var cmFileInt: Int = 0
            var text = "null"
            when (prod) {
                "94" -> when (code) {
                    "AF" -> text = readpalfile("colormaprefaf.txt")
                    "EAK" -> text = readpalfile("colormaprefeak.txt")
                    "DKenh" -> text = readpalfile("colormaprefdkenh.txt")
                    "CUST", "CODE" -> text = readpalfile("colormaprefcode.txt")
                    "NSSL" -> text = readpalfile("colormaprefnssl.txt")
                    "NWSD" -> text = readpalfile("colormaprefnwsd.txt")
                    "COD", "CODENH" -> text = readpalfile("colormaprefcodenh.txt")
                    "MENH" -> text = readpalfile("colormaprefmenh.txt")
                    "ELY" -> text = readpalfile("colormapownref.txt")
                    else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + prod + "_" + code, "")
                }
                "99" -> when (code) {
                    "COD", "CODENH" -> text = readpalfile("colormapbvcod.txt")
                    "AF" -> text = readpalfile("colormapbvaf.txt")
                    "EAK" -> text = readpalfile("colormapbveak.txt")
                    "ELY" -> text = readpalfile("colormapownvel.txt")
                    "ENH" -> text = readpalfile("colormapownenhvel.txt")

                    else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + prod + "_" + code, "")
                }
                "135" -> when (code) {
                    "COD", "CODENH" -> text = readpalfile("colormap135cod.txt")
                    else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + prod + "_" + code, "")
                }
                "161" -> when (code) {
                    "COD", "CODENH" -> text = readpalfile("colormap161cod.txt")
                    else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + prod + "_" + code, "")
                }
                "163" -> when (code) {
                    "COD", "CODENH" -> text = readpalfile("colormap163cod.txt")
                    else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + prod + "_" + code, "")
                }
                "159" -> when (code) {
                    "COD", "CODENH" -> text = readpalfile("colormap159cod.txt")
                    else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + prod + "_" + code, "")
                }
                "134" -> when (code) {
                    "COD", "CODENH" -> text = readpalfile("colormap134cod.txt")
                    else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + prod + "_" + code, "")
                }
                "165" -> when (code) {
                    "COD", "CODENH" -> text = readpalfile("colormap165cod.txt")
                    else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + prod + "_" + code, "")
                }
                "172" -> when (code) {
                    "COD", "CODENH" -> text = readpalfile("colormap172cod.txt")
                    else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + prod + "_" + code, "")
                }
            }
            if (text == "null") {
                text = UtilityIO.readTextFile(context.resources.openRawResource(cmFileInt))
            }
            return text
    }


    fun readpalfile(palfile: String): String {
        Log.i(TAG, "trying to open "+palfile)
        var text = ""
        try {
            val initialFile = File(MyApplication.PalFilesPath + palfile)
            val getpalfile = FileInputStream(initialFile)
            text = UtilityIO.readTextFile(getpalfile)
            Log.i(TAG, palfile + ": " + text + "\n")
        } catch (e: IOException) {
            Log.i(TAG, "failed to open file "+palfile+"\nopenpalfile error: "+e.message)
            }
        return text
    }


    fun scanfor94pal() {
        Log.i(TAG, "running scanfor94pal()")
        File(MyApplication.PalFilesPath).walk().forEach {
            if (it.toString().equals(RegExp.palfile94)) {
                Log.i(TAG, "found: "+it)
                val file = File(it.toString())
                var content: String = file.readText()
                Log.i(TAG, content)
            }

        }
    }



}





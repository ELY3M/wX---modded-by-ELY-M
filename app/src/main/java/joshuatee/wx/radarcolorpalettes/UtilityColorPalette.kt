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
//modded by ELY M.

package joshuatee.wx.radarcolorpalettes

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.markodevcic.peko.PermissionRequester
import com.markodevcic.peko.PermissionResult
import joshuatee.wx.StartupActivity
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import kotlin.system.exitProcess


object UtilityColorPalette  {


    //TODO Where is one for SRM (code 56)
    //TODO make color table for SRM

    fun getColorMapStringFromDisk(context: Context, product: Int, code: String): String {


        /*
        if (SDK_INT <= 30) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            UtilityLog.d("wx-elys", "UtilityColorPalette - storage permissions denied")
            //requestPermissions(StartupActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), GlobalVariables.StoragePerms)
        }
        }
        */

        //TODO TESTING scan dir for *_94.txt files....
        //scanfor94pal()

        var fileId = 0
            var text = "null"
            when (product) {
            	94 -> when (code) {
                    "AF" -> text = readpalfile("colormaprefaf.txt")
                    "EAK" -> text = readpalfile("colormaprefeak.txt")
                    "DKenh" -> text = readpalfile("colormaprefdkenh.txt")
                    "CUST", "CODE" -> text = readpalfile("colormaprefcode.txt")
                    "NSSL" -> text = readpalfile("colormaprefnssl.txt")
                    "NWSD" -> text = readpalfile("colormaprefnwsd.txt")
             	    "NWS" -> text = readpalfile("colormaprefnws.txt")
                    "COD", "CODENH" -> text = readpalfile("colormaprefcodenh.txt")
                    "MENH" -> text = readpalfile("colormaprefmenh.txt")
                    "ELY" -> text = readpalfile("colormapownref.txt")
                    else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + product + "_" + code, "")
                }
    	        99 -> when (code) {
                    "COD", "CODENH" -> text = readpalfile("colormapbvcod.txt")
                    "AF" -> text = readpalfile("colormapbvaf.txt")
                    "EAK" -> text = readpalfile("colormapbveak.txt")
                    "ELY" -> text = readpalfile("colormapownvel.txt")
                    "ENH" -> text = readpalfile("colormapownenhvel.txt")

                    else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + product + "_" + code, "")
                }
        	    135 -> when (code) {
                    "COD", "CODENH" -> text = readpalfile("colormap135cod.txt")
                    else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + product + "_" + code, "")
                }
            	161 -> when (code) {
                    "COD", "CODENH" -> text = readpalfile("colormap161cod.txt")
                    else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + product + "_" + code, "")
                }
            	163 -> when (code) {
                    "COD", "CODENH" -> text = readpalfile("colormap163cod.txt")
                    else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + product + "_" + code, "")
                }
                159 -> when (code) {
                    "COD", "CODENH" -> text = readpalfile("colormap159cod.txt")
                    else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + product + "_" + code, "")
                }
                134 -> when (code) {
                    "COD", "CODENH" -> text = readpalfile("colormap134cod.txt")
                    else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + product + "_" + code, "")
                }
                165 -> when (code) {
                    "COD", "CODENH" -> text = readpalfile("colormap165cod.txt")
                    else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + product + "_" + code, "")
                }
                172 -> when (code) {
                    "COD", "CODENH" -> text = readpalfile("colormap172cod.txt")
                    else -> text = Utility.readPref(context, "RADAR_COLOR_PAL_" + product + "_" + code, "")
                }
            }
            if (text == "null") text = UtilityIO.readTextFileFromRaw(context.resources, fileId)
	    return text
    }


    fun readpalfile(palfile: String): String {
        //Log.i(TAG, "trying to open "+palfile)
        var text = ""
        try {
            val initialFile = File(GlobalVariables.PalFilesPath + palfile)
            val getpalfile = FileInputStream(initialFile)
            text = UtilityIO.readTextFile(getpalfile)
            //Log.i(TAG, palfile + ": " + text + "\n")
        } catch (e: IOException) {
            UtilityLog.d("wx-elys", "failed to open file "+palfile+"\nopenpalfile error: "+e.message)
            }
        return text
    }

/*
    fun scanfor94pal() {
        //Log.i(TAG, "running scanfor94pal()")
        File(MyApplication.PalFilesPath).walk().forEach {
            if (it.toString().equals(RegExp.palfile94)) {
                //Log.i(TAG, "found: "+it)
                val file = File(it.toString())
                var content: String = file.readText()
                //Log.i(TAG, content)
            }

        }
    }
*/




}

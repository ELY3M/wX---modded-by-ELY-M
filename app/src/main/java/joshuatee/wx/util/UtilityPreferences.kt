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

package joshuatee.wx.util

import android.content.Context
import android.preference.PreferenceManager

object UtilityPreferences {

    fun writePrefs(context: Context, prefList: List<String>) {
        (0 until prefList.size step 2).forEach {
            Utility.writePref(context, prefList[it], prefList[it + 1])
        }
    }

    private const val sep = "-:-"
    private const val newline = "-:-NL-:-"

    fun printAllPreferences(context: Context): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val keys = preferences.all
        val sb = StringBuilder(1000)
        for ((key, value) in keys) {
            if (value != null) {
                if (value.javaClass == Int::class.java) {
                    sb.append("INT")
                    sb.append(sep)
                    sb.append(key)
                    sb.append(sep)
                    sb.append(value.toString())
                    sb.append(newline)
                }
            }
            if (value != null) {
                if (value.javaClass == Float::class.java) {
                    sb.append("FLOAT")
                    sb.append(sep)
                    sb.append(key)
                    sb.append(sep)
                    sb.append(value.toString())
                    sb.append(newline)
                }
            }
            if (value != null) {
                if (value.javaClass == String::class.java) {
                    sb.append("STRING")
                    sb.append(sep)
                    sb.append(key)
                    sb.append(sep)
                    sb.append(value.toString())
                    sb.append(newline)
                }
            }
        }
        return sb.toString()

    }

    fun applySettings(context: Context, txt: String) {
        val prefLines = txt.split(newline.toRegex()).dropLastWhile { it.isEmpty() }
        var lineArr: List<String>
        prefLines.forEach { line ->
            lineArr = line.split(sep.toRegex()).dropLastWhile { it.isEmpty() }
            when (lineArr[0]) {
                "STRING" -> if (lineArr.size < 3) {
                    Utility.writePref(context, lineArr[1], "")
                } else {
                    Utility.writePref(context, lineArr[1], lineArr[2])
                }
                "FLOAT" -> Utility.writePref(
                    context, lineArr[1], lineArr[2].toFloatOrNull()
                        ?: 0.0f
                )
                "INT" -> Utility.writePref(context, lineArr[1], lineArr[2].toIntOrNull() ?: 0)
            }
        }
        Utility.commitPref(context)
    }
}

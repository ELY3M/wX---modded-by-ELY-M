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

package joshuatee.wx.objects

import android.content.Context
import androidx.preference.PreferenceManager
import joshuatee.wx.util.Utility

//
// class to represent a value in memory but also simultaneously stored to disk
//

class DataStorage(private val preference: String) {

    private var storedVal = ""

    val value: String
        get() = storedVal

    // update in memory value from what is on disk
    fun update(context: Context) {
        storedVal = Utility.readPref(context, preference, "")
    }

    fun valueSet(context: Context, newValue: String) {
        storedVal = newValue
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString(preference, newValue)
        editor.apply()
    }
}

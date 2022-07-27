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

package joshuatee.wx.ui

import android.app.DatePickerDialog
import android.content.Context
import joshuatee.wx.util.UtilityTime
import java.util.*

//
// Used in SpcStormReportsActivity
//

class ObjectDatePicker(context: Context, var year: Int, var month: Int, var day: Int, private val updateFunc: () -> Unit) {

    private val pDateSetListener =
            DatePickerDialog.OnDateSetListener { _, yearInDate, monthOfYear, dayOfMonth ->
                year = yearInDate
                month = monthOfYear
                day = dayOfMonth
                updateFunc()
            }

    init {
        val stDatePicker = DatePickerDialog(context, pDateSetListener, year, month, day)
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, 2004) // 2011-05-27 was the earliest date for filtered, moved to non-filtered and can go back to 2004-03-23
        cal.set(Calendar.MONTH, 2)
        cal.set(Calendar.DAY_OF_MONTH, 23)
        stDatePicker.datePicker.minDate = cal.timeInMillis - 1000
        stDatePicker.datePicker.maxDate = UtilityTime.currentTimeMillis()
        stDatePicker.setCanceledOnTouchOutside(true)
        stDatePicker.show()
    }
}

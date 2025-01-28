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

package joshuatee.wx.ui

import android.app.DatePickerDialog
import android.content.Context
import joshuatee.wx.objects.ObjectDateTime

//
// Used in SpcStormReportsActivity
//

class DatePicker(
    context: Context,
    var year: Int,
    var month: Int,
    var day: Int,
    private val updateFunc: () -> Unit
) {

    private val pDateSetListener =
        DatePickerDialog.OnDateSetListener { _, yearInDate, monthOfYear, dayOfMonth ->
            year = yearInDate
            month = monthOfYear
            day = dayOfMonth
            updateFunc()
        }

    init {
        val datePickerDialog = DatePickerDialog(context, pDateSetListener, year, month, day)
        // 2011-05-27 was the earliest date for filtered, moved to non-filtered and can go back to 2004-03-23
        with(datePickerDialog) {
            datePicker.minDate = ObjectDateTime.from(2004, 3, 24).toEpochMilli()
            datePicker.maxDate = ObjectDateTime.currentTimeMillis()
            setCanceledOnTouchOutside(true)
            show()
        }
    }
}

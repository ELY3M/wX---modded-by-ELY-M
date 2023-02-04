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

package joshuatee.wx.util

object To {

    fun int(s: String): Int = s.toIntOrNull() ?: 0

    fun float(s: String): Float = s.toFloatOrNull() ?: 0.0f

    fun double(s: String): Double = s.toDoubleOrNull() ?: 0.0

    fun string(i: Int): String = i.toString()

    fun stringPadLeft(s: String, padAmount: Int): String = String.format("%-" + padAmount.toString() + "s", s)

    fun stringPadLeftZeros(s: Int, padAmount: Int): String = String.format("%0" + padAmount.toString() + "d", s)

    fun stringFromFloatFixed(d: Double, precision: Int): String = String.format("%." + precision.toString() + "f", d)
}

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

package joshuatee.wx.models

class RunTimeData {

    // list of model runs
    var listRun = mutableListOf<String>()
    // most recently completed run, what user will see by default
    var mostRecentRun = ""
    var imageCompleteInt = 0
    var imageCompleteStr = ""
    var timeStrConv = ""
    var validTime = ""

    fun listRunAdd(string: String) {
        listRun.add(string)
    }

    fun listRunAddAll(list: List<String>) {
        listRun.addAll(list)
    }

    override fun toString(): String {
        return "$mostRecentRun $imageCompleteStr $timeStrConv $validTime $listRun"
    }
}



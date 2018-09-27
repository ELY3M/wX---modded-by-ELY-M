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

package joshuatee.wx.radar

class Spotter internal constructor(firstName: String, lastName: String, lat: String, lon: String, reportAt: String, email: String, phone: String, uniq: String) {

    var firstName = ""
    var lastName = ""
    var lat = ""
    var lon = ""
    var reportAt = ""
    var email = ""
    var phone = ""
    var uniq = ""
    var latD = 0.0
        private set
    var lonD = 0.0
        private set

    init {
        this.firstName = firstName
        this.lastName = lastName.replace("^ ".toRegex(), "")
        this.lat = lat
        this.lon = lon
        this.reportAt = reportAt
        this.email = email
        this.phone = phone
        this.uniq = uniq
        latD = lat.toDoubleOrNull() ?: 0.0
        lonD = -1.0 * (lon.toDoubleOrNull() ?: 0.0)
    }
}



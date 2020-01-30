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
//modded by ELY M. 

package joshuatee.wx.radar

// I reordered and added all requests for later like add a icon for live camera etc
// ELY M.
//#uniq,icon,live camera,reportAt,lat,lon,callsign,active,moving,dir,phone,email,freq,note,first,last

class Spotter internal constructor(uniq: String, icon: String, camera: String, reportAt: String, lat: String, lon: String, callsign: String, active: String, moving: String, dir: String, phone: String, email: String, freq: String, note: String, firstName: String, lastName: String) {
    var uniq: String = ""
    var camera: String = ""
    var icon: String = ""
    var reportAt: String = ""
    var lat: String = ""
    var lon: String = ""
    var callsign: String = ""
    var active: String = ""
    var moving: String = ""
    var dir: String = ""
    var phone: String = ""
    var email: String = ""
    var freq: String = ""
    var note: String = ""
    var firstName: String = ""
    var lastName: String = ""

    var latD: Double = 0.0
        private set
    var lonD: Double = 0.0
        private set

    init {
        this.uniq = uniq
        this.camera = camera
        this.icon = icon
        this.reportAt = reportAt
        this.lat = lat
        this.lon = lon
        this.callsign = callsign
        this.active = active
        this.moving = moving
        this.dir = dir
        this.phone = phone
        this.email = email
        this.freq = freq
        this.note = note
        this.firstName = firstName
        this.lastName = lastName.replace("^ ".toRegex(), "")

        latD = lat.toDoubleOrNull() ?: 0.0
        lonD = -1.0 * (lon.toDoubleOrNull() ?: 0.0)
    }

    override fun toString(): String {
        return "$firstName $lastName"
    }
}



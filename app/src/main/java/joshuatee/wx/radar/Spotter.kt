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

package joshuatee.wx.radar

import joshuatee.wx.objects.LatLon

// I reordered and added all requests for later like add a icon for live camera etc
// ELY M.
//#uniq,icon,live camera,reportAt,lat,lon,callsign,active,moving,dir,phone,email,freq,note,first,last

class Spotter internal constructor(
    unique: String,
    icon: String,
    camera: String,
    reportAt: String,
    lat: String, 
    lon: String, 
    callsign: String, 
    active: String, 
    moving: String, 
    dir: String, 
    phone: String, 
    email: String, 
    freq: String, 
    note: String, 
    firstName: String, 
    lastName: String
) {    
    var unique = ""
    var camera = ""
    var icon = ""
    var reportAt = ""
    var lat = ""
    var lon = ""
    var callsign = ""
    var active = ""
    var moving = ""
    var dir = ""
    var phone = ""
    var email = ""
    var freq = ""
    var note = ""
    var firstName = ""
    var lastName = ""	
    private var lastNameSort = ""
    val latLon = LatLon(lat, lon)

    init {
        this.unique = unique
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
	    this.lastName = lastName
        //latD = lat.toDoubleOrNull() ?: 0.0
        //lonD = -1.0 * (lon.toDoubleOrNull() ?: 0.0)
    }

    override fun toString() = "$firstName $lastName"
}




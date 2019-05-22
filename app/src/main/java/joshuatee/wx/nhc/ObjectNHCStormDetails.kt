/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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

package joshuatee.wx.nhc

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication

class ObjectNhcStormDetails(val data: String) {

    /*
   <nhc:center>30.8, -68.3<br>
   <nhc:type>Post-Tropical Cyclone<br>
   <nhc:name>Andrea<br>
   <nhc:wallet>AT1<br>
   <nhc:atcf>AL012019<br>
   <nhc:datetime>5:00 PM AST Tue May 21<br>
   <nhc:movement>ENE at 8 mph<br>
   <nhc:pressure>1009 mb<br>
   <nhc:wind>35 mph<br>
   <nhc:headline> ...ANDREA IS A REMNANT LOW... ...THIS IS THE LAST ADVISORY...<br>
     */

    var center = ""
    var type = ""
    var name = ""
    var wallet = ""
    var atcf = ""
    var dateTime = ""
    var movement = ""
    var pressure = ""
    var wind = ""
    var headline = ""

    init {
        center = data.parse("<nhc:center>(.*?)<br> ")
        type = data.parse("<nhc:type>(.*?)<br> ")
        name = data.parse("<nhc:name>(.*?)<br> ")
        wallet = data.parse("<nhc:wallet>(.*?)<br> ")
        atcf = data.parse("<nhc:atcf>(.*?)<br> ")
        dateTime = data.parse("<nhc:datetime>(.*?)<br> ")
        movement = data.parse("<nhc:movement>(.*?)<br> ")
        pressure = data.parse("<nhc:pressure>(.*?)<br> ")
        wind = data.parse("<nhc:wind>(.*?)<br> ")
        headline = data.parse("<nhc:headline>(.*?)<br> ")
    }

    override fun toString (): String {
        var string = center + MyApplication.newline
        string += type + MyApplication.newline
        string += name + MyApplication.newline
        string += wallet + MyApplication.newline
        string += atcf + MyApplication.newline
        string += dateTime + MyApplication.newline
        string += movement + MyApplication.newline
        string += pressure + MyApplication.newline
        string += wind + MyApplication.newline
        string += headline + MyApplication.newline
        return string
    }
}



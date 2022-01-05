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

package joshuatee.wx.activitiesmisc

object UtilityWfoText {

    val labels = listOf(
        "Area Forecast Discussion",
        "Aviation only AFD",
        "Hazardous Weather Outlook",
        "Special Weather Statement",
        "Hydrologic Summary",
        "Hydrologic Outlook",
        "Regional Temp/Precip Summary",
        "Regional Temp/Precip Summary by State",
        "Regional Weather Roundup",
        "Fire Weather Forecast",
        "Public Information Statement",
        "Local Storm Report",
        "Record Event Report",
        "Nearshore Marine Forecast",
        "Hurricane Local Statement",
        "Marine Weather Warning",
        "Coastal Flood Advisory",
        "Areal Flood Watch"
    )

    val codes = listOf(
        "AFD",
        "VFD",
        "HWO",
        "SPS",
        "RVA",
        "ESF",
        "RTP",
        "RTPZZ",
        "RWR",
        "FWF",
        "PNS",
        "LSR",
        "RER",
        "NSH",
        "HLS",
        "MWW",
        "CFW",
        "FFA"
    )

    val codeToName = mapOf(
        "AFD" to "Area Forecast Discussion",
        "VFD" to "Aviation only AFD",
        "HWO" to "Hazardous Weather Outlook",
        "SPS" to "Special Weather Statement",
        "RVA" to "Hydrologic Summary",
        "ESF" to "Hydrologic Outlook",
        "RTP" to "Regional Temp/Precip Summary",
        "RTPZZ" to "Regional Temp/Precip Summary by State",
        "RWR" to "Regional Weather Roundup",
        "FWF" to "Fire Weather Forecast",
        "PNS" to "Public Information Statement",
        "LSR" to "Local Storm Report",
        "RER" to "Record Event Report",
        "NSH" to "Nearshore Marine Forecast",
        "HLS" to "Hurricane Local Statement",
        "MWW" to "Marine Weather Warning",
        "CFW" to "Coastal Flood Advisory",
        "FFA" to "Areal Flood Watch"
    )
}


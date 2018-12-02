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

package joshuatee.wx.activitiesmisc

import joshuatee.wx.MyApplication

internal object UtilityOPCImages {

    val labels = listOf(
            "Atlantic Surface Analysis",
            "Atlantic Wind/Wave Analysis",
            "Atlantic 24-hour 500 mb",
            "Atlantic 48-hour 500 mb",
            "Atlantic 96-hour 500 mb",
            "Atlantic 24-hour Surface",
            "Atlantic 48-hour Surface",
            "Atlantic 96-hour Surface",
            "Atlantic 24-hour Wind & Wave",
            "Atlantic 48-hour Wind & Wave",
            "Atlantic 96-hour Wind & Wave",
            "Atlantic 24-hour Wave period & Direction",
            "Atlantic 48-hour Wave period & Direction",
            "Atlantic 96-hour Wave period & Direction",
            "Pacific Surface Analysis",
            "Pacific Wind/Wave Analysis",
            "Pacific 24-hour 500 mb",
            "Pacific 48-hour 500 mb",
            "Pacific 96-hour 500 mb",
            "Pacific 24-hour Surface",
            "Pacific 48-hour Surface",
            "Pacific 96-hour Surface",
            "Pacific 24-hour Wind & Wave",
            "Pacific 48-hour Wind & Wave",
            "Pacific 96-hour Wind & Wave",
            "Pacific 24-hour Wave period & Direction",
            "Pacific 48-hour Wave period & Direction",
            "Pacific 96-hour Wave period & Direction",
            "Alaska/Arctic Surface Analysis",
            "Alaska/Arctic SST/Ice Edge Analysis",
            "Alaska/Arctic 24-hour Surface",
            "Alaska/Arctic 48-hour Surface",
            "Alaska/Arctic 96-hour Surface",
            "Alaska/Arctic 24-hour Wind & Wave",
            "Alaska/Arctic 48-hour Wind & Wave",
            "Alaska/Arctic 96-hour Wind & Wave",
            "Alaska/Arctic 24-hour Wave period & Direction",
            "Alaska/Arctic 48-hour Wave period & Direction",
            "Alaska/Arctic 96-hour Wave period & Direction"
    )

    val urls = listOf(
            "${MyApplication.nwsOpcWebsitePrefix}/A_sfc_full_ocean_color.png",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/ira1.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/A_24hr500.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/A_48hr500.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/A_96hr500.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/A_24hrsfc.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/A_48hrsfc.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/A_96hrsfc.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/A_24hrww.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/A_48hrww.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/A_96hrww.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/A_024hrwper_color.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/A_048hrwper_color.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/A_096hrwper_color.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/P_sfc_full_ocean_color.png",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/irp1.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/P_24hr500.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/P_48hr500.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/P_96hr500.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/P_24hrsfc.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/P_48hrsfc.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/P_96hrsfc.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/P_24hrww.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/P_48hrww.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/P_96hrww.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/P_024hrwper_color.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/P_048hrwper_color.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/P_096hrwper_color.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/arctic/UA_LATEST.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/arctic/OISSTICE_LATEST.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/arctic/24SFC_LATEST.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/arctic/48SFC_LATEST.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/arctic/96SFC_LATEST.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/arctic/24WW_LATEST.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/arctic/48WW_LATEST.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/arctic/96WW_LATEST.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/AK_024hrwper_color.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/AK_048hrwper_color.gif",
            "${MyApplication.nwsOpcWebsitePrefix}/shtml/AK_096hrwper_color.gif"
    )
}

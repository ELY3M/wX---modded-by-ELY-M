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

package joshuatee.wx.activitiesmisc

import joshuatee.wx.common.GlobalVariables

internal object UtilityOpcImages {

    val labels = listOf(
            "Atlantic Surface Analysis",
            "Atlantic Wind/Wave Analysis",
            "Atlantic 24-hour 500 mb",
            "Atlantic 48-hour 500 mb",
            "Atlantic 72-hour 500 mb",
            "Atlantic 96-hour 500 mb",
            "Atlantic 24-hour Surface",
            "Atlantic 48-hour Surface",
            "Atlantic 72-hour Surface",
            "Atlantic 96-hour Surface",
            "Atlantic 24-hour Wind & Wave",
            "Atlantic 48-hour Wind & Wave",
            "Atlantic 72-hour Wind & Wave",
            "Atlantic 96-hour Wind & Wave",
            "Atlantic 24-hour Wave period & Direction",
            "Atlantic 48-hour Wave period & Direction",
            "Atlantic 72-hour Wave period & Direction",
            "Atlantic 96-hour Wave period & Direction",

            "Pacific Surface Analysis",
            "Pacific Wind/Wave Analysis",
            "Pacific 24-hour 500 mb",
            "Pacific 48-hour 500 mb",
            "Pacific 72-hour 500 mb",
            "Pacific 96-hour 500 mb",
            "Pacific 24-hour Surface",
            "Pacific 48-hour Surface",
            "Pacific 72-hour Surface",
            "Pacific 96-hour Surface",
            "Pacific 24-hour Wind & Wave",
            "Pacific 48-hour Wind & Wave",
            "Pacific 72-hour Wind & Wave",
            "Pacific 96-hour Wind & Wave",
            "Pacific 24-hour Wave period & Direction",
            "Pacific 48-hour Wave period & Direction",
            "Pacific 72-hour Wave period & Direction",
            "Pacific 96-hour Wave period & Direction",

            "Alaska/Arctic Surface Analysis",
            "Alaska/Arctic SST/Ice Edge Analysis",
            "Alaska/Arctic 24-hour Surface",
            "Alaska/Arctic 48-hour Surface",
            "Alaska/Arctic 72-hour Surface",
            "Alaska/Arctic 96-hour Surface",
            "Alaska/Arctic 24-hour Wind & Wave",
            "Alaska/Arctic 48-hour Wind & Wave",
            "Alaska/Arctic 72-hour Wind & Wave",
            "Alaska/Arctic 96-hour Wind & Wave",
            "Alaska/Arctic 24-hour Wave period & Direction",
            "Alaska/Arctic 48-hour Wave period & Direction",
            "Alaska/Arctic 72-hour Wave period & Direction",
            "Alaska/Arctic 96-hour Wave period & Direction",

            "Atlantic Offshore 15m lightning strike density",
            "N Atlantic 15m lightning strike density",
            "S Atlantic 15m lightning strike density",
            "E Tropics 15m lightning strike density",
            "W Tropics 15m lightning strike density",
            "S Pac 15m lightning strike density",
            "N Pac 15m lightning strike density"
    )

    val urls = listOf(
            GlobalVariables.nwsOpcWebsitePrefix + "/A_sfc_full_ocean_color.png",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/ira1.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/A_24hr500.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/A_48hr500.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/A_72hr500.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/A_96hr500.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/A_24hrsfc.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/A_48hrsfc.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/A_72hrsfc.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/A_96hrsfc.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/A_24hrww.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/A_48hrww.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/A_72hrww.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/A_96hrww.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/A_024hrwper_color.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/A_048hrwper_color.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/A_072hrwper_color.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/A_096hrwper_color.gif",

            GlobalVariables.nwsOpcWebsitePrefix + "/P_sfc_full_ocean_color.png",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/irp1.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/P_24hr500.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/P_48hr500.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/P_72hr500.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/P_96hr500.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/P_24hrsfc.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/P_48hrsfc.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/P_72hrsfc.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/P_96hrsfc.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/P_24hrww.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/P_48hrww.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/P_72hrww.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/P_96hrww.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/P_024hrwper_color.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/P_048hrwper_color.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/P_072hrwper_color.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/P_096hrwper_color.gif",

            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/arctic/UA_LATEST.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/arctic/NSSTICE_LATEST.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/arctic/24SFC_LATEST.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/arctic/48SFC_LATEST.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/arctic/72SFC_LATEST.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/arctic/96SFC_LATEST.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/arctic/24WW_LATEST.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/arctic/48WW_LATEST.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/arctic/72WW_LATEST.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/arctic/96WW_LATEST.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/AK_024hrwper_color.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/AK_048hrwper_color.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/AK_072hrwper_color.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/shtml/AK_096hrwper_color.gif",

            GlobalVariables.nwsOpcWebsitePrefix + "/lightning/data/nAtlOff_IR_15min_latest.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/lightning/data/nAtl_IR_15min_latest.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/lightning/data/sAtlOff_IR_15min_latest.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/lightning/data/eTrop_IR_15min_latest.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/lightning/data/wTrop_IR_15min_latest.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/lightning/data/sPacOff_IR_15min_latest.gif",
            GlobalVariables.nwsOpcWebsitePrefix + "/lightning/data/nPacOff_IR_15min_latest.gif"
    )
}

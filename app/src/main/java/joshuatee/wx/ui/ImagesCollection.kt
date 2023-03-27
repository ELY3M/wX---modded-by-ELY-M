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
//Modded by ELY M. - Aurora Forecast

package joshuatee.wx.ui

import joshuatee.wx.activitiesmisc.UtilityOpcImages
import joshuatee.wx.activitiesmisc.UtilityObservations
import joshuatee.wx.vis.UtilityGoesFullDisk
import joshuatee.wx.space.UtilityAurora

class ImagesCollection(val title: String, val urls: List<String>, val labels: List<String>, val prefTokenIdx: String, val prefImagePosition: String) {

    companion object {

        val map = mapOf(
            "OPC" to ImagesCollection(
                    "OPC",
                    UtilityOpcImages.urls,
                    UtilityOpcImages.labels,
                    "OPC_IMG_FAV_IDX",
                    "OPCIMG"),
            "OBSERVATIONS" to ImagesCollection(
                    "Observations",
                    UtilityObservations.urls,
                    UtilityObservations.labels,
                    "SFC_OBS_IMG_IDX",
                    "OBS"),
            "GOESFD" to ImagesCollection(
                    "GOESFD",
                    UtilityGoesFullDisk.urls,
                    UtilityGoesFullDisk.labels,
                    "GOESFULLDISK_IMG_FAV_IDX",
                    "GOESFULLDISKIMG"),
					
            //elys mod
            "AURORA" to ImagesCollection(
                    "AURORA",
                    UtilityAurora.urls,
                    UtilityAurora.labels,
                    "AURORA_IMG_FAV_IDX",
                    "AURORA"),
									
        )
    }
}

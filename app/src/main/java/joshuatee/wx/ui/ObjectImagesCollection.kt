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

package joshuatee.wx.ui

import joshuatee.wx.activitiesmisc.UtilityOPCImages
import joshuatee.wx.activitiesmisc.UtilityObservations
import joshuatee.wx.vis.UtilityNWSGOESFullDisk

class ObjectImagesCollection(
    var title: String,
    var urls: List<String>,
    var labels: List<String>,
    val prefTokenIdx: String,
    val prefImagePosition: String
) {

    companion object {
        fun initialize(): MutableMap<String, ObjectImagesCollection> {
            val collectionMap: MutableMap<String, ObjectImagesCollection> = mutableMapOf()
            collectionMap["OPC"] = ObjectImagesCollection(
                "OPC",
                UtilityOPCImages.urls,
                UtilityOPCImages.labels,
                "OPC_IMG_FAV_IDX",
                "OPCIMG"
            )
            collectionMap["OBSERVATIONS"] = ObjectImagesCollection(
                "Observations",
                UtilityObservations.urls,
                UtilityObservations.labels,
                "SFC_OBS_IMG_IDX",
                "OBS"
            )
            collectionMap["GOESFD"] = ObjectImagesCollection(
                "GOESFD",
                UtilityNWSGOESFullDisk.urls,
                UtilityNWSGOESFullDisk.labels,
                "GOESFULLDISK_IMG_FAV_IDX",
                "GOESFULLDISKIMG"
            )
            return collectionMap
        }
    }
}

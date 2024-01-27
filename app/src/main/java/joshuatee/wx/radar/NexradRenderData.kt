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
//Modded by ELY M. 

package joshuatee.wx.radar

import android.content.Context
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.PolygonWarningType
import joshuatee.wx.settings.RadarPreferences

class NexradRenderData(val context: Context) {

    //elys mod - cant be private
    val zoomToHideMiscFeatures = 0.5f
    val radarBuffers = OglRadarBuffers(RadarPreferences.nexradBackgroundColor)
    val geographicBuffers = mapOf(
            RadarGeometryTypeEnum.StateLines to OglBuffers(RadarGeometryTypeEnum.StateLines, 0.0f),
            RadarGeometryTypeEnum.CaLines to OglBuffers(RadarGeometryTypeEnum.CaLines, 0.0f),
            RadarGeometryTypeEnum.MxLines to OglBuffers(RadarGeometryTypeEnum.MxLines, 0.0f),
            RadarGeometryTypeEnum.CountyLines to OglBuffers(RadarGeometryTypeEnum.CountyLines, 0.75f),
            RadarGeometryTypeEnum.HwLines to OglBuffers(RadarGeometryTypeEnum.HwLines, 0.45f),
            RadarGeometryTypeEnum.HwExtLines to OglBuffers(RadarGeometryTypeEnum.HwExtLines, 3.0f),
            RadarGeometryTypeEnum.LakeLines to OglBuffers(RadarGeometryTypeEnum.LakeLines, zoomToHideMiscFeatures)
    )
    val swoBuffers = OglBuffers(PolygonType.SWO)
    var wpcFrontBuffersList = mutableListOf<OglBuffers>()
    var wpcFrontPaints = mutableListOf<Int>()
    val locationDotBuffers = OglBuffers(PolygonType.LOCDOT)
	//elys mod
    val locIconBuffers = OglBuffers()
    val locBugBuffers = OglBuffers()
	
    val wbCircleBuffers = OglBuffers(PolygonType.WIND_BARB_CIRCLE, zoomToHideMiscFeatures)
    val wbBuffers = OglBuffers(PolygonType.WIND_BARB, zoomToHideMiscFeatures)
    val wbGustsBuffers = OglBuffers(PolygonType.WIND_BARB_GUSTS, zoomToHideMiscFeatures)
    val spotterBuffers = OglBuffers(PolygonType.SPOTTER, zoomToHideMiscFeatures)
    val stiBuffers = OglBuffers(PolygonType.STI, zoomToHideMiscFeatures)
    //elys mod - this is WORST and UGLY way of doing hail icons!  I cant stand this!   
	//val hiBuffers = OglBuffers(PolygonType.HI, zoomToHideMiscFeatures)
    //elys mod - custom hail icons
    var hiBuffersList = mutableListOf<OglBuffers>()	
    val tvsBuffers = OglBuffers(PolygonType.TVS, zoomToHideMiscFeatures)
    val polygonBuffers = mapOf(
            PolygonType.WATCH to OglBuffers(PolygonType.WATCH),
            PolygonType.WATCH_TORNADO to OglBuffers(PolygonType.WATCH_TORNADO),
            PolygonType.MCD to OglBuffers(PolygonType.MCD),
            PolygonType.MPD to OglBuffers(PolygonType.MPD),
    )
    val warningBuffers = mutableMapOf<PolygonWarningType, OglBuffers>()		
	//elys mod
    val userPointsBuffers = OglBuffers(PolygonType.USERPOINTS, zoomToHideMiscFeatures)
	val conusRadarBuffers = OglBuffers()
}

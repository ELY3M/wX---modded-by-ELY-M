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

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import joshuatee.wx.MyApplication
import joshuatee.wx.common.RegExp
import joshuatee.wx.objects.LatLon
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.ui.TextViewMetal
import joshuatee.wx.ui.UtilityUI
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.util.To
import kotlin.math.*

class NexradRenderTextObject(
    private val context: Context,
    private val relativeLayout: RelativeLayout,
    private val wxglSurfaceView: NexradRenderSurfaceView,
    private val state: NexradRenderState,
    private val numberOfPanes: Int,
    private val paneNumber: Int
) {
    private var citiesInitialized = false
    private var countyLabelsInitialized = false
    private var observationsInitialized = false
    private var maxCitiesPerGlview = 16
    private val glviewWidth: Int
    private val glviewHeight: Int
    private var scale = 0.0f
    private var oglrZoom = 0.0f
    private var textSize = 0.0f
    private var projectionNumbers: ProjectionNumbers
    private val textViewFudgeFactor = 4.05f

    init {
        maxCitiesPerGlview /= numberOfPanes
        // locfrag should show fewer then full screen wxogl
        if (numberOfPanes == 1 && !wxglSurfaceView.fullScreen) {
            maxCitiesPerGlview = (maxCitiesPerGlview * 0.6).toInt()
        }
        glviewWidth = if (numberOfPanes != 1) {
            MyApplication.dm.widthPixels / (numberOfPanes / 2)
        } else {
            MyApplication.dm.widthPixels
        }
        glviewHeight = if (numberOfPanes != 1) {
            MyApplication.dm.heightPixels / 2
        } else {
            MyApplication.dm.heightPixels
        }
        projectionNumbers = ProjectionNumbers(state.rid, ProjectionType.WX_OGL)

        if (numberOfPanes == 4) {
            relativeLayout.layoutParams.width = glviewWidth
            relativeLayout.layoutParams.height = glviewHeight
        }

        if (numberOfPanes == 2) {
            if (UtilityUI.isLandScape(context)) {
                relativeLayout.layoutParams.height = glviewWidth
            } else {
                relativeLayout.layoutParams.height = glviewHeight
            }
        }
    }

    private fun addCities() {
        if (RadarPreferences.cities && citiesInitialized) {
            projectionNumbers = ProjectionNumbers(state.rid, ProjectionType.WX_OGL)
            hideCities()
            wxglSurfaceView.cities.clear()
            scale = getScale()
            textSize = UIPreferences.textSizeSmall * RadarPreferences.textSize * 0.80f
            val cityMinZoom = 0.50
            if (state.zoom > cityMinZoom) {
                val cityExtLength = CitiesExtended.labels.size
                for (index in 0 until cityExtLength) {
                    if (wxglSurfaceView.cities.size < maxCitiesPerGlview) {
                        checkAndDrawText(
                            wxglSurfaceView.cities,
                            CitiesExtended.lat[index],
                            CitiesExtended.lon[index],
                            CitiesExtended.labels[index],
                            RadarPreferences.colorCity,
                        )
                    } else {
                        break
                    }
                }
            } else {
                hideCities()
            }
        } else {
            hideCities()
        }
    }

    private fun hideCities() {
        wxglSurfaceView.cities.forEach {
            it.visibility = View.GONE
            relativeLayout.removeView(it)
        }
    }

    private fun initializeCities(context: Context) {
        if (RadarPreferences.cities) {
            citiesInitialized = true
            CitiesExtended.create(context)
        }
    }

    private fun initializeCountyLabels(context: Context) {
        if (RadarPreferences.countyLabels) {
            CountyLabels.create(context)
            countyLabelsInitialized = true
        }
    }

    private fun getScale(): Float =
        8.1f * state.zoom / UIPreferences.deviceScale * (glviewWidth / 800.0f * UIPreferences.deviceScale) / textViewFudgeFactor

    private fun addCountyLabels() {
        if (RadarPreferences.countyLabels && countyLabelsInitialized) {
            projectionNumbers = ProjectionNumbers(state.rid, ProjectionType.WX_OGL)
            hideCountyLabels()
            wxglSurfaceView.countyLabels.clear()
            scale = getScale()
            oglrZoom = 1.0f
            if (state.zoom < 1.0f) {
                oglrZoom = state.zoom * 0.8f
            }
            textSize = UIPreferences.textSizeSmall * oglrZoom * 0.65f * RadarPreferences.textSize
            if (state.zoom > 1.50) {
                CountyLabels.labels.indices.forEach {
                    checkAndDrawText(
                        wxglSurfaceView.countyLabels,
                        CountyLabels.lat[it],
                        CountyLabels.lon[it],
                        CountyLabels.labels[it],
                        RadarPreferences.colorCountyLabels,
                    )
                }
            } else {
                hideCountyLabels()
            }
        } else {
            hideCountyLabels()
        }
    }

    private fun hideCountyLabels() {
        wxglSurfaceView.countyLabels.forEach {
            it.visibility = View.GONE
            relativeLayout.removeView(it)
        }
    }

    fun addSpottersLabels() {
        if (PolygonType.SPOTTER_LABELS.pref) {
            projectionNumbers = ProjectionNumbers(state.rid, ProjectionType.WX_OGL)
            hideSpottersLabels()
            wxglSurfaceView.spotterLabels.clear()
            scale = getScale()
            oglrZoom = 1.0f
            if (state.zoom < 1.0f) {
                oglrZoom = state.zoom * 0.8f
            }
            textSize = UIPreferences.textSizeSmall * oglrZoom * RadarPreferences.textSize * 0.75f
            if (state.zoom > 0.5) {
                // spotter list make copy first
                // multiple bug reports against this
                // look at performance impact
                val spotterListCopy = UtilitySpotter.spotterList.toList()
                spotterListCopy.forEach {
	    	//elys mod
		    val firstName = it.firstName
		    val lastName = it.lastName.replace("0FAV ", "")
            var spotterString  = "$firstName $lastName"
            if (RadarPreferences.spottersLabelLastName) {
                spotterString = lastName
            }
                    checkAndDrawText(
                        wxglSurfaceView.spotterLabels,
                        it.latLon.lat,
                        it.latLon.lon * -1.0,
                            spotterString,
                            RadarPreferences.colorSpotter
                    )
                }
            } else {
                hideSpottersLabels()
            }
        } else {
            hideSpottersLabels()
        }
    }

    //elys mod
    fun addHailLabels() {
        if (PolygonType.HAIL_LABELS.pref) {
            projectionNumbers = ProjectionNumbers(state.rid, ProjectionType.WX_OGL)
            hideHailLabels()
            wxglSurfaceView.hailLabels = mutableListOf()
            scale = getScale()
            oglrZoom = 1.0f
            if (state.zoom < 1.00f) {
                oglrZoom = state.zoom * 0.8f
            }
            textSize = UIPreferences.textSizeSmall * oglrZoom * RadarPreferences.hiTextSize * 0.75f
            if (state.zoom > 0.5) {
                //val hailListCopy = NexradLevel3HailIndex.decode(context, state.rid, state.indexString)
                //hailListCopy.forEach {
		        NexradLevel3HailIndex.hailList.forEach {
                    checkAndDrawText(
                            wxglSurfaceView.hailLabels,
                            it.latD - 0.163 / state.zoom, //move down to under HI icon
                            it.lonD,
                    		it.hailSize,
                            RadarPreferences.colorHiText
                    )
                }
            } else {
                hideHailLabels()
            }
        } else {
            hideHailLabels()
        }
    }
    private fun hideHailLabels() {
        wxglSurfaceView.hailLabels.forEach {
            it.visibility = View.GONE
            relativeLayout.removeView(it)
        }
    }
    private fun checkAndDrawText(
        textViews: MutableList<TextView>,
        lat: Double,
        lon: Double,
        text: String,
        color: Int
    ) {
        val renderX: Float
        val renderY: Float
        if (RadarPreferences.wxoglCenterOnLocation) {
            renderX = state.gpsLatLonTransformed[0]
            renderY = state.gpsLatLonTransformed[1]
        } else {
            renderX = state.x / state.zoom
            renderY = state.y / state.zoom
        }
        val coordinates = Projection.computeMercatorNumbers(lat, lon, projectionNumbers)
        coordinates[0] = coordinates[0] + renderX
        coordinates[1] = coordinates[1] - renderY
        // hack fix for quad pane: glviewWidth - 200
        if (abs(coordinates[0] * scale) < glviewWidth && abs(coordinates[1] * scale) < glviewHeight) {
            val textView = TextViewMetal(context, text, color, textSize)
            textViews.add(textView.getView())
            relativeLayout.addView(textView.getView())
            if ((coordinates[1] * scale).toInt() < 0) {
                if ((coordinates[0] * scale).toInt() < 0)
                    textView.setPadding(
                        0,
                        0,
                        (-(coordinates[0] * scale)).toInt(),
                        (-(coordinates[1] * scale)).toInt()
                    )
                else
                    textView.setPadding(
                        (coordinates[0] * scale).toInt(),
                        0,
                        0,
                        (-(coordinates[1] * scale)).toInt()
                    )
            } else {
                if ((coordinates[0] * scale).toInt() < 0)
                    textView.setPadding(
                        0,
                        (coordinates[1] * scale).toInt(),
                        (-(coordinates[0] * scale)).toInt(),
                        0
                    )
                else
                    textView.setPadding(
                        (coordinates[0] * scale).toInt(),
                        (coordinates[1] * scale).toInt(),
                        0,
                        0
                    )
            }
        }
    }

    private fun checkButDoNotDrawText(
        textViews: MutableList<TextView>,
        lat: Double,
        lon: Double,
        color: Int,
        textSizeTv: Float,
        singleLine: Boolean
    ): Boolean {
        val coordinates = Projection.computeMercatorNumbers(lat, lon, projectionNumbers)
        if (RadarPreferences.wxoglCenterOnLocation) {
            coordinates[0] = coordinates[0] + state.gpsLatLonTransformed[0]
            coordinates[1] = coordinates[1] - state.gpsLatLonTransformed[1]
        } else {
            coordinates[0] = coordinates[0] + state.x / state.zoom
            coordinates[1] = coordinates[1] - state.y / state.zoom
        }
        var drawText = false
        if (abs(coordinates[0] * scale) < glviewWidth && abs(coordinates[1] * scale) < glviewHeight) {
            drawText = true
            val textView =
                TextViewMetal(
                    context,
                    "",
                    color,
                    textSizeTv,
                    singleLine,
                    drawText = false
                )
            textViews.add(textView.getView())
            relativeLayout.addView(textView.getView())
            if ((coordinates[1] * scale).toInt() < 0) {
                if ((coordinates[0] * scale).toInt() < 0)
                    textView.setPadding(
                        0,
                        0,
                        (-(coordinates[0] * scale)).toInt(),
                        (-(coordinates[1] * scale)).toInt()
                    )
                else
                    textView.setPadding(
                        (coordinates[0] * scale).toInt(),
                        0,
                        0,
                        (-(coordinates[1] * scale)).toInt()
                    )
            } else {
                if ((coordinates[0] * scale).toInt() < 0)
                    textView.setPadding(
                        0,
                        (coordinates[1] * scale).toInt(),
                        (-(coordinates[0] * scale)).toInt(),
                        0
                    )
                else
                    textView.setPadding(
                        (coordinates[0] * scale).toInt(),
                        (coordinates[1] * scale).toInt(),
                        0,
                        0
                    )
            }
        }
        return drawText
    }

    private fun hideSpottersLabels() {
        wxglSurfaceView.spotterLabels.forEach {
            it.visibility = View.GONE
            relativeLayout.removeView(it)
        }
    }

    private fun addSpotter() {
        if (WXGLRadarActivity.spotterShowSelected) {
            projectionNumbers = ProjectionNumbers(state.rid, ProjectionType.WX_OGL)
            var report = false
            hideSpotter()
            wxglSurfaceView.spotterTextView.clear()
            var indexSpotter = 0
            while (indexSpotter < UtilitySpotter.spotterList.size) {
                if (UtilitySpotter.spotterList[indexSpotter].unique == WXGLRadarActivity.spotterId) {
                    break
                }
                indexSpotter += 1
            }
            var indexSpotterReport = 0
            while (indexSpotterReport < UtilitySpotter.reports.size) {
                if (UtilitySpotter.reports[indexSpotterReport].uniq == WXGLRadarActivity.spotterId) {
                    report = true
                    break
                }
                indexSpotterReport += 1
            }
            val latLon = if (!report) {
                val spotter = UtilitySpotter.spotterList.getOrNull(indexSpotter)
                spotter?.latLon ?: LatLon.empty()
            } else {
                val spotter = UtilitySpotter.reports.getOrNull(indexSpotterReport)
                spotter?.latLon ?: LatLon.empty()
            }
            scale = getScale()
            oglrZoom = 1.0f
            if (state.zoom < 1.0f) {
                oglrZoom = state.zoom * 0.8f
            }
            if (state.zoom > 0.5) {
                val drawText = checkButDoNotDrawText(
                    wxglSurfaceView.spotterTextView,
                    latLon.lat,
                    latLon.lon * -1.0,
                    RadarPreferences.colorSpotter,
                    UIPreferences.textSizeSmall * oglrZoom * 1.5f * RadarPreferences.textSize,
                    true
                )
                if (drawText) {
                    if (!report) {
                        //elys mod
                        val firstName = UtilitySpotter.spotterList[indexSpotter].firstName
                        val lastName = UtilitySpotter.spotterList[indexSpotter].lastName.replace("0FAV ", "")
                        var spotterString  = "$firstName $lastName"
                        if (RadarPreferences.spottersLabelLastName) {
                            spotterString = lastName
                        }
                        wxglSurfaceView.spotterTextView[0].text = spotterString
                    } else {
                        wxglSurfaceView.spotterTextView[0].text = UtilitySpotter.reports[indexSpotterReport].type
                    }
                }
            } else {
                hideSpotter()
            }
        }
    }

    private fun hideSpotter() {
        if (WXGLRadarActivity.spotterShowSelected) {
            wxglSurfaceView.spotterTextView.forEach {
                it.visibility = View.GONE
                relativeLayout.removeView(it)
            }
        }
    }

    fun initializeLabels(context: Context) {
        initializeCities(context)
        initializeCountyLabels(context)
    }

    fun addLabels() {
        addCities()
        addCountyLabels()
        addObservations()
        addSpottersLabels()
        if (numberOfPanes == 1 && WXGLRadarActivity.spotterShowSelected) {
            addSpotter()
        }
        //elys mod
        addHailLabels()
        addWpcPressureCenters()
    }

    fun hideLabels() {
        hideCities()
        hideCountyLabels()
        hideObservations()
        hideSpottersLabels()
        if (numberOfPanes == 1 && WXGLRadarActivity.spotterShowSelected) {
            hideSpotter()
        }
        //elys mod
        hideHailLabels()
        hideWpcPressureCenters()
    }

    fun initializeObservations() {
        if (PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) {
            observationsInitialized = true
        }
    }

    fun addWpcPressureCenters() {
        if (PolygonType.WPC_FRONTS.pref) {
            projectionNumbers = ProjectionNumbers(state.rid, ProjectionType.WX_OGL)
            hideWpcPressureCenters()
            wxglSurfaceView.pressureCenterLabels.clear()
            scale = getScale()
            oglrZoom = 1.0f
            if (state.zoom < 1.0f) {
                oglrZoom = state.zoom * 0.8f
            }
            textSize = UIPreferences.textSizeNormal * RadarPreferences.textSize
            if (state.zoom < (0.5 / state.zoomScreenScaleFactor)) {
                WpcFronts.pressureCenters.forEach {
                    var color = Color.rgb(0, 127, 225)
                    if (it.type == PressureCenterTypeEnum.LOW) {
                        color = Color.RED
                    }
                    checkAndDrawText(
                        wxglSurfaceView.pressureCenterLabels,
                        it.lat,
                        it.lon,
                        it.pressureInMb,
                        color,
                    )
                }
            } else {
                hideWpcPressureCenters()
            }
        } else {
            hideWpcPressureCenters()
        }
    }

    fun addObservations() {
        if ((PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) && observationsInitialized) {
            val obsExtZoom = RadarPreferences.obsExtZoom.toDouble()
            projectionNumbers = ProjectionNumbers(state.rid, ProjectionType.WX_OGL)
            var lat = 0.0
            var lon = 0.0
            hideObservations()
            wxglSurfaceView.observations.clear()
            scale = getScale()
            oglrZoom = 1.0f
            textSize = UIPreferences.textSizeSmall * oglrZoom * RadarPreferences.textSize * 0.75f
            val obsArr = Metar.data[paneNumber].obsArr.toList()
            val obsArrExt = Metar.data[paneNumber].obsArrExt.toList()
            if (state.zoom > 0.5) {
                obsArr.indices.forEach {
                    if (it < obsArr.size && it < obsArrExt.size) {
                        val observations = RegExp.colon.split(obsArr[it])
                        val observationsExtended = RegExp.colon.split(obsArrExt[it])
                        if (observations.size > 1) {
                            lat = To.double(observations[0])
                            lon = To.double(observations[1])
                        }
                        val drawText = checkButDoNotDrawText(
                            wxglSurfaceView.observations,
                            lat,
                            lon * -1.0,
                            RadarPreferences.colorObs,
                            textSize,
                            false
                        )
                        if (drawText) {
                            if (state.zoom > obsExtZoom) {
                                wxglSurfaceView.observations.last().text = observationsExtended[2]
                            } else if (PolygonType.OBS.pref) {
                                wxglSurfaceView.observations.last().text = observations[2]
                            }
                        }
                    }
                }
            } else {
                hideObservations()
            }
        } else {
            hideObservations()
        }
    }

    private fun hideObservations() {
        wxglSurfaceView.observations.forEach {
            it.visibility = View.GONE
            relativeLayout.removeView(it)
        }
    }

    private fun hideWpcPressureCenters() {
        wxglSurfaceView.pressureCenterLabels.forEach {
            it.visibility = View.GONE
            relativeLayout.removeView(it)
        }
    }

    companion object {

        fun hideLabels(textObjects: List<NexradRenderTextObject>) {
            textObjects.forEach {
                it.hideLabels()
            }
        }

        fun showLabels(textObjects: List<NexradRenderTextObject>) {
            textObjects.forEach {
                it.addLabels()
            }
        }

        fun updateSpotterLabels(textObjects: List<NexradRenderTextObject>) {
            textObjects.forEach {
                it.addSpottersLabels()
            }
        }

        fun updateObservations(textObjects: List<NexradRenderTextObject>) {
            textObjects.forEach {
                it.initializeObservations()
                it.addObservations()
            }
        }

        fun updateWpcFronts(textObjects: List<NexradRenderTextObject>) {
            textObjects.forEach {
                it.addWpcPressureCenters()
            }
        }
        //elys mod
        fun updateHailLabels(textObjects: List<NexradRenderTextObject>) {
            textObjects.forEach {
                it.addHailLabels()
            }
        }
    }
}

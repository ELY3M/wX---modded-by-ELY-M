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
//modded by ELY M.

package joshuatee.wx.radar

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.common.RegExp
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.util.UtilityCanvasProjection

import kotlin.math.*

class WXGLTextObject(
        private val context: Context,
        private val relativeLayout: RelativeLayout,
        private val wxglSurfaceView: WXGLSurfaceView,
        private var wxglRender: WXGLRender,
        private val numberOfPanes: Int,
        private val paneNumber: Int
) {
    private var layoutParams: RelativeLayout.LayoutParams
    private var citiesInitialized = false
    private var countyLabelsInitialized = false

    
    private var observationsInitialized = false
    //private var obsTvArrInit = false
    //private val obsSingleLabelTvArrInit = false
    private var obsLat = 0.0
    private var obsLon = 0.0


    private var spotterLat = 0.0
    private var spotterLon = 0.0


    private var initializehailLabels = false
    private val hailSingleLabelTvArrInit = false
    private var hailLat = 0.0
    private var hailLon = 0.0

    private var maxCitiesPerGlview = 16
    private val glviewWidth: Int
    private val glviewHeight: Int
    private var scale = 0.0f
    private var oglrZoom = 0.0f
    private var textSize = 0.0f
    private var projectionNumbers: ProjectionNumbers
    private val textViewFudgeFactor = 4.05f

    init {
        this.maxCitiesPerGlview = maxCitiesPerGlview / numberOfPanes
        // locfrag should show fewer then full screen wxogl
        if (numberOfPanes == 1 && !wxglSurfaceView.fullScreen) {
            this.maxCitiesPerGlview = (maxCitiesPerGlview * 0.60).toInt()
        }
        if (numberOfPanes != 1) {
            this.glviewWidth = MyApplication.dm.widthPixels / (numberOfPanes / 2)
        } else {
            this.glviewWidth = MyApplication.dm.widthPixels
        }
        if (numberOfPanes != 1) {
            this.glviewHeight = MyApplication.dm.heightPixels / 2
        } else {
            this.glviewHeight = MyApplication.dm.heightPixels
        }
        projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
        layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
    }

    private fun addCities() {
        if (GeographyType.CITIES.pref && citiesInitialized) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            hideCities()
            wxglSurfaceView.cities.clear()
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.00f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            textSize = UIPreferences.textSizeSmall * oglrZoom * 0.75f * RadarPreferences.radarTextSize
            val cityMinZoom = 0.50
            if (wxglRender.zoom > cityMinZoom) {
                val cityExtLength = UtilityCitiesExtended.cities.size
                for (index in 0 until cityExtLength) {
                    if (wxglSurfaceView.cities.size < maxCitiesPerGlview) {
                        checkAndDrawText(
                                wxglSurfaceView.cities,
                                UtilityCitiesExtended.cityLat[index],
                                UtilityCitiesExtended.cityLon[index],
                                UtilityCitiesExtended.cityLabels[index],
                                RadarPreferences.radarColorCity
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
        for (index in 0 until wxglSurfaceView.cities.size) {
            wxglSurfaceView.cities[index].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.cities[index])
        }
    }

    private fun initializeCities(context: Context) {
        if (GeographyType.CITIES.pref) {
            citiesInitialized = true
            UtilityCitiesExtended.create(context)
        }
    }

    private fun initializeCountyLabels(context: Context) {
        if (RadarPreferences.radarCountyLabels) {
            UtilityCountyLabels.create(context)
            countyLabelsInitialized = true
        }
    }

    private fun getScale() = 8.1f * wxglRender.zoom / UIPreferences.deviceScale * (glviewWidth / 800.0f * UIPreferences.deviceScale) / textViewFudgeFactor

    private fun addCountyLabels() {
        if (RadarPreferences.radarCountyLabels && countyLabelsInitialized) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            hideCountyLabels()
            wxglSurfaceView.countyLabels.clear()
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.00f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            textSize = UIPreferences.textSizeSmall * oglrZoom * 0.65f * RadarPreferences.radarTextSize
            if (wxglRender.zoom > 1.50) {
                UtilityCountyLabels.countyName.indices.forEach {
                    checkAndDrawText(
                            wxglSurfaceView.countyLabels,
                            UtilityCountyLabels.countyLat[it],
                            UtilityCountyLabels.countyLon[it],
                            UtilityCountyLabels.countyName[it],
                            RadarPreferences.radarColorCountyLabels
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
        wxglSurfaceView.countyLabels.indices.forEach {
            wxglSurfaceView.countyLabels[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.countyLabels[it])
        }
    }

    fun addSpottersLabels() {
        if (PolygonType.SPOTTER_LABELS.pref) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            spotterLat = 0.0
            spotterLon = 0.0
            hideSpottersLabels()
            wxglSurfaceView.spotterLabels.clear()
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.00f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            textSize = UIPreferences.textSizeSmall * oglrZoom * RadarPreferences.radarTextSize * 0.75f
            if (wxglRender.zoom > 0.5) {
                // spotter list make copy first
                // multiple bug reports against this
                // look at performance impact
                val spotterListCopy = UtilitySpotter.spotterList.toList()
                spotterListCopy.indices.forEach {
                    checkAndDrawText(
                            wxglSurfaceView.spotterLabels,
                            spotterListCopy[it].latD,
                            spotterListCopy[it].lonD,
                            spotterListCopy[it].lastName.replace("0FAV ", ""),
                            RadarPreferences.radarColorSpotter
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
    fun initializeHailLabels() {
        if (PolygonType.HAIL_LABELS.pref) initializehailLabels = true
    }

    fun addHailLabels() {
        if (PolygonType.HAIL_LABELS.pref && initializehailLabels) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            hailLat = 0.0
            hailLon = 0.0
            hideHailLabels()
            wxglSurfaceView.hailLabels = mutableListOf()
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.00f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            textSize = UIPreferences.textSizeSmall * oglrZoom * RadarPreferences.radarHiTextSize * 0.75f
            if (wxglRender.zoom > 0.5) {
                // FIXME make copy first
                WXGLNexradLevel3HailIndex.hailList.indices.forEach {
                    checkAndDrawText(
                            wxglSurfaceView.hailLabels,
                            WXGLNexradLevel3HailIndex.hailList[it].lat - 0.163 / wxglRender.zoom, //move down to under HI icon
                            WXGLNexradLevel3HailIndex.hailList[it].lon,
                            WXGLNexradLevel3HailIndex.hailList[it].hailSize,
                            RadarPreferences.radarColorHiText
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
        wxglSurfaceView.hailLabels.indices.forEach {
            wxglSurfaceView.hailLabels[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.hailLabels[it])
        }
    }

    private fun checkAndDrawText(textViews: MutableList<TextView>, lat: Double, lon: Double, text: String, color: Int) {
        val coordinates = UtilityCanvasProjection.computeMercatorNumbers(lat, lon, projectionNumbers)
        val renderX: Float
        val renderY: Float
        if (RadarPreferences.wxoglCenterOnLocation) {
            renderX = wxglRender.gpsLatLonTransformed[0]
            renderY = wxglRender.gpsLatLonTransformed[1]
        } else {
            renderX = wxglRender.x / wxglRender.zoom
            renderY = wxglRender.y / wxglRender.zoom
        }
        coordinates[0] = coordinates[0] + renderX
        coordinates[1] = coordinates[1] - renderY
        if (abs(coordinates[0] * scale) < glviewWidth && abs(coordinates[1] * scale) < glviewHeight) {
            val textView = TextView(context)
            textViews.add(textView)
            textView.setTextColor(color)
            textView.setShadowLayer(1.5f, 2.0f, 2.0f, R.color.black)
            relativeLayout.addView(textView)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            if ((coordinates[1] * scale).toInt() < 0) {
                if ((coordinates[0] * scale).toInt() < 0)
                    textView.setPadding(0, 0, (-(coordinates[0] * scale)).toInt(), (-(coordinates[1] * scale)).toInt())
                else
                    textView.setPadding((coordinates[0] * scale).toInt(), 0, 0, (-(coordinates[1] * scale)).toInt())
            } else {
                if ((coordinates[0] * scale).toInt() < 0)
                    textView.setPadding(0, (coordinates[1] * scale).toInt(), (-(coordinates[0] * scale)).toInt(), 0)
                else
                    textView.setPadding((coordinates[0] * scale).toInt(), (coordinates[1] * scale).toInt(), 0, 0)
            }
            layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
            textView.layoutParams = layoutParams
            textView.text = text
        }
    }

    private fun checkButDoNotDrawText(textViews: MutableList<TextView>, lat: Double, lon: Double, color: Int, textSizeTv: Float): Boolean {
        val coordinates = UtilityCanvasProjection.computeMercatorNumbers(lat, lon, projectionNumbers)
        if (RadarPreferences.wxoglCenterOnLocation) {
            coordinates[0] = coordinates[0] + wxglRender.gpsLatLonTransformed[0]
            coordinates[1] = coordinates[1] - wxglRender.gpsLatLonTransformed[1]
        } else {
            coordinates[0] = coordinates[0] + wxglRender.x / wxglRender.zoom
            coordinates[1] = coordinates[1] - wxglRender.y / wxglRender.zoom
        }
        var drawText = false
        if (abs(coordinates[0] * scale) < glviewWidth && abs(coordinates[1] * scale) < glviewHeight) {
            drawText = true
            val textView = TextView(context)
            textViews.add(textView)
            textView.setTextColor(color)
            textView.setShadowLayer(1.5f, 2.0f, 2.0f, R.color.black)
            relativeLayout.addView(textView)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeTv)
            if ((coordinates[1] * scale).toInt() < 0) {
                if ((coordinates[0] * scale).toInt() < 0)
                    textView.setPadding(0, 0, (-(coordinates[0] * scale)).toInt(), (-(coordinates[1] * scale)).toInt())
                else
                    textView.setPadding((coordinates[0] * scale).toInt(), 0, 0, (-(coordinates[1] * scale)).toInt())
            } else {
                if ((coordinates[0] * scale).toInt() < 0)
                    textView.setPadding(0, (coordinates[1] * scale).toInt(), (-(coordinates[0] * scale)).toInt(), 0)
                else
                    textView.setPadding((coordinates[0] * scale).toInt(), (coordinates[1] * scale).toInt(), 0, 0)
            }
            layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
            textView.layoutParams = layoutParams
        }
        return drawText
    }

    private fun hideSpottersLabels() {
        wxglSurfaceView.spotterLabels.indices.forEach {
            wxglSurfaceView.spotterLabels[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.spotterLabels[it])
        }
    }

    private fun addSpotter() {
        if (WXGLRadarActivity.spotterShowSelected) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            val spotterLat: Double
            val spotterLon: Double
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
            if (!report) {
                spotterLat = UtilitySpotter.spotterList[indexSpotter].lat.toDoubleOrNull() ?: 0.0
                spotterLon = UtilitySpotter.spotterList[indexSpotter].lon.toDoubleOrNull() ?: 0.0
            } else {
                spotterLat = UtilitySpotter.reports[indexSpotterReport].lat.toDoubleOrNull() ?: 0.0
                spotterLon = UtilitySpotter.reports[indexSpotterReport].lon.toDoubleOrNull() ?: 0.0
            }
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.0f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            if (wxglRender.zoom > 0.5) {
                for (c in 0 until 1) {
                   val drawText = checkButDoNotDrawText(
                            wxglSurfaceView.spotterTextView,
                            spotterLat,
                            spotterLon * -1,
                           RadarPreferences.radarColorSpotter,
                           UIPreferences.textSizeSmall * oglrZoom * 1.5f * RadarPreferences.radarTextSize
                    )
                    if (drawText) {
                        if (!report) {
                            wxglSurfaceView.spotterTextView[c].text =
                                    UtilitySpotter.spotterList[indexSpotter].lastName.replace("0FAV ", "")
                        } else {
                            wxglSurfaceView.spotterTextView[c].text = UtilitySpotter.reports[indexSpotterReport].type
                        }
                    }

                }
            } else {
                hideSpotter()
            }
        }
    }

    private fun hideSpotter() {
        if (WXGLRadarActivity.spotterShowSelected) {
            wxglSurfaceView.spotterTextView.indices.forEach {
                wxglSurfaceView.spotterTextView[it].visibility = View.GONE
                relativeLayout.removeView(wxglSurfaceView.spotterTextView[it])
            }
        }
    }

    fun initializeLabels(context: Context) {
        initializeCities(context)
        initializeCountyLabels(context)
        ///TODO TEST THIS!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //initTVSpottersLabels()
        initializeHailLabels()
    }

    fun addLabels() {
        addCities()
        addCountyLabels()
        addObservations()
        addSpottersLabels()
        if (numberOfPanes == 1 && WXGLRadarActivity.spotterShowSelected) { 
	addSpotter()
	}
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
        hideHailLabels()
        hideWpcPressureCenters()
    }

    fun initializeObservations() {
        if (PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) {
            observationsInitialized = true
        }
    }

    fun addWpcPressureCenters() {
        if (RadarPreferences.radarShowWpcFronts) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            hideWpcPressureCenters()
            wxglSurfaceView.pressureCenterLabels.clear()
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.00f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            textSize = UIPreferences.textSizeNormal * RadarPreferences.radarTextSize
            if (wxglRender.zoom < (0.5 / wxglRender.zoomScreenScaleFactor)) {
                UtilityWpcFronts.pressureCenters.indices.forEach {
                    var color = Color.rgb(0,127,225)
                    if (UtilityWpcFronts.pressureCenters[it].type == PressureCenterTypeEnum.LOW) {
                        color = Color.RED
                    }
                    checkAndDrawText(
                            wxglSurfaceView.pressureCenterLabels,
                            UtilityWpcFronts.pressureCenters[it].lat,
                            UtilityWpcFronts.pressureCenters[it].lon,
                            UtilityWpcFronts.pressureCenters[it].pressureInMb,
                            color
                    )
                }
		//elys mod
                ///Fix to hide the wpc pressure center texts from conus radar.
                if (RadarPreferences.radarConusRadar) {
                    if (wxglRender.zoom < (RadarPreferences.radarConusRadarZoom / 1000.0).toFloat()) {
                        hideWpcPressureCenters()
                    }
                }
                //

            } else {
                hideWpcPressureCenters()
            }
        } else {
            hideWpcPressureCenters()
        }
    }

    fun addObservations() {
        if ((PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) && observationsInitialized) {
            val obsExtZoom = RadarPreferences.radarObsExtZoom.toDouble()
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            obsLat = 0.0
            obsLon = 0.0
            val fontScaleFactorObs = 0.65f
            hideObservations()
            wxglSurfaceView.observations.clear()
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.0f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            textSize = UIPreferences.textSizeSmall * oglrZoom * fontScaleFactorObs * RadarPreferences.radarTextSize
            val obsArr = UtilityMetar.metarDataList[paneNumber].obsArr.toList()
            val obsArrExt = UtilityMetar.metarDataList[paneNumber].obsArrExt.toList()
            if (wxglRender.zoom > 0.5) {
                obsArr.indices.forEach {
                    if (it < obsArr.size && it < obsArrExt.size) {
                        val observations = RegExp.colon.split(obsArr[it])
                        val observationsExtended = RegExp.colon.split(obsArrExt[it])
                        if (observations.size > 1) {
                            obsLat = observations[0].toDoubleOrNull() ?: 0.0
                            obsLon = observations[1].toDoubleOrNull() ?: 0.0
                        }
                        val drawText = checkButDoNotDrawText(
                                wxglSurfaceView.observations,
                            obsLat,
                            obsLon * -1,
                                RadarPreferences.radarColorObs,
                                textSize
                        )
                        if (drawText) {
                            if (wxglRender.zoom > obsExtZoom) {
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
        wxglSurfaceView.observations.indices.forEach {
            wxglSurfaceView.observations[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.observations[it])
        }
    }

    private fun hideWpcPressureCenters() {
        wxglSurfaceView.pressureCenterLabels.indices.forEach {
            wxglSurfaceView.pressureCenterLabels[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.pressureCenterLabels[it])
        }
    }

    fun setWXGLRender(wxglRender: WXGLRender) {
        this.wxglRender = wxglRender
    }
}

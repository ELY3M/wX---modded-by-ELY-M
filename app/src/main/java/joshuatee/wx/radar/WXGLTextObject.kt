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
//modded by ELY M.

package joshuatee.wx.radar

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.objects.GeographyType
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.ProjectionType
import joshuatee.wx.util.ProjectionNumbers
import joshuatee.wx.util.UtilityCanvasProjection
import joshuatee.wx.util.UtilityLog

import kotlin.math.*

// TODO camelcase methods

class WXGLTextObject(
        private val context: Context,
        private val relativeLayout: RelativeLayout,
        private val wxglSurfaceView: WXGLSurfaceView,
        private var wxglRender: WXGLRender,
        private val numberOfPanes: Int,
        private val paneNumber: Int
) {
    private var layoutParams: RelativeLayout.LayoutParams
    private var cityextTvArrInit = false
    private var countyLabelsTvArrInit = false


    private var obsTvArrInit = false
    //private val obsSingleLabelTvArrInit = false
    private var obsLat = 0.toDouble()
    private var obsLon = 0.toDouble()


    private var spottersLabelsTvArrInit = false
    private val spotterSingleLabelTvArrInit = false
    private var spotterLat = 0.toDouble()
    private var spotterLon = 0.toDouble()


    private var hailLabelsTvArrInit = false
    private val hailSingleLabelTvArrInit = false
    private var hailLat = 0.toDouble()
    private var hailLon = 0.toDouble()

    private var maxCitiesPerGlview = 16
    // TODO variable naming
    private var ii = 0
    private val glviewWidth: Int
    private val glviewHeight: Int
    private var scale = 0.toFloat()
    private var oglrZoom = 0.toFloat()
    private var textSize = 0.toFloat()
    private var projectionNumbers: ProjectionNumbers
    private val textViewFudgeFactor = 4.05f

    init {
        this.maxCitiesPerGlview = maxCitiesPerGlview / numberOfPanes
        // locfrag should show fewer then full screen wxogl
        if (numberOfPanes == 1 && !wxglSurfaceView.fullScreen)
            this.maxCitiesPerGlview = (maxCitiesPerGlview * 0.60).toInt()
        if (numberOfPanes != 1)
            this.glviewWidth = MyApplication.dm.widthPixels / (numberOfPanes / 2)
        else
            this.glviewWidth = MyApplication.dm.widthPixels
        if (numberOfPanes != 1)
            this.glviewHeight = MyApplication.dm.heightPixels / 2
        else
            this.glviewHeight = MyApplication.dm.heightPixels
        projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
        layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
    }

    private fun addTextLabelsCitiesExtended() {
        if (GeographyType.CITIES.pref && cityextTvArrInit) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            hideCitiesExtended()
            wxglSurfaceView.citiesExtAl = mutableListOf()
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.00f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            textSize = MyApplication.textSizeSmall * oglrZoom * 0.75f * MyApplication.radarTextSize
            val cityMinZoom = 0.50
            if (wxglRender.zoom > cityMinZoom) {
                val cityExtLength = UtilityCitiesExtended.cities.size
                for (c in 0 until cityExtLength) {
                    if (wxglSurfaceView.citiesExtAl.size > maxCitiesPerGlview) {
                        break
                    }
                    checkAndDrawText(
                            wxglSurfaceView.citiesExtAl,
                            UtilityCitiesExtended.cities[c].latD,
                            UtilityCitiesExtended.cities[c].lonD,
                            UtilityCitiesExtended.cities[c].name,
                            MyApplication.radarColorCity
                    )
                }
            } else {
                hideCitiesExtended()
            }
        } else {
            hideCitiesExtended()
        }
    }

    private fun hideCitiesExtended() {
        wxglSurfaceView.citiesExtAl.indices.forEach {
            wxglSurfaceView.citiesExtAl[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.citiesExtAl[it])
        }
    }

    private fun initializeTextLabelsCitiesExtended(context: Context) {
        if (GeographyType.CITIES.pref) {
            cityextTvArrInit = true
            UtilityCitiesExtended.create(context)
        }
    }

    private fun initializeTextLabelsCountyLabels(context: Context) {
        if (MyApplication.radarCountyLabels) {
            UtilityCountyLabels.create(context)
            countyLabelsTvArrInit = true
        }
    }

    private fun getScale() =
            8.1f * wxglRender.zoom / MyApplication.deviceScale * (glviewWidth / 800.0f * MyApplication.deviceScale) / textViewFudgeFactor

    private fun addTextLabelsCountyLabels() {
        if (MyApplication.radarCountyLabels && countyLabelsTvArrInit) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            hideCountyLabels()
            wxglSurfaceView.countyLabelsAl = mutableListOf()
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.00f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            textSize = MyApplication.textSizeSmall * oglrZoom * 0.65f * MyApplication.radarTextSize
            if (wxglRender.zoom > 1.50) {
                UtilityCountyLabels.countyName.indices.forEach {
                    checkAndDrawText(
                            wxglSurfaceView.countyLabelsAl,
                            UtilityCountyLabels.countyLat[it],
                            UtilityCountyLabels.countyLon[it],
                            UtilityCountyLabels.countyName[it],
                            MyApplication.radarColorCountyLabels
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
        wxglSurfaceView.countyLabelsAl.indices.forEach {
            wxglSurfaceView.countyLabelsAl[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.countyLabelsAl[it])
        }
    }

    fun addTextLabelsSpottersLabels() {
        if (PolygonType.SPOTTER_LABELS.pref) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            spotterLat = 0.0
            spotterLon = 0.0
            hideSpottersLabels()
            wxglSurfaceView.spotterLabels = mutableListOf()
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.00f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            textSize = MyApplication.textSizeSmall * oglrZoom * MyApplication.radarTextSize * 0.75f
            if (wxglRender.zoom > 0.5) {
                // spotter list make copy first
                // multiple bug reports against this
                // look at performance impact
                val spotterListCopy = UtilitySpotter.spotterList.toMutableList()
                spotterListCopy.indices.forEach {
                    checkAndDrawText(
                            wxglSurfaceView.spotterLabels,
                            spotterListCopy[it].latD,
                            spotterListCopy[it].lonD,
                            spotterListCopy[it].lastName.replace("0FAV ", ""),
                            MyApplication.radarColorSpotter
                    )
                }
            } else {
                hideSpottersLabels()
            }
        } else {
            hideSpottersLabels()
        }
    }

    fun initTVHailLabels() {
        if (PolygonType.HAIL_LABELS.pref) hailLabelsTvArrInit = true
    }

    fun addTVHailLabels() {
        if (PolygonType.HAIL_LABELS.pref && hailLabelsTvArrInit) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            hailLat = 0.0
            hailLon = 0.0
            hideHailLabels()
            wxglSurfaceView.hailLabelAl = mutableListOf()
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.00f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            textSize = MyApplication.textSizeSmall * oglrZoom * MyApplication.radarHiTextSize * 0.75f
            if (wxglRender.zoom > 0.5) {
                // FIXME make copy first
                WXGLNexradLevel3HailIndex.hailList.indices.forEach {
                    checkAndDrawText(
                            wxglSurfaceView.hailLabelAl,
                            WXGLNexradLevel3HailIndex.hailList[it].lat - 0.130 / wxglRender.zoom, //move down to under HI icon
                            WXGLNexradLevel3HailIndex.hailList[it].lon,
                            WXGLNexradLevel3HailIndex.hailList[it].hailsize,
                            MyApplication.radarColorHiText
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
        wxglSurfaceView.hailLabelAl.indices.forEach {
            wxglSurfaceView.hailLabelAl[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.hailLabelAl[it])
        }
    }

    private fun addTVHail() {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            var foundhail: Boolean = false
            var hailLat: Double
            var hailLon: Double
            wxglSurfaceView.hailTv = mutableListOf()
            var aa = 0

            UtilityLog.d("wx", "hailList.size: "+WXGLNexradLevel3HailIndex.hailList.size)

            if (WXGLNexradLevel3HailIndex.hailList.size < 0) {
                UtilityLog.d("wx", "haillist < 0")
                foundhail = true
            }

            while (aa < WXGLNexradLevel3HailIndex.hailList.size) {
                aa += 1
            }

                if (foundhail) {
                    UtilityLog.d("wx", "hail found")
                    hailLat = WXGLNexradLevel3HailIndex.hailList[aa].lat //move down abit
                    hailLon = WXGLNexradLevel3HailIndex.hailList[aa].lon

                    scale = getScale()
                    oglrZoom = 1.0f
                    if (wxglRender.zoom < 1.0f) {
                        oglrZoom = wxglRender.zoom * 0.8f
                    }
                    if (wxglRender.zoom > 0.5) {
                        showHail()
                        for (c in 0 until 1) {
                            val drawText = checkButDoNotDrawText(
                                    wxglSurfaceView.hailTv,
                                    hailLat,
                                    hailLon, //move down abit so can read
                                    MyApplication.radarColorHiText,
                                    MyApplication.textSizeSmall * oglrZoom * 1.5f * MyApplication.radarHiTextSize
                            )
                            if (drawText) {
                                wxglSurfaceView.hailTv[c].text = WXGLNexradLevel3HailIndex.hailList[aa].hailsize

                            }
                        }
                    } else {
                        hideHail()
                    }

                } //foundhail

    }

    private fun showHail() {
            if (wxglSurfaceView.hailTv.size > 0) {
                var c = 0
                while (c < 1) {
                    wxglSurfaceView.hailTv[c].visibility = View.VISIBLE
                    c += 1
                }
            }
    }

    private fun hideHail() {
            if (hailSingleLabelTvArrInit)
            if (wxglSurfaceView.hailTv.size > 0) {
                var c = 0
                while (c < wxglSurfaceView.hailTv.size) {
                    wxglSurfaceView.hailTv[c].visibility = View.GONE
                    c += 1
                }
            }
    }


    private fun checkAndDrawText(
            tvList: MutableList<TextView>,
            lat: Double,
            lon: Double,
            text: String,
            color: Int
    ) {
        val tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(lat, lon, projectionNumbers)
        tmpCoords[0] = tmpCoords[0] + wxglRender.x / wxglRender.zoom
        tmpCoords[1] = tmpCoords[1] - wxglRender.y / wxglRender.zoom
        if (abs(tmpCoords[0] * scale) < glviewWidth && abs(tmpCoords[1] * scale) < glviewHeight) {
            tvList.add(TextView(context))
            ii = tvList.lastIndex
            tvList[ii].setTextColor(color)
            tvList[ii].setShadowLayer(1.5f, 2.0f, 2.0f, R.color.black)
            relativeLayout.addView(tvList[ii])
            tvList[ii].setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            if ((tmpCoords[1] * scale).toInt() < 0) {
                if ((tmpCoords[0] * scale).toInt() < 0)
                    tvList[ii].setPadding(
                            0,
                            0,
                            (-(tmpCoords[0] * scale)).toInt(),
                            (-(tmpCoords[1] * scale)).toInt()
                    )
                else
                    tvList[ii].setPadding(
                            (tmpCoords[0] * scale).toInt(),
                            0,
                            0,
                            (-(tmpCoords[1] * scale)).toInt()
                    )
            } else {
                if ((tmpCoords[0] * scale).toInt() < 0)
                    tvList[ii].setPadding(
                            0,
                            (tmpCoords[1] * scale).toInt(),
                            (-(tmpCoords[0] * scale)).toInt(),
                            0
                    )
                else
                    tvList[ii].setPadding(
                            (tmpCoords[0] * scale).toInt(),
                            (tmpCoords[1] * scale).toInt(),
                            0,
                            0
                    )
            }
            layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
            tvList[ii].layoutParams = layoutParams
            tvList[ii].text = text
        }
    }

    private fun checkButDoNotDrawText(
            tvList: MutableList<TextView>,
            lat: Double,
            lon: Double,
            color: Int,
            textSizeTv: Float
    ): Boolean {
        val tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(lat, lon, projectionNumbers)
        tmpCoords[0] = tmpCoords[0] + wxglRender.x / wxglRender.zoom
        tmpCoords[1] = tmpCoords[1] - wxglRender.y / wxglRender.zoom
        var drawText = false
        if (abs(tmpCoords[0] * scale) < glviewWidth && abs(tmpCoords[1] * scale) < glviewHeight) {
            drawText = true
            tvList.add(TextView(context))
            ii = tvList.lastIndex
            tvList[ii].setTextColor(color)
            tvList[ii].setShadowLayer(1.5f, 2.0f, 2.0f, R.color.black)
            relativeLayout.addView(tvList[ii])
            tvList[ii].setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeTv)
            if ((tmpCoords[1] * scale).toInt() < 0) {
                if ((tmpCoords[0] * scale).toInt() < 0)
                    tvList[ii].setPadding(
                            0,
                            0,
                            (-(tmpCoords[0] * scale)).toInt(),
                            (-(tmpCoords[1] * scale)).toInt()
                    )
                else
                    tvList[ii].setPadding(
                            (tmpCoords[0] * scale).toInt(),
                            0,
                            0,
                            (-(tmpCoords[1] * scale)).toInt()
                    )
            } else {
                if ((tmpCoords[0] * scale).toInt() < 0)
                    tvList[ii].setPadding(
                            0,
                            (tmpCoords[1] * scale).toInt(),
                            (-(tmpCoords[0] * scale)).toInt(),
                            0
                    )
                else
                    tvList[ii].setPadding(
                            (tmpCoords[0] * scale).toInt(),
                            (tmpCoords[1] * scale).toInt(),
                            0,
                            0
                    )
            }
            layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
            tvList[ii].layoutParams = layoutParams
        }
        return drawText
    }

    private fun hideSpottersLabels() {
        wxglSurfaceView.spotterLabels.indices.forEach {
            wxglSurfaceView.spotterLabels[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.spotterLabels[it])
        }
    }

    private fun addTextLabelForSpotter() {
        if (WXGLRadarActivity.spotterShowSelected) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            val spotterLat: Double
            val spotterLon: Double
            var report = false
            hideSpotter()
            wxglSurfaceView.spotterTv = mutableListOf()
            // FIXME var rename
            var aa = 0
            while (aa < UtilitySpotter.spotterList.size) {
                if (UtilitySpotter.spotterList[aa].unique == WXGLRadarActivity.spotterId) {
                    break
                }
                aa += 1
            }
            var bb = 0
            while (bb < UtilitySpotter.spotterReports.size) {
                if (UtilitySpotter.spotterReports[bb].uniq == WXGLRadarActivity.spotterId) {
                    report = true
                    break
                }
                bb += 1
            }
            if (!report) {
                spotterLat = UtilitySpotter.spotterList[aa].lat.toDoubleOrNull() ?: 0.0
                spotterLon = UtilitySpotter.spotterList[aa].lon.toDoubleOrNull() ?: 0.0
            } else {
                spotterLat = UtilitySpotter.spotterReports[bb].lat.toDoubleOrNull() ?: 0.0
                spotterLon = UtilitySpotter.spotterReports[bb].lon.toDoubleOrNull() ?: 0.0
            }
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.0f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            if (wxglRender.zoom > 0.5) {
                for (c in 0 until 1) {
                   val drawText = checkButDoNotDrawText(
                            wxglSurfaceView.spotterTv,
                            spotterLat,
                            spotterLon * -1,
                            MyApplication.radarColorSpotter,
                            MyApplication.textSizeSmall * oglrZoom * 1.5f * MyApplication.radarTextSize
                    )
                    if (drawText) {
                        if (!report) {
                            wxglSurfaceView.spotterTv[c].text =
                                    UtilitySpotter.spotterList[aa].lastName.replace("0FAV ", "")
                        } else {
                            wxglSurfaceView.spotterTv[c].text = UtilitySpotter.spotterReports[bb].type
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
            wxglSurfaceView.spotterTv.indices.forEach {
                UtilityLog.d("Wx", "hide spotter")
                wxglSurfaceView.spotterTv[it].visibility = View.GONE
                relativeLayout.removeView(wxglSurfaceView.spotterTv[it])
            }
        }
    }

    fun initializeTextLabels(context: Context) {
        initializeTextLabelsCitiesExtended(context)
        initializeTextLabelsCountyLabels(context)
        ///TODO TEST THIS!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //initTVSpottersLabels()
        initTVHailLabels()
    }

    fun addTextLabels() {
        addTextLabelsCitiesExtended()
        addTextLabelsCountyLabels()
        addTextLabelsObservations()
        addTextLabelsSpottersLabels()
        if (numberOfPanes == 1 && WXGLRadarActivity.spotterShowSelected) {
            addTextLabelForSpotter()
        }
        addTVHailLabels()
	addWpcPressureCenters()
    }

    fun hideTextLabels() {
        hideCitiesExtended()
        hideCountyLabels()
        hideObservations()
        hideSpottersLabels()
        hideHailLabels()
        if (numberOfPanes == 1 && WXGLRadarActivity.spotterShowSelected) {
            hideSpotter()
        }
        hideWpcPressureCenters()
    }

    fun initializeTextLabelsObservations() {
        if (PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) {
            obsTvArrInit = true
        }
    }

    fun addWpcPressureCenters() {
        if (MyApplication.radarShowWpcFronts) {
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            hideWpcPressureCenters()
            wxglSurfaceView.pressureCenterLabelAl = mutableListOf()
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.00f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            textSize = MyApplication.textSizeNormal * MyApplication.radarTextSize
            if (wxglRender.zoom < (0.5 / wxglRender.zoomScreenScaleFactor)) {
                UtilityWpcFronts.pressureCenters.indices.forEach {
                    var color = Color.rgb(0,127,225)
                    if (UtilityWpcFronts.pressureCenters[it].type == PressureCenterTypeEnum.LOW) {
                        color = Color.RED
                    }
                    checkAndDrawText(
                            wxglSurfaceView.pressureCenterLabelAl,
                            UtilityWpcFronts.pressureCenters[it].lat,
                            UtilityWpcFronts.pressureCenters[it].lon,
                            UtilityWpcFronts.pressureCenters[it].pressureInMb,
                            color
                    )
                }

                ///Fix to hide the wpc pressure center texts from conus radar.
                if (MyApplication.radarConusRadar) {
                    if (wxglRender.zoom < (MyApplication.radarConusRadarZoom / 1000.0).toFloat()) {
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

    fun addTextLabelsObservations() {
        if ((PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) && obsTvArrInit) {
            val obsExtZoom = MyApplication.radarObsExtZoom.toDouble()
            projectionNumbers = ProjectionNumbers(wxglRender.rid, ProjectionType.WX_OGL)
            obsLat = 0.0
            obsLon = 0.0
            val fontScaleFactorObs = 0.65f
            hideObservations()
            wxglSurfaceView.obsAl = mutableListOf()
            var tmpArrObs: Array<String>
            var tmpArrObsExt: Array<String>
            scale = getScale()
            oglrZoom = 1.0f
            if (wxglRender.zoom < 1.0f) {
                oglrZoom = wxglRender.zoom * 0.8f
            }
            textSize = MyApplication.textSizeSmall * oglrZoom * fontScaleFactorObs * MyApplication.radarTextSize
            val obsArr = UtilityMetar.metarDataList[paneNumber].obsArr.toList()
            val obsArrExt = UtilityMetar.metarDataList[paneNumber].obsArrExt.toList()
            if (wxglRender.zoom > 0.5) {
                obsArr.indices.forEach {
                    if (it < obsArr.size && it < obsArrExt.size) {
                        tmpArrObs = MyApplication.colon.split(obsArr[it])
                        tmpArrObsExt = MyApplication.colon.split(obsArrExt[it])
                        if (tmpArrObs.size > 1) {
                            obsLat = tmpArrObs[0].toDoubleOrNull() ?: 0.0
                            obsLon = tmpArrObs[1].toDoubleOrNull() ?: 0.0
                        }
                        val drawText = checkButDoNotDrawText(
                                wxglSurfaceView.obsAl,
                            obsLat,
                            obsLon * -1,
                                MyApplication.radarColorObs,
                                textSize
                        )
                        if (drawText) {
                            if (wxglRender.zoom > obsExtZoom) {
                                wxglSurfaceView.obsAl.last().text = tmpArrObsExt[2]
                            } else if (PolygonType.OBS.pref) {
                                wxglSurfaceView.obsAl.last().text = tmpArrObs[2]
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
        wxglSurfaceView.obsAl.indices.forEach {
            wxglSurfaceView.obsAl[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.obsAl[it])
        }
    }

    private fun hideWpcPressureCenters() {
        wxglSurfaceView.pressureCenterLabelAl.indices.forEach {
            wxglSurfaceView.pressureCenterLabelAl[it].visibility = View.GONE
            relativeLayout.removeView(wxglSurfaceView.pressureCenterLabelAl[it])
        }
    }

    fun setWXGLRender(wxglRender: WXGLRender) {
        this.wxglRender = wxglRender
    }
}

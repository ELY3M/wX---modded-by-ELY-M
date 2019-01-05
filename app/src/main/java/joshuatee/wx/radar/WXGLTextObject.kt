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
//modded by ELY M.

package joshuatee.wx.radar

import android.content.Context
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
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityCanvasProjection
import joshuatee.wx.util.UtilityLog

import kotlin.math.*

class WXGLTextObject(
    private val context: Context,
    private val rl: RelativeLayout,
    private val glview: WXGLSurfaceView,
    private var OGLR: WXGLRender,
    private val numPanes: Int
) {

    private var lp: RelativeLayout.LayoutParams
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
    private var ii = 0
    private val glviewWidth: Int
    private val glviewHeight: Int
    private var scale = 0.toFloat()
    private var oglrZoom = 0.toFloat()
    private var textSize = 0.toFloat()
    private var pn: ProjectionNumbers

    init {
        this.maxCitiesPerGlview = maxCitiesPerGlview / numPanes
        // locfrag should show fewer then full screen wxogl
        if (numPanes == 1 && !glview.fullScreen)
            this.maxCitiesPerGlview = (maxCitiesPerGlview * 0.60).toInt()
        if (numPanes != 1)
            this.glviewWidth = MyApplication.dm.widthPixels / (numPanes / 2)
        else
            this.glviewWidth = MyApplication.dm.widthPixels
        if (numPanes != 1)
            this.glviewHeight = MyApplication.dm.heightPixels / 2
        else
            this.glviewHeight = MyApplication.dm.heightPixels
        pn = ProjectionNumbers(context, OGLR.rid, ProjectionType.WX_OGL)
        lp = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL)
        lp.addRule(RelativeLayout.CENTER_VERTICAL)
    }

    private fun addTVCitiesExt() {
        if (GeographyType.CITIES.pref && cityextTvArrInit) {
            pn = ProjectionNumbers(context, OGLR.rid, ProjectionType.WX_OGL)
            hideCitiesExt()
            glview.citiesExtAl = mutableListOf()
            scale = getScale()
            oglrZoom = 1.0f
            if (OGLR.zoom < 1.00f) {
                oglrZoom = OGLR.zoom * 0.8f
            }
            textSize = MyApplication.textSizeSmall * oglrZoom * 0.75f * MyApplication.radarTextSize
            val cityMinZoom = 0.50
            if (OGLR.zoom > cityMinZoom) {
                val cityExtLength = UtilityCitiesExtended.cityAl.size
                for (c in 0 until cityExtLength) {
                    if (glview.citiesExtAl.size > maxCitiesPerGlview) {
                        break
                    }
                    checkAndDrawText(
                        glview.citiesExtAl,
                        UtilityCitiesExtended.cityAl[c].latD,
                        UtilityCitiesExtended.cityAl[c].lonD,
                        UtilityCitiesExtended.cityAl[c].name,
                        MyApplication.radarColorCity
                    )
                }
            } else {
                hideCitiesExt()
            }
        } else {
            hideCitiesExt()
        }
    }

    private fun hideCitiesExt() {
        glview.citiesExtAl.indices.forEach {
            glview.citiesExtAl[it].visibility = View.GONE
            rl.removeView(glview.citiesExtAl[it])
        }
    }

    private fun initTVCitiesExt(context: Context) {
        if (GeographyType.CITIES.pref) {
            cityextTvArrInit = true
            UtilityCitiesExtended.populateArrays(context)
        }
    }

    private fun initTVCountyLabels(context: Context) {
        if (MyApplication.radarCountyLabels) {
            UtilityCountyLabels.populateArrays(context)
            countyLabelsTvArrInit = true
        }
    }

    private fun getScale() =
        8.1f * OGLR.zoom / MyApplication.deviceScale * (glviewWidth / 800.0f * MyApplication.deviceScale) / MyApplication.TEXTVIEW_MAGIC_FUDGE_FACTOR

    private fun addTVCountyLabels() {
        if (MyApplication.radarCountyLabels && countyLabelsTvArrInit) {
            pn = ProjectionNumbers(context, OGLR.rid, ProjectionType.WX_OGL)
            hideCountyLabels()
            glview.countyLabelsAl = mutableListOf()
            scale = getScale()
            oglrZoom = 1.0f
            if (OGLR.zoom < 1.00f) {
                oglrZoom = OGLR.zoom * 0.8f
            }
            textSize = MyApplication.textSizeSmall * oglrZoom * 0.65f * MyApplication.radarTextSize
            if (OGLR.zoom > 1.50) {
                UtilityCountyLabels.countyName.indices.forEach {
                    checkAndDrawText(
                        glview.countyLabelsAl,
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
        glview.countyLabelsAl.indices.forEach {
            glview.countyLabelsAl[it].visibility = View.GONE
            rl.removeView(glview.countyLabelsAl[it])
        }
    }

    fun initTVSpottersLabels() {
        if (PolygonType.SPOTTER_LABELS.pref) spottersLabelsTvArrInit = true
    }

    fun addTVSpottersLabels() {
        if (PolygonType.SPOTTER_LABELS.pref && spottersLabelsTvArrInit) {
            pn = ProjectionNumbers(context, OGLR.rid, ProjectionType.WX_OGL)
            spotterLat = 0.0
            spotterLon = 0.0
            hideSpottersLabels()
            glview.spottersLabelAl = mutableListOf()
            scale = getScale()
            oglrZoom = 1.0f
            if (OGLR.zoom < 1.00f) {
                oglrZoom = OGLR.zoom * 0.8f
            }
            textSize = MyApplication.textSizeSmall * oglrZoom * MyApplication.radarTextSize * 0.75f
            if (OGLR.zoom > 0.5) {
                // FIXME make copy first
                UtilitySpotter.spotterList.indices.forEach {
                    checkAndDrawText(
                        glview.spottersLabelAl,
                        UtilitySpotter.spotterList[it].latD,
                        UtilitySpotter.spotterList[it].lonD,
                        UtilitySpotter.spotterList[it].lastName.replace("0FAV ", ""),
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
            pn = ProjectionNumbers(context, OGLR.rid, ProjectionType.WX_OGL)
            hailLat = 0.0
            hailLon = 0.0
            hideHailLabels()
            glview.hailLabelAl = mutableListOf()
            scale = getScale()
            oglrZoom = 1.0f
            if (OGLR.zoom < 1.00f) {
                oglrZoom = OGLR.zoom * 0.8f
            }
            textSize = MyApplication.textSizeSmall * oglrZoom * MyApplication.radarHiTextSize * 0.75f
            if (OGLR.zoom > 0.5) {
                // FIXME make copy first
                WXGLNexradLevel3HailIndex.hailList.indices.forEach {
                    checkAndDrawText(
                            glview.hailLabelAl,
                            WXGLNexradLevel3HailIndex.hailList[it].lat - 0.130 / OGLR.zoom, //move down to under HI icon
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
        glview.hailLabelAl.indices.forEach {
            glview.hailLabelAl[it].visibility = View.GONE
            rl.removeView(glview.hailLabelAl[it])
        }
    }

    private fun addTVHail() {
            pn = ProjectionNumbers(context, OGLR.rid, ProjectionType.WX_OGL)
            var foundhail: Boolean = false
            var hailLat: Double
            var hailLon: Double
            glview.hailTv = mutableListOf()
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
                    if (OGLR.zoom < 1.0f) {
                        oglrZoom = OGLR.zoom * 0.8f
                    }
                    if (OGLR.zoom > 0.5) {
                        showHail()
                        for (c in 0 until 1) {
                            val drawText = checkButDoNotDrawText(
                                    glview.hailTv,
                                    hailLat,
                                    hailLon, //move down abit so can read
                                    MyApplication.radarColorHiText,
                                    MyApplication.textSizeSmall * oglrZoom * 1.5f * MyApplication.radarHiTextSize
                            )
                            if (drawText) {
                                glview.hailTv[c].text = WXGLNexradLevel3HailIndex.hailList[aa].hailsize

                            }
                        }
                    } else {
                        hideHail()
                    }

                } //foundhail

    }

    private fun showHail() {
            if (glview.hailTv.size > 0) {
                var c = 0
                while (c < 1) {
                    glview.hailTv[c].visibility = View.VISIBLE
                    c += 1
                }
            }
    }

    private fun hideHail() {
            if (hailSingleLabelTvArrInit)
            if (glview.hailTv.size > 0) {
                var c = 0
                while (c < glview.hailTv.size) {
                    glview.hailTv[c].visibility = View.GONE
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
        val tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(lat, lon, pn)
        tmpCoords[0] = tmpCoords[0] + OGLR.x / OGLR.zoom
        tmpCoords[1] = tmpCoords[1] - OGLR.y / OGLR.zoom
        if (abs(tmpCoords[0] * scale) < glviewWidth && abs(tmpCoords[1] * scale) < glviewHeight) {
            tvList.add(TextView(context))
            ii = tvList.size - 1
            tvList[ii].setTextColor(color)
            tvList[ii].setShadowLayer(1.5f, 2.0f, 2.0f, R.color.black)
            rl.addView(tvList[ii])
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
            lp = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL)
            lp.addRule(RelativeLayout.CENTER_VERTICAL)
            tvList[ii].layoutParams = lp
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
        val tmpCoords = UtilityCanvasProjection.computeMercatorNumbers(lat, lon, pn)
        tmpCoords[0] = tmpCoords[0] + OGLR.x / OGLR.zoom
        tmpCoords[1] = tmpCoords[1] - OGLR.y / OGLR.zoom
        var drawText = false
        if (abs(tmpCoords[0] * scale) < glviewWidth && abs(tmpCoords[1] * scale) < glviewHeight) {
            drawText = true
            tvList.add(TextView(context))
            ii = tvList.size - 1
            tvList[ii].setTextColor(color)
            tvList[ii].setShadowLayer(1.5f, 2.0f, 2.0f, R.color.black)
            rl.addView(tvList[ii])
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
            lp = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL)
            lp.addRule(RelativeLayout.CENTER_VERTICAL)
            tvList[ii].layoutParams = lp
        }
        return drawText
    }

    private fun hideSpottersLabels() {
        glview.spottersLabelAl.indices.forEach {
            glview.spottersLabelAl[it].visibility = View.GONE
            rl.removeView(glview.spottersLabelAl[it])
        }
    }

    private fun addTVSpotter() {
        if (WXGLRadarActivity.spotterShowSelected) {
            pn = ProjectionNumbers(context, OGLR.rid, ProjectionType.WX_OGL)
            val spotterLat: Double
            val spotterLon: Double
            var report = false
            glview.spotterTv = mutableListOf()
            var aa = 0
            while (aa < UtilitySpotter.spotterList.size) {
                if (UtilitySpotter.spotterList[aa].uniq == WXGLRadarActivity.spotterId) break
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
            if (OGLR.zoom < 1.0f) {
                oglrZoom = OGLR.zoom * 0.8f
            }
            if (OGLR.zoom > 0.5) {
                showSpotter()
                for (c in 0 until 1) {
                    val drawText = checkButDoNotDrawText(
                        glview.spotterTv,
                        spotterLat,
                        spotterLon * -1,
                        MyApplication.radarColorSpotter,
                        MyApplication.textSizeSmall * oglrZoom * 1.5f * MyApplication.radarTextSize
                    )
                    if (drawText) {
                        if (!report) {
                            glview.spotterTv[c].text =
                                    UtilitySpotter.spotterList[aa].lastName.replace("0FAV ", "")
                        } else {
                            glview.spotterTv[c].text = UtilitySpotter.spotterReports[bb].type
                        }
                    }
                }
            } else {
                hideSpotter()
            }
        }
    }

    private fun showSpotter() {
        if (WXGLRadarActivity.spotterShowSelected) {
            if (glview.spotterTv.size > 0) {
                var c = 0
                while (c < 1) {
                    glview.spotterTv[c].visibility = View.VISIBLE
                    c += 1
                }
            }
        }
    }

    private fun hideSpotter() {
        if (WXGLRadarActivity.spotterShowSelected || spotterSingleLabelTvArrInit)
            if (glview.spotterTv.size > 0) {
                var c = 0
                while (c < glview.spotterTv.size) {
                    glview.spotterTv[c].visibility = View.GONE
                    c += 1
                }
            }
    }

    fun initTV(context: Context) {
        initTVCitiesExt(context)
        initTVCountyLabels(context)
        initTVSpottersLabels()
        initTVHailLabels()
    }

    fun addTV() {
        addTVCitiesExt()
        addTVCountyLabels()
        if (numPanes == 1) {
        addTVObs()
        }
        addTVSpottersLabels()
        if (numPanes == 1 && WXGLRadarActivity.spotterShowSelected) {
            addTVSpotter()
        }
        addTVHailLabels()
        if (numPanes == 1) {
            addTVHail()
        }
    }

    fun hideTV() {
        hideCitiesExt()
        hideCountyLabels()
        if (numPanes == 1) {
        hideObs()
        }
        hideSpottersLabels()
        hideHailLabels()
        if (numPanes == 1 && WXGLRadarActivity.spotterShowSelected) {
            hideSpotter()
        }
        if (numPanes == 1) {
            hideHail()
        }
    }

    fun initTVObs() {
        if (PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) {
            obsTvArrInit = true
        }
    }

    fun addTVObs() {
        if ((PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) && obsTvArrInit) {
            val obsExtZoom = MyApplication.radarObsExtZoom.toDouble()
            pn = ProjectionNumbers(context, OGLR.rid, ProjectionType.WX_OGL)
            obsLat = 0.0
            obsLon = 0.0
            val fontScaleFactorObs = 0.65f
            hideObs()
            glview.obsAl = mutableListOf()
            var tmpArrObs: Array<String>
            var tmpArrObsExt: Array<String>
            scale = getScale()
            oglrZoom = 1.0f
            if (OGLR.zoom < 1.0f) {
                oglrZoom = OGLR.zoom * 0.8f
            }
            textSize = MyApplication.textSizeSmall * oglrZoom * fontScaleFactorObs *
                    MyApplication.radarTextSize
            val obsArr = UtilityMetar.obsArr.toList()
            val obsArrExt = UtilityMetar.obsArrExt.toList()
            if (OGLR.zoom > 0.5) {
                obsArr.indices.forEach {
                    if (it < obsArr.size && it < obsArrExt.size) {
                        tmpArrObs = MyApplication.colon.split(obsArr[it])
                        tmpArrObsExt = MyApplication.colon.split(obsArrExt[it])
                        if (tmpArrObs.size > 1) {
                            obsLat = tmpArrObs[0].toDoubleOrNull() ?: 0.0
                            obsLon = tmpArrObs[1].toDoubleOrNull() ?: 0.0
                        }
                        val drawText = checkButDoNotDrawText(
                            glview.obsAl,
                            obsLat,
                            obsLon * -1,
                            MyApplication.radarColorObs,
                            textSize
                        )
                        if (drawText) {
                            if (OGLR.zoom > obsExtZoom) {
                                glview.obsAl[glview.obsAl.size - 1].text = tmpArrObsExt[2]
                            } else if (PolygonType.OBS.pref) {
                                glview.obsAl[glview.obsAl.size - 1].text = tmpArrObs[2]
                            }
                        }
                    }
                }
            } else {
                hideObs()
            }
        } else {
            hideObs()
        }
    }

    private fun hideObs() {
            glview.obsAl.indices.forEach {
                glview.obsAl[it].visibility = View.GONE
                rl.removeView(glview.obsAl[it])
            }
        }

    fun setOGLR(OGLR: WXGLRender) {
        this.OGLR = OGLR
    }
}

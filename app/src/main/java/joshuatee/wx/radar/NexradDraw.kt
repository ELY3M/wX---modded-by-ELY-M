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

import android.opengl.GLSurfaceView
import android.view.View
import joshuatee.wx.objects.LatLon
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.objects.PolygonWarning
import joshuatee.wx.objects.PolygonWarningType
import joshuatee.wx.ui.ObjectImageMap
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.UtilityLog

internal object NexradDraw {

    fun initGlviewMainScreen(
            index: Int,
            nexradState: NexradStateMainScreen,
            changeListener: NexradRenderSurfaceView.OnProgressChangeListener
    ): Boolean {
        with (nexradState) {
            wxglSurfaceViews[index].setEGLContextClientVersion(2)
            wxglRenders[index].state.indexString = index.toString()
            wxglSurfaceViews[index].setRenderer(nexradState.wxglRenders[index])
            wxglSurfaceViews[index].setRenderVar(nexradState.wxglRenders[index], nexradState.wxglRenders, nexradState.wxglSurfaceViews)
            wxglSurfaceViews[index].renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            wxglSurfaceViews[index].setOnProgressChangeListener(changeListener)
            wxglRenders[index].state.zoom = RadarPreferences.wxoglSize / 10.0f
            wxglSurfaceViews[index].scaleFactor = RadarPreferences.wxoglSize / 10.0f
        }
        return true
    }

    fun initGlView(
            index: Int,
            nexradState: NexradState,
            activity: VideoRecordActivity,
            changeListener: NexradRenderSurfaceView.OnProgressChangeListener,
            archived: Boolean = false
    ) {
        with (nexradState.wxglSurfaceViews[index]) {
            setEGLContextClientVersion(2)
            setRenderer(nexradState.wxglRenders[index])
            setRenderVar(nexradState.wxglRenders[index], nexradState.wxglRenders, nexradState.wxglSurfaceViews, activity)
            fullScreen = true
            setOnProgressChangeListener(changeListener)
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
            toolbar = activity.toolbar
            toolbarBottom = activity.toolbarBottom
            archiveMode = archived
        }
    }

    fun initGeom(
            index: Int,
            oldRadarSites: Array<String>,
            wxglRenders: List<NexradRender>,
            wxglTextObjects: List<NexradRenderTextObject>,
            imageMap: ObjectImageMap?,
            wxglSurfaceViews: List<NexradRenderSurfaceView>,
            fnGps: () -> Unit,
            fnGetLatLon: () -> LatLon,
            archived: Boolean,
            forceReset: Boolean
    ) {
        wxglRenders[index].initializeGeometry()
        if (forceReset || (oldRadarSites[index] != wxglRenders[index].state.rid)) {
            wxglRenders[index].setChunkCount(0)
            wxglRenders[index].construct.setChunkCountSti(0)
            //elys mod
            wxglRenders[index].construct.setHiInit(false)
            //elys mod	    
            wxglRenders[index].construct.setTvsInit(false)
            wxglRenders[index].construct.setUserPointsInit(false)
            RadarGeometry.orderedTypes.forEach {
                Thread {
                    if (RadarGeometry.dataByType[it]!!.isEnabled) {
                        wxglRenders[index].construct.geographic(wxglRenders[index].data.geographicBuffers[it]!!, forceReset)
                        try {
                            wxglSurfaceViews[index].requestRender()
                        } catch (e: Exception) {
                            UtilityLog.handleException(e)
                        }
                    }
                }.start()
            }
            wxglTextObjects[index].addLabels()
            oldRadarSites[index] = wxglRenders[index].state.rid
        }

	    //elys mod - conus radar
        Thread {
            if (RadarPreferences.conusRadar) {
                try {
                    wxglRenders[index].construct.ConusRadar()
                    wxglSurfaceViews[index].requestRender()
                } catch (e: Exception) {
                    UtilityLog.handleException(e)
                }
            }
	        //else {
            //    wxglRenders[index].deconstructConusRadar()
            //  }
        }.start()

        Thread {
            PolygonWarning.byType.values.forEach {
                if (it.isEnabled) {
                    wxglRenders[index].construct.warningLines(it.type)
                }
            }
            if (PolygonType.MCD.pref) {
                wxglRenders[index].construct.lines(wxglRenders[index].data.polygonBuffers[PolygonType.WATCH]!!)
                wxglRenders[index].construct.lines(wxglRenders[index].data.polygonBuffers[PolygonType.WATCH_TORNADO]!!)
                wxglRenders[index].construct.lines(wxglRenders[index].data.polygonBuffers[PolygonType.MCD]!!)
            }
            if (PolygonType.MPD.pref) {
                wxglRenders[index].construct.lines(wxglRenders[index].data.polygonBuffers[PolygonType.MPD]!!)
            }
            try {
                wxglSurfaceViews[index].requestRender()
            } catch (e: Exception) {
                UtilityLog.handleException(e)
            }
        }.start()
        if (RadarPreferences.locationDotFollowsGps) {
            fnGps()
        }
        if (PolygonType.LOCDOT.pref || RadarPreferences.locationDotFollowsGps) {
            val latLon = fnGetLatLon()
            wxglRenders[index].construct.locationDot(latLon.lat, latLon.lon, archived)
        }
        if ((imageMap != null) && (imageMap.visibility != View.VISIBLE)) {
            wxglSurfaceViews.forEach {
                it.visibility = View.VISIBLE
            }
        }
    }

    fun plotWarningPolygon(polygonWarningType: PolygonWarningType, wxglSurfaceView: NexradRenderSurfaceView, wxglRender: NexradRender) {
        Thread {
            wxglRender.construct.warningLines(polygonWarningType)
            wxglSurfaceView.requestRender()
        }.start()
    }

    fun plotPolygons(polygonType: PolygonType, wxglSurfaceView: NexradRenderSurfaceView, wxglRender: NexradRender) {
        Thread {
            if (polygonType.pref) {
                wxglRender.construct.lines(wxglRender.data.polygonBuffers[polygonType]!!)
            }
            //
            // PolygonType.WATCH and PolygonType.WATCH_TORNADO need to be handled together
            //
            if (polygonType == PolygonType.WATCH && polygonType.pref) {
                wxglRender.construct.lines(wxglRender.data.polygonBuffers[PolygonType.WATCH_TORNADO]!!)
            }
            wxglSurfaceView.requestRender()
        }.start()
    }

    fun plotWpcFronts(wxglSurfaceView: NexradRenderSurfaceView, wxglRender: NexradRender) {
        Thread {
            if (PolygonType.WPC_FRONTS.pref) {
                try {
                    wxglRender.construct.wpcFronts()
                    wxglSurfaceView.requestRender()
                } catch (e: Exception) {
                    UtilityLog.handleException(e)
                }
            }
        }.start()
    }

    fun plotRadar(wxglRender: NexradRender, fnGps: () -> Unit, fnGetLatLon: () -> LatLon, archived: Boolean, url: String = "") {
        wxglRender.constructPolygons("", true, url)
        // work-around for a bug in which raster doesn't show on first launch
        if (wxglRender.state.product == "NCR" || wxglRender.state.product == "NCZ") {
            wxglRender.constructPolygons("", true, url)
        }
        if (RadarPreferences.locationDotFollowsGps) {
            fnGps()
        }
        if (PolygonType.LOCDOT.pref || RadarPreferences.locationDotFollowsGps) {
            val latLon = fnGetLatLon()
            wxglRender.construct.locationDot(latLon.lat, latLon.lon, archived)
        }
    }

    fun resetGlview(wxglSurfaceView: NexradRenderSurfaceView, wxglRender: NexradRender) {
        wxglSurfaceView.scaleFactor = RadarPreferences.wxoglSize / 10.0f
        wxglRender.setViewInitial(RadarPreferences.wxoglSize / 10.0f, 0.0f, 0.0f)
        wxglSurfaceView.requestRender()
    }
}

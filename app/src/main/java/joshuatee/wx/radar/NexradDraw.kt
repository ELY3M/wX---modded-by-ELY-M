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
//Modded by ELY M. 

package joshuatee.wx.radar

import android.opengl.GLSurfaceView
import android.view.View
import joshuatee.wx.objects.*
import joshuatee.wx.ui.ObjectImageMap
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.UtilityLog

internal object NexradDraw {

    fun initGlviewMainScreen(
            index: Int,
            nexradState: NexradStateMainScreen,
            changeListener: WXGLSurfaceView.OnProgressChangeListener
    ): Boolean {
        nexradState.wxglSurfaceViews[index].setEGLContextClientVersion(2)
        nexradState.wxglTextObjects[index].setWXGLRender(nexradState.wxglRenders[index])
        nexradState.wxglRenders[index].indexString = index.toString()
        nexradState.wxglSurfaceViews[index].setRenderer(nexradState.wxglRenders[index])
        nexradState.wxglSurfaceViews[index].setRenderVar(nexradState.wxglRenders[index], nexradState.wxglRenders, nexradState.wxglSurfaceViews)
        nexradState.wxglSurfaceViews[index].renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        nexradState.wxglSurfaceViews[index].setOnProgressChangeListener(changeListener)
        nexradState.wxglRenders[index].zoom = RadarPreferences.wxoglSize / 10.0f
        nexradState.wxglSurfaceViews[index].scaleFactor = RadarPreferences.wxoglSize / 10.0f
        return true
    }

    fun initGlView(
            index: Int,
            nexradState: NexradState,
            activity: VideoRecordActivity,
            changeListener: WXGLSurfaceView.OnProgressChangeListener,
            archived: Boolean = false
    ) {
        nexradState.wxglSurfaceViews[index].setEGLContextClientVersion(2)
        nexradState.wxglSurfaceViews[index].setRenderer(nexradState.wxglRenders[index])
        nexradState.wxglSurfaceViews[index].setRenderVar(nexradState.wxglRenders[index], nexradState.wxglRenders, nexradState.wxglSurfaceViews, activity)
        nexradState.wxglSurfaceViews[index].fullScreen = true
        nexradState.wxglSurfaceViews[index].setOnProgressChangeListener(changeListener)
        nexradState.wxglSurfaceViews[index].renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        nexradState.wxglSurfaceViews[index].toolbar = activity.toolbar
        nexradState.wxglSurfaceViews[index].toolbarBottom = activity.toolbarBottom
        nexradState.wxglSurfaceViews[index].archiveMode = archived
    }

    fun initGeom(
            index: Int,
            oldRadarSites: Array<String>,
            wxglRenders: List<WXGLRender>,
            wxglTextObjects: List<WXGLTextObject>,
            imageMap: ObjectImageMap?,
            wxglSurfaceViews: List<WXGLSurfaceView>,
            fnGps: () -> Unit,
            fnGetLatLon: () -> LatLon,
            archived: Boolean,
            forceReset: Boolean
    ) {
        wxglRenders[index].initializeGeometry()
        if (forceReset || (oldRadarSites[index] != wxglRenders[index].rid)) {
            wxglRenders[index].setChunkCount(0)
            wxglRenders[index].setChunkCountSti(0)
            //elys mod
            //wxglRenders[index].setHiInit(false) //crashing???
            UtilityLog.d("hail", "before clearHailList()")
            wxglRenders[index].clearHailList()
            wxglRenders[index].setTvsInit(false)
            RadarGeometry.orderedTypes.forEach {
                Thread {
                    if (RadarGeometry.dataByType[it]!!.isEnabled) {
                        wxglRenders[index].constructGeographic(wxglRenders[index].geographicBuffers[it]!!, forceReset)
                        try {
                            wxglSurfaceViews[index].requestRender()
                        } catch (e: Exception) {
                            UtilityLog.handleException(e)
                        }
                    }
                }.start()
            }
            wxglTextObjects[index].addLabels()
            oldRadarSites[index] = wxglRenders[index].rid
        }


	    //elys mod
        //conus radar
        Thread {
            if (RadarPreferences.conusRadar) {
                try {
                    wxglRenders[index].constructConusRadar()
                    wxglSurfaceViews[index].requestRender()
                } catch (e: Exception) {
                    UtilityLog.handleException(e)
                }
            }

	        else {
                wxglRenders[index].deconstructConusRadar()
              }

        }.start()


        Thread {
            ObjectPolygonWarning.polygonDataByType.values.forEach {
                if (it.isEnabled) {
                    wxglRenders[index].constructWarningLines(it.type)
                }
            }
            if (PolygonType.MCD.pref) {
                wxglRenders[index].constructLines(wxglRenders[index].polygonBuffers[PolygonType.WATCH]!!)
                wxglRenders[index].constructLines(wxglRenders[index].polygonBuffers[PolygonType.WATCH_TORNADO]!!)
                wxglRenders[index].constructLines(wxglRenders[index].polygonBuffers[PolygonType.MCD]!!)

            }
            if (PolygonType.MPD.pref) {
                wxglRenders[index].constructLines(wxglRenders[index].polygonBuffers[PolygonType.MPD]!!)
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
            wxglRenders[index].constructLocationDot(latLon.lat, latLon.lon, archived)
        } else {
            wxglRenders[index].deconstructLocationDot()
        }
	    //elys mod
        //if (PolygonType.TVS.pref) wxglRender.constructTvs() else wxglRender.deconstructTvs()
        //if (PolygonType.HI.pref) wxglRender.constructHi() else wxglRender.deconstructHi()
        if (PolygonType.USERPOINTS.pref) wxglRenders[index].constructUserPoints() else wxglRenders[index].deconstructUserPoints()
        if ((imageMap != null) && (imageMap.visibility != View.VISIBLE)) {
            wxglSurfaceViews.forEach {
                it.visibility = View.VISIBLE
            }
        }
    }

    fun plotWarningPolygon(polygonWarningType: PolygonWarningType, wxglSurfaceView: WXGLSurfaceView, wxglRender: WXGLRender) {
        Thread {
            wxglRender.constructWarningLines(polygonWarningType)
            wxglSurfaceView.requestRender()
        }.start()
    }

    fun plotPolygons(polygonType: PolygonType, wxglSurfaceView: WXGLSurfaceView, wxglRender: WXGLRender) {
        Thread {
            if (polygonType.pref) {
                wxglRender.constructLines(wxglRender.polygonBuffers[polygonType]!!)
            }
            //
            // PolygonType.WATCH and PolygonType.WATCH_TORNADO need to be handled together
            //
            if (polygonType == PolygonType.WATCH && polygonType.pref) {
                wxglRender.constructLines(wxglRender.polygonBuffers[PolygonType.WATCH_TORNADO]!!)
            }
            wxglSurfaceView.requestRender()
        }.start()
    }

    fun plotWpcFronts(wxglSurfaceView: WXGLSurfaceView, wxglRender: WXGLRender) {
        Thread {
            if (PolygonType.WPC_FRONTS.pref) {
                try {
                    wxglRender.constructWpcFronts()
                    wxglSurfaceView.requestRender()
                } catch (e: Exception) {
                    UtilityLog.handleException(e)
                }
            }
        }.start()
    }

    fun plotRadar(wxglRender: WXGLRender, url: String, fnGps: () -> Unit, fnGetLatLon: () -> LatLon, archived: Boolean) {
        wxglRender.constructPolygons("", url, true)
        // work-around for a bug in which raster doesn't show on first launch
        if (wxglRender.product == "NCR" || wxglRender.product == "NCZ") {
            wxglRender.constructPolygons("", url, true)
        }
        if (RadarPreferences.locationDotFollowsGps) {
            fnGps()
        }
        if (PolygonType.LOCDOT.pref || RadarPreferences.locationDotFollowsGps) {
            val latLon = fnGetLatLon()
            wxglRender.constructLocationDot(latLon.lat, latLon.lon, archived)
        } else {
            wxglRender.deconstructLocationDot()
        }
    }

    // TODO FIXME move to NexradState
    fun resetGlview(wxglSurfaceView: WXGLSurfaceView, wxglRender: WXGLRender) {
        wxglSurfaceView.scaleFactor = RadarPreferences.wxoglSize / 10.0f
        wxglRender.setViewInitial(RadarPreferences.wxoglSize / 10.0f, 0.0f, 0.0f)
        wxglSurfaceView.requestRender()
    }
}

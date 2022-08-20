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

import android.app.Activity
import android.opengl.GLSurfaceView
import android.view.View
import androidx.appcompat.widget.Toolbar
import joshuatee.wx.objects.*
import joshuatee.wx.ui.ObjectImageMap
import joshuatee.wx.settings.RadarPreferences
import joshuatee.wx.util.UtilityLog

internal object NexradDraw {

    fun initGlviewFragment(
            wxglSurfaceView: WXGLSurfaceView,
            index: Int,
            wxglRenders: List<WXGLRender>,
            wxglSurfaceViews: List<WXGLSurfaceView>,
            wxglTextObjects: List<WXGLTextObject>,
            changeListener: WXGLSurfaceView.OnProgressChangeListener
    ): Boolean {
        wxglSurfaceView.setEGLContextClientVersion(2)
        wxglTextObjects[index].setWXGLRender(wxglRenders[index])
        wxglRenders[index].indexString = index.toString()
        wxglSurfaceView.setRenderer(wxglRenders[index])
        wxglSurfaceView.setRenderVar(wxglRenders[index], wxglRenders, wxglSurfaceViews)
        wxglSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        wxglSurfaceView.setOnProgressChangeListener(changeListener)
        wxglRenders[index].zoom = RadarPreferences.wxoglSize / 10.0f
        wxglSurfaceView.scaleFactor = RadarPreferences.wxoglSize / 10.0f
        return true
    }

    fun initGlView(
            wxglSurfaceView: WXGLSurfaceView,
            wxglSurfaceViews: List<WXGLSurfaceView>,
            wxglRender: WXGLRender,
            wxglRenders: List<WXGLRender>,
            activity: Activity,
            toolbar: Toolbar,
            toolbarBottom: Toolbar,
            changeListener: WXGLSurfaceView.OnProgressChangeListener,
            archived: Boolean = false
    ) {
        wxglSurfaceView.setEGLContextClientVersion(2)
        wxglSurfaceView.setRenderer(wxglRender)
        wxglSurfaceView.setRenderVar(wxglRender, wxglRenders, wxglSurfaceViews, activity)
        wxglSurfaceView.fullScreen = true
        wxglSurfaceView.setOnProgressChangeListener(changeListener)
        wxglSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        wxglSurfaceView.toolbar = toolbar
        wxglSurfaceView.toolbarBottom = toolbarBottom
        wxglSurfaceView.archiveMode = archived
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
            wxglRenders[index].setHiInit(false)
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
            wxglRenders[index].constructLocationDot(latLon.latString, latLon.lonString, archived)
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
            wxglRender.constructLocationDot(latLon.latString, latLon.lonString, archived)
        } else {
            wxglRender.deconstructLocationDot()
        }
    }

    fun resetGlview(wxglSurfaceView: WXGLSurfaceView, wxglRender: WXGLRender) {
        wxglSurfaceView.scaleFactor = RadarPreferences.wxoglSize / 10.0f
        wxglRender.setViewInitial(RadarPreferences.wxoglSize / 10.0f, 0.0f, 0.0f)
        wxglSurfaceView.requestRender()
    }
}

/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

    This file is part of wX.4

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
//Hail Labels

package joshuatee.wx.radar

import android.content.Context
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.ObjectPolygonWarning
import joshuatee.wx.objects.ObjectPolygonWatch
import joshuatee.wx.objects.PolygonType

object NexradLayerDownload {

    fun download(
            context: Context,
            numberOfPanes: Int,
            wxglRender: WXGLRender,
            wxglSurfaceView: WXGLSurfaceView,
            wxglTextObjects: List<WXGLTextObject>,
            radarUpdateFn: () -> Unit,
            showWpcFronts: Boolean = true
    ) {
        //
        // Warnings
        //
        ObjectPolygonWarning.polygonDataByType.values.forEach {
            if (it.isEnabled) {
                FutureVoid(context, it::download) {
                    NexradDraw.plotWarningPolygon(it.type, wxglSurfaceView, wxglRender)
                    radarUpdateFn()
                }
            }
        }
        //
        // MPD, MCD / Watch download/update
        //
        listOf(PolygonType.WATCH, PolygonType.MCD, PolygonType.MPD).forEach {
            if (it.pref) {
                FutureVoid(context, { ObjectPolygonWatch.polygonDataByType[it]!!.download(context) }) {
                    NexradDraw.plotPolygons(it, wxglSurfaceView, wxglRender)
                }
            }
        }
        //
        // WPC Fronts download/update
        //
        if (showWpcFronts) {
            if (PolygonType.WPC_FRONTS.pref) {
                FutureVoid(context, { UtilityWpcFronts.get(context) }, {
                    NexradDraw.plotWpcFronts(wxglSurfaceView, wxglRender)
                    UtilityWXGLTextObject.updateWpcFronts(numberOfPanes, wxglTextObjects)
                })
            }
        }
        //
        // SPC Convective Outlook
        //
        if (PolygonType.SWO.pref) {
            FutureVoid(context, { UtilitySwoDayOne.get(context) }) {
                wxglRender.constructSwoLines()
                wxglSurfaceView.requestRender()
            }
        }
        //
        // Wind barbs and observations
        //
        if (PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) {
            FutureVoid(context, { UtilityMetar.getStateMetarArrayForWXOGL(context, wxglRender.rid, wxglRender.paneNumber) }) {
                if (PolygonType.WIND_BARB.pref) {
                    wxglRender.constructWindBarbs()
                }
                if (PolygonType.OBS.pref) {
                    UtilityWXGLTextObject.updateObservations(numberOfPanes, wxglTextObjects)
                }
                wxglSurfaceView.requestRender()
            }
        }
        //
        // TVS
        //
        if (PolygonType.TVS.pref) {
            FutureVoid(context, { wxglRender.constructTvs() }) {
                wxglSurfaceView.requestRender()
            }
        }
        //
        // Hail Index (HI)
        //
        if (PolygonType.HI.pref) {
            FutureVoid(context, { wxglRender.constructHi() }) {
                wxglSurfaceView.requestRender()
                //elys mod - Hail Labels
                if (PolygonType.HAIL_LABELS.pref) {
                    UtilityWXGLTextObject.updateHailLabels(numberOfPanes, wxglTextObjects)
                }
            }
        }

        //
        // FIXME TODO future use, incorporate spotter as well
        //
//        listOf(PolygonType.TVS, PolygonType.HI).forEach {
//            if (it.pref) {
//                FutureVoid(context, { wxglRender.constructTrianglesGeneric(it) }) {
//                    wxglSurfaceView.requestRender()
//                }
//            }
//        }

        //
        // Storm Tracks (STI)
        //
        if (PolygonType.STI.pref) {
            // FutureVoid(context, { wxglRender.constructStiLines() }) {
            FutureVoid(context, { wxglRender.constructLines(wxglRender.stiBuffers) }) {
                wxglSurfaceView.requestRender()
            }
        }
        //
        // Spotters
        //
        if (PolygonType.SPOTTER.pref || PolygonType.SPOTTER_LABELS.pref) {
            FutureVoid(context, { wxglRender.constructSpotters() }) {
                wxglSurfaceView.requestRender()
                if (PolygonType.SPOTTER_LABELS.pref) {
                    UtilityWXGLTextObject.updateSpotterLabels(numberOfPanes, wxglTextObjects)
                }
            }
        }
    }
}

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
//Hail Labels
//User Points

package joshuatee.wx.radar

import android.content.Context
import joshuatee.wx.objects.FutureVoid
import joshuatee.wx.objects.PolygonWarning
import joshuatee.wx.objects.PolygonWatch
import joshuatee.wx.objects.PolygonType

object NexradLayerDownload {

    fun download(
            context: Context,
            wxglRender: NexradRender,
            wxglSurfaceView: NexradRenderSurfaceView,
            wxglTextObjects: List<NexradRenderTextObject>,
            radarUpdateFn: () -> Unit,
            showWpcFronts: Boolean = true
    ) {
        //
        // Warnings
        //
        PolygonWarning.byType.values.forEach {
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
                FutureVoid(context, { PolygonWatch.byType[it]!!.download(context) }) {
                    NexradDraw.plotPolygons(it, wxglSurfaceView, wxglRender)
                }
            }
        }
        //
        // WPC Fronts download/update
        //
        if (showWpcFronts) {
            if (PolygonType.WPC_FRONTS.pref) {
                FutureVoid(context, { WpcFronts.get(context) }, {
                    NexradDraw.plotWpcFronts(wxglSurfaceView, wxglRender)
                    NexradRenderTextObject.updateWpcFronts(wxglTextObjects)
                })
            }
        }
        //
        // SPC Convective Outlook
        //
        if (PolygonType.SWO.pref) {
            FutureVoid(context, { SwoDayOne.get(context) }) {
                wxglRender.construct.swoLines()
                wxglSurfaceView.requestRender()
            }
        }
        //
        // Wind barbs and observations
        //
        if (PolygonType.OBS.pref || PolygonType.WIND_BARB.pref) {
            FutureVoid(context, { Metar.get(context, wxglRender.state.rid, wxglRender.paneNumber) }) {
                if (PolygonType.WIND_BARB.pref) {
                    wxglRender.construct.windBarbs()
                }
                if (PolygonType.OBS.pref) {
                    NexradRenderTextObject.updateObservations(wxglTextObjects)
                }
                wxglSurfaceView.requestRender()
            }
        }
        //
        // TVS
        //
        if (PolygonType.TVS.pref) {
            FutureVoid(context, {
                wxglRender.construct.tvs()
            }) {
                wxglSurfaceView.requestRender()
            }
        }
        //
        // Hail Index (HI)
        //
        if (PolygonType.HI.pref) {
            FutureVoid(context, {
                wxglRender.construct.hailIndex()
            }) {
                wxglSurfaceView.requestRender()
                //elys mod
                if (PolygonType.HAIL_LABELS.pref) {
                    NexradRenderTextObject.updateHailLabels(wxglTextObjects)
                }		
            }
        }
        //elys mod
        // User Points
        //
        if (PolygonType.USERPOINTS.pref) {
            FutureVoid(context, { wxglRender.construct.userPoints() }) {
                wxglSurfaceView.requestRender()
            }
        }
        //
        // Storm Tracks (STI)
        //
        if (PolygonType.STI.pref) {
            FutureVoid(context, { wxglRender.construct.lines(wxglRender.data.stiBuffers) }) {
                wxglSurfaceView.requestRender()
            }
        }
        //
        // Spotters
        //
        if (PolygonType.SPOTTER.pref || PolygonType.SPOTTER_LABELS.pref) {
            FutureVoid(context, {
                wxglRender.construct.spotters()
            }) {
                wxglSurfaceView.requestRender()
                if (PolygonType.SPOTTER_LABELS.pref) {
                    NexradRenderTextObject.updateSpotterLabels(wxglTextObjects)
                }
            }
        }
    }
}

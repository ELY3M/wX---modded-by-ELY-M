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

package joshuatee.wx.objects

enum class ProjectionType constructor(
    val isMercator: Boolean,
    internal val needsBlackPaint: Boolean,
    internal val needsCanvasShift: Boolean,
    val isCanvas: Boolean
) {
    NWS_MOSAIC(false, false, false, true),
    NWS_MOSAIC_SECTOR(false, false, false, true),
    WX_RENDER(true, false, true, true),
    WX_RENDER_48(true, false, true, true),
    WX_OGL(true, false, false, false),
    WX_OGL_48(true, false, false, false),
    NWS_GOES(true, true, false, false);
}




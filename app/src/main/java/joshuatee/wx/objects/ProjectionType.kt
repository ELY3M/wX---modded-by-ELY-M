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

package joshuatee.wx.objects

//
// WX_OGL is used by the main Nexrad OpenGL based radar
// WX_RENDER is used by the Nexrad Canvas based radar (widget)
// WX_RENDER_48 is used by the Nexrad Canvas based radar for TDWR radar
//

enum class ProjectionType(
        val isMercator: Boolean,
        internal val needsBlackPaint: Boolean,
        internal val needsCanvasShift: Boolean,
) {
    WX_RENDER(true, false, true),
    WX_RENDER_48(true, false, true),
    WX_OGL(true, false, false),
}

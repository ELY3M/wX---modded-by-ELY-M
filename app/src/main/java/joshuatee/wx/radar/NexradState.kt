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

package joshuatee.wx.radar

import android.widget.RelativeLayout
import joshuatee.wx.common.GlobalArrays

open class NexradState(val numberOfPanes: Int) {

    val wxglRenders = mutableListOf<WXGLRender>()
    val wxglSurfaceViews = mutableListOf<WXGLSurfaceView>()
    var wxglTextObjects = mutableListOf<WXGLTextObject>()
    val relativeLayouts = mutableListOf<RelativeLayout>()
    var curRadar = 0
    var isHomeScreen = false

    var product: String
        get() = wxglRenders[curRadar].product
        set(prod) {
            wxglRenders[curRadar].product = prod
        }

    var radarSite: String
        get() = wxglRenders[curRadar].rid
        set(rid) {
            wxglRenders[curRadar].rid = rid
        }

    val surface: WXGLSurfaceView
        get() = wxglSurfaceViews[curRadar]

    val render: WXGLRender
        get() = wxglRenders[curRadar]

    val isTdwr: Boolean
        get() = isRidTdwr(radarSite)

    protected fun isRidTdwr(rid: String) = GlobalArrays.tdwrRadars.any { rid == it.split(" ")[0] }
}

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

import joshuatee.wx.*

enum class WidgetFile constructor(
    val fileName: String,
    val prefString: String,
    val action: String,
    val clazz: Class<*>
) {
    NEXRAD_RADAR("nexrad.png", "DOWNLOAD_LOC1_RADAR", "ACTION_WX_NEXRAD", WidgetNexrad::class.java),
    MOSAIC_RADAR(
        "rad_1km.png",
        "DOWNLOAD_LOC1_MOSAICS_RAD",
        "ACTION_WX",
        WidgetMosaicsRad::class.java
    ),
    SPCMESO(
        "spc_meso.png",
        "DOWNLOAD_WIDGET_SPCMESO",
        "ACTION_WX_SPCMESO",
        WidgetSPCMESO::class.java
    ),
    SPCSWO("spc_swo.png", "DOWNLOAD_WIDGET_SPCSWO", "ACTION_WX_SPCSWO", WidgetSPCSWO::class.java),
    VIS("vis_1km.png", "DOWNLOAD_LOC1_MOSAICS", "ACTION_WX_VIS", WidgetMosaics::class.java),
    WPCIMG(
        "widget_wpcimg.png",
        "DOWNLOAD_WIDGET_WPCIMG",
        "ACTION_WX_WPCIMG",
        WidgetWPCIMG::class.java
    ),
    CONUSWV(
        "widget_conuswv.png",
        "DOWNLOAD_WIDGET_CONUSWV",
        "ACTION_WX_CONUSWV",
        WidgetCONUSWV::class.java
    ),
    STRPT("widget_strpt.png", "DOWNLOAD_WIDGET_STRPT", "ACTION_WX_STRPT", WidgetSTRPT::class.java),
    NHC("widget_nhc.png", "DOWNLOAD_WIDGET_NHC", "ACTION_WX_NHC", WidgetNHC::class.java),
    HWO("", "DOWNLOAD_LOC1_TXT_HWO", "ACTION_TEXTPROD_HWO", WidgetTextHWO::class.java),
    AFD("", "DOWNLOAD_LOC1_TXT", "ACTION_TEXTPROD", WidgetTextProd::class.java),
    TEXT_WPC("", "DOWNLOAD_WIDGET_TEXT_WPC", "ACTION_TEXTWPC", WidgetTextWPC::class.java),
    CC("", "", "", WidgetCC::class.java),
    CCLegacy("", "", "", Widget::class.java);
}



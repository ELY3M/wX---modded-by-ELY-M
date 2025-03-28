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

package joshuatee.wx.util

import java.util.Locale
import android.content.Context
import android.graphics.Bitmap
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.settings.Location
import joshuatee.wx.misc.UtilityRtma
import joshuatee.wx.getImage
import joshuatee.wx.objects.FavoriteType
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.radar.UtilityRadarMosaic
import joshuatee.wx.spc.UtilitySpc
import joshuatee.wx.spc.UtilitySpcMeso
import joshuatee.wx.spc.UtilitySpcMesoInputOutput
import joshuatee.wx.spc.UtilitySpcSoundings
import joshuatee.wx.spc.UtilitySpcSwo
import joshuatee.wx.vis.UtilityGoes

@Suppress("SpellCheckingInspection")
object DownloadImage {

    fun radarMosaic(context: Context): Bitmap = try {
        val prefTokenSector = "REMEMBER_NWSMOSAIC_SECTOR"
        val sector = Utility.readPref(
            context,
            prefTokenSector,
            UtilityRadarMosaic.getNearest(Location.latLon)
        )
        UtilityRadarMosaic.get(sector).getImage()
    } catch (e: Exception) {
        UtilityLog.handleException(e)
        UtilityImg.getBlankBitmap()
    }

    fun byProduct(context: Context, product: String): Bitmap {
        var url = ""
        var bitmap = UtilityImg.getBlankBitmap()
        var needsBitmap = true
        when (product) {
            "GOES16" -> {
                needsBitmap = false
                val index = Utility.readPrefInt(context, "GOES16_IMG_FAV_IDX", 0)
                bitmap = UtilityGoes.getImage(
                    UtilityGoes.codes[index],
                    Utility.readPref(context, "GOES16_SECTOR", "cgl")
                ).getImage()
            }

            "VIS_1KM", "VIS_MAIN" -> needsBitmap = false
            "RAD_2KM" -> {
                needsBitmap = false
                bitmap = radarMosaic(context)
            }

            "IR_2KM", "WV_2KM", "VIS_2KM" -> needsBitmap = false
            "RTMA_DEW" -> url = UtilityRtma.getUrlForHomeScreen("2m_dwpt")
            "RTMA_TEMP" -> url = UtilityRtma.getUrlForHomeScreen("2m_temp")
            "RTMA_WIND" -> url = UtilityRtma.getUrlForHomeScreen("10m_wnd")
            "VIS_CONUS" -> {
                needsBitmap = false
                bitmap = UtilityGoes.getImage("02", "CONUS").getImage()
            }

            "USWARN" -> url = "https://forecast.weather.gov/wwamap/png/US.png"
            "AKWARN" -> url = "https://forecast.weather.gov/wwamap/png/ak.png"
            "HIWARN" -> url = "https://forecast.weather.gov/wwamap/png/hi.png"
            "FMAP" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/noaa/noaad1.gif"
            "FMAPD2" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/noaa/noaad2.gif"
            "FMAPD3" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/noaa/noaad3.gif"
            "FMAP12" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/basicwx/92fwbg.gif"
            "FMAP24" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/basicwx/94fwbg.gif"
            "FMAP36" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/basicwx/96fwbg.gif"
            "FMAP48" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/basicwx/98fwbg.gif"
            "FMAP72" -> url =
                GlobalVariables.NWS_WPC_WEBSITE_PREFIX + "/medr/display/wpcwx+frontsf072.gif"

            "FMAP96" -> url =
                GlobalVariables.NWS_WPC_WEBSITE_PREFIX + "/medr/display/wpcwx+frontsf096.gif"

            "FMAP120" -> url =
                GlobalVariables.NWS_WPC_WEBSITE_PREFIX + "/medr/display/wpcwx+frontsf120.gif"

            "FMAP144" -> url =
                GlobalVariables.NWS_WPC_WEBSITE_PREFIX + "/medr/display/wpcwx+frontsf144.gif"

            "FMAP168" -> url =
                GlobalVariables.NWS_WPC_WEBSITE_PREFIX + "/medr/display/wpcwx+frontsf168.gif"

            "FMAP3D" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/medr/9jhwbg_conus.gif"
            "FMAP4D" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/medr/9khwbg_conus.gif"
            "FMAP5D" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/medr/9lhwbg_conus.gif"
            "FMAP6D" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/medr/9mhwbg_conus.gif"
            "QPF1" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/qpf/fill_94qwbg.gif"
            "QPF2" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/qpf/fill_98qwbg.gif"
            "QPF3" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/qpf/fill_99qwbg.gif"
            "QPF1-2" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/qpf/d12_fill.gif"
            "QPF1-3" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/qpf/d13_fill.gif"
            "QPF4-5" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/qpf/95ep48iwbg_fill.gif"
            "QPF6-7" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/qpf/97ep48iwbg_fill.gif"
            "QPF1-5" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/qpf/p120i.gif"
            "QPF1-7" -> url = "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/qpf/p168i.gif"
            "WPC_ANALYSIS" -> url =
                "${GlobalVariables.NWS_WPC_WEBSITE_PREFIX}/images/wwd/radnat/NATRAD_24.gif"

            "NHC2ATL" -> url = "${GlobalVariables.NWS_NHC_WEBSITE_PREFIX}/xgtwo/two_atl_2d0.png"
            "NHC5ATL" -> url = "${GlobalVariables.NWS_NHC_WEBSITE_PREFIX}/xgtwo/two_atl_7d0.png"
            "NHC2EPAC" -> url = "${GlobalVariables.NWS_NHC_WEBSITE_PREFIX}/xgtwo/two_pac_2d0.png"
            "NHC5EPAC" -> url = "${GlobalVariables.NWS_NHC_WEBSITE_PREFIX}/xgtwo/two_pac_7d0.png"
            "NHC2CPAC" -> url = "${GlobalVariables.NWS_NHC_WEBSITE_PREFIX}/xgtwo/two_cpac_2d0.png"
            "NHC5CPAC" -> url = "${GlobalVariables.NWS_NHC_WEBSITE_PREFIX}/xgtwo/two_cpac_5d0.png"
            "SPC_TST" -> {
                needsBitmap = false
                val images = UtilitySpc.thunderStormOutlookImages
                bitmap = UtilityImg.mergeImagesVertically(images)
            }

            "SWOD1" -> {
                needsBitmap = false
                bitmap = UtilitySpcSwo.getImages("1", false)[0]
            }

            "WEATHERSTORY" -> {
                needsBitmap = false
                bitmap = WeatherStory.getUrl().getImage()
            }

            "WFOWARNINGS" -> {
                needsBitmap = false
                bitmap =
                    ("https://www.weather.gov/wwamap/png/" + Location.wfo.lowercase(Locale.US) + ".png").getImage()
            }

            "SWOD2" -> {
                needsBitmap = false
                bitmap = UtilitySpcSwo.getImages("2", false)[0]
            }

            "SWOD3" -> {
                needsBitmap = false
                bitmap = UtilitySpcSwo.getImages("3", false)[0]
            }

            "SWOD4" -> {
                needsBitmap = false
                bitmap = UtilitySpcSwo.getImages("4", false)[0]
            }

            "SPCMESO1" -> {
                var param = "500mb"
                val items = UIPreferences.favorites[FavoriteType.SPCMESO]!!.split(":")
                    .dropLastWhile { it.isEmpty() }
                if (items.size > 3) {
                    param = items[3]
                }
                needsBitmap = false
                bitmap = UtilitySpcMesoInputOutput.getImage(
                    context,
                    param,
                    Utility.readPref(
                        context,
                        "SPCMESO" + 1 + "_SECTOR_LAST_USED",
                        UtilitySpcMeso.DEFAULT_SECTOR
                    ),
                    UtilitySpcMesoInputOutput.getLayers(context)
                )
            }

            "SPCMESO2" -> {
                var param = "pmsl"
                val items = UIPreferences.favorites[FavoriteType.SPCMESO]!!.split(":")
                if (items.size > 4) {
                    param = items[4]
                }
                needsBitmap = false
                bitmap = UtilitySpcMesoInputOutput.getImage(
                    context,
                    param,
                    Utility.readPref(
                        context,
                        "SPCMESO" + 1 + "_SECTOR_LAST_USED",
                        UtilitySpcMeso.DEFAULT_SECTOR
                    ),
                    UtilitySpcMesoInputOutput.getLayers(context)
                )
            }

            "SPCMESO3" -> {
                var param = "ttd"
                val items = UIPreferences.favorites[FavoriteType.SPCMESO]!!.split(":")
                if (items.size > 5) {
                    param = items[5]
                }
                needsBitmap = false
                bitmap = UtilitySpcMesoInputOutput.getImage(
                    context,
                    param,
                    Utility.readPref(
                        context,
                        "SPCMESO" + 1 + "_SECTOR_LAST_USED",
                        UtilitySpcMeso.DEFAULT_SECTOR
                    ),
                    UtilitySpcMesoInputOutput.getLayers(context)
                )
            }

            "SPCMESO4" -> {
                var param = "rgnlrad"
                val items = UIPreferences.favorites[FavoriteType.SPCMESO]!!.split(":")
                if (items.size > 6) {
                    param = items[6]
                }
                needsBitmap = false
                bitmap = UtilitySpcMesoInputOutput.getImage(
                    context,
                    param,
                    Utility.readPref(
                        context,
                        "SPCMESO" + 1 + "_SECTOR_LAST_USED",
                        UtilitySpcMeso.DEFAULT_SECTOR
                    ),
                    UtilitySpcMesoInputOutput.getLayers(context)
                )
            }

            "SPCMESO5" -> {
                var param = "lllr"
                val items = UIPreferences.favorites[FavoriteType.SPCMESO]!!.split(":")
                if (items.size > 7) {
                    param = items[7]
                }
                needsBitmap = false
                bitmap = UtilitySpcMesoInputOutput.getImage(
                    context,
                    param,
                    Utility.readPref(
                        context,
                        "SPCMESO" + 1 + "_SECTOR_LAST_USED",
                        UtilitySpcMeso.DEFAULT_SECTOR
                    ),
                    UtilitySpcMesoInputOutput.getLayers(context)
                )
            }

            "SPCMESO6" -> {
                var param = "laps"
                val items = UIPreferences.favorites[FavoriteType.SPCMESO]!!.split(":")
                if (items.size > 8) {
                    param = items[8]
                }
                needsBitmap = false
                bitmap = UtilitySpcMesoInputOutput.getImage(
                    context,
                    param,
                    Utility.readPref(
                        context,
                        "SPCMESO" + 1 + "_SECTOR_LAST_USED",
                        UtilitySpcMeso.DEFAULT_SECTOR
                    ),
                    UtilitySpcMesoInputOutput.getLayers(context)
                )
            }

            "CONUSWV" -> {
                needsBitmap = false
                bitmap = UtilityGoes.getImage("09", "CONUS").getImage()
            }

            "LTG" -> {
                needsBitmap = false
                bitmap = UtilityGoes.getImage("GLM", "CONUS").getImage()
            }

            "SND" -> {
                needsBitmap = false
                bitmap = UtilitySpcSoundings.getImage(
                    context,
                    SoundingSites.sites.getNearest(Location.latLon)
                )
            }

            "STRPT" -> url = UtilitySpc.getStormReportsTodayUrl()
            else -> needsBitmap = false
        }
        return if (needsBitmap) {
            url.getImage()
        } else {
            bitmap
        }
    }
}

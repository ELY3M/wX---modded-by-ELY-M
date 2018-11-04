/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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

package joshuatee.wx.wpc

import android.util.SparseArray
import joshuatee.wx.MyApplication

import joshuatee.wx.util.Group
import joshuatee.wx.ui.ObjectMenuTitle
import java.util.*

internal object UtilityWPCImages {

    private val TITLES = Arrays.asList(
            ObjectMenuTitle("Surface Analysis", 7),
            ObjectMenuTitle("Forecast Maps", 20),
            ObjectMenuTitle("QPF", 27),
            ObjectMenuTitle("Snow / Ice", 21),
            ObjectMenuTitle("National Digital Forecast Database", 15),
            ObjectMenuTitle("CPC Outlooks", 22),
            ObjectMenuTitle("Aviation", 14),
            ObjectMenuTitle("Space Weather", 3)
    )

    val LABELS = listOf(
            "WPC Analysis, Radar, Warnings",
            "Surface Analysis with Obs (CONUS)",
            "Surface Analysis with Obs (NHEM)",
            "Analysis with Satellite (CONUS)",
            "Analysis with Satellite (NHEM)",
            "Analysis with Satellite (NHEM+PAC)",
            "High resolution surface analysis",

            "Forecast Chart - Day 1",
            "Forecast Chart - Day 2",
            "Forecast Chart - Day 3",
            "Forecast Chart - Day 1 Experimental",
            "Forecast Chart - Day 2 Experimental",
            "Forecast Chart - Day 3 Experimental",
            "Fronts/Weather Type - 12hr",
            "Fronts/Weather Type - 24hr",
            "Fronts/Weather Type - 36hr",
            "Fronts/Weather Type - 48hr",
            "WPC Fronts - 3day",
            "WPC Fronts - 4day",
            "WPC Fronts - 5day",
            "WPC Fronts - 6day",
            "WPC Fronts - 7day",
            "Forecast map - 3day (NHEM)",
            "Forecast map - 4day (NHEM)",
            "Forecast map - 5day (NHEM)",
            "Forecast map - 6day (NHEM)",
            "Forecast map - 7day (NHEM)",

            "QPF Day 1",
            "QPF Day 2",
            "QPF Day 3",
            "QPF Day 1-2",
            "QPF Day 1-3",
            "QPF Day 4-5",
            "QPF Day 6-7",
            "QPF 5 Day Total",
            "QPF 7 Day Total",
            "WPC Day 1 Excessive Rainfall Outlook",
            "WPC Day 2 Excessive Rainfall Outlook",
            "WPC Day 3 Excessive Rainfall Outlook",
            "QPF Day 1 00-06 hr",
            "QPF Day 1 06-12 hr",
            "QPF Day 1 12-18 hr",
            "QPF Day 1 18-24 hr",
            "QPF Day 1 24-30 hr",
            "QPF Day 2 30-36 hr",
            "QPF Day 2 36-42 hr",
            "QPF Day 2 42-48 hr",
            "QPF Day 2 48-54 hr",
            "QPF Day 3 54-60 hr",
            "QPF Day 3 60-66 hr",
            "QPF Day 3 66-72 hr",
            "QPF Day 3 72-78 hr",
            "QPF Day 3 1/2 78-84 hr",
            "QPF Day 3 1/2 84-90 hr",

            "Snow Day 1 Prob > 4in",
            "Snow Day 1 Prob > 8in",
            "Snow Day 1 Prob > 12in",
            "Ice Day 1 Prob > 0.25in",
            "Snow Day 2 Prob > 4in",
            "Snow Day 2 Prob > 8in",
            "Snow Day 2 Prob > 12in",
            "Ice Day 2 Prob > 0.25in",
            "Snow Day 3 Prob > 4in",
            "Snow Day 3 Prob > 8in",
            "Snow Day 3 Prob > 12in",
            "Ice Day 3 Prob > 0.25in",
            "Snow/Sleet Day 4 Prob (experimental)",
            "Snow/Sleet Day 5 Prob (experimental)",
            "Snow/Sleet Day 6 Prob (experimental)",
            "Snow/Sleet Day 7 Prob (experimental)",
            "GLSEA Ice Analysis",
            "GL ice concentration",
            "GL estimated max thickness of level ice",
            "GL Traditional Composite - West",
            "GL Traditional Composite - East",

            "High Temperature",
            "Min Temperature",
            "Prob Precip (12hr)",
            "Predominant Weather",
            "Hazards",
            "Temperature",
            "Dewpoint",
            "Wind Speed (kts)",
            "Wind Gust (kts)",
            "Sky Cover",
            "Snow Amount (6hr)",
            "Ice Accumulation (6hr)",
            "Wave Height",
            "Apparent Temp",
            "Relative Humidity",

            "6 to 10 Day Outlooks - Temperature",
            "6 to 10 Day Outlooks - Precipitation",
            "8 to 14 Day Outlooks - Temperature",
            "8 to 14 Day Outlooks - Precipitation",
            "6-10 Day Heat Index Outlook",
            "8-14 Day Heat Index Outlook",
            "6-10 Day Lowest Wind Chill Outlook",
            "8-14 Day Lowest Wind Chill Outlook",
            "Week 3-4 Outlooks - Temperature ( Experimental )",
            "Week 3-4 Outlooks - Precipitation ( Experimental )",
            "One Month Outlook - Temperature",
            "One Month Outlook - Precipitation",
            "US Monthly Drought Outlook",
            "US Seasonal Drought Outlook",
            "US Drought Monitor",
            "UV Index Forecast Day 1",
            "UV Index Forecast Day 2",
            "UV Index Forecast Day 3",
            "UV Index Forecast Day 4",
            "Day 3-7 U.S. Hazards Outlook",
            "Day 8-14 U.S. Hazards Outlook",
            "Global Tropics Hazards and Benefits Outlook",

            "AWC Convective Sigmets",
            "AWC Turbulence Sigmets",
            "AWC Icing Sigmets",
            "Aviation - WPC Surface Analysis",
            "Aviation - WPC 12 hour Forecast",
            "Aviation - WPC 24 hour Forecast",
            "Aviation - WPC 36 hour Forecast",
            "Aviation - WPC 48 hour Forecast",
            "Aviation - WPC 60 hour Forecast",
            "Aviation - WPC 3 day Forecast",
            "Aviation - WPC 4 day Forecast",
            "Aviation - WPC 5 day Forecast",
            "Aviation - WPC 6 day Forecast",
            "Aviation - WPC 7 day Forecast",

            "Aurora Forecast - North",
            "Aurora Forecast - South",
            "Estimated Planetary K index"
    )

    val PARAMS = listOf(
            "${MyApplication.nwsWPCwebsitePrefix}/images/wwd/radnat/NATRAD_24.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/sfc/namussfcwbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/sfc/90fwbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/sfc/ussatsfc.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/sfc/satsfcnps.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/sfc/satsfc.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/sfc/lrgnamsfc09wbg.gif",

            "${MyApplication.nwsWPCwebsitePrefix}/noaa/noaad1.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/noaa/noaad2.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/noaa/noaad3.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/exper/nationalforecastchart/data/day1.png",
            "${MyApplication.nwsWPCwebsitePrefix}/exper/nationalforecastchart/data/day2.png",
            "${MyApplication.nwsWPCwebsitePrefix}/exper/nationalforecastchart/data/day3.png",
            "${MyApplication.nwsWPCwebsitePrefix}/basicwx/92fwbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/basicwx/94fwbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/basicwx/96fwbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/basicwx/98fwbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/medr/9jhwbg_conus.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/medr/9khwbg_conus.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/medr/9lhwbg_conus.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/medr/9mhwbg_conus.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/medr/9nhwbg_conus.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/medr/9jh.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/medr/9kh.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/medr/9lh.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/medr/9mh.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/medr/9nh.gif",

            "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_94qwbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_98qwbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_99qwbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/d12_fill.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/d13_fill.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/95ep48iwbg_fill.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/97ep48iwbg_fill.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/p120i.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/p168i.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/94ewbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/98ewbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/99ewbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_91ewbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_92ewbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_93ewbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_9eewbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_9fewbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_9gewbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_9hewbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_9iewbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_9jewbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_9kewbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_9lewbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_9oewbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_9newbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_9pewbg.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/qpf/fill_9qewbg.gif",

            "${MyApplication.nwsWPCwebsitePrefix}/wwd/day1_psnow_gt_04.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/wwd/day1_psnow_gt_08.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/wwd/day1_psnow_gt_12.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/wwd/day1_pice_gt_25.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/wwd/day2_psnow_gt_04.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/wwd/day2_psnow_gt_08.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/wwd/day2_psnow_gt_12.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/wwd/day2_pice_gt_25.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/wwd/day3_psnow_gt_04.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/wwd/day3_psnow_gt_08.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/wwd/day3_psnow_gt_12.gif",
            "${MyApplication.nwsWPCwebsitePrefix}/wwd/day3_pice_gt_25.gif",
            "${MyApplication.nwsWPCwebsitePrefix}//wwd/pwpf_d47/gif/prbww_sn25_f096.gif",
            "${MyApplication.nwsWPCwebsitePrefix}//wwd/pwpf_d47/gif/prbww_sn25_f120.gif",
            "${MyApplication.nwsWPCwebsitePrefix}//wwd/pwpf_d47/gif/prbww_sn25_f144.gif",
            "${MyApplication.nwsWPCwebsitePrefix}//wwd/pwpf_d47/gif/prbww_sn25_f168.gif",
            "https://coastwatch.glerl.noaa.gov/glsea/cur/glsea_cur.png",
            "https://www.weather.gov/images/cle/ICE/dist9_concentration.jpg",
            "https://www.weather.gov/images/cle/ICE/dist9_thickness.jpg",
            "https://www.weather.gov/images/cle/ICE/egg_west.jpg",
            "https://www.weather.gov/images/cle/ICE/egg_east.jpg",

            "http://graphical.weather.gov/images/conus/MaxT",
            "http://graphical.weather.gov/images/conus/MinT",
            "http://graphical.weather.gov/images/conus/PoP12",
            "http://graphical.weather.gov/images/conus/Wx",
            "http://graphical.weather.gov/images/conus/WWA",
            "http://graphical.weather.gov/images/conus/T",
            "http://graphical.weather.gov/images/conus/Td",
            "http://graphical.weather.gov/images/conus/WindSpd",
            "http://graphical.weather.gov/images/conus/WindGust",
            "http://graphical.weather.gov/images/conus/Sky",
            "http://graphical.weather.gov/images/conus/SnowAmt",
            "http://graphical.weather.gov/images/conus/IceAccum",
            "http://graphical.weather.gov/images/conus/WaveHeight",
            "http://graphical.weather.gov/images/conus/ApparentT",
            "http://graphical.weather.gov/images/conus/RH",

            "http://www.cpc.ncep.noaa.gov/products/predictions/610day/610temp.new.gif",
            "http://www.cpc.ncep.noaa.gov/products/predictions/610day/610prcp.new.gif",
            "http://www.cpc.ncep.noaa.gov/products/predictions/814day/814temp.new.gif",
            "http://www.cpc.ncep.noaa.gov/products/predictions/814day/814prcp.new.gif",
            "http://www.cpc.ncep.noaa.gov/products/predictions/short_range/heat/himx.wcp0.08.gif",
            "http://www.cpc.ncep.noaa.gov/products/predictions/short_range/heat/himx.wcp0.11.gif",
            "http://www.cpc.ncep.noaa.gov/products/predictions/short_range/cold/LWCF.5.gif",
            "http://www.cpc.ncep.noaa.gov/products/predictions/short_range/cold/LWCF.7.gif",
            "http://www.cpc.ncep.noaa.gov/products/predictions/WK34/gifs/WK34temp.gif",
            "http://www.cpc.ncep.noaa.gov/products/predictions/WK34/gifs/WK34prcp.gif",
            "http://www.cpc.ncep.noaa.gov/products/predictions/30day/off15_temp.gif",
            "http://www.cpc.ncep.noaa.gov/products/predictions/30day/off15_prcp.gif",
            "http://www.cpc.ncep.noaa.gov/products/expert_assessment/month_drought.png",
            "http://www.cpc.ncep.noaa.gov/products/expert_assessment/sdohomeweb.png",
            "http://droughtmonitor.unl.edu/data/png/current/current_usdm.png",
            "http://www.cpc.ncep.noaa.gov/products/stratosphere/uv_index/gif_files/uvi_usa_f1_wmo.gif",
            "http://www.cpc.ncep.noaa.gov/products/stratosphere/uv_index/gif_files/uvi_usa_f2_wmo.gif",
            "http://www.cpc.ncep.noaa.gov/products/stratosphere/uv_index/gif_files/uvi_usa_f3_wmo.gif",
            "http://www.cpc.ncep.noaa.gov/products/stratosphere/uv_index/gif_files/uvi_usa_f4_wmo.gif",
            "http://www.cpc.noaa.gov/products/predictions/threats/hazards_d3_7_contours.png",
            "http://www.cpc.noaa.gov/products/predictions/threats/hazards_d8_14_contours.png",
            "http://www.cpc.ncep.noaa.gov/products/precip/CWlink/ghazards/images/gth_full_update.png",

            "http://www.aviationweather.gov/adds/data/airmets/airmets_CB.gif",
            "http://www.aviationweather.gov/adds/data/airmets/airmets_TB.gif",
            "http://www.aviationweather.gov/adds/data/airmets/airmets_IC.gif",
            "http://www.aviationweather.gov/adds/data/progs/hpc_sfc_analysis.gif",
            "http://www.aviationweather.gov/adds/data/progs/hpc_12_fcst.gif",
            "http://www.aviationweather.gov/adds/data/progs/hpc_24_fcst.gif",
            "http://www.aviationweather.gov/adds/data/progs/hpc_36_fcst.gif",
            "http://www.aviationweather.gov/adds/data/progs/hpc_48_fcst.gif",
            "http://www.aviationweather.gov/adds/data/progs/hpc_60_fcst.gif",
            "http://www.aviationweather.gov/adds/data/progs/hpc_mid_072.gif",
            "http://www.aviationweather.gov/adds/data/progs/hpc_mid_096.gif",
            "http://www.aviationweather.gov/adds/data/progs/hpc_mid_120.gif",
            "http://www.aviationweather.gov/adds/data/progs/hpc_mid_144.gif",
            "http://www.aviationweather.gov/adds/data/progs/hpc_mid_168.gif",

            "http://services.swpc.noaa.gov/images/animations/ovation-north/latest.jpg",
            "http://services.swpc.noaa.gov/images/animations/ovation-south/latest.jpg",
            "http://services.swpc.noaa.gov/images/planetary-k-index.gif"
    )

    val GROUPS = SparseArray<Group>()
    var SHORT_CODES = Array(8) { Array(28) { "" } }
    var LONG_CODES = Array(8) { Array(28) { "" } }

    internal fun createData() {
        var k = 0
        TITLES.indices.forEach { index ->
            val group = Group(TITLES[index].title)
            var m = 0
            for (j in (ObjectMenuTitle.getStart(TITLES, index) until TITLES[index].count + ObjectMenuTitle.getStart(TITLES, index))) {
                group.children.add(LABELS[j])
                SHORT_CODES[index][m] = PARAMS[k]
                LONG_CODES[index][m] = LABELS[k]
                k += 1
                m += 1
            }
            GROUPS.append(index, group)
        }
    }
}


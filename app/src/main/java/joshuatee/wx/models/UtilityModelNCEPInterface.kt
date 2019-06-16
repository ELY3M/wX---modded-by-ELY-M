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

package joshuatee.wx.models

internal object UtilityModelNcepInterface {

    val models = listOf(
            "HRRR",
            "GFS",
            "NAM",
            "NAM-HIRES",
            "RAP",
            "HRW-NMMB",
            "HRW-ARW",
            "GEFS-SPAG",
            "GEFS-MEAN-SPRD",
            "SREF",
            "NAEFS",
            "POLAR",
            "WW3",
            "WW3-ENP",
            "WW3-WNA",
            "ESTOFS",
            "FIREWX",
            "HRW-ARW2",
            "HREF",
            "NBM"
    )

    val sectorsGfs = listOf(
            "NAMER",
            "SAMER",
            "AFRICA",
            "NORTH-PAC",
            "EAST-PAC",
            "WN-ATL",
            "ATLANTIC",
            "POLAR",
            "ALASKA",
            "EUROPE",
            "ASIA",
            "SOUTH-PAC",
            "ARTIC",
            "INDIA",
            "US-SAMOA"
    )

    val sectorsNam = listOf(
            "NAMER",
            "NORTH-PAC",
            "EAST-PAC",
            "WN-ATL"
    )

    val sectorsNamHires = listOf(
            "CONUS",
            "ALASKA",
            "US-NW",
            "US-SW",
            "US-NC",
            "US-SC",
            "US-NE",
            "US-SE"
    )

    val sectorsRap = listOf("CONUS")

    val sectorsHrrr = listOf(
            "CONUS",
            "US-NW",
            "US-SW",
            "US-NC",
            "US-SC",
            "US-NE",
            "US-SE"
    )

    val sectorsSref = listOf(
            "NAMER",
            "ALASKA"
    )

    val sectorsNaefs = listOf(
            "NAMER",
            "SAMER",
            "AFRICA",
            "NORTH-PAC",
            "EAST-PAC",
            "WN-ATL",
            "ATLANTIC",
            "EUROPE",
            "ASIA",
            "SOUTH-PAC",
            "INDIA",
            "POLAR",
            "ARCTIC"
    )

    val sectorsPolar = listOf("POLAR")

    val sectorsHrwNmm = listOf(
            "CONUS",
            "HAWAII",
            "GUAM",
            "ALASKA",
            "PR",
            "US-NW",
            "US-SW",
            "US-NC",
            "US-SC",
            "US-NE",
            "US-SE"
    )

    val sectorsHrwArw2 = listOf(
            "CONUS",
            "HAWAII",
            "ALASKA",
            "PR",
            "US-NW",
            "US-SW",
            "US-NC",
            "US-SC",
            "US-NE",
            "US-SE"
    )

    val sectorsHref= listOf(
            "CONUS",
            "US-NW",
            "US-SW",
            "US-NC",
            "US-SC",
            "US-NE",
            "US-SE"
    )

    val sectorsGefsSpag = listOf(
            "NAMER",
            "SAMER",
            "AFRICA",
            "NORTH-PAC",
            "EAST-PAC",
            "WN-ATL",
            "ATLANTIC",
            "EUROPE",
            "ASIA",
            "SOUTH-PAC",
            "INDIA"
    )

    val sectorsGefsMnsprd = listOf(
            "NAMER",
            "SAMER",
            "AFRICA",
            "NORTH-PAC",
            "EAST-PAC",
            "WN-ATL",
            "ATLANTIC",
            "EUROPE",
            "ASIA",
            "SOUTH-PAC",
            "INDIA",
            "POLAR",
            "ARCTIC"
    )

    val sectorsWw3 = listOf(
            "ATLANTIC",
            "ATL-PAC",
            "NORTH-PAC",
            "EAST-PAC",
            "WN-ATL"
    )

    val sectorsWw3Enp = listOf(
            "NORTH-PAC",
            "EAST-PAC"
    )

    val sectorsWw3Wna = listOf(
            "WN-ATL"
    )

    val sectorsEstofs = listOf(
            "WEST-GOA",
            "EAST-GOA",
            "WA-OR",
            "NORTH-CAL",
            "SOUTH-CAL",
            "NE-COAST",
            "MID-ATL",
            "SE-COAST",
            "EGOM",
            "WGOM",
            "HAWAII"
    )

    val sectorsFirewx = listOf(
            "CONUS-AK"
    )

    val sectorsNbm = listOf(
            "CONUS",
            "NAMER"
    )

    val paramsGfs = listOf(
            "1000_500_thick",
            "1000_850_thick",
            "850_700_thick",
            "10m_wnd_precip",
            "10m_wnd_2m_temp",
            "850_temp_mslp_precip",
            "200_wnd_ht",
            "250_wnd_ht",
            "300_wnd_ht",
            "500_rh_ht",
            "500_wnd_ht",
            "500_vort_ht",
            "700_rh_ht",
            "850_pw_ht",
            "850_rh_ht",
            "850_temp_ht",
            "850vor_500ht_200wd",
            "850_vort_ht",
            "925_temp_ht",
            "precip_p01",
            "precip_p03",
            "precip_p06",
            "precip_p12",
            "precip_p24",
            "precip_p36",
            "precip_p48",
            "precip_p60",
            "precip_ptot",
            "dom_precip_type",
            "snodpth_chng"
    )

    val labelsGfs = listOf(
            "MSLP, 1000-500mb thickness, 0x hr PCPN (in.)",
            "MSLP, 1000-850mb thickness, 0x hr PCPN (in.)",
            "MSLP, 850-700mb thickness, 0x hr PCPN (in.)",
            "MSLP, 10 Meter Wind & Precip",
            "MSLP, 10 Meter Wind & 2 Meter Temp",
            "MSLP, 850mb temperature, 6 hourly total precipitation",
            "200mb Wind and Height",
            "250mb Wind and Height",
            "300mb Wind and Height",
            "500mb Relative Humidity and Height",
            "500mb Wind and Height",
            "500mb Vorticity, Wind, and Height",
            "700mb Relative Humidity, Height and Omega",
            "850mb Height, Precipitable Water and Wind",
            "850mb Relative Humidity and Height",
            "850mb Temperature, Wind and Height",
            "850mb Vorticity, 500mb Height, 200mb Wind",
            "850mb Vorticity, Wind and Height",
            "925mb Temperature, Wind and Height",
            "Total Precipitation every 1 hour",
            "Total Precipitation every 3 hours",
            "Total Precipitation every 6 hours",
            "Total Precipitation every 12 hours",
            "Total Precipitation every 24 hours",
            "Total Precipitation every 36 hours",
            "Total Precipitation every 48 hours",
            "Total Precipitation every 60 hours",
            "Total Accumulated Precipitation of Period",
            "Dominant Precipitation Type",
            "Snow Depth Change for previous 001 hours"
    )

    val paramsNam = listOf(
            "precip_p03",
            "precip_p06",
            "precip_p12",
            "precip_p24",
            "precip_p36",
            "precip_p48",
            "precip_p60",
            "precip_ptot",
            "snodpth_chng",
            "1000_500_thick",
            "1000_850_thick",
            "10m_wnd_2m_temp",
            "10m_wnd_precip",
            "850_700_thick",
            "850_temp_mslp_precip",
            "sim_radar_1km",
            "200_wnd_ht",
            "250_wnd_ht",
            "300_wnd_ht",
            "500_rh_ht",
            "500_wnd_ht",
            "500_vort_ht",
            "700_rh_ht",
            "850_pw_ht",
            "850_rh_ht",
            "850_temp_ht",
            "850_vort_ht",
            "850vor_500ht_200wd",
            "925_temp_ht"
    )

    val labelsNam = listOf(
            "Total Precipitation every 3 hours",
            "Total Precipitation every 6 hours",
            "Total Precipitation every 12 hours",
            "Total Precipitation every 24 hours",
            "Total Precipitation every 36 hours",
            "Total Precipitation every 48 hours",
            "Total Precipitation every 60 hours",
            "Total Accumulated Precipitation of Period",
            "Snow Depth Change for previous 001 hours",
            "MSLP, 1000-500mb thickness, 6 hourly total precipitation",
            "MSLP, 1000-850mb thickness, 6 hourly total precipitation",
            "MSLP, 10m wind, 2m temperature",
            "MSLP, 10m wind, 6 houly total precip, 2m temperature",
            "MSLP, 850-700mb thickness, 6 hourly total precipitation",
            "MSLP, 850mb temperature, 6 hourly total precipitation",
            "Simulated Radar Reflectivity",
            "200mb Wind and Height",
            "250mb Wind and Height",
            "300mb Wind and Height",
            "500mb Relative Humidity and Height",
            "500mb Wind and Height",
            "500mb Vorticity, Wind, and Height",
            "700mb Relative Humidity, Height and Omega",
            "850mb Height, Precipitable Water and Wind",
            "850mb Relative Humidity and Height",
            "850mb Temperature, Wind and Height",
            "850mb Vorticity, Wind and Height",
            "850mb Vorticity, 500mb Height, 200mb Wind",
            "925mb Temperature, Wind and Height"
    )

    val paramsRap = listOf(
            "precip_p01",
            "precip_ptot",
            "precip_rate",
            "snow_total",
            "1000_500_thick",
            "1000_850_thick",
            "850_700_thick",
            "cape_cin",
            "echo_top",
            "helicity",
            "vis",
            "sim_radar_1km",
            "2m_temp_10m_wnd",
            "2m_dewp_10m_wnd",
            "10m_wnd_sfc_gust",
            "200_wnd_ht",
            "250_wnd_ht",
            "300_wnd_ht",
            "500_vort_ht",
            "500_temp_ht",
            "700_rh_ht",
            "850_temp_ht",
            "925_temp_ht"
    )

    val labelsRap = listOf(
            "Hourly Total Precipitation",
            "Total Accumulated Precipitation",
            "Precipitation Rate",
            "RAP 1-hr Snow Acc (in.)",
            "MSLP, 1000-500mb thickness, 0-1hr PCPN (in.)",
            "MSLP, 1000-850mb thickness, 0-1hr PCPN (in.)",
            "MSLP, 850-700mb thickness, 0-1hr PCPN (in.)",
            "CAPE/CIN (J/KG)",
            "Echo Top Height Image",
            "Helicity and 30m Wind",
            "Visibility",
            "Simulated Radar Reflectivity",
            "2 Meter Temp and 10 Meter Wind",
            "2 Meter Dew Point and 10 Meter Wind",
            "10 Meter Wind and Surface Gust",
            "200mb Wind and Height",
            "250mb Wind and Height",
            "300mb Wind and Height",
            "500mb Vorticity, Wind, and Height",
            "500mb Temperature, Wind and Height",
            "700mb Relative Humidity, Wind, Height and Omega",
            "850mb Temperature, Wind, and Height",
            "925mb Temperature, Wind and Height"
    )

    val paramsHrrr = listOf(
            "precip_p01",
            "precip_ptot",
            "precip_type",
            "precip_rate",
            "snow_total",
            "1000_500_thick",
            "1000_850_thick",
            "850_700_thick",
            "850_temp_mslp_precip",
            "echo_top",
            "helicity_3km",
            "helicity_1km",
            "lightning",
            "sim_radar_1km",
            "sim_radar_max",
            "10m_wnd",
            "2m_temp_10m_wnd",
            "2m_dewp_10m_wnd",
            "10m_wnd_sfc_gust",
            "vis",
            "max_updraft_hlcy",
            "10m_maxwnd",
            "sfc_cape_cin",
            "best_cape_cin",
            "250_wnd",
            "300_wnd",
            "500_vort_ht",
            "500_temp_ht",
            "700_rh_ht",
            "850_temp_ht",
            "925_temp_wnd",
            "ceiling"
    )

    val labelsHrrr = listOf(
            "Hourly Total Precipitation",
            "15-hr Total Precipitation",
            "Precipitation Type",
            "Precipitation Rate",
            "1-hr Snow Acc (in.) ",
            "PMSL, 1000-500mb thickness, hourly precipitation",
            "PMSL, 1000-850mb thickness, hourly precipitation",
            "PMSL, 850-700mb thickness, hourly precipitation",
            "MSLP, 850mb temperature, 1hourly precipitation",
            "Echo Tops",
            "0-3km Helicity and Storm Motion",
            "0-1km Helicity and Storm Motion",
            "Lightning Flash Rate ",
            "Simulated Radar Reflectivity 1km",
            "Max Simulated Radar Reflectivity",
            "10 meter Wind",
            "2 meter Temperature and 10 meter Wind",
            "2 meter Dew Point and 10 meter wind",
            "10 meter wind gust",
            "Visibility",
            "Max 2-5km Updraft Helicity ",
            "Max 10m Wind Speed",
            "Surface-Based Convective Available Potential Energy and Convective Inhibition",
            "Most Unstable Convective Available Potential Energy and Convective Inhibition",
            "250mb Wind",
            "300mb Wind",
            "500mb Vorticity, Wind, and Height",
            "500mb Temperature, Wind and Height",
            "700mb Relative Humidity, Wind, Height and Omega",
            "850mb Temperature, Wind, and Height",
            "925mb Temperature and Wind",
            "Cloud Ceiling"
    )

    val paramsNamHires = listOf(
            "1000_500_thick",
            "1000_850_thick",
            "10m_wnd_precip",
            "850_700_thick",
            "850_temp_mslp_precip",
            "200_wnd_ht",
            "250_wnd_ht",
            "300_wnd_ht",
            "500_vort_ht",
            "500_temp_ht",
            "700_rh_ht",
            "850_temp_ht",
            "850_vort_ht",
            "925_temp_ht",
            "10m_maxwnd",
            "10m_wnd",
            "10m_wnd_sfc_gust",
            "2m_dewp_10m_wnd",
            "2m_temp_10m_wnd",
            "best_cape_cin",
            "echo_top",
            "ceiling",
            "helicity_1km",
            "helicity_3km",
            "max_updraft_hlcy",
            "sfc_cape_cin",
            "sim_radar_1km",
            "vis",
            "850vor_500ht_200wd",
            "dom_precip_type",
            "snodpth_chng",
            "precip_p01",
            "precip_p03",
            "precip_p06",
            "precip_p12",
            "precip_p24",
            "precip_p36",
            "precip_p48",
            "precip_p60",
            "precip_ptot"
    )

    val labelsNamHires = listOf(
            "MSLP, 1000-500mb thickness, 6 hourly total precipitation",
            "MSLP, 1000-850mb thickness, 6 hourly total precipitation",
            "MSLP, 10m wind, 6 houly total precip, 2m temperature",
            "MSLP, 850-700mb thickness, 6 hourly total precipitation",
            "MSLP, 850mb temperature, 6 hourly total precipitation",
            "200mb Wind and Height",
            "250mb Wind and Height",
            "300mb Wind and Height",
            "500mb Vorticity, Wind, and Height",
            "500mb Temperature, Wind and Height",
            "700mb Relative Humidity, Height and Omega",
            "850mb Temperature, Wind and Height",
            "850mb Vorticity, Wind and Height",
            "925mb Temperature, Wind and Height",
            "Max 10m Wind Speed",
            "10 meter Wind",
            "10 meter wind gust",
            "2 meter Dew Point and 10 meter wind",
            "2 meter Temperature and 10 meter Wind",
            "Most Unstable Convective Available Potential Energy and Convective Inhibition",
            "Echo Top Height Image",
            "Cloud Ceiling",
            "0-1km Helicity and Storm Motion",
            "0-3km Helicity and Storm Motion",
            "Max 2-5km Updraft Helicity",
            "Surface-Based Convective Available Potential Energy and Convective Inhibition",
            "Simulated Radar Reflectivity 1km",
            "Visibility",
            "850mb Vorticity, 500mb Height, 200mb Wind",
            "Dominant Precipitation Type",
            "Snow Depth Change for previous 001 hours",
            "1 Hour Accumulated Precipitation",
            "3 Hour Accumulated Precipitation",
            "6 Hour Accumulated Precipitation",
            "12 Hour Accumulated Precipitation",
            "24 Hour Accumulated Precipitation",
            "36 Hour Accumulated Precipitation",
            "48 Hour Accumulated Precipitation",
            "60 Hour Accumulated Precipitation",
            "Precipitation Total"
    )

    val paramsSref = listOf(
            "precip_p03",
            "precip_p06",
            "precip_p12",
            "precip_p24",
            "precip_ptot",
            "prob_precip_0.25in",
            "snow_total_mean",
            "snow_total_sprd",
            "1000_500_thick",
            "1000_850_thick",
            "10m_wind",
            "2m_temp",
            "850_700_thick",
            "cape",
            "cin",
            "lifted_index",
            "mslp",
            "prob_10m_wind",
            "prob_2m_temp",
            "prob_cape",
            "250_vort_ht",
            "250_wnd",
            "500_vort_ht",
            "700_rh",
            "700_temp",
            "850_rh",
            "850_temp",
            "850_wnd"
    )

    val labelsSref = listOf(
            "Mean 3-hour Precipitation",
            "Mean 6-hour Precipitation",
            "Mean 12-hour Precipitation",
            "Mean 24-hour Precipitation",
            "Total Accumulated Precipitation of Period",
            "Probability of 6-hrly Precipitation > 0.25 (in)",
            "Snow Mean for past 3 hours (in)",
            "Snow Spread for past 3 hours (in)",
            "Mean 1000-850mb Thickness (m)",
            "Mean 1000-500mb Thickness (m)",
            "10m Winds",
            "2m Temperature",
            "Mean 850-700mb Thickness (m)",
            "Mean Convective Available Potential Energy",
            "Mean Convective Inhibition",
            "Mean Lifted Index",
            "Mean Sea Level Pressure",
            "Probability of 10m Wind Speeds > 25 knots",
            "Probablility of 2m Temperature < 0",
            "Probability of Cape",
            "250mb Vorticity and Height",
            "250mb Wind",
            "500mb Vorticity and Height",
            "700mb Relative Humidity",
            "700mb Temperature",
            "850mb Relative Humidity",
            "850mb Temperature",
            "850mb Wind"
    )

    val paramsNaefs = listOf(
            "10m_wnd",
            "2m_temp",
            "mslp",
            "250_temp",
            "250_wnd",
            "500_temp",
            "500_vort_ht",
            "500_wnd",
            "700_temp",
            "700_vort_ht",
            "700_wnd",
            "850_temp",
            "850_vort_ht",
            "850_wnd",
            "925_wnd"
    )

    val labelsNaefs = listOf(
            "10m Winds",
            "2 meter Temperature",
            "Mean Sea Level Pressure",
            "250mb Temperature",
            "250mb Winds",
            "500mb Temperature",
            "500mb Vorticity and Height",
            "500mb Winds",
            "700mb Temperature",
            "700mb Vorticity and Height",
            "700mb Winds",
            "850mb Temperature",
            "850mb Vorticity and Height",
            "850mb Winds",
            "925mb Winds"
    )

    val paramsPolar = listOf("ice_drift")

    val labelsPolar = listOf("Polar Ice Drift")

    val paramsHrwNmm = listOf(
            "10m_wnd",
            "10m_wnd_sfc_gust",
            "2m_dewp_10m_wnd",
            "2m_temp_10m_wnd",
            "best_cape_cin",
            "ceiling",
            "echo_top",
            "helicity_1km",
            "helicity_3km",
            "max_updraft_hlcy",
            "sfc_cape_cin",
            "sim_radar_1km",
            "vis",
            "precip_p01",
            "precip_p03",
            "precip_p06",
            "precip_p12",
            "precip_p24",
            "precip_p36",
            "precip_p48",
            "precip_ptot",
            "dom_precip_type",
            "1000_500_thick",
            "10m_wnd_precip",
            "250_wnd_ht",
            "300_wnd_ht",
            "500_vort_ht",
            "700_rh_ht",
            "850_temp_ht"
    )

    val labelsHrwNmm = listOf(
            "10 meter Wind",
            "10 meter wind gust",
            "2 meter Dew Point and 10 meter wind",
            "2 meter Temperature and 10 meter Wind",
            "Most Unstable Convective Available Potential Energy and Convective Inhibition",
            "Cloud Ceiling",
            "Echo Tops",
            "0-1km Helicity and Storm Motion",
            "0-3km Helicity and Storm Motion",
            "Max 2-5km Updraft Helicity",
            "Surface-Based Convective Available Potential Energy and Convective Inhibition",
            "Simulated Radar Reflectivity 1km",
            "Visibility",
            "Total Precipitation every 1 hours",
            "Total Precipitation every 3 hours",
            "Total Precipitation every 6 hours",
            "Total Precipitation every 12 hours",
            "Total Precipitation every 24 hours",
            "Total Precipitation every 36 hours",
            "Total Precipitation every 48 hours",
            "Precipitation Total",
            "Dominant Precipitation Type",
            "MSLP, 1000-500mb thickness, 3 hourly total precipitation",
            "MSLP, 10m wind, 6 houly total precip, 2m temperature",
            "250mb Wind and Height",
            "300mb Wind and Height",
            "500mb Vorticity, Wind, and Height",
            "700mb Relative Humidity, Wind, and Height",
            "850mb Temperature, Wind, and Height"
    )

    val paramsGefsSpag = listOf(
            "200_1176_ht",
            "200_1188_ht",
            "200_1200_ht",
            "200_1212_ht",
            "200_1224_ht",
            "200_1230_ht",
            "500_510_552_ht",
            "500_516_558_ht",
            "500_522_564_ht",
            "500_528_570_ht",
            "500_534_576_ht",
            "500_540_582_ht",
            "mslp_1000_1040_iso",
            "mslp_1004_1044_iso",
            "mslp_1008_1048_iso",
            "mslp_1012_1052_iso",
            "mslp_984_1024_iso",
            "mslp_996_1036_iso"
    )

    val labelsGefsSpag = listOf(
            "200mb 1176 Height Contours",
            "200mb 1188 Height Contours",
            "200mb 1200 Height Contours",
            "200mb 1212 Height Contours",
            "200mb 1224 Height Contours",
            "200mb 1230 Height Contours",
            "500mb 510/552 Height Contours",
            "500mb 516/558 Height Contours",
            "500mb 522/564 Height Contours",
            "500mb 528/570 Height Contours",
            "500mb 534/576 Height Contours",
            "500mb 540/582 Height Contours",
            "MSLP 1000/1040 Isobar Contour",
            "MSLP 1004/1044 Isobar Contours",
            "MSLP 1008/1048 Isobar Contours",
            "MSLP 1012/1052 Isobar Contours",
            "MSLP 984/1024 Isobar Contours",
            "MSLP 996/1036 Isobar Contours"
    )

    val paramsGefsMnsprd = listOf(
            "dom_precip_type",
            "precip_p06",
            "precip_p24",
            "precip_ptot",
            "prob_ice_0.25in",
            "prob_precip_1in",
            "prob_precip_0.25in",
            "prob_precip_0.5in",
            "snodpth_chng_mean",
            "snodpth_chng_sprd",
            "10m_wnd",
            "2m_temp",
            "cape",
            "mslp",
            "prob_cape_2000",
            "prob_cape_250",
            "prob_cape_4000",
            "prob_cape_500",
            "250_temp",
            "250_wnd",
            "500_temp",
            "500_vort_ht",
            "500_wnd",
            "700_temp",
            "700_vort_ht",
            "700_wnd",
            "850_temp",
            "850_vort_ht",
            "850_wnd",
            "925_wnd"
    )

    val labelsGefsMnsprd = listOf(
            "Dominant Precipitation Type",
            "Mean 6-hour Precipitation",
            "Mean 24-hour Precipitation",
            "Total Accumulated Precipitation of Period",
            "Probability of Ice > 0.25 (in)",
            "Probability of 6-hrly Precipitation > 1.00 (in)",
            "Probability of 6-hrly Precipitation > 0.25 (in)",
            "Probability of 6-hrly Precipitation > 0.50 (in)",
            "Mean of Snow Depth Change for previous 006 hours",
            "Spread of Snow Depth Change for previous 006 hours",
            "10m Winds",
            "2 meter Temperature",
            "Mean Convective Available Potential Energy",
            "Mean Sea Level Pressure",
            "Probability of CAPE > 2000",
            "Probability of CAPE > 250",
            "Probability of CAPE > 4000",
            "Probability of CAPE > 500",
            "250mb Temperature",
            "250mb Winds",
            "500mb Temperature",
            "500mb Vorticity and Height",
            "500mb Winds",
            "700mb Temperature",
            "700mb Vorticity and Height",
            "700mb Winds",
            "850mb Temperature",
            "850mb Vorticity and Height",
            "850mb Winds",
            "925mb Winds"
    )

    val paramsWw3 = listOf(
            "peak_dir_per",
            "sig_wv_ht",
            "wnd_wv_dir_per"
    )

    val labelsWw3 = listOf(
            "Peak Wave Direction and Period (sec)",
            "Significant Wave Height and Wind",
            "Wind Wave Direction and Period (sec)"
    )

    val paramsWw3Enp = listOf(
            "regional_pk_dir_per",
            "regional_wv_dir_per",
            "regional_wv_ht"
    )

    val labelsWw3Enp = listOf(
            "Regional WW3 Model Peak Wave Direction and Period",
            "Regional WW3 Model Wind Wave Direction and Period",
            "Regional WW3 Model Sig Wave Height and Wind"
    )

    val paramsWw3Wna = listOf(
            "regional_pk_dir_per",
            "regional_wv_dir_per",
            "regional_wv_ht"
    )

    val labelsWw3Wna = listOf(
            "Regional WW3 Model Peak Wave Direction and Period",
            "Regional WW3 Model Wind Wave Direction and Period",
            "Regional WW3 Model Sig Wave Height and Wind"
    )

    val paramsEstofs = listOf(
            "storm_surge",
            "total_water_level"
    )

    val labelsEstofs = listOf(
            "Storm surge relative to Mean Sea Level (feet)",
            "Total Water Level relative to Mean Sea Level (feet)"
    )

    val paramsFirefx = listOf(
            "precip_p01",
            "precip_p12",
            "precip_p24",
            "precip_p36",
            "precip_pwat",
            "dom_precip_type",
            "snodpth_chng",
            "2m_tmpc",
            "2m_dwpc",
            "2m_rh_10m_wnd",
            "10m_maxwnd",
            "sim_radar_1km",
            "haines",
            "vent_rate",
            "850_temp_ht",
            "max_downdraft",
            "max_updraft",
            "max_updraft_hlcy",
            "best_cape",
            "pbl_rich_height",
            "pbl_height",
            "transport_wind"
    )

    val labelsFirefx = listOf(
            "Sea-level Pressure, 1-hr Accumulated Precip",
            "12-H Accumulated Precipitation",
            "24-H Accumulated Precipitation",
            "36-H Accumulated Precipitation",
            "Total Column Precipitable Water",
            "Categorical Precipitation Type",
            "Snow Depth Change from F00",
            "Shelter (2-m) Temperature",
            "Shelter (2-m) Dew Point Temperature",
            "1-hr Minimum Relative Humidity, 10-m Wind",
            "Maximum 1-hr 10-m Wind",
            "1000 m AGL Radar Reflectivity",
            "Haines Index",
            "Ventilation Rate",
            "850mb Temperature, Wind and Height",
            "Maximum 1-hr Downdraft Vertical Velocity",
            "Maximum 1-hr Updraft Vertical Velocity",
            "Maximum 2-5 km Updraft Helicity",
            "Best CAPE",
            "PBL Height (Based on Richardson Number)",
            "PBL Height",
            "Transport Wind and Terrain Height"
    )

    val paramsHrwArw2 = listOf(
            "precip_p01",
            "precip_p03",
            "precip_p06",
            "precip_p12",
            "precip_p24",
            "precip_p36",
            "precip_p48",
            "precip_ptot",
            "sim_radar_1km",
            "1000_500_thick",
            "1000_850_thick",
            "850_700_thick",
            "10m_wnd",
            "10m_wnd_precip",
            "10m_wnd_sfc_gust",
            "2m_dewp_10m_wnd",
            "2m_temp_10m_wnd",
            "best_cape_cin",
            "sfc_cape_cin",
            "helicity_1km",
            "helicity_3km",
            "max_updraft_hlcy",
            "echo_top",
            "ceiling",
            "vis",
            "250_wnd_ht",
            "300_wnd_ht",
            "500_vort_ht",
            "700_rh_ht",
            "850_temp_ht"
    )

    val labelsHrwArw2 = listOf(
            "Total Precipitation every 1 hour",
            "Total Precipitation every 3 hours",
            "Total Precipitation every 6 hours",
            "Total Precipitation every 12 hours",
            "Total Precipitation every 24 hours",
            "Total Precipitation every 36 hours",
            "Total Precipitation every 48 hours",
            "Total Accumulated Precipitation",
            "Simulated Radar Reflectivity 1km",
            "MSLP, 1000-500mb thickness, 3-hourly total precipitation",
            "MSLP, 1000-850mb thickness, 3-hourly total precipitation",
            "MSLP, 850-700mb thickness, 3-hourly total precipitation",
            "10 meter Wind",
            "MSLP, 10m wind, 3-hourly total precip, 2m temperature",
            "10 meter wind gust",
            "2 meter Dew Point and 10 meter wind",
            "2 meter Temperature and 10 meter Wind",
            "Most Unstable Convective Available Potential Energy and Convective Inhibition",
            "Surface-Based Convective Available Potential Energy and Convective Inhibition",
            "0-1km Helicity and Storm Motion",
            "0-3km Helicity and Storm Motion",
            "Max 2-5km Updraft Helicity ",
            "Echo Tops",
            "Cloud Ceiling ",
            "Visibility",
            "250mb Wind and Height",
            "300mb Wind and Height",
            "500mb Vorticity, Wind, and Height",
            "700mb Relative Humidity, Wind, and Height",
            "850mb Temperature, Wind, and Height"
    )

    val paramsHref= listOf(
            "mean_precip_p01",
            "mean_precip_p03",
            "mean_precip_ptot",
            "blend_mean_precip_p01",
            "blend_mean_precip_p03",
            "blend_mean_precip_ptot",
            "pmm_refd_1km",
            "pmm_refd_max",
            "prob_refd_40dbz",
            "prob_refd_max_40dbz",
            "prob_cref_50dbz",
            "prob_rain",
            "prob_snow",
            "prob_sleet",
            "prob_freezing_rain",
            "prob_3h_rain_0.5in",
            "prob_3h_rain_1in",
            "mean_snow_1h",
            "mean_snow_3h",
            "mean_snow_total",
            "prob_1h_snow_1in",
            "prob_3h_snow_1in",
            "prob_3h_snow_3in",
            "mean_pwat",
            "prob_pwat_1.5in",
            "prob_pwat_2in",
            "mean_2m_temp",
            "prob_2m_temp_0C",
            "mean_2m_dewp",
            "prob_2m_dewp_55F",
            "prob_2m_dewp_65F",
            "prob_10m_wspd_20kt",
            "prob_10m_wspd_30kt",
            "mean_vis",
            "prob_vis_0.5mi",
            "prob_etop_30000ft",
            "prob_etop_35000ft",
            "prob_ceil_1000ft",
            "prob_ceil_2000ft",
            "prob_ceil_3000ft",
            "prob_low_IFR",
            "prob_IFR",
            "prob_marginal_VFR",
            "prob_VFR",
            "mean_ml_cape",
            "prob_cape_500",
            "prob_cape_1000",
            "prob_cape_2000",
            "prob_cape_3000",
            "mean_vwshr",
            "prob_vwshr_30kt",
            "prob_max_hlcy_25",
            "prob_max_hlcy_100"
    )

    val labelsHref = listOf(
            "1h mean precip",
            "3h mean precip",
            "Accumulated mean precip",
            "1h blend mean precip",
            "3h blend mean precip",
            "Accumulated blend mean precip",
            "PMM REFD 1 KM",
            "PMM COL MAX REFD",
            "Probability of 1km REFD greater than 40dBZ",
            "Probability of 1km MAX REFD greater than 40dBZ",
            "Probability of COMP MAX REFC greater than 50dBZ",
            "Probability of rain",
            "Probability of snow",
            "Probability of sleet",
            "Probability of freezing rain ",
            "Probability of rain greater than 0.5in in 3h",
            "Probability of rain greater than 1in in 3hr",
            "1-hr mean snow ",
            "3-hr mean snow ",
            "Mean snow total",
            "Probability of snow greater than 1in in 1h",
            "Probability of snow greater than 1in in 3h",
            "Probability of snow greater than 3in in 3hr",
            "Mean precipitable water ",
            "Probability of PWAT greater than 1.5in",
            "Probability of PWAT greater than 2.0in",
            "Mean 2m temp",
            "Probability of 2m temp less than 0C ",
            "Mean 2m dewpoint temp",
            "Probability of 2m dewpoint temp greater than 55F ",
            "Probability of 2m dewpoint temp greater than 65F ",
            "Probability of 10m wind speed greater than 20kt",
            "Probability of 10m wind speed greater than 30kt",
            "Mean visibility ",
            "Probability of visibility less than 0.5mile",
            "Probability of etop greater than 30000ft",
            "Probability of etop greater than 35000ft",
            "Probability of ceil hght less than 1000ft",
            "Probability of ceil hght less than 2000ft",
            "Probability of ceil hght less than 3000ft",
            "Probability of low instrument flt rule",
            "Probability of instrument flt rule",
            "Probability of marginal visual flt rule",
            "Probability of visual flt rule",
            "Mean mixed layer CAPE ",
            "Probability of mixed layer CAPE greater than 500J/kg",
            "Probability of mixed layer CAPE greater than 1000J/kg",
            "Probability of mixed layer CAPE greater than 2000J/kg",
            "Probability of mixed layer CAPE greater than 3000J/kg",
            "Mean vertical wind shear ",
            "Probability of vertical wind shear greater than 30kts",
            "Probability of max updraft helicity greater than 25m**2/s**2",
            "Probability of max updraft helicity greater than 100m**2/s**2"
    )

    val paramsNbm = listOf(
            "precip_p06",
            "precip_ptot",
            "2m_temp_10m_wnd",
            "2m_dewp_10m_wnd",
            "2m_relh_10m_wnd",
            "2m_apparent_temp",
            "2m_min_temp",
            "2m_max_temp",
            "10m_wnd_gust",
            "total_cloud_cover"
    )

    val labelsNbm = listOf(
            "Total precipitation every 6 hours",
            "Accumulated precip",
            "2 meter Temperature and 10 meter Wind",
            "2 meter dew point temp and 10 meter wind",
            "2 meter Relative Humidity and 10 meter Wind",
            "2 meter Apparent Temperature and 10 meter Wind",
            "2 meter minimum Temperature",
            "2 meter maximum temperature",
            "10 meter wind and gust",
            "Total Cloud Cover"
    )

    // grep title /tmp/a | egrep -o ">.*</a>" | sed 's/>/\"/' | sed 's/<\/a>/\"\,/'
    // grep title /tmp/a | egrep -o "title=.*\"" | sed 's/title=//' | sed 's/$/\"\,/'| awk -F'"' '{print $2}' | sed 's/$/\"\,/' | sed 's/^/\"/'

}

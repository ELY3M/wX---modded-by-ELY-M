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

package joshuatee.wx

val NWS_TXT_ARR: List<String> = listOf(
        "pmdspd: Short Range Forecast Discussion",
        "pmdepd: Extended Forecast Discussion",
        "qpfpfd: QPF Discussion",
        "qpferd: Excessive Rainfall Discussion",
        "qpfhsd: Heavy Snow and Icing Discussion",
        "pmdhmd: Model Diagnostics Discussion",
        "pmdhi: Hawaii Extended Forecast Discussion",
        "pmdak: Alaska Extended Forecast Discussion",
        "pmdsa: South American Synoptic Discussion",
        "pmdca: Tropical Discussion",
        "sccns1: Storm Summary 1",
        "sccns2: Storm Summary 2",
        "sccns3: Storm Summary 3",
        "sccns4: Storm Summary 4",
        "sccns5: Storm Summary 5",
        "pmdthr: CPC US Hazards Outlook",
        "uvicac: NOAA/EPA Ultraviolet Index /UVI/ Forecast",
        "tptwrn: Hourly temp/wx for Western US",
        "tptcrn: Hourly temp/wx for Central US",
        "tptern: Hourly temp/wx for Eastern US",
        "tptnam: Hourly temp/wx for NA Cities",
        "rwrmx: Latin America and Caribbean Regional Weather Roundup",
        "tptcan: Canadian temp and precip Table",
        "tptint: Foreign temp and weather table",
        "tptlat: Latin American temp and weather table",
        "miahsfat2: High Seas Forecasts - Atlantic",
        "miahsfep2: High Seas Forecasts - NE Pacific",
        "miahsfep3: High Seas Forecasts - SE Pacific",
        "nfdhsfat1: High Seas Forecasts - N Atlantic",
        "nfdhsfep1: High Seas Forecasts - N Pacific",
        "nfdhsfepi: High Seas Forecasts - E and C N Pacific",
        "mimpac: Marine Weather disc for N PAC Ocean",
        "offn09: Marine fsct for WA and ORE Waters",
        "offn08: Marine fcst for N CA Waters",
        "offn07: Marine fcst for S CA Waters",
        "offpz5: Offshore Waters fsct - PAC 1",
        "offpz6: Offshore Waters fsct - PAC 2",
        "nfdoffn35: (VOBRA) for Offshore Waters - WA/OR",
        "nfdoffn36: (VOBRA) for Offshore Waters - CA",
        "mimatn: Marine disc for N Atlantic Ocean",
        "offn01: Navtex Marine fcst for NE US Waters",
        "offn02: Navtex Marine fcst for Atlantic States Waters",
        "offn03: Navtex Marine fcst for SE US Waters",
        "offn04: Navtex Marine fcst for SE Gulf Of Mexico",
        "offn05: Navtex Marine fcst for San Jaun Atlantic Waters",
        "offn06: Navtex Marine fcst for NW Gulf Of Mexico",
        "offnt1: Offshore Waters fsct - ATL 1",
        "offnt2: Offshore Waters fsct - ATL 2",
        "offnt3: Offshore Waters fsct - Caribbean & SW North Atlantic",
        "offnt4: Offshore Waters fsct - Gulf of Mexico",
        "nfdoffn31: (VOBRA) for Offshore Waters - New England",
        "nfdoffn32: (VOBRA) for Offshore Waters - West Central North Atlantic",
        "offajk: Offshore Waters fsct - Eastern Gulf of Alaska",
        "offaer: Offshore Waters fsct - Western Gulf of Alaska",
        "offalu: Offshore Waters fsct - Bering Sea",
        "offafg: Offshore Waters fsct - US Artic Waters",
        "offhfo: Offshore Waters fsct - Hawaii",
        "offn10: Navtex Marine fcst for Hawaii",
        "offn11: Navtex Marine fcst for Kodiak, AK (SE)",
        "offn12: Navtex Marine fcst for Kodiak, AK (N Gulf)",
        "offn13: Navtex Marine fcst for Kodiak, AK (West)",
        "offn14: Navtex Marine fcst for Kodiak, AK (NW)",
        "offn15: Navtex Marine fcst for Kodiak, AK(Arctic)",
        "pmdmrd: Prognostic disc for 6-10 and 8-14 Day Outlooks",
        "pmd30d: Prognostic disc for Monthly Outlook",
        "pmd90d: Prognostic disc for long-lead Seasonal Outlooks",
        "pmdhco: Prognostic disc for long-lead Hawaiian Outlooks",
        "swomcd: Most recent MCD",
        "swody1: Day 1 Convective Outlook",
        "swody2: Day 2 Convective Outlook",
        "swody3: Day 3 Convective Outlook",
        "swod48: Day 4-8 Convective Outlook",
        "fwddy1: Day 1 Fire Weather Outlook",
        "fwddy2: Day 2 Fire Weather Outlook",
        "fwddy38: Days 3-8 Fire Weather Outlook",
        "focn45: Significant Weather Discussion, PASPC",
        "fxcn01: FXCN01",
        "awcn11: Weather Summary S. Manitoba",
        "awcn12: Weather Summary N. Manitoba",
        "awcn13: Weather Summary S. Saskatchewan",
        "awcn14: Weather Summary N. Saskatchewan",
        "awcn15: Weather Summary S. Alberta",
        "awcn16: Weather Summary N. Alberta",
        "miatwoat: ATL Tropical Weather Outlook",
        "miatwdat: ATL Tropical Weather Discussion",
        "miatwoep: EPAC Tropical Weather Outlook",
        "miatwdep: EPAC Tropical Weather Discussion",
        "miatwsat: ATL Monthly Tropical Summary",
        "miatwsep: EPAC Monthly Tropical Summary",
        "GLFLM: Lake Michigan - Open Lake Forecast",
        "GLFLS: Lake Superior - Open Lake Forecast",
        "GLFLH: Lake Huron - Open Lake Forecast",
        "GLFSC: Lake St Clair - Open Lake Forecast",
        "GLFLE: Lake Erie - Open Lake Forecast",
        "GLFLO: Lake Ontario - Open Lake Forecast",
        "GLFSL: Saint Lawrence River",
        "swpc3day: NOAA Geomagnetic Activity Observation and Forecast",
        "swpc3daygeo: NOAA Geomagnetic Activity Probabilities",
        "swpchigh: Weekly Highlights and Forecasts",
        "swpc27day: 27-day Space Weather Outlook Table",
        "swpcdisc: Forecast Discussion",
        "swpcwwa: Advisory Outlook"
)

val NWS_IMG_ARR: List<String> = listOf(
        "GOES16: GOES16 product last viewed",
        "VIS_CONUS: Visible CONUS",
        "CONUSWV: US Water Vapor",
        "SND: Sounding/Hodograph",
        "FMAP: Forecast map",
        "FMAP12: Forecast map - 12hr",
        "FMAP24: Forecast map - 24hr",
        "FMAP36: Forecast map - 36hr",
        "FMAP48: Forecast map - 48hr",
        "FMAP3D: Forecast map - 3day",
        "FMAP4D: Forecast map - 4day",
        "FMAP5D: Forecast map - 5day",
        "FMAP6D: Forecast map - 6day",
        "LTG: Lightning map",
        "SWOD1: SPC Convective Outlook Day 1",
        "SWOD2: SPC Convective Outlook Day 2",
        "SWOD3: SPC Convective Outlook Day 3",
        "STRPT: SPC Storm reports for today",
        "QPF1: QPF Day 1",
        "QPF2: QPF Day 2",
        "QPF3: QPF Day 3",
        "QPF1-2: QPF Day 1-2",
        "QPF1-3: QPF Day 1-3",
        "QPF4-5: QPF Day 4-5",
        "QPF6-7: QPF Day 6-7",
        "QPF1-5: QPF 5 Day Total",
        "QPF1-7: QPF 7 Day Total",
        "SPCMESO1: Favorite #1 (500mb)",
        "SPCMESO2: Favorite #2 (pmsl)",
        "SPCMESO3: Favorite #3 (ttd)",
        "SPCMESO4: Favorite #4 (rgnlrad)",
        "SPCMESO5: Favorite #5 (lllr)",
        "SPCMESO6: Favorite #6 (laps)"
)

val TDWR_RIDS: List<String> = listOf(
        "TPHX AZ, Phoenix",
        "TDEN CO, Denver",
        "TFLL FL, Fort Lauderdale",
        "TMIA FL, Miami",
        "TMCO FL, Orlando International",
        "TTPA FL, Tampa Bay",
        "TPBI FL, West Palm Beach",
        "TATL GA, Atlanta",
        "TMDW IL, Chicago Midway",
        "TORD IL, Chicago O'Hare",
        "TIDS IN, Indianapolis",
        "TICT KS, Wichita",
        "TSDF KY, Louisville",
        "TMSY LA, New Orleans",
        "TBOS MA, Boston",
        "TBWI MD, Baltimore/Wash",
        "TDCA MD, Washington National",
        "TDTW MI, Detroit",
        "TMSP MN, Minneapolis",
        "TMCI MO, Kansas City",
        "TSTL MO, St Louis",
        "TCLT NC, Charlotte",
        "TRDU NC, Raleigh Durham",
        "TEWR NJ, Newark",
        "TLAS NV, Las Vegas",
        "TJFK NY, New York City",
        "TLVE OH, Cleveland",
        "TCMH OH, Columbus",
        "TCVG OH, Covington",
        "TDAY OH, Dayton",
        "TOKC OK, Oklahoma City",
        "TTUL OK, Tulsa",
        "TPHL PA, Philadelphia",
        "TPIT PA, Pittsburgh",
        "TSJU PR, San Juan",
        "TMEM TN, Memphis",
        "TBNA TN, Nashville",
        "TDFW TX, Dallas/Ft. Worth",
        "TDAL TX, Dallas Love Field",
        "THOU TX, Houston Hobby",
        "TIAH TX, Houston International",
        "TSLC UT, Salt Lake City",
        "TIAD VA, Dulles",
        "TMKE WI, Milwaukee",
        "TJRV: PR1,",
        "TJBQ: PR2,"
)

val SND_ARR: List<String> = listOf(
        "ABR",
        "ALB",
        "ABQ",
        "AMA",
        "BIS",
        "RNK",
        "BOI",
        "BRO",
        "BUF",
        "CAR",
        "ILX",
        "CHS",
        "MPX",
        "CHH",
        "CRP",
        "DRT",
        "DNR",
        "DDC",
        "LKN",
        "EPZ",
        "FWD",
        "APX",
        "GGW",
        "GJT",
        "GYX",
        "TFX",
        "GRB",
        "GSO",
        "INL",
        "JAN",
        "JAX",
        "LCH",
        "LZK",
        "MFR",
        "MAF",
        "BNA",
        "MHX",
        "OUN",
        "LBF",
        "OAK",
        "FFC",
        "PIT",
        "DVN",
        "UIL",
        "UNR",
        "REV",
        "RIW",
        "SLE",
        "SLC",
        "NKX",
        "BMX",
        "SHV",
        "OTX",
        "SGF",
        "LWX",
        "TLH",
        "TBW",
        "TOP",
        "TUS",
        "OKX",
        "OAX",
        "WAL",
        "DTX",
        "ILN",
        "VBG",
        "1Y7",
        "76405",
        "76458"
)

val WFO_ARR: List<String> = listOf(
        "AFC: AK, Anchorage",
        "AFG: AK, Fairbanks",
        "AJK: AK, Juneau",
        "BMX: AL, Birmingham",
        "HUN: AL, Huntsville",
        "MOB: AL, Mobile",
        "LZK: AR, Little Rock",
        "FGZ: AZ, Flagstaff",
        "PSR: AZ, Phoenix",
        "TWC: AZ, Tucson",
        "EKA: CA, Eureka",
        "HNX: CA, Hanford",
        "LOX: CA, Los Angeles",
        "MTR: CA, San Francisco",
        "SGX: CA, San Diego",
        "STO: CA, Sacramento",
        "BOU: CO, Denver/Boulder",
        "GJT: CO, Grand Junction",
        "PUB: CO, Pueblo",
        "JAX: FL, Jacksonville",
        "KEY: FL, Key West",
        "MFL: FL, Miami",
        "MLB: FL, Melbourne",
        "TAE: FL, Tallahassee",
        "TBW: FL, Tampa Bay Area",
        "FFC: GA, Atlanta",
        "GUM: GU, Guam",
        "HFO: HI, Honolulu",
        "DMX: IA, Des Moines",
        "DVN: IA, Quad Cities",
        "BOI: ID, Boise",
        "PIH: ID, Pocatello",
        "ILX: IL, Lincoln",
        "LOT: IL, Chicago",
        "IND: IN, Indianapolis",
        "IWX: IN, Nrn. Indiana",
        "DDC: KS, Dodge City",
        "GLD: KS, Goodland",
        "ICT: KS, Wichita",
        "TOP: KS, Topeka",
        "JKL: KY, Jackson",
        "LMK: KY, Louisville",
        "PAH: KY, Paducah",
        "LCH: LA, Lake Charles",
        "LIX: LA, New Orleans",
        "SHV: LA, Shreveport",
        "BOX: MA, Boston",
        "CAR: ME, Caribou",
        "GYX: ME, Gray",
        "APX: MI, Gaylord",
        "DTX: MI, Detroit",
        "GRR: MI, Grand Rapids",
        "MQT: MI, Marquette",
        "DLH: MN, Duluth",
        "MPX: MN, Twin Cities",
        "EAX: MO, Kansas City",
        "LSX: MO, St. Louis",
        "SGF: MO, Springfield",
        "JAN: MS, Jackson",
        "BYZ: MT, Billings",
        "GGW: MT, Glasgow",
        "MSO: MT, Missoula",
        "TFX: MT, Great Falls",
        "ILM: NC, Wilmington",
        "MHX: NC, Morehead City",
        "RAH: NC, Raleigh",
        "BIS: ND, Bismarck",
        "FGF: ND, Grand Forks",
        "GID: NE, Hastings",
        "LBF: NE, North Platte",
        "OAX: NE, Omaha",
        "PHI: NJ, Mount Holly",
        "ABQ: NM, Albuquerque",
        "LKN: NV, Elko",
        "REV: NV, Reno",
        "VEF: NV, Las Vegas",
        "ALY: NY, Albany",
        "BGM: NY, Binghamton",
        "BUF: NY, Buffalo",
        "OKX: NY, New York City",
        "CLE: OH, Cleveland",
        "ILN: OH, Wilmington",
        "OUN: OK, Norman",
        "TSA: OK, Tulsa",
        "MFR: OR, Medford",
        "PDT: OR, Pendleton",
        "PQR: OR, Portland",
        "CTP: PA, State College",
        "PBZ: PA, Pittsburgh",
        "SJU: PR, San Juan",
        "CAE: SC, Columbia",
        "CHS: SC, Charleston",
        "GSP: SC, Greer",
        "ABR: SD, Aberdeen",
        "FSD: SD, Sioux Falls",
        "UNR: SD, Rapid City",
        "MEG: TN, Memphis",
        "MRX: TN, Morristown",
        "OHX: TN, Nashville",
        "AMA: TX, Amarillo",
        "BRO: TX, Brownsville",
        "CRP: TX, Corpus Christi",
        "EPZ: TX, El Paso",
        "EWX: TX, Austin/San Antonio",
        "FWD: TX, Dallas/Fort Worth",
        "HGX: TX, Houston",
        "LUB: TX, Lubbock",
        "MAF: TX, Midland/Odessa",
        "SJT: TX, San Angelo",
        "SLC: UT, Salt Lake City",
        "AKQ: VA, Wakefield",
        "LWX: VA, Sterling",
        "RNK: VA, Blacksburg",
        "BTV: VT, Burlington",
        "OTX: WA, Spokane",
        "SEW: WA, Seattle",
        "ARX: WI, La Crosse",
        "GRB: WI, Green Bay",
        "MKX: WI, Milwaukee",
        "RLX: WV, Charleston",
        "CYS: WY, Cheyenne",
        "RIW: WY, Riverton"
)

val RID_ARR: List<String> = listOf(
        "ABC: AK, Bethel",
        "APD: AK, Fairbanks/Pedro Dome",
        "AHG: AK, Kenai",
        "AKC: AK, King Salmon",
        "AIH: AK, Middleton Island",
        "AEC: AK, Nome",
        "ACG: AK, Sitka/Biorka Island",
        "BMX: AL, Birmingham",
        "EOX: AL, Fort Rucker",
        "HTX: AL, Huntsville",
        "MXX: AL, Maxwell AFB",
        "MOB: AL, Mobile",
        "SRX: AR, Fort Smith",
        "LZK: AR, Little Rock",
        "FSX: AZ, Flagstaff",
        "IWA: AZ, Phoenix",
        "EMX: AZ, Tucson",
        "YUX: AZ, Yuma",
        "BBX: CA, Beale AFB",
        "EYX: CA, Edwards AFB",
        "BHX: CA, Eureka",
        "VTX: CA, Los Angeles",
        "DAX: CA, Sacramento",
        "NKX: CA, San Diego",
        "MUX: CA, San Francisco",
        "HNX: CA, San Joaquin Valley",
        "SOX: CA, Santa Ana Mountains",
        "VBX: CA, Vandenberg AFB",
        "FTG: CO, Denver",
        "GJX: CO, Grand Junction",
        "PUX: CO, Pueblo",
        "DOX: DE, Dover AFB",
        "EVX: FL, Eglin AFB",
        "JAX: FL, Jacksonville",
        "BYX: FL, Key West",
        "MLB: FL, Melbourne",
        "AMX: FL, Miami",
        "TLH: FL, Tallahassee",
        "TBW: FL, Tampa",
        "FFC: GA, Atlanta",
        "VAX: GA, Moody AFB",
        "JGX: GA, Robins AFB",
        "GUA: GU, Andersen AFB",
        "HKI: HI, Kauai",
        "HKM: HI, Kohala",
        "HMO: HI, Molokai",
        "HWA: HI, South Shore",
        "DVN: IA, Davenport",
        "DMX: IA, Des Moines",
        "CBX: ID, Boise",
        "SFX: ID, Pocatello/Idaho Falls",
        "LOT: IL, Chicago",
        "ILX: IL, Lincoln",
        "VWX: IN, Evansville",
        "IND: IN, Indianapolis",
        "IWX: IN, North Webster",
        "DDC: KS, Dodge City",
        "GLD: KS, Goodland",
        "TWX: KS, Topeka",
        "ICT: KS, Wichita",
        "HPX: KY, Fort Campbell",
        "JKL: KY, Jackson",
        "LVX: KY, Louisville",
        "PAH: KY, Paducah",
        "POE: LA, Fort Polk",
        "LCH: LA, Lake Charles",
        "LIX: LA, New Orleans",
        "SHV: LA, Shreveport",
        "BOX: MA, Boston",
        "CBW: ME, Loring AFB",
        "GYX: ME, Portland",
        "DTX: MI, Detroit/Pontiac",
        "APX: MI, Gaylord",
        "GRR: MI, Grand Rapids",
        "MQT: MI, Marquette",
        "DLH: MN, Duluth",
        "MPX: MN, Minneapolis/St. Paul",
        "EAX: MO, Kansas City",
        "SGF: MO, Springfield",
        "LSX: MO, St. Louis",
        "DGX: MS, Brandon/Jackson",
        "GWX: MS, Columbus AFB",
        "BLX: MT, Billings",
        "GGW: MT, Glasgow",
        "TFX: MT, Great Falls",
        "MSX: MT, Missoula",
        "KSG: NA, Camp Humphreys, South Korea",
        "ODN: NA, Kadena Air Base, Japan",
        "KJK: NA, Kunsan Air Base, South Korea",
        "PLA: NA, Lajes Field, Azores",
        "MHX: NC, Morehead City",
        "RAX: NC, Raleigh/Durham",
        "LTX: NC, Wilmington",
        "BIS: ND, Bismarck",
        "MVX: ND, Grand Forks (Mayville)",
        "MBX: ND, Minot AFB",
        "UEX: NE, Grand Island/Hastings",
        "LNX: NE, North Platte",
        "OAX: NE, Omaha",
        "ABX: NM, Albuquerque",
        "FDX: NM, Cannon AFB",
        "HDX: NM, Holloman AFB",
        "LRX: NV, Elko",
        "ESX: NV, Las Vegas",
        "RGX: NV, Reno",
        "ENX: NY, Albany",
        "BGM: NY, Binghamton",
        "BUF: NY, Buffalo",
        "TYX: NY, Montague",
        "OKX: NY, New York City",
        "CLE: OH, Cleveland",
        "ILN: OH, Wilmington",
        "FDR: OK, Frederick",
        "TLX: OK, Oklahoma City",
        "INX: OK, Tulsa",
        "VNX: OK, Vance AFB",
        "MAX: OR, Medford",
        "PDT: OR, Pendleton",
        "RTX: OR, Portland",
        "DIX: PA, Philadelphia",
        "PBZ: PA, Pittsburgh",
        "CCX: PA, State College",
        "JUA: PR, San Juan",
        "CLX: SC, Charleston",
        "CAE: SC, Columbia",
        "GSP: SC, Greer",
        "ABR: SD, Aberdeen",
        "UDX: SD, Rapid City",
        "FSD: SD, Sioux Falls",
        "MRX: TN, Knoxville/Tri Cities",
        "NQA: TN, Memphis",
        "OHX: TN, Nashville",
        "AMA: TX, Amarillo",
        "EWX: TX, Austin/San Antonio",
        "BRO: TX, Brownsville",
        "CRP: TX, Corpus Christi",
        "FWS: TX, Dallas/Ft. Worth",
        "DYX: TX, Dyess AFB",
        "EPZ: TX, El Paso",
        "GRK: TX, Fort Hood",
        "HGX: TX, Houston/Galveston",
        "DFX: TX, Laughlin AFB",
        "LBB: TX, Lubbock",
        "MAF: TX, Midland/Odessa",
        "SJT: TX, San Angelo",
        "ICX: UT, Cedar City",
        "MTX: UT, Salt Lake City",
        "AKQ: VA, Norfolk/Richmond",
        "FCX: VA, Roanoke",
        "LWX: VA, Sterling",
        "CXX: VT, Burlington",
        "LGX: WA, Langley Hill",
        "ATX: WA, Seattle/Tacoma",
        "OTX: WA, Spokane",
        "GRB: WI, Green Bay",
        "ARX: WI, La Crosse",
        "MKX: WI, Milwaukee",
        "RLX: WV, Charleston",
        "CYS: WY, Cheyenne",
        "RIW: WY, Riverton"
)

val CA_RID_ARR: List<String> = listOf(
        "WUJ: Aldergrove, BC (near Vancouver)",
        "XPG: Prince George, BC",
        "XSS: Silver Star Mountain, BC (near Vernon)",
        "XSI: Victoria, BC",
        "XBE: Bethune, SK (near Regina)",
        "WHK: Carvel, AB (near Edmonton)",
        "XFW: Foxwarren, MB (near Brandon)",
        "XRA: Radisson, SK (near Saskatoon)",
        "XBU: Schuler, AB (near Medicine Hat)",
        "WWW: Spirit River, AB (near Grande Prairie)",
        "XSM: Strathmore, AB (near Calgary)",
        "XWL: Woodlands, MB (near Winnipeg)",
        "WHN: Jimmy Lake, AB (near Cold Lake)",
        "WBI: Britt, ON (near Sudbury)",
        "XDR: Dryden, ON",
        "WSO: Exeter, ON (near London)",
        "XFT: Franktown, ON (near Ottawa)",
        "WKR: King City, ON (near Toronto)",
        "WGJ: Montreal River, ON (near Sault Ste Marie)",
        "XTI: Northeast Ontario, ON (near Timmins)",
        "XNI: Superior West, ON (near Thunder Bay)",
        "XLA: Landrienne, QC (near Rouyn-Noranda)",
        "WMN: McGill, QC (near Montréal)",
        "XAM: Val d'Irène, QC (near Mont Joli)",
        "WVY: Villeroy, QC (near Trois-Rivières)",
        "WMB: Lac Castor, QC (near Saguenay)",
        "XNC: Chipman, NB (near Fredericton)",
        "XGO: Halifax, NS",
        "WTP: Holyrood, NL (near St. John's)",
        "XME: Marble Mountain, NL",
        "XMB: Marion Bridge, NS (near Sydney)",
        "CAN",
        "PAC",
        "WRN",
        "ONT",
        "QUE",
        "ERN"
)

val STATE_ARR: List<String> = listOf(
        "AL: Alabama",
        "AK: Alaska",
        "AZ: Arizona",
        "AR: Arkansas",
        "CA: California",
        "CO: Colorado",
        "CT: Connecticut",
        "DE: Delaware",
        "FL: Florida",
        "GA: Georgia",
        "HI: Hawaii",
        "ID: Idaho",
        "IL: Illinois",
        "IN: Indiana",
        "IA: Iowa",
        "KS: Kansas",
        "KY: Kentucky",
        "LA: Louisiana",
        "ME: Maine",
        "MD: Maryland",
        "MA: Massachusetts",
        "MI: Michigan",
        "MN: Minnesota",
        "MS: Mississippi",
        "MO: Missouri",
        "MT: Montana",
        "NE: Nebraska",
        "NV: Nevada",
        "NH: New Hampshire",
        "NJ: New Jersey",
        "NM: New Mexico",
        "NY: New York",
        "NC: North Carolina",
        "ND: North Dakota",
        "OH: Ohio",
        "OK: Oklahoma",
        "OR: Oregon",
        "PA: Pennsylvania",
        "RI: Rhode Island",
        "SC: South Carolina",
        "SD: South Dakota",
        "TN: Tennessee",
        "TX: Texas",
        "UT: Utah",
        "VT: Vermont",
        "VA: Virginia",
        "WA: Washington",
        "WV: West Virginia",
        "WI: Wisconsin",
        "WY: Wyoming"
)
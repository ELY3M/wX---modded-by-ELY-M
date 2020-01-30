/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

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

object UtilityCities {

    private const val num = 345
    internal val CITY_OBJ = arrayOfNulls<City>(num)
    val cities: Array<String> = Array(num) { "" }
    val lat: DoubleArray = DoubleArray(num)
    val lon: DoubleArray = DoubleArray(num)

    fun initCitiesArray() {
        loadCitiesArray()
        (0 until num).forEach { CITY_OBJ[it] = City(cities[it], lat[it], lon[it]) }
    }

    private fun loadCitiesArray() {
        cities[0] = "Anchorage, AK"
        cities[1] = "Fairbanks, AK"
        cities[2] = "Juneau, AK"
        cities[3] = "Birmingham, AL"
        cities[4] = "Dothan, AL"
        cities[5] = "Decatur, AL"
        cities[6] = "Florence, AL"
        cities[7] = "Pensacola, FL"
        cities[8] = "Ft. Walton Beach, FL"
        cities[9] = "Montgomery, AL"
        cities[10] = "Selma, AL"
        cities[11] = "Rogers, AR"
        cities[12] = "Jonesboro, AR"
        cities[13] = "Pine Bluff, AR"
        cities[14] = "El Dorado, AR"
        cities[15] = "Phoenix, AZ"
        cities[16] = "Tucson, AZ"
        cities[17] = "Sierra Vista, AZ"
        cities[18] = "El Centro, CA"
        cities[19] = "Bakersfield, CA"
        cities[20] = "Redding, CA"
        cities[21] = "Eureka, CA"
        cities[22] = "Visalia, CA"
        cities[23] = "Los Angeles, CA"
        cities[24] = "Salinas, CA"
        cities[25] = "Palm Springs, CA"
        cities[26] = "Modesto, CA"
        cities[27] = "San Diego, CA"
        cities[28] = "San Jose, CA"
        cities[29] = "San Luis Obispo, CA"
        cities[30] = "El Centro, CA"
        cities[31] = "Pueblo, CO"
        cities[32] = "Denver, CO"
        cities[33] = "Montrose, CO"
        cities[34] = "Hartford, CT"
        cities[35] = "New Haven, CT"
        cities[36] = "Washington DC "
        cities[37] = "Hagerstown, MD"
        cities[38] = "Naples, FL"
        cities[39] = "Gainesville, FL"
        cities[40] = "Jacksonville, FL"
        cities[41] = "Ft. Lauderdale, FL"
        cities[42] = "Pensacola, FL"
        cities[43] = "Ft. Walton Beach, FL"
        cities[44] = "Melbourne, FL"
        cities[45] = "Panama City, FL"
        cities[46] = "Thomasville, GA"
        cities[47] = "St. Petersburg, FL"
        cities[48] = "Sarasota, FL"
        cities[49] = "Ft. Pierce, FL"
        cities[50] = "Albany, GA"
        cities[51] = "Atlanta, GA"
        cities[52] = "Augusta, GA"
        cities[53] = "Columbus, GA"
        cities[54] = "Macon, GA"
        cities[55] = "Savannah, GA"
        cities[56] = "Thomasville, GA"
        cities[57] = "Honolulu, HI"
        cities[58] = "Iowa City, IA"
        cities[59] = "Moline, IL"
        cities[60] = "Ames, IA"
        cities[61] = "Kirksville, MO"
        cities[62] = "Keokuk, IA"
        cities[63] = "Austin, MN"
        cities[64] = "Sioux City, IA"
        cities[65] = "Boise, ID"
        cities[66] = "Pocatello, ID"
        cities[67] = "Twin Falls, ID"
        cities[68] = "Champaign, IL"
        cities[69] = "Springfield, IL"
        cities[70] = "Decatur, IL"
        cities[71] = "Chicago, IL"
        cities[72] = "Moline, IL"
        cities[73] = "Mount Vernon, IL"
        cities[74] = "Bloomington"
        cities[75] = "Keokuk, IA"
        cities[76] = "Rockford, IL"
        cities[77] = "Evansville, IN"
        cities[78] = "Ft. Wayne, IN"
        cities[79] = "Indianapolis, IN"
        cities[80] = "Lafayette, IN"
        cities[81] = "Montpelier, VT"
        cities[82] = "Terre Haute, IN"
        cities[83] = "Pittsburg, KS"
        cities[84] = "Topeka, KS"
        cities[85] = "Hutchinson"
        cities[86] = "Bowling Green, KY"
        cities[87] = "Lexington, KY"
        cities[88] = "Louisville, KY"
        cities[89] = "Mount Vernon, IL"
        cities[90] = "Alexandria, LA"
        cities[91] = "Baton Rouge, LA"
        cities[92] = "Lafayette, LA"
        cities[93] = "Lake Charles, LA"
        cities[94] = "El Dorado"
        cities[95] = "New Orleans, LA"
        cities[96] = "Shreveport, LA"
        cities[97] = "Manchester, NH"
        cities[98] = "New Bedford, MA"
        cities[99] = "Holyoke, MA"
        cities[100] = "Baltimore, MD"
        cities[101] = "Salisbury, MD"
        cities[102] = "Bangor, ME"
        cities[103] = "Auburn, ME"
        cities[104] = "Presque Isle, ME"
        cities[105] = "Alpena, MI"
        cities[106] = "Detroit, MI"
        cities[107] = "Bay City, MI"
        cities[108] = "Battle Creek, MI"
        cities[109] = "Lansing, MI"
        cities[110] = "Marquette, MI"
        cities[111] = "Cadillac, MI"
        cities[112] = "Superior, WI"
        cities[113] = "Mankato, MN"
        cities[114] = "St. Paul, MN"
        cities[115] = "Austin, MN"
        cities[116] = "Jefferson City, MO"
        cities[117] = "Pittsburg, KS"
        cities[118] = "Kansas City, MO"
        cities[119] = "Kirksville, MO"
        cities[120] = "Mount Vernon, IL"
        cities[121] = "Keokuk, IA"
        cities[122] = "Springfield, MO"
        cities[123] = "St. Joseph, MO"
        cities[124] = "St. Louis, MO"
        cities[125] = "Gulfport, MS"
        cities[126] = "West Point, MS"
        cities[127] = "Greenville, MS"
        cities[128] = "Laurel, MS"
        cities[129] = "Jackson, MS"
        cities[130] = "Meridian, MS"
        cities[131] = "Billings, MT"
        cities[132] = "Bozeman, MT"
        cities[133] = "Glendive, MT"
        cities[134] = "Great Falls, MT"
        cities[135] = "Helena, MT"
        cities[136] = "Missoula, MT"
        cities[137] = "Charlotte, NC"
        cities[138] = "Winston Salem, NC"
        cities[139] = "Washington, NC"
        cities[140] = "Anderson, SC"
        cities[141] = "Durham, NC"
        cities[142] = "Fayetteville, NC"
        cities[143] = "Wilmington, NC"
        cities[144] = "Valley City, ND"
        cities[145] = "Dickinson, ND"
        cities[146] = "Williston, ND"
        cities[147] = "Scottsbluff, NE"
        cities[148] = "Kearney, NE"
        cities[149] = "North Platte, NE"
        cities[150] = "Omaha, NE"
        cities[151] = "Manchester, NH"
        cities[152] = "Santa Fe, NM"
        cities[153] = "Las Vegas, NV"
        cities[154] = "Reno, NV"
        cities[155] = "Albany, NY"
        cities[156] = "Binghamton, NY"
        cities[157] = "Buffalo, NY"
        cities[158] = "Plattsburgh"
        cities[159] = "Elmira, NY"
        cities[160] = "New York, NY"
        cities[161] = "Rochester, NY"
        cities[162] = "Syracuse, NY"
        cities[163] = "Utica, NY"
        cities[164] = "Watertown, NY"
        cities[165] = "Cincinnati, OH"
        cities[166] = "Akron, OH"
        cities[167] = "Canton, OH"
        cities[168] = "Columbus, OH"
        cities[169] = "Dayton, OH"
        cities[170] = "Lima, OH"
        cities[171] = "Toledo, OH"
        cities[172] = "Steubenville, OH"
        cities[173] = "Youngstown, OH"
        cities[174] = "Zanesville, OH"
        cities[175] = "Oklahoma City, OK"
        cities[176] = "Ada, OK"
        cities[177] = "Tulsa, OK"
        cities[178] = "Wichita Falls, TX"
        cities[179] = "Lawton, OK"
        cities[180] = "Bend, OR"
        cities[181] = "Eugene, OR"
        cities[182] = "Klamath Falls, OR"
        cities[183] = "Portland, OR"
        cities[184] = "Erie, PA"
        cities[185] = "York, PA"
        cities[186] = "Altoona, PA"
        cities[187] = "Philadelphia, PA"
        cities[188] = "Pittsburgh, PA"
        cities[189] = "Scranton, PA"
        cities[190] = "New Bedford"
        cities[191] = "Charleston, SC"
        cities[192] = "Columbia, SC"
        cities[193] = "Myrtle Beach, SC"
        cities[194] = "Anderson, SC"
        cities[195] = "Rapid City, SD"
        cities[196] = "Sioux Falls, SD"
        cities[197] = "Mitchell, SD"
        cities[198] = "Chattanooga, TN"
        cities[199] = "Jackson, TN"
        cities[200] = "Knoxville, TN"
        cities[201] = "Memphis, TN"
        cities[202] = "Nashville, TN"
        cities[203] = "Little Rock, AR"
        cities[204] = "Sweetwater, TX"
        cities[205] = "Amarillo, TX"
        cities[206] = "Austin, TX"
        cities[207] = "Port Arthur, TX"
        cities[208] = "Corpus Christi, TX"
        cities[209] = "Ft. Worth, TX"
        cities[210] = "El Paso, TX"
        cities[211] = "McAllen, TX"
        cities[212] = "Houston, TX"
        cities[213] = "Laredo, TX"
        cities[214] = "Lubbock, TX"
        cities[215] = "Midland, TX"
        cities[216] = "San Angelo, TX"
        cities[217] = "San Antonio, TX"
        cities[218] = "Ada, TX"
        cities[219] = "Longview, TX"
        cities[220] = "Lufkin, TX"
        cities[221] = "Nacogdoches, TX"
        cities[222] = "Victoria, TX"
        cities[223] = "Bryan, TX"
        cities[224] = "Salt Lake City, UT"
        cities[225] = "Charlottesville, VA"
        cities[226] = "Harrisonburg, VA"
        cities[227] = "Newport News, VA"
        cities[228] = "Petersburg, VA"
        cities[229] = "Lynchburg, VA"
        cities[230] = "Plattsburgh"
        cities[231] = "Tacoma, WA"
        cities[232] = "Spokane, WA"
        cities[233] = "Kennewick, WA"
        cities[234] = "Superior, WI"
        cities[235] = "Appleton, WI"
        cities[236] = "Eau Claire, WI"
        cities[237] = "Madison, WI"
        cities[238] = "Milwaukee, WI"
        cities[239] = "Rhinelander, WI"
        cities[240] = "Oak Hill, WV"
        cities[241] = "Huntington, WV"
        cities[242] = "Weston, WV"
        cities[243] = "Parkersburg, WV"
        cities[244] = "Steubenville, OH"
        cities[245] = "Riverton, WY"

        lat[0] = 61.2180556
        lat[1] = 64.837777799999998
        lat[2] = 58.301944399999996
        lat[3] = 33.520660800000002
        lat[4] = 31.223231299999998
        lat[5] = 34.605925300000003
        lat[6] = 34.799810000000001
        lat[7] = 30.421309000000001
        lat[8] = 30.405755200000002
        lat[9] = 32.366805200000002
        lat[10] = 32.407358899999998
        lat[11] = 36.332019600000002
        lat[12] = 35.842296699999999
        lat[13] = 34.228431200000003
        lat[14] = 33.20732
        lat[15] = 33.448377100000002
        lat[16] = 32.221742900000002
        lat[17] = 31.545500100000002
        lat[18] = 32.792000000000002
        lat[19] = 35.3732921
        lat[20] = 40.586539600000002
        lat[21] = 40.8020712
        lat[22] = 36.330228400000003
        lat[23] = 34.052234200000001
        lat[24] = 36.677737200000003
        lat[25] = 33.830296099999998
        lat[26] = 37.639097200000002
        lat[27] = 32.715329199999999
        lat[28] = 37.339385700000001
        lat[29] = 35.2827524
        lat[30] = 32.792000000000002
        lat[31] = 38.254447200000001
        lat[32] = 39.739153600000002
        lat[33] = 38.478319800000001
        lat[34] = 41.763711100000002
        lat[35] = 41.308152700000001
        lat[36] = 38.895111800000002
        lat[37] = 39.641762900000003
        lat[38] = 26.142035799999999
        lat[39] = 29.651634399999999
        lat[40] = 30.332183799999999
        lat[41] = 26.122308400000001
        lat[42] = 30.421309000000001
        lat[43] = 30.405755200000002
        lat[44] = 28.083626899999999
        lat[45] = 30.158812900000001
        lat[46] = 30.836581500000001
        lat[47] = 27.782253999999998
        lat[48] = 27.336434700000002
        lat[49] = 27.446705600000001
        lat[50] = 31.578507399999999
        lat[51] = 33.748995399999998
        lat[52] = 33.469999999999999
        lat[53] = 32.4609764
        lat[54] = 32.840694599999999
        lat[55] = 32.0835407
        lat[56] = 30.836581500000001
        lat[57] = 21.306944399999999
        lat[58] = 41.677204000000003
        lat[59] = 41.506700299999999
        lat[60] = 42.023350000000001
        lat[61] = 40.194753900000002
        lat[62] = 40.402524999999997
        lat[63] = 43.6666296
        lat[64] = 42.499994200000003
        lat[65] = 43.613739000000002
        lat[66] = 42.8713032
        lat[67] = 42.562966799999998
        lat[68] = 40.0960397
        lat[69] = 39.7989763
        lat[70] = 39.862791
        lat[71] = 41.850033000000003
        lat[72] = 41.506700299999999
        lat[73] = 38.317271400000003
        lat[74] = 40.484202699999997
        lat[75] = 40.402524999999997
        lat[76] = 42.271131099999998
        lat[77] = 37.974764399999998
        lat[78] = 41.130604099999999
        lat[79] = 39.768376500000002
        lat[80] = 40.416702200000003
        lat[81] = 44.260015
        lat[82] = 39.4667034
        lat[83] = 37.410884000000003
        lat[84] = 39.048333599999999
        lat[85] = 38.060844500000002
        lat[86] = 36.990319900000003
        lat[87] = 38.031713600000003
        lat[88] = 38.254237600000003
        lat[89] = 38.317271400000003
        lat[90] = 31.311293599999999
        lat[91] = 30.450746200000001
        lat[92] = 30.2240897
        lat[93] = 30.226594899999998
        lat[94] = -34.650878900000002
        lat[95] = 29.964722200000001
        lat[96] = 32.525151600000001
        lat[97] = 42.995639699999998
        lat[98] = 41.636215200000002
        lat[99] = 42.204258600000003
        lat[100] = 39.290384799999998
        lat[101] = 38.360673599999998
        lat[102] = 44.801182099999998
        lat[103] = 44.097850899999997
        lat[104] = 46.681153000000002
        lat[105] = 45.061679400000003
        lat[106] = 42.34888
        lat[107] = 43.594467700000003
        lat[108] = 42.3211522
        lat[109] = 42.732534999999999
        lat[110] = 46.543544199999999
        lat[111] = 44.251952600000003
        lat[112] = 46.720773700000002
        lat[113] = 44.163577500000002
        lat[114] = 44.944167
        lat[115] = 43.6666296
        lat[116] = 38.576701700000001
        lat[117] = 37.410884000000003
        lat[118] = 39.099726500000003
        lat[119] = 40.194753900000002
        lat[120] = 38.317271400000003
        lat[121] = 40.402524999999997
        lat[122] = 37.215325999999997
        lat[123] = 39.7577778
        lat[124] = 38.646991
        lat[125] = 30.3674198
        lat[126] = 33.607618600000002
        lat[127] = 33.410116100000003
        lat[128] = 31.694050900000001
        lat[129] = 32.298757299999998
        lat[130] = 32.364309800000001
        lat[131] = 45.783285599999999
        lat[132] = 45.683459999999997
        lat[133] = 47.108491000000001
        lat[134] = 47.500235400000001
        lat[135] = 46.595804999999999
        lat[136] = 46.872146000000001
        lat[137] = 35.227086900000003
        lat[138] = 36.099859600000002
        lat[139] = 35.546551700000002
        lat[140] = 34.503439399999998
        lat[141] = 35.994032900000001
        lat[142] = 35.052664100000001
        lat[143] = 34.225725500000003
        lat[144] = 46.923312899999999
        lat[145] = 46.879175600000003
        lat[146] = 48.146968299999997
        lat[147] = 41.867139999999999
        lat[148] = 40.699959
        lat[149] = 41.1238873
        lat[150] = 41.254005999999997
        lat[151] = 42.995639699999998
        lat[152] = 35.686975199999999
        lat[153] = 36.114646
        lat[154] = 39.529632900000003
        lat[155] = 42.6511674
        lat[156] = 42.098686700000002
        lat[157] = 42.886446800000002
        lat[158] = 44.699487300000001
        lat[159] = 42.089796499999999
        lat[160] = 40.714269100000003
        lat[161] = 43.154784499999998
        lat[162] = 43.048122100000001
        lat[163] = 43.100903000000002
        lat[164] = 43.974783799999997
        lat[165] = 39.136111100000001
        lat[166] = 41.081444699999999
        lat[167] = 40.798947300000002
        lat[168] = 39.961175500000003
        lat[169] = 39.758947800000001
        lat[170] = 40.742550999999999
        lat[171] = 41.663938299999998
        lat[172] = 40.369790500000001
        lat[173] = 41.099780299999999
        lat[174] = 39.940345299999997
        lat[175] = 35.467560200000001
        lat[176] = 34.774531000000003
        lat[177] = 36.153981600000002
        lat[178] = 33.695379099999997
        lat[179] = 34.608685399999999
        lat[180] = 44.058172800000001
        lat[181] = 44.052069099999997
        lat[182] = 42.224867000000003
        lat[183] = 45.5234515
        lat[184] = 42.129224100000002
        lat[185] = 39.962598399999997
        lat[186] = 40.5186809
        lat[187] = 39.952334999999998
        lat[188] = 40.440624800000002
        lat[189] = 41.408968999999999
        lat[190] = 41.636215200000002
        lat[191] = 32.776565599999998
        lat[192] = 34.000710400000003
        lat[193] = 33.689060300000001
        lat[194] = 34.503439399999998
        lat[195] = 44.080543400000003
        lat[196] = 43.549974900000002
        lat[197] = 43.709428299999999
        lat[198] = 35.045629699999999
        lat[199] = 35.614516899999998
        lat[200] = 35.960638400000001
        lat[201] = 35.149534299999999
        lat[202] = 36.165889900000003
        lat[203] = 34.7464809
        lat[204] = 32.470951900000003
        lat[205] = 35.221997100000003
        lat[206] = 30.267153
        lat[207] = 29.884950400000001
        lat[208] = 27.800582800000001
        lat[209] = 32.725408999999999
        lat[210] = 31.758719800000001
        lat[211] = 26.2034071
        lat[212] = 29.762884400000001
        lat[213] = 27.506406999999999
        lat[214] = 33.577863100000002
        lat[215] = 31.997345599999999
        lat[216] = 31.463772299999999
        lat[217] = 29.424121899999999
        lat[218] = 34.774531000000003
        lat[219] = 32.500703700000003
        lat[220] = 31.338240599999999
        lat[221] = 31.603512899999998
        lat[222] = 28.805267400000002
        lat[223] = 30.674364300000001
        lat[224] = 40.760779300000003
        lat[225] = 38.029305899999997
        lat[226] = 38.449568800000002
        lat[227] = 36.978758800000001
        lat[228] = 37.227927899999997
        lat[229] = 37.4137536
        lat[230] = 44.699487300000001
        lat[231] = 47.252876800000003
        lat[232] = 47.658780200000002
        lat[233] = 46.2112458
        lat[234] = 46.720773700000002
        lat[235] = 44.261930900000003
        lat[236] = 44.811349
        lat[237] = 43.073051700000001
        lat[238] = 43.038902499999999
        lat[239] = 45.636622799999998
        lat[240] = 37.972333900000002
        lat[241] = 38.419249600000001
        lat[242] = 39.038427400000003
        lat[243] = 39.266741799999998
        lat[244] = 40.369790500000001
        lat[245] = 43.024959199999998

        lon[0] = 149.9002778
        lon[1] = 147.7163889
        lon[2] = 134.4197222
        lon[3] = 86.802490000000006
        lon[4] = 85.3904888
        lon[5] = 86.983341699999997
        lon[6] = 87.677250999999998
        lon[7] = 87.216914900000006
        lon[8] = 86.618842000000001
        lon[9] = 86.299968899999996
        lon[10] = 87.021100700000005
        lon[11] = 94.118536599999999
        lon[12] = 90.704279
        lon[13] = 92.003195500000004
        lon[14] = 92.66569
        lon[15] = 112.0740373
        lon[16] = 110.926479
        lon[17] = 110.2772856
        lon[18] = 115.56305140000001
        lon[19] = 119.01871250000001
        lon[20] = 122.3916754
        lon[21] = 124.16367289999999
        lon[22] = 119.2920585
        lon[23] = 118.24368490000001
        lon[24] = 121.6555013
        lon[25] = 116.5452921
        lon[26] = 120.9968782
        lon[27] = 117.1572551
        lon[28] = 121.89495549999999
        lon[29] = 120.6596156
        lon[30] = 115.56305140000001
        lon[31] = 104.6091409
        lon[32] = 104.9847034
        lon[33] = 107.8761738
        lon[34] = 72.685093199999997
        lon[35] = 72.9281577
        lon[36] = 77.036365799999999
        lon[37] = 77.719993200000005
        lon[38] = 81.794810299999995
        lon[39] = 82.324826200000004
        lon[40] = 81.655651000000006
        lon[41] = 80.143378600000005
        lon[42] = 87.216914900000006
        lon[43] = 86.618842000000001
        lon[44] = 80.608108900000005
        lon[45] = 85.6602058
        lon[46] = 83.978780799999996
        lon[47] = 82.667619000000002
        lon[48] = 82.530652700000005
        lon[49] = 80.325605600000003
        lon[50] = 84.155741000000006
        lon[51] = 84.387982399999999
        lon[52] = 81.974999999999994
        lon[53] = 84.9877094
        lon[54] = 83.632402200000001
        lon[55] = 81.099834200000004
        lon[56] = 83.978780799999996
        lon[57] = 157.8583333
        lon[58] = 91.5162792
        lon[59] = 90.515134200000006
        lon[60] = 93.625622000000007
        lon[61] = 92.583249600000002
        lon[62] = 91.394372000000004
        lon[63] = 92.974636700000005
        lon[64] = 96.400306900000004
        lon[65] = 116.237651
        lon[66] = 112.4455344
        lon[67] = 114.46087110000001
        lon[68] = 88.304742984406
        lon[69] = 89.6443688
        lon[70] = 88.8938600532607
        lon[71] = 87.650052299999999
        lon[72] = 90.515134200000006
        lon[73] = 88.903120099999995
        lon[74] = 88.993687300000005
        lon[75] = 91.394372000000004
        lon[76] = 89.093995199999995
        lon[77] = 87.5558482
        lon[78] = 85.128859700000007
        lon[79] = 86.158042300000005
        lon[80] = 86.875286900000006
        lon[81] = 72.5753599
        lon[82] = 87.413909200000006
        lon[83] = 94.70496
        lon[84] = 95.678037099999997
        lon[85] = 97.929774300000005
        lon[86] = 86.443601799999996
        lon[87] = 84.495135899999994
        lon[88] = 85.759406999999996
        lon[89] = 88.903120099999995
        lon[90] = 92.445137099999997
        lon[91] = 91.154550999999998
        lon[92] = 92.019842699999998
        lon[93] = 93.217375799999999
        lon[94] = 61.583858499999998
        lon[95] = 90.070555600000006
        lon[96] = 93.750178899999995
        lon[97] = 71.454789099999999
        lon[98] = 70.934205000000006
        lon[99] = 72.616200899999995
        lon[100] = 76.612189299999997
        lon[101] = 75.599369199999998
        lon[102] = 68.777813800000004
        lon[103] = 70.231165500000003
        lon[104] = 68.0158615
        lon[105] = 83.432752800000003
        lon[106] = 83.088540
        lon[107] = 83.888864699999999
        lon[108] = 85.179714200000006
        lon[109] = 84.555534699999995
        lon[110] = 87.395416999999995
        lon[111] = 85.401161900000005
        lon[112] = 92.104079600000006
        lon[113] = 93.999399600000004
        lon[114] = 93.086074999999994
        lon[115] = 92.974636700000005
        lon[116] = 92.173516399999997
        lon[117] = 94.70496
        lon[118] = 94.578566699999996
        lon[119] = 92.583249600000002
        lon[120] = 88.903120099999995
        lon[121] = 91.394372000000004
        lon[122] = 93.298243600000006
        lon[123] = 94.836388900000003
        lon[124] = 90.224967000000007
        lon[125] = 89.0928155
        lon[126] = 88.6503254
        lon[127] = 91.061773500000001
        lon[128] = 89.130612400000004
        lon[129] = 90.184810299999995
        lon[130] = 88.703655999999995
        lon[131] = 108.5006904
        lon[132] = 111.050499
        lon[133] = 104.710419
        lon[134] = 111.3008083
        lon[135] = 112.02703099999999
        lon[136] = 113.99399819999999
        lon[137] = 80.843126699999999
        lon[138] = 80.244215999999994
        lon[139] = 77.052174199999996
        lon[140] = 82.650133199999999
        lon[141] = 78.898618999999997
        lon[142] = 78.878358500000004
        lon[143] = 77.944710200000003
        lon[144] = 98.003154699999996
        lon[145] = 102.78962420000001
        lon[146] = 103.6179745
        lon[147] = 103.660709
        lon[148] = 99.083106999999998
        lon[149] = 100.7654232
        lon[150] = 95.999257999999998
        lon[151] = 71.454789099999999
        lon[152] = 105.937799
        lon[153] = 115.172816
        lon[154] = 119.8138027
        lon[155] = 73.754968
        lon[156] = 75.917973799999999
        lon[157] = 78.878368899999998
        lon[158] = 73.452912400000002
        lon[159] = 76.807733799999994
        lon[160] = 74.005972900000003
        lon[161] = 77.615556699999999
        lon[162] = 76.147424400000006
        lon[163] = 75.232664
        lon[164] = 75.910756500000005
        lon[165] = 84.503055599999996
        lon[166] = 81.519005300000003
        lon[167] = 81.378446999999994
        lon[168] = 82.998794200000006
        lon[169] = 84.191606899999996
        lon[170] = 84.105225599999997
        lon[171] = 83.555211999999997
        lon[172] = 80.633963800000004
        lon[173] = 80.649519400000003
        lon[174] = 82.013192399999994
        lon[175] = 97.5164276
        lon[176] = 96.678344899999999
        lon[177] = 95.992774999999995
        lon[178] = 98.308844100000002
        lon[179] = 98.390330500000005
        lon[180] = 121.31530960000001
        lon[181] = 123.08675359999999
        lon[182] = 121.7816704
        lon[183] = 122.6762071
        lon[184] = 80.085059000000001
        lon[185] = 76.727744999999999
        lon[186] = 78.394735900000001
        lon[187] = 75.163788999999994
        lon[188] = 79.995886400000003
        lon[189] = 75.662412200000006
        lon[190] = 70.934205000000006
        lon[191] = 79.930921600000005
        lon[192] = 81.034814400000002
        lon[193] = 78.886694300000002
        lon[194] = 82.650133199999999
        lon[195] = 103.23101490000001
        lon[196] = 96.700327000000001
        lon[197] = 98.029799199999999
        lon[198] = 85.309680099999994
        lon[199] = 88.813946900000005
        lon[200] = 83.9207392
        lon[201] = 90.048980099999994
        lon[202] = 86.784443199999998
        lon[203] = 92.2895948
        lon[204] = 100.4059384
        lon[205] = 101.8312969
        lon[206] = 97.743060799999995
        lon[207] = 93.939947000000004
        lon[208] = 97.396381000000005
        lon[209] = 97.320849600000003
        lon[210] = 106.4869314
        lon[211] = 98.230012400000007
        lon[212] = 95.383061499999997
        lon[213] = 99.507542099999995
        lon[214] = 101.8551665
        lon[215] = 102.0779146
        lon[216] = 100.4370375
        lon[217] = 98.493628200000003
        lon[218] = 96.678344899999999
        lon[219] = 94.740489100000005
        lon[220] = 94.729096999999996
        lon[221] = 94.655487399999998
        lon[222] = 97.003598199999999
        lon[223] = 96.369963200000001
        lon[224] = 111.89104740000001
        lon[225] = 78.476678100000001
        lon[226] = 78.8689155
        lon[227] = 76.428003000000004
        lon[228] = 77.401926700000004
        lon[229] = 79.142246400000005
        lon[230] = 73.452912400000002
        lon[231] = 122.4442906
        lon[232] = 117.42604660000001
        lon[233] = 119.1372338
        lon[234] = 92.104079600000006
        lon[235] = 88.415384700000004
        lon[236] = 91.498494100000002
        lon[237] = 89.401230200000001
        lon[238] = 87.906473599999998
        lon[239] = 89.412075299999998
        lon[240] = 81.148713499999999
        lon[241] = 82.445154000000002
        lon[242] = 80.467313000000004
        lon[243] = 81.561513500000004
        lon[244] = 80.633963800000004
        lon[245] = 108.3801036

        cities[246] = "Flint, MI"
        lat[246] = 43.0171773
        lon[246] = 83.7236024

        // added

        cities[247] = "Grand Rapids, MI"
        lat[247] = 42.9633599
        lon[247] = 85.6680863

        cities[248] = "Muskegon, MI"
        lat[248] = 43.2341813
        lon[248] = 86.2483921

        cities[249] = "Gaylord, MI"
        lat[249] = 45.0275126
        lon[249] = 84.6747523

        cities[250] = "Sault Ste. Marie, MI"
        lat[250] = 46.491292
        lon[250] = 84.3515787

        cities[251] = "Mt Pleasant, MI"
        lat[251] = 43.597646
        lon[251] = 84.7668495

        cities[252] = "Port Huron, MI"
        lat[252] = 42.9815877
        lon[252] = 82.440466

        cities[253] = "Oscoda, MI"
        lat[253] = 44.4108489
        lon[253] = 83.3321899

        cities[254] = "Grayling, MI"
        lat[254] = 44.6615168
        lon[254] = 84.7146371

        cities[255] = "West Branch, MI"
        lat[255] = 44.2764083
        lon[255] = 84.2386132

        cities[256] = "South Bend, IN"
        lat[256] = 41.6833813
        lon[256] = 86.2500066

        cities[257] = "Bloomington, IN"
        lat[257] = 39.1670396
        lon[257] = 86.5342881

        cities[258] = "Peoria, IL"
        lat[258] = 40.6938609
        lon[258] = 89.5891008

        cities[259] = "Rockford, IL"
        lat[259] = 42.2713945
        lon[259] = 89.093966

        cities[260] = "Cleveland, OH"
        lat[260] = 41.4871888
        lon[260] = 81.6778691

        cities[261] = "Traverse City, MI"
        lat[261] = 44.7606441
        lon[261] = 85.6165301

        cities[262] = "Boston, MA"
        lat[262] = 42.3604823
        lon[262] = 71.0595678

        cities[263] = "Portland, ME"
        lat[263] = 43.6610277
        lon[263] = 70.2548596

        cities[264] = "Fort Smith, AR"
        lat[264] = 35.3857623
        lon[264] = 94.3986725

        cities[265] = "Miami, FL"
        lat[265] = 25.7742658
        lon[265] = 80.1936589

        cities[266] = "Raleigh, NC"
        lat[266] = 35.7804015
        lon[266] = 78.6390779

        cities[267] = "Orlando, FL"
        lat[267] = 28.5421175
        lon[267] = 81.3790462

        cities[268] = "Key West, FL"
        lat[268] = 24.5625566
        lon[268] = 81.7724368

        cities[269] = "San Francisco, CA"
        lat[269] = 37.7789601
        lon[269] = 122.419199

        cities[270] = "Fresno, CA"
        lat[270] = 36.7394421
        lon[270] = 119.7848307

        cities[271] = "Sacramento, CA"
        lat[271] = 38.5815719
        lon[271] = 121.4943996

        cities[272] = "Flagstaff, AZ"
        lat[272] = 35.199458
        lon[272] = 111.6514259

        cities[273] = "Albuquerque, NM"
        lat[273] = 35.0841034
        lon[273] = 106.6509851

        cities[274] = "Green Bay, WI"
        lat[274] = 44.5418195
        lon[274] = 87.8688458600556

        cities[275] = "Cheyenne, WY"
        lat[275] = 41.1399814
        lon[275] = 104.8202462

        cities[276] = "Casper, WY"
        lat[276] = 42.866632
        lon[276] = 106.313081

        cities[277] = "Elko, NV"
        lat[277] = 40.8324212
        lon[277] = 115.7631233

        cities[278] = "Provo, UT"
        lat[278] = 40.2338438
        lon[278] = 111.6585337

        cities[279] = "Logan, UT"
        lat[279] = 41.7313447
        lon[279] = 111.8348631

        cities[280] = "Lincoln, NE"
        lat[280] = 40.8000554
        lon[280] = 96.6674005

        cities[281] = "Caribou, ME"
        lat[281] = 46.8605982
        lon[281] = 68.0119714

        cities[282] = "Fargo, ND"
        lat[282] = 46.8770537
        lon[282] = 96.7897661

        cities[283] = "Bismarck, ND"
        lat[283] = 46.8083268
        lon[283] = 100.7837392

        cities[284] = "Fort Collins, CO"
        lat[284] = 40.5508527
        lon[284] = 105.0668085

        cities[285] = "Colorado Springs, CO"
        lat[285] = 38.8338816
        lon[285] = 104.8213634

        cities[286] = "Aberdeen, SD"
        lat[286] = 45.4646985
        lon[286] = 98.4864829

        cities[287] = "Richmond, VA"
        lat[287] = 37.5385087
        lon[287] = 77.43428

        cities[288] = "Virginia Beach, VA"
        lat[288] = 36.8529841
        lon[288] = 75.9774183

        cities[289] = "Dover, DE"
        lat[289] = 39.158168
        lon[289] = 75.5243682

        cities[290] = "Harrisburg, PA"
        lat[290] = 40.2663107
        lon[290] = 76.8861122

        cities[291] = "Atlantic City, NJ"
        lat[291] = 39.3642852
        lon[291] = 74.4229351

        cities[292] = "Providence, RI"
        lat[292] = 41.8239891
        lon[292] = 71.4128343

        cities[293] = "Rutland, VT"
        lat[293] = 43.6106237
        lon[293] = 72.9726065

        cities[294] = "Des Moines, IA"
        lat[294] = 41.5910641
        lon[294] = 93.6037149



        cities[295] = "Hilo, HI"
        lat[295] = 19.725
        lon[295] = 155.09

        cities[296] = "Haiku, HI"
        lat[296] = 20.9172
        lon[296] = 156.3294

        cities[297] = "Mobile, AL"
        lat[297] = 30.6928
        lon[297] = 88.0564

        cities[298] = "Tallahassee, FL"
        lat[298] = 30.4379
        lon[298] = 84.2814

        cities[299] = "Medford, OR"
        lat[299] = 42.3436
        lon[299] = 122.8441

        cities[300] = "Grand Junction, CO"
        lat[300] = 39.0646
        lon[300] = 108.5506

        cities[301] = "Goodland, KS"
        lat[301] = 39.3492
        lon[301] = 101.7104

        cities[302] = "Wichita, KS"
        lat[302] = 37.6840
        lon[302] = 97.3502

        cities[303] = "Glasgow, MT"
        lat[303] = 48.1973
        lon[303] = 106.6359

        cities[304] = "San Juan, PR"
        lat[304] = 18.4479
        lon[304] = 66.0762

        cities[305] = "Ponce, PR"
        lat[305] = 18.0119
        lon[305] = 66.6123

        cities[306] = "Seattle, WA"
        lat[306] = 47.65
        lon[306] = 122.31

        cities[307] = "Jackson, WY"
        lat[307] = 43.475278
        lon[307] = 110.769167

        cities[308] = "Idaho Falls, ID"
        lat[308] = 43.5
        lon[308] = 112.033333

        cities[309] = "Tampa, FL"
        lat[309] = 27.968056
        lon[309] = 82.476389

        cities[310] = "Butte, MT"
        lat[310] = 46.006389
        lon[310] = 112.529722

        cities[311] = "Lewiston, ID"
        lat[311] = 46.41
        lon[311] = 117.02

        cities[312] = "Ely, NV"
        lat[312] = 39.253333
        lon[312] = 114.877222

        cities[313] = "Cedar City, UT"
        lat[313] = 37.6825
        lon[313] = 113.074444

        cities[314] = "Kingman, AZ"
        lat[314] = 35.208333
        lon[314] = 114.025833

        cities[315] = "Seward, AK"
        lat[315] = 60.1196475
        lon[315] = 149.3748701

        cities[316] = "Kenai, AK"
        lat[316] = 60.5599189
        lon[316] = 151.2038401

        cities[317] = "Bethel, AK"
        lat[317] = 60.7907944
        lon[317] = 161.793728

        cities[318] = "Dillingham, AK"
        lat[318] = 59.0492389
        lon[318] = 158.5254715

        cities[319] = "King Salmon, AK"
        lat[319] = 58.7552401
        lon[319] = 156.5486959

        cities[320] = "New Stuyahok, AK"
        lat[320] = 59.441279
        lon[320] = 157.2390512

        cities[321] = "Nome, AK"
        lat[321] = 64.5241501
        lon[321] = 165.4118314

        cities[322] = "Savoonga, AK"
        lat[322] = 63.6800927
        lon[322] = 170.4891565

        cities[323] = "North Pole, AK"
        lat[323] = 64.7536865
        lon[323] = 147.3682196

        cities[324] = "Nenana, AK"
        lat[324] = 64.5267315
        lon[324] = 148.9891512

        cities[325] = "Healy, AK"
        lat[325] = 63.9492065
        lon[325] = 148.921923

        cities[326] = "Kapaa, HI"
        lat[326] = 22.0870939
        lon[326] = 159.354737

        cities[327] = "North Shore, HI"
        lat[327] = 21.5989375
        lon[327] = 158.1007655

        cities[328] = "Wailua, HI"
        lat[328] = 20.8486105
        lon[328] = 156.136389

        cities[329] = "Lanai City, HI"
        lat[329] = 20.8328859
        lon[329] = 156.9264704

        cities[330] = "Kaunakakai, HI"
        lat[330] = 21.0903347
        lon[330] = 157.0123233

        cities[331] = "Waimea, HI"
        lat[331] = 21.963357
        lon[331] = 159.67307

        cities[332] = "Sitka, AK"
        lat[332] = 57.0811562
        lon[332] = 135.5301858

        cities[333] = "Ketchikan, AK"
        lat[333] = 55.3464511
        lon[333] = 131.6591813

        cities[334] = "Petersburg, AK"
        lat[334] = 56.7663732
        lon[334] = 132.855609


        cities[335] = "Bemidji, MN" // MN
        lat[335] = 47.4757
        lon[335] = 94.8745

        cities[336] = "Tupelo, MS" // MS
        lat[336] = 34.3628
        lon[336] = 88.7250

        cities[337] = "Minot, MS" // MS
        lat[337] = 48.2373
        lon[337] = 101.2706

        cities[338] = "Carlsbad, NM" // NM
        lat[338] = 32.398
        lon[338] = 104.2155

        cities[339] = "Roswell, NM" // NM
        lat[339] = 33.3369
        lon[339] = 104.53

        cities[340] = "Clovis, NM" // NM
        lat[340] = 34.4539
        lon[340] = 104.2155

        cities[341] = "Hattiesburg, MS"
        lat[341] = 31.3108
        lon[341] = 89.3055

        cities[342] = "Ann Arbor, MI"
        lat[342] = 42.2681569
        lon[342] = 83.7312291

        cities[343] = "Dallas, TX"
        lat[343] = 32.7756
        lon[343] = 96.7995

        cities[344] = "Duluth, MN"
        lat[344] = 46.7833
        lon[344] = 92.1066
    }
}


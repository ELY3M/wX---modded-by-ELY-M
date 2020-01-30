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

package joshuatee.wx.radar

import android.content.Context

import joshuatee.wx.MyApplication
import joshuatee.wx.util.UCARRandomAccessFile
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.external.ExternalEllipsoid
import joshuatee.wx.external.ExternalGeodeticCalculator
import joshuatee.wx.external.ExternalGlobalCoordinates

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp
import joshuatee.wx.settings.UtilityLocation
import joshuatee.wx.util.UtilityIO

internal object WXGLNexradLevel3TVS {

    private const val tvsBaseFn = "nids_tvs_tab"

    fun decodeAndPlot(context: Context, rid: String, fnSuffix: String): List<Double> {
        val stormList = mutableListOf<Double>()
        val location = UtilityLocation.getSiteLocation(context, rid)
        WXGLDownload.getNidsTab(context, "TVS", rid, tvsBaseFn + fnSuffix)
        val dis: UCARRandomAccessFile
        try {
            dis = UCARRandomAccessFile(UtilityIO.getFilePath(context, tvsBaseFn + fnSuffix))
            dis.bigEndian = true
        } catch (e: Exception) {
            UtilityLog.handleException(e)
            return listOf()
        } catch (e: OutOfMemoryError) {
            UtilityLog.handleException(e)
            return listOf()
        }
        var retStr = UtilityLevel3TextProduct.read(dis)
        // P  TVS    R7   216/ 50    29    57    57/ 6.5    15.9    6.5/ 22.4    18/ 6.5    &#0;
        val tvs = retStr.parseColumn(RegExp.tvsPattern1)
        var tmpStr: String
        var tmpStrArr: Array<String>
        var degree: Int
        var nm: Int
        val bearing = DoubleArray(2)
        var start: ExternalGlobalCoordinates
        var ec: ExternalGlobalCoordinates
        tvs.indices.forEach {
            val ecc = ExternalGeodeticCalculator()
            tmpStr = tvs[it].parse(RegExp.tvsPattern2)
            tmpStrArr = MyApplication.slash.split(tmpStr)
            retStr += tmpStr
            degree = tmpStrArr[0].replace(" ", "").toIntOrNull() ?: 0
            nm = tmpStrArr[1].replace(" ", "").toIntOrNull() ?: 0
            start = ExternalGlobalCoordinates(location)
            ec = ecc.calculateEndingGlobalCoordinates(
                ExternalEllipsoid.WGS84,
                start,
                degree.toDouble(),
                nm * 1852.0,
                bearing
            )
            stormList.add(ec.latitude)
            stormList.add(ec.longitude * -1.0)
        }
        return stormList
    }
}

/*


 AZ    RAN  216   50  164   35  176   40  194   41                              &#0; &#0;V&#0;&#0;&#0;&#0;&#0;
 LLDV  MDV   57   57   62   62   56   72   52   62                              &#0; &#0;V&#0;&#0;&#0;&#0;&#0;
 AVGDV            29        14        38        27                              &#0; &#0;V&#0;&#0;&#0;&#0;&#0;)
 BASE DPTH   6.5  16  19.8   9   8.8  13   9.2  19                              &#0;
&#0;2&#0; &#0; &#0;&#0; î&#0;&#0;&#0; &#0;

top row is 216/50

;P         &#0;P                            Tornado Vortex Signature       &#0;P      Radar Id 312   Date/Time  08:02:15/21:27:58
Number of TVS/ETVS    1/  3 &#0;P     &#0;P Feat  Storm   AZ/RAN  AVGDV  LLDV  MXDV/Hgt   Depth    Base/Top   MXSHR/Hgt    &#0;
P Type    ID   (deg,nm)  (kt)  (kt)  (kt,kft)   (kft)     (kft)     (E-3/s,kft)  &#0;
P       &#0;
P  TVS    R7   216/ 50    29    57    57/ 6.5    15.9    6.5/ 22.4    18/ 6.5    &#0;
P ETVS    Y5   164/ 35    14    62    62/19.8     9.1   19.8/ 28.9    28/19.8    &#0;
P ETVS    G6   176/ 40    38    56    72/11.3    13.0    8.8/ 21.8    29/11.3    &#0;
P ETVS    V6   194/ 41    27    52    62/11.6    19.1    9.2/ 28.3    24/11.6    ÿÿ&#0;
P                 TORNADO VORTEX SIGNATURE ADAPTATION PARAMETERS                 &#0;
P                                                                                &#0;
P    0(dBZ).Min Reflectivity               2.5(km)..Circulation Radius #1        &#0;
P   11(m/s).Vector Velocity Difference     4.0(km)..Circulation Radius #2        &#0;
P  100(km)..Max Pattern Vector Range        80(km)..Circulation Radius Range     &#0;
P 10.0(km)..Max Pattern Vector Height      600......Max # of 2D Features         &#0;
P 2500......Max # of Pattern Vectors         3......Min # of 2D Feat/3D Feature  &#0;
P   11(m/s).Differential Velocity #1       1.5(km)..Min 3D Feature Depth         &#0;
P   15(m/s).Differential Velocity #2        25(m/s).Min 3D Feat Low-Lvl Delta Vel&#0;
P   20(m/s).Differential Velocity #3        36(m/s).Min TVS Delta Velocity       &#0;
P   25(m/s).Differential Velocity #4        35......Max # of 3D Features         &#0;
P   30(m/s).Differential Velocity #5        15......Max # of TVSs                &#0;
P   35(m/s).Differential Velocity #6        10......Max # of Elevated TVSs       &#0;
P    3......Min # of Vectors/2D Feature    0.6(km)..Min TVS Base Height          &#0;
P  0.5(km)..2D Vector Radial Distance      1.0(deg).Min TVS Elevation            &#0;
P  1.5(deg).2D Vector Azimuthal Dist       3.0(km)..Min Avg Delta Velocity Hgt   &#0;
P  4.0(km/km).2D Feature Aspect Ratio     20.0(km)..Max Storm Association Dist   ÿÿ



 TYPE STID  TVS   Y5 ETVS   J8 ETVS   M0 ETVS   S1 ETVS   S1 ETVS   S1          &#0; &#0;V&#0;&#0;&#0;&#0;&#0;
 AZ    RAN  138   33  186   46  210   49  167   45  157   43  147   39          &#0; &#0;V&#0;&#0;&#0;&#0;&#0;
 LLDV  MDV   50   79   76   76   73   73   67   67   66  120   52   67          &#0; &#0;V&#0;&#0;&#0;&#0;&#0;
 AVGDV            46        30        37        22        52        32          &#0; &#0;V&#0;&#0;&#0;&#0;&#0;)
 BASE DPTH < 2.5 >16   7.9  18   8.7  14  13.1  18   9.7   6  11.0  11          &#0;

P      Radar Id 312   Date/Time  08:02:15/21:37:42   Number of TVS/ETVS    1/  5 &#0;
P                                                                                &#0;
P Feat  Storm   AZ/RAN  AVGDV  LLDV  MXDV/Hgt   Depth    Base/Top   MXSHR/Hgt    &#0;
P Type    ID   (deg,nm)  (kt)  (kt)  (kt,kft)   (kft)     (kft)     (E-3/s,kft)  &#0;
P                                                                                &#0;
P  TVS    Y5   138/ 33    46    50    79/ 5.6   >16.0  < 2.5/ 18.5    37/ 5.6    &#0;
P ETVS    J8   186/ 46    30    76    76/ 7.9    17.6    7.9/ 25.5    26/ 7.9    &#0;
P ETVS    M0   210/ 49    37    73    73/ 8.7    14.1    8.7/ 22.8    23/ 8.7    &#0;
P ETVS    S1   167/ 45    22    67    67/13.1    17.7   13.1/ 30.7    24/13.1    &#0;
P ETVS    S1   157/ 43    52    66   120/15.5     5.8    9.7/ 15.5    45/15.5    &#0;
P ETVS    S1   147/ 39    32    52    67/17.2    11.2   11.0/ 22.2    28/17.2    ÿÿ&#0;
P                 TORNADO VORTEX SIGNATURE ADAPTATION PARAMETERS                 &#0;P                                                                                &#0;P    0(dBZ).Min Reflectivity               2.5(km)..Circulation Radius #1        &#0;P   11(m/s).Vector Velocity Difference     4.0(km)..Circulation Radius #2        &#0;P  100(km)..Max Pattern Vector Range        80(km)..Circulation Radius Range     &#0;P 10.0(km)..Max Pattern Vector Height      600......Max # of 2D Features         &#0;P 2500......Max # of Pattern Vectors         3......Min # of 2D Feat/3D Feature  &#0;P   11(m/s).Differential Velocity #1       1.5(km)..Min 3D Feature Depth         &#0;P   15(m/s).Differential Velocity #2        25(m/s).Min 3D Feat Low-Lvl Delta Vel&#0;P   20(m/s).Differential Velocity #3        36(m/s).Min TVS Delta Velocity       &#0;P   25(m/s).Differential Velocity #4        35......Max # of 3D Features         &#0;P   30(m/s).Differential Velocity #5        15......Max # of TVSs                &#0;P   35(m/s).Differential Velocity #6        10......Max # of Elevated TVSs       &#0;P    3......Min # of Vectors/2D Feature    0.6(km)..Min TVS Base Height          &#0;P  0.5(km)..2D Vector Radial Distance      1.0(deg).Min TVS Elevation            &#0;P  1.5(deg).2D Vector Azimuthal Dist       3.0(km)..Min Avg Delta Velocity Hgt   &#0;P  4.0(km/km).2D Feature Aspect Ratio     20.0(km)..Max Storm Association Dist   ÿÿ


 */

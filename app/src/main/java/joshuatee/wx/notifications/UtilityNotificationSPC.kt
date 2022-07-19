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

package joshuatee.wx.notifications

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.settings.Location
import joshuatee.wx.spc.UtilitySpc
import joshuatee.wx.util.UtilityDownload
import joshuatee.wx.external.ExternalPoint
import joshuatee.wx.external.ExternalPolygon
import joshuatee.wx.spc.SpcMcdWatchShowActivity
import joshuatee.wx.spc.SpcSwoActivity
import joshuatee.wx.util.UtilityString
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import joshuatee.wx.Extensions.*
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.common.RegExp
import joshuatee.wx.objects.ObjectPolygonWatch
import joshuatee.wx.radar.LatLon
import joshuatee.wx.settings.NotificationPreferences
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityTime

internal object UtilityNotificationSpc {

    fun sendSwoNotifications(context: Context, inBlackout: Boolean): String {
        var notifUrls = ""
        if (NotificationPreferences.alertSpcSwoNotification) {
            val urls = arrayOf("SWODY1", "SWODY2", "SWODY3")
            urls.indices.forEach {
                val threatLevel = UtilitySpc.checkSpcDayX(context, urls[it])
                val test = if (NotificationPreferences.alertSpcSwoSlightNotification) {
                    threatLevel[0] == "high" || threatLevel[0] == "modt" || threatLevel[0] == "enh" || threatLevel[0] == "slight"
                } else {
                    threatLevel[0] == "high" || threatLevel[0] == "modt" || threatLevel[0] == "enh"
                }
                if (test) {
                    val dayStr = "SWODY" + (it + 1).toString()
                    val noMain = dayStr + " " + threatLevel[0]
                    val validTime = threatLevel[1].parse("Valid ([0-9]{6}Z - [0-9]{6}Z)")
                    var detailRaw = threatLevel[1].replace("<.*?>".toRegex(), " ")
                    detailRaw = detailRaw.replace("&nbsp".toRegex(), " ")
                    val noBody = detailRaw
                    val objectPendingIntents = ObjectPendingIntents(
                            context, SpcSwoActivity::class.java,
                            SpcSwoActivity.NUMBER,
                            arrayOf((it + 1).toString(), ""), arrayOf((it + 1).toString(), "sound")
                    )
                    val cancelString = "usspcswo" + it.toString() + threatLevel[0] + validTime
                    if (!(NotificationPreferences.alertOnlyOnce && UtilityNotificationUtils.checkToken(context, cancelString))) {
                        val sound = NotificationPreferences.alertNotificationSoundSpcswo && !inBlackout
                        val objectNotification = ObjectNotification(
                                context,
                                sound,
                                noMain,
                                noBody,
                                objectPendingIntents.resultPendingIntent,
                                GlobalVariables.ICON_MAP,
                                noBody,
                                NotificationCompat.PRIORITY_HIGH,
                                Color.YELLOW, // was Notification.PRIORITY_DEFAULT
                                GlobalVariables.ICON_ACTION,
                                objectPendingIntents.resultPendingIntent2,
                                context.resources.getString(R.string.read_aloud)
                        )
                        val notification = UtilityNotification.createNotificationBigTextWithAction(objectNotification)
                        objectNotification.sendNotification(context, cancelString, 1, notification)
                    }
                    notifUrls += cancelString + NotificationPreferences.notificationStrSep
                }
            }
        }
        return notifUrls
    }

    fun locationNeedsMcd() = (0 until Location.numLocations).any { MyApplication.locations.getOrNull(it)?.notificationMcd ?: false }

    fun locationNeedsSwo() = (0 until Location.numLocations).any { MyApplication.locations.getOrNull(it)?.notificationSwo ?: false }

    /*	... CATEGORICAL ...

		SLGT   26488256 27058145 27138124 27337986
		MRGL   28789106 30249015 31008913 31258721 31278684 30718447
		       30638425 29008048
		TSTM   28699165 30149157 30869191 31359271 31809310 32319337
		       33099340 33579303 34129258 34359194 34479089 34378920
		       34098685 33588443 32698161 31977981 99999999 33441119
		       34061155 35021134 35901083 36201040 37320921 38350840
		       39170739 39440690 39440630 39170596 38030631 37340639
		       36630646 35820694 34680763 33530770 33000803 32740833
		       32680870 32640991 33441119 99999999 43482148 43822052
		       43861949 43591907 43081905 42331997 41952061 41952098
		       41912155 42182207 42402218 42952203 43482148

		&&*/
    fun sendSwoLocationNotifications(context: Context): String {
        var notifUrls = ""
        val threatList = listOf("HIGH", "MDT", "ENH", "SLGT", "MRGL")
        (1..3).forEach { day ->
            val urlBlob = "${GlobalVariables.nwsSPCwebsitePrefix}/products/outlook/KWNSPTSDY" + day.toString() + ".txt"
            val html = urlBlob.getHtmlSep()
            val validTime = html.parse("VALID TIME ([0-9]{6}Z - [0-9]{6}Z)")
            val htmlBlob = html.parse("... CATEGORICAL ...(.*?&)&")
            threatList.forEach { threat ->
                var string = ""
                val htmlList = htmlBlob.parseColumn(threat.substring(1) + "(.*?)[A-Z&]")
                htmlList.forEach {
                    string += UtilityNotification.storeWatchMcdLatLon(it)
                    string = string.replace(" 99.99 99.99 ", " ") // need for the way SPC ConvO seperates on 8 's
                } // end looping over polygons of one threat level
                val items = RegExp.colon.split(string)
                items.forEach {
                    val latLons = LatLon.parseStringToLatLons(it, -1.0, false)
                    // inject bounding box coords if first doesn't equal last
                    // focus on east coast for now
                    //
                    // 52,-130               52,-62
                    // 21,-130                21,-62
                    //
                    if (latLons.isNotEmpty()) {
                        val polygonFrame = ExternalPolygon.Builder()
                        latLons.forEach {latLon -> polygonFrame.addVertex(ExternalPoint(latLon)) }
                        val polygonShape = polygonFrame.build()
                        (1..Location.numLocations).forEach { n ->
                            val locNum = n.toString()
                            if (MyApplication.locations[n - 1].notificationSwo) {
                                // if location is watching for MCDs pull ib lat/lon and iterate over polygons
                                // call secondary method to send notif if required
                                if (polygonShape.contains(Location.getLatLon(n - 1).asPoint())) {
                                    if (!notifUrls.contains("spcswoloc$day$locNum")) notifUrls += sendSwoNotification(context, locNum, day, threat, validTime)
                                }
                            }
                        }
                    }
                } // end loop over polygons for specific day
            } // end loop over treat level
        } // end loop of day 1-3
        return notifUrls
    }

    fun sendMcdLocationNotifications(context: Context): String {
        val textMcd = ObjectPolygonWatch.polygonDataByType[PolygonType.MCD]!!.latLonList.value
        val textMcdNoList = ObjectPolygonWatch.polygonDataByType[PolygonType.MCD]!!.numberList.value
        var notifUrls = ""
        val items = RegExp.colon.split(textMcd)
        val mcdNumbers = RegExp.colon.split(textMcdNoList)
        items.indices.forEach { z ->
            val latLons = LatLon.parseStringToLatLons(items[z], -1.0, false)
            if (latLons.isNotEmpty()) {
                val polygonFrame = ExternalPolygon.Builder()
                latLons.forEach { latLon -> polygonFrame.addVertex(ExternalPoint(latLon)) }
                val polygonShape = polygonFrame.build()
                for (n in 1..Location.numLocations) {
                    val locNum = n.toString()
                    if (MyApplication.locations[n - 1].notificationMcd) {
                        // if location is watching for MCDs pull ib lat/lon and iterate over polygons
                        // call secondary method to send notif if required
                        if (polygonShape.contains(Location.getLatLon(n - 1).asPoint())) notifUrls += sendMcdNotification(context, locNum, mcdNumbers.safeGet(z))
                    }
                }
            }
        }
        return notifUrls
    }

    private fun sendMcdNotification(context: Context, locNum: String, mdNo: String): String {
        val locNumInt = (locNum.toIntOrNull() ?: 0) - 1
        val locLabel = Utility.readPref(context, "LOC" + locNum + "_LABEL", "")
        val requestID = UtilityTime.currentTimeMillis().toInt()
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        val locLabelStr = "($locLabel) "
        val mcdPre = UtilityDownload.getTextProduct(context, "SPCMCD$mdNo").replace("<.*?>".toRegex(), " ")
        val noMain = "$locLabelStr SPC MCD #$mdNo"
        val resultIntent = Intent(context, SpcMcdWatchShowActivity::class.java)
        val resultIntent2 = Intent(context, SpcMcdWatchShowActivity::class.java)
        val polygonType = PolygonType.MCD
        resultIntent.putExtra(SpcMcdWatchShowActivity.NUMBER, arrayOf(mdNo, "", polygonType.toString()))
        resultIntent2.putExtra(SpcMcdWatchShowActivity.NUMBER, arrayOf(mdNo, "sound", polygonType.toString()))
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(SpcMcdWatchShowActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(requestID, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val resultPendingIntent2 = PendingIntent.getActivity(context, requestID + 1, resultIntent2, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val cancelString = "spcmcdloc$mdNo$locNum"
        if (!(NotificationPreferences.alertOnlyOnce && UtilityNotificationUtils.checkToken(context, cancelString))) {
            val sound = MyApplication.locations[locNumInt].sound && !inBlackout
            val objectNotification = ObjectNotification(
                    context,
                    sound,
                    noMain,
                    mcdPre,
                    resultPendingIntent,
                    GlobalVariables.ICON_ALERT,
                    mcdPre,
                    NotificationCompat.PRIORITY_HIGH,
                    Color.YELLOW,
                    GlobalVariables.ICON_ACTION,
                    resultPendingIntent2,
                    context.resources.getString(R.string.read_aloud)
            )
            val notification = UtilityNotification.createNotificationBigTextWithAction(objectNotification)
            objectNotification.sendNotification(context, cancelString, 1, notification)
        }
        return cancelString + NotificationPreferences.notificationStrSep
    }

    private fun sendSwoNotification(context: Context, locNum: String, day: Int, threatLevel: String, validTime: String): String {
        val locNumInt = (locNum.toIntOrNull() ?: 0) - 1
        val locLabel = Utility.readPref(context, "LOC" + locNum + "_LABEL", "")
        val requestID = UtilityTime.currentTimeMillis().toInt()
        val inBlackout = UtilityNotificationUtils.checkBlackOut()
        val locLabelStr = "($locLabel) "
        val dayStr = "SWODY$day"
        val noMain = "$locLabelStr$dayStr $threatLevel"
        val detailRaw = threatLevel.replace("<.*?>".toRegex(), " ").replace("&nbsp".toRegex(), " ")
        val resultIntent = Intent(context, SpcSwoActivity::class.java)
        val resultIntent2 = Intent(context, SpcSwoActivity::class.java)
        var dayStrArg = day.toString()
        if (day > 3) dayStrArg = "4-8"
        resultIntent.putExtra(SpcSwoActivity.NUMBER, arrayOf(dayStrArg, ""))
        resultIntent2.putExtra(SpcSwoActivity.NUMBER, arrayOf(dayStrArg, "sound"))
        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(SpcSwoActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(requestID, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val resultPendingIntent2 = PendingIntent.getActivity(context, requestID + 1, resultIntent2, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val cancelString = "spcswoloc$day$locNum$threatLevel$validTime"
        if (!(NotificationPreferences.alertOnlyOnce && UtilityNotificationUtils.checkToken(context, cancelString))) {
            val sound = (MyApplication.locations.getOrNull(locNumInt)?.sound ?: false) && !inBlackout
            val objectNotification = ObjectNotification(
                    context,
                    sound,
                    noMain,
                    detailRaw,
                    resultPendingIntent,
                    GlobalVariables.ICON_ALERT,
                    detailRaw,
                    NotificationCompat.PRIORITY_HIGH,
                    Color.YELLOW,
                    GlobalVariables.ICON_ACTION,
                    resultPendingIntent2,
                    context.resources.getString(R.string.read_aloud)
            )
            val notification = UtilityNotification.createNotificationBigTextWithAction(objectNotification)
            objectNotification.sendNotification(context, cancelString, 1, notification)
        }
        return cancelString + NotificationPreferences.notificationStrSep
    }

    fun sendSwoD48LocationNotifications(context: Context): String {
        var notifUrls = ""
        val threatList = listOf("SPC30percent", "SPC15percent")
        var urlBlob = UtilityString.getHtmlAndParse("${GlobalVariables.nwsSPCwebsitePrefix}/products/exper/day4-8/", "CLICK TO GET <a href=.(.*?txt).>WUUS48 PTSD48</a>")
        urlBlob = "${GlobalVariables.nwsSPCwebsitePrefix}$urlBlob"
        var html = urlBlob.getHtmlSep()
        val validTime = html.parse("VALID TIME ([0-9]{6}Z - [0-9]{6}Z)")
        html = html.replace("<br>", " ").replace("0.30", "SPC30percent").replace("0.15", "SPC15percent")
        (4..8).forEach { day ->
            val htmlBlob = html.parse("SEVERE WEATHER OUTLOOK POINTS DAY $day(.*?&)&") // was (.*?)&&
            threatList.forEach { threat ->
                var string = ""
                val htmlList = htmlBlob.parseColumn(threat.substring(1) + "(.*?)[A-Z&]")
                htmlList.forEach {
                    string += UtilityNotification.storeWatchMcdLatLon(it)
                    string = string.replace(" 99.99 99.99 ", " ") // need for the way SPC ConvO seperates on 8 's
                } // end looping over polygons of one threat level
                val items = string.split(":")
                items.indices.forEach { z ->
                    val latLons = LatLon.parseStringToLatLons(items[z], -1.0, false)
                    // inject bounding box coordinates if first doesn't equal last
                    // focus on east coast for now
                    // 52,-130               52,-62
                    // 21,-130                21,-62
                    if (latLons.isNotEmpty()) {
                        val polygonFrame = ExternalPolygon.Builder()
                        latLons.forEach { latLon -> polygonFrame.addVertex(ExternalPoint(latLon)) }
                        val polygonShape = polygonFrame.build()
                        (1..Location.numLocations).forEach { n ->
                            val locNum = n.toString()
                            if (MyApplication.locations[n - 1].notificationSwo) {
                                // if location is watching for MCDs pull ib lat/lon and iterate over polygons
                                // call secondary method to send notif if required
                                if (polygonShape.contains(Location.getLatLon(n - 1).asPoint())) {
                                    if (!notifUrls.contains("spcswoloc$day$locNum")) notifUrls += sendSwoNotification(context, locNum, day, threat, validTime)
                                }
                            }
                        }
                    }
                } // end loop over polygons for specific day
            } // end loop over treat level
        } // end loop of day 1-3
        return notifUrls
    }
}

/*
 *
 *http://www.spc.noaa.gov/products/outlook/archive/2015/KWNSPTSDY1_201504080100.txt


 DAY 1 CONVECTIVE OUTLOOK AREAL OUTLINE
NWS STORM PREDICTION CENTER NORMAN OK
0759 PM CDT TUE APR 07 2015

VALID TIME 080100Z - 081200Z

PROBABILISTIC OUTLOOK POINTS DAY 1

... TORNADO ...

0.02   36979679 37759596 38579520 39679442 39679330 39209253
       38089272 37389343 36909444 36299609 36519660 36979679
0.05   37009653 38409511 39079454 39089374 38859327 38369319
       37639377 37069472 36549598 36679629 37009653
&&

... HAIL ...

0.05   36309788 38169601 39459500 40319463 40259307 39349122
       38359031 37749012 37179037 37239175 36909359 36099606
       36019710 36119766 36309788
0.05   38632211 38882165 38812094 37421974 36471885 36101938
       36202014 37092099 38632211
0.05   36778614 36858480 37518340 38328213 37798124 37018118
       36418169 36078244 35818385 35858518 36138622 36498643
       36778614
0.15   38799284 38169302 37129433 36359594 36419669 36699709
       37969574 38669510 39499444 39519373 38799284
SIGN   38259395 37719405 37409450 36959511 36349594 36349668
       36699712 37779579 38279527 38759472 38679426 38259395
&&

... WIND ...

0.05   36339785 38199598 39509498 40319462 40249302 39359121
       38339032 37749011 37199036 37219179 36809359 36089607
       36049713 36119768 36339785
0.05   34988154 34578037 34148026 33608059 33458131 33678172
       34138191 34698195 34988154
0.05   36738616 36878478 37478350 38298209 37778127 37038113
       36608161 36048252 35828395 35868523 36118617 36468644
       36738616
0.15   36999669 38429530 39499443 39519374 38809285 38159300
       37129431 36569548 36709664 36999669
&&

CATEGORICAL OUTLOOK POINTS DAY 1

... CATEGORICAL ...

SLGT   38799284 38579290 38159300 37129431 36719512 36359597
       36399676 36659712 37529619 38209552 38619515 39209467
       39499444 39519374 38799284
MRGL   38812094 37421974 36471885 36101938 36202014 37092099
       38632211 38882165 38812094
MRGL   40249305 39359121 38819073 38359031 37749011 37199036
       37209107 37219179 36919310 36759380 36419498 36089607
       36079630 36019710 36099755 36309788 37469673 38179600
       38529573 39499498 40319463 40259307 40249305
MRGL   34988154 34598044 34178030 33638059 33418127 33658178
       34158189 34698197 34988154
MRGL   37768122 37028114 36388177 36028263 35818394 35838509
       36128622 36478640 36808609 36838478 37648325 38308209
       37768122
TSTM   47352510 46352247 45242096 44102032 43271901 43111661
       44141272 44220909 43910760 43010697 41570848 41350993
       41001321 39691586 37991734 36691729 34951724 33611767
       32271899 99999999 28990364 30340173 30530063 30509974
       29580002 28500060 99999999 29028319 32258440 34668606
       35328744 35839000 35999126 35779379 35399610 34949755
       35139823 36119851 38449621 39989536 42869412 44679149
       44888893 43748483 42748053 41507861 38837683 37927579
       37057444

&&
THERE IS A SLGT RISK OF SVR TSTMS TO THE RIGHT OF A LINE FROM 35 W
COU 35 ESE SZL 50 SE SZL 10 E JLN 20 WNW GMJ 10 NNW TUL 30 SE PNC
PNC 40 WSW CNU 35 N CNU 30 SW OJC MKC 25 ENE FLV 25 SSW CDJ 35 W
COU.

THERE IS A MRGL RISK OF SVR TSTMS TO THE RIGHT OF A LINE FROM 35 ENE
SAC 45 ENE MER 55 ESE FAT 50 NNW BFL 45 NE PRB 30 WSW MER 35 WNW SAC
25 NNW SAC 35 ENE SAC.

THERE IS A MRGL RISK OF SVR TSTMS TO THE RIGHT OF A LINE FROM 30 WNW
IRK 40 S UIN 20 WNW STL 30 S STL 20 E FAM 30 NNE POF 45 NW POF 30 N
UNO 25 SE SGF 10 SSE UMN 20 SW GMJ 15 SW TUL 25 WSW TUL 25 NW CQB 25
SE END END 40 ESE ICT 15 SE EMP 25 ENE EMP 10 NNW FLV 40 NNE STJ 30
WNW IRK 30 WNW IRK.

THERE IS A MRGL RISK OF SVR TSTMS TO THE RIGHT OF A LINE FROM 25 E
SPA 50 NW FLO 35 W FLO 20 NE OGB 25 W OGB 25 NNE AGS 45 WNW CAE 15 S
SPA 25 E SPA.

THERE IS A MRGL RISK OF SVR TSTMS TO THE RIGHT OF A LINE FROM BKW 20
S BLF 35 E TRI 15 NE HSS TYS 10 S CSV 25 E BNA 30 NNE BNA 20 ESE BWG
40 WSW LOZ 10 NE JKL 25 E HTS BKW.

GEN TSTMS ARE FCST TO THE RIGHT OF A LINE FROM 45 SSW UIL 45 SSE OLM
30 SSE DLS 45 ESE RDM 25 S BNO 35 SSW BOI 35 SW MQM 20 S COD 20 ESE
WRL 25 WNW CPR 30 E RKS 50 WSW RKS 45 ENE ENV 15 NNE P68 15 WSW TPH
70 W DRA 25 WNW DAG 25 SSW RAL 110 WSW SAN ...CONT... 100 SSE MRF 35
NE 6R6 50 W JCT JCT 55 WNW HDO 65 SSE DRT ...CONT... 45 S CTY 35 ESE
CSG 40 E HSV 40 NNE MSL 35 WSW DYR 20 WSW ARG 25 SE FYV 40 NNW MLC
25 ESE CHK 15 W CHK 35 WSW END 10 N EMP 15 ESE FNB 20 N FOD 15 S EAU
35 E AUW 40 WNW MBS 50 NNW ERI 20 S BFD 10 E DCA 20 W WAL 85 SE WAL.

 */


/*http://www.spc.noaa.gov/products/exper/day4-8/archive/2015/KWNSPTSD48_20150406.txt



DAY 4-8 CONVECTIVE OUTLOOK AREAL OUTLINE
NWS STORM PREDICTION CENTER NORMAN OK
0401 AM CDT MON APR 06 2015

VALID TIME 091200Z - 141200Z

SEVERE WEATHER OUTLOOK POINTS DAY 4  

... ANY SEVERE ...

0.15   31959336 30699643 31589790 34369626 37119373 39279256
       42309164 42998737 42758582 41158502 38618533 35848745
       33219085 31959336
0.30   41788932 41318746 39148834 37438991 36889170 37829212
       41029099 41788932
&&

SEVERE WEATHER OUTLOOK POINTS DAY 5  

... ANY SEVERE ...

0.15   39707546 38557507 36467643 36157675 33548094 32518301
       33028418 35268350 36808070 40277732 39707546
&&

SEVERE WEATHER OUTLOOK POINTS DAY 6  

... ANY SEVERE ...

&&

SEVERE WEATHER OUTLOOK POINTS DAY 7  

... ANY SEVERE ...

&&

SEVERE WEATHER OUTLOOK POINTS DAY 8  

... ANY SEVERE ...

&&*/

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
//Modded by ELY M.

package joshuatee.wx.activitiesmisc

import android.content.Context
import joshuatee.wx.Extensions.parseColumn
import joshuatee.wx.MyApplication
import joshuatee.wx.RegExp
import joshuatee.wx.external.ExternalDuplicateRemover
import joshuatee.wx.objects.PolygonType
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.util.UtilityString
import java.util.regex.Matcher

//parses specical weather statements
//only get SPS with Lat Lons in them.


internal class SpecialWeather(private val type: PolygonType) {

    var text = ""
        private set
    var count = 0
        private set



    fun generateSpsString(context: Context, spsText: String) {
        var label = ""
        when (type) {
            PolygonType.SPS -> label = "Special Weather Statement"
            else -> {
            }
        }
        UtilityLog.d("SpecialWeather", "spsText: "+spsText)

        /*
             "senderName": "NWS Huntsville AL",
            "headline": "Special Weather Statement issued December 31 at 4:40PM CST by NWS Huntsville AL",
            "description": "At 440 PM CST, Doppler radar was tracking strong thunderstorms along\na line extending from near Woodville to near Arab. Movement was east\nat 25 mph.\n\nWinds in excess of 40 mph will be possible with these storms.\n\nLocations impacted include...\nAlbertville, Boaz, Guntersville, Arab, Grant, Douglas, Union Grove,\nHigh Point, Horton and Columbus City.",
            "instruction": "Strong gusty winds will be capable of knocking down small tree limbs\nalong with other small unsecured items. Seek safe shelter until this\nline of storms has passed.\n\nVery heavy rainfall is also occurring with these storms, and may lead\nto localized flooding. Do not drive your vehicle through flooded\nroadways.",
            "response": "Execute",
            "parameters": {
                "eventMotionDescription": [
                    "2018-12-31T16:40:00.000-06:00...storm...271DEG...23KT...34.58,-86.26 34.28,-86.53"
                ],
                "NWSheadline": [
                    "SIGNIFICANT WEATHER ADVISORY FOR MARSHALL COUNTY UNTIL 515 PM CST"
                ],



 */

        val urlList = spsText.parseColumn("\"id\"\\: .(https://api.weather.gov/alerts/NWS-IDP-.*?)\"")
        UtilityLog.d("SpecialWeather", "urllist: "+urlList)

        val sendername = spsText.parseColumn(RegExp.spsSenderName)
        UtilityLog.d("SpecialWeather", "sendername: "+sendername)

        val headline = spsText.parseColumn(RegExp.spsHeadLine)
        UtilityLog.d("SpecialWeather", "headline: "+headline)

        val description = spsText.parseColumn(RegExp.spsDescription)
        UtilityLog.d("SpecialWeather", "description: "+description)

        val instruction = spsText.parseColumn(RegExp.spsInstruction)
        UtilityLog.d("SpecialWeather", "instruction: "+instruction)

        //val polygonArr = spsText.parseColumn(RegExp.warningLatLonPattern)

        val spsLatLon = spsText.parseColumn(RegExp.spsLatLonPattern)
        UtilityLog.d("SpecialWeather", "spsLatLon: "+spsLatLon)


        count = urlList.size
        sendername.forEach {
            UtilityLog.d("SpecialWeather", "sendname: "+it)
        }

        urlList.forEach {
            UtilityLog.d("SpecialWeather", "urlList: "+it)
        }



        val remover = ExternalDuplicateRemover()
        text = remover.stripDuplicates(text)
        text = "(" + text.split(MyApplication.newline).dropLastWhile { it.isEmpty() }.size +
                ") " + label + MyApplication.newline +
                text.replace((MyApplication.newline + "$").toRegex(), "")




    }



    /*




               try {
                dataAsString = "${MyApplication.nwsWPCwebsitePrefix}/metwatch/metwatch_mpd.php".getHtml()
                MyApplication.severeDashboardMpd.valueSet(context, dataAsString)
                if (MyApplication.alertWpcmpdNotificationCurrent || PolygonType.MPD.pref || locationNeedsWpcmpd) {
                    // FIXME matcher
                    m = RegExp.mpdPattern.matcher(dataAsString)
                    var mdNo: String
                    while (m.find()) {
                        mdNo = m.group(1)
                        var mcdPre = UtilityDownload.getTextProduct(context, "WPCMPD$mdNo")
                        if (PolygonType.MPD.pref || locationNeedsWpcmpd) {
                            mpdNoList = "$mpdNoList$mdNo:"
                            mpdLatlon += UtilityNotification.storeWatMCDLATLON(mcdPre)
                        }
                        if (MyApplication.alertWpcmpdNotificationCurrent) {
                            noMain = "WPC MPD #$mdNo"
                            mcdPre = mcdPre.replace("<.*?>".toRegex(), " ")
                            noBody = mcdPre
                            noSummary = mcdPre
                            val polygonType = MPD
                            val objPI = ObjectPendingIntents(context, SPCMCDWShowActivity::class.java, SPCMCDWShowActivity.NO,
                                    arrayOf(mdNo, "", polygonType.toString()), arrayOf(mdNo, "sound", polygonType.toString()))
                            cancelStr = "uswpcmpd$mdNo"
                            if (!(MyApplication.alertOnlyonce && UtilityNotificationUtils.checkToken(context, cancelStr))) {
                                val sound = MyApplication.alertNotificationSoundWpcmpd && !inBlackout
                                val notifObj = ObjectNotification(context, sound, noMain, noBody, objPI.resultPendingIntent,
                                        MyApplication.ICON_MPD, noSummary, Notification.PRIORITY_DEFAULT, Color.GREEN,
                                        MyApplication.ICON_ACTION, objPI.resultPendingIntent2, context.resources.getString(R.string.read_aloud))
                                val noti = UtilityNotification.createNotifBigTextWithAction(notifObj)
                                notifObj.sendNotification(context, cancelStr, 1, noti)
                                //notifier.notify(cancelStr, 1, noti)
                            }
                            notifUrls += cancelStr + MyApplication.notificationStrSep
                        }

                    } // end while find
                }
            } catch (e: Exception) {
                UtilityLog.HandleException(e)
            }





    */




    /*
    fun generateSpsString(context: Context, spsText: String) {
        var nwsOfficeArr: List<String>
        var nwsOffice: String
        var nwsLoc = ""
        var label = ""
        when (type) {
            PolygonType.SPS -> label = "Special Weather Statement"
            else -> {
            }
        }
        UtilityLog.d("SpecialWeather", "spsText: "+spsText)
        val warningAl = spsText.parseColumn(RegExp.warningLatLonPattern)
        UtilityLog.d("SpecialWeather", "warningAl: "+warningAl)
        count = warningAl.size
        warningAl.forEach {
            text += it
            nwsOfficeArr = it.split(".")
            if (nwsOfficeArr.size > 1) {
                nwsOffice = nwsOfficeArr[2]
                nwsOffice = nwsOffice.replace("^[KP]".toRegex(), "")
                nwsLoc = Utility.readPref(context, "NWS_LOCATION_$nwsOffice", "")
            }
            text += "  " + nwsLoc + MyApplication.newline
        }
        val remover = ExternalDuplicateRemover()
        text = remover.stripDuplicates(text)
        text = "(" + text.split(MyApplication.newline).dropLastWhile { it.isEmpty() }.size +
                ") " + label + MyApplication.newline +
                text.replace((MyApplication.newline + "$").toRegex(), "")


        UtilityLog.d("SpecialWeather", "text: "+text)



    }
    */



    private fun storeLATLON(html: String): String {
        val coords = html.parseColumn("([0-9]{4}).*?")
        var retStr = ""
        var xStrTmp: String
        var yStrTmp: String
        for (temp in coords) {
            xStrTmp = temp.substring(0, 2)
            yStrTmp = temp.substring(2, 4)
            if (yStrTmp.matches("^0".toRegex())) {
                yStrTmp = yStrTmp.replace("^0".toRegex(), "")
                yStrTmp += "0"
            }
            xStrTmp = UtilityString.addPeriodBeforeLastTwoChars(xStrTmp)
            yStrTmp = UtilityString.addPeriodBeforeLastTwoChars(yStrTmp)
            try {
                var tmpDbl = yStrTmp.toDoubleOrNull() ?: 0.0
                if (tmpDbl < 40.00) {
                    tmpDbl += 100
                    yStrTmp = tmpDbl.toString()
                }
            } catch (e: Exception) {
                UtilityLog.HandleException(e)
            }
            retStr = "$retStr$xStrTmp $yStrTmp "
        }
        retStr += ":"
        retStr = retStr.replace(" :", ":")
        return retStr
    }



}







/*
internal class SpecialWeather(private val type: PolygonType) {

    var TAG = "SpecialWeather"
    var text = ""
    var label = ""
        private set
    var count = 0
        private set

    init {
        when (type) {
            PolygonType.SPS -> label = "SPS"
            else -> {
            }
        }


        UtilityLog.d(TAG, "Trying to parse SPS......\n")
    }


    fun generateString(context: Context, textSps: String) {
        var label = ""
        when (type) {
            PolygonType.SPS -> label = "Special Weather Statement"
            else -> {
            }
        }


        UtilityLog.d(TAG, "textsps: "+textSps+"\n");
        //"eventMotionDescription": [
        // "2018-09-23T17:19:00.000-04:00...storm...249DEG...13KT...37.13,-81.47"
        //"id": "https://api.weather.gov/alerts/NWS-IDP-PROD-3217762"
        //FIXME not all warnings/statments have vtec//
        //"id": "https://api.weather.gov/alerts/NWS-IDP-PROD-3217762"

        var SPSText = MyApplication.severeDashboardSps.valueGet()
        val urlList = SPSText.parseColumn("\"id\"\\: .(https://api.weather.gov/alerts/NWS-IDP-.*?)\"")
        SPSText = SPSText.replace("\n", "")
        SPSText = SPSText.replace(" ", "")
        val polygonArr = SPSText.parseColumn(RegExp.warningLatLonPattern)

        UtilityLog.d(TAG, "urllist: "+urlList)
        val warningAl = textSps.parseColumn(RegExp.warningLatLonPattern)
        //Log.i(TAG, "warningAl: "+warningAl)

        count = polygonArr.size

        urlList.forEach {
            UtilityLog.d(TAG, "foreach it: "+it)
            //text += it
            UtilityLog.d(TAG, "for each text: "+text)

        }

        polygonArr.forEach {
            UtilityLog.d(TAG, "polygonArr it: "+it)
            //text += it
            MyApplication.SPSLatlon.valueSet(context, it)
        }

        val spsremover = ExternalDuplicateRemover()
        text = spsremover.stripDuplicates(text)
        text = "(" + text.split(MyApplication.newline).dropLastWhile { it.isEmpty() }.size + ") " + label + MyApplication.newline + text.replace((MyApplication.newline + "$").toRegex(), "")


        count++
        UtilityLog.d(TAG, "spscount: "+count+"\n");
        val remover = ExternalDuplicateRemover()
        text = remover.stripDuplicates(text)
        text = "(" + text.split(MyApplication.newline).dropLastWhile { it.isEmpty() }.size + ") " + label + MyApplication.newline + text.replace((MyApplication.newline + "$").toRegex(), "")
        UtilityLog.d(TAG, "text: "+text+"\n");


    }

}

*/


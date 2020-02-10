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

package joshuatee.wx.activitiesmisc

import joshuatee.wx.util.UtilityDownloadNws
import joshuatee.wx.Extensions.*
import joshuatee.wx.UIPreferences

class CapAlert {

    var text: String = ""
        private set
    var title: String = ""
        private set
    var summary: String = ""
        private set
    var area: String = ""
        private set
    var instructions: String = ""
        private set
    var zones: String = ""
    var vtec: String = ""
    var url: String = ""
        private set
    var event: String = ""
    var effective: String  = ""
    var expires: String  = ""

    companion object {

        // used by usAlerts
        fun initializeFromCap(eventText: String): CapAlert {
            val capAlert = CapAlert()
            capAlert.url = eventText.parse("<id>(.*?)</id>")
            capAlert.title  = eventText.parse("<title>(.*?)</title>")
            capAlert.summary = eventText.parse("<summary>(.*?)</summary>")
            capAlert.instructions = eventText.parse("</description>.*?<instruction>(.*?)</instruction>.*?<areaDesc>")
            capAlert.area = eventText.parse("<cap:areaDesc>(.*?)</cap:areaDesc>")
            capAlert.area = capAlert.area.replace("&apos;", "'")
            capAlert.effective = eventText.parse("<cap:effective>(.*?)</cap:effective>")
            capAlert.expires = eventText.parse("<cap:expires>(.*?)</cap:expires>")
            capAlert.event = eventText.parse("<cap:event>(.*?)</cap:event>")
            capAlert.vtec = eventText.parse("<valueName>VTEC</valueName>.*?<value>(.*?)</value>")
            capAlert.zones = eventText.parse("<valueName>UGC</valueName>.*?<value>(.*?)</value>")
            capAlert.text = "<h4><b>"
            capAlert.text += capAlert.title
            capAlert.text += "</b></h4>"
            capAlert.text += "<b>Counties: "
            capAlert.text += capAlert.area
            capAlert.text += "</b><br><br>"
            capAlert.text += capAlert.summary
            capAlert.text += "<br><br><br>"
            capAlert.text += capAlert.instructions
            capAlert.text += "<br><br><br>"
            capAlert.summary = capAlert.summary.replace("<br>\\*", "<br><br>*")
            if (UIPreferences.nwsTextRemovelinebreaks) {
                capAlert.instructions = capAlert.instructions.replace("<br><br>", "<BR><BR>").replace("<br>", " ")
            }
            return capAlert
        }

        fun createFromUrl(url: String): CapAlert {
            val expireStr = "This alert has expired"
            val capAlert = CapAlert()
            capAlert.url = url
            val html = if (url.contains("NWS-IDP-PROD")) {
                UtilityDownloadNws.getStringFromUrlSep(url)
            } else {
                url.getHtmlSep()
            }
            if (!html.contains("NWS-IDP-PROD")) {
                if (html.contains(expireStr)) {
                    capAlert.text = expireStr
                } else {
                    capAlert.title = html.parse("<headline>(.+?)</headline>.*?<description>")
                    capAlert.summary = html.parse("</headline>.*?<description>(.*?)</description>.*?<instruction>")
                    capAlert.instructions = html.parse("</description>.*?<instruction>(.*?)</instruction>.*?<areaDesc>")
                    capAlert.area = html.parse("</instruction>.*?<areaDesc>(.*?)</areaDesc>.*?")
                    capAlert.area = capAlert.area.replace("&apos;", "'")
                    capAlert.text = "<h4><b>"
                    capAlert.text += capAlert.title
                    capAlert.text += "</b></h4>"
                    capAlert.text += "<b>Counties: "
                    capAlert.text += capAlert.area
                    capAlert.text += "</b><br><br>"
                    capAlert.text += capAlert.summary
                    capAlert.text += "<br><br><br>"
                    capAlert.text += capAlert.instructions
                    capAlert.text += "<br><br><br>"
                }
            } else {
                capAlert.title = html.parse("\"headline\": \"(.*?)\"")
                capAlert.summary = html.parse("\"description\": \"(.*?)\"")
                capAlert.instructions = html.parse("\"instruction\": \"(.*?)\"")
                capAlert.area = html.parse("\"areaDesc\": \"(.*?)\"")
                capAlert.summary = capAlert.summary.replace("\\n\\n", "ABC123")
                capAlert.summary = capAlert.summary.replace("\\n", " ")
                capAlert.summary = capAlert.summary.replace("ABC123", "\n\n")
                capAlert.instructions = capAlert.instructions.replace("\\n", " ")
                capAlert.text = "<h4><b>"
                capAlert.text += capAlert.title
                capAlert.text += "</b></h4>"
                capAlert.text += "<b>Counties: "
                capAlert.text += capAlert.area
                capAlert.text += "</b><br><br>"
                capAlert.text += capAlert.summary
                capAlert.text += "<br><br><br>"
                capAlert.text += capAlert.instructions
                capAlert.text += "<br><br><br>"
            }
            capAlert.summary = capAlert.summary.replace("<br>\\*".toRegex(), "<br><br>*")
            if (UIPreferences.nwsTextRemovelinebreaks) {
                capAlert.instructions = capAlert.instructions.replace("<br><br>", "<BR><BR>")
                capAlert.instructions = capAlert.instructions.replace("<br>", " ")
            }
            return capAlert
        }
    }
}



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

    /*constructor(
        url: String,
        title: String,
        event: String,
        area: String,
        zones: String,
        vtec: String
    ) {
        this.url = url
        this.title = title
        this.event = event
        this.area = area
        this.zones = zones
        this.vtec = vtec
    }*/

    companion object {

        // used by usAlerts
        fun initializeFromCap(eventText: String): CapAlert {
            val obj = CapAlert()
            obj.url = eventText.parse("<id>(.*?)</id>")
            obj.title  = eventText.parse("<title>(.*?)</title>")
            obj.summary = eventText.parse("<summary>(.*?)</summary>")
            obj.instructions = eventText.parse("</description>.*?<instruction>(.*?)</instruction>.*?<areaDesc>")
            obj.area = eventText.parse("<cap:areaDesc>(.*?)</cap:areaDesc>")
            obj.area = obj.area.replace("&apos;", "'")
            obj.effective = eventText.parse("<cap:effective>(.*?)</cap:effective>")
            obj.expires = eventText.parse("<cap:expires>(.*?)</cap:expires>")
            obj.event = eventText.parse("<cap:event>(.*?)</cap:event>")
            obj.vtec = eventText.parse("<valueName>VTEC</valueName>.*?<value>(.*?)</value>")
            obj.zones = eventText.parse("<valueName>UGC</valueName>.*?<value>(.*?)</value>")
            obj.text = "<h4><b>"
            obj.text += obj.title
            obj.text += "</b></h4>"
            obj.text += "<b>Counties: "
            obj.text += obj.area
            obj.text += "</b><br><br>"
            obj.text += obj.summary
            obj.text += "<br><br><br>"
            obj.text += obj.instructions
            obj.text += "<br><br><br>"
            obj.summary = obj.summary.replace("<br>\\*", "<br><br>*")
            if (UIPreferences.nwsTextRemovelinebreaks) {
                obj.instructions = obj.instructions.replace("<br><br>", "<BR><BR>").replace("<br>", " ")
            }
            return obj
        }

        fun createFromUrl(url: String): CapAlert {
            val expireStr = "This alert has expired"
            val obj = CapAlert()
            obj.url = url
            val html = if (url.contains("NWS-IDP-PROD")) {
                UtilityDownloadNws.getStringFromUrlSep(url)
            } else {
                url.getHtmlSep()
            }
            if (!html.contains("NWS-IDP-PROD")) {
                if (html.contains(expireStr)) {
                    obj.text = expireStr
                } else {
                    obj.title = html.parse("<headline>(.+?)</headline>.*?<description>")
                    obj.summary =
                        html.parse("</headline>.*?<description>(.*?)</description>.*?<instruction>")
                    obj.instructions =
                        html.parse("</description>.*?<instruction>(.*?)</instruction>.*?<areaDesc>")
                    obj.area = html.parse("</instruction>.*?<areaDesc>(.*?)</areaDesc>.*?")
                    obj.area = obj.area.replace("&apos;", "'")
                    obj.text = "<h4><b>"
                    obj.text += obj.title
                    obj.text += "</b></h4>"
                    obj.text += "<b>Counties: "
                    obj.text += obj.area
                    obj.text += "</b><br><br>"
                    obj.text += obj.summary
                    obj.text += "<br><br><br>"
                    obj.text += obj.instructions
                    obj.text += "<br><br><br>"
                }
            } else {
                obj.title = html.parse("\"headline\": \"(.*?)\"")
                obj.summary = html.parse("\"description\": \"(.*?)\"")
                obj.instructions = html.parse("\"instruction\": \"(.*?)\"")
                obj.area = html.parse("\"areaDesc\": \"(.*?)\"")
                //obj.summary = obj.summary.replace("\\n", "\n")
                obj.summary = obj.summary.replace("\\n\\n", "ABC123")
                obj.summary = obj.summary.replace("\\n", " ")
                obj.summary = obj.summary.replace("ABC123", "\n\n")
                //obj.instructions = obj.instructions.replace("\\n", "\n")
                obj.instructions = obj.instructions.replace("\\n", " ")
                obj.text = "<h4><b>"
                obj.text += obj.title
                obj.text += "</b></h4>"
                obj.text += "<b>Counties: "
                obj.text += obj.area
                obj.text += "</b><br><br>"
                obj.text += obj.summary
                obj.text += "<br><br><br>"
                obj.text += obj.instructions
                obj.text += "<br><br><br>"
            }
            obj.summary = obj.summary.replace("<br>\\*".toRegex(), "<br><br>*")
            if (UIPreferences.nwsTextRemovelinebreaks) {
                obj.instructions = obj.instructions.replace("<br><br>", "<BR><BR>")
                obj.instructions = obj.instructions.replace("<br>", " ")
            }

            return obj
        }
    }
}



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

import java.util.regex.Pattern

import joshuatee.wx.Extensions.*
import joshuatee.wx.MyApplication
import joshuatee.wx.RegExp
import java.util.*

object UtilityString {

    fun extractPre(html: String): String {
        val separator = "ABC123E"
        val htmlOneLine = html.replace(MyApplication.newline, separator)
        val parsedText = htmlOneLine.parse(MyApplication.pre2Pattern)
        return parsedText.replace(separator, MyApplication.newline)
    }

    fun extractPreLsr(html: String): String {
        val separator = "ABC123E"
        val htmlOneLine = html.replace(MyApplication.newline, separator)
        val parsedText = htmlOneLine.parse(RegExp.prePattern)
        return parsedText.replace(separator, MyApplication.newline)
    }

    internal fun capitalizeString(s: String): String {
        val chars = s.lowercase(Locale.US).toCharArray()
        var found = false
        if (chars.size > 3) {
            (2 until chars.size).forEach {
                if (!found && Character.isLetter(chars[it])) {
                    chars[it] = Character.toUpperCase(chars[it])
                    found = true
                } else if (Character.isWhitespace(chars[it]) || chars[it] == '.' || chars[it] == '\'') {
                    found = false
                }
            }
            chars[0] = Character.toUpperCase(chars[0])
            chars[1] = Character.toUpperCase(chars[1])
        }
        return String(chars)
    }

    fun getHtmlAndParse(url: String, match: String) = url.getHtml().parse(match)

    fun getHtmlAndParseLastMatch(url: String, match: String) = url.getHtml().parseLastMatch(match)

    fun getHtmlAndParseLastMatch(url: String, pattern: Pattern) = url.getHtml().parseLastMatch(pattern)

    fun parseLastMatch(str: String, pattern: Pattern): String {
        var content = ""
        try {
            val m = pattern.matcher(str)
            while (m.find()) content = m.group(1)!!
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return content
    }

    fun parseLastMatch(string: String, match: String): String {
        var content = ""
        try {
            val p = Pattern.compile(match)
            val m = p.matcher(string)
            while (m.find()) content = m.group(1)!!
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return content
    }

    fun parse(data: String, match: String): String {
        var content = ""
        try {
            val p = Pattern.compile(match)
            val m = p.matcher(data)
            if (m.find()) content = m.group(1)!!
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return content
    }

    internal fun parseAcrossLines(data: String, match: String): String {
        var content = ""
        try {
            val pattern = Pattern.compile(match, Pattern.DOTALL)
            val m = pattern.matcher(data)
            if (m.find()) content = m.group(1)!!
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return content
    }

    fun parse(data: String, pattern: Pattern): String {
        var content = ""
        try {
            val m = pattern.matcher(data)
            if (m.find()) content = m.group(1)!!
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return content
    }

    fun getHtmlAndParseSep(url: String, match: String) = url.getHtmlSep().parse(match)

    internal fun getHtmlAndParseSep(url: String, pattern: Pattern) = url.getHtmlSep().parse(pattern)

    fun parseMultiple(data: String, match: String, number: Int): MutableList<String> {
        val result = MutableList(number) { "" }
        try {
            val pattern = Pattern.compile(match)
            val m = pattern.matcher(data)
            while (m.find()) {
                (0 until number).forEach { result[it] = m.group(it + 1)!! }
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return result
    }

    fun parseMultiple(data: String, pattern: Pattern, number: Int): MutableList<String> {
        val result = MutableList(number) { "" }
        try {
            val m = pattern.matcher(data)
            while (m.find()) {
                (0 until number).forEach { result[it] = m.group(it + 1)!! }
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return result
    }

    fun parseColumn(data: String, match: String): List<String> {
        val result = mutableListOf<String>()
        try {
            val pattern = Pattern.compile(match)
            val m = pattern.matcher(data)
            while (m.find()) result.add(m.group(1)!!)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return result
    }

    fun parseColumnMutable(data: String, match: String): MutableList<String> {
        val result = mutableListOf<String>()
        try {
            val pattern = Pattern.compile(match)
            val m = pattern.matcher(data)
            while (m.find()) result.add(m.group(1)!!)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return result
    }

    fun parseColumnMutable(data: String, pattern: Pattern): MutableList<String> {
        val result = mutableListOf<String>()
        try {
            val m = pattern.matcher(data)
            while (m.find()) result.add(m.group(1)!!)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return result
    }

    fun parseColumn(data: String, pattern: Pattern): List<String> {
        val result = mutableListOf<String>()
        try {
            val m = pattern.matcher(data)
            while (m.find()) result.add(m.group(1)!!)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return result
    }

    fun parseColumnAl(data: String, pattern: Pattern): MutableList<String> {
        val result = mutableListOf<String>()
        try {
            val m = pattern.matcher(data)
            while (m.find()) result.add(m.group(1)!!)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return result
    }

    fun parseColumnAll(data: String, pattern: Pattern): List<String> {
        val result = mutableListOf<String>()
        try {
            val m = pattern.matcher(data)
            while (m.find()) result.add(m.group())
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return result
    }

    fun parseAndCount(data: String, match: String): Int {
        var i = 0
        try {
            val p = Pattern.compile(match)
            val m = p.matcher(data)
            while (m.find()) i += 1
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return i
    }

    //fun getNwsPre(url: String) = url.getHtmlSep().parse(RegExp.pre2Pattern)

    fun getLastXChars(s: String, x: Int) = when {
        s.length == x -> s
        s.length > x -> s.substring(s.length - x)
        else -> s
    }

    fun addPeriodBeforeLastTwoChars(string: String) = StringBuilder(string).insert(string.length - 2, ".").toString()

    fun replaceAllRegexp(s: String, a: String, b: String): String {
        return s.replace(Regex(a), b)
    }

    //
    // Legacy forecast support
    //
    fun parseXml(payloadF: String?, delimF: String?): Array<String> {
        var payload = payloadF
        var delim = delimF
        if ( payloadF == null ) {
            payload = ""
        }
        if ( delimF == null ) {
            delim = ""
        }
        if (delim == "start-valid-time") {
            payload = payload!!.replace( "<end-valid-time>.*?</end-valid-time>".toRegex() , "").replace( "<layout-key>.*?</layout-key>".toRegex() , "")
        }
        payload = payload!!.replace( "<name>.*?</name>".toRegex() , "").replace( "</" + delim + ">".toRegex() , "")
        return payload.split("<$delim>").toTypedArray()
    }

    fun parseXmlValue(payloadF: String?): Array<String> {
        var payload = payloadF
        if (payload == null ) {
            payload = ""
        }
        payload = payload.replace("<name>.*?</name>".toRegex() , "").replace( "</value>" , "")
        return MyApplication.xml_value_pattern.split(payload)
    }

    fun parseXmlExt (regexpList: Array<String>, html: String): Array<String> {
        val items = Array(regexpList.size) {""}
        var p: Pattern
        // var m: Matcher
        for (i in regexpList.indices) {
            try {
                p = Pattern.compile(regexpList[i], Pattern.DOTALL)
                val m = p.matcher(html)
                while (m.find()) {
                    items[i] = m.group(1)
                }
            } catch (e: Exception) {
            }
        }
        return items
    }
}

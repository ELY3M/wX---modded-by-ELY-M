/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024  joshua.tee@gmail.com

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
import joshuatee.wx.common.RegExp
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.getHtml
import joshuatee.wx.parse
import joshuatee.wx.parseLastMatch
import java.util.Locale

object UtilityString {

    fun extractPre(html: String): String {
        val separator = "ABC123E"
        val htmlOneLine = html.replace(GlobalVariables.newline, separator)
        val parsedText = htmlOneLine.parse(GlobalVariables.pre2Pattern)
        return parsedText.replace(separator, GlobalVariables.newline)
    }

    fun extractPreLsr(html: String): String {
        val separator = "ABC123E"
        val htmlOneLine = html.replace(GlobalVariables.newline, separator)
        val parsedText = htmlOneLine.parse(RegExp.prePattern)
        return parsedText.replace(separator, GlobalVariables.newline)
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

    fun getHtmlAndParseLastMatch(url: String, match: String): String =
            url.getHtml().parseLastMatch(match)

    fun getHtmlAndParseLastMatch(url: String, pattern: Pattern): String =
            url.getHtml().parseLastMatch(pattern)

    fun parseLastMatch(s: String, pattern: Pattern): String = try {
        var content = ""
        val m = pattern.matcher(s)
        while (m.find()) {
            content = m.group(1)!!
        }
        content
    } catch (e: Exception) {
        UtilityLog.handleException(e)
        ""
    }

    fun parseLastMatch(s: String, match: String): String = try {
        var content = ""
        val p = Pattern.compile(match)
        val m = p.matcher(s)
        while (m.find()) {
            content = m.group(1)!!
        }
        content
    } catch (e: Exception) {
        UtilityLog.handleException(e)
        ""
    }

    fun parse(s: String, match: String): String = try {
        var content = ""
        val p = Pattern.compile(match)
        val m = p.matcher(s)
        if (m.find()) {
            content = m.group(1)!!
        }
        content
    } catch (e: Exception) {
        UtilityLog.handleException(e)
        ""
    }

    internal fun parseAcrossLines(s: String, match: String): String = try {
        var content = ""
        val pattern = Pattern.compile(match, Pattern.DOTALL)
        val m = pattern.matcher(s)
        if (m.find()) {
            content = m.group(1)!!
        }
        content
    } catch (e: Exception) {
        UtilityLog.handleException(e)
        ""
    }

    fun parse(s: String, pattern: Pattern): String = try {
        var content = ""
        val m = pattern.matcher(s)
        if (m.find()) {
            content = m.group(1)!!
        }
        content
    } catch (e: Exception) {
        UtilityLog.handleException(e)
        ""
    }

    fun parseMultiple(s: String, match: String, number: Int): List<String> = try {
        val result = MutableList(number) { "" }
        val pattern = Pattern.compile(match)
        val m = pattern.matcher(s)
        while (m.find()) {
            (0 until number).forEach {
                result[it] = m.group(it + 1)!!
            }
        }
        result
    } catch (e: Exception) {
        UtilityLog.handleException(e)
        List(number) { "" }
    }

    fun parseMultiple(s: String, pattern: Pattern, number: Int): List<String> = try {
        val result = MutableList(number) { "" }
        val m = pattern.matcher(s)
        while (m.find()) {
            (0 until number).forEach {
                result[it] = m.group(it + 1)!!
            }
        }
        result
    } catch (e: Exception) {
        UtilityLog.handleException(e)
        List(number) { "" }
    }

    fun parseColumn(s: String, match: String): List<String> = try {
        val result = mutableListOf<String>()
        val pattern = Pattern.compile(match)
        val m = pattern.matcher(s)
        while (m.find()) {
            result.add(m.group(1)!!)
        }
        result
    } catch (e: Exception) {
        UtilityLog.handleException(e)
        emptyList()
    }

    fun parseColumn(s: String, pattern: Pattern): List<String> = try {
        val result = mutableListOf<String>()
        val m = pattern.matcher(s)
        while (m.find()) {
            result.add(m.group(1)!!)
        }
        result
    } catch (e: Exception) {
        UtilityLog.handleException(e)
        emptyList()
    }

    fun parseColumnAcrossLines(s: String, match: String): List<String> = try {
        val result = mutableListOf<String>()
        val pattern = Pattern.compile(match, Pattern.DOTALL)
        val m = pattern.matcher(s)
        while (m.find()) {
            result.add(m.group(1)!!)
        }
        result
    } catch (e: Exception) {
        UtilityLog.handleException(e)
        emptyList()
    }

    fun parseColumnAll(s: String, pattern: Pattern): List<String> = try {
        val result = mutableListOf<String>()
        val m = pattern.matcher(s)
        while (m.find()) {
            result.add(m.group())
        }
        result
    } catch (e: Exception) {
        UtilityLog.handleException(e)
        emptyList()
    }

    fun getLastXChars(s: String, x: Int): String = when {
        s.length == x -> s
        s.length > x -> s.substring(s.length - x)
        else -> s
    }

    fun addPeriodBeforeLastTwoChars(string: String): String =
            StringBuilder(string).insert(string.length - 2, ".").toString()

    fun replaceAllRegexp(s: String, a: String, b: String): String = s.replace(Regex(a), b)

    //
    // Legacy forecast support
    //
    fun parseXml(payloadF: String?, delimF: String?): List<String> {
        var payload = payloadF ?: ""
        val delim = delimF ?: ""
        if (delim == "start-valid-time") {
            payload = payload.replace("<end-valid-time>.*?</end-valid-time>".toRegex(), "")
                    .replace("<layout-key>.*?</layout-key>".toRegex(), "")
        }
        payload = payload.replace("<name>.*?</name>".toRegex(), "")
                .replace("</" + delim + ">".toRegex(), "")
        return payload.split("<$delim>")
    }

    fun parseXmlValue(payloadF: String?): List<String> {
        var payload = payloadF ?: ""
        payload = payload.replace("<name>.*?</name>".toRegex(), "")
                .replace("</value>", "")
        return GlobalVariables.xmlValuePattern.split(payload).toList()
    }

    fun parseXmlExt(regexpList: List<String>, html: String): List<String> =
            regexpList.map { parseAcrossLines(html, it) }
}

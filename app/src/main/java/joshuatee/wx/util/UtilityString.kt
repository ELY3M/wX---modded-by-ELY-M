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

package joshuatee.wx.util

import java.util.regex.Pattern

import joshuatee.wx.Extensions.*
import joshuatee.wx.RegExp

object UtilityString {

    internal fun capitalizeString(string: String): String {
        val chars = string.toLowerCase().toCharArray()
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

    internal fun shortenTimeV2(longTimeF: String): String {
        var longTime = longTimeF
        longTime = longTime.replace("-09:00", "").replace("-10:00", "").replace("-05:00", "")
            .replace("T", " ").replace("-04:00", "").replace(":00 ", " ")
        return longTime.replace("-06:00", "").replace("-07:00", "")
    }

    fun getHTMLandParse(url: String, matchStr: String): String = url.getHtml().parse(matchStr)

    fun getHTMLandParse(url: String, p: Pattern): String = url.getHtml().parse(p)

    fun getHTMLandParseLastMatch(url: String, matchStr: String): String =
        url.getHtml().parseLastMatch(matchStr)

    fun getHTMLandParseLastMatch(url: String, p: Pattern): String = url.getHtml().parseLastMatch(p)

    fun parseLastMatchS(str: String, p: Pattern): String {
        var content = ""
        try {
            val m = p.matcher(str)
            while (m.find()) {
                content = m.group(1)
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return content
    }

    fun parseLastMatchS(str: String, matchStr: String): String {
        var content = ""
        try {
            val p = Pattern.compile(matchStr)
            val m = p.matcher(str)
            while (m.find()) {
                content = m.group(1)
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return content
    }

    fun parseS(data: String, matchStr: String): String {
        var content = ""
        try {
            val p = Pattern.compile(matchStr)
            val m = p.matcher(data)
            if (m.find()) content = m.group(1)
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return content
    }

    internal fun parseAcrossLines(data: String, matchStr: String): String {
        var content = ""
        try {
            val p = Pattern.compile(matchStr, Pattern.DOTALL)
            val m = p.matcher(data)
            if (m.find()) content = m.group(1)
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return content
    }

    fun parseS(data: String, p: Pattern): String {
        var content = ""
        try {
            val m = p.matcher(data)
            if (m.find()) content = m.group(1)
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return content
    }

    fun getHTMLandParseSep(url: String, matchStr: String): String = url.getHtmlSep().parse(matchStr)

    internal fun getHTMLandParseSep(url: String, p: Pattern) = url.getHtmlSep().parse(p)

    fun getHTMLandParseMultipeFirstMatch(
        url: String,
        matchStr: String,
        number: Int
    ): MutableList<String> = parseMultipeFirst(url.getHtml(), matchStr, number)

    private fun parseMultipeFirst(
        data: String,
        match_str: String,
        number: Int
    ): MutableList<String> {
        val result = MutableList(number) { "" }
        try {
            val p = Pattern.compile(match_str)
            val m = p.matcher(data)
            m.find()
            (0 until number).forEach { result[it] = m.group(it + 1) }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return result
    }

    fun parseMultipe(data: String, match_str: String, number: Int): MutableList<String> {
        val result = MutableList(number) { "" }
        try {
            val p = Pattern.compile(match_str)
            val m = p.matcher(data)
            while (m.find()) {
                (0 until number).forEach { result[it] = m.group(it + 1) }
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return result
    }

    fun parseMultipe(data: String, p: Pattern, number: Int): MutableList<String> {
        val result = MutableList(number) { "" }
        try {
            val m = p.matcher(data)
            while (m.find()) {
                (0 until number).forEach { result[it] = m.group(it + 1) }
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return result
    }

    fun parseColumnS(data: String, matchStr: String): List<String> {
        val result = mutableListOf<String>()
        try {
            val p = Pattern.compile(matchStr)
            val m = p.matcher(data)
            while (m.find()) {
                result.add(m.group(1))
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return result
    }

    fun parseColumnMutable(data: String, matchStr: String): MutableList<String> {
        val result = mutableListOf<String>()
        try {
            val p = Pattern.compile(matchStr)
            val m = p.matcher(data)
            while (m.find()) {
                result.add(m.group(1))
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return result
    }

    fun parseColumnMutable(data: String, p: Pattern): MutableList<String> {
        val result = mutableListOf<String>()
        try {
            val m = p.matcher(data)
            while (m.find()) {
                result.add(m.group(1))
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return result
    }

    fun parseColumnS(data: String, p: Pattern): List<String> {
        val result = mutableListOf<String>()
        try {
            val m = p.matcher(data)
            while (m.find()) {
                result.add(m.group(1))
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return result
    }

    fun parseColumnAl(data: String, p: Pattern): MutableList<String> {
        val result = mutableListOf<String>()
        try {
            val m = p.matcher(data)
            while (m.find()) {
                result.add(m.group(1))
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return result
    }

    fun parseColumnAllS(data: String, p: Pattern): List<String> {
        val result = mutableListOf<String>()
        try {
            val m = p.matcher(data)
            while (m.find()) {
                result.add(m.group())
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return result
    }

    fun parseAndCount(data: String, matchStr: String): Int {
        var i = 0
        try {
            val p = Pattern.compile(matchStr)
            val m = p.matcher(data)
            while (m.find()) {
                i += 1
            }
        } catch (e: Exception) {
            UtilityLog.HandleException(e)
        }
        return i
    }

    fun getNWSPRE(url: String): String = url.getHtmlSep().parse(RegExp.pre2Pattern)

    fun getLastXChars(s: String, x: Int): String = when {
        s.length == x -> s
        s.length > x -> s.substring(s.length - x)
        else -> s
    }

    fun addPeriodBeforeLastTwoChars(str: String): String =
        StringBuilder(str).insert(str.length - 2, ".").toString()
}

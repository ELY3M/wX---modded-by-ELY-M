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

    fun removeHtml(text: String): String {
        return Utility.fromHtml(text)
    }

    fun getHtmlAndParse(url: String, match: String): String = url.getHtml().parse(match)

    fun getHtmlAndParse(url: String, pattern: Pattern): String = url.getHtml().parse(pattern)

    fun getHtmlAndParseLastMatch(url: String, match: String): String =
        url.getHtml().parseLastMatch(match)

    fun getHtmlAndParseLastMatch(url: String, pattern: Pattern): String = url.getHtml().parseLastMatch(pattern)

    fun parseLastMatch(str: String, pattern: Pattern): String {
        var content = ""
        try {
            val m = pattern.matcher(str)
            while (m.find()) {
                content = m.group(1)
            }
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
            while (m.find()) {
                content = m.group(1)
            }
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
            if (m.find()) content = m.group(1)
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
            if (m.find()) content = m.group(1)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return content
    }

    fun parse(data: String, pattern: Pattern): String {
        var content = ""
        try {
            val m = pattern.matcher(data)
            if (m.find()) content = m.group(1)
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return content
    }

    fun getHtmlAndParseSep(url: String, match: String): String = url.getHtmlSep().parse(match)

    internal fun getHtmlAndParseSep(url: String, pattern: Pattern) = url.getHtmlSep().parse(pattern)

    fun getHtmlAndParseMultipleFirstMatch(
        url: String,
        match: String,
        number: Int
    ): MutableList<String> = parseMultipleFirst(url.getHtml(), match, number)

    private fun parseMultipleFirst(
        data: String,
        match: String,
        number: Int
    ): MutableList<String> {
        val result = MutableList(number) { "" }
        try {
            val pattern = Pattern.compile(match)
            val m = pattern.matcher(data)
            m.find()
            (0 until number).forEach { result[it] = m.group(it + 1) }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return result
    }

    fun parseMultiple(data: String, match: String, number: Int): MutableList<String> {
        val result = MutableList(number) { "" }
        try {
            val pattern = Pattern.compile(match)
            val m = pattern.matcher(data)
            while (m.find()) {
                (0 until number).forEach { result[it] = m.group(it + 1) }
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
                (0 until number).forEach { result[it] = m.group(it + 1) }
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
            while (m.find()) {
                result.add(m.group(1))
            }
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
            while (m.find()) {
                result.add(m.group(1))
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return result
    }

    fun parseColumnMutable(data: String, pattern: Pattern): MutableList<String> {
        val result = mutableListOf<String>()
        try {
            val m = pattern.matcher(data)
            while (m.find()) {
                result.add(m.group(1))
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return result
    }

    fun parseColumn(data: String, pattern: Pattern): List<String> {
        val result = mutableListOf<String>()
        try {
            val m = pattern.matcher(data)
            while (m.find()) {
                result.add(m.group(1))
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return result
    }

    fun parseColumnAl(data: String, pattern: Pattern): MutableList<String> {
        val result = mutableListOf<String>()
        try {
            val m = pattern.matcher(data)
            while (m.find()) {
                result.add(m.group(1))
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return result
    }

    fun parseColumnAll(data: String, pattern: Pattern): List<String> {
        val result = mutableListOf<String>()
        try {
            val m = pattern.matcher(data)
            while (m.find()) {
                result.add(m.group())
            }
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
            while (m.find()) {
                i += 1
            }
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
        return i
    }

    fun getNwsPre(url: String): String = url.getHtmlSep().parse(RegExp.pre2Pattern)

    fun getLastXChars(s: String, x: Int): String = when {
        s.length == x -> s
        s.length > x -> s.substring(s.length - x)
        else -> s
    }

    fun addPeriodBeforeLastTwoChars(string: String): String =
        StringBuilder(string).insert(string.length - 2, ".").toString()
}

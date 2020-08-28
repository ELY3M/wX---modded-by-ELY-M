// thanks to Mike:
// http://stackoverflow.com/questions/5931665/remove-duplicate-lines-from-text-using-java
//

package joshuatee.wx.external

import java.util.LinkedHashSet

class ExternalDuplicateRemover {

    fun stripDuplicates(aHunk: String): String {
        val result = StringBuilder()
        val uniqueLines = LinkedHashSet<String>()
        val chunks = aHunk.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        uniqueLines.addAll(listOf(*chunks))
        for (chunk in uniqueLines) { result.append(chunk).append("\n") }
        return result.toString()
    }
}

// Example usage
// String input = "z\nb\nc\nb\nz\n";
// String expected = "z\nb\nc\n";
// DuplicateRemover remover = new DuplicateRemover();
// String actual = remover.stripDuplicates(input);
// assertEquals(expected, actual);

// 
// thanks to Stefan Rusek
// http://stackoverflow.com/questions/24195674/image-share-intent-works-for-gmail-but-crashes-fb-and-twitter/25020642
//

package joshuatee.wx.util

import android.content.Context
import java.util.Arrays

import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.MediaStore
import java.io.File

class FileProvider : androidx.core.content.FileProvider() {

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {
        val source = super.query(uri, projection, selection, selectionArgs, sortOrder)
        val columnNames = source!!.columnNames
        val newColumnNames = columnNamesWithData(columnNames)
        val cursor = MatrixCursor(newColumnNames, source.count)
        source.moveToPosition(-1)
        while (source.moveToNext()) {
            val row = cursor.newRow()
            columnNames.indices.forEach { row.add(source.getString(it)) }
        }
        return cursor
    }

    private fun columnNamesWithData(columnNames: Array<String>): Array<String> {
        columnNames
            .filter { MediaStore.MediaColumns.DATA == it }
            .forEach { _ -> return columnNames }
        val newColumnNames = Arrays.copyOf(columnNames, columnNames.size + 1)
        newColumnNames[columnNames.size] = MediaStore.MediaColumns.DATA
        return newColumnNames
    }

    companion object {
        fun getUriForFile(context: Context, authority: String, file: File): Uri =
            androidx.core.content.FileProvider.getUriForFile(context, authority, file)
    }
}

// http://stackoverflow.com/questions/15804805/android-action-bar-searchview-as-autocomplete
// thanks Michael Herbig
// use like app:actionViewClass="com.yourpackage.ArrayAdapterSearchView"
//

package joshuatee.wx.settings

import android.content.Context
import androidx.cursoradapter.widget.CursorAdapter
import androidx.appcompat.widget.SearchView
import android.util.AttributeSet
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.R

class ArrayAdapterSearchView : SearchView {

    private var searchAutoComplete: SearchAutoComplete = findViewById(R.id.search_src_text)

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize()
    }

    private fun initialize() {
        this.setAdapter(null)
        this.setOnItemClickListener(null)
    }

    override fun setSuggestionsAdapter(adapter: CursorAdapter) {}

    fun setOnItemClickListener(listener: AdapterView.OnItemClickListener?) {
        searchAutoComplete.onItemClickListener = listener
    }

    fun setAdapter(adapter: ArrayAdapter<*>?) {
        searchAutoComplete.setAdapter<ArrayAdapter<*>>(adapter)
    }

    fun setText(text: String) {
        searchAutoComplete.setText(text)
    }
}

package joshuatee.wx.audio

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.TextSize
import joshuatee.wx.ui.Card
import joshuatee.wx.ui.Text
import joshuatee.wx.util.Utility

internal class PlayListAdapter(val context: Context, private val dataSet: MutableList<String>) :
    RecyclerView.Adapter<PlayListAdapter.DataObjectHolder>() {

    private val maxLength = 400

    internal class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val label =
            Text(itemView, R.id.single_text, UIPreferences.textHighlightColor, TextSize.MEDIUM)
        val contentPreview = Text(itemView, R.id.text2, backgroundText = true)
        val timeAndSize = Text(itemView, R.id.time_and_size, TextSize.SMALL)

        init {
            Card(itemView, R.id.cv1)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            myClickListener!!.onItemClick(layoutPosition)
        }
    }

    fun setListener(fn: (Int) -> Unit) {
        myClickListener = object : MyClickListener {
            override fun onItemClick(position: Int) {
                fn(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataObjectHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cardview_playlist, parent, false)
        return DataObjectHolder(view)
    }

    override fun onBindViewHolder(holder: DataObjectHolder, position: Int) {
        val items = dataSet[position].split(";")
        with(holder) {
            label.text = items[0]
            timeAndSize.text = items[1]
            contentPreview.text = Utility.readPref(context, "PLAYLIST_" + items[0], "")
                .replace(GlobalVariables.newline, " ")
                .take(maxLength)
        }
    }

    fun deleteItem(index: Int) {
        dataSet.removeAt(index)
        notifyItemRemoved(index)
    }

    override fun getItemCount() = dataSet.size

    interface MyClickListener {
        fun onItemClick(position: Int)
    }

    companion object {
        private var myClickListener: MyClickListener? = null
    }
}

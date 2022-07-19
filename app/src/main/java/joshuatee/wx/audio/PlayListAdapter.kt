package joshuatee.wx.audio

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.TextSize
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectTextView
import joshuatee.wx.util.Utility

internal class PlayListAdapter(private val dataSet: MutableList<String>) : RecyclerView.Adapter<PlayListAdapter.DataObjectHolder>() {

    private val maxLength = 400

    internal class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val label = ObjectTextView(itemView, R.id.singletext, UIPreferences.textHighlightColor, TextSize.MEDIUM)
        val contentPreview = ObjectTextView(itemView, R.id.text2, backgroundText = true)
        val timeAndSize = ObjectTextView(itemView, R.id.timeandsize, TextSize.SMALL)

        init {
            ObjectCard(itemView, R.id.cv1)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) { myClickListener!!.onItemClick(layoutPosition) }
    }

    fun setListener(fn: (Int) -> Unit) {
        myClickListener = object : MyClickListener { override fun onItemClick(position: Int) { fn(position) } }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataObjectHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_playlist, parent, false)
        return DataObjectHolder(view)
    }

    override fun onBindViewHolder(holder: DataObjectHolder, position: Int) {
        val items = dataSet[position].split(";")
        holder.label.text = items[0]
        holder.timeAndSize.text = items[1]
        val string = Utility.fromHtml(Utility.readPref("PLAYLIST_" + items[0], ""))
        holder.contentPreview.text = string.replace(GlobalVariables.newline, " ").take(maxLength)
    }

    fun deleteItem(index: Int) {
        dataSet.removeAt(index)
        // notifyDataSetChanged()
        notifyItemRemoved(index)
    }

    override fun getItemCount() = dataSet.size

    interface MyClickListener { fun onItemClick(position: Int) }

    companion object {
        private var myClickListener: MyClickListener? = null
    }
}

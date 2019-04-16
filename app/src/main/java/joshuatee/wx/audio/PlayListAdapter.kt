package joshuatee.wx.audio

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.TextSize
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectTextView
import joshuatee.wx.util.Utility

internal class PlayListAdapter(private val mDataset: MutableList<String>) :
    RecyclerView.Adapter<PlayListAdapter.DataObjectHolder>() {

    internal class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val label = ObjectTextView(itemView, R.id.singletext, UIPreferences.textHighlightColor)
        val contentPreview = ObjectTextView(itemView, R.id.text2)
        val timeAndSize = ObjectTextView(itemView, R.id.timeandsize, TextSize.SMALL)

        init {
            ObjectCard(itemView, R.id.cv1)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            myClickListener!!.onItemClick(adapterPosition)
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
        val tmpArr = mDataset[position].split(";")
        holder.label.text = tmpArr[0]
        holder.timeAndSize.text = tmpArr[1]
        val tmpStr = Utility.fromHtml(Utility.readPref("PLAYLIST_" + tmpArr[0], ""))
        holder.contentPreview.text = tmpStr.replace(MyApplication.newline, " ")
        holder.contentPreview.setAsBackgroundText()
    }

    fun deleteItem(index: Int) {
        mDataset.removeAt(index)
        notifyDataSetChanged()
    }

    override fun getItemCount() = mDataset.size

    interface MyClickListener {
        fun onItemClick(position: Int)
    }

    companion object {
        private var myClickListener: MyClickListener? = null
    }
}

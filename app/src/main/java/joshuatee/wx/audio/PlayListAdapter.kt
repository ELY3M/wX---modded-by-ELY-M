package joshuatee.wx.audio

import androidx.recyclerview.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.util.Utility

// thanks http://www.truiton.com/2015/03/android-cardview-example/

internal class PlayListAdapter(private val mDataset: MutableList<String>) : RecyclerView.Adapter<PlayListAdapter.DataObjectHolder>() {

    internal class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val label: TextView = itemView.findViewById(R.id.singletext)
        val label2: TextView = itemView.findViewById(R.id.text2)
        val timeandsize: TextView = itemView.findViewById(R.id.timeandsize)

        init {
            ObjectCard(itemView, R.id.cv1)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            myClickListener!!.onItemClick(adapterPosition)
        }
    }

    fun setOnItemClickListener(myClickListenerloc: MyClickListener) {
        myClickListener = myClickListenerloc
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataObjectHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_playlist, parent, false)
        return DataObjectHolder(view)
    }

    override fun onBindViewHolder(holder: DataObjectHolder, position: Int) {
        val tmpArr = MyApplication.semicolon.split(mDataset[position])
        holder.label.text = tmpArr[0]
        holder.label.setTextColor(UIPreferences.textHighlightColor)
        holder.timeandsize.text = tmpArr[1]
        holder.timeandsize.setTextColor(UIPreferences.backgroundColor)
        holder.timeandsize.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
        val tmpStr = Utility.fromHtml(Utility.readPref("PLAYLIST_" + tmpArr[0], ""))
        holder.label2.text = tmpStr.replace(MyApplication.newline, " ")
        holder.label2.setTextColor(UIPreferences.backgroundColor)
        // FIXME deprecation
        holder.label2.setTextAppearance(holder.label2.context, UIPreferences.smallTextTheme)
        holder.label2.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
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

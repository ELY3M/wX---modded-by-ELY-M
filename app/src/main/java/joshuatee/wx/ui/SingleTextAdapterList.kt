package joshuatee.wx.ui

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import joshuatee.wx.R
import joshuatee.wx.objects.TextSize
import joshuatee.wx.settings.UIPreferences

class SingleTextAdapterList(private val dataSet: MutableList<String>) : RecyclerView.Adapter<SingleTextAdapterList.DataObjectHolder>() {

    class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val label = Text(itemView, R.id.single_text, TextSize.MEDIUM)

        init {
            Card(itemView, R.id.cv1)
            itemView.setOnClickListener(this)
            label.setPadding(UIPreferences.paddingSettings)
        }

        // was onItemClick(adapterPosition)
        override fun onClick(v: View) {
            myClickListener!!.onItemClick(layoutPosition)
        }
    }

    fun setOnItemClickListener(myClickListenerloc: MyClickListener) {
        myClickListener = myClickListenerloc
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataObjectHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.textview, parent, false)
        return DataObjectHolder(view)
    }

    override fun onBindViewHolder(holder: DataObjectHolder, position: Int) {
        holder.label.text = dataSet[position]
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteItem(index: Int) {
        if (dataSet.size > index) {
            dataSet.removeAt(index)
            notifyDataSetChanged()
        }
    }

    // used in FavRemoveActivity for SPCMeso
    override fun toString(): String {
        var s = ""
        dataSet.forEach {
            s += ":$it"
        }
        return "$s:"
    }

    fun getItem(index: Int): String = dataSet[index]

    override fun getItemCount(): Int = dataSet.size

    interface MyClickListener {
        fun onItemClick(position: Int)
    }

    companion object {
        private var myClickListener: MyClickListener? = null
    }
}

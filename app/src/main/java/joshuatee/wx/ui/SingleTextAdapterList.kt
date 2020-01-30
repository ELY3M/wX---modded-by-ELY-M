package joshuatee.wx.ui

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import joshuatee.wx.MyApplication

import joshuatee.wx.R
import joshuatee.wx.objects.TextSize

class SingleTextAdapterList(private val dataSet: MutableList<String>) :
        RecyclerView.Adapter<SingleTextAdapterList.DataObjectHolder>() {

    class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {

        val label = ObjectTextView(itemView, R.id.singletext, TextSize.MEDIUM)

        init {
            ObjectCard(itemView, R.id.cv1)
            itemView.setOnClickListener(this)
            label.setPadding(MyApplication.paddingSettings, MyApplication.paddingSettings, MyApplication.paddingSettings, MyApplication.paddingSettings)
        }

        override fun onClick(v: View) {
            myClickListener!!.onItemClick(adapterPosition)
        }
    }

    fun setOnItemClickListener(myClickListenerloc: MyClickListener) {
        myClickListener = myClickListenerloc
    }

    /*fun setListener(fn: (Int) -> Unit) {
        myClickListener = object : MyClickListener {
            override fun onItemClick(position: Int) {
                fn(position)
            }
        }
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataObjectHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.textview, parent, false)
        return DataObjectHolder(view)
    }

    override fun onBindViewHolder(holder: DataObjectHolder, position: Int) {
        holder.label.text = dataSet[position]
    }

    fun deleteItem(index: Int) {
        dataSet.removeAt(index)
        notifyDataSetChanged()
    }

    fun setItem(index: Int, str: String) {
        if (index < dataSet.size) {
            dataSet[index] = str
        }
        notifyDataSetChanged()
    }

    // used in FavRemoveActivity for SPCMeso
    override fun toString(): String {
        var string = ""
        dataSet.forEach { string += ":$it" }
        return "$string:"
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
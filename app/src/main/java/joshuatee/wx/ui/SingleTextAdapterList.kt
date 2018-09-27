package joshuatee.wx.ui

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import joshuatee.wx.R

// thanks http://www.truiton.com/2015/03/android-cardview-example/

class SingleTextAdapterList(private val mDataset: MutableList<String>) : RecyclerView.Adapter<SingleTextAdapterList.DataObjectHolder>() {

    class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val label: TextView = itemView.findViewById(R.id.singletext)

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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.textview, parent, false)
        return DataObjectHolder(view)
    }

    override fun onBindViewHolder(holder: DataObjectHolder, position: Int) {
        holder.label.text = mDataset[position]
    }

    fun deleteItem(index: Int) {
        mDataset.removeAt(index)
        notifyDataSetChanged()
    }

    fun setItem(index: Int, str: String) {
        if (index < mDataset.size) {
            mDataset[index] = str
        }
        notifyDataSetChanged()
    }

    // used in FavRemoveActivity for SPCMeso
    override fun toString(): String {
        var string = ""
        mDataset.forEach { string += ":$it" }
        return "$string:"
    }

    fun getItem(index: Int) = mDataset[index]

    override fun getItemCount() = mDataset.size

    interface MyClickListener {
        fun onItemClick(position: Int)
    }

    companion object {
        private var myClickListener: MyClickListener? = null
    }
}
package joshuatee.wx.settings

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.external.UtilityStringExternal
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectTextView

internal class SettingsLocationAdapterList(private val mDataset: MutableList<String>) :
    RecyclerView.Adapter<SettingsLocationAdapterList.DataObjectHolder>() {

    companion object {
        private var myClickListener: MyClickListener? = null
    }

    internal class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val text1 = ObjectTextView(itemView, R.id.text1)
        val currentConditions = ObjectTextView(itemView, R.id.currentConditions)
        val text2 = ObjectTextView(itemView, R.id.text2)
        val text3 = ObjectTextView(itemView, R.id.text3)
        val objCard = ObjectCard(itemView, R.id.cv1)

        init {
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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardview_settingslocation, parent, false)
        return DataObjectHolder(view)
    }

    override fun onBindViewHolder(holder: DataObjectHolder, position: Int) {
        var nonUs = true
        var lat = ""
        var lon = ""
        if (Location.isUS(position)) {
            nonUs = false
        } else {
            val latArr = MyApplication.colon.split(Location.getX(position))
            val lonArr = MyApplication.colon.split(Location.getY(position))
            if (latArr.size > 2 && lonArr.size > 1) {
                lat = latArr[2]
                lon = lonArr[1]
            }
        }
        holder.text1.text = Location.getName(position)
        if (UtilityLocation.hasAlerts(position)) {
            holder.text1.setTextColor(UIPreferences.textHighlightColor)
        } else {
            holder.text1.setTextColor(UIPreferences.backgroundColor)
        }


        holder.currentConditions.text = Location.getObservation(position)
        //holder.currentConditions.tv.visibility = View.INVISIBLE


        if (nonUs)
            holder.text2.text =
                """${UtilityStringExternal.truncate(lat, 6)} , ${UtilityStringExternal.truncate(
                    lon,
                    6
                )}"""
        else
            holder.text2.text = "${UtilityStringExternal.truncate(
                Location.getX(position),
                6
            )} , ${UtilityStringExternal.truncate(Location.getY(position), 6)}"
        holder.text2.setAsBackgroundText()
        if (nonUs) {
            holder.text3.text =
                "RID: ${Location.getRid(position)} ${UtilityLocation.hasAlerts(position)}"
        } else {
            holder.text3.text =
                "WFO: ${Location.getWfo(position)}  RID: ${Location.getRid(position)}"
        }
        holder.text3.setAsBackgroundText()
    }

    fun deleteItem(index: Int) {
        if (index < mDataset.count()) {
            mDataset.removeAt(index)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = mDataset.size

    interface MyClickListener {
        fun onItemClick(position: Int)
    }
}

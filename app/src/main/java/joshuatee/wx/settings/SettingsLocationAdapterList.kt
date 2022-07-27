package joshuatee.wx.settings

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import joshuatee.wx.R
import joshuatee.wx.objects.TextSize
import joshuatee.wx.ui.Card
import joshuatee.wx.ui.Text

internal class SettingsLocationAdapterList(private val dataSet: MutableList<String>) : RecyclerView.Adapter<SettingsLocationAdapterList.DataObjectHolder>() {

    companion object {
        private var myClickListener: MyClickListener? = null
    }

    internal class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val text1 = Text(itemView, R.id.text1, TextSize.MEDIUM)
        val currentConditions = Text(itemView, R.id.currentConditions, TextSize.SMALL)
        val text2 = Text(itemView, R.id.text2, backgroundText = true)

        init {
            Card(itemView, R.id.cv1)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) { myClickListener!!.onItemClick(layoutPosition) }
    }

    fun setListener(fn: (Int) -> Unit) {
        myClickListener = object : MyClickListener { override fun onItemClick(position: Int) { fn(position) } }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataObjectHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_settingslocation, parent, false)
        return DataObjectHolder(view)
    }

    override fun onBindViewHolder(holder: DataObjectHolder, position: Int) {
        var nonUs = true
        var lat = ""
        var lon = ""
        if (Location.isUS(position)) {
            nonUs = false
        } else {
            val latArr = Location.getX(position).split(":")
            val lonArr = Location.getY(position).split(":")
            if (latArr.size > 2 && lonArr.size > 1) {
                lat = latArr[2]
                lon = lonArr[1]
            }
        }
        holder.text1.text = Location.getName(position)
        if (UtilityLocation.hasAlerts(position)) {
            holder.text1.text = Location.getName(position) + " +Alert"
        }
        holder.text1.color = UIPreferences.textHighlightColor
        holder.currentConditions.text = Location.getObservation(position)
        if (nonUs) {
            holder.text2.text = "RID: ${Location.getRid(position)} ${UtilityLocation.hasAlerts(position)} (${lat.take(6)} , ${lon.take(6)})"
        } else {
            holder.text2.text = "WFO: ${Location.getWfo(position)}  RID: ${Location.getRid(position)} (${Location.getX(position).take(6)} , ${Location.getY(position).take(6)})"
        }
    }

    fun deleteItem(index: Int) {
        if (index < dataSet.count()) {
            dataSet.removeAt(index)
//            notifyDataSetChanged()
            notifyItemRemoved(index)
        }
    }

    override fun getItemCount() = dataSet.size

    fun getItem(index: Int) = dataSet[index]

    interface MyClickListener { fun onItemClick(position: Int) }
}

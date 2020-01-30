/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

    This file is part of wX.

    wX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    wX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with wX.  If not, see <http://www.gnu.org/licenses/>.

 */

package joshuatee.wx.activitiesmisc

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.objects.TextSize
import joshuatee.wx.radar.SpotterReports
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectTextView

internal class AdapterSpotterReports(private val dataSet: List<SpotterReports>) :
    RecyclerView.Adapter<AdapterSpotterReports.DataObjectHolder>() {

    internal class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val name = ObjectTextView(itemView, R.id.name, UIPreferences.textHighlightColor, TextSize.MEDIUM)
        val email = ObjectTextView(itemView, R.id.email)
        val time = ObjectTextView(itemView, R.id.time)
        val phone = ObjectTextView(itemView, R.id.phone)
        val summary = ObjectTextView(itemView, R.id.summary)

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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardview_spotter_reports, parent, false)
        return DataObjectHolder(view)
    }

    override fun onBindViewHolder(holder: DataObjectHolder, position: Int) {
        holder.name.text = dataSet[position].type
        holder.time.text = dataSet[position].time
        holder.email.text = dataSet[position].city.replace(MyApplication.newline, " ")
        holder.phone.text = dataSet[position].lastName + ", " + dataSet[position].firstName
        holder.summary.text = dataSet[position].narrative
        holder.summary.setAsBackgroundText()
        listOf(holder.time, holder.email, holder.phone, holder.summary).forEach {
            it.color = UIPreferences.backgroundColor
            it.setTextSize(TextSize.SMALL)
        }
    }

    override fun getItemCount() = dataSet.size

    interface MyClickListener {
        fun onItemClick(position: Int)
    }

    companion object {
        private var myClickListener: MyClickListener? = null
    }
}

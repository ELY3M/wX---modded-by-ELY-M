/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com

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
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.settings.UIPreferences
import joshuatee.wx.objects.TextSize
import joshuatee.wx.radar.SpotterReports
import joshuatee.wx.ui.Card
import joshuatee.wx.ui.Text

internal class AdapterSpotterReports(private val dataSet: List<SpotterReports>) : RecyclerView.Adapter<AdapterSpotterReports.DataObjectHolder>() {

    internal class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val type = Text(itemView, R.id.type, UIPreferences.textHighlightColor, TextSize.MEDIUM)
        val city = Text(itemView, R.id.city, backgroundText = true)
        val time = Text(itemView, R.id.time, backgroundText = true)
        val name = Text(itemView, R.id.name, backgroundText = true)
        val summary = Text(itemView, R.id.summary, backgroundText = true)

        init {
            Card(itemView, R.id.cv1)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) { myClickListener!!.onItemClick(layoutPosition) }
    }

    fun setOnItemClickListener(myClickListenerloc: MyClickListener) { myClickListener = myClickListenerloc }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataObjectHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_spotter_reports, parent, false)
        return DataObjectHolder(view)
    }

    override fun onBindViewHolder(holder: DataObjectHolder, position: Int) {
        with (holder) {
            type.text = dataSet[position].type
            time.text = dataSet[position].time
            city.text = dataSet[position].city.replace(GlobalVariables.newline, " ")
            name.text = dataSet[position].lastName + ", " + dataSet[position].firstName
            summary.text = dataSet[position].narrative
        }
    }

    override fun getItemCount() = dataSet.size

    interface MyClickListener { fun onItemClick(position: Int) }

    companion object { private var myClickListener: MyClickListener? = null }
}

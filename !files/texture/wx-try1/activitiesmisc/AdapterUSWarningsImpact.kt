/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

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
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.ui.ObjectCard

internal class AdapterUSWarningsImpact(private val mDataset: List<ObjectImpactGraphic>) : RecyclerView.Adapter<AdapterUSWarningsImpact.DataObjectHolder>() {

    internal class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val title: TextView = itemView.findViewById(R.id.title)
        val cities: TextView = itemView.findViewById(R.id.cities)
        val population: TextView = itemView.findViewById(R.id.population)

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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_uswarningsimpact, parent, false)
        return DataObjectHolder(view)
    }

    override fun onBindViewHolder(holder: DataObjectHolder, position: Int) {
        holder.title.text = mDataset[position].title
        holder.title.setTextColor(UIPreferences.textHighlightColor)
        holder.title.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
        holder.cities.text = mDataset[position].cities
        holder.cities.setTextColor(UIPreferences.backgroundColor)
        holder.cities.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
        holder.population.text = mDataset[position].population
        holder.population.setTextColor(UIPreferences.backgroundColor)
        holder.population.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
    }

    override fun getItemCount() = mDataset.size

    interface MyClickListener {
        fun onItemClick(position: Int)
    }

    companion object {
        private var myClickListener: MyClickListener? = null
    }
}
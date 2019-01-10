/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.telephony.TelephonyManager
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.radar.Spotter
import joshuatee.wx.ui.ObjectCard

internal class AdapterSpotter(private val mDataset: MutableList<Spotter>) :
    RecyclerView.Adapter<AdapterSpotter.DataObjectHolder>() {

    internal class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val name: TextView = itemView.findViewById(R.id.name)
        val email: TextView = itemView.findViewById(R.id.email)
        val time: TextView = itemView.findViewById(R.id.time)
        val phone: TextView = itemView.findViewById(R.id.phone)
        val objCard = ObjectCard(itemView, R.id.cv1)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            myClickListener!!.onItemClick(adapterPosition)
        }
    }

    fun setListener(fn: (Int) -> Unit) {
        myClickListener = object : AdapterSpotter.MyClickListener {
            override fun onItemClick(position: Int) {
                fn(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataObjectHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cardview_spotter, parent, false)
        return DataObjectHolder(view)
    }

    override fun onBindViewHolder(holder: DataObjectHolder, position: Int) {
        holder.name.text = mDataset[position].lastName + ", " + mDataset[position].firstName
        holder.name.setTextColor(UIPreferences.textHighlightColor)
        holder.time.text = mDataset[position].reportAt
        holder.email.text = mDataset[position].email.replace(MyApplication.newline, " ")
        val he = holder.email
        val emailAddress = holder.email.text
        holder.email.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
            intent.putExtra(Intent.EXTRA_SUBJECT, "")
            he.context.startActivity(Intent.createChooser(intent, "Send Email"))
        }
        holder.phone.text = mDataset[position].phone.replace(MyApplication.newline, " ")
        listOf(holder.time, holder.email, holder.phone).forEach {
            it.setTextColor(UIPreferences.backgroundColor)
            it.setTextSize(TypedValue.COMPLEX_UNIT_PX, MyApplication.textSizeSmall)
            it.setTextAppearance(it.context, UIPreferences.smallTextTheme)
        }
        val hp = holder.phone
        holder.phone.setOnClickListener {
            val tm = hp.context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (tm.phoneType != TelephonyManager.PHONE_TYPE_NONE) {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:" + mDataset[position].phone)
                hp.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = mDataset.size

    interface MyClickListener {
        fun onItemClick(position: Int)
    }

    private fun removeItem(position: Int) {
        mDataset.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun addItem(position: Int, model: Spotter) {
        mDataset.add(position, model)
        notifyItemInserted(position)
    }

    private fun moveItem(fromPosition: Int, toPosition: Int) {
        val model = mDataset.removeAt(fromPosition)
        mDataset.add(toPosition, model)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun animateTo(models: List<Spotter>) {
        applyAndAnimateRemovals(models)
        applyAndAnimateAdditions(models)
        applyAndAnimateMovedItems(models)
    }

    private fun applyAndAnimateRemovals(newModels: List<Spotter>) {
        mDataset.indices.reversed().forEach {
            val model = mDataset[it]
            if (!newModels.contains(model)) {
                removeItem(it)
            }
        }
    }

    private fun applyAndAnimateAdditions(newModels: List<Spotter>) {
        var i = 0
        val count = newModels.size
        while (i < count) {
            val model = newModels[i]
            if (!mDataset.contains(model)) {
                addItem(i, model)
            }
            i += 1
        }
    }

    private fun applyAndAnimateMovedItems(newModels: List<Spotter>) {
        newModels.indices.reversed().forEach {
            val model = newModels[it]
            val fromPosition = mDataset.indexOf(model)
            if (fromPosition >= 0 && fromPosition != it) {
                moveItem(fromPosition, it)
            }
        }
    }

    companion object {
        private var myClickListener: MyClickListener? = null
    }
}

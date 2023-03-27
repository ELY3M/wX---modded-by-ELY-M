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

package joshuatee.wx.settings

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.ui.Card

internal class TileAdapterColorPalette(private val itemList: List<TileObjectColorPalette>, private val tilesPerRow: Int) : RecyclerView.Adapter<TileAdapterColorPalette.RecyclerViewHoldersColorPalette>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHoldersColorPalette {
        val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.cardview_tiles, null)
        return RecyclerViewHoldersColorPalette(layoutView)
    }

    override fun onBindViewHolder(holder: RecyclerViewHoldersColorPalette, position: Int) {
        val bitmap = itemList[position].bitmapWithText
        val layoutParams = holder.imageView.layoutParams
        layoutParams.width = MyApplication.dm.widthPixels / tilesPerRow
        layoutParams.height = layoutParams.width * bitmap.height / bitmap.width
        holder.imageView.layoutParams = layoutParams
        holder.imageView.setImageBitmap(bitmap)
    }

    override fun getItemCount() = this.itemList.size

    internal inner class RecyclerViewHoldersColorPalette(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val imageView: ImageView

        init {
            itemView.setOnClickListener(this)
            imageView = itemView.findViewById(R.id.iv)
            Card(itemView, R.color.primary_blue, R.id.card_view)
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) { myClickListener!!.onItemClick(layoutPosition) }
    }

    fun setListener(fn: (Int) -> Unit) {
        myClickListener = object : MyClickListener { override fun onItemClick(position: Int) { fn(position) } }
    }

    interface MyClickListener { fun onItemClick(position: Int) }

    companion object {
        private var myClickListener: MyClickListener? = null
    }
}

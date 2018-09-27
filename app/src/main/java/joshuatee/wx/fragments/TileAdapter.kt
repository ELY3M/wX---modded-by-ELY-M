package joshuatee.wx.fragments

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import java.util.Collections

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.util.Utility
import joshuatee.wx.util.UtilityImg

// thanks http://inducesmile.com/android/android-gridlayoutmanager-with-recyclerview-in-material-design/

internal class TileAdapter(val context: Context, private val itemList: MutableList<TileObject>, private val tilesPerRow: Int, private val prefVar: String) : RecyclerView.Adapter<RecyclerViewHolders>(), ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolders {
        val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.cardview_tiles, null)
        return RecyclerViewHolders(layoutView, itemList)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolders, position: Int) {
        val bm = if (!MyApplication.tileDownsize) {
            UtilityImg.loadBM(context, itemList[position].photo, false)
        } else {
            UtilityImg.loadBM(context, itemList[position].photo, true)
        }
        val paramsIv = holder.iv.layoutParams
        paramsIv.width = MyApplication.dm.widthPixels / tilesPerRow
        paramsIv.height = paramsIv.width * bm.height / bm.width
        holder.iv.layoutParams = paramsIv
        holder.iv.setImageBitmap(bm)
    }

    override fun getItemCount() = this.itemList.size

    // thanks https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf#.sc0gfnb2b

    override fun onItemDismiss(position: Int) {
        itemList.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition < toPosition) {
            for (i in (fromPosition until toPosition)) {
                Collections.swap(itemList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(itemList, i, i - 1)
            }
        }
        var prefSave = ""
        itemList.forEach { prefSave = prefSave + it.objectTagStr + ":" }
        Utility.writePref(context, this.prefVar, prefSave)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }
}

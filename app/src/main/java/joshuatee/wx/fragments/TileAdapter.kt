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

internal class TileAdapter(
        val context: Context,
        private val itemList: MutableList<TileObject>,
        private val tilesPerRow: Int,
        private val prefVar: String
) : RecyclerView.Adapter<RecyclerViewHolders>(), ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolders {
        val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.cardview_tiles, parent, false)
//        val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.cardview_tiles, null)
        layoutView.layoutParams.height = MyApplication.dm.widthPixels / tilesPerRow
        layoutView.layoutParams.width = MyApplication.dm.widthPixels / tilesPerRow
        return RecyclerViewHolders(layoutView, itemList)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolders, position: Int) {
        val bitmap = UtilityImg.loadBitmap(context, itemList[position].photo, false)
        val layoutParams = holder.imageView.layoutParams
        layoutParams.width = MyApplication.dm.widthPixels / tilesPerRow
//        layoutParams.height = layoutParams.width * bitmap.height / bitmap.width
        layoutParams.height = MyApplication.dm.widthPixels / tilesPerRow
        with(holder) {
            imageView.layoutParams = layoutParams
            imageView.setImageBitmap(bitmap)
            imageView.contentDescription = itemList[position].description
        }
    }

    override fun getItemCount(): Int = this.itemList.size

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
        itemList.forEach {
            prefSave = prefSave + it.objectTagStr + ":"
        }
        Utility.writePref(context, this.prefVar, prefSave)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }
}

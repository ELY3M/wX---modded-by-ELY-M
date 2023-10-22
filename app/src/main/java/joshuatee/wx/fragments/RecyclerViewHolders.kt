package joshuatee.wx.fragments

import android.content.Intent
import android.graphics.Color
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import joshuatee.wx.R
import joshuatee.wx.ui.Card

// thanks http://inducesmile.com/android/android-gridlayoutmanager-with-recyclerview-in-material-design/

internal class RecyclerViewHolders(itemView: View, private val itemList: List<TileObject>) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener, ItemTouchHelperViewHolder {

    val imageView: ImageView

    init {
        itemView.setOnClickListener(this)
        Card(itemView, R.color.primary_blue, R.id.card_view)
        imageView = itemView.findViewById(R.id.iv)
        itemView.setOnClickListener(this)
    }

    // following 2 methods available via implements ItemTouchHelperViewHolder
    override fun onItemSelected() {
        (itemView as CardView).setCardBackgroundColor(Color.RED)
    }

    override fun onItemClear() {
        (itemView as CardView).setCardBackgroundColor(0)
    }

    override fun onClick(view: View) {
        // was adapterPosition
        val intent = Intent(view.context, itemList[layoutPosition].activity)
        intent.putExtra(itemList[layoutPosition].target, itemList[layoutPosition].argsArr)
        view.context.startActivity(intent)
    }
}

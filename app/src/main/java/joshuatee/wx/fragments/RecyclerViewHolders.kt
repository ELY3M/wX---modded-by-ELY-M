package joshuatee.wx.fragments

import android.content.Intent
import android.graphics.Color
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView

import joshuatee.wx.R
import joshuatee.wx.ui.ObjectCard

// thanks http://inducesmile.com/android/android-gridlayoutmanager-with-recyclerview-in-material-design/

internal class RecyclerViewHolders(itemView: View, private val itemList: List<TileObject>) :
    RecyclerView.ViewHolder(itemView), View.OnClickListener, ItemTouchHelperViewHolder {

    val iv: ImageView

    init {
        itemView.setOnClickListener(this)
        ObjectCard(itemView, R.color.primary_blue, R.id.card_view)
        iv = itemView.findViewById(R.id.iv)
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
        val intent = Intent(view.context, itemList[adapterPosition].activity)
        intent.putExtra(itemList[adapterPosition].target, itemList[adapterPosition].argsArr)
        view.context.startActivity(intent)
    }
}

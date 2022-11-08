package edu.foodfun.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.makeramen.roundedimageview.RoundedImageView
import edu.foodfun.R
import edu.foodfun.uistate.RestaurantUIState

class SearchResultRecyclerViewAdapter(private val data: MutableList<RestaurantUIState>, private val clickListener: ItemClickListener)
    : RecyclerView.Adapter<SearchResultRecyclerViewAdapter.SearchResultViewHolder>() {
    class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val frame: ConstraintLayout = itemView.findViewById(R.id.frameRestaurant)
        val image: RoundedImageView = itemView.findViewById(R.id.imageSearchRestaurant)
        val name: TextView = itemView.findViewById(R.id.labSearchRestaurantName)
    }

    interface ItemClickListener {
        fun itemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        return SearchResultViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_search_restaurant, parent, false))
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.apply {
            name.text = data[position].restaurant.name
            image.setImageBitmap(data[position].image)
            frame.setOnClickListener {
                clickListener.itemClick(position)
            }
        }
    }

    override fun getItemCount(): Int = data.size

}
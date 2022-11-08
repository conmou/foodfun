package edu.foodfun.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import edu.foodfun.R
import edu.foodfun.model.Restaurant
import edu.foodfun.uistate.RestaurantUIState


class RestaurantRecyclerViewAdapter(private val data: MutableList<RestaurantUIState>, private val favoriteRestItemObserver: FavoriteRestItemObserver)
    : RecyclerView.Adapter<RestaurantRecyclerViewAdapter.RestaurantViewHolder>() {
    class RestaurantViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView) {
        val restName: TextView = itemView.findViewById(R.id.txtRestName)
        val time: TextView = itemView.findViewById(R.id.txtTime)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
        val frame: ConstraintLayout = itemView.findViewById(R.id.frameFavorite)
    }

    interface FavoriteRestItemObserver {
        fun onDelete(index: Int, restaurant: Restaurant)
        fun onItemClick(index: Int, restaurant: Restaurant)
        fun onItemLongClick(index: Int, restaurant: Restaurant)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        return RestaurantViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false))
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        holder.apply {
            val restaurant = data[position].restaurant
            var strTemp = ""
            restName.text = restaurant.name
            restaurant.type.forEach { strTemp += " $it" }
            time.text = strTemp
            btnDelete.setOnClickListener {
                favoriteRestItemObserver.onDelete(holder.layoutPosition, restaurant)
            }
            frame.setOnClickListener {
                favoriteRestItemObserver.onItemClick(holder.bindingAdapterPosition, restaurant)
            }
            frame.setOnLongClickListener {
                favoriteRestItemObserver.onItemLongClick(holder.bindingAdapterPosition, restaurant)
                true
            }
        }
    }

    override fun getItemCount(): Int = data.size
}
package edu.foodfun.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.foodfun.R
import edu.foodfun.uistate.RestaurantUIState

class RecommendCardRecyclerViewAdapter(private var data: MutableList<RestaurantUIState>)
    : RecyclerView.Adapter<RecommendCardRecyclerViewAdapter.RecommendCardViewHolder>() {
    class RecommendCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageRest: ImageView = itemView.findViewById(R.id.imageRest)
        val txtName: TextView = itemView.findViewById(R.id.txtContactItemName)
        val txtType: TextView = itemView.findViewById(R.id.txtType)
        val txtAddress: TextView = itemView.findViewById(R.id.txtAddress)
        val txtTel: TextView = itemView.findViewById(R.id.txtTel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendCardViewHolder {
        return  RecommendCardViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_recommend_card, parent, false))
    }

    override fun onBindViewHolder(holder: RecommendCardViewHolder, position: Int) {
        holder.apply {
            val restaurant = data[position].restaurant
            val image = data[position].image
            var strTemp = ""
            txtName.text = restaurant.name
            restaurant.type.forEach { strTemp += " $it" }
            txtType.text = strTemp
            txtAddress.text = restaurant.address
            txtTel.text = restaurant.tel
            imageRest.setImageBitmap(image)
        }
    }

    override fun getItemCount(): Int = data.size
}
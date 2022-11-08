package edu.foodfun.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import edu.foodfun.R
import edu.foodfun.uistate.UserUIState

class NearbyUserRecyclerViewAdapter(private val data : MutableList<UserUIState>, private val clickListener: ItemClickListener)
    : RecyclerView.Adapter<NearbyUserRecyclerViewAdapter.NearByUserViewHolder>() {
    class NearByUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nearbyAvatar: ImageView = itemView.findViewById(R.id.imgNearbyAvatar)
    }

    interface ItemClickListener {
        fun itemClick(holder: NearByUserViewHolder, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearByUserViewHolder {
        return NearByUserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_nearby_user_avatar, parent, false))
    }

    override fun onBindViewHolder(holder: NearByUserViewHolder, position: Int) {
        val userUIState = data[position]
        holder.apply {
            nearbyAvatar.setImageBitmap(userUIState.avatar)
            itemView.setOnClickListener {
                clickListener.itemClick(holder, position)
            }
        }
    }

    override fun getItemCount(): Int = data.size
}
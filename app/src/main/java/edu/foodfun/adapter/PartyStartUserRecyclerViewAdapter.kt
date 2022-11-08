package edu.foodfun.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import edu.foodfun.R
import edu.foodfun.uistate.UserUIState

class PartyStartUserRecyclerViewAdapter(private val data : MutableList<UserUIState>)
    : RecyclerView.Adapter<PartyStartUserRecyclerViewAdapter.PartyStartUserViewHolder>() {
    class PartyStartUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val partystartAvatar: ImageView = itemView.findViewById(R.id.imgNearbyAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyStartUserViewHolder {
        return PartyStartUserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_nearby_user_avatar, parent, false))
    }

    override fun onBindViewHolder(holder: PartyStartUserViewHolder, position: Int) {
        val userUIState = data[position]
        holder.apply {
            partystartAvatar.setImageBitmap(userUIState.avatar)
        }
    }

    override fun getItemCount(): Int = data.size
}
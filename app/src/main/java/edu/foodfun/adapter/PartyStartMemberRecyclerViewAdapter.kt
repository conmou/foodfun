package edu.foodfun.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import edu.foodfun.R
import edu.foodfun.uistate.UserUIState

class PartyStartMemberRecyclerViewAdapter(private var data: MutableList<UserUIState>, private val observer: ItemObserver)
    : RecyclerView.Adapter<PartyStartMemberRecyclerViewAdapter.PartyStartMemberViewHolder>() {

    class PartyStartMemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgAvatar: ImageView = itemView.findViewById(R.id.imgPartyRoomMemberAvatar)
    }

    open class ItemObserver {
        open fun onItemClick(index: Int) {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyStartMemberViewHolder {
        return PartyStartMemberViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_party_prepare_avatar, parent, false))
    }

    override fun onBindViewHolder(holder: PartyStartMemberViewHolder, position: Int) {
        holder.apply {
//            val user = data[position].user!!
            val avatar = data[position].avatar
            imgAvatar.setImageBitmap(avatar)
            imgAvatar.setOnClickListener { observer.onItemClick(holder.layoutPosition) }
        }
    }

    override fun getItemCount(): Int = data.size
}
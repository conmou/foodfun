package edu.foodfun.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import edu.foodfun.R
import edu.foodfun.uistate.PartyUserUIState

class PartyPrepareMemberRecyclerViewAdapter(private var data: MutableList<PartyUserUIState>, private val observer: ItemObserver)
    : RecyclerView.Adapter<PartyPrepareMemberRecyclerViewAdapter.PartyPrepareMemberViewHolder>() {

    class PartyPrepareMemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgAvatar: ImageView = itemView.findViewById(R.id.imgPartyRoomMemberAvatar)
        var imgPrepare: ImageView = itemView.findViewById(R.id.imgPartyRoomPrepare)
    }

    open class ItemObserver {
        open fun onItemClick(index: Int) {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyPrepareMemberViewHolder {
        return PartyPrepareMemberViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_party_prepare_avatar, parent, false))
    }

    override fun onBindViewHolder(holder: PartyPrepareMemberViewHolder, position: Int) {
        holder.apply {
            val avatar = data[position].avatar
            imgAvatar.setImageBitmap(avatar)
            imgAvatar.setOnClickListener { observer.onItemClick(holder.layoutPosition) }
            imgPrepare.visibility = if (data[position].isPrepared && !data[position].isOwner) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun getItemCount(): Int = data.size
}
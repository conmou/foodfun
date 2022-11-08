package edu.foodfun.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import edu.foodfun.R
import edu.foodfun.uistate.InviteUIState

class PartyInviteRecyclerViewAdapter (private val data: List<InviteUIState>, private val itemObserver: ItemObserver)
    : RecyclerView.Adapter<PartyInviteRecyclerViewAdapter.PartyInviteViewHolder>() {
    class PartyInviteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val labInviteSenderName: TextView = itemView.findViewById(R.id.labInviteSenderName)
        val labInvitePartyName: TextView = itemView.findViewById(R.id.labInvitePartyName)
        val imagePartyInviteAvatar: ImageView = itemView.findViewById(R.id.imagePartyInviteAvatar)
        val framePartyInvite: ConstraintLayout = itemView.findViewById(R.id.framePartyInvite)
        val btnPartyInviteConfirm: Button = itemView.findViewById(R.id.btnPartyInviteConfirm)
        val btnPartyInviteReject: Button = itemView.findViewById(R.id.btnPartyInviteReject)
    }

    interface ItemObserver {
        fun onClick(index: Int)
        fun onConfirm(index: Int)
        fun onReject(index: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyInviteViewHolder {
        return PartyInviteViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_party_invite, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PartyInviteViewHolder, position: Int) {
        holder.apply {
            labInviteSenderName.text = "${data[position].sender!!.nickName}的邀請"
            labInvitePartyName.text = data[position].party!!.title
            imagePartyInviteAvatar.setImageBitmap(data[position].restaurantImage)
            framePartyInvite.setOnClickListener { itemObserver.onClick(position) }
            btnPartyInviteConfirm.setOnClickListener { itemObserver.onConfirm(position) }
            btnPartyInviteReject.setOnClickListener { itemObserver.onReject(position) }
        }
    }
    override fun getItemCount(): Int = data.size
}
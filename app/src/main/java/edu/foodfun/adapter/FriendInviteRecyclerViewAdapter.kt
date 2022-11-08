package edu.foodfun.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.makeramen.roundedimageview.RoundedImageView
import edu.foodfun.R
import edu.foodfun.model.User
import edu.foodfun.uistate.UserUIState

class FriendInviteRecyclerViewAdapter (private val data: List<UserUIState>, private val InviteItemObserver: FriendInviteItemObserver)
    : RecyclerView.Adapter<FriendInviteRecyclerViewAdapter.FriendInviteViewHolder>() {
    class FriendInviteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtInviteFriendNickName)
        val imageAvatar: RoundedImageView = itemView.findViewById(R.id.imageFriendInviteAvatar)
        val btnAccept: FloatingActionButton = itemView.findViewById(R.id.btnFriendInviteAccept)
        val btnRefuse: FloatingActionButton = itemView.findViewById(R.id.btnFriendInviteRefuse)
    }

    interface FriendInviteItemObserver {
        fun onRefuse(index: Int, user: User)
        fun onAccept(index: Int, user: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendInviteViewHolder {
        return FriendInviteViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_friend_invite, parent, false))
    }

    override fun onBindViewHolder(holder: FriendInviteViewHolder, position: Int) {
        holder.apply {
            val user = data[position].user!!
            val avatar = data[position].avatar
            btnAccept.setOnClickListener { InviteItemObserver.onAccept(holder.layoutPosition, user) }
            btnRefuse.setOnClickListener { InviteItemObserver.onRefuse(holder.layoutPosition, user) }
            txtName.text = user.nickName
            imageAvatar.setImageBitmap(avatar)
        }
    }
    override fun getItemCount(): Int = data.size
}
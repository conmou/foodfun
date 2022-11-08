package edu.foodfun.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.makeramen.roundedimageview.RoundedImageView
import edu.foodfun.R
import edu.foodfun.model.User
import edu.foodfun.uistate.UserUIState

class FriendManageRecyclerViewAdapter(private val data: List<UserUIState>, private val ManageItemObserver: FriendManageItemObserver)
    : RecyclerView.Adapter<FriendManageRecyclerViewAdapter.FriendManagerViewHolder>() {
    class FriendManagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtFriendManageName)
        val imageAvatar: RoundedImageView = itemView.findViewById(R.id.imageItemAvatar)
        val txtDelete: TextView = itemView.findViewById(R.id.txtDeleteFriend)
    }

    interface FriendManageItemObserver {
        fun onDelete(index: Int, user: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendManagerViewHolder {
        return FriendManagerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_friend_manage, parent, false))
    }

    override fun onBindViewHolder(holder: FriendManagerViewHolder, position: Int) {
        holder.apply {
            val user = data[position].user!!
            val avatar = data[position].avatar
            txtDelete.setOnClickListener { ManageItemObserver.onDelete(holder.layoutPosition, user) }
            txtName.text = user.nickName
            imageAvatar.setImageBitmap(avatar)
        }
    }
    override fun getItemCount(): Int = data.size
}
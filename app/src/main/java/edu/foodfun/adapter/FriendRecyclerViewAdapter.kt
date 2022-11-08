package edu.foodfun.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.makeramen.roundedimageview.RoundedImageView
import edu.foodfun.R
import edu.foodfun.uistate.UserUIState

class FriendRecyclerViewAdapter(private val data: List<UserUIState>, private val callBackListener: CallBackListener)
    : RecyclerView.Adapter<FriendRecyclerViewAdapter.FriendViewHolder>() {
    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val frame : ConstraintLayout = itemView.findViewById(R.id.frameFriend)
        val txtName: TextView = itemView.findViewById(R.id.txtContactItemName)
        val imageAvatar: RoundedImageView = itemView.findViewById(R.id.imageContactItemAvatar)
    }

    interface CallBackListener {
        fun onItemClick(index: Int) {}
        fun onAvatarItemClick(index: Int) {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        return FriendViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false))
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.apply {
            val user = data[position].user!!
            val avatar = data[position].avatar
            frame.setOnClickListener { callBackListener.onItemClick(position) }
            txtName.text = user.nickName
            imageAvatar.setImageBitmap(avatar)
            imageAvatar.setOnClickListener { callBackListener.onAvatarItemClick(position) }
        }
    }
    override fun getItemCount(): Int = data.size
}
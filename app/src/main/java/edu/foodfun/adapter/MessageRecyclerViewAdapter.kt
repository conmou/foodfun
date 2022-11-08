package edu.foodfun.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.foodfun.R
import edu.foodfun.adapter.MessageRecyclerViewAdapter.Const.LOCAL
import edu.foodfun.adapter.MessageRecyclerViewAdapter.Const.REMOTE
import edu.foodfun.uistate.MessageUIState

class MessageRecyclerViewAdapter(private var data: MutableList<MessageUIState>, private val userId: String, private val itemObserver: ItemObserver)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    object Const {
        const val LOCAL = 0
        const val REMOTE = 1
        const val SYSTEM = 2
    }

    interface ItemObserver {
        fun onClick(index: Int)
    }

    inner class CurrentUserMessageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val currentUserChatbox: TextView = itemView.findViewById(R.id.labCurrentUserChatbox)
    }

    inner class OtherUserMessageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val otherUserName: TextView = itemView.findViewById(R.id.labOtherUserName)
        val otherUserChatbox: TextView = itemView.findViewById(R.id.labOtherUserChatbox)
        val otherUserAvatar: ImageView = itemView.findViewById(R.id.imgOtherUserAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == LOCAL) {
            CurrentUserMessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_my_chat_message, parent, false))
        }
        else {
            OtherUserMessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_others_chat_message, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = data[position].message!!
        val userUIState = data[position].userUIState

        if (getItemViewType(position) == LOCAL) {
            CurrentUserMessageViewHolder(holder.itemView).apply {
                currentUserChatbox.text = message.content
            }
        }
        else {
            val name = userUIState?.user?.nickName
            val avatar = userUIState?.avatar
            OtherUserMessageViewHolder(holder.itemView).apply {
                otherUserName.text = name
                otherUserChatbox.text = message.content
                otherUserAvatar.setImageBitmap(avatar)
                otherUserAvatar.setOnClickListener { itemObserver.onClick(position) }
            }
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
        return if(data[position].message!!.sender == userId)
            LOCAL
        else
            REMOTE
    }
}
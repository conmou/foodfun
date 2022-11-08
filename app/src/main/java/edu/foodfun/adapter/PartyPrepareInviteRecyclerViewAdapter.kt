package edu.foodfun.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.makeramen.roundedimageview.RoundedImageView
import edu.foodfun.R
import edu.foodfun.uistate.UserUIState

class PartyPrepareInviteRecyclerViewAdapter(private val data: List<UserUIState>, private val callBackListener: CallBackListener)
    : RecyclerView.Adapter<PartyPrepareInviteRecyclerViewAdapter.PartyPrepareInviteViewHolder>() {
    class PartyPrepareInviteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val frame : ConstraintLayout = itemView.findViewById(R.id.frameInvite)
        val txtName: TextView = itemView.findViewById(R.id.txtInviteUserName)
        val imageAvatar: RoundedImageView = itemView.findViewById(R.id.imageInviteItemAvatar)
        val btnInvite: Button = itemView.findViewById(R.id.btnInvite)
    }

    interface CallBackListener {
        fun onItemClick(index: Int) {}
        fun onBtnClick(index: Int) {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyPrepareInviteViewHolder {
        return PartyPrepareInviteViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_party_prepare_invite, parent, false))
    }

    override fun onBindViewHolder(holder: PartyPrepareInviteViewHolder, position: Int) {
        holder.apply {
            val name = data[position].user!!.nickName
            val avatar = data[position].avatar
            txtName.text = name
            imageAvatar.setImageBitmap(avatar)
            frame.setOnClickListener { callBackListener.onItemClick(position) }
            btnInvite.setOnClickListener { callBackListener.onBtnClick(position) }
        }
    }

    override fun getItemCount(): Int = data.size
}
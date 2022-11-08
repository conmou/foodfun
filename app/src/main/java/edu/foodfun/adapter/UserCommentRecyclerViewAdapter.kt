package edu.foodfun.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.foodfun.R
import edu.foodfun.model.UserComment
import java.text.SimpleDateFormat
import java.util.*

class UserCommentRecyclerViewAdapter(private val data: List<UserComment>): RecyclerView.Adapter<UserCommentRecyclerViewAdapter.UserCommentViewHolder>() {
    class UserCommentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.labUserComment)
        val time: TextView = itemView.findViewById(R.id.labUserCommentTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserCommentViewHolder {
        return UserCommentViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_user_comment, parent, false))
    }

    override fun onBindViewHolder(holder: UserCommentViewHolder, position: Int) {
        val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault(Locale.Category.FORMAT))
        val timeDifference = 28800
        formatter.timeZone = TimeZone.getTimeZone("asia/taipei")

        holder.apply {
            content.text = data[position].content
            time.text = formatter.format(Date((data[position].time!!.seconds + timeDifference) * 1000 )).toString()
        }
    }

    override fun getItemCount(): Int = data.size
}
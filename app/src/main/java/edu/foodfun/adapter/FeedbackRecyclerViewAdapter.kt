package edu.foodfun.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import edu.foodfun.R
import edu.foodfun.enums.AchievementType
import edu.foodfun.uistate.UserUIState

class FeedbackRecyclerViewAdapter(private val data: MutableList<UserUIState>, private val listener: FeedBackListener)
    : RecyclerView.Adapter<FeedbackRecyclerViewAdapter.FeedbackViewHolder>() {
    class FeedbackViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.imgQuestionAvatar)
        val name: TextView = itemView.findViewById(R.id.labQuestionName)
        val comment: EditText = itemView.findViewById(R.id.txtQuestionComment)
        val btnHumorous: CheckBox = itemView.findViewById(R.id.btnHumorous)
        val btnChatty: CheckBox = itemView.findViewById(R.id.btnChatty)
        val btnConsiderate: CheckBox = itemView.findViewById(R.id.btnConsiderate)
        val btnIntroverted: CheckBox = itemView.findViewById(R.id.btnIntroverted)
        val btnLech: CheckBox = itemView.findViewById(R.id.btnLech)
    }

    open class FeedBackListener {
        open fun onAchievementClick(index: Int, achievementType: AchievementType, isSeleted: Boolean){}
        open fun onCommentChanged(index: Int, content: String){}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        return FeedbackViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_party_ques, parent, false))
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        holder.apply {
            avatar.setImageBitmap(data[position].avatar)
            name.text = data[position].user!!.nickName
            btnHumorous.setOnClickListener { listener.onAchievementClick(position, AchievementType.HUMOROUS, btnHumorous.isChecked) }
            btnChatty.setOnClickListener { listener.onAchievementClick(position, AchievementType.CHATTY, btnChatty.isChecked) }
            btnConsiderate.setOnClickListener { listener.onAchievementClick(position, AchievementType.CONSIDERATE, btnConsiderate.isChecked) }
            btnIntroverted.setOnClickListener { listener.onAchievementClick(position, AchievementType.INTROVERTED, btnIntroverted.isChecked) }
            btnLech.setOnClickListener { listener.onAchievementClick(position, AchievementType.LECH, btnLech.isChecked) }
            comment.doAfterTextChanged { listener.onCommentChanged(position, comment.text.toString()) }
        }
    }

    override fun getItemCount(): Int = data.size
}
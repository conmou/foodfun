package edu.foodfun.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import edu.foodfun.R

class GroupRecyclerViewAdapter(private val data: MutableList<String>, private val itemObserver: ItemObserver)
    : RecyclerView.Adapter<GroupRecyclerViewAdapter.GroupViewHolder>() {
    class GroupViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val txtGroupName: EditText = itemView.findViewById(R.id.editGroupName)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteGroup)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnRenameGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        return GroupViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_dialog_group, parent, false))
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.apply {
            val inputMethodManager = txtGroupName.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            var counter = 0
            txtGroupName.setText(data[position])
            txtGroupName.isEnabled = false
            btnDelete.setOnClickListener { itemObserver.onDelete(position) }
            btnEdit.setOnClickListener {
                counter += 1
                if(counter % 2 == 1) {
                    btnEdit.setImageResource(R.drawable.ic_baseline_check_24)
                    btnEdit.background.setTint(ContextCompat.getColor(btnEdit.context, R.color.checkBG))
                    txtGroupName.isEnabled = true
                    txtGroupName.requestFocus()
                    inputMethodManager.showSoftInput(txtGroupName, 0)
                    txtGroupName.setBackgroundResource(R.drawable.inputbox_dialog_group_manage)
                }
                else {
                    btnEdit.setImageResource(R.drawable.ic_baseline_edit_24)
                    btnEdit.background.setTint(ContextCompat.getColor(btnEdit.context, R.color.editBG))
                    txtGroupName.isEnabled = false
                    txtGroupName.requestFocus()
                    inputMethodManager.hideSoftInputFromWindow(txtGroupName.rootView.windowToken, 0)
                    txtGroupName.setBackgroundResource(R.color.white)
                    if (data[position] != txtGroupName.text.toString()) itemObserver.onGroupNameChanged(data[position], txtGroupName.text.toString())
                }
            }
        }
    }

    override fun getItemCount(): Int = data.size

    interface ItemObserver {
        fun onDelete(index: Int)
        fun onGroupNameChanged(oldGroupName: String, newGroupName: String)
    }
}
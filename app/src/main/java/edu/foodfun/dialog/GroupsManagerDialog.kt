package edu.foodfun.dialog

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.foodfun.R
import edu.foodfun.adapter.GroupRecyclerViewAdapter
import edu.foodfun.viewmodel.ListViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class GroupsManagerDialog(private val callbackListener: CallBackListener): DialogFragment() {
    private lateinit var txtGroupName: TextView
    private lateinit var btnAddGroup: FloatingActionButton
    private lateinit var btnComplete: ExtendedFloatingActionButton
    private lateinit var recyclerViewGroupList: RecyclerView
    private val vm: ListViewModel by activityViewModels()
    private var groupNameList: MutableList<String> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_groups_manage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtGroupName = view.findViewById(R.id.txtGroupName)
        btnAddGroup = view.findViewById(R.id.btnAddGroup)
        btnComplete = view.findViewById(R.id.btnCompletedManage)
        recyclerViewGroupList = view.findViewById(R.id.recyclerGroupList)

        recyclerViewGroupList.layoutManager = LinearLayoutManager(context)
        recyclerViewGroupList.adapter = GroupRecyclerViewAdapter(groupNameList, object : GroupRecyclerViewAdapter.ItemObserver {
            override fun onDelete(index: Int) {
                AlertDialog.Builder(context)
                    .setMessage("刪除群組後，群組內剩餘的餐廳將自動移動至未分類")
                    .setPositiveButton("刪除") { dialog, _ ->
                        lifecycleScope.launch {
                            vm.removeGroup(groupNameList[index])
                            dialog.dismiss()
                            dismiss()
                        }
                    }
                    .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            }

            override fun onGroupNameChanged(oldGroupName: String, newGroupName: String) {
                lifecycleScope.launchWhenCreated { vm.renameGroup(oldGroupName, newGroupName) }
            }
        })

        btnAddGroup.setOnClickListener {
            if (txtGroupName.text.isNullOrEmpty()) {
                Toast.makeText(context, "請輸入群組名稱", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (groupNameList.contains(txtGroupName.text.toString())) {
                Toast.makeText(context, "群組名稱已存在", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            callbackListener.onAddGroup(txtGroupName.text.toString())
            txtGroupName.text = null
        }

        btnComplete.setOnClickListener {
            callbackListener.onComplete()
            dismiss()
        }

        launchFlow()
    }

    private fun launchFlow() {
        lifecycleScope.launchWhenCreated {
            vm.currentUserUIState.filterNotNull().collectLatest { userUIState ->
                val newGroupNameList = userUIState.user!!.groups.keys.toList()
                val addGroupNames = newGroupNameList.subtract(groupNameList.toSet())
                val removeGroupNames = groupNameList.subtract(userUIState.user.groups.keys)
                addGroupNames.forEach { groupName ->
                    groupNameList.add(groupName)
                    recyclerViewGroupList.adapter?.notifyItemInserted(groupNameList.size)
                }
                removeGroupNames.forEach { groupName ->
                    val target = groupNameList.indexOfFirst { it == groupName }
                    groupNameList.removeAt(target)
                    recyclerViewGroupList.adapter?.notifyItemRemoved(target)
                    recyclerViewGroupList.adapter?.notifyItemRangeChanged(target, groupNameList.size)
                }
            }
        }
    }

    open class CallBackListener {
        open fun onAddGroup(groupName: String) {}
        open fun onComplete() {}
    }
}

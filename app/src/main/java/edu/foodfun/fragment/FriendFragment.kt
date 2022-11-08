package edu.foodfun.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.databinding.ObservableArrayMap
import androidx.databinding.ObservableMap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import edu.foodfun.ModelChanges
import edu.foodfun.activity.FriendInviteActivity
import edu.foodfun.activity.FriendManageActivity
import edu.foodfun.R
import edu.foodfun.activity.ChatRoomActivity
import edu.foodfun.activity.FriendAddActivity
import edu.foodfun.adapter.FriendRecyclerViewAdapter
import edu.foodfun.dialog.UserDetailDialog
import edu.foodfun.enums.UserTemplateType
import edu.foodfun.uistate.UserUIState
import edu.foodfun.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class FriendFragment : Fragment() {
    private lateinit var recyclerFriendList: RecyclerView
    private lateinit var btnFriendInvite: ExtendedFloatingActionButton
    private lateinit var btnFriendManage: ExtendedFloatingActionButton
    private lateinit var btnQRCode: FloatingActionButton
    private lateinit var editSearch : EditText
    private lateinit var btnSearch : ImageButton
    private lateinit var labInviteCount: TextView
    private val vm: MainViewModel by activityViewModels()
    private val inviteList: MutableList<UserUIState> = mutableListOf()
    private val friendList: MutableList<UserUIState> = mutableListOf()
    private val searchlist : MutableList<UserUIState> = mutableListOf()
    private var isAdapterSet: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_friend, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnFriendInvite = view.findViewById(R.id.btnFriendInvite)
        btnFriendManage = view.findViewById(R.id.btnFriendManage)
        recyclerFriendList = view.findViewById(R.id.recyclerFriendManageList)
        btnQRCode = view.findViewById(R.id.btnQRCode)
        editSearch = view.findViewById(R.id.editfriendSearch)
        btnSearch = view.findViewById(R.id.btnFriendSearch)
        labInviteCount = view.findViewById(R.id.labInviteCount)

        editSearch.addTextChangedListener {
            if(!isAdapterSet) {
                recyclerFriendList.adapter = FriendRecyclerViewAdapter(searchlist, object : FriendRecyclerViewAdapter.CallBackListener {
                    override fun onItemClick(index: Int) {
                        val intent = Intent(activity, ChatRoomActivity::class.java)
                        intent.putExtra("currentUserId", vm.currentUserUIState.value!!.user!!.id)
                        intent.putExtra("friendId", searchlist[index].user!!.id)
                        intent.putExtra("friendName", searchlist[index].user!!.nickName)
                        startActivity(intent)
                    }

                    override fun onAvatarItemClick(index: Int) {
                        UserDetailDialog(friendList[index].user!!.id!!, UserTemplateType.FRIEND).show(childFragmentManager, "FriendInviteDialog")
                    }
                })
            }
            searchlist.clear()
            recyclerFriendList.adapter?.notifyDataSetChanged()
            val txtSearch = editSearch.text.toString()
            if (txtSearch.isNotEmpty()) {
                friendList.forEach {
                    val nickname = it.user?.nickName!!
                    if (txtSearch in nickname && it !in searchlist) {
                        searchlist.add(it)
                        recyclerFriendList.adapter?.notifyItemInserted(searchlist.size)
                    }
                    else if(txtSearch !in nickname && it in searchlist) {
                        searchlist.remove(it)
                        recyclerFriendList.adapter?.notifyItemRemoved(searchlist.indexOf(it))
                    }
                }
            }
            else {
                friendList.forEach {
                    searchlist.add(it)
                    recyclerFriendList.adapter?.notifyItemInserted(searchlist.size)
                }
            }
            isAdapterSet = true
        }

        btnFriendInvite.setOnClickListener { startActivity(Intent(activity, FriendInviteActivity::class.java)) }
        btnFriendManage.setOnClickListener { startActivity(Intent(activity, FriendManageActivity::class.java)) }
        btnQRCode.setOnClickListener { startActivity(Intent(activity, FriendAddActivity::class.java)) }

        recyclerFriendList.adapter = FriendRecyclerViewAdapter(friendList, object : FriendRecyclerViewAdapter.CallBackListener {
            override fun onItemClick(index: Int) {
                val intent = Intent(activity, ChatRoomActivity::class.java).apply {
                    putExtra("friendId", friendList[index].user!!.id)
                }
                startActivity(intent)
            }
        })
        recyclerFriendList.layoutManager = LinearLayoutManager(context)

        launchFlow()
    }

    private fun launchFlow() {
        lifecycleScope.launchWhenCreated {
            val adapter = recyclerFriendList.adapter
            val mapChangedCallback = object : ObservableMap.OnMapChangedCallback<ObservableArrayMap<String, ModelChanges<UserUIState>>, String, ModelChanges<UserUIState>>() {
                override fun onMapChanged(sender: ObservableArrayMap<String, ModelChanges<UserUIState>>?, key: String?) {
                    val changes = sender?.get(key)
                    val target = friendList.indexOfFirst { it.user!!.id == key }
                    if (changes != null) {
                        if (target == -1) {
                            friendList.add(changes.value)
                            lifecycleScope.launch {
                                withContext(Dispatchers.Main) { adapter?.notifyItemInserted(friendList.size) }
                            }
                        }
                        else {
                            friendList[target] = changes.value
                            lifecycleScope.launch {
                                withContext(Dispatchers.Main) { adapter?.notifyItemChanged(target) }
                            }
                        }
                    }
                    if (changes == null) {
                        if (target == -1) return
                        friendList.removeAt(target)
                        lifecycleScope.launch {
                            withContext(Dispatchers.Main) { adapter?.notifyItemRemoved(target) }
                        }
                    }
                }
            }

            vm.friendChanges.addOnMapChangedCallback(mapChangedCallback)
            synchronized(this) {
                vm.friendChanges.forEach {
                    val changes = it.value
                    friendList.add(changes.value)
                    adapter?.notifyItemInserted(friendList.size)
                }
            }
        }
        lifecycleScope.launchWhenCreated {
            val mapChangedCallback = object : ObservableMap.OnMapChangedCallback<ObservableArrayMap<String, ModelChanges<UserUIState>>, String, ModelChanges<UserUIState>>() {
                override fun onMapChanged(sender: ObservableArrayMap<String, ModelChanges<UserUIState>>?, key: String?) {
                    val changes = sender?.get(key)
                    val target = inviteList.indexOfFirst { it.user!!.id == key }
                    if (changes != null) {
                        if (target == -1) { inviteList.add(changes.value) }
                        else { inviteList[target] = changes.value }
                    }
                    if (changes == null) {
                        if (target == -1) return
                        inviteList.removeAt(target)
                    }
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main) {
                            inviteList.size.apply {
                                labInviteCount.visibility = if (this > 0) View.VISIBLE else View.INVISIBLE
                                labInviteCount.text = "$this"
                            }
                        }
                    }
                }
            }

            vm.inviteChanges.addOnMapChangedCallback(mapChangedCallback)
            synchronized(this) {
                vm.inviteChanges.forEach {
                    val changes = it.value
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main) {
                            inviteList.apply {
                                add(changes.value)
                                labInviteCount.visibility = if (this.size > 0) View.VISIBLE else View.INVISIBLE
                                labInviteCount.text = "${this.size}"
                            }
                        }
                    }
                }
            }
        }
    }
}

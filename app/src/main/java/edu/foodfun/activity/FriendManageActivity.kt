package edu.foodfun.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.ObservableArrayMap
import androidx.databinding.ObservableMap
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import edu.foodfun.ModelChanges
import edu.foodfun.R
import edu.foodfun.adapter.FriendManageRecyclerViewAdapter
import edu.foodfun.model.User
import edu.foodfun.uistate.UserUIState
import edu.foodfun.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FriendManageActivity : AppCompatActivity() {
    private lateinit var recyclerFriendManageList: RecyclerView
    private val vm: MainViewModel by viewModels()
    private val friendList: MutableList<UserUIState> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_manage)

        recyclerFriendManageList = findViewById(R.id.recyclerFriendManageList)

        setSupportActionBar(findViewById(R.id.toolbarFriendManage))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val friendManageRecyclerViewAdapter = FriendManageRecyclerViewAdapter(friendList, object : FriendManageRecyclerViewAdapter.FriendManageItemObserver {
            override fun onDelete(index: Int, user: User) {
                lifecycleScope.launch { vm.removeFriend(user.id!!) }
            }
        })
        recyclerFriendManageList.layoutManager = LinearLayoutManager(this)
        recyclerFriendManageList.adapter = friendManageRecyclerViewAdapter
        launchFlow()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }

    private fun launchFlow() {
        lifecycleScope.launchWhenCreated {
            val adapter = recyclerFriendManageList.adapter
            val mapChangedCallback = object : ObservableMap.OnMapChangedCallback<ObservableArrayMap<String, ModelChanges<UserUIState>>, String, ModelChanges<UserUIState>>() {
                override fun onMapChanged(sender: ObservableArrayMap<String, ModelChanges<UserUIState>>?, key: String?) {
                    val changes = sender?.get(key)
                    val target = friendList.indexOfFirst { it.user!!.id == key }
                    if (changes != null) {
                        if (target == -1) {
                            friendList.add(changes.value)
                            adapter?.notifyItemInserted(friendList.size)
                        }
                        else {
                            friendList[target] = changes.value
                            adapter?.notifyItemChanged(target)
                        }
                    }
                    if (changes == null) {
                        if (target == -1) return
                        friendList.removeAt(target)
                        adapter?.notifyItemRemoved(target)
                    }
                    supportActionBar?.title = "${friendList.size}位朋友"
                }
            }

            vm.friendChanges.addOnMapChangedCallback(mapChangedCallback)
            synchronized(this) {
                vm.friendChanges.forEach {
                    val changes = it.value
                    friendList.add(changes.value)
                    adapter?.notifyItemInserted(friendList.size)
                    supportActionBar?.title = "${friendList.size}位朋友"
                }
            }
        }
    }
}
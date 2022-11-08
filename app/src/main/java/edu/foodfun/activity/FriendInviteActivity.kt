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
import edu.foodfun.adapter.FriendInviteRecyclerViewAdapter
import edu.foodfun.model.User
import edu.foodfun.uistate.UserUIState
import edu.foodfun.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class FriendInviteActivity : AppCompatActivity() {
    private lateinit var recyclerFriendInviteList: RecyclerView
    private val vm: MainViewModel by viewModels()
    private val inviteList: MutableList<UserUIState> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_invite)

        recyclerFriendInviteList = findViewById(R.id.recyclerFriendInviteList)

        setSupportActionBar(findViewById(R.id.toolbarFriendInvite))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.friend_invite)

        val friendInviteRecyclerViewAdapter = FriendInviteRecyclerViewAdapter(inviteList, object : FriendInviteRecyclerViewAdapter.FriendInviteItemObserver {
            override fun onRefuse(index: Int, user: User) {
                lifecycleScope.launch { vm.refuseInvite(user.id!!) }
            }
            override fun onAccept(index: Int, user: User) {
                lifecycleScope.launch { vm.acceptInvite(user.id!!) }
            }
        })
        recyclerFriendInviteList.layoutManager = LinearLayoutManager(this)
        recyclerFriendInviteList.adapter = friendInviteRecyclerViewAdapter

        launchFlow()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }

    private fun launchFlow() {
        lifecycleScope.launchWhenCreated {
            val adapter = recyclerFriendInviteList.adapter
            val mapChangedCallback = object : ObservableMap.OnMapChangedCallback<ObservableArrayMap<String, ModelChanges<UserUIState>>, String, ModelChanges<UserUIState>>() {
                override fun onMapChanged(sender: ObservableArrayMap<String, ModelChanges<UserUIState>>?, key: String?) {
                    val changes = sender?.get(key)
                    val target = inviteList.indexOfFirst { it.user!!.id!! == key }
                    if (changes != null) {
                        if (target == -1) {
                            inviteList.add(changes.value)
                            lifecycleScope.launch {
                                withContext(Dispatchers.Main) {adapter?.notifyItemInserted(inviteList.size) }
                            }
                        }
                        else {
                            inviteList[target] = changes.value
                            lifecycleScope.launch {
                                withContext(Dispatchers.Main) { adapter?.notifyItemChanged(target) }
                            }
                        }
                    }
                    if (changes == null) {
                        if (target == -1) return
                        inviteList.removeAt(target)
                        lifecycleScope.launch {
                            withContext(Dispatchers.Main) { adapter?.notifyItemRemoved(target) }
                        }
                    }
                }
            }

            vm.inviteChanges.addOnMapChangedCallback(mapChangedCallback)
            synchronized(this) {
                vm.inviteChanges.forEach {
                    val changes = it.value
                    inviteList.add(changes.value)
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main) { adapter?.notifyItemInserted(inviteList.size) }
                    }
                }
            }
        }
    }
}
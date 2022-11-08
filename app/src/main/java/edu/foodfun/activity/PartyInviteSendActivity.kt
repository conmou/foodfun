package edu.foodfun.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.ObservableArrayMap
import androidx.databinding.ObservableMap
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import edu.foodfun.ModelChanges
import edu.foodfun.R
import edu.foodfun.adapter.PartyPrepareInviteRecyclerViewAdapter
import edu.foodfun.dialog.UserDetailDialog
import edu.foodfun.enums.UserTemplateType
import edu.foodfun.fragment.PartyPrepareFragment
import edu.foodfun.uistate.UserUIState
import edu.foodfun.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PartyInviteSendActivity : AppCompatActivity() {
    lateinit var tabInviteFriend: AppCompatButton
    lateinit var tabInviteNearby: AppCompatButton
//    lateinit var btnBack: ImageButton
    lateinit var recyclerInviteList: RecyclerView
    private val friendInviteList: MutableList<UserUIState> = mutableListOf()
    private val nearbyInviteList: MutableList<UserUIState> = mutableListOf()
    private var currentInviteList: MutableList<UserUIState> = mutableListOf()
    private val vm: MainViewModel by viewModels()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party_invite_send)

//        btnBack = findViewById(R.id.btnBack)
        tabInviteFriend = findViewById(R.id.tabInviteFriend)
        tabInviteNearby = findViewById(R.id.tabInviteNearby)
        recyclerInviteList = findViewById(R.id.recyclerInviteList)

        setSupportActionBar(findViewById(R.id.toolbarInviteSend))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "邀請飯友"

//        btnBack.setOnClickListener { startActivity(Intent(this, PartyPrepareFragment::class.java)) }

        val inviteRecyclerViewAdapter = PartyPrepareInviteRecyclerViewAdapter(currentInviteList, object : PartyPrepareInviteRecyclerViewAdapter.CallBackListener {
            override fun onItemClick(index: Int) {
                UserDetailDialog(currentInviteList[index].user!!.id!!, UserTemplateType.STRANGER).show(supportFragmentManager,"FriendInviteDialog")
            }

            override fun onBtnClick(index: Int) {
                lifecycleScope.launch { vm.sendPartyInvite(vm.currentPartyUIState.value!!.party!!.id!!, currentInviteList[index].user!!.id!!) }
            }
        })

        tabInviteFriend.setOnClickListener {
            currentInviteList.clear()
            recyclerInviteList.adapter?.notifyDataSetChanged()
            friendInviteList.forEach {
                currentInviteList.add(it)
                recyclerInviteList.adapter?.notifyItemInserted(currentInviteList.size)
            }
        }

        tabInviteNearby.setOnClickListener {
            currentInviteList.clear()
            recyclerInviteList.adapter?.notifyDataSetChanged()
            nearbyInviteList.forEach {
                currentInviteList.add(it)
                recyclerInviteList.adapter?.notifyItemInserted(currentInviteList.size)
            }
        }

        recyclerInviteList.layoutManager = LinearLayoutManager(this)
        recyclerInviteList.adapter = inviteRecyclerViewAdapter

        launchFlow()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun launchFlow() {
        lifecycleScope.launchWhenCreated {
            val mapChangedCallback = object : ObservableMap.OnMapChangedCallback<ObservableArrayMap<String, ModelChanges<UserUIState>>, String, ModelChanges<UserUIState>>() {
                override fun onMapChanged(sender: ObservableArrayMap<String, ModelChanges<UserUIState>>?, key: String?) {
                    val changes = sender?.get(key)
                    val target = friendInviteList.indexOfFirst { it.user!!.id == key }
                    if (changes != null) {
                        if (target == -1) {
                            friendInviteList.add(changes.value)
                        }
                        else {
                            friendInviteList[target] = changes.value
                        }
                    }
                    if (changes == null) {
                        if (target == -1) return
                        friendInviteList.removeAt(target)
                    }
                }
            }

            vm.friendChanges.addOnMapChangedCallback(mapChangedCallback)
            vm.friendChanges.forEach {
                val changes = it.value
                if(vm.currentPartyUIState.value!!.party!!.users.contains(changes.value.user!!.id)) return@forEach
                friendInviteList.add(changes.value)
            }
        }.invokeOnCompletion {
            friendInviteList.forEach {
                currentInviteList.add(it)
                recyclerInviteList.adapter?.notifyItemInserted(currentInviteList.size)
            }
        }

        lifecycleScope.launchWhenCreated {
            val mapChangedCallback = object : ObservableMap.OnMapChangedCallback<ObservableArrayMap<String, ModelChanges<UserUIState>>, String, ModelChanges<UserUIState>>() {
                override fun onMapChanged(sender: ObservableArrayMap<String, ModelChanges<UserUIState>>?, key: String?) {
                    val changes = sender?.get(key)
                    val target = nearbyInviteList.indexOfFirst { it.user!!.id == key }
                    if (changes != null) {
                        if (target == -1) {
                            nearbyInviteList.add(changes.value)
                        }
                        else {
                            nearbyInviteList[target] = changes.value
                        }
                    }
                    if (changes == null) {
                        if (target == -1) return
                        nearbyInviteList.removeAt(target)
                    }
                }
            }

            vm.nearbyUserChanges.addOnMapChangedCallback(mapChangedCallback)
            vm.nearbyUserChanges.forEach {
                val changes = it.value
                if(vm.currentPartyUIState.value!!.party!!.users.contains(changes.value.user!!.id)) return@forEach
                nearbyInviteList.add(changes.value)
            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }

}
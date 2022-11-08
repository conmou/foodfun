package edu.foodfun.activity

import android.app.AlertDialog
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
import edu.foodfun.adapter.PartyInviteRecyclerViewAdapter
import edu.foodfun.dialog.PartyDetailDialog
import edu.foodfun.uistate.InviteUIState
import edu.foodfun.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class PartyInviteCheckActivity : AppCompatActivity() {
    private lateinit var recyclerPartyInviteList: RecyclerView
    private val vm: MainViewModel by viewModels()
    private val partyInviteList: MutableList<InviteUIState> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party_invite)

        recyclerPartyInviteList = findViewById(R.id.recyclerPartyInviteList)

        setSupportActionBar(findViewById(R.id.toolbarPartyInvite))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "派對邀請"
        val partyInviteRecyclerViewAdapter = PartyInviteRecyclerViewAdapter(partyInviteList, object : PartyInviteRecyclerViewAdapter.ItemObserver {
            override fun onClick(index: Int) {
                PartyDetailDialog(partyInviteList[index]).show(this@PartyInviteCheckActivity.supportFragmentManager,"PartyDetailDialog")
            }

            override fun onConfirm(index: Int) {
                if(vm.currentPartyUIState.value != null) {
                    AlertDialog.Builder(this@PartyInviteCheckActivity)
                        .setMessage("確認要離開現有的房間\n加入此房間嗎")
                        .setPositiveButton("確認") { dialog, _ ->
                            lifecycleScope.launch {
                                vm.leaveCurrentParty()
                                vm.joinParty(partyInviteList[index].party!!.id!!)
                                vm.acceptPartyInvite(partyInviteList[index].invite!!)
                            }
                            dialog.dismiss()
                        }
                        .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
                        .create()
                        .show()
                }
                else {
                    lifecycleScope.launch {
                        vm.joinParty(partyInviteList[index].party!!.id!!)
                        vm.acceptPartyInvite(partyInviteList[index].invite!!)
                    }
                }
            }

            override fun onReject(index: Int) {
                lifecycleScope.launch { vm.rejectPartyInvite(partyInviteList[index].invite!!.id!!) }
            }
        })
        recyclerPartyInviteList.layoutManager = LinearLayoutManager(this)
        recyclerPartyInviteList.adapter = partyInviteRecyclerViewAdapter

        launchFlow()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }

    private fun launchFlow() {
        lifecycleScope.launchWhenCreated {
            val adapter = recyclerPartyInviteList.adapter
            val mapChangedCallback = object : ObservableMap.OnMapChangedCallback<ObservableArrayMap<String, ModelChanges<InviteUIState>>, String, ModelChanges<InviteUIState>>() {
                override fun onMapChanged(sender: ObservableArrayMap<String, ModelChanges<InviteUIState>>?, key: String?) {
                    val changes = sender?.get(key)
                    val target = partyInviteList.indexOfFirst { it.invite!!.id!! == key }
                    if (changes != null) {
                        if (changes.value.invite!!.state != "等待中" || changes.value.invite.party == vm.currentPartyUIState.value!!.party!!.id) {
                            if (target == -1) return
                            partyInviteList.removeAt(target)
                            lifecycleScope.launch {
                                withContext(Dispatchers.Main) { adapter?.notifyItemRemoved(target) }
                            }
                        }
                        else if (target == -1) {
                            partyInviteList.add(changes.value)
                            lifecycleScope.launch {
                                withContext(Dispatchers.Main) { adapter?.notifyItemInserted(partyInviteList.size) }
                            }
                        }
                        else {
                            partyInviteList[target] = changes.value
                            lifecycleScope.launch {
                                withContext(Dispatchers.Main) { adapter?.notifyItemChanged(target) }
                            }
                        }
                    }
//                    if (changes == null || changes.value.invite!!.state == "已失效") {
//                        if (target == -1) return
//                            partyInviteList.removeAt(target)
//                            lifecycleScope.launch {
//                                withContext(Dispatchers.Main) { adapter?.notifyItemRemoved(target) }
//                            }
//                    }
                }
            }

            vm.partyInviteChanges.addOnMapChangedCallback(mapChangedCallback)
            synchronized(this) {
                vm.partyInviteChanges.forEach {
                    val changes = it.value
                    if (changes.value.invite!!.state == "已失效" || changes.value.invite.party == vm.currentPartyUIState.value?.party?.id) return@forEach
                    partyInviteList.add(changes.value)
                    adapter?.notifyItemInserted(partyInviteList.size)
                }
            }
        }
    }
}
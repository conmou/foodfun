package edu.foodfun.fragment

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.databinding.ObservableArrayMap
import androidx.databinding.ObservableMap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.Timestamp
import edu.foodfun.ModelChanges
import edu.foodfun.R
import edu.foodfun.activity.PartyActivity
import edu.foodfun.activity.PartyInviteCheckActivity
import edu.foodfun.adapter.NearbyUserRecyclerViewAdapter
import edu.foodfun.adapter.PartyRecyclerViewAdapter
import edu.foodfun.dialog.UserDetailDialog
import edu.foodfun.dialog.PartyCreateDialog
import edu.foodfun.dialog.PartyHasJoinedDialog
import edu.foodfun.dialog.PartyKeyInPasswordDialog
import edu.foodfun.enums.UserTemplateType
import edu.foodfun.hilt.MyApplication
import edu.foodfun.uistate.PartyUIState
import edu.foodfun.uistate.UserUIState
import edu.foodfun.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.droidsonroids.gif.GifImageView

class PartyFragment : Fragment() {
    private lateinit var recyclerPartyRoomList: RecyclerView
    private lateinit var recyclerNearbyList: RecyclerView
    private lateinit var btnPartyState: GifImageView
    private lateinit var btnCreateParty: Button
    private lateinit var btnPartyInviteManager: ExtendedFloatingActionButton
    private lateinit var editSearchParty: EditText
    private val vm: MainViewModel by activityViewModels()
    private val partyList: MutableList<PartyUIState> = mutableListOf()
    private val nearbyUserList: MutableList<UserUIState> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_party, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerNearbyList = view.findViewById(R.id.recyclerViewNearby)
        recyclerPartyRoomList = view.findViewById(R.id.recyclerViewPartys)
        btnCreateParty = view.findViewById(R.id.btnCreateParty)
        btnPartyState = view.findViewById(R.id.btnPartyState)
        editSearchParty = view.findViewById(R.id.editPartyRoomSearch)
        btnPartyInviteManager = view.findViewById(R.id.btnPartyInviteManager)

        btnCreateParty.setOnClickListener { showPartyCreateDialog() }
        btnPartyState.setOnClickListener { startActivity(Intent(context, PartyActivity::class.java)) }

        recyclerNearbyList.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL,false)
        recyclerNearbyList.adapter = NearbyUserRecyclerViewAdapter(nearbyUserList, object : NearbyUserRecyclerViewAdapter.ItemClickListener{
            override fun itemClick(holder: NearbyUserRecyclerViewAdapter.NearByUserViewHolder, position: Int) {
                UserDetailDialog(nearbyUserList[position].user!!.id!!, UserTemplateType.STRANGER).show(childFragmentManager,"FriendInviteDialog")
            }
        })

        recyclerPartyRoomList.layoutManager = GridLayoutManager(context, 2)
        val adapter = PartyRecyclerViewAdapter(partyList, object : PartyRecyclerViewAdapter.ItemClickListener {
            override fun itemClick(holder: PartyRecyclerViewAdapter.PartyViewHolder, position: Int) {
                val user = vm.currentUserUIState.value!!.user!! ?: return
                val party = partyList[position].party!!
                val intent = Intent(context, PartyActivity::class.java)
                if ((party.state == "進行中" || party.state == "已結束") && !party.users.contains(vm.currentUserUIState.value!!.user!!.id!!) && !party.users.contains(user.id!!)) {
                    Toast.makeText(context, "房間${party.state}", Toast.LENGTH_LONG).show()
                }
                else if (party.users.size == party.maxMember) Toast.makeText(context, "房間人數已達上限", Toast.LENGTH_LONG).show()
                else if (user.currentParty == party.id!!) context!!.startActivity(intent)
                else if (user.currentParty == null && !party.users.contains(user.id)) {
                    if (party.password?.isNotEmpty() == true) {
                        PartyKeyInPasswordDialog(requireContext(), object : PartyKeyInPasswordDialog.CallBackListener() {
                            override fun onConfirm(password: String) {
                                if (password != party.password) {
                                    Toast.makeText(context, "密碼輸入錯誤", Toast.LENGTH_LONG).show()
                                    return
                                }
                                lifecycleScope.launch {
                                    vm.leaveCurrentParty()
                                    vm.joinParty(party.id)
                                    context!!.startActivity(intent)
                                }
                            }
                        }).show()
                    }
                    else {
                        lifecycleScope.launch {
                            vm.leaveCurrentParty()
                            vm.joinParty(party.id)
                            context!!.startActivity(intent)
                        }
                    }
                }
                else if (user.currentParty != null && user.currentParty != party.id && !party.users.contains(user.id!!)) {
                    PartyHasJoinedDialog(requireContext(), object : PartyHasJoinedDialog.CallBackListener() {
                        override fun onConfirm() {
                            if (party.password?.isNotEmpty() == true) {
                                PartyKeyInPasswordDialog(requireContext(), object : PartyKeyInPasswordDialog.CallBackListener() {
                                    override fun onConfirm(password: String) {
                                        if (password != party.password) {
                                            Toast.makeText(context, "密碼輸入錯誤", Toast.LENGTH_LONG).show()
                                            return
                                        }
                                        lifecycleScope.launch {
                                            vm.leaveCurrentParty()
                                            vm.joinParty(party.id)
                                            context!!.startActivity(intent)
                                        }
                                    }
                                }).show()
                            }
                            else {
                                lifecycleScope.launch {
                                    vm.leaveCurrentParty()
                                    vm.joinParty(party.id)
                                    context!!.startActivity(intent)
                                }
                            }
                        }
                    }).show()
                }
            }
        })

        editSearchParty.addTextChangedListener { adapter.filter.filter(it) }
        recyclerPartyRoomList.adapter = adapter

        btnPartyInviteManager.setOnClickListener { startActivity(Intent(activity, PartyInviteCheckActivity::class.java)) }

        launchFlow()
    }

    private fun showPartyCreateDialog() {
        val callBackListener = object : PartyCreateDialog.PartyCreateCallBackListener() {
            override fun onConfirm(title: String, content: String, maxMember: Int, restaurantId: String, reservation: Timestamp, password: String?) {
                lifecycleScope.launch {
                    if(vm.currentUserUIState.value!!.user!!.currentParty != null)
                        Toast.makeText(context, "建立失敗，當前使用者已存在於房間。", Toast.LENGTH_LONG).show()
                    else {
                        vm.createParty(content, maxMember, password, reservation, restaurantId, title)
                        context!!.startActivity(Intent(context, PartyActivity::class.java))
                        Toast.makeText(context, "建立成功。", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        PartyCreateDialog(callBackListener).show(childFragmentManager, "PartyCreateDialog")
    }

    private fun launchFlow() {
        lifecycleScope.launchWhenCreated {
            val adapter = recyclerPartyRoomList.adapter
            val mapChangedCallback = object : ObservableMap.OnMapChangedCallback<ObservableArrayMap<String, ModelChanges<PartyUIState>>, String, ModelChanges<PartyUIState>>() {
                override fun onMapChanged(sender: ObservableArrayMap<String, ModelChanges<PartyUIState>>?, key: String?) {
                    val changes = sender?.get(key)
                    val target = partyList.indexOfFirst { it.party!!.id == key }
                    if (changes != null) {
                        if (target == -1) {
                            partyList.add(changes.value)
                            adapter?.notifyItemInserted(partyList.size)
                        }
                        else {
                            partyList[target] = changes.value
                            adapter?.notifyItemChanged(target)
                        }
                    }
                    if (changes == null) {
                        if (target == -1) return
                        partyList.removeAt(target)
                        adapter?.notifyItemRemoved(target)
                    }
                }
            }

            vm.partyChanges.addOnMapChangedCallback(mapChangedCallback)
            synchronized(this) {
                vm.partyChanges.forEach {
                    val changes = it.value
                    partyList.add(changes.value)
                    adapter?.notifyItemInserted(partyList.size)
                }
            }
        }
        lifecycleScope.launchWhenCreated {
            val adapter = recyclerNearbyList.adapter
            val mapChangedCallback = object : ObservableMap.OnMapChangedCallback<ObservableArrayMap<String, ModelChanges<UserUIState>>, String, ModelChanges<UserUIState>>() {
                override fun onMapChanged(sender: ObservableArrayMap<String, ModelChanges<UserUIState>>?, key: String?) {
                    val changes = sender?.get(key)
                    val target = nearbyUserList.indexOfFirst { it.user!!.id == key }
                    if (changes != null) {
                        if (target == -1) {
                            nearbyUserList.add(changes.value)
                            adapter?.notifyItemInserted(partyList.size)
                        }
                        else {
                            nearbyUserList[target] = changes.value
                            adapter?.notifyItemChanged(target)
                        }
                    }
                    if (changes == null) {
                        if (target == -1) return
                        nearbyUserList.removeAt(target)
                        adapter?.notifyItemRemoved(target)
                    }
                }
            }

            vm.nearbyUserChanges.addOnMapChangedCallback(mapChangedCallback)
            synchronized(this) {
                vm.nearbyUserChanges.forEach {
                    val changes = it.value
                    nearbyUserList.add(changes.value)
                    adapter?.notifyItemInserted(partyList.size)
                }
            }
        }
        lifecycleScope.launchWhenCreated {
            vm.currentPartyUIState.collectLatest {
                if (it == null || (it.party!!.state == "已結束" && it.party.feedbacks.contains(MyApplication.getInstance().currentUserUIStateStateFlow.value!!.user!!.id))) {
                    btnPartyState.isVisible = false
                    btnCreateParty.isVisible = true
                }
                else {
                    when (it.party.state) {
                        "準備中" -> { btnPartyState.setImageResource(R.drawable.party_prepare) }
                        "進行中" -> { btnPartyState.setImageResource(R.drawable.party_start) }
                        "已結束" -> { btnPartyState.setImageResource(R.drawable.party_end) }
                    }
                    btnPartyState.isVisible = true
                    btnCreateParty.isVisible = false
                }
            }
        }
    }
}
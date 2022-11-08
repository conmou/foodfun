package edu.foodfun.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import edu.foodfun.R
import edu.foodfun.activity.PartyInviteSendActivity
import edu.foodfun.adapter.PartyPrepareMemberRecyclerViewAdapter
import edu.foodfun.adapter.MessageRecyclerViewAdapter
import edu.foodfun.dialog.PartyLeaveDialog
import edu.foodfun.dialog.UserDetailDialog
import edu.foodfun.enums.FieldChangeType
import edu.foodfun.enums.UserTemplateType
import edu.foodfun.uistate.MessageUIState
import edu.foodfun.uistate.PartyUserUIState
import edu.foodfun.viewmodel.PartyViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PartyPrepareFragment : Fragment() {
    private lateinit var recyclerPartyRoomMemberList: RecyclerView
    private lateinit var recyclerPartyRoomMessageList: RecyclerView
    private lateinit var btnBack: FloatingActionButton
    private lateinit var btnInvite: FloatingActionButton
    private lateinit var btnSend: FloatingActionButton
    private lateinit var btnLeaveOrClose: Button
    private lateinit var btnTitle: Button
    private lateinit var btnStart: Button
    private lateinit var txtMessage: EditText
    private val vm: PartyViewModel by activityViewModels()
    private val partyUserList: MutableList<PartyUserUIState> = mutableListOf()
    private val messageList: MutableList<MessageUIState> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_party_prepare, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnBack = view.findViewById(R.id.btnPartyRoomBack)
        btnInvite = view.findViewById(R.id.btnPartyRoomInvite)
        btnSend = view.findViewById(R.id.btnPartyRoomSend)
        btnTitle = view.findViewById(R.id.btnPartyRoomTitle)
        txtMessage = view.findViewById(R.id.txtPartyRoomMessage)
        btnLeaveOrClose = view.findViewById(R.id.btnPartyRoomLeaveOrClose)
        btnStart = view.findViewById(R.id.btnPartyRoomStart)
        recyclerPartyRoomMemberList = view.findViewById(R.id.recyclerPartyRoomMemberList)
        recyclerPartyRoomMessageList = view.findViewById(R.id.recyclerPartyRoomMessageList)

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        btnInvite.setOnClickListener { startActivity(Intent(activity, PartyInviteSendActivity::class.java)) }
        btnSend.setOnClickListener {
            if(txtMessage.text.isEmpty()) return@setOnClickListener
            lifecycleScope.launch { vm.sendMessage(txtMessage.text.toString()) }
            txtMessage.text = null
        }

        btnLeaveOrClose.setOnClickListener {
            PartyLeaveDialog(requireContext(), object : PartyLeaveDialog.CallBackListener() {
                override fun onConfirm() {
                    lifecycleScope.launch { vm.leaveCurrentParty() }
                    requireActivity().onBackPressed()
                }
            }).show()
        }

        btnStart.setOnClickListener {
            lifecycleScope.launch {
                if (vm.isPartyOwner.value) vm.startParty()
                else vm.switchPrepare()
            }
        }

        recyclerPartyRoomMemberList.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerPartyRoomMemberList.adapter = PartyPrepareMemberRecyclerViewAdapter(partyUserList, object : PartyPrepareMemberRecyclerViewAdapter.ItemObserver() {
            override fun onItemClick(index: Int) {
                if (vm.currentUserUIState.value!!.user!!.friends.contains(partyUserList[index].user!!.id!!) || vm.currentUserUIState.value!!.user!!.id == partyUserList[index].user!!.id!!)
                    UserDetailDialog(partyUserList[index].user!!.id!!, UserTemplateType.FRIEND).show(childFragmentManager, "UserDetailDialog")
                else
                    UserDetailDialog(partyUserList[index].user!!.id!!, UserTemplateType.STRANGER).show(childFragmentManager, "UserDetailDialog")
            }
        })

        recyclerPartyRoomMessageList.layoutManager = LinearLayoutManager(context)
        recyclerPartyRoomMessageList.adapter = MessageRecyclerViewAdapter(messageList, vm.currentUserUIState.value!!.user!!.id!!, object : MessageRecyclerViewAdapter.ItemObserver {
            override fun onClick(index: Int) {
                if (vm.currentUserUIState.value!!.user!!.friends.contains(messageList[index].userUIState!!.user!!.id))
                    UserDetailDialog(messageList[index].userUIState!!.user!!.id!!, UserTemplateType.FRIEND).show(childFragmentManager, "UserDetailDialog")
                else
                    UserDetailDialog(messageList[index].userUIState!!.user!!.id!!, UserTemplateType.STRANGER).show(childFragmentManager, "UserDetailDialog")
            }

        })

        launchFlow()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onStop() {
        super.onStop()
        messageList.clear()
        recyclerPartyRoomMessageList.adapter?.notifyDataSetChanged()
    }

    private fun launchFlow() {
        lifecycleScope.launchWhenCreated {
            combine(vm.userChanges, vm.currentPartyUIState) { changes, partyUIState ->
                object {
                    val changes = changes
                    val isPartyOwner = partyUIState!!.party!!.owner == changes.value.user!!.id
                    val isPrepared = partyUIState!!.party!!.prepares.contains(changes.value.user!!.id)
                }
            }.collect {
                val userUIState = it.changes.value
                val partyUserUIState = PartyUserUIState(userUIState.user!!, userUIState.avatar!!, it.isPrepared, it.isPartyOwner)
                val target = partyUserList.indexOfFirst { it.user!!.id == userUIState.user.id }
                val adapter = recyclerPartyRoomMemberList.adapter
                if (it.changes.changeType == FieldChangeType.ADDED || it.changes.changeType == FieldChangeType.MODIFIED) {
                    if (target == -1) {
                        partyUserList.add(partyUserUIState)
                        adapter?.notifyItemInserted(partyUserList.size)
                    }
                    else {
                        partyUserList[target] = partyUserUIState
                        adapter?.notifyItemChanged(target)
                    }
                    val messageAdapter = recyclerPartyRoomMessageList.adapter
                    if(messageList.size == 0) return@collect
                    messageList.filter { messageUIState ->
                        messageUIState.message!!.sender == userUIState.user.id && messageUIState.message.sender != vm.currentUserUIState.value!!.user!!.id!!
                                && (messageUIState.userUIState?.avatar == null || messageUIState.userUIState?.user == null)
                    }.forEach { messageUIState ->
                        messageUIState.userUIState = userUIState
                        messageAdapter?.notifyItemChanged(messageList.indexOf(messageUIState))
                    }
                }
                else if (it.changes.changeType == FieldChangeType.REMOVED) {
                    if (target == -1) return@collect
                    partyUserList.removeAt(target)
                    adapter?.notifyItemRemoved(target)
                }
            }
        }
        lifecycleScope.launchWhenCreated {
            vm.partyTitle.collectLatest { btnTitle.text = it }
        }
        lifecycleScope.launchWhenCreated {
            vm.messageChanges.collect { change ->
                val message = change.value
                if (change.changeType == FieldChangeType.ADDED) {
                    val userUIState = partyUserList.firstOrNull { it.user!!.id == message.sender && it.user.id != vm.currentUserUIState.value!!.user!!.id!! }
                    messageList.add(MessageUIState(message, userUIState))
                }
                recyclerPartyRoomMessageList.apply {
                    adapter?.notifyItemInserted(messageList.size)
                    scrollToPosition(messageList.size - 1)
                }
            }
        }
        lifecycleScope.launchWhenCreated {
            combine(vm.isPartyOwner, vm.isPrepared, vm.isAllPrepared) { isOwner, isPrepared, isAllPrepared ->
                object {
                    val isOwner = isOwner
                    val isPrepared = isPrepared
                    val isAllPrepared = isAllPrepared
                }
            }.collect {
                if (it.isOwner) {
                    btnStart.text = "開始派對"
                    btnStart.isEnabled = it.isAllPrepared
                }
                else {
                    if (it.isPrepared) btnStart.text = "取消準備"
                    else btnStart.text = "準備"
                }
            }
        }
    }
}
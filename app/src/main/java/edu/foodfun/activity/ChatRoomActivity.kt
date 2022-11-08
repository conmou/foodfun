package edu.foodfun.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import edu.foodfun.R
import edu.foodfun.adapter.MessageRecyclerViewAdapter
import edu.foodfun.dialog.UserDetailDialog
import edu.foodfun.enums.UserTemplateType
import edu.foodfun.uistate.MessageUIState
import edu.foodfun.viewmodel.ChatRoomViewModel
import kotlinx.coroutines.flow.filterNotNull

@AndroidEntryPoint
class ChatRoomActivity : AppCompatActivity() {
    private lateinit var txtMessage: EditText
    private lateinit var btnSend: FloatingActionButton
    private lateinit var recyclerMessageList: RecyclerView
    private val vm: ChatRoomViewModel by viewModels()
    private val messageList: MutableList<MessageUIState> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        btnSend = findViewById(R.id.btnChatRoomSend)
        txtMessage = findViewById(R.id.txtChatRoomMessage)
        recyclerMessageList = findViewById(R.id.recyclerMessageList)
        setSupportActionBar(findViewById(R.id.toolbarChatRoom))

        intent.getStringExtra("friendId").apply { vm.setChatRoomUser(this!!) }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        txtMessage.setOnClickListener { recyclerMessageList.scrollToPosition(messageList.size - 1) }

        btnSend.setOnClickListener {
            if(txtMessage.text.isEmpty()) return@setOnClickListener
            vm.sendMessage("${txtMessage.text}")
            txtMessage.text = null
        }

        recyclerMessageList.layoutManager = LinearLayoutManager(this)
        recyclerMessageList.adapter = MessageRecyclerViewAdapter(messageList, vm.currentUserUIState.value!!.user!!.id!!, object : MessageRecyclerViewAdapter.ItemObserver {
            override fun onClick(index: Int) {
                UserDetailDialog(messageList[index].userUIState!!.user!!.id!!, UserTemplateType.FRIEND).show(supportFragmentManager, "UserDetailDialog")
            }

        })

        launchFlow()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun launchFlow() {
        lifecycleScope.launchWhenCreated {
            vm.messageChanges.collect {
                messageList.add(it.value)
                messageList.sortBy { message -> message.message!!.time }
                recyclerMessageList.adapter?.notifyDataSetChanged()
                //recyclerMessageList.scrollToPosition(messageList.size - 1) //DO 當在最下方時才觸發
            }
        }
        lifecycleScope.launchWhenCreated {
            vm.friendUserUIState.filterNotNull().collect {
                supportActionBar?.title = it.user!!.nickName!!
            }
        }
    }
}
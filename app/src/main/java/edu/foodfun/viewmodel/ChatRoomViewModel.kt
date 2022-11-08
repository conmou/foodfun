package edu.foodfun.viewmodel

import androidx.databinding.ObservableArrayMap
import androidx.databinding.ObservableMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.foodfun.ModelChanges
import edu.foodfun.hilt.MyApplication
import edu.foodfun.model.Message
import edu.foodfun.repository.MessageRepository
import edu.foodfun.uistate.MessageUIState
import edu.foodfun.uistate.UserUIState
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
): ViewModel() {
    private val app = MyApplication.getInstance()
    private var mapChangedCallback: ObservableMap.OnMapChangedCallback<ObservableArrayMap<String, ModelChanges<UserUIState>>, String, ModelChanges<UserUIState>>? = null
    val currentUserUIState = app.currentUserUIStateStateFlow
    val friendUserUIState: MutableStateFlow<UserUIState?> = MutableStateFlow(null)
    var listeningMessageJob: Job? = null
    var messageChanges: MutableSharedFlow<ModelChanges<MessageUIState>> = MutableSharedFlow(replay = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.SUSPEND)

    fun setChatRoomUser(userId: String) {
        if (!currentUserUIState.value!!.user!!.friends.contains(userId)) return
        app.friendChangesMap.removeOnMapChangedCallback(mapChangedCallback)
        mapChangedCallback = object : ObservableMap.OnMapChangedCallback<ObservableArrayMap<String, ModelChanges<UserUIState>>, String, ModelChanges<UserUIState>>() {
            override fun onMapChanged(sender: ObservableArrayMap<String, ModelChanges<UserUIState>>?, key: String?) {
                if (key != userId) return
                val changes = sender?.get(key)
                friendUserUIState.value = changes!!.value
            }
        }
        app.friendChangesMap.addOnMapChangedCallback(mapChangedCallback)
        friendUserUIState.value = app.friendChangesMap[userId]!!.value
        listeningMessage()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun listeningMessage() {
        messageChanges.resetReplayCache()
        listeningMessageJob?.cancel()
        listeningMessageJob = viewModelScope.launch {
            merge(
                messageRepository.fetcgListeningMessagesFlow(currentUserUIState.value!!.user!!.id!!, friendUserUIState.value!!.user!!.id!!),
                messageRepository.fetcgListeningMessagesFlow(friendUserUIState.value!!.user!!.id!!, currentUserUIState.value!!.user!!.id!!)
            )
            .collect {
                if(it.value.sender == currentUserUIState.value!!.user!!.id!!)
                    messageChanges.emit(ModelChanges(it.changeType, MessageUIState(it.value)))
                else
                    messageChanges.emit(ModelChanges(it.changeType, MessageUIState(it.value, friendUserUIState.value)))
            }
        }
    }

    fun sendMessage(content: String) {
        val message = Message(content, friendUserUIState.value!!.user!!.id!!, currentUserUIState.value!!.user!!.id!!, "已送出", Timestamp.now())
        messageRepository.sendMessage(message)
    }
}
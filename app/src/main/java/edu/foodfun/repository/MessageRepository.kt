package edu.foodfun.repository

import android.content.Context
import com.google.firebase.firestore.Query
import edu.foodfun.ModelChanges
import edu.foodfun.deserializer.MessageDocumentDeserializer
import edu.foodfun.model.Message
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessageRepository @Inject constructor(val deserializer: MessageDocumentDeserializer, context: Context): BaseRepository(context) {
    companion object {
        internal const val collectionName = "messages"
        internal const val content = "content"
        internal const val sender = "sender"
        internal const val receiver = "receiver"
        internal const val state = "state"
        internal const val time = "time"
    }

    fun sendMessage(message: Message) {
        val chatroomDocRef = firestore.collection(collectionName).document()
        val setTask = chatroomDocRef.set(message)
        setTask.addOnSuccessListener { return@addOnSuccessListener }
    }

    suspend fun fetcgListeningMessagesFlow(senderId: String, receiverId: String): Flow<ModelChanges<Message>> {
        return fetchListeningQueryFlow(firestore.collection(collectionName)
            .whereEqualTo(sender, senderId)
            .whereEqualTo(receiver, receiverId)
            .orderBy(time, Query.Direction.ASCENDING)
            , deserializer)
    }

}
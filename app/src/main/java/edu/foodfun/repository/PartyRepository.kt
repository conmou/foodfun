package edu.foodfun.repository

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import edu.foodfun.ModelChanges
import edu.foodfun.deserializer.MessageDeserializer
import edu.foodfun.deserializer.PartyDocumentDeserializer
import edu.foodfun.model.Message
import edu.foodfun.model.Party
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PartyRepository @Inject constructor(private val partyDeserializer: PartyDocumentDeserializer, private val messageDeserializer: MessageDeserializer, context: Context) : BaseRepository(context) {
    companion object {
        internal const val partyCollectionName = "parties"
        internal const val messageCollectionName = "messages"
        internal const val inviteCollectionName = "invites"
        internal const val content = "content"
        internal const val maxMember = "maxMember"
        internal const val owner = "owner"
        internal const val password = "password"
        internal const val reservation = "reservation"
        internal const val restaurant = "restaurant"
        internal const val setting = "setting"
        internal const val state = "state"
        internal const val title = "title"
        internal const val users = "users"
        internal const val chatRecord = "chatRecord"
        internal const val time = "time"
        internal const val sender = "sender"
        internal const val feedbacks = "feedbacks"
        internal const val prepares = "prepares"
    }

    fun generateParty(
        owner: String,
        content: String,
        maxMember: Int,
        password: String? = null,
        reservation: Timestamp,
        restaurantId: String,
        title: String
    ): Party {
        return Party(
            owner,
            password,
            reservation,
            content,
            restaurantId,
            state = "準備中",
            title = title,
            maxMember = maxMember,
            users = listOf(owner)
        )
    }

    private fun partyDocRef(partyId: String): DocumentReference = firestore.document("${partyCollectionName}/$partyId")

    suspend fun fetchParty(partyId: String): Party = fetchDocumentSnapShot(partyDocRef(partyId), partyDeserializer)

    suspend fun createParty(party: Party): String = suspendCancellableCoroutine { cont ->
        val partyDocRef = firestore.collection(partyCollectionName).document()
        val setTask = partyDocRef.set(party)
        setTask.addOnSuccessListener { cont.resume(partyDocRef.id) }
        setTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun fetchListeningPartiesFlow(): Flow<ModelChanges<Party>> {
        return fetchListeningQueryFlow(firestore.collection(partyCollectionName), partyDeserializer)
    }

    suspend fun fetchListeningPartyFlow(partyId: String): Flow<Party?> {
        return fetchListeningDocumentFlow(firestore.document("${partyCollectionName}/${partyId}"), partyDeserializer)
    }

    suspend fun fetchListeningPartyMessagesFlow(partyId: String): Flow<ModelChanges<Message>> {
        return fetchListeningQueryFlow(firestore.collection(partyCollectionName).document(partyId).collection(messageCollectionName).orderBy(time,Query.Direction.ASCENDING), messageDeserializer)
    }

    fun addPrepare(userId: String, partyId: String) {
        val partyDocRef = partyDocRef(partyId)
        val updateTask = partyDocRef.update(prepares, FieldValue.arrayUnion(userId))
        updateTask.addOnSuccessListener { return@addOnSuccessListener }
    }

    fun removePrepare(userId: String, partyId: String) {
        val partyDocRef = partyDocRef(partyId)
        val updateTask = partyDocRef.update(prepares, FieldValue.arrayRemove(userId))
        updateTask.addOnSuccessListener { return@addOnSuccessListener }
    }

    fun addUser(userId: String, partyId: String) {
        val partyDocRef = partyDocRef(partyId)
        val updateTask = partyDocRef.update(users, FieldValue.arrayUnion(userId))
        updateTask.addOnSuccessListener { return@addOnSuccessListener }
    }

    fun removeUser(userId: String, partyId: String) {
        val partyDocRef = partyDocRef(partyId)
        val updateTask = partyDocRef.update(users, FieldValue.arrayRemove(userId))
        updateTask.addOnSuccessListener { return@addOnSuccessListener }
    }

    suspend fun sendMessage(partyId: String, userId: String, sendTime: Timestamp, content: String) = withContext(Dispatchers.IO) {
        val messageDoc: HashMap<String, Any> = hashMapOf(
            sender to userId,
            time to sendTime,
            PartyRepository.content to content,
            state to "已送出"
        )
        return@withContext partyDocRef(partyId).collection(messageCollectionName).document().set(messageDoc)
    }

    suspend fun updateState(partyId: String, state: String) = withContext(Dispatchers.IO) {
        val partyDocRef = partyDocRef(partyId)
        return@withContext partyDocRef.update(PartyRepository.state, state)
    }

    fun updateFeedbackDoneList(userId: String, partyId: String) {
        val partyDocRef = partyDocRef(partyId)
        val updateTask = partyDocRef.update(feedbacks, FieldValue.arrayUnion(userId), users, FieldValue.arrayRemove(userId))
        updateTask.addOnSuccessListener { return@addOnSuccessListener }
    }
}
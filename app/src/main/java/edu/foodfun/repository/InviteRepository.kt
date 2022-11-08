package edu.foodfun.repository

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import edu.foodfun.ModelChanges
import edu.foodfun.deserializer.PartyInviteDocumentDeserializer
import edu.foodfun.model.Invite
import edu.foodfun.model.Party
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class InviteRepository @Inject constructor(private val deserializer: PartyInviteDocumentDeserializer, context: Context): BaseRepository(context) {
    companion object {
        internal const val collectionName = "invites"
        internal const val receiver = "receiver"
        internal const val state = "state"
        internal const val party = "party"
    }

    private fun inviteDocRef(inviteId: String): DocumentReference = firestore.document("${collectionName}/$inviteId")

    fun generateInvite(partyId: String, receiver: String, sender: String): Invite {
        return Invite(partyId, receiver, sender, state = "等待中", time = Timestamp.now())
    }

    suspend fun createInvite(invite: Invite): Unit = suspendCoroutine { cont ->
        val inviteDocRef = firestore.collection(collectionName).document()
        val setTask = inviteDocRef.set(invite)
        setTask.addOnSuccessListener { cont.resume(Unit) }
        setTask.addOnFailureListener { cont.resumeWithException(it) }
    }

//    suspend fun fetchListeningPartyInviteFlow(userId: String): Flow<ModelChanges<Invite>> {
//        return fetchListeningQueryFlow(firestore.collection(PartyRepository.inviteCollectionName).whereEqualTo(
//            receiver, userId), partyInviteDeserializer)
//    }

    suspend fun fetchListeningPartyInviteFlow(userId: String): Flow<ModelChanges<Invite>> {
        return fetchListeningQueryFlow(firestore.collection(collectionName).whereEqualTo(receiver, userId), deserializer)
    }

    suspend fun acceptPartyInvite(inviteId: String): Unit = suspendCoroutine { cont ->
        val inviteDocRef = inviteDocRef(inviteId)
        val updateTask = inviteDocRef.update(state, "已接受")
        updateTask.addOnSuccessListener { cont.resume(Unit) }
        updateTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun rejectPartyInvite(inviteId: String): Unit = suspendCoroutine { cont ->
        val inviteDocRef = inviteDocRef(inviteId)
        val updateTask = inviteDocRef.update(state, "已拒絕")
        updateTask.addOnSuccessListener { cont.resume(Unit) }
        updateTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun changeInviteStatus(partyId: String, state: String) {
        val query = firestore.collection(collectionName).whereEqualTo(party, partyId)
        val result = fetchQuerySnapShot(query, deserializer)
        result.forEach { invite ->
            suspendCoroutine { cont ->
                val inviteDocRef = inviteDocRef(invite.id!!)
                val updateTask = inviteDocRef.update(InviteRepository.state, state)
                updateTask.addOnSuccessListener { cont.resume(Unit) }
                updateTask.addOnFailureListener { cont.resumeWithException(it) }
            }
        }
    }
}


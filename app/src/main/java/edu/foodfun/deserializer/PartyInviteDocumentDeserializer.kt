package edu.foodfun.deserializer

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import edu.foodfun.model.Invite
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PartyInviteDocumentDeserializer @Inject constructor() : IFirestoreDocumentDeserializer<Invite> {
    override fun deserialize(input: DocumentSnapshot): Invite {
        return input.toObject()!!
    }
}
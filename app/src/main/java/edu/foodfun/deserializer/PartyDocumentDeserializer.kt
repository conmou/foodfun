package edu.foodfun.deserializer

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import edu.foodfun.model.Party
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PartyDocumentDeserializer @Inject constructor() : IFirestoreDocumentDeserializer<Party> {
    override fun deserialize(input: DocumentSnapshot): Party {
        return input.toObject()!!
    }
}
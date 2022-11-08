package edu.foodfun.deserializer

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import edu.foodfun.model.Message
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageDeserializer @Inject constructor() : IFirestoreDocumentDeserializer<Message> {
    override fun deserialize(input: DocumentSnapshot): Message {
        return input.toObject()!!
    }
}
package edu.foodfun.deserializer

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import edu.foodfun.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDocumentDeserializer @Inject constructor() : IFirestoreDocumentDeserializer<User> {
    override fun deserialize(input: DocumentSnapshot): User {
        return input.toObject()!!
    }
}
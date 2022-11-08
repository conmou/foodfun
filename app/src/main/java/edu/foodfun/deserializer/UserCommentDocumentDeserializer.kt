package edu.foodfun.deserializer

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import edu.foodfun.model.UserComment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserCommentDocumentDeserializer @Inject constructor(): IFirestoreDocumentDeserializer<UserComment> {
    override fun deserialize(input: DocumentSnapshot): UserComment {
        return input.toObject()!!
    }
}
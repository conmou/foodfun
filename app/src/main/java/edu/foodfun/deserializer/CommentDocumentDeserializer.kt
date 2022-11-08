package edu.foodfun.deserializer

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import edu.foodfun.model.Comment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentDocumentDeserializer @Inject constructor() : IFirestoreDocumentDeserializer<Comment> {
    override fun deserialize(input: DocumentSnapshot): Comment {
        return input.toObject()!!
    }
}
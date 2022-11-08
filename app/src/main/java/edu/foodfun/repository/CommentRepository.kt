package edu.foodfun.repository

import android.content.Context
import edu.foodfun.deserializer.CommentDocumentDeserializer
import edu.foodfun.model.Comment
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CommentRepository @Inject constructor(val deserializer: CommentDocumentDeserializer, context: Context) : BaseRepository(context) {
    companion object {
        internal const val restaurantId = "restaurantId"
        internal const val content = "content"
    }

    suspend fun fetchComments(restaurantId: String): MutableList<Comment> = suspendCoroutine { cont ->
        val query = firestore.collection("comments").whereEqualTo(CommentRepository.restaurantId, restaurantId).whereNotEqualTo(content, "")
        val queryTask = query.get()
        queryTask.addOnSuccessListener { querySnap ->
            val list = mutableListOf<Comment>()
            querySnap.documents.forEach {
                val model = deserializer.deserialize(it)
                list.add(model)
            }
            cont.resume(list)
        }
        queryTask.addOnFailureListener {
            cont.resumeWithException(it)
        }
    }
}
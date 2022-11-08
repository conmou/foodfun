package edu.foodfun.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

class Message(
    val content: String? = null,
    val receiver: String? = null,
    val sender: String? = null,
    val state: String? = "",
    val time: Timestamp? = null
    ) : BaseModel()
package edu.foodfun.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

class Invite(
    val party: String? = null,
    val receiver: String? = null,
    val sender: String? = null,
    val state: String? = null,
    val time: Timestamp? = null
) : BaseModel()
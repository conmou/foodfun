package edu.foodfun.model

import com.google.firebase.Timestamp

class UserComment (
    val content: String? = null,
    val time: Timestamp? = null
): BaseModel()
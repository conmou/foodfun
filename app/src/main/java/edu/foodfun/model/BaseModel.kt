package edu.foodfun.model

import com.google.firebase.firestore.DocumentId

abstract class BaseModel {
    @DocumentId
    val id: String? = null
}
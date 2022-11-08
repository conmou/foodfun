package edu.foodfun.model

import com.google.firebase.Timestamp
import kotlin.collections.HashMap

class Party(
    val owner: String? = null,
    val password: String? = null,
    val reservation: Timestamp? = null,
    val content: String? = null,
    val restaurant: String? = null,
    val setting: HashMap<String, Any> = hashMapOf(),
    var state: String? = null,
    val title: String? = null,
    val maxMember: Int? = null,
    val users: List<String> = emptyList(),
    val prepares: List<String> = emptyList(),
    val feedbacks: List<String> = emptyList()) : BaseModel()
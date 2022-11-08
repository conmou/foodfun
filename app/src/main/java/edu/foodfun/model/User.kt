package edu.foodfun.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import kotlin.collections.HashMap

class User(
    val nickName: String? = null,
    val bio: String? = null,
    val birthday: Timestamp? = null,
    val sex: String? = null,
    val state: String? = null,
    val location: GeoPoint? = null,
    val locationHash: String? = null,
    val avatarVersion: Long? = null,
    val currentParty: String? = null,
    val friends: List<String> = emptyList(),
    val achivements: HashMap<String, Int> = hashMapOf(),
    val groups: HashMap<String, List<String>> = hashMapOf(),
    val recommendParams: HashMap<String, Any> = hashMapOf(),
    val invites: List<String> = emptyList()) : BaseModel()
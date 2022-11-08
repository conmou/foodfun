package edu.foodfun.model

import com.google.firebase.firestore.GeoPoint

class Restaurant(
    val address: String? = null,
    val avgCost: Double? = null,
    val location: GeoPoint? = null,
    val locationHash: String? = null,
    val name: String? = null,
    val openingTime: HashMap<String, String> = hashMapOf(),
    val rating: Double? = null,
    val tel: String? = null,
    val type: List<String> = emptyList()) : BaseModel()
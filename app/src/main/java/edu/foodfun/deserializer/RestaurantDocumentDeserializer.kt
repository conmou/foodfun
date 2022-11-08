package edu.foodfun.deserializer

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import edu.foodfun.model.Restaurant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestaurantDocumentDeserializer @Inject constructor() : IFirestoreDocumentDeserializer<Restaurant> {
    override fun deserialize(input: DocumentSnapshot): Restaurant {
        return input.toObject()!!
    }
}
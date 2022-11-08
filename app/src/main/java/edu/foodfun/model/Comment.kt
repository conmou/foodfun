package edu.foodfun.model

class Comment(
    val nickName: String? = null,
    val content: String? = null,
    val postTime: String? = null,
    val restaurantId: String? = null,
    val star: Int? = null
) : BaseModel()
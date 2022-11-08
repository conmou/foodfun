package edu.foodfun.uistate

import android.graphics.Bitmap
import edu.foodfun.model.User

open class UserUIState(
    val user: User? = null,
    val avatar: Bitmap? = null,
)
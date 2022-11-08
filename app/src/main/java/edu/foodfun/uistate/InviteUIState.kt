package edu.foodfun.uistate

import android.graphics.Bitmap
import edu.foodfun.model.Invite
import edu.foodfun.model.Party
import edu.foodfun.model.User

class InviteUIState(
    val invite: Invite? = null,
    val sender: User? = null,
    val party: Party? = null,
    val restaurantImage: Bitmap? = null
)
package edu.foodfun.uistate

import android.graphics.Bitmap
import edu.foodfun.model.User

class PartyUserUIState(user: User, avatar: Bitmap, val isPrepared: Boolean, val isOwner: Boolean) : UserUIState(user, avatar)
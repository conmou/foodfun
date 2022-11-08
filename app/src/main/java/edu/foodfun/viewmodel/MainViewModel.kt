package edu.foodfun.viewmodel

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.*
import com.firebase.ui.auth.AuthUI
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.foodfun.hilt.MyApplication
import edu.foodfun.model.Invite
import edu.foodfun.model.Restaurant
import edu.foodfun.repository.*
import edu.foodfun.uistate.*
import edu.foodfun.usecase.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository : UserRepository,
    private val restaurantRepository: RestaurantRepository,
    private val commentRepository: CommentRepository,
    private val partyRepository: PartyRepository,
    private val inviteRepository: InviteRepository
) : ViewModel() {
    @Inject lateinit var addUserToPartyUseCase: AddUserToPartyUseCase
    @Inject lateinit var removeUserFromPartyUseCase: RemoveUserFromPartyUseCase
    private val app = MyApplication.getInstance()
    val currentUserUIState = app.currentUserUIStateStateFlow
    val currentPartyUIState = app.currentPartyUIStateStateFlow
    val friendChanges = app.friendChangesMap
    val inviteChanges = app.inviteChangesMap
    val partyChanges = app.partyChangesMap
    val nearbyUserChanges = app.nearByUserChangesMap
    val partyInviteChanges = app.partyInviteChangesMap

    var fetchRecommendRestaurants: Flow<RecommendUIState> = flow {
        while (app.isCurrentUserDataInitingStateFlow.value) { delay(200) }
        restaurantRepository.fetchRecommendRestaurants(currentUserUIState.value?.user).collect {
            val image = restaurantRepository.tryFetchImage(it.id!!)
            val comments = commentRepository.fetchComments(it.id)
            val restaurantUIState = RestaurantUIState(it, image)
            val commentUIState = CommentUIState(comments)
            emit(RecommendUIState(restaurantUIState, commentUIState))
        }
    }

    fun logout() = FirebaseAuth.getInstance().signOut()

    fun login(activityResultLauncher: ActivityResultLauncher<Intent>) {
        val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build())
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .build()
        activityResultLauncher.launch(signInIntent)
    }

    suspend fun addRecommendParams(price: Double?, restaurantLocation: GeoPoint, type: List<String>) = withContext(Dispatchers.IO) {
        userRepository.addRecommendParams(currentUserUIState.value!!.user!!, price, restaurantLocation, type)
    }

    suspend fun addRestaurantToDefaultGroup(restaurantId: String) = withContext(Dispatchers.IO) {
        userRepository.addRestaurant(currentUserUIState.value!!.user!!.id!!, restaurantId = restaurantId)
    }

    suspend fun createParty(content: String, maxMember: Int, password: String? = null, reservation: Timestamp, restaurantId: String, title: String) = withContext(Dispatchers.IO) {
        val party = partyRepository.generateParty(currentUserUIState.value!!.user!!.id!!, content, maxMember, password, reservation, restaurantId, title)
        val partyId = partyRepository.createParty(party)
        userRepository.updateCurrentParty(currentUserUIState.value!!.user!!.id!!, partyId)
        return@withContext partyId
    }

    suspend fun joinParty(partyId: String) = withContext(Dispatchers.IO) {
        return@withContext addUserToPartyUseCase.invoke(currentUserUIState.value!!.user!!.id!!, partyId)
    }

    suspend fun leaveCurrentParty() {
        val user = currentUserUIState.value!!.user!!
        val party = currentPartyUIState.value?.party
        if (user.currentParty == null && party == null) return
        removeUserFromPartyUseCase.invoke(user.id!!, party!!.id!!)
    }

    suspend fun removeFriend(userId: String) = withContext(Dispatchers.IO) {
        userRepository.removeFriend(currentUserUIState.value!!.user!!.id!!, userId)
    }

    suspend fun fetchSearchRestaurant(searchText: String): List<Restaurant> {
        return restaurantRepository.fetchSearchRestaurant(searchText)
    }

    suspend fun tryFetchImage(restaurantId: String): Bitmap? {
        return restaurantRepository.tryFetchImage(restaurantId)
    }

    suspend fun fetchUserUIState(userId: String) = withContext(Dispatchers.IO) {
        val user = userRepository.fetchUser(userId)
        val avatar = userRepository.tryFetchAvatar(user.id!!, user.avatarVersion)
        return@withContext UserUIState(user, avatar)
    }

    suspend fun sendInvite(userId: String) = withContext(Dispatchers.IO) {
        userRepository.updateInviteList(currentUserUIState.value!!.user!!.id!!, userId)
    }

    suspend fun acceptInvite(userId: String) = withContext(Dispatchers.IO) {
        if (currentUserUIState.value!!.user!!.invites.indexOfFirst { it == userId } == -1) return@withContext
        userRepository.addFriend(currentUserUIState.value!!.user!!.id!!, userId)
        userRepository.addFriend(userId, currentUserUIState.value!!.user!!.id!!)
        userRepository.removeInvite(currentUserUIState.value!!.user!!.id!!, userId)
        userRepository.removeInvite(userId, currentUserUIState.value!!.user!!.id!!)
    }

    suspend fun refuseInvite(userId: String) = withContext(Dispatchers.IO) {
        if (currentUserUIState.value!!.user!!.invites.indexOfFirst { it == userId } == -1) return@withContext
        userRepository.removeInvite(currentUserUIState.value!!.user!!.id!!, userId)
    }

    suspend fun sendPartyInvite(partyId: String, userId: String) = withContext(Dispatchers.IO) {
        val invite = inviteRepository.generateInvite(partyId, receiver = userId, sender = currentUserUIState.value!!.user!!.id!!)
        inviteRepository.createInvite(invite)
    }

    suspend fun acceptPartyInvite(invite: Invite) = withContext(Dispatchers.IO) {
        inviteRepository.acceptPartyInvite(invite.id!!)
        partyRepository.addUser(invite.receiver!!, invite.party!!)
    }

    suspend fun rejectPartyInvite(inviteId: String) = withContext(Dispatchers.IO) {
        inviteRepository.rejectPartyInvite(inviteId)
    }
}
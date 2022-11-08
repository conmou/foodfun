package edu.foodfun.viewmodel

import android.graphics.*
import androidx.lifecycle.*
import com.google.android.gms.maps.model.*
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.foodfun.ModelChanges
import edu.foodfun.comparator.PartyComparator
import edu.foodfun.enums.AchievementType
import edu.foodfun.enums.FieldChangeType
import edu.foodfun.hilt.MyApplication
import edu.foodfun.model.Message
import edu.foodfun.model.Party
import edu.foodfun.model.User
import edu.foodfun.repository.InviteRepository
import edu.foodfun.repository.PartyRepository
import edu.foodfun.repository.RestaurantRepository
import edu.foodfun.repository.UserRepository
import edu.foodfun.uistate.PartyUIState
import edu.foodfun.uistate.UserUIState
import edu.foodfun.usecase.RemoveUserFromPartyUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PartyViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val restaurantRepository: RestaurantRepository,
    private val partyRepository: PartyRepository,
    private val inviteRepository: InviteRepository
) : ViewModel() {
    @Inject lateinit var removeUserFromPartyUseCase: RemoveUserFromPartyUseCase
    private var listeningCurrentPartyJob: Job? = null
    private var listeningPartyMessagesJob: Job? = null
    private var tempPartyId: String? = null
    val app = MyApplication.getInstance()
    val currentUserUIState = app.currentUserUIStateStateFlow.asStateFlow()
    val currentPartyUIState = app.currentPartyUIStateStateFlow.asStateFlow()
    val isPartyOwner = app.currentPartyUIStateStateFlow.combine(app.currentUserUIStateStateFlow) { a1, a2 -> a1?.party?.owner == a2?.user?.id!! }.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val isAllPrepared = app.currentPartyUIStateStateFlow.filterNotNull().map { it.party!!.prepares.toMutableList().apply { add(it.party.owner!!) }.containsAll(it.party.users) }.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val isPrepared = app.currentPartyUIStateStateFlow.filterNotNull().map { it.party!!.prepares.contains(currentUserUIState.value!!.user!!.id!!) }.stateIn(viewModelScope, SharingStarted.Lazily, false)
    val currentFocusUser: MutableStateFlow<UserUIState?> = MutableStateFlow(null)
    val partyTitle: MutableStateFlow<String?> = MutableStateFlow(null)
    val userChanges: MutableSharedFlow<ModelChanges<UserUIState>> = MutableSharedFlow(replay = 2000, onBufferOverflow = BufferOverflow.SUSPEND)
    val messageChanges: MutableSharedFlow<ModelChanges<Message>> = MutableSharedFlow(replay = 2000, onBufferOverflow = BufferOverflow.SUSPEND)

    init {
        viewModelScope.launch {
            val partyComparator = PartyComparator(object : PartyComparator.PartyChangeListner() {
                val memberJobs = mutableMapOf<String, Job>()
                override fun onUserChanged(type: FieldChangeType, userId: String) {
                    if (type == FieldChangeType.ADDED) {
                        memberJobs[userId]?.cancel()
                        memberJobs[userId] = viewModelScope.launch {
                            userRepository.fetchListeningUserFlow(userId).collectLatest {
                                if (it == null) throw Exception("target document: $userId is not exist in database.")
                                val avatar = userRepository.tryFetchAvatar(userId, it.avatarVersion)
                                userChanges.emit(ModelChanges(type, UserUIState(it, avatar)))
                            }
                        }
                    }
                    else if (type == FieldChangeType.REMOVED) {
                        memberJobs[userId]?.cancel()
                        memberJobs.remove(userId)
                        viewModelScope.launch {
                            val cache = userChanges.replayCache.findLast { it.value.user!!.id == userId }
                            userChanges.emit(ModelChanges(type, cache!!.value))
                        }
                    }
                }

                override fun onTitleChanged(type: FieldChangeType, title: String) {
                    partyTitle.value = title
                }
            })
            var tempParty: Party? = null

            currentPartyUIState.filterNotNull().collectLatest {
                val party = it.party!!
                if (party.id!! != tempPartyId) {
                    userChanges.resetReplayCache()
                    messageChanges.resetReplayCache()
                    currentFocusUser.value = currentUserUIState.value
                    listeningPartyMessagesJob?.cancel()
                    listeningPartyMessagesJob = viewModelScope.launch {
                        partyRepository.fetchListeningPartyMessagesFlow(party.id).collect { changes -> messageChanges.emit(changes) }
                    }
                }
                tempPartyId = party.id
                partyComparator.compare(tempParty, party)
                tempParty = party
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun listenParty() {
        userChanges.resetReplayCache()
        messageChanges.resetReplayCache()
        currentFocusUser.value = currentUserUIState.value
        partyTitle.value = null
        val partyComparator = PartyComparator(object : PartyComparator.PartyChangeListner() {
            val memberJobs = mutableMapOf<String, Job>()
            override fun onUserChanged(type: FieldChangeType, userId: String) {
                if (type == FieldChangeType.ADDED) {
                    memberJobs[userId]?.cancel()
                    memberJobs[userId] = viewModelScope.launch {
                        userRepository.fetchListeningUserFlow(userId).collectLatest {
                            if (it == null) throw Exception("target document: $userId is not exist in database.")
                            val avatar = userRepository.tryFetchAvatar(userId, it.avatarVersion)
                            userChanges.emit(ModelChanges(type, UserUIState(it, avatar)))
                        }
                    }
                }
                else if (type == FieldChangeType.REMOVED) {
                    memberJobs[userId]?.cancel()
                    memberJobs.remove(userId)
                    viewModelScope.launch {
                        val cache = userChanges.replayCache.findLast { it.value.user!!.id == userId }
                        userChanges.emit(ModelChanges(type, cache!!.value))
                    }
                }
            }

            override fun onTitleChanged(type: FieldChangeType, title: String) {
                partyTitle.value = title
            }
        })
        listeningCurrentPartyJob?.cancel()
        listeningCurrentPartyJob = viewModelScope.launch {
            var tempParty : Party? = null
            partyRepository.fetchListeningPartyFlow(currentPartyUIState.value!!.party!!.id!!).collect {
                partyComparator.compare(tempParty, it!!)
                tempParty = it
            }
        }
        listeningPartyMessagesJob?.cancel()
        listeningPartyMessagesJob = viewModelScope.launch {
            partyRepository.fetchListeningPartyMessagesFlow(app.currentPartyUIStateStateFlow.value!!.party!!.id!!).collect {
                messageChanges.emit(it)
            }
        }
    }

    suspend fun leaveCurrentParty() {
        listeningCurrentPartyJob?.cancelAndJoin()
        listeningPartyMessagesJob?.cancelAndJoin()
        removeUserFromPartyUseCase.invoke(currentUserUIState.value!!.user!!.id!!, app.currentPartyUIStateStateFlow.value!!.party!!.id!!)
    }

    suspend fun sendMessage(content: String) {
        partyRepository.sendMessage(app.currentPartyUIStateStateFlow.value!!.party!!.id!!, currentUserUIState.value!!.user!!.id!!, Timestamp.now(), content)
    }

    suspend fun endParty() {
        if (currentPartyUIState.value!!.party!!.owner != currentUserUIState.value!!.user!!.id!!) return
        listeningCurrentPartyJob?.cancelAndJoin()
        listeningPartyMessagesJob?.cancelAndJoin()
        partyRepository.updateState(app.currentPartyUIStateStateFlow.value!!.party!!.id!!, "已結束")
    }

    suspend fun startParty() {
        if (!isAllPrepared.value) return
        if (currentPartyUIState.value!!.party!!.owner != currentUserUIState.value!!.user!!.id!!) return
        partyRepository.updateState(app.currentPartyUIStateStateFlow.value!!.party!!.id!!, "進行中")
        inviteRepository.changeInviteStatus(app.currentPartyUIStateStateFlow.value!!.party!!.id!!, "已失效")
    }

    suspend fun switchPrepare() {
        val user = currentUserUIState.value!!.user!!
        val party = currentPartyUIState.value?.party ?: return
        if (isPartyOwner.value) return
        if (party.prepares.contains(user.id!!))
            partyRepository.removePrepare(user.id, party.id!!)
        else
            partyRepository.addPrepare(user.id, party.id!!)
    }

    suspend fun fetchUserModel(userId: String): User {
        return userRepository.fetchUser(userId)
    }

    suspend fun tryFetchAvatar(userId: String, avatarVersion: Long): Bitmap? {
        return userRepository.tryFetchAvatar(userId, avatarVersion)
    }

    fun getAllUserBounds(userUIStateList: MutableList<UserUIState>): LatLngBounds {
        val builder = LatLngBounds.builder()
        userUIStateList.forEach {
            builder.include(LatLng(it.user!!.location!!.latitude, it.user.location!!.longitude))
        }
        return builder.build()
    }

    fun getRestaurantBounds(): LatLngBounds {
        val builder = LatLngBounds.builder()
        builder.include(LatLng(currentPartyUIState.value!!.restaurant!!.location!!.latitude, currentPartyUIState.value!!.restaurant!!.location!!.longitude))
        return builder.build()
    }

    fun generateMarkerIcon(userUIState: UserUIState): BitmapDescriptor {
        val bitmap = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            style = Paint.Style.FILL_AND_STROKE
            color = Color.WHITE
            isAntiAlias = true
        }
        val strokeInPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeWidth = 10f
            isAntiAlias = true
        }
        val strokeOutPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.argb(255, 255, 200, 200)
            strokeWidth = 3f
            isAntiAlias = true
        }

        canvas.drawCircle(80f, 70f, 70f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val resizeAvatar = userUIState.avatar?.let { Bitmap.createScaledBitmap(it,140, 140, false) }
        if (resizeAvatar != null) canvas.drawBitmap(resizeAvatar, 10f, 0f, paint)
        canvas.drawCircle(80f, 70f, 70f, strokeInPaint)
        canvas.drawCircle(80f, 70f, 70f, strokeOutPaint)
        val path = Path().apply {
            moveTo(48f, 134f)
            lineTo(80f, 160f)
            lineTo(112f, 134f)
            lineTo(80f, 136f)
            lineTo(48f, 134f)
            close()
        }
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
        canvas.drawPath(path, paint)
        canvas.drawLine(48f, 134f, 80f, 160f, strokeOutPaint)
        canvas.drawLine(112f, 134f, 80f, 160f, strokeOutPaint)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    suspend fun generateRestaurantMarkerIcon(partyUIState: PartyUIState): BitmapDescriptor {
        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            style = Paint.Style.FILL_AND_STROKE
            color = Color.WHITE
            isAntiAlias = true
        }
        val strokeInPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeWidth = 10f
            isAntiAlias = true
        }
        val strokeOutPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = Color.argb(255, 255, 200, 200)
            strokeWidth = 3f
            isAntiAlias = true
        }

        canvas.drawRect(RectF(0f, 0f, 160f, 160f), paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val image = restaurantRepository.tryFetchImage(partyUIState.restaurant!!.id!!)
        val resizeAvatar = image?.let { Bitmap.createScaledBitmap(it,140, 140, false) }
        if (resizeAvatar != null) canvas.drawBitmap(resizeAvatar, 10f, 10f, paint)
        canvas.drawRect(RectF(0f, 0f, 160f, 160f), strokeInPaint)
        canvas.drawRect(RectF(0f, 0f, 160f, 160f), strokeOutPaint)
        val path = Path().apply {
            moveTo(40f, 160f)
            lineTo(75f, 185f)
            lineTo(110f, 160f)
            lineTo(50f, 160f)
            close()
        }
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
        canvas.drawPath(path, paint)
        canvas.drawLine(40f, 160f, 75f, 185f, strokeOutPaint)
        canvas.drawLine(75f, 185f, 110f, 160f, strokeOutPaint)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    suspend fun postFeedback(userId: String, content: String, achivements: MutableList<AchievementType>) {
        userRepository.updateUserComment(userId, Timestamp.now(), content)
        userRepository.updateAchivements(userId, achivements)
    }

    suspend fun feedbackDone() {
        val partyId = app.currentPartyUIStateStateFlow.value!!.party!!.id!!
        userRepository.deleteCurrentParty(currentUserUIState.value!!.user!!.id!!)
        partyRepository.updateFeedbackDoneList(currentUserUIState.value!!.user!!.id!!, partyId)

    }

}
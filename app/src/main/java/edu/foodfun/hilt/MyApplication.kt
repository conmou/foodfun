package edu.foodfun.hilt

import android.app.Application
import android.util.Log
import androidx.databinding.ObservableArrayMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.HiltAndroidApp
import edu.foodfun.ModelChanges
import edu.foodfun.comparator.UserComparator
import edu.foodfun.enums.FieldChangeType
import edu.foodfun.model.User
import edu.foodfun.model.common.BundleConfig
import edu.foodfun.model.common.RecommendParams
import edu.foodfun.repository.*
import edu.foodfun.uistate.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var restaurantRepository: RestaurantRepository
    @Inject lateinit var partyRepository: PartyRepository
    @Inject lateinit var inviteRepository: InviteRepository
    @Volatile
    var currentUserUIStateStateFlow: MutableStateFlow<UserUIState?> = MutableStateFlow(null)
    @Volatile
    var currentPartyUIStateStateFlow: MutableStateFlow<PartyUIState?> = MutableStateFlow(null)
    @Volatile
    var bundleConfigStateFlow: MutableStateFlow<BundleConfig?> = MutableStateFlow(null)
    @Volatile
    var recommendParamsStateFlow: MutableStateFlow<RecommendParams?> = MutableStateFlow(null)
    @Volatile
    var isCurrentUserDataInitingStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)
    @Volatile
    var isBundleCofigLoaddingStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)
    @Volatile
    var isRecommendParamsLoaddingStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)

    val groupRestaurantChangesMap: ObservableArrayMap<String, ModelChanges<GroupUIState>> = ObservableArrayMap()
    val partyChangesMap: ObservableArrayMap<String, ModelChanges<PartyUIState>> = ObservableArrayMap()
    val nearByUserChangesMap: ObservableArrayMap<String, ModelChanges<UserUIState>> = ObservableArrayMap()
    val friendChangesMap: ObservableArrayMap<String, ModelChanges<UserUIState>> = ObservableArrayMap()
    val inviteChangesMap: ObservableArrayMap<String, ModelChanges<UserUIState>> = ObservableArrayMap()
    val partyInviteChangesMap: ObservableArrayMap<String, ModelChanges<InviteUIState>> = ObservableArrayMap()

    private var listeningCurrentUserJob: Job? = null
    private var listeningCurrentPartyJob: Job? = null
    private var listeningCurrentPartyInvitesJob: Job? = null

    companion object {
        private lateinit var mInstance: MyApplication
        fun getInstance() = mInstance
    }

    override fun onCreate() {
        super.onCreate()

        mInstance = this

        //auth init.
        auth = FirebaseAuth.getInstance()
        auth.addAuthStateListener {
            if (auth.currentUser == null) {
                removeListenCurrentUser()
                removelistenCurrentPartyInvites()
                removeListenCurrentParty()
            }
            else {
                listenCurrentUser()
                listenCurrentPartyInvites()
            }
        }

        //firestore init.
        firestore = Firebase.firestore
        firestore.document("commons/bundleConfig").addSnapshotListener { snap, e ->
            isBundleCofigLoaddingStateFlow.value = true
            if (e != null) {
                Log.i("ErrorWithBundleConfig", e.message.toString())
                return@addSnapshotListener
            }
            if (snap == null) {
                Log.i("ErrorSnapshot", "bundleConfig document is not found.")
                return@addSnapshotListener
            }
            bundleConfigStateFlow.value = snap.toObject<BundleConfig>()!!
            isBundleCofigLoaddingStateFlow.value = false
        }
        firestore.document("commons/usersParams").addSnapshotListener { snap, e ->
            isRecommendParamsLoaddingStateFlow.value = true
            if (e != null) {
                Log.i("ErrorWithBundleConfig", e.message.toString())
                return@addSnapshotListener
            }
            if (snap == null) {
                Log.i("ErrorSnapshot", "RecommendParams document is not found.")
                return@addSnapshotListener
            }
            recommendParamsStateFlow.value = snap.toObject<RecommendParams>()!!
            isRecommendParamsLoaddingStateFlow.value = false
        }

        //fetch all parties.
        MainScope().launch {
            partyRepository.fetchListeningPartiesFlow().collect {
                val party = it.value
                val restaurant = restaurantRepository.fetchRestaurant(it.value.restaurant!!)
                val partyUIState = PartyUIState(party, restaurant)
                if (it.changeType == FieldChangeType.ADDED || it.changeType == FieldChangeType.MODIFIED) {
                    partyChangesMap[party.id!!] = ModelChanges(it.changeType, partyUIState)
                }
                else if (it.changeType == FieldChangeType.REMOVED) {
                    partyChangesMap.remove(party.id)
                }
            }
        }
    }

    private fun listenCurrentUser() {
        if (listeningCurrentUserJob != null && listeningCurrentUserJob!!.isActive) return
        val authUser = auth.currentUser ?: return
        isCurrentUserDataInitingStateFlow.value = true
        val userComparator = UserComparator(object : UserComparator.UserChangeListener() {
            val friendJobs = mutableMapOf<String, Job>()
            val inviteJobs = mutableMapOf<String, Job>()
            val restaurantJobs = mutableMapOf<String, Job>()
            val nearByUserJobs = mutableMapOf<String, Job>()
            val userListMap = mutableMapOf<String, MutableList<String>>()

            override fun onFriendChanged(type: FieldChangeType, userId: String) {
                //manage friend listener below.
                if (type == FieldChangeType.ADDED) {
                    friendJobs[userId]?.cancel()
                    friendJobs[userId] = MainScope().launch(Dispatchers.IO) {
                        userRepository.fetchListeningUserFlow(userId).collectLatest {
                            if (it == null) throw Exception("target document: $userId is not exist in database.")
                            val avatar = userRepository.tryFetchAvatar(userId, it.avatarVersion)
                            friendChangesMap[userId] = ModelChanges(type, UserUIState(it, avatar))
                        }
                    }
                }
                else if (type == FieldChangeType.REMOVED) {
                    friendJobs[userId]?.cancel()
                    friendJobs.remove(userId)
                    MainScope().launch { friendChangesMap.remove(userId) }
                }
            }

            override fun onCurrentPartyChanged(type: FieldChangeType, partyId: String) {
                when (type) {
                    FieldChangeType.ADDED -> {
                        listenCurrentParty()
                    }
                    FieldChangeType.MODIFIED -> {
                        removeListenCurrentParty()
                        listenCurrentParty()
                    }
                    FieldChangeType.REMOVED -> {
                        removeListenCurrentParty()
                    }
                }
            }

            override fun onInviteChanged(type: FieldChangeType, userId: String) {
                if (type == FieldChangeType.ADDED) {
                    inviteJobs[userId]?.cancel()
                    inviteJobs[userId] = MainScope().launch(Dispatchers.IO) {
                        userRepository.fetchListeningUserFlow(userId).collectLatest {
                            if (it == null) throw Exception("target document: $userId is not exist in database.")
                            val avatar = userRepository.tryFetchAvatar(userId, it.avatarVersion)
                            inviteChangesMap[userId] = ModelChanges(type, UserUIState(it, avatar))
                        }
                    }
                }
                else if (type == FieldChangeType.REMOVED) {
                    inviteJobs[userId]?.cancel()
                    inviteJobs.remove(userId)
                    inviteChangesMap.remove(userId)
                }
            }

            override fun onLocationChanged(type: FieldChangeType, location: GeoPoint) {
                val distance = 25.0 * 1000 //25km
                val queries = userRepository.nearbyUserQueries(location.latitude, location.longitude, distance)
//                val hashCodes = queries.map { "${it.hashCode()}" }.toSet()
//                nearByUserJobs.keys.subtract(hashCodes).forEach {
//                    nearByUserJobs[it]?.cancel()
//                    nearByUserJobs.remove("${it.hashCode()}")
//                    userListMap["${it.hashCode()}"]?.forEach { user -> nearByUserChangesMap.remove(user) }
//                    userListMap.remove("${it.hashCode()}")
//                }
                nearByUserJobs.forEach { (_, u) -> u.cancel() }
                nearByUserJobs.clear()
                queries.forEach {
                    nearByUserJobs["${it.hashCode()}"] = MainScope().launch {
                        userRepository.fetchListeningUsersFlow(it).collect { changes ->
                            val user = changes.value
                            if (changes.changeType == FieldChangeType.ADDED || changes.changeType == FieldChangeType.MODIFIED) {
                                if (user.state != "線上") return@collect
                                if (user.id == currentUserUIStateStateFlow.value!!.user!!.id!!) return@collect
                                if (BaseRepository.distanceBetween(currentUserUIStateStateFlow.value!!.user!!.location!!, user.location!!) >= distance) return@collect
                                val avatar = userRepository.tryFetchAvatar(user.id!!, user.avatarVersion)
                                nearByUserChangesMap[user.id] = ModelChanges(changes.changeType, UserUIState(user, avatar))
//                                if (!userListMap.containsKey("${it.hashCode()}")) userListMap["${it.hashCode()}"] = mutableListOf()
//                                userListMap["${it.hashCode()}"]!!.add(user.id)
                            }
                            else if (changes.changeType == FieldChangeType.REMOVED) {
                                nearByUserChangesMap.remove(user.id)
//                                userListMap["${it.hashCode()}"]!!.remove(user.id)
                            }
                        }
                    }
                }
            }

            override fun onGroupRestaurantChanged(type: FieldChangeType, groupName: String, restaurantId: String) {
                if (type == FieldChangeType.ADDED) {
                    restaurantJobs[restaurantId]?.cancel()
                    restaurantJobs[restaurantId] = MainScope().launch(Dispatchers.IO) {
                        val restaurant = restaurantRepository.fetchRestaurant(restaurantId)
                        val image = restaurantRepository.tryFetchImage(restaurantId)
                        val groupUIState = GroupUIState(groupName, RestaurantUIState(restaurant, image))
                        groupRestaurantChangesMap["${groupName}.${restaurantId}"] = ModelChanges(type, groupUIState)
                    }
                }
                else if (type == FieldChangeType.REMOVED) {
                    restaurantJobs[restaurantId]?.cancel()
                    restaurantJobs.remove(restaurantId)
                    groupRestaurantChangesMap.remove("${groupName}.${restaurantId}")
                }
            }
        })

        listeningCurrentUserJob = MainScope().launch(Dispatchers.IO) {
            var tempUser: User? = null
            var listener: ValueEventListener? = null
            var connectRef: DatabaseReference? = null
            userRepository.fetchListeningUserFlow(authUser.uid).onCompletion {
                userRepository.closeRTDBConnect()
                listener?.let { connectRef?.removeEventListener(it) } //remove listener if existed.
            }.collectLatest { user ->
                if (user == null) {
                    if (auth.currentUser == null) {
                        isCurrentUserDataInitingStateFlow.value = false
                        throw Exception("can not create user without sign in.")
                    }
                    else userRepository.createUser(authUser.uid) //if has account but have no user document, create user document.
                }
                else {
                    val avatar = userRepository.tryFetchAvatar(user.id!!, user.avatarVersion)
                    currentUserUIStateStateFlow.value = UserUIState(user, avatar)
                    userComparator.compare(tempUser, user)
                    tempUser = user
                    isCurrentUserDataInitingStateFlow.value = false
                    if (listener != null) return@collectLatest
                    //create online listener
                    connectRef = userRepository.fetchRTDBConnectRef()
                    listener = connectRef!!.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val connected = snapshot.getValue(Boolean::class.java) ?: false
                            if (!connected) {
                                this@launch.launch { userRepository.updateState(tempUser!!.id!!, false) }
                            }
                            else {
                                val userStateRef = userRepository.fetchRTDBUserStatusRef(tempUser!!.id!!)
                                userStateRef.onDisconnect().setValue(hashMapOf("state" to "offline", "lastChanged" to ServerValue.TIMESTAMP)).addOnSuccessListener {
                                    userStateRef.setValue(hashMapOf("state" to "online", "lastChanged" to ServerValue.TIMESTAMP))
                                    this@launch.launch { userRepository.updateState(tempUser!!.id!!, true) }
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                    userRepository.keepRTDBConnecting()
                }
            }
        }
    }

    private fun removeListenCurrentUser() {
        listeningCurrentUserJob?.cancel() //cancel current listening user changes.
        isCurrentUserDataInitingStateFlow.value = false
        currentUserUIStateStateFlow.value = null
    }

    private fun listenCurrentParty() {
        listeningCurrentPartyJob?.cancel()
        listeningCurrentPartyJob = MainScope().launch {
            partyRepository.fetchListeningPartyFlow(currentUserUIStateStateFlow.value!!.user!!.currentParty!!).collect {
                val restaurant = restaurantRepository.fetchRestaurant(it!!.restaurant!!)
                currentPartyUIStateStateFlow.value = PartyUIState(it, restaurant)
            }
        }
    }

    private fun removeListenCurrentParty() {
        listeningCurrentPartyJob?.cancel()
        currentPartyUIStateStateFlow.value = null
    }

    private fun listenCurrentPartyInvites() {
        val authUser = auth.currentUser ?: return
        listeningCurrentPartyInvitesJob?.cancel()
        listeningCurrentPartyInvitesJob = MainScope().launch {
            inviteRepository.fetchListeningPartyInviteFlow(authUser.uid).collect {
                if (it.changeType == FieldChangeType.ADDED || it.changeType == FieldChangeType.MODIFIED) {
                    val party = partyRepository.fetchParty(it.value.party!!)
                    val user = userRepository.fetchUser(it.value.sender!!)
                    partyInviteChangesMap[it.value.id] = ModelChanges(it.changeType, InviteUIState(it.value, user, party))
                }
                else if (it.changeType == FieldChangeType.REMOVED) {
                    partyInviteChangesMap.remove(it.value.id)
                }
            }
        }
    }

    private fun removelistenCurrentPartyInvites() {
        listeningCurrentPartyInvitesJob?.cancel()
        partyInviteChangesMap.clear()
    }

}
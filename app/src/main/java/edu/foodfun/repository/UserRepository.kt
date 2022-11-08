package edu.foodfun.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.Timestamp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.*
import edu.foodfun.ModelChanges
import edu.foodfun.deserializer.UserCommentDocumentDeserializer
import edu.foodfun.deserializer.UserDocumentDeserializer
import edu.foodfun.enums.AchievementType
import edu.foodfun.model.User
import edu.foodfun.model.UserComment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class UserRepository @Inject constructor(private val userCommentDeserializer: UserCommentDocumentDeserializer, private val userDeserializer: UserDocumentDeserializer, context: Context): BaseRepository(context) {

    companion object {
        internal const val userCollectionName = "users"
        internal const val commentCollectionName = "comments"
        internal const val achivements = "achivements"
        internal const val humorous = "幽默"
        internal const val chatty = "健談"
        internal const val considerate = "細心"
        internal const val introverted = "內向"
        internal const val lech = "老司機"
        internal const val bio = "bio"
        internal const val birthday = "birthday"
        internal const val location = "location"
        internal const val locationHash = "locationHash"
        internal const val defaultGroupName = "未分類"
        internal const val friends = "friends"
        internal const val currentParty = "currentParty"
        internal const val avatarVersion = "avatarVersion"
        internal const val nickName = "nickName"
        internal const val recommendParams = "recommendParams"
        internal const val sex = "sex"
        internal const val state = "state"
        internal const val groups = "groups"
        internal const val invites = "invites"
        internal const val projectUrl = "https://us-central1-foodfun-1c362.cloudfunctions.net/"
        internal const val avatarStoragePrefix = "users/avatars/"
        internal const val avatarExtention = ".jpg"
        internal const val time = "time"
        internal const val content = "content"
        private val achivementsType = arrayOf("幽默", "健談", "細心", "內向", "老司機") //改成enum

        fun generateUser(): User {
            val hashMap = hashMapOf<String, Int>()
            achivementsType.forEach { hashMap[it] = 0 }
            return User(groups = hashMapOf("未分類" to emptyList()), achivements = hashMap)
        }
    }

    private fun userDocRef(userId: String): DocumentReference = firestore.document("$userCollectionName/$userId")

    suspend fun fetchUser(userId: String): User = fetchDocumentSnapShot(userDocRef(userId), userDeserializer)

    suspend fun updateLocation(userId: String, location: GeoPoint): Unit = suspendCoroutine { cont ->
        val userRef = userDocRef(userId)
        val hash = GeoFireUtils.getGeoHashForLocation(GeoLocation(location.latitude, location.longitude))
        val updateTask = userRef.update(UserRepository.location, location, locationHash, hash)
        updateTask.addOnSuccessListener { cont.resume(Unit) }
        updateTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun createUser(userId: String): Unit = suspendCoroutine { cont ->
        val userRef = userDocRef(userId)
        val user = generateUser()
        val setTask = userRef.set(user)
        setTask.addOnSuccessListener { cont.resume(Unit) }
        setTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    fun keepRTDBConnecting() {
        database.goOnline()
        databaseRef(Date().toString()).keepSynced(true)
    }

    fun closeRTDBConnect() {
        database.goOffline()
        databaseRef(Date().toString()).keepSynced(false)
    }

    fun fetchRTDBUserStatusRef(userId: String): DatabaseReference = databaseRef("status/$userId")

    fun fetchRTDBConnectRef(): DatabaseReference = databaseRef(".info/connected")

    suspend fun addFriend(userId: String, friendId: String) = suspendCancellableCoroutine { cont ->
        val userDocRef = userDocRef(userId)
        val updateTask = userDocRef.update(friends, FieldValue.arrayUnion(friendId))
        updateTask.addOnSuccessListener { cont.resume(Unit) }
        updateTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun removeFriend(userId: String, friendId: String) = suspendCancellableCoroutine { cont ->
        val userDocRef = userDocRef(userId)
        val updateTask = userDocRef.update(friends, FieldValue.arrayRemove(friendId))
        updateTask.addOnSuccessListener { cont.resume(Unit) }
        updateTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun addRecommendParams(user: User, price: Double?, restaurantLocation: GeoPoint, type: List<String>): Unit = suspendCoroutine {
        // commons要移出去嗎?
        val commonsRef: DocumentReference = firestore.document("/commons/usersParams")
        val userRef = userDocRef(user.id!!)
        val distance = distanceBetween(user.location!!, restaurantLocation)
        val indexDistance = kotlin.math.floor(distance / 250).toInt()

        firestore.runBatch {
            if (price != null) {
                val indexPrice = kotlin.math.floor(price / 100).toInt()
                it.update(userRef, "recommendParams.price.$indexPrice", FieldValue.increment(1))
                it.update(commonsRef, "recommendParams.price.$indexPrice", FieldValue.increment(1))
            }
            it.update(userRef, "recommendParams.distance.$indexDistance", FieldValue.increment(1))
            it.update(commonsRef, "recommendParams.distance.$indexDistance", FieldValue.increment(1))
            type.forEach { restaurantType ->
                it.update(userRef, "recommendParams.type.$restaurantType", FieldValue.increment(1))
                it.update(commonsRef, "recommendParams.type.$restaurantType", FieldValue.increment(1))
            }
        }
    }

    suspend fun addRestaurant(userId: String, groupName: String = defaultGroupName, restaurantId: String): Unit = suspendCoroutine { cont ->
        val userDocRef = userDocRef(userId)
        val updateTask = userDocRef.update("$groups.$groupName", FieldValue.arrayUnion(restaurantId))
        updateTask.addOnSuccessListener { cont.resume(Unit) }
        updateTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun removeRestaurant(userId: String, groupName: String = defaultGroupName, restaurantId: String): Unit = suspendCoroutine { cont ->
        val userRef = userDocRef(userId)
        val updateTask = userRef.update("$groups.$groupName", FieldValue.arrayRemove(restaurantId))
        updateTask.addOnSuccessListener { cont.resume(Unit) }
        updateTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun removeInvite(userId: String, InviteId: String): Unit = suspendCoroutine { cont ->
        val userDocRef = userDocRef(userId)
        val updateTask = userDocRef.update(invites, FieldValue.arrayRemove(InviteId))
        updateTask.addOnSuccessListener { cont.resume(Unit) }
        updateTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun tryFetchAvatar(userId: String, avatarVersion: Long?): Bitmap? = suspendCoroutine { cont ->
        val file = getFileFromCache(userId, RestaurantRepository.imageExtention)
        if (!file.exists() && file.length() != 0L) {
            cont.resume(BitmapFactory.decodeFile(file.absolutePath))
            return@suspendCoroutine
        }
        val fileDownloadTask = storage.child("${avatarStoragePrefix}${userId}_${avatarVersion}${avatarExtention}").getFile(file)
        fileDownloadTask.addOnSuccessListener { cont.resume(BitmapFactory.decodeFile(file.absolutePath)) }
        fileDownloadTask.addOnFailureListener { cont.resume(null) }
    }

    suspend fun updateAvatar(userId: String, data: ByteArray, avatarVersion: Long): Boolean = suspendCoroutine { cont ->
        val uploadTask = storage.child("${avatarStoragePrefix}${userId}_${avatarVersion}${avatarExtention}").putBytes(data)
        uploadTask.addOnSuccessListener { cont.resume(true) }
        uploadTask.addOnFailureListener { cont.resume(false) }
    }

    suspend fun updateAvatarVersion(userId: String, avatarVersion: Long): Boolean = suspendCoroutine { cont ->
        val userDocRef = userDocRef(userId)
        val uploadTask = userDocRef.update(UserRepository.avatarVersion, avatarVersion)
        uploadTask.addOnSuccessListener { cont.resume(true) }
        uploadTask.addOnFailureListener { cont.resume(false) }
    }

    fun nearbyUserQueries(lat: Double, lng: Double, radius: Double): List<Query> {
        val queries = mutableListOf<Query>()
        val bounds = GeoFireUtils.getGeoHashQueryBounds(GeoLocation(lat, lng), radius)
        bounds.forEach {
            val query = firestore.collection("users")
                .whereEqualTo(state, "線上")
                .orderBy(locationHash)
                .startAt(it.startHash)
                .endAt(it.endHash)
            queries.add(query)
        }
        return queries.toList()
    }

    suspend fun fetchListeningUserFlow(userId: String): Flow<User?> = fetchListeningDocumentFlow(userDocRef(userId), userDeserializer)

    suspend fun fetchListeningUsersFlow(query: Query): Flow<ModelChanges<User>> = fetchListeningQueryFlow(query, userDeserializer)

    suspend fun fetchListeningUserCommentsFlow(query: Query): Flow<ModelChanges<UserComment>> = fetchListeningQueryFlow(query, userCommentDeserializer)

    suspend fun updateNickName(userId: String, name : String): Unit = suspendCoroutine { cont ->
        val userRef = userDocRef(userId)
        val updateTask = userRef.update(nickName, name)
        updateTask.addOnSuccessListener { cont.resume(Unit) }
        updateTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun updateState(userId: String, isOnline: Boolean): Unit = suspendCoroutine { cont ->
        val userRef = userDocRef(userId)
        val updateTask = userRef.update(state, if (isOnline) "線上" else "離線")
        updateTask.addOnSuccessListener { cont.resume(Unit) }
        updateTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun updateBio(userId: String, content : String): Unit = suspendCoroutine { cont ->
        val userRef = userDocRef(userId)
        val updateTask = userRef.update(bio, content)
        updateTask.addOnSuccessListener { cont.resume(Unit) }
        updateTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun updateSex(userId: String, sex : String): Unit = suspendCoroutine { cont ->
        val userRef = userDocRef(userId)
        val updateTask = userRef.update(UserRepository.sex, sex)
        updateTask.addOnSuccessListener { cont.resume(Unit) }
        updateTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun updateCurrentParty(userId: String, partyId : String): Unit = suspendCoroutine { cont ->
        val userRef = userDocRef(userId)
        val updateTask = userRef.update(currentParty, partyId)
        updateTask.addOnSuccessListener { cont.resume(Unit) }
        updateTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun deleteCurrentParty(userId: String): Unit = suspendCoroutine { cont ->
        val userRef = userDocRef(userId)
        val deleteTask = userRef.update(currentParty, FieldValue.delete())
        deleteTask.addOnSuccessListener { cont.resume(Unit) }
        deleteTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun updateBirthday(userId: String, newBirthday : Timestamp): Unit = suspendCoroutine { cont ->
        val userRef = userDocRef(userId)
        val updateTask = userRef.update(birthday, newBirthday)
        updateTask.addOnSuccessListener { cont.resume(Unit) }
        updateTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun createGroup(userId: String, groupName: String, content: MutableList<String>? = mutableListOf()): Unit = suspendCoroutine { cont ->
        val userRef = userDocRef(userId)
        val hashMap = hashMapOf(groups to hashMapOf(groupName to content))
        val updateTask = userRef.set(hashMap, SetOptions.merge())
        updateTask.addOnSuccessListener { cont.resume(Unit) }
        updateTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun removeGroup(userId: String, groupName: String): Unit = suspendCoroutine { cont ->
        val userRef = userDocRef(userId)
        val updateTask = userRef.update("$groups.$groupName", FieldValue.delete())
        updateTask.addOnSuccessListener { cont.resume(Unit) }
        updateTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun updateAchivements(userId: String, achivements: MutableList<AchievementType>): Unit = suspendCoroutine { cont ->
        val userRef = userDocRef(userId)
        val updateTask = firestore.runBatch {
            if(AchievementType.HUMOROUS in achivements)
                it.update(userRef, "achivements.$humorous", FieldValue.increment(1))
            if(AchievementType.CHATTY in achivements)
                it.update(userRef, "achivements.$chatty", FieldValue.increment(1))
            if(AchievementType.CONSIDERATE in achivements)
                it.update(userRef, "achivements.$considerate", FieldValue.increment(1))
            if(AchievementType.INTROVERTED in achivements)
                it.update(userRef, "achivements.$introverted", FieldValue.increment(1))
            if(AchievementType.LECH in achivements)
                it.update(userRef, "achivements.$lech", FieldValue.increment(1))
        }
        updateTask.addOnSuccessListener { cont.resume(Unit) }
        updateTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun fetchUserComments(userId: String): List<UserComment> {
        val userRef = userDocRef(userId)
        val query = userRef.collection(commentCollectionName).orderBy(time, Query.Direction.ASCENDING)
        return fetchQuerySnapShot(query, userCommentDeserializer)
    }

    suspend fun updateUserComment(userId: String, time: Timestamp, content: String): Unit = suspendCoroutine { cont ->
        val userRef = userDocRef(userId)
        val hashMap = hashMapOf<String, Any>(
            UserRepository.time to time,
            UserRepository.content to content
        )
        val setTask = userRef.collection(commentCollectionName).document().set(hashMap)
        setTask.addOnSuccessListener { cont.resume(Unit) }
        setTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun updateInviteList(userId: String, targetUserId: String): Unit = suspendCoroutine { cont ->
        val userRef = userDocRef(userId)
        val updateTask = userRef.update(invites, FieldValue.arrayUnion(targetUserId))
        updateTask.addOnSuccessListener { cont.resume(Unit) }
        updateTask.addOnFailureListener { cont.resumeWithException(it) }
    }
}
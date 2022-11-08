package edu.foodfun.repository

import android.content.Context
import android.content.SharedPreferences
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import edu.foodfun.ModelChanges
import edu.foodfun.deserializer.IFirestoreDocumentDeserializer
import edu.foodfun.enums.FieldChangeType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


abstract class BaseRepository constructor(val context: Context) {
    private val sharedPrefConfigName = "config"
    internal val storage = Firebase.storage("gs://foodfun-1c362.appspot.com").reference
    internal val firestore = Firebase.firestore
    internal val database = Firebase.database("https://foodfun-1c362-default-rtdb.asia-southeast1.firebasedatabase.app/")
    internal val sharedConfigPref: SharedPreferences = context.getSharedPreferences(sharedPrefConfigName, Context.MODE_PRIVATE)
    
    companion object {
        fun distanceBetween(location1: GeoPoint, location2: GeoPoint): Double {
            return GeoFireUtils.getDistanceBetween(
                GeoLocation(location1.latitude, location1.longitude),
                GeoLocation(location2.latitude, location2.longitude)
            )
        }
    }

    fun getFileFromCache(fileName: String, suffix: String): File = File(context.cacheDir, "$fileName$suffix")

    @Suppress("BlockingMethodI123nNonBlockingContext")
    suspend fun downloadFromStorage(storagePath: String, cacheFileName: String, suffix: String): File = suspendCoroutine { cont ->
        val file = getFileFromCache(cacheFileName, suffix)
        val fileDownloadTask = storage.child(storagePath).getFile(file)
        fileDownloadTask.addOnSuccessListener { cont.resume(file) }
        fileDownloadTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    protected suspend fun downloadBundle(storagePath: String): ByteArray {
        val file = downloadFromStorage(storagePath, "bundle", "txt")
        return file.readBytes()
    }

    protected suspend fun loadBundleIntoFirestore(byteArrayBundle: ByteArray): Unit = suspendCoroutine { cont ->
        val loadBundleTask = firestore.loadBundle(byteArrayBundle)
        loadBundleTask.addOnSuccessListener { cont.resume(Unit) }
        loadBundleTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    protected suspend fun tryGetNamedQuery(queryName: String): Query? = suspendCoroutine { cont ->
        val queryTask = firestore.getNamedQuery(queryName)
        queryTask.addOnSuccessListener { cont.resume(it) }
        queryTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    protected suspend fun <T> fetchDocumentSnapShot(reference: DocumentReference, deserializer: IFirestoreDocumentDeserializer<T>): T = suspendCoroutine { cont ->
        val documentSnapTask = reference.get()
        documentSnapTask.addOnSuccessListener {
            if (!it.exists()) {
                cont.resumeWithException(Exception("Snapshot document is empty. ${it.id} (If not exsit in firestore, maybe was cause by cache. Wait a minute later and try it again.)"))
                return@addOnSuccessListener
            }
            cont.resume(deserializer.deserialize(it))
        }
        documentSnapTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    protected suspend fun <T> fetchQuerySnapShot(query: Query, deserializer: IFirestoreDocumentDeserializer<T>): List<T> = suspendCoroutine { cont ->
        val querySnapTask = query.get()
        querySnapTask.addOnSuccessListener { querySnap ->
            val result = mutableListOf<T>()
            querySnap.documents.forEach {
                result.add(deserializer.deserialize(it))
            }
            cont.resume(result.toList())
        }
        querySnapTask.addOnFailureListener { cont.resumeWithException(it) }
    }

    protected suspend fun <T> fetchListeningDocumentFlow(reference: DocumentReference, deserializer: IFirestoreDocumentDeserializer<T>): Flow<T?> = callbackFlow {
        val eventListener = EventListener<DocumentSnapshot> { docSnap, error ->
            if (error != null) {
                close(error)
                return@EventListener
            }
            if (!docSnap!!.exists()) {
                trySend(null)
                return@EventListener
            }
            val model = deserializer.deserialize(docSnap)
            trySend(model)
        }
        val listener = fetchListeningDocument(reference, eventListener)
        awaitClose { listener.remove() }
    }

    protected suspend fun <T> fetchListeningQueryFlow(query: Query, deserializer: IFirestoreDocumentDeserializer<T>): Flow<ModelChanges<T>> = callbackFlow {
        val eventListener = EventListener<QuerySnapshot> { querySnap, error ->
            if (error != null) {
                close(error)
                return@EventListener
            }
            if (querySnap == null) return@EventListener
            querySnap.documentChanges.forEach {
                val model = deserializer.deserialize(it.document)
                val baseChanges: ModelChanges<T> = when (it.type) {
                    DocumentChange.Type.ADDED -> ModelChanges(FieldChangeType.ADDED, model)
                    DocumentChange.Type.MODIFIED -> ModelChanges(FieldChangeType.MODIFIED, model)
                    DocumentChange.Type.REMOVED -> ModelChanges(FieldChangeType.REMOVED, model)
                }
                trySend(baseChanges)
            }
        }
        val listener = fetchListeningQuery(query, eventListener)
        awaitClose { listener.remove() }
    }

    protected fun fetchListeningDocument(reference: DocumentReference, eventListener: EventListener<DocumentSnapshot>): ListenerRegistration {
        return reference.addSnapshotListener(eventListener)
    }

    protected fun fetchListeningQuery(query: Query, eventListener: EventListener<QuerySnapshot>): ListenerRegistration = query.addSnapshotListener(eventListener)

    protected fun databaseRef(path: String): DatabaseReference = database.getReference(path)
}
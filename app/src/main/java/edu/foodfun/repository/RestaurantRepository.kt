package edu.foodfun.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Query
import edu.foodfun.deserializer.RestaurantDocumentDeserializer
import edu.foodfun.hilt.MyApplication
import edu.foodfun.model.Restaurant
import edu.foodfun.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.random.Random
import kotlinx.coroutines.*

class RestaurantRepository @Inject constructor(val deserializer: RestaurantDocumentDeserializer, context: Context) : BaseRepository(context) {
    companion object {
        internal const val collectionName = "restaurants"
        internal const val price = "price"
        internal const val type = "type"
        internal const val avgCost = "avgCost"
        internal const val random = "random"
        internal const val name = "name"
        internal const val tempRestaurantsStoragePrefix = "restaurants/bundles/"
        internal const val restaurantImageStoragePrefix = "restaurants/images/"
        internal const val imageExtention = ".jpg"
        internal const val bundleExtention = ".txt"
        internal const val restaurantBundleNamedQuery = "restaurantBundle"
        const val restaurantBundleVersion = "restaurantBundleVersion"
    }

    private fun restaurantDocRef(restaurantId: String): DocumentReference = firestore.document("${collectionName}/$restaurantId")

    suspend fun tryFetchImage(restaurantId: String): Bitmap? = suspendCancellableCoroutine { cont ->
        val file = getFileFromCache(restaurantId, imageExtention)
        if (!file.exists() && file.length() != 0L) {
            cont.resume(BitmapFactory.decodeFile(file.absolutePath))
            return@suspendCancellableCoroutine
        }
        val fileDownloadTask = storage.child("${restaurantImageStoragePrefix}${restaurantId}${imageExtention}").getFile(file)
        fileDownloadTask.addOnSuccessListener { cont.resume(BitmapFactory.decodeFile(file.absolutePath)) }
        fileDownloadTask.addOnFailureListener { cont.resume(null) }
    }

    suspend fun fetchRestaurant(restaurantId: String): Restaurant = fetchDocumentSnapShot(restaurantDocRef(restaurantId), deserializer)

    suspend fun fetchSearchRestaurant(txtSearch: String): List<Restaurant> {
        val query = fetchRestaurantBundleQuery()
        val filter = query.whereGreaterThanOrEqualTo(name, txtSearch)
            .whereLessThanOrEqualTo(name, txtSearch + "\uf8ff")
        return fetchQuerySnapShot(filter, deserializer)
    }

    @Suppress("UNCHECKED_CAST", "BlockingMethodInNonBlockingContext")
    suspend fun fetchRecommendRestaurants(user: User? = null): Flow<Restaurant> {
        val maxCount = 60 //單次最大回傳量
        var resultList: MutableList<Restaurant> = mutableListOf()
        return flow {
            if (!isLocalRestaurantsBundleNewest() || !isRestaurantBundleExist()) updateLocalRestaurantBundle()
            val baseQuery = fetchRestaurantBundleQuery()
            if(user == null) { fetchQuerySnapShot(baseQuery, deserializer).shuffled().take(maxCount).forEach { emit(it) } }
            else {
                val app = MyApplication.getInstance()
                val priceList: MutableList<Int> =
                    if (user.recommendParams[price] == null) mutableListOf()
                    else (user.recommendParams[price] as HashMap<String, Int>)
                        .toList()
                        .sortedByDescending { (_, value) -> value }
                        .map { it.first.toInt() }
                        .toMutableList()
                val typeList: MutableList<String> =
                    if (user.recommendParams[price] == null) mutableListOf()
                    else (user.recommendParams[type] as HashMap<String, Int>)
                        .toList()
                        .sortedByDescending { (_, value) -> value }
                        .map { it.first }
                        .toMutableList()

                while (app.isRecommendParamsLoaddingStateFlow.value) delay(100)

                if(priceList.size < 5) {
                    val commonPriceMap = (app.recommendParamsStateFlow.value?.recommendParams!![price] as HashMap<String, Int>).toList().sortedByDescending { (_, value) -> value }
                    commonPriceMap.forEach {
                        if (priceList.size >= 5) return@forEach
                        if (priceList.contains(it.first.toInt())) return@forEach
                        priceList.add(it.first.toInt())
                    }
                }

                if(typeList.size < 5) {
                    val commonTypeMap = (app.recommendParamsStateFlow.value?.recommendParams!![price] as HashMap<String, Int>).toList().sortedByDescending { (_, value) -> value }
                    commonTypeMap.forEach {
                        if (typeList.size >= 5) return@forEach
                        typeList.add(it.first)
                    }
                }

                val queries = mutableListOf<Query>()
                priceList.forEach { price ->
                    typeList.forEach { type ->
                        val filter = baseQuery
                            .whereGreaterThan(avgCost, price * 100)
                            .whereLessThan(avgCost, price * 100 + 99)
                            .whereArrayContains(RestaurantRepository.type, type)
                        queries.add(filter)
                    }
                }

                withContext(Dispatchers.Default) {
                    val rnd = Random(LocalDateTime.now().second).nextInt(0, 1000000)
                    val filter =
                        if (rnd > 500000) baseQuery.whereLessThanOrEqualTo(random, rnd).limit(70)
                        else baseQuery.whereGreaterThanOrEqualTo(random, rnd).limit(70)
                    awaitAll(
                        async {
                            queries.map { async { resultList.addAll(fetchQuerySnapShot(it, deserializer)) } }.forEach { it.await() }
                        },
                        async {
                            val result = fetchQuerySnapShot(filter, deserializer)
                            result.forEach { restaurant ->
                                typeList.forEach {
                                    if (restaurant.type.contains(it)) return@forEach
                                    resultList.add(restaurant)
                                }
                            }
                        },
                    )
                }

//                withContext(Dispatchers.Default) {
//                    queries.map { async { resultList.addAll(fetchQuerySnapShot(it, deserializer)) } }.forEach { it.await() }
//                    val rnd = Random(LocalDateTime.now().second).nextInt(0, 1000000)
//                    val filter =
//                        if (rnd > 500000) baseQuery.whereLessThanOrEqualTo(random, rnd).limit(70)
//                        else baseQuery.whereGreaterThanOrEqualTo(random, rnd).limit(70)
//                    val result = fetchQuerySnapShot(filter, deserializer)
//                    result.forEach { restaurant ->
//                        typeList.forEach {
//                            if (restaurant.type!!.contains(it)) return@forEach
//                            resultList.add(restaurant)
//                        }
//                    }
//                }

                resultList = resultList.distinctBy { it.id }.toMutableList() //去除重複
                resultList.shuffled().take(maxCount).forEach { emit(it) }
            }
        }
    }

    private suspend fun updateLocalRestaurantBundle() {
        try {
            val app = MyApplication.getInstance()
            while (app.isBundleCofigLoaddingStateFlow.value) { delay(100) }
            if (app.bundleConfigStateFlow.value!!.restaurantBundleVersion == getLocalRestaurantsBundleVersion() && isRestaurantBundleExist()) return
            val bundleBytes = downloadBundle("${tempRestaurantsStoragePrefix}${app.bundleConfigStateFlow.value!!.restaurantBundleVersion}${bundleExtention}")
            loadBundleIntoFirestore(bundleBytes)
            sharedConfigPref.edit().putLong(restaurantBundleVersion, app.bundleConfigStateFlow.value!!.restaurantBundleVersion).apply()
        }
        catch(e: Exception) {
            throw e
        }
    }

    private suspend fun fetchRestaurantBundleQuery(): Query = tryGetNamedQuery(restaurantBundleNamedQuery)!!

    private fun getLocalRestaurantsBundleVersion(): Long = sharedConfigPref.getLong(restaurantBundleVersion, -1L)

    private suspend fun isRestaurantBundleExist(): Boolean = tryGetNamedQuery(restaurantBundleNamedQuery) != null

    private suspend fun isLocalRestaurantsBundleNewest(): Boolean {
        val app = MyApplication.getInstance()
        val currentVersion = getLocalRestaurantsBundleVersion()
        if (currentVersion == -1L) return false
        while (app.isBundleCofigLoaddingStateFlow.value) { delay(100) }
        if (currentVersion < app.bundleConfigStateFlow.value!!.restaurantBundleVersion) return false
        return true
    }

}
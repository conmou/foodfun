package edu.foodfun.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.data.geojson.GeoJsonLayer
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.foodfun.hilt.MyApplication
import edu.foodfun.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class ListViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel(){
    val app = MyApplication.getInstance()
    val currentUserUIState = app.currentUserUIStateStateFlow
    val groupRestaurantChanges = app.groupRestaurantChangesMap
    val currentMarker: MutableStateFlow<Marker?> = MutableStateFlow(null)
    val currentDirectionLayer: MutableStateFlow<GeoJsonLayer?> = MutableStateFlow(null)

    fun generateMarkerBounds(vararg LatLngs: LatLng): LatLngBounds {
        val builder = LatLngBounds.builder()
        LatLngs.forEach { builder.include(it) }
        return builder.build()
    }

    fun generateCameraUpdate(bounds: LatLngBounds, padding: Int): CameraUpdate {
        return CameraUpdateFactory.newLatLngBounds(bounds, padding)
    }

    suspend fun createGroup(groupName: String, content: MutableList<String>? = mutableListOf()) {
        userRepository.createGroup(currentUserUIState.value!!.user!!.id!!, groupName, content)
    }

    suspend fun removeGroup(groupName: String, isTransToDefaultGroup: Boolean = true) = withContext(Dispatchers.IO) {
        if (isTransToDefaultGroup) {
            val restaurantList = currentUserUIState.value!!.user!!.groups[groupName]
            restaurantList?.forEach { addRestaurantToDefaultGroup(it) }
        }
        userRepository.removeGroup(currentUserUIState.value!!.user!!.id!!, groupName)
    }

    suspend fun removeRestaurantFromGroup(restaurantId: String) = withContext(Dispatchers.IO) {
        val groupMap = app.currentUserUIStateStateFlow.value!!.user!!.groups
        groupMap.forEach { (groupName, list) ->
            if (list.indexOfFirst { it == restaurantId } != -1) {
                this.launch { userRepository.removeRestaurant(currentUserUIState.value!!.user!!.id!!, groupName, restaurantId) }
            }
        }
    }

    suspend fun addRestaurantToDefaultGroup(restaurantId: String) = withContext(Dispatchers.IO) {
        userRepository.addRestaurant(currentUserUIState.value!!.user!!.id!!, restaurantId = restaurantId)
    }

    suspend fun addRestaurantToGroup(groupName: String, restaurantId: String) = withContext(Dispatchers.IO) {
        userRepository.addRestaurant(currentUserUIState.value!!.user!!.id!!, groupName, restaurantId)
    }

    suspend fun fetchDirections(startPosition: GeoPoint, endPosition: GeoPoint) = suspendCoroutine {
        val startTemplate = "${startPosition.longitude},${startPosition.latitude}"
        val endTemplate = "${endPosition.longitude},${endPosition.latitude}"
        val url = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=5b3ce3597851110001cf62481cbd8d45bd5945c098761b2cdedf809e&start=${startTemplate}&end=${endTemplate}"
        val request = Request.Builder().url(url).build()
        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                it.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonObject = JSONObject(response.body!!.string())
                it.resume(jsonObject)
            }
        })
    }

    suspend fun renameGroup(groupName: String, newGroupName: String) = withContext(Dispatchers.IO) {
        val restaurantList = currentUserUIState.value!!.user!!.groups[groupName]
        removeGroup(groupName, false)
        createGroup(newGroupName)
        restaurantList?.forEach { addRestaurantToGroup(newGroupName, it) }
    }
}
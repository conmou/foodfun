package edu.foodfun.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.databinding.ObservableArrayMap
import androidx.databinding.ObservableMap
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.tabs.TabLayout
import com.google.maps.android.data.geojson.GeoJsonLayer
import dagger.hilt.android.AndroidEntryPoint
import edu.foodfun.R
import edu.foodfun.ListSwipe
import edu.foodfun.ModelChanges
import edu.foodfun.adapter.RestaurantRecyclerViewAdapter
import edu.foodfun.dialog.GroupsManagerDialog
import edu.foodfun.enums.ListSwipeType
import edu.foodfun.model.Restaurant
import edu.foodfun.uistate.GroupUIState
import edu.foodfun.uistate.RestaurantUIState
import edu.foodfun.viewmodel.ListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ListFragment : Fragment() {
    private lateinit var recyclerRestList: RecyclerView
    private lateinit var tabGroups: TabLayout
    private lateinit var btnManagerGroups: ImageButton
    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: MapView
    private val vm: ListViewModel by activityViewModels()
    private var groupNameList: MutableList<String> = mutableListOf()
    private var currentGroupRestaurantList: MutableList<RestaurantUIState> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabGroups = view.findViewById(R.id.tabGroups)
        btnManagerGroups = view.findViewById(R.id.btnManagerGroups)
        recyclerRestList = view.findViewById(R.id.recyclerRestList)
        mapView = view.findViewById(R.id.ListMapView)
        mapView.onCreate(savedInstanceState)

        val mapChangedCallback = object : ObservableMap.OnMapChangedCallback<ObservableArrayMap<String, ModelChanges<GroupUIState>>, String, ModelChanges<GroupUIState>>() {
            override fun onMapChanged(sender: ObservableArrayMap<String, ModelChanges<GroupUIState>>?, key: String?) {
                val changes = sender?.get(key)
                if (changes != null) {
                    if (changes.value.groupName != tabGroups.getTabAt(tabGroups.selectedTabPosition)?.text.toString()) return
                    currentGroupRestaurantList.add(changes.value.restaurantUIState)
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main) {
                            recyclerRestList.adapter?.notifyItemInserted(currentGroupRestaurantList.size)
                        }
                    }
                }
                else {
                    val target = currentGroupRestaurantList.indexOfFirst { key!!.contains(key) }
                    if (target == -1) return
                    currentGroupRestaurantList.removeAt(target)
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main) {
                            recyclerRestList.adapter?.notifyItemRemoved(target)
                        }
                    }
                }
            }
        }

        tabGroups.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                synchronized(this) {
                    vm.groupRestaurantChanges.addOnMapChangedCallback(mapChangedCallback)
                    vm.groupRestaurantChanges.forEach {
                        if (it.value.value.groupName != tabGroups.getTabAt(tabGroups.selectedTabPosition)?.text.toString()) return@forEach
                        currentGroupRestaurantList.add(it.value.value.restaurantUIState)
                        recyclerRestList.adapter?.notifyItemInserted(currentGroupRestaurantList.size)
                    }
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                currentGroupRestaurantList.clear()
                recyclerRestList.adapter?.notifyDataSetChanged()
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        btnManagerGroups.setOnClickListener {
            GroupsManagerDialog(object : GroupsManagerDialog.CallBackListener() {
                override fun onAddGroup(groupName: String) {
                    lifecycleScope.launchWhenCreated { vm.createGroup(groupName) }
                }
            }).show(childFragmentManager, "dialogFragment")
        }

        val favoriteListRecyclerViewAdapter = RestaurantRecyclerViewAdapter(currentGroupRestaurantList, object : RestaurantRecyclerViewAdapter.FavoriteRestItemObserver {
            override fun onDelete(index: Int, restaurant: Restaurant) {
                lifecycleScope.launch { vm.removeRestaurantFromGroup(restaurant.id!!) }
            }

            override fun onItemClick(index: Int, restaurant: Restaurant) {
                val location = restaurant.location!!
                vm.currentMarker.value?.remove()
                vm.currentMarker.value = googleMap.addMarker(MarkerOptions().position(LatLng(location.latitude, location.longitude)).title(restaurant.name))!!
                lifecycleScope.launch {
                    val jsonObject = vm.fetchDirections(vm.currentUserUIState.value!!.user!!.location!!, location)
                    val layer = GeoJsonLayer(googleMap, jsonObject)
                    layer.addLayerToMap()
                    vm.currentDirectionLayer.value?.removeLayerFromMap()
                    vm.currentDirectionLayer.value = layer
                }
            }

            override fun onItemLongClick(index: Int, restaurant: Restaurant) {
                val listItems = groupNameList.toTypedArray()
                val mBuilder = AlertDialog.Builder(context)
                mBuilder.setTitle("選擇群組")
                mBuilder.setItems(listItems) { dialog, which ->
                    lifecycleScope.launchWhenCreated {
                        vm.removeRestaurantFromGroup(restaurant.id!!)
                        delay(1000)
                        vm.addRestaurantToGroup(listItems[which], restaurant.id)
                    }.invokeOnCompletion { dialog.dismiss() }
                }
                mBuilder.setNegativeButton("取消") { dialog, _ ->
                    dialog.dismiss()
                }
                val mDialog = mBuilder.create()
                mDialog.show()
            }
        })

        val listSwipe = ListSwipe(requireContext(), ListSwipeType.FAVORITE)
        val itemTouchHelper = ItemTouchHelper(listSwipe)
        itemTouchHelper.attachToRecyclerView(recyclerRestList)

        recyclerRestList.setItemViewCacheSize(1000)
        recyclerRestList.layoutManager = LinearLayoutManager(context)
        recyclerRestList.adapter = favoriteListRecyclerViewAdapter

        mapView.getMapAsync {
            googleMap = it
            googleMap.isMyLocationEnabled = true

            launchFlow()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    private fun launchFlow() {
        lifecycleScope.launchWhenCreated {
            vm.currentMarker.filterNotNull().collectLatest {
                val currentLocation = vm.currentUserUIState.value!!.user!!.location!!
                val bounds = vm.generateMarkerBounds(it.position, LatLng(currentLocation.latitude, currentLocation.longitude))
                val cameraUpdate = vm.generateCameraUpdate(bounds, 200)
                googleMap.animateCamera(cameraUpdate)
            }
        }
        lifecycleScope.launchWhenCreated {
            vm.currentUserUIState.filterNotNull().collectLatest {
                val newGroupNameList = it.user!!.groups.keys.toList()
                val addGroupNames = newGroupNameList.subtract(groupNameList.toSet())
                val removeGroupNames = groupNameList.subtract(it.user.groups.keys)
                addGroupNames.forEach { groupName ->
                    groupNameList.add(groupName)
                    tabGroups.addTab(tabGroups.newTab().setText(groupName))
                }
                removeGroupNames.forEach { groupName ->
                    tabGroups.removeTabAt(groupNameList.indexOf(groupName))
                    groupNameList.remove(groupName)
                }
            }
        }
    }
}
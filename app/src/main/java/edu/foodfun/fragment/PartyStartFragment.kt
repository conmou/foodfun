package edu.foodfun.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.foodfun.R
import edu.foodfun.adapter.MessageRecyclerViewAdapter
import edu.foodfun.adapter.PartyPrepareMemberRecyclerViewAdapter
import edu.foodfun.adapter.PartyStartMemberRecyclerViewAdapter
import edu.foodfun.dialog.UserDetailDialog
import edu.foodfun.enums.FieldChangeType
import edu.foodfun.enums.UserTemplateType
import edu.foodfun.uistate.MessageUIState
import edu.foodfun.uistate.UserUIState
import edu.foodfun.viewmodel.PartyViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class PartyStartFragment: Fragment() {
    private lateinit var recyclerUserList: RecyclerView
    private lateinit var recyclerMessageList: RecyclerView
    private lateinit var btnShowAllUsers: ImageButton
    private lateinit var btnShowRestaurant: ImageButton
    private lateinit var btnStartEating : Button
    private lateinit var btnBack: FloatingActionButton
    private lateinit var btnSend: FloatingActionButton
    private lateinit var txtMessage: EditText
    private lateinit var googleMap: GoogleMap
    private lateinit var mapView: MapView
    private val vm: PartyViewModel by activityViewModels()
    private val userList: MutableList<UserUIState> = mutableListOf()
    private val messageList: MutableList<MessageUIState> = mutableListOf()
    private val markerMap: MutableMap<String, Marker> = mutableMapOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_party_start, container, false)
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSend = view.findViewById(R.id.btnPartyStartSend)
        btnBack = view.findViewById(R.id.btnPartyRoomBack)
        btnStartEating = view.findViewById(R.id.btnPartyStartEating)
        txtMessage = view.findViewById(R.id.txtPartyStartMessage)
        btnShowAllUsers = view.findViewById(R.id.btnShowMembersLocation)
        btnShowRestaurant = view.findViewById(R.id.btnShowRestaurantLocation)
        recyclerUserList = view.findViewById(R.id.recyclerPartyStartMember)
        recyclerMessageList = view.findViewById(R.id.recyclerPartyStartMessageList)
        mapView = view.findViewById(R.id.PartyStartMapView)
        mapView.onCreate(savedInstanceState)

        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        btnSend.setOnClickListener {
            if(txtMessage.text.isEmpty()) return@setOnClickListener
            lifecycleScope.launch { vm.sendMessage(txtMessage.text.toString()) }
            txtMessage.text = null
        }
        if(vm.currentPartyUIState.value!!.party!!.owner != vm.currentUserUIState.value!!.user!!.id) btnStartEating.isVisible = false
        btnStartEating.setOnClickListener {
            val transaction = activity?.supportFragmentManager?.beginTransaction()
            transaction?.replace(R.id.party_container, PartyEndFragment())?.commit()
        }

        btnShowAllUsers.setOnClickListener {
            val bounds = vm.getAllUserBounds(userList)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
        }

        btnShowRestaurant.setOnClickListener {
            val bounds = vm.getRestaurantBounds()
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
        }

        recyclerUserList.layoutManager = LinearLayoutManager(context)
        recyclerUserList.adapter = PartyStartMemberRecyclerViewAdapter(userList, object : PartyStartMemberRecyclerViewAdapter.ItemObserver() {
            override fun onItemClick(index: Int) {
                vm.currentFocusUser.value = userList[index]
                moveCameraToUser(userList[index])
            }
        })

        recyclerMessageList.layoutManager = LinearLayoutManager(context)
        recyclerMessageList.adapter = MessageRecyclerViewAdapter(messageList, vm.currentUserUIState.value!!.user!!.id!!, object : MessageRecyclerViewAdapter.ItemObserver {
            override fun onClick(index: Int) {
                if (vm.currentUserUIState.value!!.user!!.friends.contains(messageList[index].userUIState!!.user!!.id))
                    UserDetailDialog(messageList[index].userUIState!!.user!!.id!!, UserTemplateType.FRIEND).show(childFragmentManager, "UserDetailDialog")
                else
                    UserDetailDialog(messageList[index].userUIState!!.user!!.id!!, UserTemplateType.STRANGER).show(childFragmentManager, "UserDetailDialog")
            }
        })

        mapView.getMapAsync {
            googleMap = it
            it.isMyLocationEnabled = false
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onStop() {
        super.onStop()
        messageList.clear()
        recyclerMessageList.adapter?.notifyDataSetChanged()
    }

    private fun launchFlow() {
        lifecycleScope.launchWhenCreated {
            vm.userChanges.collect { changes ->
                val userUIState = changes.value
                val target = userList.indexOfFirst { it.user!!.id == userUIState.user!!.id }
                val adapter = recyclerUserList.adapter
                if (changes.changeType == FieldChangeType.ADDED || changes.changeType == FieldChangeType.MODIFIED) {
                    if (target == -1) {
                        userList.add(userUIState)
                        adapter?.notifyItemInserted(userList.size)
                    }
                    else {
                        userList[target] = userUIState
                        adapter?.notifyItemChanged(target)
                    }
                    //將未載入userUIState的訊息添加userUIState
                    messageList.filter {
                        it.message!!.sender == userUIState.user!!.id && it.message.sender != vm.currentUserUIState.value!!.user!!.id!!
                                && (it.userUIState?.avatar == null || it.userUIState?.user == null)
                    }.forEach {
                        it.userUIState = userUIState
                        recyclerMessageList.adapter?.notifyItemChanged(messageList.indexOf(it))
                    }
                    updateUserMarker(changes.value)
                    if (vm.currentFocusUser.value!!.user!!.id!! == changes.value.user!!.id!!) moveCameraToUser(changes.value)
                }
                else if (changes.changeType == FieldChangeType.REMOVED) {
                    if (target == -1) return@collect
                    userList.removeAt(target)
                    adapter?.notifyItemRemoved(target)
                }
            }
        }
        lifecycleScope.launchWhenCreated {
            vm.messageChanges.collect { change ->
                val message = change.value
                val adapter = recyclerMessageList.adapter
                if (change.changeType == FieldChangeType.ADDED) {
                    val userUIState = userList.firstOrNull { it.user!!.id == message.sender && it.user.id != vm.currentUserUIState.value!!.user!!.id!! }
                    messageList.add(MessageUIState(message, userUIState))
                    adapter?.notifyItemInserted(messageList.size)
                }
                recyclerMessageList.scrollToPosition(messageList.size - 1)
            }
        }
        lifecycleScope.launchWhenCreated {
            vm.currentPartyUIState.filterNotNull().collect {
                val location = it.restaurant!!.location
                val markerIcon = vm.generateRestaurantMarkerIcon(it)
                val marker = googleMap.addMarker(MarkerOptions().position(LatLng(location!!.latitude, location.longitude)).title(it.restaurant.name).icon(markerIcon))!!
                markerMap[it.restaurant.id]?.remove()
                markerMap[it.restaurant.id!!] = marker
            }
        }
    }

    private fun updateUserMarker(userUIState: UserUIState) {
        val location = userUIState.user!!.location
        val markerIcon = vm.generateMarkerIcon(userUIState)
        val marker = googleMap.addMarker(MarkerOptions().position(LatLng(location!!.latitude, location.longitude)).title(userUIState.user.nickName).icon(markerIcon))!!
        markerMap[userUIState.user.id!!]?.remove()
        markerMap[userUIState.user.id] = marker
    }

    private fun moveCameraToUser(userUIState: UserUIState) {
        val location = userUIState.user!!.location
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location!!.latitude, location.longitude), 14f))
    }
}
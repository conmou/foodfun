package edu.foodfun.dialog

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import edu.foodfun.R
import edu.foodfun.adapter.SearchResultRecyclerViewAdapter
import edu.foodfun.uistate.RestaurantUIState
import edu.foodfun.viewmodel.MainViewModel
import kotlinx.coroutines.Job
import java.text.SimpleDateFormat
import java.util.*

class PartyCreateDialog(private val partyCreateCallBackListener: PartyCreateCallBackListener): DialogFragment() {
    private lateinit var txtTitle: EditText
    private lateinit var txtxContent: EditText
    private lateinit var txtRestaurant: EditText
    private lateinit var recyclerViewSearchList: RecyclerView
    private lateinit var spinnerMaxMember: Spinner
    private lateinit var labStartTime: TextView
    private lateinit var txtPassword: EditText
    private lateinit var btnConfirm: Button
    private lateinit var btnCancel: Button
    private lateinit var btnEncrypt: Button
    private val vm: MainViewModel by activityViewModels()
    private val maxMemberList = arrayListOf(4, 6, 8, 10)
    private var loaddingJob: Job? = null
    private var selectedRestaurantId: String = ""
    private var resultList: MutableList<RestaurantUIState> = mutableListOf()
    private var isInSearching = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_party_create, container, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtTitle = view.findViewById(R.id.txtPartyCreateName)
        txtxContent = view.findViewById(R.id.txtPartyCreateDescription)
        spinnerMaxMember = view.findViewById(R.id.spinnerPartyCreateMaxMember)
        txtRestaurant = view.findViewById(R.id.txtPartyCreateRestaurant)
        recyclerViewSearchList = view.findViewById(R.id.recyclerViewSearchResault)
        labStartTime = view.findViewById(R.id.txtPartyCreateStartTime)
        txtPassword = view.findViewById(R.id.txtPartyCreatePassword)
        btnConfirm = view.findViewById(R.id.btnConfirmCreate)
        btnCancel = view.findViewById(R.id.btnCancelCreate)
        btnEncrypt = view.findViewById(R.id.checkPartyCreateEncrypt)

        recyclerViewSearchList.adapter = SearchResultRecyclerViewAdapter(resultList, object: SearchResultRecyclerViewAdapter.ItemClickListener {
            override fun itemClick(position: Int) {
                isInSearching = true
                txtRestaurant.setText(resultList[position].restaurant.name)
                selectedRestaurantId = resultList[position].restaurant.id!!
                recyclerViewSearchList.isVisible = false
                isInSearching = false
            }
        })
        recyclerViewSearchList.layoutManager = LinearLayoutManager(context)

        txtRestaurant.addTextChangedListener { it ->
            if(isInSearching) return@addTextChangedListener
            val txtSearch = it.toString()
            loaddingJob?.cancel()
            selectedRestaurantId = ""
            resultList.clear()
            recyclerViewSearchList.adapter?.notifyDataSetChanged()
            if(txtSearch.isEmpty()) recyclerViewSearchList.isVisible = false
            else {
                recyclerViewSearchList.isVisible = true
                loaddingJob = lifecycleScope.launchWhenCreated {
                    vm.fetchSearchRestaurant(txtSearch).forEach {
                        val image = vm.tryFetchImage(it.id!!)
                        resultList.add(RestaurantUIState(it, image))
                        recyclerViewSearchList.adapter?.notifyItemInserted(resultList.size)
                    }
                }
            }
        }

        labStartTime.setOnClickListener { setPartyStartTime() }
        btnEncrypt.setOnClickListener { txtPassword.isVisible = !txtPassword.isVisible }
        btnConfirm.setOnClickListener {
            if (!checkFields()) return@setOnClickListener
            val title = txtTitle.text.toString()
            val content = txtxContent.text.toString()
            val maxMember = spinnerMaxMember.selectedItem.toString().toInt()
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault(Locale.Category.FORMAT))
            formatter.timeZone = TimeZone.getTimeZone("asia/taipei")
            val timeDifference = 28800
            val startTime = formatter.parse(labStartTime.text.toString())
            val password = txtPassword.text.toString()
            partyCreateCallBackListener.onConfirm(title, content, maxMember, selectedRestaurantId, Timestamp(startTime!!.time / 1000 - timeDifference,0), password)
            dismiss()
        }
        btnCancel.setOnClickListener {
            partyCreateCallBackListener.onCancel()
            dismiss()
        }
        spinnerMaxMember.adapter = ArrayAdapter(this.requireContext(), R.layout.support_simple_spinner_dropdown_item, maxMemberList)
    }

    private fun setPartyStartTime() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        DatePickerDialog(requireContext(), { _, year, month, day ->
            TimePickerDialog(context, { _, hour, minute ->
                labStartTime.text = String.format("%d-%02d-%02d %02d:%02d", year, month + 1, day, hour, minute)
            }, currentHour, currentMinute, true).show()
        }, currentYear, currentMonth, currentDay).show()
    }

    private fun checkFields(): Boolean {
        if(txtTitle.length() == 0) {
            txtTitle.error = "請輸入房間名稱"
            return false
        }
        else if(txtxContent.length() == 0) {
            txtxContent.error = "請輸入房間簡介"
            return false
        }
        else if(selectedRestaurantId.isEmpty()) {
            txtRestaurant.error = "請輸入目標餐廳"
            return false
        }
        else if(labStartTime.length() == 0) {
            labStartTime.error = "請選擇開始日期或時間"
            return false
        }
        else if(btnEncrypt.isSelected && txtPassword.length() == 0) {
            txtPassword.error = "請輸入房間密碼"
            return false
        }
        return true
    }

    open class PartyCreateCallBackListener {
        open fun onConfirm(title: String, content: String, maxMember: Int, restaurantId: String, reservation: Timestamp, password: String? = null){}
        open fun onCancel(){}
    }
}
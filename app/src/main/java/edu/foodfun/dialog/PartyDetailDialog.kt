package edu.foodfun.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.makeramen.roundedimageview.RoundedImageView
import dagger.hilt.android.AndroidEntryPoint
import edu.foodfun.R
import edu.foodfun.enums.UserTemplateType
import edu.foodfun.uistate.InviteUIState
import edu.foodfun.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class PartyDetailDialog(private val inviteUIState: InviteUIState): DialogFragment() {
    private lateinit var imgRestaurant: RoundedImageView
    private lateinit var labPartyInviteName: TextView
    private lateinit var labInviteStartTime: TextView
    private lateinit var labPartyInviteBioContent: TextView
    private val vm: MainViewModel by activityViewModels()
    private val timeDifference = 28800
    private val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault(Locale.Category.FORMAT)).apply {
        timeZone = TimeZone.getTimeZone("asia/taipei")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_party_detail, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgRestaurant = view.findViewById(R.id.imgRestaurant)
        labPartyInviteName = view.findViewById(R.id.labPartyInviteName)
        labInviteStartTime = view.findViewById(R.id.labInviteStartTime)
        labPartyInviteBioContent = view.findViewById(R.id.labPartyInviteBioContent)

        imgRestaurant.setImageBitmap(inviteUIState.restaurantImage)
        labPartyInviteName.text = inviteUIState.party!!.title
        labInviteStartTime.text = "開始時間："+formatter.format(Date((inviteUIState.party.reservation!!.seconds + timeDifference) * 1000 )).toString()
        labPartyInviteBioContent.text = inviteUIState.party.content

    }
}
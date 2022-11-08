package edu.foodfun.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.makeramen.roundedimageview.RoundedImageView
import dagger.hilt.android.AndroidEntryPoint
import edu.foodfun.R
import edu.foodfun.enums.UserTemplateType
import edu.foodfun.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class UserDetailDialog(private val userId: String, private val templateType: UserTemplateType): DialogFragment() {
    private lateinit var imgInviteAvatar: RoundedImageView
    private lateinit var labNickName: TextView
    private lateinit var labBirthDay: TextView
    private lateinit var labSex: TextView
    private lateinit var labBio: TextView
    private lateinit var img1stAchievement: ImageView
    private lateinit var img2ndAchievement: ImageView
    private lateinit var img3rdAchievement: ImageView
    private lateinit var img4thAchievement: ImageView
    private lateinit var btnFriendInvite: Button
    private lateinit var btnClose: Button
    private val vm: MainViewModel by activityViewModels()
    private val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault(Locale.Category.FORMAT)).apply {
        timeZone = TimeZone.getTimeZone("asia/taipei")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_user_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imgInviteAvatar = view.findViewById(R.id.imgInviteAvatar)
        labNickName = view.findViewById(R.id.labInviteName)
        labBirthDay = view.findViewById(R.id.labInviteBirthDay)
        labSex = view.findViewById(R.id.labInviteSex)
        labBio = view.findViewById(R.id.labInviteBioContent)
        img1stAchievement = view.findViewById(R.id.img1stAchievement)
        img2ndAchievement = view.findViewById(R.id.img2ndAchievement)
        img3rdAchievement = view.findViewById(R.id.img3rdAchievement)
        img4thAchievement = view.findViewById(R.id.img4thAchievement)
        btnFriendInvite = view.findViewById(R.id.btnDialogUserDetailInvite)
        btnClose = view.findViewById(R.id.btnDialogUserDetailClose)

        if(templateType == UserTemplateType.FRIEND) btnFriendInvite.isVisible = false
        btnFriendInvite.setOnClickListener {
            lifecycleScope.launch { vm.sendInvite(userId) }
            Toast.makeText(context, "發送請求成功!", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        btnClose.setOnClickListener { dismiss() }

        launchFlow()
    }

    private fun launchFlow() {
        lifecycleScope.launchWhenCreated {
            vm.fetchUserUIState(userId).let {
                imgInviteAvatar.setImageBitmap(it.avatar)
                labNickName.text = it.user!!.nickName
                labSex.text = it.user.sex
                labBio.text = it.user.bio
                labBirthDay.text = formatter.format(Date((it.user.birthday!!.seconds + 28800) * 1000 )).toString()
            }
        }
    }
}
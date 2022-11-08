package edu.foodfun.fragment

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.makeramen.roundedimageview.RoundedImageView
import dagger.hilt.android.AndroidEntryPoint
import edu.foodfun.R
import edu.foodfun.activity.*
import edu.foodfun.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class DetailFragment : Fragment() {
    private lateinit var btnLog: Button
    private lateinit var imageAvatar: RoundedImageView
    private lateinit var labNickName: TextView
    private lateinit var labUserCommentCount: TextView
    private lateinit var labStatus: TextView
    private lateinit var editUser: FloatingActionButton
    private lateinit var btnSetting: FloatingActionButton
    private lateinit var frameVisitedRest: ConstraintLayout
    private lateinit var frameComment : ConstraintLayout
    private lateinit var frameAchievement : ConstraintLayout
    private lateinit var loginResult: ActivityResultLauncher<Intent>
    private val vm: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loginResult = registerForActivityResult(FirebaseAuthUIActivityResultContract()) { res -> this.onSignInResult(res) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnLog = view.findViewById(R.id.btnLog)
        imageAvatar = view.findViewById(R.id.imageAvatar)
        labNickName = view.findViewById(R.id.labUserName)
        labStatus = view.findViewById(R.id.labStatus)
        editUser = view.findViewById(R.id.editUser)
        btnSetting = view.findViewById(R.id.btnSetting)
        frameVisitedRest = view.findViewById(R.id.frameVisitedRest)
        frameComment = view.findViewById(R.id.frameComment)
        labUserCommentCount = view.findViewById(R.id.labUserCommentCount)
        frameAchievement = view.findViewById(R.id.frameAchievement)

        btnLog.setOnClickListener {
            if (btnLog.text == "登出") vm.logout()
            else if (btnLog.text == "登入") vm.login(loginResult)
        }
        frameComment.setOnClickListener { startActivity(Intent(activity, CommentActivity::class.java)) }
        frameAchievement.setOnClickListener { startActivity(Intent(activity, AchievementActivity::class.java)) }
        btnSetting.setOnClickListener { startActivity(Intent(activity, SettingActivity::class.java)) }
        frameVisitedRest.setOnClickListener { startActivity(Intent(activity, VisitedActivity::class.java)) }
        editUser.setOnClickListener { startActivity(Intent(activity, DetailEditActivity::class.java)) }

        launchFlow()
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode != RESULT_OK) Toast.makeText(activity, "sign in error with : ${result.idpResponse!!.error}.", Toast.LENGTH_SHORT).show()
        else Toast.makeText(activity, "登入成功.", Toast.LENGTH_SHORT).show()
    }

    private fun launchFlow() {
        lifecycleScope.launchWhenCreated {
            vm.currentUserUIState.collectLatest {
                if (it?.user == null) {
                    btnLog.text = "登入"
                    labStatus.text = "訪客"
                    labNickName.text = "訪客"
                    imageAvatar.setImageResource(R.drawable.fui_ic_anonymous_white_24dp)
                }
                else {
                    btnLog.text = "登出"
                    labStatus.text = "會員"
                    labNickName.text = it.user.nickName
                    imageAvatar.setImageBitmap(it.avatar)
                }
            }
        }
    }
}
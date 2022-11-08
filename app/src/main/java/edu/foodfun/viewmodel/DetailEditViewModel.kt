package edu.foodfun.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.foodfun.hilt.MyApplication
import edu.foodfun.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class DetailEditViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
    val app = MyApplication.getInstance()
    val currentUserUIState = app.currentUserUIStateStateFlow

    suspend fun updateDetail(avatar: Bitmap?, nickName: String?, birthday: Timestamp?, bio: String?, sex: String?) = withContext(Dispatchers.IO) {
        val userId = currentUserUIState.value!!.user!!.id!!
        if (avatar != null) {
            val avatarVersion = Timestamp.now().seconds
            userRepository.updateAvatar(userId, transBitmapToBytes(avatar), avatarVersion)
            userRepository.updateAvatarVersion(userId, avatarVersion)
        }
        if (nickName != null) userRepository.updateNickName(userId, nickName)
        if (birthday != null) userRepository.updateBirthday(userId, birthday)
        if (bio != null) userRepository.updateBio(userId, bio)
        if (sex != null) userRepository.updateSex(userId, sex)
    }

    private fun transBitmapToBytes(bitmap: Bitmap): ByteArray {
        val imageBytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageBytes)
        return imageBytes.toByteArray()
    }
}
package edu.foodfun.activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Timestamp
import com.makeramen.roundedimageview.RoundedImageView
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import edu.foodfun.R
import edu.foodfun.viewmodel.DetailEditViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class DetailEditActivity : AppCompatActivity() {
    private lateinit var imageAvatar: RoundedImageView
    private lateinit var txtNickName : EditText
    private lateinit var txtBirthday : TextView
    private lateinit var txtBio : EditText
    private lateinit var btnSubmit : Button
    private lateinit var male : RadioButton
    private lateinit var female : RadioButton
    private val vm: DetailEditViewModel by viewModels()
    private val calender = Calendar.getInstance()
    private val timeDifference = 28800
    private val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault(Locale.Category.FORMAT)).apply {
        timeZone = TimeZone.getTimeZone("asia/taipei")
    }
    private var currentAvatar: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_edit)

        imageAvatar = findViewById(R.id.imageAvatar)
        txtNickName = findViewById(R.id.txtNickName)
        txtBirthday = findViewById(R.id.txtBirthday)
        txtBio = findViewById(R.id.txtBio)
        btnSubmit = findViewById(R.id.btnDetailEditSubmit)
        male = findViewById(R.id.radioMale)
        female = findViewById(R.id.radioFemale)

        if (vm.currentUserUIState.value == null) finish()


        val pickLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            if(it == null) return@registerForActivityResult
            Picasso.get().load(it).into(imageAvatar)
            lifecycleScope.launch(Dispatchers.IO) { currentAvatar = Picasso.get().load(it).get() }
        }

        imageAvatar.setOnClickListener { pickLauncher.launch("image/*") }
        txtBirthday.setOnClickListener{ setBirthday() }
        btnSubmit.setOnClickListener {
            lifecycleScope.launch {
                val birthday =
                    if (txtBirthday.text.isNotEmpty()) Timestamp(formatter.parse(txtBirthday.text.toString())!!.time / 1000 - timeDifference,0)
                    else null
                val sex =
                    if (male.isChecked) "男"
                    else if (female.isChecked) "女"
                    else ""
                vm.updateDetail(currentAvatar, txtNickName.text.toString(), birthday, txtBio.text.toString(), sex)
                Toast.makeText(this@DetailEditActivity, "update successfully!", Toast.LENGTH_LONG).show()
                finish()
            }
        }

        launchFlow()
    }


    @SuppressLint("SetTextI18n")
    private fun setBirthday() {
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            txtBirthday.text = "$year/${month+1}/$day"
        }
        DatePickerDialog(this, dateListener, calender.get(Calendar.YEAR), calender.get(Calendar.MONTH), calender.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun launchFlow() {
        lifecycleScope.launchWhenCreated {
            vm.currentUserUIState.filterNotNull().collectLatest {
                //set avatar
                if (it.avatar == null)
                    imageAvatar.setImageResource(R.drawable.avatar)
                else
                    imageAvatar.setImageBitmap(it.avatar)
                //set sex
                if (it.user!!.sex == "男")
                    male.isChecked = true
                else if (it.user.sex == "女")
                    female.isChecked = true
                //set birthday
                if (it.user.birthday != null)
                    txtBirthday.text = formatter.format(Date((it.user.birthday.seconds + timeDifference) * 1000 )).toString()
                //set others
                txtNickName.setText(it.user.nickName)
                txtBio.setText(it.user.bio)
            }
        }
    }

}
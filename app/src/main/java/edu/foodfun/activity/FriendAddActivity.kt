package edu.foodfun.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import dagger.hilt.android.AndroidEntryPoint
import edu.foodfun.R
import edu.foodfun.fragment.ScanQRcodeFragment
import edu.foodfun.fragment.ShowQRcodeFragment

@AndroidEntryPoint
class FriendAddActivity : AppCompatActivity() {
    lateinit var btnScanner: AppCompatButton
    lateinit var btnShowQRcode: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_add)
        btnScanner = findViewById(R.id.btnScanner)
        btnShowQRcode = findViewById(R.id.btnShowQRcode)

        //預設掃QRCODE
        supportFragmentManager.beginTransaction().replace(R.id.QRcodeContainer, ScanQRcodeFragment()).commit()

        btnScanner.setOnClickListener { supportFragmentManager.beginTransaction().replace(R.id.QRcodeContainer, ScanQRcodeFragment()).commit() }
        btnShowQRcode.setOnClickListener { supportFragmentManager.beginTransaction().replace(R.id.QRcodeContainer, ShowQRcodeFragment()).commit() }
    }
}
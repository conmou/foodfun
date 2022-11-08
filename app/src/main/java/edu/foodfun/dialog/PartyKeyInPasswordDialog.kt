package edu.foodfun.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import edu.foodfun.R

class PartyKeyInPasswordDialog (context: Context, private val callBackListener: CallBackListener) : Dialog(context) {
    private lateinit var btnConfirm : Button
    private lateinit var btnCancel : Button
    private lateinit var password : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_party_key_in_password)

        btnConfirm = findViewById(R.id.btnPartyKeyInPasswordConfirm)
        btnCancel = findViewById(R.id.btnPartyKeyInPasswordCancel)
        password = findViewById(R.id.txtKeyInPartyPassword)

        btnConfirm.setOnClickListener {
            callBackListener.onConfirm(password.text.toString())
            dismiss()
        }
        btnCancel.setOnClickListener {
            callBackListener.onCancel()
            dismiss()
        }

    }

    open class CallBackListener {
        open fun onConfirm(password: String){}
        open fun onCancel(){}
    }
}
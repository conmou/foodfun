package edu.foodfun.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import edu.foodfun.R

class PartyLeaveDialog (context: Context, private val callBackListener: CallBackListener) : Dialog(context) {
    private lateinit var btnConfirm : Button
    private lateinit var btnCancel : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_party_leave)

        btnConfirm = findViewById(R.id.btnPartyLeaveConfirm)
        btnCancel = findViewById(R.id.btnPartyLeaveCancel)

        btnConfirm.setOnClickListener {
            callBackListener.onConfirm()
            dismiss()
        }
        btnCancel.setOnClickListener {
            callBackListener.onCancel()
            dismiss()
        }
    }

    open class CallBackListener {
        open fun onConfirm(){}
        open fun onCancel(){}
    }
}

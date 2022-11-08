package edu.foodfun.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import edu.foodfun.R

class SendCheckCodeFragment : Fragment() {
    lateinit var txtPhoneNumber: EditText
    lateinit var btnSendCheckCode: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_check_phone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtPhoneNumber = view.findViewById(R.id.txtPhoneNumber)
        btnSendCheckCode = view.findViewById(R.id.btnSendCheckCode)

        btnSendCheckCode.setOnClickListener {
        }
    }
}
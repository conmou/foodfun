package edu.foodfun.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import edu.foodfun.R
import edu.foodfun.viewmodel.PartyViewModel
import kotlinx.coroutines.launch

class PartyEndFragment: Fragment() {
    private lateinit var btnEnd: Button
    private val vm: PartyViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_party_end, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnEnd = view.findViewById(R.id.btnPartyEnd)
        btnEnd.setOnClickListener { lifecycleScope.launch { vm.endParty() } }
//        btnEnd.setOnClickListener {
//            activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.party_container, PartyFeedbackFragment())?.commit()
//            vm.updatePartyState("已結束")
//        }

    }
}
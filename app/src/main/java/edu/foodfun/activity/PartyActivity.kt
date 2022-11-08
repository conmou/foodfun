package edu.foodfun.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import edu.foodfun.R
import edu.foodfun.fragment.PartyFeedbackFragment
import edu.foodfun.fragment.PartyPrepareFragment
import edu.foodfun.fragment.PartyStartFragment
import edu.foodfun.viewmodel.PartyViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull

@AndroidEntryPoint
class PartyActivity : AppCompatActivity() {
    private val vm: PartyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party)

        lifecycleScope.launchWhenCreated {
            vm.currentPartyUIState.filterNotNull().collectLatest {
                if (it.party!!.state == "準備中")
                    supportFragmentManager.beginTransaction().replace(R.id.party_container, PartyPrepareFragment()).commit()
                else if (it.party.state == "進行中")
                    supportFragmentManager.beginTransaction().replace(R.id.party_container, PartyStartFragment()).commit()
                else if (it.party.state == "已結束" && it.party.users.contains(vm.currentUserUIState.value!!.user!!.id!!))
                    supportFragmentManager.beginTransaction().replace(R.id.party_container, PartyFeedbackFragment()).commit()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        lifecycleScope.launchWhenCreated {
            vm.currentPartyUIState.filterNotNull().collectLatest {
                if (it.party!!.state == "準備中")
                    supportFragmentManager.beginTransaction().replace(R.id.party_container, PartyPrepareFragment()).commit()
                else if (it.party.state == "進行中")
                    supportFragmentManager.beginTransaction().replace(R.id.party_container, PartyStartFragment()).commit()
                else if (it.party.state == "已結束" && it.party.users.contains(vm.currentUserUIState.value!!.user!!.id!!))
                    supportFragmentManager.beginTransaction().replace(R.id.party_container, PartyFeedbackFragment()).commit()
            }
        }
    }

    override fun onBackPressed() = finish()
}
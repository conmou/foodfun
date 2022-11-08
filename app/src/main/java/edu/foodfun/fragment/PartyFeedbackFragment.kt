package edu.foodfun.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.foodfun.R
import edu.foodfun.adapter.FeedbackRecyclerViewAdapter
import edu.foodfun.enums.AchievementType
import edu.foodfun.uistate.FeedbackUIState
import edu.foodfun.uistate.UserUIState
import edu.foodfun.viewmodel.PartyViewModel
import kotlinx.coroutines.launch

class PartyFeedbackFragment : Fragment() {
    private lateinit var btnFeedbackCommit: Button
    private lateinit var recyclerFeedbackList: RecyclerView
    private lateinit var userList: MutableList<String>
    private val vm: PartyViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_party_feedback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnFeedbackCommit = view.findViewById(R.id.btnQuestionCommit)
        recyclerFeedbackList = view.findViewById(R.id.recycleQuestion)
        btnFeedbackCommit = view.findViewById(R.id.btnQuestionCommit)

        val memberUserUIStateList: MutableList<UserUIState> = mutableListOf()
        val feedbackUIStateMap: MutableMap<String, FeedbackUIState> = mutableMapOf()

        val party = vm.currentPartyUIState.value!!.party!!
        userList = party.users.union(party.feedbacks).filter{ it != vm.currentUserUIState.value!!.user!!.id!! }.toMutableList()
        userList.forEach {
            lifecycleScope.launchWhenCreated {
                val user = vm.fetchUserModel(it)
                val avatar = vm.tryFetchAvatar(it, user.avatarVersion!!)
                memberUserUIStateList.add(UserUIState(user, avatar))
                recyclerFeedbackList.adapter?.notifyItemInserted(memberUserUIStateList.size)
            }
        }

        recyclerFeedbackList.layoutManager = LinearLayoutManager(context)
        recyclerFeedbackList.adapter = FeedbackRecyclerViewAdapter(memberUserUIStateList, object : FeedbackRecyclerViewAdapter.FeedBackListener() {
            override fun onAchievementClick(index: Int, achievementType: AchievementType, isSeleted: Boolean) {
                if (isSeleted) {
                    if (!feedbackUIStateMap.containsKey(userList[index]))
                        feedbackUIStateMap[userList[index]] = FeedbackUIState(achievementList = mutableListOf(achievementType))
                    else
                        feedbackUIStateMap[userList[index]]?.achievementList!!.add(achievementType)
                }
                else {
                    feedbackUIStateMap[userList[index]]?.achievementList?.remove(achievementType)
                }
            }

            override fun onCommentChanged(index: Int, content: String) {
                if (!feedbackUIStateMap.containsKey(userList[index]))
                    feedbackUIStateMap[userList[index]] = FeedbackUIState(comment = content)
                else
                    feedbackUIStateMap[userList[index]]?.comment = content
            }
        })

        btnFeedbackCommit.setOnClickListener {
            feedbackUIStateMap.forEach { (userId, feedbackState) ->
                if (feedbackState.comment?.isEmpty() == true && feedbackState.achievementList?.isEmpty() == true) return@forEach
                lifecycleScope.launch { vm.postFeedback(userId, feedbackState.comment!!, feedbackState.achievementList!!) }
            }
            lifecycleScope.launch { vm.feedbackDone() }
            requireActivity().finish()
//            startActivity(Intent(context, MainActivity::class.java))
        }
    }
}
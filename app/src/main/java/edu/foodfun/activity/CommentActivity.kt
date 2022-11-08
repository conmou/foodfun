package edu.foodfun.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import edu.foodfun.R
import edu.foodfun.adapter.UserCommentRecyclerViewAdapter
import edu.foodfun.model.UserComment
import edu.foodfun.repository.UserRepository
import edu.foodfun.viewmodel.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class CommentActivity : AppCompatActivity() {
    @Inject lateinit var userRepository: UserRepository
    private val vm: MainViewModel by viewModels()
    private lateinit var userCommentRecyclerView: RecyclerView
    private var commentList: List<UserComment> = listOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        val userId = vm.currentUserUIState.value!!.user!!.id!!
        userCommentRecyclerView = findViewById(R.id.recyclerUserCommentList)

//        lifecycleScope.launchWhenCreated {
//            commentList = userRepository.fetchUserComments(userId)
//        }.invokeOnCompletion {
//            userCommentRecyclerView.adapter  = UserCommentRecyclerViewAdapter(commentList)
//            userCommentRecyclerView.layoutManager = LinearLayoutManager(this)
//        }

        setSupportActionBar(findViewById(R.id.toolbarUserComment))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "我的評論"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }

}
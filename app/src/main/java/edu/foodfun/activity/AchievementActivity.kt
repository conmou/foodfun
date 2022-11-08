package edu.foodfun.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import edu.foodfun.R
import edu.foodfun.repository.UserRepository
import javax.inject.Inject

@AndroidEntryPoint
class AchievementActivity : AppCompatActivity() {
    @Inject lateinit var userRepository: UserRepository
    private var achievementMap: HashMap<String, Int>? = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievement)

//        val userId = intent.getStringExtra("currentUserId")
//        lifecycleScope.launchWhenCreated {
//            achievementMap = userRepository.fetchUserAchievement(userId!!)
//        }.invokeOnCompletion {
//            if(achievementMap?.isNotEmpty() == true) {
//                Toast.makeText(this, achievementMap!!.keys.toString(), Toast.LENGTH_SHORT).show()
//            }
//        }

        setSupportActionBar(findViewById(R.id.toolbarAchievement))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "我的勳章"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }

}
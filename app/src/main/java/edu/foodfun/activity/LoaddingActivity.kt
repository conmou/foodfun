package edu.foodfun.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.foodfun.R
import edu.foodfun.hilt.MyApplication
import kotlinx.coroutines.flow.combine

class LoaddingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loadding)

        lifecycleScope.launchWhenCreated {
            val app = MyApplication.getInstance()
            app.isCurrentUserDataInitingStateFlow
                .combine(app.isBundleCofigLoaddingStateFlow) { a1, a2 -> a1 || a2 }
                .combine(app.isRecommendParamsLoaddingStateFlow) { a1, a2 -> a1 || a2 }.collect {
                    if (it) return@collect
                    startActivity(Intent(this@LoaddingActivity, MainActivity::class.java))
                    finish()
                }
        }
    }
}
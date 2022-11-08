package edu.foodfun.activity

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.foodfun.R


class SettingActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.setTitle(R.string.detail_setting_title)


        val list: ListView=findViewById(R.id.setlistview)
        val data = arrayOf("通知設定", "功能介紹", "使用條款", "隱私政策", "語言設定", "系統版本")
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,data)
        list.adapter = adapter

        list.setOnItemClickListener { _, _, i, _ ->
            Toast.makeText(this, list.getItemAtPosition(i).toString(),Toast.LENGTH_LONG).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }
}
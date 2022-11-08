package edu.foodfun

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner

class ItemMainSelect : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_main_selected)

        val spinner:Spinner=findViewById(R.id.spinner)
        val lunch = arrayListOf("價格","100以下","100-250","250-500","500-750","750-1000")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, lunch)
        spinner.adapter = adapter
    }
}
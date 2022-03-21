package com.mic

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mic.local.LocalActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toLoaclPage()
    }


    fun toLoaclPage(){
        val intent = Intent(this,LocalActivity::class.java)
        startActivity(intent)
    }
}
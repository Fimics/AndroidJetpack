package com.mic.aptmain

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mic.annotation.Inject


class MainActivity : AppCompatActivity() {

    val tag = "MainActivity"

    @Inject("zhang shan")
    lateinit var user: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        DaggerApplicationComponent.create().inject(this)
        Log.d(tag, "user:${user}")
    }
}

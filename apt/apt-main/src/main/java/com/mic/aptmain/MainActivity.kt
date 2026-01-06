package com.mic.aptmain

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mic.annotation.Inject


class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var user: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }
}

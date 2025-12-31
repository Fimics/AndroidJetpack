package com.mic.dagger

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mic.dagger.demo.DemoFragment


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.supportFragmentManager.beginTransaction().add(R.id.root, DemoFragment()).commit()
    }
}

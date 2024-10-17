package com.mic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mic.jetpack.dagger2.Dagger2Fragment


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.supportFragmentManager.beginTransaction().add(R.id.root,Dagger2Fragment()).commit()
    }
}

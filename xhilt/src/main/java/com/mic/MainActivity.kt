package com.mic

import android.os.Bundle
import android.os.IInterface
import androidx.appcompat.app.AppCompatActivity
import com.mic.jetpack.hilt.HiltFragment
import com.mic.jetpack.hilt.`object`.HttpObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.supportFragmentManager.beginTransaction().add(R.id.root,HiltFragment()).commit()

    }
}

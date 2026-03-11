package com.mic.jetpack.livedata

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mic.R

class LivedataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_livedata)
        LiveDataFragment.Companion.mLiveData.observe(this){
            KLog2.d("livedata",it)
        }
    }
}
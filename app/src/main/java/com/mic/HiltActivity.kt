package com.mic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.mic.jetpack.hilt.`object`.HttpObject
import com.mic.libcore.utils.KLog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HiltActivity:AppCompatActivity() {

    @Inject
    @JvmField
    var httpObject:HttpObject?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hilt)
//        supportFragmentManager.beginTransaction().add(R.id.root,HiltFragment()).commit()
        this.findViewById<AppCompatButton>(R.id.btn_hiltactivity).setOnClickListener {
            KLog.d("hilt","http code ->${httpObject.hashCode()}")
        }
    }
}
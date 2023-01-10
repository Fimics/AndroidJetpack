package com.mic.ui.event

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mic.R
import com.mic.databinding.ActivityEventBinding

class EventActivity : AppCompatActivity() {

    val list = listOf(
        R.mipmap.iv_0, R.mipmap.iv_1, R.mipmap.iv_2,
        R.mipmap.iv_3, R.mipmap.iv_4, R.mipmap.iv_5,
        R.mipmap.iv_6, R.mipmap.iv_7, R.mipmap.iv_8
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)

        val binding = ActivityEventBinding.bind(findViewById(R.id.root))

        var map: HashMap<String,Int>?=null
        val lists: ArrayList<Map<String, Int>> = ArrayList()
        for (i in 0 until 20) {
            map = HashMap<String,Int>()
            map.put("key", list[i % 9]);
            lists.add(map)
        }

        val pager = binding.viewpager
        val adapter = MyPagerAdapter(this, lists)
        pager.adapter=adapter
    }
}
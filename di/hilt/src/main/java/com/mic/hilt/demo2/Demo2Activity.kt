package com.mic.hilt.demo2

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.ViewModelProvider
import com.mic.hilt.KLog
import com.mic.hilt.R
import com.mic.hilt.demo.hilt.HiltFragment
import com.mic.hilt.demo.http.annoation.BindXUtils
import com.mic.hilt.demo.http.client.HttpCallback
import com.mic.hilt.demo.http.client.IHttpProcessor
import com.mic.hilt.demo.http.result.ResponseData
import com.mic.hilt.javassist.JavassistUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// hilt 可以注入的类
@AndroidEntryPoint
class Demo2Activity : AppCompatActivity() {

    private val TAG = "Demo2Activity"
   @Inject
   lateinit var user: User

    @Inject
    lateinit var student: Student

    @Inject
    lateinit var viewModel: ViewModel

//    @Inject
//    lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        KLog.d(TAG," user -> $user")
        KLog.d(TAG," student -> $student")
        viewModel.test()

        val mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        mainViewModel.test()

        val button = findViewById<AppCompatButton>(R.id.btn_http)
        button.setOnClickListener {
            // 方式1：直接调用
//            basicJavassistTest()


        }

    }


}

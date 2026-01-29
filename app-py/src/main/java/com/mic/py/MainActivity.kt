package com.mic.py

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.mic.apppy.R
import com.mic.libpy.PyBridge
import com.mic.log.runtime.AutoLog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<AppCompatButton>(R.id.btn_test).setOnClickListener {
            val hello = PyBridge.hello("William")
            val sum = PyBridge.add(7, 35)
            val fib = PyBridge.fib(8)

            Log.d("MainActivity", "hello: $hello, sum: $sum, fib: $fib")

            val result = foo(x = 11)
            Log.d("MainActivity", "result: $result")

//            AutoLog.enter("Test", "foo", "()V")
            bar()
        }
    }

    fun foo(x: Int): Int {
        if (x < 0) error("bad")
        return x + 1
    }

    fun bar() {
        // 这里不要写 AutoLog
        Log.d("TEST", "bar called")
    }

}

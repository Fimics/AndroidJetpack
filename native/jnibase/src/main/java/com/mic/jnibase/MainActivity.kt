package com.mic.jnibase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.change_name).setOnClickListener {
            NativeLib.getInstance().changeName()
            findViewById<Button>(R.id.text_name).text = NativeLib.getInstance().name
        }

        findViewById<Button>(R.id.change_age).setOnClickListener {
              NativeLib.changeAge()
              findViewById<Button>(R.id.text_age).text=""+NativeLib.A;
        }

        findViewById<Button>(R.id.add_call).setOnClickListener {
            NativeLib.getInstance().callAddMethod();
        }
    }


}
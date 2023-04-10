package com.mic.jnibase

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.mic.jnibase.NativeLib.*
import org.fmod.FMOD

class MainActivity : AppCompatActivity() {

    private val TAG="jni_base";
    private var path:String?=null;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FMOD.init(this)
        path =  "file:///android_asset/audio.mp3";
        //111111111111111111111111111111111111111
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
        //22222222222222222222222222222
        findViewById<Button>(R.id.array).setOnClickListener {
            val ints = intArrayOf(1, 2, 3, 4, 5, 6) // 基本类型的数组
            val strs = arrayOf("李小龙", "李连杰", "李元霸") // 对象类型的数组
            NativeLib.getInstance().testArrayAction(99, "你好", ints, strs)
            for (anInt in ints) {
                Log.d(TAG, "Java test01: anInt:$anInt")
            }
        }

        findViewById<Button>(R.id.pass_object).setOnClickListener {
            val student = Student() // Java new
            student.name = "史泰龙"
            student.age = 88
            NativeLib.getInstance().putObject(student, "九阳神功")
        }

        findViewById<Button>(R.id.create_java_object).setOnClickListener {
            NativeLib.getInstance().insertObject()
        }

        findViewById<Button>(R.id.create_java_object).setOnClickListener {
            NativeLib.getInstance().testQuote()
        }

        findViewById<Button>(R.id.create_java_object).setOnClickListener {
            NativeLib.getInstance().delQuote()
        }
        //3333333333333333333333333333333333333333333333333333
        findViewById<Button>(R.id.yuansheng).setOnClickListener {
            NativeLib.getInstance().voiceChangeNative(MODE_NORMAL, path) // 真实开发中，必须子线程  JNI线程（很多坑）
        }
        findViewById<Button>(R.id.luoli).setOnClickListener {
            NativeLib.getInstance().voiceChangeNative(MODE_LUOLI, path)
        }
        findViewById<Button>(R.id.dashu).setOnClickListener {
            NativeLib.getInstance().voiceChangeNative(MODE_DASHU, path)
        }
        findViewById<Button>(R.id.jingsong).setOnClickListener {
            NativeLib.getInstance().voiceChangeNative(MODE_JINGSONG, path)
        }

        findViewById<Button>(R.id.gaoguai).setOnClickListener {
            NativeLib.getInstance().voiceChangeNative(MODE_GAOGUAI, path)
        }
        findViewById<Button>(R.id.kongling).setOnClickListener {
            NativeLib.getInstance().voiceChangeNative(MODE_KONGLING, path)
        }
        //44444444444444444444444444444444444444444444444444444444444
    }

    override fun onDestroy() {
        super.onDestroy()
        NativeLib.getInstance().delQuote()
        FMOD.close()
    }

}
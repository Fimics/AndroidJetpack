package com.mic.jnibase

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mic.jnibase.NativeLib.*
import org.fmod.FMOD


class MainActivity : AppCompatActivity(), FmodCallback, UpdateUiCallback {

    private val TAG = "jni_base";
    private var path: String? = null;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FMOD.init(this)
        getInstance().setFmodCallback(this)
        getInstance().setUpdateUiCallback(this)
        path = "file:///android_asset/audio.mp3";
        //111111111111111111111111111111111111111
        findViewById<Button>(R.id.change_name).setOnClickListener {
            NativeLib.getInstance().changeName()
            findViewById<Button>(R.id.text_name).text = NativeLib.getInstance().name
        }

        findViewById<Button>(R.id.change_age).setOnClickListener {
            NativeLib.changeAge()
            findViewById<Button>(R.id.text_age).text = "" + NativeLib.A;
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

        findViewById<Button>(R.id.static_register).setOnClickListener {
            getInstance().dynamicJavaMethod01()
        }
        findViewById<Button>(R.id.dynamic_register).setOnClickListener {
            val result = getInstance().dynamicJavaMethod02("神照功")
            Toast.makeText(this, "result:" + result, Toast.LENGTH_SHORT).show();
        }

        findViewById<Button>(R.id.jni_thread).setOnClickListener {
            getInstance().nativeThread()
        }
        findViewById<Button>(R.id.jvm_env).setOnClickListener {
            getInstance().nativeFun1() // main线程调用的
            getInstance().nativeFun2() // main线程调用的
            staticFun3() // main线程调用的
            // 第四个  new Thread 调用  ThreadClass == clasz 当前函数clazz地址
            // 第四个  new Thread 调用  ThreadClass == clasz 当前函数clazz地址
            object : Thread() {
                override fun run() {
                    super.run()
                    staticFun4() // Java的子线程调用
                }
            }.start()
        }

        //5555555555555555555555555555555555555555555555555555555555555555
        findViewById<Button>(R.id.sort_ndk).setOnClickListener {
            val arr = intArrayOf(11, 22, -3, 2, 4, 6, -15)
            getInstance().sort(arr)
            for (element in arr) {
                Log.e(TAG, "sortAction: " + element.toString() + "\t")
            }
        }
        findViewById<Button>(R.id.load_cache).setOnClickListener {
            NativeLib.localCache("aaaa")
        }

        findViewById<Button>(R.id.init_static_cache).setOnClickListener {
            NativeLib.initStaticCache()
        }

        findViewById<Button>(R.id.static_cache).setOnClickListener {
            NativeLib.staticCache("bbbb")
        }
        findViewById<Button>(R.id.except).setOnClickListener {
            NativeLib.exceptionNative() // C++层自己做了补救措施了
            // 捕获人家C++层抛上来的异常
            // 捕获人家C++层抛上来的异常
            try {
                NativeLib.exception2Native2()
            } catch (exception: NoSuchFieldException) {
                exception.printStackTrace()
                Log.d("Derry", "exceptionAction: 异常被我捕获了")
            }

            NativeLib.exception2Native2()
            val result: String = NativeLib.thisAction()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NativeLib.getInstance().delQuote()
        FMOD.close()
        getInstance().closeThread()
    }

    // 给C++调用的函数 NI 调用 Java函数的时候，忽略掉 私有、公开 等
    //Fmod播放完成回调
    override fun onPlalyerEnd(msg: String?) {
        Toast.makeText(this, "" + msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * TODO 下面是 被native代码调用的 Java方法
     * 第二部分 JNI线程
     */
    override fun onUpdateUi() {
        if (Looper.getMainLooper() == Looper.myLooper()) { // TODO C++ 用主线程调用到此函数 ---->  主线程
            AlertDialog.Builder(this@MainActivity)
                .setTitle("UI")
                .setMessage("updateActivityUI Activity UI ...")
                .setPositiveButton("老夫知道了", null)
                .show()
        } else {  // TODO  C++ 用异步线程调用到此函数 ---->  异步线程
            Log.d(TAG, "updateActivityUI 所属于子线程，只能打印日志了..")
            runOnUiThread { // 可以在子线程里面 操作UI
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("updateActivityUI")
                    .setMessage("所属于子线程，只能打印日志了..")
                    .setPositiveButton("老夫知道了", null)
                    .show()
            }
        }
    }
}
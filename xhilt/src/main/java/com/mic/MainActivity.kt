package com.mic

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mic.jetpack.hilt.HiltFragment
import com.mic.jetpack.http.annoation.BindOkhttp
import com.mic.jetpack.http.annoation.BindXUtils
import com.mic.jetpack.http.client.HttpCallback
import com.mic.jetpack.http.client.IHttpProcessor
import com.mic.jetpack.http.result.ResponseData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @BindXUtils
    @Inject
    @JvmField
    var http:IHttpProcessor?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.supportFragmentManager.beginTransaction().add(R.id.root,HiltFragment()).commit()
        this.findViewById<View>(R.id.btn_http).setOnClickListener {
            val url = "https://v.juhe.cn/historyWeather/citys"
            val params = mapOf<String?, Any?>("province_id" to 2,"key" to "bb52107206585ab074f5e59a8c73875b")
            //https://v.juhe.cn/historyWeather/citys?&province_id=2&key=bb52107206585ab074f5e59a8c73875b
            //https://v.juhe.cn/historyWeather/citys?&province_id=2&key=bb52107206585ab074f5e59a8c73875b
            http?.post(url,params, object : HttpCallback<ResponseData>() {
                override fun onSuccess(objResult: ResponseData) {
                    KLog.d("hilt","result->${objResult.result}")
                }
            })

        }

    }
}

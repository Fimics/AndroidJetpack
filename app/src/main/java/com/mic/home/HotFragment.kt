package com.mic.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mic.databinding.FragmentTabHotBinding
import com.mic.di.User
import com.mic.home.observer.TestObserver
import com.mic.server.client.AndroidServer
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import javax.inject.Inject


@AndroidEntryPoint
class HotFragment : Fragment() {

    val TAG: String = "http"
    //https://blog.csdn.net/u013064109/article/details/78786646  let run with apply


    //如果一个类有两个概念上相同的属性，但一个是公共API的一部分，另一个是实现细节，请使用下划线作为私有属性名称的前缀
    private var _binding :FragmentTabHotBinding?=null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTabHotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val host = AndroidServer.get(context).host
        Log.d(TAG,"host-->"+host)
        lifecycle.addObserver(TestObserver())

        val webView = binding.webview
        binding.btnWebview.setOnClickListener {
            webView.loadUrl(host+"/storage/emulated/0/Documents/json/tabs.json")
        }

        binding.btnOkhttp.setOnClickListener {
            val client = OkHttpClient()

//            GET
//            val request = Request.Builder()
//                .url("http://192.168.2.37:9999/storage/emulated/0/Documents/json/test.json")
//                .build()

            val json = "application/json; charset=gbk".toMediaTypeOrNull()
            var requestBody = RequestBody.create(json,"json")
            val request = Request.Builder()
                .url(host+"/storage/emulated/0/Documents/json/tabs.json")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d(TAG, "onFailure")
                }

                override fun onResponse(call: Call, response: Response) {
                    val url =call.request().url
                    Log.d(TAG, "url ->"+url)
                    Log.d(TAG, "onResponse")
                    val result = response.body?.string()
                    Log.d(TAG, "onResponse data->  " + result)
                    Log.d(TAG, "onResponse code->  " + response.code)
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

}
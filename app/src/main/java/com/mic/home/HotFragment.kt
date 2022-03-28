package com.mic.home

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import androidx.fragment.app.Fragment
import com.mic.R
import com.mic.server.client.AndroidServer
import com.mic.utils.FileTools
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException


class HotFragment : Fragment() {

    val TAG: String = "http"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tab_hot, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btn_server).setOnClickListener {
            var httpServer = AndroidServer(context)
            httpServer.init()
        }

        view.findViewById<Button>(R.id.btn_assets).setOnClickListener {
            val array = resources.assets.list("json")
            array?.forEach { Log.d(TAG, it.toString()) }
            val dir = "json"
            this.context?.let { it -> FileTools.copyDir(dir, it) }
            val fileName = StringBuilder(dir).append("/").append("test.json").toString()
            var data = File(context?.let { it1 -> FileTools.getStorageDir() }, fileName)

            Log.d(TAG, data.absolutePath)
            Log.d(TAG, data.readText())

        }

        val webView = view.findViewById<WebView>(R.id.webview)

        view.findViewById<Button>(R.id.btn_webview).setOnClickListener {
            webView.loadUrl("http://192.168.2.37:9999/data/user/0/com/mic/cache/json/test.json")
        }

        view.findViewById<Button>(R.id.btn_okhttp).setOnClickListener {
            val client = OkHttpClient()
//            val url = "http://192.168.2.37:8080/storage/emulated/0/Documents/json/test.json"
//            var request: Request = Request.Builder().url(url).build()
//            client.newCall(request).enqueue(object :Callback{
//                override fun onFailure(call: Call, e: IOException) {
//                    Log.d(TAG, "onFailure")
//                }
//
//                override fun onResponse(call: Call, response: Response) {
//                    Log.d(TAG, "onResponse")
//                    Log.d(TAG, "onResponse data->  "+response.body.toString())
//                    Log.d(TAG, "onResponse code->  " + response.code)
//                }
//            })
//        }


//             val file = File("/storage/emulated/0/Documents/json/test.json")
//            Log.d(TAG,"file path-->"+file.absolutePath)
//            Log.d(TAG,"file text  ->"+file.readText())


            val request = Request.Builder()
                .url("http://192.168.2.37:9999/storage/emulated/0/Documents/json/test.json")

                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d(TAG, "onFailure")
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.d(TAG, "onResponse")
                    val result = response.body.toString()
                    Log.d(TAG, "onResponse data->  " + result)
                    Log.d(TAG, "onResponse code->  " + response.code)

                }
            })
        }
    }

}
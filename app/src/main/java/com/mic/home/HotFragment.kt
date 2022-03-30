package com.mic.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import androidx.fragment.app.Fragment
import com.mic.R
import com.mic.databinding.FragmentTabHotBinding
import com.mic.server.client.AndroidServer
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
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
        val host = AndroidServer.get(context).host
        Log.d(TAG,"host-->"+host)

        val webView = view.findViewById<WebView>(R.id.webview)
        view.findViewById<Button>(R.id.btn_webview).setOnClickListener {
            webView.loadUrl(host+"/storage/emulated/0/Documents/json/tabs.json")
        }

        view.findViewById<Button>(R.id.btn_okhttp).setOnClickListener {
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

}
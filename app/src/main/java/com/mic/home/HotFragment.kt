package com.mic.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mic.databinding.FragmentTabHotBinding
import com.mic.ex.asAutoDisposable
import com.mic.home.observer.TestObserver
import com.mic.server.client.AndroidServer
import com.mic.utils.KLog
//import com.mic.utils.isConnected
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import kotlin.concurrent.thread


@AndroidEntryPoint
class HotFragment : Fragment() {

    val TAG: String = "http"
    //https://blog.csdn.net/u013064109/article/details/78786646  let run with apply


    //如果一个类有两个概念上相同的属性，但一个是公共API的一部分，另一个是实现细节，请使用下划线作为私有属性名称的前缀
    private var _binding: FragmentTabHotBinding? = null
    private val binding get() = _binding!!

    //尽管我们有了作用域就可以实现协程与UI的关联，不过在每个Activity或者Fragment中手动创建一个MainScope似乎并不是什么好办法。
    //使用AutoDispose
    private val mainScope by lazy { MainScope() }

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
        Log.d(TAG, "host-->" + host)
        lifecycle.addObserver(TestObserver())

        val webView = binding.webview
        binding.btnWebview.setOnClickListener {
            webView.loadUrl(host + "/storage/emulated/0/Documents/json/tabs.json")
        }

        binding.btnOkhttp.setOnClickListener {

            //ext activity
            val client = OkHttpClient()
//            activity?.start<MainActivity>()
//            snackbar("我是HotFragment")
//             println(requireActivity().isConnected())


//            GET

            thread {
                val requestGet= Request.Builder()
                    .url(host+"/storage/emulated/0/Documents/json/test.json")
                    .build()
                KLog.d(tag,"execute start")
                val response =client.newCall(requestGet).execute();
                KLog.d(tag,response.body?.string())
                KLog.d(tag,"execute end")
            }


             //POST
//            val json = "application/json; charset=gbk".toMediaTypeOrNull()
//            var requestBody = RequestBody.create(json, "json")
//            val requestPost = Request.Builder()
//                .url(host + "/storage/emulated/0/Documents/json/tabs.json")
//                .post(requestBody)
//                .build()
//
//            client.newCall(requestPost).enqueue(object : Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    Log.d(TAG, "onFailure")
//                }
//
//                override fun onResponse(call: Call, response: Response) {
//                    val url = call.request().url
//                    Log.d(TAG, "url ->" + url)
//                    Log.d(TAG, "onResponse")
//                    val result = response.body?.string()
//                    Log.d(TAG, "onResponse data->  " + result)
//                    Log.d(TAG, "onResponse code->  " + response.code)
//                }
////            })
        }

        binding.btnMainScope.setOnClickListener {
            //1.mainScope
             mainScope.launch {
                 KLog.d("调度到UI线程")
             }

            //2.autoDisposable
            GlobalScope.launch(Dispatchers.Main) {
                //调到UI线程上
            }.asAutoDisposable(it)


            //3.
            lifecycleScope.launch {
                //执行携程
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onDestroy() {
        super.onDestroy()
        //调用完销毁
        //我们注意到，作用域的好处就是可以方便地绑定到UI组件的生命周期上，在Activity销毁的时候直接取消，所有该作用域启动的协程就会被取消。
        mainScope.cancel()
    }
}
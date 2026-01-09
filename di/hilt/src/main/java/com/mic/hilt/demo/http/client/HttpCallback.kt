package com.mic.hilt.demo.http.client

import com.google.gson.Gson
import java.lang.reflect.ParameterizedType

/**
 * 回调接口的一种实现  new HttpCallback<javaBean>
</javaBean> */
abstract class HttpCallback<Result> : ICallback {
    override fun onSuccess(result: String?) {
        //result就是网络访问第三方框架返回的字符串
        //1.得到调用者用什么样的javaBean接收数据
        val clz = analysisClassInfo(this)
        //2.把String result转成对应的javaBean
        val gson = Gson()
        val objResult = gson.fromJson(result, clz) as Result
        //3.objResult交给程序员
        onSuccess(objResult)
    }

    abstract fun onSuccess(objResult: Result)
    private fun analysisClassInfo(`object`: Any): Class<*> {
        //getGenericSuperclass();
        //可以得到包含原始类型，参数化类型，数组，类型变量，基本数据类型
        val getType = `object`.javaClass.genericSuperclass
        val params = (getType as ParameterizedType).actualTypeArguments
        return params[0] as Class<*>
    }

    override fun onFailure(e: String?) {}
}
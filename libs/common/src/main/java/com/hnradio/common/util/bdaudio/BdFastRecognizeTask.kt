package com.hnradio.common.util.bdaudio

import android.os.Build
import android.os.SystemClock
import android.util.Base64
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.hnradio.common.BuildConfig
import com.hnradio.common.ktx.int
import com.hnradio.common.ktx.string
import com.hnradio.common.util.L
import com.hnradio.common.util.MMKVUtils
import com.hnradio.common.util.TimeUtils
import com.orhanobut.hawk.Hawk
import com.yingding.lib_net.easy.OkHttpManager
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * @author ytf
 * Created by on 2021/11/03 16:56
 */
class BdFastRecognizeTask {


    private val httpManager by lazy { OkHttpManager.getInstance() }

    interface Callback{
        fun onError(key : Int, msg : String)
        fun onSuccess(key : Int, ret : Pair<Int, String>)
    }

    private var mCallback : Callback? = null
    private var currentPcmPath = ""
    private var currentKey = 0
    private var userToken = ""
    private var disposable : Disposable? = null

    fun recorgnize(userToken : String, key : Int, filePath : String, callback : Callback){
        this.userToken = userToken
        currentKey = key
        currentPcmPath = filePath
        mCallback = callback
        disposable = getAccessToken()
            .flatMap { fastQuery(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                mCallback?.onSuccess(currentKey, Pair(0, it.get(0).asString))
            }, {
                mCallback?.onError(currentKey, it.message?:"")
            })
    }

    private fun getAccessToken() : Observable<String> {
        return Observable.create(object : ObservableOnSubscribe<String> {
            override fun subscribe(emitter: ObservableEmitter<String>) {

                val bean = Hawk.get<BdAccessTokenBean>("bd_access_token")
                var needRefresh = false
                var token = ""

                if(bean != null){

                    if(MMKVUtils.decodeString("bd_access_token")?.isEmpty() == true){
                        MMKVUtils.encode("bd_access_token", bean.access_token)
                    }

                    val start = MMKVUtils.decodeString("bd_access_time")
                    val now = TimeUtils.getNowString(TimeUtils.FORMAT_YYYY_MM_DD)
                    if(now == start){
                        token = bean.access_token
                    }else{
                        needRefresh = true
                    }
                }else{
                    needRefresh = true
                }
                if(needRefresh){
                    httpManager.post("https://aip.baidubce.com/oauth/2.0/token")
                        .queryParam("grant_type", "client_credentials")
                        .queryParam("client_id", BuildConfig.BAIDU_API_KEY)
                        .queryParam("client_secret", BuildConfig.BAIDU_SECRET_KEY)
                        .withMainThread(false)
                        .callback(object : BdResultCallback<BdAccessTokenBean>(){

                            override fun isSuccess(rawData: JSONObject?): Boolean {
                                return rawData?.has("access_token")?:false
                            }

                            override fun getServerFailedMessage(obj: JSONObject?): String {
                                return obj?.string("error_description")?:""
                            }

                            override fun onNetErr(code: Int, msg: String?) {
                                super.onNetErr(code, msg)
                                emitter.onError(IllegalArgumentException(msg))
                            }

                            override fun onError(simpleMsg: String?, code: Int, e: Exception?) {
                                emitter.onError(IllegalArgumentException(simpleMsg))
                            }

                            override fun onSuccess(response: BdAccessTokenBean?) {
                                L.e("获取到response==>${response}")
                                Hawk.put("bd_access_token", response)
                                MMKVUtils.encode("bd_access_token", response?.access_token?:"")
                                MMKVUtils.encode("bd_access_time", TimeUtils.getNowString(TimeUtils.FORMAT_YYYY_MM_DD))
                                emitter.onNext(response?.access_token?:"")
                                emitter.onComplete()
                            }
                        }).enqueue()
                }else{
                    emitter.onNext(token)
                    emitter.onComplete()
                }
            }
        })
    }


    private fun readPcmFileBytesAndConvert(path : String) : Pair<Int, String>{
        val file = File(path)
        if(!file.exists() || !file.canRead())
            throw RuntimeException("file not exist or can't read")

        val bos = ByteArrayOutputStream()
        var fis : FileInputStream? = null
        try {
            val buf = ByteArray(1024)
            fis = FileInputStream(file)
            var read = 0
            while (fis.read(buf).also { read = it } > 0){
                bos.write(buf, 0, read)
            }
            val data = bos.toByteArray()
            val retStr = com.hnradio.common.util.Base64.encode(data, "US-ASCII")
            return Pair(data.size, retStr)
        }catch (e : java.lang.Exception){
            return Pair(0, "")
        }finally {
            fis?.close()
        }
    }

    private fun fastQuery(token : String) : Observable<JsonArray> {
        return Observable.create(object : ObservableOnSubscribe<JsonArray> {
            override fun subscribe(emitter: ObservableEmitter<JsonArray>) {

                val speech = readPcmFileBytesAndConvert(currentPcmPath)

                httpManager.post("https://vop.baidu.com/pro_api")
                    .postJson(JsonObject().also {
                        it.addProperty("format", "pcm")
                        it.addProperty("rate", 16000)
                        it.addProperty("channel", 1)
                        it.addProperty("cuid", userToken)
                        it.addProperty("token", token)
                        it.addProperty("dev_pid", 80001)
                        it.addProperty("len", speech.first)
                        it.addProperty("speech", speech.second)
                    })
                    .withMainThread(false)
                    .callback(object : BdResultCallback<BdFastSuccessBean>(){

                        override fun isSuccess(rawData: JSONObject): Boolean {
                            return rawData.has("result")
                        }

                        override fun getServerFailedMessage(obj: JSONObject): String {
                            return obj.string("err_msg")?:""
                        }

                        override fun onNetErr(code: Int, msg: String?) {
                            super.onNetErr(code, msg)
                            emitter.onError(IllegalArgumentException(msg))
                        }

                        override fun onError(simpleMsg: String?, code: Int, e: Exception?) {
                            emitter.onError(IllegalArgumentException(simpleMsg))
                            L.e("极速版错误==>${simpleMsg}")
                        }

                        override fun onSuccess(response: BdFastSuccessBean) {
                            L.e("极速版获取到response==>${response.result}")
                            emitter.onNext(response.result)
                            emitter.onComplete()
                        }
                    }).enqueue()
            }
        })
    }
}
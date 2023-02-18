package com.hnradio.common.util.bdaudio

import android.os.SystemClock
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.hnradio.common.BuildConfig
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
import java.lang.Exception
import java.lang.IllegalArgumentException

/**
 * @author ytf
 * Created by on 2021/11/02 09:50
 * 百度语音转写服务
 */
class BdRecorgnizeTask {

    private val httpManager by lazy { OkHttpManager.getInstance() }

    interface Callback{
        fun onError(key : Int, msg : String)
        fun onSuccess(key : Int, ret : Pair<Int, String>)
    }

    private var mCallback : Callback? = null
    private var currentMp3Url = ""
    private var currentKey = 0
    private var disposable : Disposable? = null

    fun recorgnize(key : Int, url : String, callback : Callback){
        currentKey = key
        currentMp3Url = url
        mCallback = callback
        disposable = getAccessToken()
            .flatMap { postTask(currentMp3Url, it) }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                queryResult(it.first, it.second)
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

    private fun postTask(url : String, token : String) : Observable<Pair<String, String>> {
        return Observable.create(object : ObservableOnSubscribe<Pair<String, String>> {
            override fun subscribe(emitter: ObservableEmitter<Pair<String, String>>) {
                httpManager.post("https://aip.baidubce.com/rpc/2.0/aasr/v1/create?access_token=${token}")
                    .postJson(JsonObject().also {
                        it.addProperty("speech_url", url)
                        it.addProperty("format", "mp3")
                        it.addProperty("pid", 80001)
                        it.addProperty("rate", 16000)
                    })
                    .withMainThread(false)
                    .callback(object : BdResultCallback<BdTaskBean>(){

                        override fun isSuccess(rawData: JSONObject?): Boolean {
                            return rawData?.has("task_id")?:false
                        }

                        override fun getServerFailedMessage(obj: JSONObject?): String {
                            return obj?.string("error_msg")?:""
                        }

                        override fun onNetErr(code: Int, msg: String?) {
                            super.onNetErr(code, msg)
                            emitter.onError(IllegalArgumentException(msg))
                        }

                        override fun onError(simpleMsg: String?, code: Int, e: Exception?) {
                            emitter.onError(IllegalArgumentException(simpleMsg))
                        }

                        override fun onSuccess(response: BdTaskBean) {
                            L.e("获取到response==>${response}")
                            MMKVUtils.encode("bd_current_task_id", response.task_id)
                            emitter.onNext(Pair(token, response.task_id))
                            emitter.onComplete()
                        }
                    }).enqueue()
            }
        })
    }

    private fun queryResult(token : String, taskId : String){

        val jsonObj = JsonObject()
        jsonObj.add("task_ids", JsonArray().also { it.add(taskId) })
        httpManager.post("https://aip.baidubce.com/rpc/2.0/aasr/v1/query?access_token=${token}")
            .withMainThread(false)
            .postJson(jsonObj)
            .callback(object : BdResultCallback<BdRSuccessBean>(){

                override fun isSuccess(rawData: JSONObject): Boolean {
                    return getServerCode(rawData) == 1
                }

                override fun getServerCode(obj: JSONObject): Int {
                    val taskInfo = obj.optJSONArray("tasks_info")
                    if(taskInfo != null && taskInfo.length() > 0){
                        val statusStr =  taskInfo.getJSONObject(0).optString("task_status")
                        return when(statusStr){
                            "Running" -> 0
                            "Success" -> 1
                            else -> -1
                        }
                    }
                    return -1
                }

                override fun needCustomReturn(): Boolean {
                    return true
                }

                override fun getCustomReturnClass(serverCode: Int): Class<*> {
                    return when (serverCode) {
                        0 -> {
                            BdRGoingBean::class.java
                        }
                        1 -> {
                            BdRSuccessBean::class.java
                        }
                        else -> {
                            BdRErrorBean::class.java
                        }
                    }
                }

                override fun getServerFailedMessage(obj: JSONObject): String {
                    return ""
                }

                override fun onError(simpleMsg: String?, code: Int, e: Exception?) {
                    mCallback?.onError(currentKey, simpleMsg?:"")
                }

                override fun onNetErr(code: Int, msg: String?) {
                    super.onNetErr(code, msg)
                    mCallback?.onError(currentKey, msg?:"")
                }

                override fun onSuccess(response: BdRSuccessBean) {
                    L.e("获取到response==>${response}")
                    val sc = response.tasks_info[0].task_result
                    val duration = sc.audio_duration
                    val txt = sc.result[0].asString
                    mCallback?.onSuccess(currentKey, Pair(duration, txt))
                    disposable?.dispose()
                }

                override fun onCustomSuccess(serverCode: Int, d: Any?) {
                    super.onCustomSuccess(serverCode, d)
                    when(serverCode){
                        0 ->{
                            SystemClock.sleep(500)
                            queryResult(token, taskId)
                        }
                        -1 ->{
                            mCallback?.onError(currentKey, (d as BdRErrorBean).tasks_info[0].task_result.err_msg)
                        }
                    }
                }
            }).enqueue()

    }
}
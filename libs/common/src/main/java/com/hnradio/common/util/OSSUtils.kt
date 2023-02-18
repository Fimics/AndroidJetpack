package com.hnradio.common.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.alibaba.sdk.android.oss.*
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback
import com.alibaba.sdk.android.oss.common.OSSLog
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.hnradio.common.http.CommonApiUtil
import com.hnradio.common.manager.UserManager

/**
 *  Ali OSS封装
 * created by qiaoyan on 2021/8/4
 */
class OSSUtils {

    companion object {

        private const val tag = "OSSUtils_status"
        private const val tag_progress = "OSSUtils_progress"
        private var oss: OSS? = null
        private var bucketName: String? = null

        //单文件上传
        private var currentUploadFilePath: String? = null
        private var uploadFileCallback: UploadFileCallback? = null

        //字节流上传
        private var currentUploadByteArray: ByteArray? = null

        //多文件上传
        private var currentUploadFilePathList: ArrayList<String> = ArrayList()

        private var currentUploadFileCount = 1
        private var currentUploadPosition = 1

        private var currentUrlList = ArrayList<String>()
        private var uploadMultiFilesCallback: UploadMultiFilesCallback? = null

        private val mHandler = Handler(Looper.myLooper()!!)

        /**
         * 初始化服务
         */
        fun prepare(context: Context, initCallback : ()->Unit) {

            if(oss == null){
                Log.d(tag, "oss init")
                OSSLog.logInfo("oss init")
                CommonApiUtil.getAliOSSConfig {
                    if (it?.data != null) {
                        Log.d(tag, "oss getInfo success")
                        OSSLog.logInfo("oss getInfo success")
                        bucketName = it.data?.bucketName

                        //该配置类如果不设置，会有默认配置，具体可看该类
                        val conf = ClientConfiguration()
                        conf.connectionTimeout = 15 * 1000 // 连接超时，默认15秒
                        conf.socketTimeout = 15 * 1000 // socket超时，默认15秒
                        conf.maxConcurrentRequest = 5 // 最大并发请求数，默认5个
                        conf.maxErrorRetry = 2 // 失败后最大重试次数，默认2次
                        OSSLog.enableLog() //这个开启会支持写入手机sd卡中的一份日志文件位置在SDCard_path\OSSLog\logs.csv
                        val credentialProvider: OSSCredentialProvider = OSSStsTokenCredentialProvider(
                            it.data?.accessKeyId,
                            it.data?.accessKeySecret,
                            it.data?.securityToken
                        )
                        oss = OSSClient(context, it.data?.endpoint, credentialProvider, conf)

                        initCallback()
                        /*//初始化完成 检查一下当前有没有任务
                        if (currentUploadFilePath != null) {
                            upLoadFile(
                                Global.application,
                                currentUploadFilePath!!,
                                uploadFileCallback!!
                            )
                        }
                        if (!currentUploadFilePathList.isNullOrEmpty()) {
                            upLoadMultiFiles(
                                Global.application,
                                currentUploadFilePathList!!,
                                uploadMultiFilesCallback!!
                            )
                        }
                        if (currentUploadByteArray != null) {
                            upLoadBytes(
                                Global.application,
                                currentUploadByteArray!!,
                                uploadFileCallback!!
                            )
                        }*/

                    } else {
                        Log.d(tag, "oss getInfo failure")
                        OSSLog.logInfo("oss getInfo failure")
                        ToastUtils.show("OSS服务初始化失败")
                        uploadFileCallback?.onUploadFailure()
                        uploadMultiFilesCallback?.onUploadFailure()
                    }
                }
            }else{
                initCallback()
            }
        }

        //避免多次回调
        private var lastProgress = 0

        /**
         * 上传文件
         *
         */
        fun upLoadFile(
            context: Context,
            uploadFilePath: String,
            uploadFileCallback: UploadFileCallback,
            progressCallback: (percent: Int) -> Unit = {}
        ) {
            lastProgress = 0
            currentUploadFilePath = uploadFilePath
            this.uploadFileCallback = uploadFileCallback

            prepare(context){
                Log.d(tag, "upLoadFile start")
                OSSLog.logInfo("upLoadFile start")
                //上传文件的名称格式   反转时间戳+userId.文件格式
                val fileName =
                    StringBuilder(System.currentTimeMillis().toString())
                        .reverse()
                        .append(UserManager.getLoginUser()!!.id)
                        .append(uploadFilePath.substring(uploadFilePath.lastIndexOf(".")))
                        .toString()
                val objectKey = "tfapp/live/$fileName"
                // 构造上传请求
                val put = PutObjectRequest(bucketName, objectKey, uploadFilePath)
                // 异步上传时可以设置进度回调
                put.progressCallback =
                    OSSProgressCallback { request, currentSize, totalSize ->
//                    Log.d(tag_progress, "currentSize: $currentSize  ===  totalSize: $totalSize")
                        val prog = (currentSize.toFloat() / totalSize * 100).toInt()
                        if (lastProgress != prog) {
                            lastProgress = prog
                            mHandler.post{
                                progressCallback(prog)
                            }
                        }
                    }
                //开始上传
                val task: OSSAsyncTask<*> = oss!!.asyncPutObject(
                    put,
                    object : OSSCompletedCallback<PutObjectRequest?, PutObjectResult?> {
                        override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult?) {
                            currentUploadFilePath = null
                            val url = oss!!.presignPublicObjectURL(bucketName, objectKey)
                            Log.d(tag, "onSuccess url = $url")
                            OSSLog.logInfo("onSuccess url = $url")
                            mHandler.post {
                                uploadFileCallback.onUploadSuccess(url)
                            }
                        }

                        override fun onFailure(
                            request: PutObjectRequest?,
                            clientExcepion: ClientException,
                            serviceException: ServiceException
                        ) {
                            currentUploadFilePath = null
                            mHandler.post {
                                uploadFileCallback.onUploadFailure()
                            }
                            // 请求异常
                            Log.e(tag, clientExcepion.toString())
                            OSSLog.logError("onFailure = $clientExcepion")
                            // 服务异常
                            Log.e(tag, serviceException.toString())
                            OSSLog.logError("onFailure = $serviceException")
                        }
                    })
            }
        }


        /**
         * 上传文件
         *
         */
        fun upLoadFile(
            uploadFilePath: String,
            uploadFileCallback: UploadFileCallback
        ): OSSAsyncTask<*>? {
            if (oss == null) {
                ToastUtils.show("OSS服务未初始化，请稍后重试")
                prepare(Global.application){

                }
                uploadFileCallback.onUploadFailure()
                return null
            }
            Log.d(tag, "upLoadFile start")
            OSSLog.logInfo("upLoadFile start")
            val fileName =
                StringBuilder(System.currentTimeMillis().toString())
                    .reverse()
                    .append(UserManager.getLoginUser()!!.id)
                    .append(uploadFilePath.substring(uploadFilePath.lastIndexOf(".")))
                    .toString()
            val objectKey = "tfapp/live/$fileName"
            // 构造上传请求
            val put = PutObjectRequest(bucketName, objectKey, uploadFilePath)
            //开始上传
            return oss!!.asyncPutObject(
                put,
                object : OSSCompletedCallback<PutObjectRequest?, PutObjectResult?> {
                    override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult?) {
                        currentUploadFilePath = null
                        val url = oss!!.presignPublicObjectURL(bucketName, objectKey)
                        Log.d(tag, "onSuccess url = $url")
                        OSSLog.logInfo("onSuccess url = $url")
                        mHandler.post {
                            uploadFileCallback.onUploadSuccess(url)
                        }
                    }

                    override fun onFailure(
                        request: PutObjectRequest?,
                        clientExcepion: ClientException,
                        serviceException: ServiceException
                    ) {
                        currentUploadFilePath = null
                        mHandler.post {
                            uploadFileCallback.onUploadFailure()
                        }
                        // 请求异常
                        Log.e(tag, clientExcepion.toString())
                        OSSLog.logError("onFailure = $clientExcepion")
                        // 服务异常
                        Log.e(tag, serviceException.toString())
                        OSSLog.logError("onFailure = $serviceException")
                    }
                })
        }


        /**
         * 上传 二进制流
         *
         */
        fun upLoadBytes(
            context: Context,
            byteArray: ByteArray,
            uploadFileCallback: UploadFileCallback
        ) {
            currentUploadByteArray = byteArray
            this.uploadFileCallback = uploadFileCallback
            prepare(context){
                Log.d(tag, "upLoadBytes start")
                OSSLog.logInfo("upLoadBytes start")
                //上传文件的名称格式   反转时间戳+userId.文件格式
                val fileName =
                    StringBuilder(System.currentTimeMillis().toString())
                        .reverse()
                        .append(UserManager.getLoginUser()!!.id)
                        .append(".png")
                        .toString()
                val objectKey = "tfapp/live/$fileName"
                // 构造上传请求
                val put = PutObjectRequest(bucketName, objectKey, byteArray)
                put.progressCallback =
                    OSSProgressCallback { request, currentSize, totalSize ->
                        Log.d(tag_progress, "currentSize: $currentSize  ===  totalSize: $totalSize")
                    }
                //开始上传
                val task: OSSAsyncTask<*> = oss!!.asyncPutObject(
                    put,
                    object : OSSCompletedCallback<PutObjectRequest?, PutObjectResult?> {
                        override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult?) {
                            currentUploadByteArray = null
                            val url = oss!!.presignPublicObjectURL(bucketName, objectKey)
                            Log.d(tag, "onSuccess url = $url")
                            OSSLog.logInfo("onSuccess url = $url")
                            mHandler.post {
                                uploadFileCallback.onUploadSuccess(url)
                            }
                        }

                        override fun onFailure(
                            request: PutObjectRequest?,
                            clientExcepion: ClientException,
                            serviceException: ServiceException
                        ) {
                            currentUploadByteArray = null
                            mHandler.post {
                                uploadFileCallback.onUploadFailure()
                            }
                            // 请求异常
                            Log.e(tag, clientExcepion.toString())
                            OSSLog.logError("onFailure = $clientExcepion")
                            // 服务异常
                            Log.e(tag, serviceException.toString())
                            OSSLog.logError("onFailure = $serviceException")
                        }
                    })
            }
        }


        /**
         * 上传多文件
         *
         */
        fun upLoadMultiFiles(
            context: Context,
            uploadFilePathList: ArrayList<String>,
            uploadMultiFilesCallback: UploadMultiFilesCallback,
            progressCallback: (current: Int, all: Int) -> Unit = { _, _ -> {} }
        ) {

            this.uploadMultiFilesCallback = uploadMultiFilesCallback

            currentUploadFilePathList.clear()
            currentUploadFilePathList.addAll(uploadFilePathList)

            currentUploadFileCount = uploadFilePathList.size
            currentUploadPosition = 1

            //清空数据
            currentUrlList.clear()
            prepare(context){
                Log.d(tag, "upLoadMultiFiles start")
                OSSLog.logInfo("upLoadMultiFiles start")
                //递归上传
                if (currentUploadFilePathList.size > 0) {
                    upLoadFileRecursion(currentUploadFilePathList.removeAt(0), progressCallback)
                } else {
                    //全部上传完成
                    mHandler.post {
                        uploadMultiFilesCallback.onUploadSuccess(currentUrlList)
                    }
                }
            }
        }


        /**
         * 递归上传
         */
        private fun upLoadFileRecursion(
            uploadFilePath: String,
            progressCallback: (current: Int, all: Int) -> Unit = { _, _ -> {} }
        ) {
            Log.d(tag, "upLoadFileRecursion start")
            OSSLog.logInfo("upLoadFileRecursion start")

            currentUploadPosition = currentUploadFileCount - currentUploadFilePathList.size
            //进度, 走handler不更新
            progressCallback(currentUploadPosition, currentUploadFileCount)

            //上传文件的名称格式   反转时间戳+userId.文件格式
            val fileName =
                StringBuilder(System.currentTimeMillis().toString())
                    .reverse()
                    .append(UserManager.getLoginUser()!!.id)
                    .append(uploadFilePath.substring(uploadFilePath.lastIndexOf(".")))
                    .toString()
            val objectKey = "tfapp/live/$fileName"
            // 构造上传请求
            val put = PutObjectRequest(bucketName, objectKey, uploadFilePath)
            put.progressCallback =
                OSSProgressCallback { request, currentSize, totalSize ->
                    Log.d(tag_progress, "currentSize: $currentSize  ===  totalSize: $totalSize")
                }
            //开始上传
            val task: OSSAsyncTask<*> = oss!!.asyncPutObject(
                put,
                object : OSSCompletedCallback<PutObjectRequest?, PutObjectResult?> {
                    override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult?) {
                        val url = oss!!.presignPublicObjectURL(bucketName, objectKey)
                        Log.d(tag, "upLoadFileRecursion url = $url")
                        OSSLog.logInfo("upLoadFileRecursion url = $url")
                        //将地址添加进去
                        currentUrlList.add(url)
                        //再次调用自身
                        if (currentUploadFilePathList!!.size > 0) {
                            upLoadFileRecursion(currentUploadFilePathList!!.removeAt(0))
                        } else {
                            //全部上传完成
                            mHandler.post {
                                uploadMultiFilesCallback?.onUploadSuccess(currentUrlList)
                            }
                        }
                    }

                    override fun onFailure(
                        request: PutObjectRequest?,
                        clientExcepion: ClientException,
                        serviceException: ServiceException
                    ) {
                        // 上传失败
                        mHandler.post {
                            uploadMultiFilesCallback?.onUploadFailure()
                        }
                        // 请求异常
                        Log.e(tag, clientExcepion.toString())
                        OSSLog.logError("onFailure = $clientExcepion")
                        // 服务异常
                        Log.e(tag, serviceException.toString())
                        OSSLog.logError("onFailure = $serviceException")
                    }
                })
        }
    }


    interface UploadFileCallback {

        fun onUploadSuccess(url: String)

        fun onUploadFailure()
    }

    interface UploadMultiFilesCallback {

        fun onUploadSuccess(urlList: ArrayList<String>)

        fun onUploadFailure()
    }
}
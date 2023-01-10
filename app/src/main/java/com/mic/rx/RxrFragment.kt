package com.mic.rx

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mic.databinding.FragmentRxBinding
import com.mic.rx.login.*
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.net.HttpURLConnection
import java.net.URL
import kotlin.properties.Delegates

class RxrFragment : Fragment() {

    private var _binding: FragmentRxBinding? = null
    private val binding get() = _binding!!
    private var progressDialog by Delegates.notNull<ProgressDialog>()
    private val apiHelper = ApiHelper()

    // 网络图片的链接地址
    private val PATH = "http://pic1.win4000.com/wallpaper/c/53cdd1f7c1f21.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRxBinding.inflate(inflater, container, false)
        progressDialog = ProgressDialog(activity)
        binding.btnDownload.setOnClickListener {
            downloadImage()
        }
        shakeProjectData()
        binding.btnLogin.setOnClickListener {
            doOnNext()
        }
        return binding.root
    }

    private fun downloadImage() {
        Observable
            .just(PATH)
            .map(object : Function<String, Bitmap> {
                override fun apply(t: String): Bitmap? {
                    val url = URL(PATH)
                    val httpUrlConnection = url.openConnection() as HttpURLConnection
                    httpUrlConnection.connectTimeout = 5000
                    val resultCode = httpUrlConnection.responseCode
                    if (resultCode == HttpURLConnection.HTTP_OK) {
                        val inputStream = httpUrlConnection.inputStream
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        return bitmap
                    }
                    return null
                }
            })
            .map(object : Function<Bitmap,Bitmap> {
                override fun apply(t: Bitmap): Bitmap {
                    val paint = Paint()
                    paint.textSize= 80F
                    paint.color= Color.RED
                    return drawTextToBitmap(t,"rxjava",paint,100f,100f)
                }
            })
            .compose(RxUtils.rxUd())
            .subscribe(object : Observer<Bitmap> {
                override fun onSubscribe(d: Disposable) {
                    //1.开始订阅
                    progressDialog.setTitle("图片下载中...")
                    progressDialog.show()
                    println("downloadImage...")
                }

                override fun onNext(t: Bitmap) {
                    binding.image.setImageBitmap(t)
                }

                override fun onError(e: Throwable) {
                    //错误事件
                }

                override fun onComplete() {
                    //完成事件
                    if (progressDialog != null) {
                        progressDialog.dismiss()
                    }
                }
            })
    }

    private fun drawTextToBitmap(bitmap: Bitmap, text: String, paint: Paint, paddingLeft: Float, paddingTop: Float):Bitmap {
        var config = bitmap.config
        // 获取跟清晰的图像采样
        paint.isDither = true
        paint.isFilterBitmap = true
        if (config == null) {
            config = Bitmap.Config.ARGB_8888
        }
        val bitmap = bitmap.copy(config, true)
        val canvas = Canvas(bitmap)
        canvas.drawText(text, paddingLeft, paddingTop, paint)
        return bitmap
    }

    /**
     * 功能防抖 + 网络嵌套 (解决嵌套的问题) flatMap
     */
    @SuppressLint("CheckResult")
    private fun shakeProjectData(){
         apiHelper.getData(binding.btnProject)
    }

    /**
     * 一行代码 实现需求
     * 需求：
     *   还有弹出加载
     *  * 1.请求服务器注册操作
     *  * 2.注册完成之后，更新注册UI
     *  * 3.马上去登录服务器操作
     *  * 4.登录完成之后，更新登录的UI
     */
    private var disposable:Disposable?=null
    @SuppressLint("CheckResult")
    private fun doOnNext(){

           RetrofitClient.getRetrofitClient("")
               .create(IReqeustNetwor::class.java)
               .registerAction(RegisterRequest())//1.请求服务器注册操作
               .subscribeOn(Schedulers.io()) // 给上面 异步
               .observeOn(AndroidSchedulers.mainThread()) // 给下面分配主线程
               .doOnNext(object : Consumer<RegisterResponse> {
                   override fun accept(t: RegisterResponse?) {
                       // todo 2.注册完成之后，更新注册UI
                   }
               })
              // 3.马上去登录服务器操作
               .observeOn(Schedulers.io()) // 给下面分配了异步线程
               .flatMap(object : Function<RegisterResponse,ObservableSource<LoginResponse>> {
                   override fun apply(registerResponse: RegisterResponse): ObservableSource<LoginResponse> {
                       val loginResponseObservable :Observable<LoginResponse> = RetrofitClient.getRetrofitClient("").create(IReqeustNetwor::class.java)
                           .loginAction(LoginReqeust())
                       return loginResponseObservable
                   }
               })
               .observeOn(AndroidSchedulers.mainThread())
               // 一定是主线程，为什么，因为 subscribe 马上调用onSubscribe
               .subscribe(object : Observer<LoginResponse> {
                   override fun onSubscribe(d: Disposable) {
                       disposable=d
                   }

                   override fun onNext(t: LoginResponse) {
                   }

                   override fun onError(e: Throwable) {
                   }

                   override fun onComplete() {
                   }
               })

    }

    override fun onDestroy() {
        super.onDestroy()
        // 必须这样写，最起码的标准 不然会内存泄漏
        disposable?.dispose()
    }
}
package com.hnradio.common.util

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.http.HttpResponseCache
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.alibaba.android.arouter.launcher.ARouter
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.services.core.ServiceSettings.updatePrivacyAgree
import com.amap.api.services.core.ServiceSettings.updatePrivacyShow
import com.amap.api.services.poisearch.PoiSearch
import com.hnradio.common.AppContext
import com.hnradio.common.R
import com.hnradio.common.constant.UrlConstant
import com.hnradio.common.di.appModule
import com.hnradio.common.manager.UserManager
import com.hnradio.common.util.mqtt.MQTTUtil
import com.hnradio.common.util.mqtt.MqttEngine
import com.hnradio.common.util.upgrade.XHttpUpdateHttpService
import com.opensource.svgaplayer.SVGACache
import com.opensource.svgaplayer.SVGAParser
import com.orhanobut.hawk.Hawk
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import com.tencent.mmkv.MMKV
import com.umeng.commonsdk.UMConfigure
import com.xinstall.XInstall
import com.xuexiang.xhttp2.XHttp
import com.xuexiang.xhttp2.XHttpSDK
import com.xuexiang.xupdate.XUpdate
import com.xuexiang.xupdate.entity.UpdateError
import com.xuexiang.xupdate.utils.UpdateUtils
import com.yingding.lib_net.BuildConfig
import com.yingding.lib_net.http.RetrofitUtil
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import java.io.File
import java.util.*


/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.util
 * @ClassName: Test
 * @Description: java类作用描述
 * @Author: shaoguotong
 * @CreateDate: 2021/6/20 11:46 下午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/6/20 11:46 下午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
class Global {
    companion object {
        //极光认证是否可用
        var JGVerifyEnable = false

        var mainIsActive = false

        //mqtt 辅助类
        val mqttUtil: MQTTUtil = MQTTUtil()

        /**
         * 公共全局变量
         */
        lateinit var application: Application private set
        lateinit var activityStack: Stack<Activity>

        /**剪切板*/
        val clipboardManager by lazy(LazyThreadSafetyMode.NONE) { application.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

        fun copyText(what : String){
            val data = ClipData.newPlainText("", what)
            clipboardManager.setPrimaryClip(data)
        }


        private fun isMainProcess(application: Application): Boolean {
            val pid = Process.myPid()
            val activityManager = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (appProcess in activityManager.runningAppProcesses) {
                if (appProcess.pid == pid) {
                    return application.packageName == appProcess.processName
                }
            }
            return false
        }

        /**
         * 初始化
         */
        fun init(isDebug: Boolean, application: Application, activityStack: Stack<Activity>) {
            this.application = application
            this.activityStack = activityStack

            //数据存储初始化
            MMKV.initialize(application)

            val formatStrategy = PrettyFormatStrategy.newBuilder()
                .tag("hnradio_fans")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build()
            Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
                override fun isLoggable(priority: Int, tag: String?): Boolean {
                    return isDebug
                }
            })
            RetrofitUtil.instance.exceptionInterface = NetExceptionProcess()

            if (isDebug) {           // These two lines must be written before init, otherwise these configurations will be invalid in the init process
                ARouter.openLog()      // Print log
                ARouter.openDebug()    // Turn on debugging mode (If you are running in InstantRun mode, you must turn on debug mode! Online version needs to be closed, otherwise there is a security risk)
            }
            ARouter.init(application)
            initHawk()
            /**
             * 错误日志聚成aa
             */
            //  ErrorHandler.getInstance().init(application)
            //初始化koin依赖框架
            // Start Koin
            startKoin {
                androidLogger()
                androidContext(application)
                modules(appModule)
            }

            if(isMainProcess(application)){
                L.e("主线程")
                MqttEngine.instance.init(application)
            }
        }

        fun initAfterAgree(isDebug: Boolean) {
            /**
             * 初始化XInstall
             * Xinstall是集智能传参、快速安装、一键拉起、多维数据统计等功能，帮您提高拉新转化率、安装率和多元化精确统计渠道效果的产品。
             */
            if (XInstall.isMainProcess(application)) {
                // 启用log
                XInstall.setDebug(isDebug)
                XInstall.init(application)
            }
            //svga 初始化
            SVGACache.onCreate(application, SVGACache.Type.FILE)
            val cacheDir = File(application.cacheDir, "http")
            HttpResponseCache.install(cacheDir, 1024 * 1024 * 512)

            UserManager.initUser()

            UMConfigure.init(
                application,
                UrlConstant.UMENG_APPID,
                UrlConstant.UMENG_CHANNEL,
                UMConfigure.DEVICE_TYPE_PHONE,
                ""
            )
            UMConfigure.setLogEnabled(BuildConfig.DEBUG)
            initXHttp()
            initXUpdate()
            //高德地图，5.6.0才有这两个方法***确保调用SDK任何接口前先调用更新隐私合规updatePrivacyShow、updatePrivacyAgree两个接口并且参数值都为true，若未正确设置有崩溃风险***
            AMapLocationClient.updatePrivacyShow(application, true, true)
            AMapLocationClient.updatePrivacyAgree(application, true)
            //初始化mqtt，延迟到使用时再建立连接
//            if (UserManager.isLogin()) {
//                mqttUtil.initConfig()
//                MqttEngine.instance.notifyConfigChanged()
//            }
        }


        private fun initHawk() {
            Hawk.init(this.application).build()
            Hawk.put("Tag_Normal_Show_This_Time", true)
            Hawk.put("Tag_Zimu_Show_This_Time", true)
        }

        /**
         * -----------------------------------------------------------------------------------------------------------------
         * -----------------------------------------------------------------------------------------------------------------
         * common
         * -----------------------------------------------------------------------------------------------------------------
         * -----------------------------------------------------------------------------------------------------------------
         */
        fun getTopActivity(): Activity? =
            if (activityStack.isEmpty()) null else activityStack.lastElement()

        fun thisIsTopActivity(activity: Activity): Boolean {
            getTopActivity()?.run { return activity.javaClass.name == javaClass.name }
            return false
        }

        fun topActivityIs(vararg names: String): Boolean {
            getTopActivity()?.run {
                return !names.filter { javaClass.name.contains(it) }.isNullOrEmpty()
            }
            return false
        }

        fun dog2newTaskActivity(clazz: Class<*>) {
            getTopActivity()?.apply {
                startActivity(
                    Intent(
                        this,
                        clazz
                    ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }

        fun dog2ActivityByRouter(
            router: String,
            bundle: Bundle? = null
        ) {
            ARouter.getInstance().build(router).with(bundle).navigation()
        }


        fun showFragmentByRouter(
            activity: FragmentActivity,
            router: String,
            bundle: Bundle? = null,
            addBackStack: Boolean = true
        ) {
            try {
                //改行创建fragment实例失败，可能是由缓存造成的，清除缓存卸载app重装
                val fragment = ARouter.getInstance().build(router).navigation() as Fragment
                showFragment(activity, fragment, bundle, addBackStack)
            } catch (e: Exception) {
                ToastUtils.show(e.message)
            }
        }

        private fun showFragment(
            activity: FragmentActivity,
            fragment: Fragment,
            bundle: Bundle? = null,
            addBackStack: Boolean = true
        ) {
            val transaction = activity.supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(
                R.anim.translate_right_to_center,
                0,
                0,
                R.anim.translate_center_to_right
            )
            if (bundle != null) fragment.arguments = bundle
            transaction.add(android.R.id.content, fragment)
            if (addBackStack) transaction.addToBackStack(null)
            transaction.commit()
        }

        fun hideSoftKeyboard(activity: Activity?) {
            if (activity != null && activity.currentFocus != null && activity.window.attributes.softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                val systemService =
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                systemService.hideSoftInputFromWindow(
                    activity.currentFocus!!.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        }

        fun killApp() {
            Runtime.getRuntime().gc()
            android.os.Process.killProcess(android.os.Process.myPid())
        }

        fun getIdByName(context: Context, className: String, name: String): Int {
            val packageName = context.packageName
            val r: Class<*>?
            var id = 0

            try {
                r = Class.forName("$packageName.R")
                val classes = r!!.classes
                var desireClass: Class<*>? = null

                for (i in classes.indices) {
                    if (classes[i].name.split("\\$".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()[1] == className) {
                        desireClass = classes[i]
                        break
                    }
                }

                if (desireClass != null) {
                    id = desireClass.getField(name).getInt(desireClass)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return id
        }

        fun isRunForeground(context: Context): Boolean {
            var isRunForeground = false
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val packageName = application.packageName
            val appProcesses = activityManager.runningAppProcesses
            if (!appProcesses.isNullOrEmpty()) {
                for (appProcess in appProcesses) {
                    if (appProcess.processName.equals(packageName)
                        && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    ) {
                        isRunForeground = true
                    }

                }
            }
            Logger.d("点击通知栏是否后台运行${isRunForeground}")
            return isRunForeground
        }

        fun isBackground(context: Context): Boolean {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val appProcesses = activityManager.runningAppProcesses
            var isBackground = true
            var processName = "empty"
            for (appProcess in appProcesses) {
                if (appProcess.processName == context.getPackageName()) {
                    processName = appProcess.processName
                    isBackground =
                        !(appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                                || appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE)

                }
            }
            Logger.d("点击通知栏是否后台运行${isBackground}==${processName}")

            return isBackground
        }

        private fun initXHttp() {
            //初始化网络请求框架，必须首先执行
            XHttpSDK.init(AppContext.getContext())
            //需要调试的时候执行
            XHttpSDK.debug("XHttp")
            XHttp.getInstance().setTimeout(20000)
        }

        fun initXUpdate() {
            XUpdate.get()
                .debug(BuildConfig.DEBUG)
                .isWifiOnly(true)
                .isGet(true)
                .isAutoMode(false)
                .param(
                    "versionCode",
                    UpdateUtils.getVersionCode(AppContext.getContext())
                )
                .param("appKey", AppContext.getContext().packageName)
                .setOnUpdateFailureListener { error ->
                    if (error.code !== UpdateError.ERROR.CHECK_NO_NEW_VERSION) {
                        ToastUtils.show(error.toString())
                    }
                }
                .supportSilentInstall(true)
                .setIUpdateHttpService(XHttpUpdateHttpService(BuildConfig.ApiUrl))
                .init(AppContext.getContext())

        }


    }
}
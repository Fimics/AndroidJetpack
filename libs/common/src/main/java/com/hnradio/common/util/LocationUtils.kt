package com.hnradio.common.util

import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.orhanobut.logger.Logger

/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-08-09 15:45
 * @Version: 1.0
 */
object LocationUtils {
    private var locationClient: AMapLocationClient? = null
    private var locationOption: AMapLocationClientOption? = null
    private var myLocationListener: LocationListener? = null

    fun init() {
        if (locationClient == null) {
            locationClient = AMapLocationClient(Global.application)
        }
        if (locationOption == null) {
            locationOption = getDefaultOption();
        }
        locationClient!!.setLocationListener {
            Logger.d("定位完成：${it.errorCode}")
            if (null != it) {
                myLocationListener!!.onLocationChanged(it.errorCode, it)
                Logger.d("定位成功：${it.address}")
            }
        }
    }

    fun setMyLocationListener(myLocationListener: LocationListener) {
        this.myLocationListener = myLocationListener
    }

    /**
     * 默认的定位参数
     */
    private fun getDefaultOption(): AMapLocationClientOption? {
        val mOption = AMapLocationClientOption()
        mOption.locationMode =
            AMapLocationClientOption.AMapLocationMode.Hight_Accuracy //可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.isGpsFirst = false //可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.httpTimeOut = 30000 //可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.interval = 5000 //可选，设置定位间隔。默认为2秒
        mOption.isNeedAddress = true //可选，设置是否返回逆地理地址信息。默认是true
        mOption.isOnceLocationLatest =
            false //可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP) //可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.isSensorEnable = false //可选，设置是否使用传感器。默认是false
        mOption.isWifiScan =
            true //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.isLocationCacheEnable = true //可选，设置是否使用缓存定位，默认为true
        mOption.geoLanguage =
            AMapLocationClientOption.GeoLanguage.DEFAULT //可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）


        mOption.isOnceLocation = true //可选，设置是否单次定位。默认是false

        return mOption
    }

    /**
     * 开始定位
     */
    fun startLocation() {
        locationClient!!.startLocation()
    }

    /**
     * 停止定位
     */
    fun stopLocation() {
        locationClient!!.stopLocation()
    }

    /**
     * 销毁定位
     */
    fun destroyLocation() {
        if (null != locationClient) {
            locationClient!!.onDestroy()
            locationClient = null
            locationOption = null
        }
    }
}

/**
 * 定位回调接口
 */
interface LocationListener {
    fun onLocationChanged(code: Int, location: AMapLocation?)
}
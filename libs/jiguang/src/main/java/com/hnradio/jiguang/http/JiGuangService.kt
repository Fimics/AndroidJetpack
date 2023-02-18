package com.hnradio.jiguang.http

import com.hnradio.common.http.bean.UserInfo
import com.hnradio.jiguang.http.bean.JGLoginBean
import com.yingding.lib_net.bean.base.BaseResBean
import io.reactivex.Observable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 *
 * @ProjectName: hnradio_fans
 * @Package: com.hnradio.common.http
 * @ClassName: CommonServer
 * @Description: java类作用描述
 * @Author: shaoguotong
 * @CreateDate: 2021/7/20 6:09 下午
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/7/20 6:09 下午
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
interface JiGuangService {

    @POST(login)
    fun login(@Body body: RequestBody): Observable<BaseResBean<JGLoginBean>>

    @GET(getLoginUserInfo)
    fun getUserInfo(): Observable<BaseResBean<UserInfo>>

}
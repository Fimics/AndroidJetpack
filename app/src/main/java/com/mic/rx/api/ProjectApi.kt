package com.mic.rx.api

import com.mic.rx.bean.ProjectBean
import com.mic.rx.bean.ProjectItem
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface ProjectApi {

    //总数据
    @GET("project/tree/json")
    fun getProject(): Observable<ProjectBean> // 异步线程 耗时操作

    //Item数据
    @GET("project/list/{pageIndex}/json") // ?cid=294
    fun getProjectItem(@Path("pageIndex") pageIndex:Int,@Query("cid") cid:Int):Observable<ProjectItem>

}
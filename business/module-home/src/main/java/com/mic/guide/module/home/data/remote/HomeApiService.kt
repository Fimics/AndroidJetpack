package com.mic.guide.module.home.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/** 首页 HTTP 接口（本模块的 data/remote；BaseUrl 由 NetworkClient 注入）。 */
interface HomeApiService {

    @GET("posts")
    suspend fun getPosts(
        @Query("_page") page: Int,
        @Query("_limit") limit: Int = 20,
    ): List<PostDto>
}

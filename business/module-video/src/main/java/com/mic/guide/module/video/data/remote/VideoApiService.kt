package com.mic.guide.module.video.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/** 视频 HTTP 接口（本模块的 data/remote；BaseUrl 由 NetworkClient 注入）。 */
interface VideoApiService {

    /**
     * 分页拉取视频列表。
     *
     * 示范用公共测试 API：以 `photos` 作为视频源（jsonplaceholder 支持 `_page`/`_limit` 分页）；
     * 接自家后端时把路径换成 `video/feed` 即可，调用方无需改动。
     */
    @GET("photos")
    suspend fun getPhotos(
        @Query("_page") page: Int = 1,
        @Query("_limit") limit: Int = 20,
    ): List<PhotoDto>
}
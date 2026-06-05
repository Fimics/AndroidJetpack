package com.mic.guide.module.music.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/** 音乐 HTTP 接口（本模块的 data/remote；BaseUrl 由 NetworkClient 注入）。 */
interface MusicApiService {

    /** 示范用公共测试 API：以 `albums` 作为歌单源；接自家后端时换成 `music/tracks` 即可。 */
    @GET("albums")
    suspend fun getAlbums(
        @Query("_limit") limit: Int = 20,
    ): List<AlbumDto>
}
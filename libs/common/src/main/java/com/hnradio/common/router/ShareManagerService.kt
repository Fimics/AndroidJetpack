package com.hnradio.common.router

import android.content.Context
import com.alibaba.android.arouter.facade.template.IProvider
import com.hnradio.common.http.bean.UserInfo

/**
 *
 * @Description: 分享
 * @Author: huqiang
 * @CreateDate: 2021-07-22 11:52
 * @Version: 1.0
 */
interface ShareManagerService : IProvider {
    fun shareUrl(
        context: Context,
        shareTitle: String,
        shareDescribe: String,
        shareUrl: String,
        shareImageUrl: String
    )
    fun shareImage(
        context: Context,
        shareImageUrl: String
    )

    fun shareMusic(
        context: Context,
        shareTitle: String,
        shareDescribe: String,
        shareMusicFileUrl: String?,
        shareMusicUrl: String?,
        shareImageUrl: String?
    )

    fun shareVideo(
        context: Context,
        shareTitle: String,
        shareDescribe: String,
        shareVideoUrl: String?,
        shareImageUrl: String?
    )

}
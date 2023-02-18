package com.hnradio.jiguang.jshare

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.hnradio.common.router.MainRouter
import com.hnradio.common.router.ShareManagerService

/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-07-21 17:26
 * @Version: 1.0
 */
@Route(path = MainRouter.ShareManagerProvider, name = "分享服务")
class ShareProvider : ShareManagerService {
    override fun shareUrl(
        context: Context,
        shareTitle: String,
        shareDescribe: String,
        shareUrl: String,
        shareImageUrl: String
    ) {
        ShareManager(context, null).shareUrl(
            true,
            shareTitle,
            shareDescribe,
            shareUrl,
            shareImageUrl
        )
    }

    override fun shareImage(context: Context, shareImageUrl: String) {
        ShareManager(context, null).shareImage(true, null, shareImageUrl)
    }

    override fun shareMusic(
        context: Context,
        shareTitle: String,
        shareDescribe: String,
        shareMusicFileUrl: String?,
        shareMusicUrl: String?,
        shareImageUrl: String?
    ) {
        ShareManager(context, null).shareMusic(
            true,
            null,
            shareTitle,
            shareDescribe,
            shareMusicFileUrl,
            shareMusicUrl,
            shareImageUrl
        )
    }

    override fun shareVideo(
        context: Context,
        shareTitle: String,
        shareDescribe: String,
        shareVideoUrl: String?,
        shareImageUrl: String?
    ) {
        ShareManager(context, null).shareVideo(
            true,
            null,
            shareTitle,
            shareDescribe,
            shareVideoUrl,
            shareImageUrl
        )
    }


    override fun init(context: Context?) {
    }
}
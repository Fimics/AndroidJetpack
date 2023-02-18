package com.hnradio.jiguang.jshare

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import cn.jiguang.share.android.api.JShareInterface
import cn.jiguang.share.android.api.PlatActionListener
import cn.jiguang.share.android.api.Platform
import cn.jiguang.share.android.api.ShareParams
import cn.jiguang.share.weibo.SinaWeibo
import com.hnradio.common.util.FileUtils
import com.hnradio.common.util.StringUtils
import com.hnradio.common.util.ToastUtils
import kotlinx.coroutines.runBlocking
import java.util.*


/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-08-26 18:14
 * @Version: 1.0
 */
class ShareUtils(val context: Context) {
    fun share(
        alias: String,
        shareType: Int,
        share_title: String? = "铁粉生活",
        share_text: String? = null,
        share_url: String? = null,
        share_image_url: String? = null,
        share_video_url: String? = null,
        share_music_url: String? = null,
        music_share_url: String? = null,
        share_image_data: Bitmap? = null
    ) {
        var shareParams = ShareParams()
        shareParams.shareType = shareType
        shareParams.title = share_title
        when (shareType) {
            Platform.SHARE_WEBPAGE -> {
                share_text?.let {
                    shareParams.text = it
                }
                share_url?.let {
                    shareParams.url = it
                    if (alias == ShareManager.ALIAS_COPY_URL) {
                        StringUtils.copyText(context, it)
                        return
                    }
                }
                if (!share_image_url.isNullOrBlank()) {
                    runBlocking {
                        shareParams.imageData = FileUtils.getNetWorkImage(context, share_image_url)
                    }
                }
            }
            Platform.SHARE_TEXT -> {
                share_text?.let {
                    shareParams.text = it
                }
            }
            Platform.SHARE_IMAGE -> {
                if (!share_image_url.isNullOrBlank()) {
                    runBlocking {
                        FileUtils.getNetWorkImage(context, share_image_url,false)?.let {
                            shareParams.imagePath =
                                FileUtils.saveImage(it, context, "tf_share").absolutePath
                        }
                    }
                } else {
                    share_image_data?.let {
                        runBlocking {
                            shareParams.imagePath =
                                FileUtils.saveImage(it, context, "tf_share").absolutePath
                            //shareParams.imageData = it
                        }
                    }
                }

            }
            Platform.SHARE_MUSIC -> {
                share_text?.let {
                    shareParams.text = it
                }
                if (alias == SinaWeibo.Name) {
                    music_share_url?.let {
                        shareParams.url = it
                    }
                } else {
                    share_music_url?.let {
                        shareParams.musicUrl = it
                    }
                    music_share_url?.let {
                        shareParams.url = it
                    }
                    share_image_url?.let {
                        runBlocking {
                            shareParams.imageData = FileUtils.getNetWorkImage(context, it, true)
                        }
                    }

                }

            }
            Platform.SHARE_VIDEO -> {
                share_text?.let {
                    shareParams.text = it
                }
                share_video_url?.let {
                    shareParams.url = it
                }
                share_image_url?.let {
                    runBlocking {
                        shareParams.imageData = FileUtils.getNetWorkImage(context, it, true)
                    }
                }
            }
        }
        JShareInterface.share(alias, shareParams, mPlatActionListener)
    }

    private val mPlatActionListener: PlatActionListener = object : PlatActionListener {
        override fun onComplete(platform: Platform, action: Int, data: HashMap<String, Any>) {
            Handler(Looper.getMainLooper()).post {
                ToastUtils.show("分享成功")
            }

        }

        override fun onError(platform: Platform, action: Int, errorCode: Int, error: Throwable) {
            Handler(Looper.getMainLooper()).post {
                ToastUtils.show("分享失败:" + (if (error != null) error.message else "") + "---" + errorCode)
            }
        }

        override fun onCancel(platform: Platform, action: Int) {
            Handler(Looper.getMainLooper()).post {
                ToastUtils.show("分享取消")
            }
        }
    }


}
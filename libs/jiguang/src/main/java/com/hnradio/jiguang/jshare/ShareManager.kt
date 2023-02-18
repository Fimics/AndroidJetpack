package com.hnradio.jiguang.jshare

import android.content.Context
import android.graphics.Bitmap
import cn.jiguang.share.android.api.Platform
import cn.jiguang.share.wechat.Wechat
import cn.jiguang.share.wechat.WechatFavorite
import cn.jiguang.share.wechat.WechatMoments
import cn.jiguang.share.weibo.SinaWeibo
import com.hnradio.common.manager.UserManager
import com.hnradio.jiguang.R
import com.orhanobut.logger.Logger


/**
 *
 * @Description: 分享工具
 * @Author: huqiang
 * @CreateDate: 2021-07-14 17:37
 * @Version: 1.0
 */
class ShareManager(val context: Context, var shareList: MutableList<ShareBean>? = null) {
    companion object {
        //微信
        val ALIAS_WECHAT = Wechat.Name

        //微信朋友圈
        val ALIAS_WX_FIREND = WechatMoments.Name

        //微信收藏
        val ALIAS_WX_FAVORITE = WechatFavorite.Name

        //微博
        val ALIAS_SINAWEIBO = SinaWeibo.Name

        //复制链接
        val ALIAS_COPY_URL = "copyUrl"
    }

    fun shareText(text: String) {
        val shareDialog = getShareDialog(Platform.SHARE_TEXT)
        shareDialog.setShareInfo(share_text = text);
        shareDialog.show()
    }

    fun shareUrl(
        needDialog: Boolean = true,
        title: String? = "铁粉生活",
        text: String?,
        url: String?,
        imageUrl: String?,
        alias: String? = null,
    ) {
        if (url.isNullOrBlank()) {
            return
        }
        val xInstallUrl = if (url.contains("?")) {
            url + "&inviteId=${UserManager.getLoginUser()?.invitationCode}"
        } else {
            url + "?inviteId=${UserManager.getLoginUser()?.invitationCode}"
        }

        Logger.d("分享地址${xInstallUrl}")
        if (needDialog) {
            shareList = getDefaultShareList(Platform.SHARE_WEBPAGE)
            shareList?.add(ShareBean(Platform.SHARE_WEBPAGE, "复制链接", R.drawable.icon_link_copy, ALIAS_COPY_URL))
            val shareDialog = getShareDialog(Platform.SHARE_WEBPAGE)
            shareDialog.setShareInfo(
                share_title = title,
                share_text = text,
                share_url = xInstallUrl,
                share_image_url = imageUrl
            )
            shareDialog.show()
        } else {
            if (alias != null) {
                ShareUtils(context).share(
                    alias = alias,
                    shareType = Platform.SHARE_WEBPAGE,
                    share_title = title,
                    share_text = text,
                    share_url = xInstallUrl,
                    share_image_url = imageUrl
                )
            }
        }
    }

    fun shareImage(
        needDialog: Boolean = true,
        alias: String? = null,
        imageUrl: String?,

        ) {
        if (imageUrl.isNullOrBlank()) {
            return
        }
        if (needDialog) {
            val shareDialog = getShareDialog(Platform.SHARE_IMAGE)
            shareDialog.setShareInfo(share_image_url = imageUrl);
            shareDialog.show()
        } else {
            if (alias != null) {
                ShareUtils(context).share(
                    alias = alias,
                    shareType = Platform.SHARE_IMAGE,
                    share_image_url = imageUrl
                )
            }
        }

    }

    fun shareImage(
        needDialog: Boolean = true,
        alias: String? = null,
        imageData: Bitmap
    ) {
        if (needDialog) {
            val shareDialog = getShareDialog(Platform.SHARE_IMAGE)
            shareDialog.setShareInfo(share_image_data = imageData)
            shareDialog.show()
        } else {
            if (alias != null) {
                ShareUtils(context).share(
                    alias = alias,
                    shareType = Platform.SHARE_IMAGE,
                    share_image_data = imageData
                )
            }
        }

    }

    fun shareMusic(
        needDialog: Boolean = true,
        alias: String? = null,
        title: String? = "铁粉生活",
        text: String?,
        url: String?,
        musicUrl: String?,
        imageUrl: String?
    ) {
        if (needDialog) {
            val shareDialog = getShareDialog(Platform.SHARE_MUSIC)
            shareDialog.setShareInfo(
                share_title = title,
                share_text = text,
                share_music_url = url,
                music_share_url = musicUrl,
                share_image_url = imageUrl
            )
            shareDialog.show()
        } else {
            if (alias != null) {
                ShareUtils(context).share(
                    alias = alias,
                    share_title = title,
                    share_text = text,
                    shareType = Platform.SHARE_MUSIC,
                    share_music_url = url,
                    music_share_url = musicUrl,
                    share_image_url = imageUrl
                )
            }
        }

    }

    fun shareVideo( needDialog: Boolean = true,
                    alias: String? = null,
                    title: String? = "铁粉生活",
                    text: String?,
                    url: String?,
                    imageUrl: String?) {
        if (needDialog) {
            val shareDialog = getShareDialog(Platform.SHARE_VIDEO)
            shareDialog.setShareInfo(
                share_title = title,
                share_text = text,
                share_video_url = url,
                share_image_url = imageUrl
            )
            shareDialog.show()
        } else {
            if (alias != null) {
                ShareUtils(context).share(
                    alias = alias,
                    share_title = title,
                    share_text = text,
                    shareType = Platform.SHARE_VIDEO,
                    share_video_url = url,
                    share_image_url = imageUrl
                )
            }
        }
    }

    private fun getShareDialog(shareType: Int): ShareDialog {
        return shareList?.let {
            ShareDialog(
                context,
                it
            )
        } ?: ShareDialog(
            context,
            getDefaultShareList(shareType)
        )
    }

    private fun getDefaultShareList(shareType: Int): MutableList<ShareBean> {
        return mutableListOf(
            ShareBean(shareType, "微信", R.drawable.icon_share_weixin, Wechat.Name),
            ShareBean(shareType, "微信朋友圈", R.drawable.icon_share_wc_firend, WechatMoments.Name),
            ShareBean(shareType, "微信收藏", R.drawable.icon_share_wc_collection, WechatFavorite.Name),
            ShareBean(shareType, "微博", R.drawable.icon_share_sina, SinaWeibo.Name)
        )
    }

}
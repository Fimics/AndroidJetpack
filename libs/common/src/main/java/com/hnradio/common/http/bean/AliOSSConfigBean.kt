package com.hnradio.common.http.bean

import androidx.annotation.Keep

/**
 *  ali oss 相关配置
 * created by qiaoyan on 2021/8/3
 */
@Keep
data class AliOSSConfigBean(
    val accessKeyId: String,
    val securityToken: String,
    val accessKeySecret: String,
    val bucketName: String,
    val endpoint: String,
    val regionId: String,
    val domain: String,
    val expiration: String
)

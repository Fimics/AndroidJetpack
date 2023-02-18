package com.hnradio.common.http.bean

/**
 *
 * @Description: java类作用描述
 * @Author: huqiang
 * @CreateDate: 2021-08-23 11:07
 * @Version: 1.0
 */
data class AppVersionBean(
    val createTime: String,
    val downloadUrl: String,
    val id: Int,
    val isNeed: Int,
    val isPass: Int,
    val minVersion: Int,
    val platfrom: Int,
    val version: String,
    val versionNum: Int,
    val describ: String,
    val fileMd5: String
)
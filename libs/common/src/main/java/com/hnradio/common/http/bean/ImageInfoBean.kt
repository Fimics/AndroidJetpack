package com.hnradio.common.http.bean

data class ImageInfoBean(
    val FileSize: OSSInfo,
    val Format: OSSInfo,
    val ImageHeight: OSSInfo,
    val ImageWidth: OSSInfo
)

class OSSInfo(
    val value: String
)

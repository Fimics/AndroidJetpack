package com.yingding.lib_net.bean

import androidx.annotation.Keep

/**
 *
 * @property code Int (0:成功，-1:未初始，-2:录音太短啦，-3:录音失败 ,-5:权限未开启，-4:语音转换失败)
 * @property msg String? 错误提示
 * @property filePath String? 文件路径
 * @property translateStr String? 转换文字
 * @constructor
 */
@Keep
data class JsResRecorderBean (
    var code: Int = 0,
    var msg: String? = null,
    var filePath: String? = null,
    var translateStr: String? = null
)

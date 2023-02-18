package com.hnradio.common.util.bdaudio

import com.google.gson.JsonArray

data class BdAccessTokenBean(
    val refresh_token : String,
    val expires_in : Int,
    val session_key : String,
    val access_token : String,
)

data class BdAccessTokenErrorBean(
    val error : String,
    val error_description : String,
)

//{
//    "log_id": 12345678,
//    "task_status": "Created"，
//    "task_id":  "234acb234acb234acb234acb"  #注意保存该id，用于后续请求识别结果
//}

data class BdTaskBean(
    val log_id : Int,
    val task_status : String,
    val task_id : String,
)

//{
//    "error_code": 336203,
//    "error_msg": "missing param: speech_url",
//    "log_id": 5414433131138366128
//}
data class BdTaskErrorBean(
    val error_code : Int,
    val error_msg : String,
    val log_id : Long,
)

//{
//    "log_id": 12345678,
//    "tasks_info": [
//    { # 转写中
//        "task_status": "Running"
//        "task_id": "234acb234acb234acb234acb",
//    },
//    { # 转写失败
//        "task_status": "Failure"
//        "task_id": "234acb234acb234acb234acd",
//        "task_result": {
//        "err_no":  3301
//        "err_msg": "speech quality error",
//        "sn": "xxx"
//    }
//    },
//    { # 转写成功
//        "task_status": "Success",
//        "task_result": {
//        "result": [
//        "观众朋友大家好，欢迎收看本期视频哦。毕竟..."
//        ],
//        "audio_duration": 6800,
//        "detailed_result": [
//        {
//            "res": [
//            "观众朋友大家好，欢迎收看本期视频哦。"
//            ],
//            "end_time": 6700,
//            "begin_time": 4240,
//            "words_info": [],
//            "sn": "257826606251573543780",
//            "corpus_no": "6758319075297447880"
//        }
//        ...
//        ],
//        "corpus_no": "6758319075297447880"
//    },
//        "task_id": "234acb234acb234acb234ace"
//    }
//    ]
//}
data class BdResultBean<T>(
    val log_id : Int,
    val tasks_info : MutableList<T>
)

data class BdRGoing(
    val task_status : String,
    val task_id : String
)

data class Wrapper<T>(
    val task_status : String,
    val task_id : String,
    val task_result : T
)

data class BdRError(
    val err_no : Int,
    val err_msg : String,
    val sn : String,
)

data class BdRSuccess(
    val audio_duration : Int,
    val result : JsonArray
)


data class BdRGoingBean(
    val log_id : Int,
    val tasks_info : MutableList<BdRGoing>
)

data class BdRErrorBean(
    val log_id : Int,
    val tasks_info : MutableList<Wrapper<BdRError>>
)

data class BdRSuccessBean(
    val log_id : Int,
    val tasks_info : MutableList<Wrapper<BdRSuccess>>
)

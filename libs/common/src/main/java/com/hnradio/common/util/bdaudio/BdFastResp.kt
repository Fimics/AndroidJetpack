package com.hnradio.common.util.bdaudio

import com.google.gson.JsonArray

/**
 * @author ytf
 * Created by on 2021/11/03 17:46
 */
class BdFastSuccessBean(
    var err_no : String,
    var err_msg : String,
    var corpus_no : String,
    var sn : String,
    var result : JsonArray,
)
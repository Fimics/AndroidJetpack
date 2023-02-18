package com.hnradio.common.ktx

import com.yingding.lib_net.easy.GsonKit
import org.json.JSONObject

inline fun <reified T> JSONObject.string(key : String) : T? {
    return if(has(key)){
        getString(key) as T
    }else{
        "" as T
    }
}

inline fun <reified T> JSONObject.int(key : String) : T? {
    return if(has(key)){
        getInt(key) as T
    }else{
        null
    }
}

inline fun <reified T> JSONObject.long(key : String) : T? {
    return if(has(key)){
        getLong(key) as T
    }else{
        null
    }
}

inline fun <reified T> JSONObject.float(key : String) : T?  {
    return if(has(key)){
        getDouble(key) as T
    }else{
        null
    }
}

inline fun <reified T> JSONObject.bool(key : String) : T? {
    return if(has(key)){
        getBoolean(key) as T
    }else{
        null
    }
}

inline fun <reified T> JSONObject.model() : T?{
    return GsonKit.singleGson.fromJson(toString(), T::class.java)
}

inline fun <reified T> JSONObject.model(key : String) : T?{
    if(has(key)){
        val json = getJSONObject(key).toString()
        return GsonKit.singleGson.fromJson(json, T::class.java)
    }
    return null
}
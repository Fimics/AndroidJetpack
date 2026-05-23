package com.mic.libcore.http

import com.google.gson.JsonObject
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class NXRequestBody{

    companion object{

        private val mediaType:MediaType = "application/json; charset=utf-8".toMediaTypeOrNull()!!
        @JvmStatic
        fun create(map: Map<String, String>): RequestBody {
            val jsonObject = JsonObject()
            map.forEach { (k, v) ->
                    jsonObject.addProperty(k, v)
            }
            val json: String = jsonObject.toString()
            return json.toRequestBody(mediaType)
        }

        @JvmStatic
        fun createWithJson(json:String):RequestBody{
            return json.toRequestBody(mediaType)
        }
    }
}
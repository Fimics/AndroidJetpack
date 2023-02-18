package com.yingding.lib_net.easy;

import android.text.TextUtils
import com.google.gson.*
import java.lang.reflect.Type

/**
 * Created by luogui on 2016/12/30.
 */

object GsonKit {


    class StringConverter : JsonSerializer<String>, JsonDeserializer<String> {
        override fun serialize(
            src: String?, typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement {
            return if (src == null) {
                JsonPrimitive("")
            } else {
                JsonPrimitive(src)
            }
        }

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement, typeOfT: Type,
            context: JsonDeserializationContext
        ): String {
            return json.asJsonPrimitive.asString
        }
    }

    class IntegerDefault0Adapter : JsonSerializer<Int>, JsonDeserializer<Int> {
        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Int? {
            try {
                if (json.asString == "" || json.asString == "null") {//定义为int类型,如果后台返回""或者null,则返回0
                    return 0
                }
            } catch (ignore: Exception) {
            }

            try {
                return json.asInt
            } catch (e: NumberFormatException) {
                throw JsonSyntaxException(e)
            }

        }

        override fun serialize(
            src: Int?,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement {
            return JsonPrimitive(src!!)
        }
    }

    class DoubleDefault0Adapter : JsonSerializer<Double>, JsonDeserializer<Double> {
        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Double? {
            try {
                if (json.asString == "" || json.asString == "null") {//定义为double类型,如果后台返回""或者null,则返回0.00
                    return 0.00
                }
            } catch (ignore: Exception) {
            }

            try {
                return json.asDouble
            } catch (e: NumberFormatException) {
                throw JsonSyntaxException(e)
            }

        }

        override fun serialize(
            src: Double?,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement {
            return JsonPrimitive(src!!)
        }
    }

    class FloatDefault0Adapter : JsonSerializer<Float>, JsonDeserializer<Float> {
        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Float? {
            try {
                if (json.asString == "" || json.asString == "null") {//定义为double类型,如果后台返回""或者null,则返回0.00
                    return 0.00f
                }
            } catch (ignore: Exception) {
            }

            try {
                return json.asFloat
            } catch (e: NumberFormatException) {
                throw JsonSyntaxException(e)
            }

        }

        override fun serialize(
            src: Float?,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement {
            return JsonPrimitive(src!!)
        }
    }

    class LongDefault0Adapter : JsonSerializer<Long>, JsonDeserializer<Long> {
        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Long? {
            try {
                if (json.asString == "" || json.asString == "null") {//定义为long类型,如果后台返回""或者null,则返回0
                    return 0L
                }
            } catch (ignore: Exception) {
            }

            try {
                return json.asLong
            } catch (e: NumberFormatException) {
                throw JsonSyntaxException(e)
            }

        }

        override fun serialize(
            src: Long?,
            typeOfSrc: Type,
            context: JsonSerializationContext
        ): JsonElement {
            return JsonPrimitive(src!!)
        }
    }

    //                .registerTypeAdapterFactory(new NullStringToEmptyAdapterFactory())
    val typeFormatGson
        get() = GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(String::class.java, StringConverter())
            .registerTypeAdapter(Int::class.java, IntegerDefault0Adapter())
            .registerTypeAdapter(Int::class.javaPrimitiveType, IntegerDefault0Adapter())
            .registerTypeAdapter(Long::class.java, LongDefault0Adapter())
            .registerTypeAdapter(Long::class.javaPrimitiveType, LongDefault0Adapter())
            .registerTypeAdapter(Float::class.java, FloatDefault0Adapter())
            .registerTypeAdapter(Float::class.javaPrimitiveType, FloatDefault0Adapter())
            .registerTypeAdapter(Double::class.java, DoubleDefault0Adapter())
            .registerTypeAdapter(Double::class.javaPrimitiveType, DoubleDefault0Adapter())
            .create()

    val singleGson
        get() = Gson()

}

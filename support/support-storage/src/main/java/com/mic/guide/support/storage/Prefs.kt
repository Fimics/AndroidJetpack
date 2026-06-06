package com.mic.guide.support.storage

import android.content.Context
import android.content.SharedPreferences
import com.mic.guide.lib.common.AppGlobals

/**
 * 同步 SharedPreferences 门面（迁移自 libcore `P`，转 Kotlin 单例）。
 *
 * 与 [PreferenceStorage]（DataStore，异步 Flow）互补：
 * - 需要**同步**读写、对接旧逻辑用 [Prefs]；
 * - 需要**响应式** Flow / 协程用 [PreferenceStorage]。
 */
object Prefs {

    private const val SHARE_NAME = "aiguide_prefs"

    const val KEY_RESOLUTION = "resolution"
    const val KEY_AUTO_TO_FRONT = "auto_to_front"

    private val sp: SharedPreferences by lazy {
        AppGlobals.getApplication().getSharedPreferences(SHARE_NAME, Context.MODE_PRIVATE)
    }

    fun putInt(key: String, value: Int) = sp.edit().putInt(key, value).apply()
    fun getInt(key: String, def: Int = 0): Int = sp.getInt(key, def)

    fun putLong(key: String, value: Long) = sp.edit().putLong(key, value).apply()
    fun getLong(key: String, def: Long = 0L): Long = sp.getLong(key, def)

    fun putFloat(key: String, value: Float) = sp.edit().putFloat(key, value).apply()
    fun getFloat(key: String, def: Float = 0f): Float = sp.getFloat(key, def)

    fun putString(key: String, value: String?) = sp.edit().putString(key, value).apply()
    fun getString(key: String, def: String = ""): String = sp.getString(key, def) ?: def

    fun putBoolean(key: String, value: Boolean) = sp.edit().putBoolean(key, value).apply()
    fun getBoolean(key: String, def: Boolean = false): Boolean = sp.getBoolean(key, def)

    fun contains(key: String): Boolean = sp.contains(key)
    fun remove(key: String) = sp.edit().remove(key).apply()
    fun clear() = sp.edit().clear().apply()
}

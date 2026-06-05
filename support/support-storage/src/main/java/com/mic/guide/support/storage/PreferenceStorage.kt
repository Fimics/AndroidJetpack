package com.mic.guide.support.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** 全局唯一 DataStore 实例（按进程持有，避免重复创建抛异常）。 */
private val Context.dataStore by preferencesDataStore(name = "aiguide_prefs")

/**
 * 轻量 KV 存储门面（§9）：封装 Jetpack DataStore（Preferences），提供 Flow 读取 + suspend 写入。
 *
 * 业务/能力实现（如 `module-settings` 的深色模式开关）经本类读写，不直接接触 DataStore API。
 */
class PreferenceStorage(private val context: Context) {

    fun stringFlow(key: String, default: String = ""): Flow<String> =
        context.dataStore.data.map { it[stringPreferencesKey(key)] ?: default }

    fun boolFlow(key: String, default: Boolean = false): Flow<Boolean> =
        context.dataStore.data.map { it[booleanPreferencesKey(key)] ?: default }

    suspend fun putString(key: String, value: String) {
        context.dataStore.edit { it[stringPreferencesKey(key)] = value }
    }

    suspend fun putBool(key: String, value: Boolean) {
        context.dataStore.edit { it[booleanPreferencesKey(key)] = value }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
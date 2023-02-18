package com.mic.jetpack.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mic.libcore.utils.AppGlobals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("setting")

object DataStoreUtils2 {


    suspend fun <V> put(key: String, v: V) {
        putValue(AppGlobals.getApplication(), key, v)
    }

    fun <R> get(key: String, r: R): Flow<R>{
        return getValue(AppGlobals.getApplication(), key, r)
    }


    private suspend fun <V> putValue(context: Context, key: String, v: V) {
        context.dataStore.edit {
            when (v) {
                is Int -> it[intPreferencesKey(key)] = v
                is Long -> it[longPreferencesKey(key)] = v
                is String -> it[stringPreferencesKey(key)] = v
                is Boolean -> it[booleanPreferencesKey(key)] = v
                is Float -> it[floatPreferencesKey(key)] = v
                is Double -> it[doublePreferencesKey(key)] = v
                else -> throw IllegalArgumentException("This type can be saved into DataStore")
            }
        }
    }

    private  fun <R> getValue(context: Context,key: String,default:R):Flow<R>{
        val data = when(default){
            is Int-> getInt(context,key,default)
            is Boolean-> getBoolean(context,key,default)
            else -> throw IllegalArgumentException("This type can be saved into DataStore")
        }
        return data as Flow<R>
    }

    private  fun<Int> getInt(context: Context, key: String, default:Int): Flow<Int> = context.dataStore.data.map {
       val v =it[intPreferencesKey(key)]?:default
        return@map v as Int
    }

    private  fun<Boolean> getBoolean(context: Context, key: String, default:Boolean): Flow<Boolean> = context.dataStore.data.map {
        val v =it[intPreferencesKey(key)]?:default
        return@map v as Boolean
    }

}

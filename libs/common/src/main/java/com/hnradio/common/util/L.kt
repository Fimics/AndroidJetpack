package com.hnradio.common.util

import android.util.Log
import com.hnradio.common.BuildConfig

/**
 * Created by liguangze on 2021/7/23.
 */
object L {
    private val TAG = "lg"
    private val DEBUG: Boolean = BuildConfig.DEBUG
    private val CANCEL_TAG = false

    fun i(message: String?) {
        if (DEBUG) {
            Log.i(TAG, message.toString())
        }
    }

    fun i(tag: String?, message: String?) {
        if (DEBUG) {
            if (!CANCEL_TAG) {
                Log.i(tag, message.toString())
            } else {
                i(message)
            }
        }
    }

    fun w(message: String?) {
        if (DEBUG) {
            Log.w(TAG, message.toString())
        }
    }

    fun w(tag: String?, message: String?) {
        if (DEBUG) {
            if (!CANCEL_TAG) {
                Log.w(tag, message.toString())
            } else {
                w(message)
            }
        }
    }

    fun e(message: String?) {
        if (DEBUG) {
            Log.e(TAG, message.toString())
        }
    }

    fun e(tag: String?, message: String?) {
        if (DEBUG) {
            if (!CANCEL_TAG) {
                Log.i(tag, message.toString())
            } else {
                e(message)
            }
        }
    }
}
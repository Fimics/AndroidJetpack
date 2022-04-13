package com.mic.di

import android.util.Log
import javax.inject.Inject

class AnalyticsAdapter @Inject constructor() {

    fun test(){
        Log.d("di","AnalyticsAdapter")
    }
}
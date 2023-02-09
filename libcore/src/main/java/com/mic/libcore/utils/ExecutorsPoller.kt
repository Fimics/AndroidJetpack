package com.mic.libcore.utils

import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class ExecutorsPoller {

    companion object{

        private val scheduledExecutor:ScheduledExecutorService = Executors.newScheduledThreadPool(2)
        private val list:MutableList<TimerTask> = ArrayList()

        fun poll(task:TimerTask){
            list.add(task)
            scheduledExecutor.scheduleAtFixedRate(task,3000,3000,TimeUnit.MILLISECONDS)
        }

        fun shutdown(){
            list.clear()
            scheduledExecutor.shutdown()
        }
    }
}
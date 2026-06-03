package com.mic.jetpack.workmanager

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mic.libcore.utils.KLog

class WorkTask(context: Context, var workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val tag = "work-task"

    @SuppressLint("RestrictedApi")
    override fun doWork(): Result {
        KLog.d(tag,"do work")

        // 接收Activity传递过来的数据
        val dataString = workerParams.inputData.getString("data")
        KLog.d(tag,"data string ->$dataString")

        // 反馈数据 给 Activity
        // 把任务中的数据回传到activity中,数据限制10K
        val result:Data = Data.Builder().putString("data","i'm work-task").build()
        val success:Result.Success = Result.Success(result)

       // return Result.Failure() // 本地执行 doWork 任务时 失败
        //上传，work必须要有网络，上传一半失败，
        // return new Result.Retry(); // 本地执行 doWork 任务时 重试
        // return new Result.Success(); // 本地执行 doWork 任务时 成功 执行任务完毕

        return success
    }

}
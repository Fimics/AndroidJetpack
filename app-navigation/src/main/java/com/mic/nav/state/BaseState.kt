package com.noetix.robotics.state

import com.noetix.libcore.utils.KLog
import com.noetix.libcore.utils.TaskExecutors

open class BaseState : IState {


    override fun enter() {
        KLog.d(TAG, "[${this::class.java.simpleName} ]  enter")
        TaskExecutors.get().mainHandler.postDelayed({
//            ISDKManager.get().resetMotorAndAngles()
//            EmotionDefault.getInstance().toDefault()
        }, 2000)
    }

    override fun execute() {
        KLog.d(TAG, "[${this::class.java.simpleName} ]  execute")
    }

    override fun exit() {
        KLog.d(TAG, "[${this::class.java.simpleName} ]  exit")
    }

    companion object {
        private const val TAG = "BaseState"
    }
}

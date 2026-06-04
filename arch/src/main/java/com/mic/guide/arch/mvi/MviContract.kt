package com.mic.guide.arch.mvi

/** 用户意图（Intent）标记接口：描述「用户想做什么」。 */
interface MviIntent

/** UI 状态标记接口：应为不可变数据类，描述「界面当前是什么样」。 */
interface MviState

/** 一次性副作用标记接口：如 Toast、导航、弹窗，仅消费一次。 */
interface MviEffect
